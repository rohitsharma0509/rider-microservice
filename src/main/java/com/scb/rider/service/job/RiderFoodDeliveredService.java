package com.scb.rider.service.job;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import com.scb.rider.service.document.RiderProfileService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.scb.rider.constants.DocumentType;
import com.scb.rider.exception.DataNotFoundException;
import com.scb.rider.exception.FileConversionException;
import com.scb.rider.exception.InvalidImageExtensionException;
import com.scb.rider.kafka.KafkaPublisher;
import com.scb.rider.model.RiderJobStatusEventModel;
import com.scb.rider.model.document.RiderJobDetails;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.enumeration.AvailabilityStatus;
import com.scb.rider.model.enumeration.CancellationSource;
import com.scb.rider.model.enumeration.RiderJobStatus;
import com.scb.rider.repository.RiderJobDetailsRepository;
import com.scb.rider.repository.RiderProfileRepository;
import com.scb.rider.service.AmazonS3ImageService;
import com.scb.rider.service.cache.RiderProfileUpdaterService;
import com.scb.rider.service.document.RiderActiveTrackingZoneService;
import com.scb.rider.util.DateFormatterUtils;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class RiderFoodDeliveredService implements RiderJobService {

    @Autowired
    private RiderJobDetailsRepository riderJobDetailsRepository;

    @Autowired
    private RiderProfileRepository riderProfileRepository;

    @Autowired
    private KafkaPublisher kafkaPublisher;

    @Autowired
    private RiderActiveTrackingZoneService riderInactiveTrackingService;
    
    @Autowired
    private AmazonS3ImageService amazonS3ImageService;

    @Autowired
	private RiderProfileUpdaterService riderProfileUpdaterService;

    @Autowired
    private RiderProfileService riderProfileService;
    
    @Override
    public RiderJobStatus getStatusType() {
        return RiderJobStatus.FOOD_DELIVERED;
    }

    @Override
    public RiderJobDetails performActionRiderJobStatus(MultipartFile file, String profileId, String jobId,
                                                       BigDecimal fee,BigDecimal jobPrice, Boolean isJobPriceModified,  String remark, CancellationSource source, LocalDateTime timeStamp, String updatedBy) {

        RiderProfile riderProfile = riderProfileRepository.findById(profileId)
                .orElseThrow(() -> new DataNotFoundException("Record not found for id " + profileId));

        RiderJobDetails jobDetail = riderJobDetailsRepository.findByJobIdAndProfileId(jobId, profileId)
                .orElseThrow(() -> new DataNotFoundException("Record not found for id " + jobId));

        if(getStatusType().equals(jobDetail.getJobStatus())) {
            log.info("Same-Status-Job-Request FOOD_DELIVERED for RiderID-{}, JobID-{}", jobDetail.getProfileId(), jobDetail.getJobId());
            return jobDetail;
        }

        log.info("Start : Validating state transition for job Id {} for rider {} ", jobId, riderProfile.getRiderId());
        validateStateTransition(RiderJobStatus.ARRIVED_AT_CUST_LOCATION,jobDetail,jobId);
        log.info("End : Validation state transition for job Id {} for rider {} ", jobId, riderProfile.getRiderId());

        jobDetail.setJobStatus(RiderJobStatus.FOOD_DELIVERED);
        LocalDateTime foodDeliveredTime = Objects.nonNull(timeStamp) ? timeStamp : LocalDateTime.now();
        jobDetail.setFoodDeliveredTime(foodDeliveredTime);

        if (remark != null)
            jobDetail.setRemarks(remark);

        String imageUrl = StringUtils.EMPTY;

        if (file != null) {
            try {
                imageUrl = amazonS3ImageService.uploadMultipartFile(file, profileId, DocumentType.FOOD_DELIVERED_PHOTO);
                jobDetail.setMealDeliveredPhotoUrl(imageUrl);
            } catch (FileConversionException | InvalidImageExtensionException e) {
                log.error("Exception occurs While File Uploading {}", e.getMessage());
            }
        }

        jobDetail = riderJobDetailsRepository.save(jobDetail);
        // Update Rider Profile to Active Stage to Accept Order
        log.info("FOOD_DELIVERED for job Id {} by rider {} , updating status to Inactive", jobId, riderProfile.getRiderId());
        riderProfile.setAvailabilityStatus(AvailabilityStatus.Inactive);
        riderProfileRepository.save(riderProfile);
        
        riderProfileUpdaterService.publish(riderProfile);
        
        log.info("RiderActiveTrackingService - Saving Rider Food Delivered Time Stamp Id:{}", riderProfile.getId());
        riderInactiveTrackingService.saveOrUpdateRiderInactiveStatus(riderProfile);
        log.info("Rider status changes successfully after completing the job to {} ", riderProfile.getAvailabilityStatus());

        try {
            log.info("Sending Kafka message to JOB-SERVICE for jobId {} and status {}", jobId, getStatusType() );
            kafkaPublisher.publish(new RiderJobStatusEventModel(profileId, jobId,
                    DateFormatterUtils.zonedDateTimeToString(foodDeliveredTime.atZone(ZoneOffset.UTC)),
                    RiderJobStatus.FOOD_DELIVERED, riderProfile.getRiderId(), imageUrl));
            log.info("Kafka message sent successfully to JOB-SERVICE for jobId {} and status {}", jobId, getStatusType() );
        } catch (ExecutionException | InterruptedException e) {
            log.error("Exception while sending message to queue {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
        return jobDetail;
    }
}