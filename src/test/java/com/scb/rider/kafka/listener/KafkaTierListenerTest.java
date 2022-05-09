package com.scb.rider.kafka.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.scb.rider.exception.DataNotFoundException;
import com.scb.rider.exception.KafkaReadException;
import com.scb.rider.service.document.RiderProfileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KafkaTierListenerTest {


    @Mock
    private RiderProfileService riderProfileService;

    @InjectMocks
    private KafkaTierListener kafkaTierListener;

    @Test
    void consumeTest() throws JsonProcessingException {
        String message = "sample message";
        kafkaTierListener.consume(message);
        verify(riderProfileService, times(1))
                .processKafkaTopic(anyString());
    }
}
