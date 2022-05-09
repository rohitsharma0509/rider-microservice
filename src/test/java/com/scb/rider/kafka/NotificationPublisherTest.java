package com.scb.rider.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.rider.constants.Constants;
import com.scb.rider.exception.NotificationPublishException;
import com.scb.rider.model.document.RiderDeviceDetails;
import com.scb.rider.model.enumeration.Platform;
import com.scb.rider.model.kafka.BroadcastNotification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationPublisherTest {

    private static final String JOB_ID = "123";

    @Mock
    private KafkaTemplate<String, BroadcastNotification> kafkaTemplate;

    private NotificationPublisher notificationPublisher;

    @BeforeEach
    public void setup() {
        notificationPublisher = new NotificationPublisher(kafkaTemplate, "topic", new ObjectMapper());
    }

    @Test
    void testSendNotificationForAndroid() {
        ListenableFuture mockFuture = Mockito.mock(ListenableFuture.class);
        when(kafkaTemplate.send(any(Message.class))).thenReturn(mockFuture);
        RiderDeviceDetails deviceDetails = RiderDeviceDetails.builder().platform(Platform.GCM).build();
        notificationPublisher.sendNotification(deviceDetails, JOB_ID, Constants.JOB_CANCELLED, Constants.JOB_CANCELLED, Constants.JOB_CANCELLED);
        verify(kafkaTemplate, times(1)).send(any(Message.class));
    }

    @Test
    void testSendNotificationForIos() {
        ListenableFuture mockFuture = Mockito.mock(ListenableFuture.class);
        when(kafkaTemplate.send(any(Message.class))).thenReturn(mockFuture);
        RiderDeviceDetails deviceDetails = RiderDeviceDetails.builder().platform(Platform.APNS).build();
        notificationPublisher.sendNotification(deviceDetails, JOB_ID, Constants.JOB_CANCELLED, Constants.JOB_CANCELLED, Constants.JOB_CANCELLED);
        verify(kafkaTemplate, times(1)).send(any(Message.class));
    }

    @Test
    void testSend() {
        ListenableFuture mockFuture = Mockito.mock(ListenableFuture.class);
        BroadcastNotification jobBroadcastNotification = BroadcastNotification.builder().payload("android payload").build();
        when(kafkaTemplate.send(any(Message.class))).thenReturn(mockFuture);
        notificationPublisher.send(jobBroadcastNotification);
        verify(kafkaTemplate, times(1)).send(any(Message.class));
    }

    @Test
    void testPublishFailedTest() throws ExecutionException, InterruptedException {

        BroadcastNotification jobBroadcastNotification = BroadcastNotification.builder().payload("android payload").build();
        ListenableFuture mockFuture = Mockito.mock(ListenableFuture.class);
        when(kafkaTemplate.send(any(Message.class))).thenReturn(mockFuture);
        doThrow(new InterruptedException()).when(mockFuture).get();
        assertThrows(NotificationPublishException.class,
                () -> notificationPublisher.send(jobBroadcastNotification));


    }
}
