package com.scb.rider.kafka;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.rider.constants.Constants;
import com.scb.rider.model.document.RiderDeviceDetails;
import com.scb.rider.model.enumeration.Platform;
import com.scb.rider.model.kafka.Alert;
import com.scb.rider.model.kafka.AndroidPayload;
import com.scb.rider.model.kafka.Aps;
import com.scb.rider.model.kafka.BroadcastNotification;
import com.scb.rider.model.kafka.IosPayload;
import com.scb.rider.model.kafka.RiderJobCancellationPayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import com.scb.rider.exception.NotificationPublishException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@SuppressWarnings("squid:S2142")
public class NotificationPublisher {

    private KafkaTemplate<String, BroadcastNotification> kafkaTemplate;

    private String topic;

    private ObjectMapper objectMapper;

    @Autowired
    public NotificationPublisher(
            KafkaTemplate<String, BroadcastNotification> kafkaTemplate,
            @Value("${kafka.notification-topic}") String topic, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
        this.objectMapper = objectMapper;
    }

    public void sendNotification(RiderDeviceDetails deviceDetails, String jobId, String type, String title, String body) {
        String payload = "";
        RiderJobCancellationPayload data = RiderJobCancellationPayload.builder()
                .jobId(jobId).type(type).dateTime(LocalDateTime.now()).title(title).body(body)
                .sound(Constants.SOUND).click_action(Constants.CLICK_ACTION).build();
        try {
            // checking if push notification is to be sent to Android device or IOS device
            log.info("sending notification to platform {} for job {}", deviceDetails.getPlatform(), jobId);
            if (Platform.GCM.equals(deviceDetails.getPlatform())) {
                AndroidPayload androidPayload = AndroidPayload.builder().priority(Constants.PRIORITY).data(data).build();
                payload = objectMapper.writeValueAsString(androidPayload);
            } else if (Platform.APNS.equals(deviceDetails.getPlatform()) || Platform.APNS_SANDBOX.equals(deviceDetails.getPlatform())) {
                IosPayload iosPayload = IosPayload.builder().aps(Aps.builder().alert(
                        Alert.builder().title(title).body(body).build()
                ).badge(1).sound(Constants.SOUND).build()).data(data).build();
                payload = objectMapper.writeValueAsString(iosPayload);
            }
        } catch (JsonProcessingException e) {
            log.error("Error while converting notification payload to string", e);
        }
        send(BroadcastNotification.builder().arn(deviceDetails.getArn()).type(type)
                .platform(deviceDetails.getPlatform().name()).payload(payload).build());
    }

    public void send(BroadcastNotification data) {
        log.info("sending data to topic='{}'", topic);

        Message<BroadcastNotification> message =
                MessageBuilder.withPayload(data).setHeader(KafkaHeaders.TOPIC, topic).build();
        try {

            kafkaTemplate.send(message).get();
            log.info("Notification has been  successfully broadcasted for rider");
            
        } catch (InterruptedException | ExecutionException ex) {

            log.error("Exception has been occured while sending the Kafka message");
            throw new NotificationPublishException();
        }
    }
}
