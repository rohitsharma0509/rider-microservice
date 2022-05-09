package com.scb.rider.kafka;

import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.dto.SmsPayload;
import com.scb.rider.model.enumeration.SmsServiceStatus;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Log4j2
@Service
public class SmsPublisher {

    private KafkaTemplate<String, SmsPayload> kafkaTemplate;
    private String topic;

    @Value("${rider.sms-service.status}")
    private String smsServiceStatus;

    @Autowired
    public SmsPublisher(KafkaTemplate<String, SmsPayload> kafkaTemplate,
                        @Value("${kafka.sms-topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void sendSmsNotificationEvent(RiderProfile riderProfile, String message) {
        try {
            if (canPublishSmsEvent(riderProfile.getPhoneNumber())) {
                String fullMobileNumber = riderProfile.getCountryCode().concat(riderProfile.getPhoneNumber());
                SmsPayload smsPayload = SmsPayload.of(fullMobileNumber, message);
                publish(smsPayload, riderProfile.getId());
            }
        } catch (Exception e) {
            log.error("Error while sending sms notification", e);
        }
    }

    public void publish(SmsPayload smsPayload, String key) {
        log.info("Publishing event on topic='{}', with key {}, mobileNumber {}", topic, key, smsPayload.getMobileNumber());
        ListenableFuture<SendResult<String, SmsPayload>> future = kafkaTemplate.send(topic, key, smsPayload);
        future.addCallback(new ListenableFutureCallback<SendResult<String, SmsPayload>>() {
            @Override
            public void onSuccess(SendResult<String, SmsPayload> result) {
                log.info("SMS Notification event published for key {}, offset {}", key, result.getRecordMetadata().offset());
            }

            @Override
            public void onFailure(Throwable ex) {
                log.info("Failed to published SMS Notification event for key {}, exception {}", key, ex.getMessage());
            }
        });
    }

    private boolean canPublishSmsEvent(String phoneNumber) {
        boolean flag = false;
        log.info("SMS service status='{}', mobileNumber length={}", smsServiceStatus, phoneNumber.length());
        if(!SmsServiceStatus.list().contains(smsServiceStatus) //Typo case
                || SmsServiceStatus.ENABLED.name().equals(smsServiceStatus)
                || (SmsServiceStatus.RESTRICTED.name().equals(smsServiceStatus) && phoneNumber.length() == 10)) {
            flag = true;
        }
        return flag;
    }
}
