package com.scb.rider.service;

import com.scb.rider.client.JobServiceFeignClient;
import com.scb.rider.client.OperationFeignClient;
import com.scb.rider.client.PocketServiceFeignClient;
import com.scb.rider.constants.Constants;
import com.scb.rider.constants.ErrorConstants;
import com.scb.rider.kafka.NotificationPublisher;
import com.scb.rider.model.document.RiderDeviceDetails;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.dto.ConfigDataResponse;
import com.scb.rider.model.dto.ExcessiveWaitingTimeDetailsEntity;
import com.scb.rider.model.dto.RiderExcessiveWaitDetailsDto;
import com.scb.rider.repository.RiderDeviceDetailRepository;
import com.scb.rider.util.PropertyUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class ExcessiveWaitPaymentService {

    @Autowired
    private OperationFeignClient operationFeignClient;

    @Autowired
    private PocketServiceFeignClient pocketServiceFeignClient;

    @Autowired
    private JobServiceFeignClient jobServiceFeignClient;

    @Autowired
    private RiderDeviceDetailRepository riderDeviceDetailRepository;

    @Autowired
    private NotificationPublisher notificationPublisher;

    @Autowired
    private PropertyUtils propertyUtils;

    public void checkAndPayEwtAmount(String jobId, LocalDateTime arrivedAtMerchantTime, LocalDateTime currentJobStageTime, RiderProfile riderProfile) {
        if(isJobEligibleForEwtPayment(arrivedAtMerchantTime, currentJobStageTime)) {
            ConfigDataResponse topUp = operationFeignClient.getConfigData(Constants.EXCESSIVE_WAIT_TOPUP_AMOUNT);
            if(Objects.nonNull(topUp) && Objects.nonNull(topUp.getValue()) && Double.parseDouble(topUp.getValue()) > 0) {
                double topUpAmount = Double.parseDouble(topUp.getValue());
                ExcessiveWaitingTimeDetailsEntity ewtDetails = ExcessiveWaitingTimeDetailsEntity.builder()
                        .excessiveWaitTopupAmount(topUpAmount).excessiveWaitTopupDateTime(LocalDateTime.now()).build();
                jobServiceFeignClient.updateEwtAmount(jobId, ewtDetails);
                updatePocketAndSendNotification(jobId, topUpAmount, riderProfile);
            }
        }
    }

    public boolean isJobEligibleForEwtPayment(LocalDateTime arrivedAtMerchantTime, LocalDateTime currentJobStageTime) {
        ConfigDataResponse threshold = operationFeignClient.getConfigData(Constants.THRESHOLD_WAIT_TIME);
        long waitingTimeInSeconds = Duration.between(arrivedAtMerchantTime, currentJobStageTime).getSeconds();
        log.info("Rider waiting time: {}s", waitingTimeInSeconds);
        return Objects.nonNull(threshold) && Objects.nonNull(threshold.getValue()) && waitingTimeInSeconds > (Long.parseLong(threshold.getValue()) * 60);
    }

    @Async
    public void updatePocketAndSendNotification(String jobId, double topUpAmount, RiderProfile riderProfile) {
        log.info("Job {} is eligible for EWT topUp amount of {}", jobId, topUpAmount);
        RiderExcessiveWaitDetailsDto excessiveWaitTimeRequest = RiderExcessiveWaitDetailsDto.builder().riderId(riderProfile.getRiderId())
                .jobId(jobId).topUpAmount(topUpAmount).topupDateTime(LocalDateTime.now()).build();
        pocketServiceFeignClient.addRiderExcessWaitTopup(excessiveWaitTimeRequest);
        Optional<RiderDeviceDetails> deviceDetails = riderDeviceDetailRepository.findByProfileId(riderProfile.getId());
        log.info("Is rider device details present {} with Job {}", deviceDetails.isPresent(), jobId);
        if(deviceDetails.isPresent()) {
            String message = MessageFormat.format(propertyUtils.getProperty(ErrorConstants.LONG_WAIT_PAID_MSG, Locale.forLanguageTag(Constants.THAI)), jobId, topUpAmount);
            notificationPublisher.sendNotification(deviceDetails.get(), jobId, Constants.LONG_WAIT_PAYMENT, Constants.LONG_WAIT_PAYMENT_MSG, message);
        }
    }

}
