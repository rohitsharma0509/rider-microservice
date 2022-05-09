package com.scb.rider.kafka.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.scb.rider.exception.KafkaReadException;
import com.scb.rider.service.document.RiderProfileService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class KafkaTierListener {



    @Autowired
    private RiderProfileService riderProfileService;

    @KafkaListener(topics = "${kafka.rider-profile-update-topic}")
    public void consume(@Payload String message) {
        try{
            log.info(String.format("Consuming from Kafka topic: %s ", message));
            riderProfileService.processKafkaTopic(message);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            log.info(String.format("Data Consumed Failed from Kafka topic: %s ", message));
            throw new KafkaReadException();
        }

        log.info(String.format("Data Consumed successfully from Kafka topic: %s ", message));
    }
}