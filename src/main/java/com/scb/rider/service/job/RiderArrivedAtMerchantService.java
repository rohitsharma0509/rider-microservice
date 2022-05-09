package com.scb.rider.service.job;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import com.scb.rider.client.JobServiceFeignClient;
import com.scb.rider.exception.FoodAlreadyDeliveredException;
import com.scb.rider.exception.InvalidStateTransitionException;
import com.scb.rider.model.dto.JobDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.scb.rider.exception.DataNotFoundException;
import com.scb.rider.exception.JobCancelledByOperatorException;
import com.scb.rider.kafka.KafkaPublisher;
import com.scb.rider.model.RiderJobStatusEventModel;
import com.scb.rider.model.document.RiderJobDetails;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.enumeration.CancellationSource;
import com.scb.rider.model.enumeration.RiderJobStatus;
import com.scb.rider.repository.RiderJobDetailsRepository;
import com.scb.rider.repository.RiderProfileRepository;
import com.scb.rider.util.DateFormatterUtils;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class RiderArrivedAtMerchantService implements RiderJobService {

    @Autowired
    private RiderJobDetailsRepository riderJobDetailsRepository;

    @Autowired
    private KafkaPublisher kafkaPublisher;

    @Autowired
    private RiderProfileRepository riderProfileRepository;

    @Autowired
    private JobServiceFeignClient jobServiceFeignClient;

    @Override
    public RiderJobStatus getStatusType() {
        return RiderJobStatus.ARRIVED_AT_MERCHANT;
    }

    @Override
    public RiderJobDetails performActionRiderJobStatus(MultipartFile file, String profileId, String jobId,
                                                       BigDecimal fee, BigDecimal jobPrice, Boolean isJobPriceModified, String remark, CancellationSource source, LocalDateTime timeStamp, String updatedBy) {

        RiderProfile riderProfile = riderProfileRepository.findById(profileId)
                .orElseThrow(() -> new DataNotFoundException("Record not found for id " + profileId));

        RiderJobDetails jobDetail = riderJobDetailsRepository.findByJobIdAndProfileId(jobId, profileId)
                .orElseThrow(() -> new DataNotFoundException("Record not found for id " + jobId));

        if (getStatusType().equals(jobDetail.getJobStatus())) {
            log.info("Same-Status-Job-Request ARRIVED_AT_MERCHANT for RiderID-{}, JobID-{}", jobDetail.getProfileId(), jobDetail.getJobId());
            return jobDetail;
        }
        if (isJobConfirmFromMerchant(jobId)) {
            if (RiderJobStatus.ORDER_CANCELLED_BY_OPERATOR.equals(jobDetail.getJobStatus())) {
                throw new JobCancelledByOperatorException("Order is cancelled for the job id" + jobId);
            }
            if (RiderJobStatus.FOOD_DELIVERED.equals(jobDetail.getJobStatus())) {
                throw new FoodAlreadyDeliveredException("Food already delivered for job id:" + jobId);
            }
        } else {
            validateStateTransition(RiderJobStatus.CALLED_MERCHANT, jobDetail, jobId);
        }
        jobDetail.setJobStatus(RiderJobStatus.ARRIVED_AT_MERCHANT);
        LocalDateTime arrivedAtMerchantTime = Objects.nonNull(timeStamp) ? timeStamp : LocalDateTime.now();
        jobDetail.setArrivedAtMerchantTime(arrivedAtMerchantTime);

        if (remark != null)
            jobDetail.setRemarks(remark);

        jobDetail = riderJobDetailsRepository.save(jobDetail);

        try {
            kafkaPublisher.publish(new RiderJobStatusEventModel(profileId, jobId,
                    DateFormatterUtils.zonedDateTimeToString(arrivedAtMerchantTime.atZone(ZoneOffset.UTC)), RiderJobStatus.ARRIVED_AT_MERCHANT,
                    riderProfile.getRiderId()));
        } catch (ExecutionException | InterruptedException e) {
            log.error("Exception while sending message to queue {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
        return jobDetail;
    }

    private boolean isJobConfirmFromMerchant(String jobId) {
        try {
            ResponseEntity<JobDetails> jobDetails = jobServiceFeignClient.getJobByJobId(jobId);
            log.info("Status {} and Job Details {} ", jobDetails.getStatusCode(), jobDetails.getBody().toString() );
            return HttpStatus.OK.equals(jobDetails.getStatusCode()) ? jobDetails.getBody().isMerchantConfirm() :false;
        } catch (Exception e) {
            log.info("Exception Occurs while fetching Job {}", e.getCause());
            return false;
        }
    }
}
