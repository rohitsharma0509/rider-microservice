package com.scb.rider.service.job;

import com.scb.rider.constants.DocumentType;
import com.scb.rider.exception.*;
import com.scb.rider.model.RiderJobStatusEventModel;
import com.scb.rider.model.document.RiderJobDetails;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.enumeration.CancellationSource;
import com.scb.rider.model.enumeration.RiderJobStatus;
import com.scb.rider.repository.RiderJobDetailsRepository;
import com.scb.rider.repository.RiderProfileRepository;
import com.scb.rider.service.AmazonS3ImageService;
import com.scb.rider.service.ExcessiveWaitPaymentService;
import com.scb.rider.util.DateFormatterUtils;
import com.scb.rider.kafka.KafkaPublisher;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

@Log4j2
@Component
public class RiderMealPickedUpService implements RiderJobService {

    @Autowired
    private RiderJobDetailsRepository riderJobDetailsRepository;

    @Autowired
    private AmazonS3ImageService amazonS3ImageService;

    @Autowired
    private RiderProfileRepository riderProfileRepository;

    @Autowired
    private KafkaPublisher kafkaPublisher;

    @Autowired
    private ExcessiveWaitPaymentService excessiveWaitPaymentService;

    @Override
    public RiderJobStatus getStatusType() {
        return RiderJobStatus.MEAL_PICKED_UP;
    }

    @Override
    public RiderJobDetails performActionRiderJobStatus(MultipartFile file, String profileId, String jobId,
                                                       BigDecimal fee, BigDecimal jobPrice, Boolean isJobPriceModified, String remark, CancellationSource source, LocalDateTime timeStamp, String updatedBy) {

        RiderProfile riderProfile = riderProfileRepository.findById(profileId)
                .orElseThrow(() -> new DataNotFoundException("Record not found for id " + profileId));

        RiderJobDetails jobDetail = riderJobDetailsRepository.findByJobIdAndProfileId(jobId, profileId)
                .orElseThrow(() -> new DataNotFoundException("Record not found for id " + jobId));

        if(getStatusType().equals(jobDetail.getJobStatus())) {
          log.info("Same-Status-Job-Request MEAL_PICKED_UP for RiderID-{}, JobID-{}", jobDetail.getProfileId(), jobDetail.getJobId());
          return jobDetail;
        }
        
        validateStateTransition(RiderJobStatus.ARRIVED_AT_MERCHANT,jobDetail,jobId);

        String imageUrl = StringUtils.EMPTY;
        if (file != null) {
            try {
                imageUrl = amazonS3ImageService.uploadMultipartFile(file, profileId, DocumentType.MEAL_PICKUP_PHOTO);
                jobDetail.setMealPhotoUrl(imageUrl);
            } catch (FileConversionException | InvalidImageExtensionException e) {
                log.error("Exception occurs While File Uploading {}", e.getMessage());
            }
        }

        jobDetail.setJobStatus(RiderJobStatus.MEAL_PICKED_UP);
        LocalDateTime mealPickedUpTime = Objects.nonNull(timeStamp) ? timeStamp : LocalDateTime.now();
        jobDetail.setMealPickedUpTime(mealPickedUpTime);

        if (remark != null)
            jobDetail.setRemarks(remark);

        jobDetail = riderJobDetailsRepository.save(jobDetail);

        excessiveWaitPaymentService.checkAndPayEwtAmount(jobDetail.getJobId(), jobDetail.getArrivedAtMerchantTime(),
                jobDetail.getMealPickedUpTime(), riderProfile);
        try {
            kafkaPublisher.publish(new RiderJobStatusEventModel(profileId, jobId,
                    DateFormatterUtils.zonedDateTimeToString(mealPickedUpTime.atZone(ZoneOffset.UTC)),
                    RiderJobStatus.MEAL_PICKED_UP, riderProfile.getRiderId(), imageUrl));
        } catch (ExecutionException | InterruptedException e) {
            log.error("Exception while sending message to queue {}", e.getMessage());
            Thread.currentThread().interrupt();
        }

        return jobDetail;
    }
}
