package com.scb.rider.kafka;

import com.scb.rider.exception.RiderJobStatusException;
import com.scb.rider.model.RiderJobStatusEventModel;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.concurrent.ExecutionException;

import static com.scb.rider.constants.Constants.RIDER_JOB_STATUS_FAILED_CODE;

@Service
@Log4j2
public class KafkaPublisher {

    private KafkaTemplate<String, RiderJobStatusEventModel> kafkaTemplate;

    private String topic;

    @Autowired
    public KafkaPublisher(KafkaTemplate<String, RiderJobStatusEventModel> kafkaTemplate,
                          @Value("${kafka.rider-status-topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    //THROWS DECLARATION TO BE REMOVED :: CASCADES UPWARDS IN THE PROJECT
    public void publish(RiderJobStatusEventModel data) throws InterruptedException, ExecutionException {
        log.info("sending data to topic ='{}'", topic);
        String key = data.getJobId();
        ListenableFuture<SendResult<String, RiderJobStatusEventModel>> listenableFuture = kafkaTemplate.send(topic, key, data);
        listenableFuture.addCallback(callback());
        log.info("Message sent successfully for riderId:{}", data.getRiderId());
    }

    private ListenableFutureCallback<? super SendResult<String, RiderJobStatusEventModel>> callback() {
        return new ListenableFutureCallback<SendResult<String, RiderJobStatusEventModel>>() {

            @Override
            public void onSuccess(SendResult<String, RiderJobStatusEventModel> result) {
                log.info("Message published successfully");
            }

            @Override
            public void onFailure(Throwable ex) {
                log.error("Error while publishing message.", ex);
                throw new RiderJobStatusException(RIDER_JOB_STATUS_FAILED_CODE, "Failed getting rider job status");
            }

        };
    }
}
