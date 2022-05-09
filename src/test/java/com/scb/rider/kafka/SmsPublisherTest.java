package com.scb.rider.kafka;

import com.scb.rider.constants.Constants;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.dto.SmsPayload;
import com.scb.rider.model.enumeration.SmsServiceStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@ExtendWith(MockitoExtension.class)
class SmsPublisherTest {

    private static final int INVOKED_ONCE = 1;
    private static final String TOPIC = "topic";
    private static final String TEST_SMS = "test sms";
    private static final String KEY = "123";
    private static final String VALID_PHONE_NO = "8888888888";
    private static final String INVALID_PHONE_NO = "888888888";

    @Mock
    private KafkaTemplate<String, SmsPayload> kafkaTemplate;

    private SmsPublisher smsPublisher;

    @BeforeEach
    public void setup() {
        smsPublisher = new SmsPublisher(kafkaTemplate, TOPIC);
    }

    @Test
    void shouldSendSmsNotificationEvent() {
        ReflectionTestUtils.setField(smsPublisher, "smsServiceStatus", SmsServiceStatus.ENABLED.name());
        ListenableFuture mockFuture = Mockito.mock(ListenableFuture.class);
        when(kafkaTemplate.send(eq(TOPIC), eq(KEY), any(SmsPayload.class))).thenReturn(mockFuture);
        SendResult<String, Object> sendResult = mock(SendResult.class);
        doAnswer(invocationOnMock -> {
            ListenableFutureCallback listenableFutureCallback = invocationOnMock.getArgument(0);
            listenableFutureCallback.onSuccess(sendResult);
            return null;
        }).when(mockFuture).addCallback(any(ListenableFutureCallback.class));
        smsPublisher.sendSmsNotificationEvent(getRiderProfile(VALID_PHONE_NO), TEST_SMS);
        verify(kafkaTemplate, times(INVOKED_ONCE)).send(eq(TOPIC), eq(KEY), any(SmsPayload.class));
    }

    @Test
    void shouldNotSendSmsNotificationEventWhenExceptionOccurs() {
        ReflectionTestUtils.setField(smsPublisher, "smsServiceStatus", SmsServiceStatus.ENABLED.name());
        when(kafkaTemplate.send(eq(TOPIC), eq(KEY), any(SmsPayload.class))).thenThrow(new NullPointerException());
        smsPublisher.sendSmsNotificationEvent(getRiderProfile(VALID_PHONE_NO), TEST_SMS);
    }

    @Test
    void shouldNotSendSmsNotificationEvent() {
        ReflectionTestUtils.setField(smsPublisher, "smsServiceStatus", SmsServiceStatus.ENABLED.name());
        ListenableFuture mockFuture = Mockito.mock(ListenableFuture.class);
        when(kafkaTemplate.send(eq(TOPIC), eq(KEY), any(SmsPayload.class))).thenReturn(mockFuture);
        Throwable throwable = mock(Throwable.class);
        doAnswer(invocationOnMock -> {
            ListenableFutureCallback listenableFutureCallback = invocationOnMock.getArgument(0);
            listenableFutureCallback.onFailure(throwable);
            return null;
        }).when(mockFuture).addCallback(any(ListenableFutureCallback.class));
        smsPublisher.sendSmsNotificationEvent(getRiderProfile(VALID_PHONE_NO), TEST_SMS);
        verify(kafkaTemplate, times(INVOKED_ONCE)).send(eq(TOPIC), eq(KEY), any(SmsPayload.class));
    }

    @Test
    void shouldNotSendSmsWhenItsDisabled() {
        ReflectionTestUtils.setField(smsPublisher, "smsServiceStatus", SmsServiceStatus.DISABLED.name());
        smsPublisher.sendSmsNotificationEvent(getRiderProfile(VALID_PHONE_NO), TEST_SMS);
        verifyZeroInteractions(kafkaTemplate);
    }

    @Test
    void shouldNotSendSmsWhenItsRestrictedAndPhoneNumberLengthIsLessThan10() {
        ReflectionTestUtils.setField(smsPublisher, "smsServiceStatus", SmsServiceStatus.RESTRICTED.name());
        smsPublisher.sendSmsNotificationEvent(getRiderProfile(INVALID_PHONE_NO), TEST_SMS);
        verifyZeroInteractions(kafkaTemplate);
    }

    @Test
    void shouldSendSmsWhenItsRestrictedAndHaveValidPhoneNumber() {
        ReflectionTestUtils.setField(smsPublisher, "smsServiceStatus", SmsServiceStatus.RESTRICTED.name());
        ListenableFuture mockFuture = Mockito.mock(ListenableFuture.class);
        when(kafkaTemplate.send(eq(TOPIC), eq(KEY), any(SmsPayload.class))).thenReturn(mockFuture);
        SendResult<String, Object> sendResult = mock(SendResult.class);
        doAnswer(invocationOnMock -> {
            ListenableFutureCallback listenableFutureCallback = invocationOnMock.getArgument(0);
            listenableFutureCallback.onSuccess(sendResult);
            return null;
        }).when(mockFuture).addCallback(any(ListenableFutureCallback.class));
        smsPublisher.sendSmsNotificationEvent(getRiderProfile(VALID_PHONE_NO), TEST_SMS);
        verify(kafkaTemplate, times(INVOKED_ONCE)).send(eq(TOPIC), eq(KEY), any(SmsPayload.class));
    }

    @Test
    void shouldSendSmsWhenTypoInStatus() {
        ReflectionTestUtils.setField(smsPublisher, "smsServiceStatus", "DISABLE");
        ListenableFuture mockFuture = Mockito.mock(ListenableFuture.class);
        when(kafkaTemplate.send(eq(TOPIC), eq(KEY), any(SmsPayload.class))).thenReturn(mockFuture);
        SendResult<String, Object> sendResult = mock(SendResult.class);
        doAnswer(invocationOnMock -> {
            ListenableFutureCallback listenableFutureCallback = invocationOnMock.getArgument(0);
            listenableFutureCallback.onSuccess(sendResult);
            return null;
        }).when(mockFuture).addCallback(any(ListenableFutureCallback.class));
        smsPublisher.sendSmsNotificationEvent(getRiderProfile(VALID_PHONE_NO), TEST_SMS);
        verify(kafkaTemplate, times(INVOKED_ONCE)).send(eq(TOPIC), eq(KEY), any(SmsPayload.class));
    }

    private RiderProfile getRiderProfile(String phoneNumber) {
        RiderProfile riderProfile = new RiderProfile();
        riderProfile.setId(KEY);
        riderProfile.setPhoneNumber(phoneNumber);
        riderProfile.setCountryCode(Constants.IN_COUNTRY_CODE);
        return riderProfile;
    }
}
