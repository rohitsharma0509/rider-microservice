package com.scb.rider.service.job;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import com.scb.rider.service.ExcessiveWaitPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.rider.client.JobServiceFeignClient;
import com.scb.rider.constants.Constants;
import com.scb.rider.constants.ErrorConstants;
import com.scb.rider.exception.DataNotFoundException;
import com.scb.rider.exception.FoodAlreadyDeliveredException;
import com.scb.rider.exception.JobAlreadyCancelledException;
import com.scb.rider.exception.RiderJobStatusValidationException;
import com.scb.rider.kafka.KafkaPublisher;
import com.scb.rider.kafka.NotificationPublisher;
import com.scb.rider.model.RiderJobStatusEventModel;
import com.scb.rider.model.document.RiderDeviceDetails;
import com.scb.rider.model.document.RiderJobDetails;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.dto.JobDetails;
import com.scb.rider.model.enumeration.AvailabilityStatus;
import com.scb.rider.model.enumeration.CancellationSource;
import com.scb.rider.model.enumeration.Platform;
import com.scb.rider.model.enumeration.RiderJobStatus;
import com.scb.rider.model.kafka.Alert;
import com.scb.rider.model.kafka.AndroidPayload;
import com.scb.rider.model.kafka.Aps;
import com.scb.rider.model.kafka.BroadcastNotification;
import com.scb.rider.model.kafka.IosPayload;
import com.scb.rider.model.kafka.RiderJobCancellationPayload;
import com.scb.rider.repository.RiderDeviceDetailRepository;
import com.scb.rider.repository.RiderJobDetailsRepository;
import com.scb.rider.repository.RiderProfileRepository;
import com.scb.rider.service.document.RiderActiveTrackingZoneService;
import com.scb.rider.util.DateFormatterUtils;
import com.scb.rider.util.PropertyUtils;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
@SuppressWarnings("squid:S3776")
public class RiderJobCancelByOperationService implements RiderJobService {
    @Autowired
    private RiderJobDetailsRepository riderJobDetailsRepository;
    @Autowired
    private RiderProfileRepository riderProfileRepository;
    @Autowired
    private RiderDeviceDetailRepository riderDeviceDetailRepository;

    @Autowired
    private KafkaPublisher kafkaPublisher;
    @Autowired
    private NotificationPublisher notificationPublisher;

    @Autowired
    private RiderActiveTrackingZoneService riderInactiveTrackingService;
    
    @Autowired
    private PropertyUtils propertyUtils;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JobServiceFeignClient jobServiceFeignClient;

    @Autowired
    private ExcessiveWaitPaymentService excessiveWaitPaymentService;
    
    @Override
    public RiderJobStatus getStatusType() {
        return RiderJobStatus.ORDER_CANCELLED_BY_OPERATOR;
    }

    final static String RIDER_NOT_FOUND ="RIDER_NOT_FOUND";
    
    @Override
    public RiderJobDetails performActionRiderJobStatus(MultipartFile file, String profileId, String jobId, BigDecimal fee,
                                                       BigDecimal jobPrice, Boolean isJobPriceModified, String remark, CancellationSource source, LocalDateTime timeStamp, String updatedBy) {

        Optional<RiderJobDetails> optionalJobDetail = riderJobDetailsRepository.findByJobId(jobId);
        RiderJobDetails jobDetail = null;
        
        if (optionalJobDetail.isPresent()) {
            jobDetail = optionalJobDetail.get();
            validateRiderJobStatus(jobDetail);
            updatedBy = Constants.RIDER.equals(updatedBy) ? jobDetail.getUpdatedBy() : updatedBy;
            RiderJobStatus oldStatus = jobDetail.getJobStatus();
            if(jobDetail.getJobStatus().equals(RiderJobStatus.ORDER_CANCELLED_BY_OPERATOR))
            	throw new JobAlreadyCancelledException("Job is already cancelled for the job id :" + jobId);

            if(jobDetail.getJobStatus().equals(RiderJobStatus.FOOD_DELIVERED)){
                throw new FoodAlreadyDeliveredException("Food already delivered for job id:" + jobId );
            }

            RiderProfile riderProfile = riderProfileRepository.findById(jobDetail.getProfileId())
                    .orElseThrow(
                            () -> new DataNotFoundException("Record not found for id " + profileId));

            jobDetail.setJobStatus(RiderJobStatus.ORDER_CANCELLED_BY_OPERATOR);
            LocalDateTime orderCancelledByOperationTime = Objects.nonNull(timeStamp) ? timeStamp : LocalDateTime.now();
            jobDetail.setOrderCancelledByOperationTime(orderCancelledByOperationTime);

            if (remark != null)
                jobDetail.setRemarks(remark);
            jobDetail.setCancellationSource(source);
            jobDetail.setUpdatedBy(updatedBy);
            jobDetail = riderJobDetailsRepository.save(jobDetail);

            // Update Rider Profile to Active Stage to Accept Order
            riderProfile.setAvailabilityStatus(AvailabilityStatus.Active);
            riderProfileRepository.save(riderProfile);
            log.info("RiderActiveTrackingService - Saving Rider Job Cancelled Time Stamp Id:{}", riderProfile.getId());
            riderInactiveTrackingService.saveOrUpdateRiderInactiveStatus(riderProfile);
            if(RiderJobStatus.ARRIVED_AT_MERCHANT.equals(oldStatus)) {
                excessiveWaitPaymentService.checkAndPayEwtAmount(jobId, jobDetail.getArrivedAtMerchantTime(),
                        orderCancelledByOperationTime, riderProfile);
            }
            publishKafka(new RiderJobStatusEventModel(profileId, jobId,
                    DateFormatterUtils.zonedDateTimeToString(orderCancelledByOperationTime.atZone(ZoneOffset.UTC)),
                    RiderJobStatus.ORDER_CANCELLED_BY_OPERATOR, jobPrice, isJobPriceModified, riderProfile.getRiderId(), updatedBy));
            // Notification
            sendNotificationToRider(riderProfile, jobDetail);
        } else {
        	
        	jobDetail = new RiderJobDetails();
            jobDetail.setJobId(jobId);
        	ResponseEntity<JobDetails> jobByJobId = jobServiceFeignClient.getJobByJobId(jobId);
        	
        	if(Optional.ofNullable(jobByJobId.getBody()).isPresent() && jobByJobId.getBody().getJobStatusKey().equalsIgnoreCase(RIDER_NOT_FOUND))
        	{
        		log.info("can't cancel since job status is rider not found for jobId-{}",jobId);
        		return jobDetail;
        	}
            jobDetail.setJobStatus(RiderJobStatus.ORDER_CANCELLED_BY_OPERATOR);
            LocalDateTime orderCancelledByOperationTime = Objects.nonNull(timeStamp) ? timeStamp : LocalDateTime.now();
            jobDetail.setOrderCancelledByOperationTime(orderCancelledByOperationTime);
            if (remark != null)
                jobDetail.setRemarks(remark);
            jobDetail.setCancellationSource(source);
            updatedBy = Constants.RIDER.equals(updatedBy) ? null : updatedBy;
            jobDetail.setUpdatedBy(updatedBy);
            jobDetail = riderJobDetailsRepository.save(jobDetail);
            publishKafka(new RiderJobStatusEventModel(profileId, jobId,
                    DateFormatterUtils.zonedDateTimeToString(orderCancelledByOperationTime.atZone(ZoneOffset.UTC)),
                    RiderJobStatus.ORDER_CANCELLED_BY_OPERATOR, jobPrice, isJobPriceModified, null, updatedBy));
        }
        return jobDetail;
    }

    private void publishKafka(RiderJobStatusEventModel riderJobStatusEventModel) {
        try {
            kafkaPublisher.publish(riderJobStatusEventModel);

        } catch (ExecutionException | InterruptedException e) {
            log.error("Exception while sending message to queue {}", e.getMessage());
            Thread.currentThread().interrupt();
        }

    }

    private void sendNotificationToRider(RiderProfile riderProfile, RiderJobDetails jobDetail) {
        Optional<RiderDeviceDetails> deviceDetails = riderDeviceDetailRepository.findByProfileId(riderProfile.getId());
        if (deviceDetails.isPresent()) {
            notificationPublisher.send(getBroadcastNotification(riderProfile, jobDetail, deviceDetails.get()));
        }

    }

    @SneakyThrows
    private BroadcastNotification getBroadcastNotification(RiderProfile riderProfile, RiderJobDetails jobDetail,
                                                           RiderDeviceDetails deviceDetails) {

        String payload = "";
        // checking if push notification is to be sent to Android device or IOS device
        if (Platform.GCM.equals(deviceDetails.getPlatform())) {
            payload =
                    objectMapper.writeValueAsString(AndroidPayload.builder()
                            .priority(Constants.PRIORITY)
                            .data(getRiderJobCancellationPayload(jobDetail))
                            .build());

        } else if (Platform.APNS.equals(deviceDetails.getPlatform()) ||
                Platform.APNS_SANDBOX.equals(deviceDetails.getPlatform())) {
            payload =
                    objectMapper.writeValueAsString(IosPayload.builder()
                            .aps(
                                    Aps.builder()
                                            .alert(
                                                    Alert.builder()
                                                            .title(Constants.TITLE)
                                                            .body("Job " + jobDetail.getJobId() + "is cancelled")
                                                            .build())
                                            .badge(1)
                                            .sound(Constants.SOUND)
                                            .build())
                            .data(getRiderJobCancellationPayload(jobDetail))
                            .build());
        }

        return BroadcastNotification.builder()
                .arn(deviceDetails.getArn())
                .type(Constants.TYPE)
                .platform(deviceDetails.getPlatform().name())
                .payload(payload)
                .build();
    }

    private RiderJobCancellationPayload getRiderJobCancellationPayload(RiderJobDetails jobDetail) {
        return RiderJobCancellationPayload.builder()
                .jobId(jobDetail.getJobId())
                .type(Constants.JOB_CANCELLED)
                .dateTime(LocalDateTime.now())
                .title(Constants.TITLE)
                .body("Job " + jobDetail.getJobId() + "is cancelled")
                .sound(Constants.SOUND)
                .click_action(Constants.CLICK_ACTION)
                .build();
    }


    private void validateRiderJobStatus(RiderJobDetails jobDetail) {
        if (jobDetail.getJobStatus() == RiderJobStatus.FOOD_DELIVERED) {
            throw new RiderJobStatusValidationException(propertyUtils
                    .getProperty(ErrorConstants.RIDER_JOB_ALREADY_COMPLETED_ERROR));
        }

    }
}
