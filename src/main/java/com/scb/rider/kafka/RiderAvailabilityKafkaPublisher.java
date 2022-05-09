package com.scb.rider.kafka;

import static com.scb.rider.constants.Constants.RIDER_JOB_STATUS_FAILED_CODE;
import java.time.LocalDateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import com.scb.rider.exception.RiderJobStatusException;
import com.scb.rider.model.RiderAvailabilityEventModel;
import com.scb.rider.model.document.RiderProfile;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class RiderAvailabilityKafkaPublisher {

  private static final String ACTIVE = "ACTIVE";

  private static final String RIDER_SERVICE = "RIDER_SERVICE";

  private KafkaTemplate<String, RiderAvailabilityEventModel> kafkaTemplate;

  private String topic;

  @Autowired
  public RiderAvailabilityKafkaPublisher(
      KafkaTemplate<String, RiderAvailabilityEventModel> kafkaTemplate,
      @Value("${kafka.rider-availability-topic}") String topic) {
    this.kafkaTemplate = kafkaTemplate;
    this.topic = topic;
  }

  public void publish(RiderProfile riderProfile) {
    try {
      log.info("sending data to topic ='{}'", topic);
      RiderAvailabilityEventModel riderAvailabilityEventModel =
          RiderAvailabilityEventModel.builder().build();
      String key = riderProfile.getRiderId();
      BeanUtils.copyProperties(riderProfile, riderAvailabilityEventModel);
      riderAvailabilityEventModel.setDateTime(dateTimeToString(LocalDateTime.now()));
      riderAvailabilityEventModel.setServiceName(RIDER_SERVICE);
      riderAvailabilityEventModel.setEvent(ACTIVE);
      ListenableFuture<SendResult<String, RiderAvailabilityEventModel>> listenableFuture =
          kafkaTemplate.send(topic, key, riderAvailabilityEventModel);
      listenableFuture.addCallback(callback());
      log.info("Message sent successfully for riderId:{}", riderProfile.getRiderId());
    } catch (Exception ex) {
      log.error("Exception Occurred while publicshing Kafka Message of Rider Availability");
    }

  }

  private ListenableFutureCallback<SendResult<String, RiderAvailabilityEventModel>> callback() {
    return new ListenableFutureCallback<SendResult<String, RiderAvailabilityEventModel>>() {

      @Override
      public void onSuccess(SendResult<String, RiderAvailabilityEventModel> result) {
        log.info("Message published successfully");
      }

      @Override
      public void onFailure(Throwable ex) {
        log.error("Error while publishing message.", ex);
        throw new RiderJobStatusException(RIDER_JOB_STATUS_FAILED_CODE,
            "Failed getting rider job status");
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
