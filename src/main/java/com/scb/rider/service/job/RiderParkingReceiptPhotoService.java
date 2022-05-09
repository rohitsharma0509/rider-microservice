package com.scb.rider.service.job;

import com.scb.rider.constants.DocumentType;
import com.scb.rider.exception.DataNotFoundException;
import com.scb.rider.exception.FoodAlreadyDeliveredException;
import com.scb.rider.exception.JobCancelledByOperatorException;
import com.scb.rider.model.RiderJobStatusEventModel;
import com.scb.rider.model.document.RiderJobDetails;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.enumeration.CancellationSource;
import com.scb.rider.model.enumeration.RiderJobStatus;
import com.scb.rider.repository.RiderJobDetailsRepository;
import com.scb.rider.repository.RiderProfileRepository;
import com.scb.rider.service.AmazonS3ImageService;
import com.scb.rider.util.DateFormatterUtils;
import com.scb.rider.kafka.KafkaPublisher;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

@Log4j2
@Component
public class RiderParkingReceiptPhotoService implements RiderJobService {
    @Autowired
    private RiderJobDetailsRepository riderJobDetailsRepository;

    @Autowired
    private AmazonS3ImageService amazonS3ImageService;

    @Autowired
    private KafkaPublisher kafkaPublisher;

    @Autowired
    private RiderProfileRepository riderProfileRepository;

    @Override
    public RiderJobStatus getStatusType() {
        return RiderJobStatus.PARKING_RECEIPT_PHOTO;
    }

    @Override
    public RiderJobDetails performActionRiderJobStatus(MultipartFile file, String profileId, String jobId,
                                                       BigDecimal fee, BigDecimal jobPrice, Boolean isJobPriceModified, String remark, CancellationSource source, LocalDateTime timeStamp, String updatedBy) {

        RiderProfile riderProfile = riderProfileRepository.findById(profileId)
                .orElseThrow(() -> new DataNotFoundException("Record not found for id " + profileId));

        RiderJobDetails jobDetail = riderJobDetailsRepository.findByJobIdAndProfileId(jobId, profileId)
                .orElseThrow(() -> new DataNotFoundException("Record not found for id " + jobId));
        
        if(jobDetail.getJobStatus().equals(RiderJobStatus.ORDER_CANCELLED_BY_OPERATOR))
        	throw new JobCancelledByOperatorException("Order is cancelled for the job id" + jobId);

        if(jobDetail.getJobStatus().equals(RiderJobStatus.FOOD_DELIVERED)){
            throw new FoodAlreadyDeliveredException("Food already delivered for job id:" + jobId );
        }
        
        if (file != null) {
            try {
                String imageUrl = amazonS3ImageService.uploadMultipartFile(file, profileId, DocumentType.PARKING_RECIEPT);
                jobDetail.setParkingPhotoUrl(imageUrl);
            } catch (Exception e) {
                log.error("Exception occurs While File Uploading {} ", e.getMessage());
            }
        }

        jobDetail.setJobStatus(RiderJobStatus.PARKING_RECEIPT_PHOTO);
        jobDetail.setParkingFee(fee);
        LocalDateTime parkingReceiptPhotoTime = Objects.nonNull(timeStamp) ? timeStamp : LocalDateTime.now();
        jobDetail.setParkingReceiptPhotoTime(parkingReceiptPhotoTime);

        if (remark != null)
            jobDetail.setRemarks(remark);

        jobDetail = riderJobDetailsRepository.save(jobDetail);

        try {
            kafkaPublisher.publish(new RiderJobStatusEventModel(profileId, jobId,
                    DateFormatterUtils.zonedDateTimeToString(parkingReceiptPhotoTime.atZone(ZoneOffset.UTC)),
                    RiderJobStatus.PARKING_RECEIPT_PHOTO, riderProfile.getRiderId()));
        } catch (ExecutionException | InterruptedException e) {
            log.error("Exception while sending message to queue {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
        return jobDetail;
    }
}

