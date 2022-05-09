package com.scb.rider.kafka.publisher;

import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.kafka.RiderStatusUpdateModel;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.time.LocalDateTime;

@Service
@Log4j2
public class RiderStatusUpdateKafkaPublisher {

  private static final String ACTIVE = "ACTIVE";

  private static final String RIDER_SERVICE = "RIDER_SERVICE";

  private KafkaTemplate<String, RiderStatusUpdateModel> kafkaTemplate;

  private String topic;

  @Autowired
  public RiderStatusUpdateKafkaPublisher(
      KafkaTemplate<String, RiderStatusUpdateModel> kafkaTemplate,
      @Value("${kafka.rider-status-update-topic}") String topic) {
    this.kafkaTemplate = kafkaTemplate;
    this.topic = topic;
  }

  public void publish(RiderProfile riderProfile) {
    try {
      log.info("sending data to topic ='{}'", topic);
      RiderStatusUpdateModel riderStatusUpdateModel =
              RiderStatusUpdateModel.builder().build();
      String key = riderProfile.getRiderId();
      BeanUtils.copyProperties(riderProfile, riderStatusUpdateModel);
      ListenableFuture<SendResult<String, RiderStatusUpdateModel>> listenableFuture =
          kafkaTemplate.send(topic, key, riderStatusUpdateModel);
      listenableFuture.addCallback(callback());
      log.info("Message sent successfully for riderId:{}", riderProfile.getRiderId());
    } catch (Exception ex) {
      log.error("Exception Occurred while publishing Kafka Message of Rider Status Update");
    }

  }

  private ListenableFutureCallback<SendResult<String, RiderStatusUpdateModel>> callback() {
    return new ListenableFutureCallback<SendResult<String, RiderStatusUpdateModel>>() {

      @Override
      public void onSuccess(SendResult<String, RiderStatusUpdateModel> result) {
        log.info("Message published successfully");
      }

      @Override
      public void onFailure(Throwable ex) {
        log.error("Error while publishing message.", ex);
      }
    };
  }
  
  public static String dateTimeToString(LocalDateTime localDateTime) {
    String PATTERN ="yyyy-MM-dd'T'HH:mm:ss.SSS";
    java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern(PATTERN);
    String dateTimeString = localDateTime.format(formatter) + "Z";
    return dateTimeString;
  }
}
