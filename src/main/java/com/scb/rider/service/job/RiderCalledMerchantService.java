package com.scb.rider.service.job;

import com.scb.rider.exception.DataNotFoundException;
import com.scb.rider.exception.FoodAlreadyDeliveredException;
import com.scb.rider.exception.InvalidStateTransitionException;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

@Component
@Log4j2
public class RiderCalledMerchantService implements RiderJobService {

    @Autowired
    private RiderJobDetailsRepository riderJobDetailsRepository;

    @Autowired
    private KafkaPublisher kafkaPublisher;

    @Autowired
    private RiderProfileRepository riderProfileRepository;

    @Override
    public RiderJobStatus getStatusType() {
        return RiderJobStatus.CALLED_MERCHANT;
    }

    @Override
    public RiderJobDetails performActionRiderJobStatus(MultipartFile file, String profileId, String jobId,
                                                       BigDecimal fee, BigDecimal jobPrice, Boolean isJobPriceModified, String remark, CancellationSource source, LocalDateTime timeStamp, String updatedBy) {

        RiderProfile riderProfile = riderProfileRepository.findById(profileId)
                .orElseThrow(() -> new DataNotFoundException("Record not found for id " + profileId));

        RiderJobDetails jobDetail = riderJobDetailsRepository.findByJobIdAndProfileId(jobId, profileId)
                .orElseThrow(() -> new DataNotFoundException("Record not found for id " + jobId));
        
        if(getStatusType().equals(jobDetail.getJobStatus())) {
          log.info("Same-Status-Job-Request CALLED_MERCHANT for RiderID-{}, JobID-{}", jobDetail.getProfileId(), jobDetail.getJobId());
          return jobDetail;
        }
        
        validateStateTransition(RiderJobStatus.JOB_ACCEPTED, jobDetail, jobId);

        jobDetail.setJobStatus(RiderJobStatus.CALLED_MERCHANT);
        LocalDateTime calledMerchantTime = Objects.nonNull(timeStamp) ? timeStamp : LocalDateTime.now();
        jobDetail.setCalledMerchantTime(calledMerchantTime);

        if (remark != null)
            jobDetail.setRemarks(remark);

        jobDetail = riderJobDetailsRepository.save(jobDetail);

        try {
            kafkaPublisher.publish(new RiderJobStatusEventModel(profileId, jobId,
                    DateFormatterUtils.zonedDateTimeToString(calledMerchantTime.atZone(ZoneOffset.UTC)),
                    RiderJobStatus.CALLED_MERCHANT, riderProfile.getRiderId()));
        } catch (ExecutionException | InterruptedException e) {
            log.error("Exception while sending message to queue {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
        return jobDetail;
    }
}
