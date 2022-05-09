package com.scb.rider.service;

import com.scb.rider.client.JobServiceFeignClient;
import com.scb.rider.client.OperationFeignClient;
import com.scb.rider.client.PocketServiceFeignClient;
import com.scb.rider.constants.Constants;
import com.scb.rider.kafka.NotificationPublisher;
import com.scb.rider.model.document.RiderDeviceDetails;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.dto.ConfigDataResponse;
import com.scb.rider.model.dto.ExcessiveWaitingTimeDetailsEntity;
import com.scb.rider.model.dto.RiderExcessiveWaitDetailsDto;
import com.scb.rider.repository.RiderDeviceDetailRepository;
import com.scb.rider.util.PropertyUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExcessiveWaitPaymentServiceTest {

    private static final String JOB_ID = "S120000000001";
    private static final String ID = "1";

    @InjectMocks
    private ExcessiveWaitPaymentService excessiveWaitPaymentService;

    @Mock
    private OperationFeignClient operationFeignClient;

    @Mock
    private PocketServiceFeignClient pocketServiceFeignClient;

    @Mock
    private JobServiceFeignClient jobServiceFeignClient;

    @Mock
    private RiderDeviceDetailRepository riderDeviceDetailRepository;

    @Mock
    private NotificationPublisher notificationPublisher;

    @Mock
    private PropertyUtils propertyUtils;

    @Test
    void testCheckAndPayEwtAmountWhenJobNotEligibleForEwt() {
        when(operationFeignClient.getConfigData(eq(Constants.THRESHOLD_WAIT_TIME))).thenReturn(ConfigDataResponse.builder().value("10").build());
        LocalDateTime arrivedAtMerchantTime = LocalDateTime.now().minusMinutes(10);
        LocalDateTime mealPickedUpTime = LocalDateTime.now();
        RiderProfile profile = new RiderProfile();
        profile.setId(ID);
        excessiveWaitPaymentService.checkAndPayEwtAmount(JOB_ID, arrivedAtMerchantTime, mealPickedUpTime, profile);
        verifyZeroInteractions(riderDeviceDetailRepository, jobServiceFeignClient, pocketServiceFeignClient, notificationPublisher, propertyUtils);
    }

    @Test
    void testCheckAndPayEwtAmountWhenJobEligibleForEwt() {
        when(operationFeignClient.getConfigData(anyString())).thenReturn(ConfigDataResponse.builder().value("10").build());
        when(riderDeviceDetailRepository.findByProfileId(eq(ID))).thenReturn(Optional.of(RiderDeviceDetails.builder().build()));
        when(propertyUtils.getProperty(anyString(), any(Locale.class))).thenReturn("test");
        LocalDateTime arrivedAtMerchantTime = LocalDateTime.now().minusMinutes(11);
        LocalDateTime mealPickedUpTime = LocalDateTime.now();
        RiderProfile profile = new RiderProfile();
        profile.setId(ID);
        excessiveWaitPaymentService.checkAndPayEwtAmount(JOB_ID, arrivedAtMerchantTime, mealPickedUpTime, profile);
        verify(jobServiceFeignClient, times(1)).updateEwtAmount(eq(JOB_ID), any(ExcessiveWaitingTimeDetailsEntity.class));
        verify(pocketServiceFeignClient, times(1)).addRiderExcessWaitTopup(any(RiderExcessiveWaitDetailsDto.class));
        verify(notificationPublisher, times(1)).sendNotification(any(RiderDeviceDetails.class), anyString(), anyString(), anyString(), anyString());
    }
}
