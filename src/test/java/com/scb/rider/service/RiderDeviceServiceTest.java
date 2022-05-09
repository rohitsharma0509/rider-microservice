package com.scb.rider.service;


import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import static org.mockito.Mockito.when;

import java.util.NoSuchElementException;
import java.util.Optional;

import org.apache.commons.lang3.ObjectUtils;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;

import com.scb.rider.client.NewsPromotionFeignClient;
import com.scb.rider.client.NotificationFeignClient;
import com.scb.rider.exception.DataNotFoundException;
import com.scb.rider.model.document.RiderDeviceDetails;
import com.scb.rider.model.document.RiderPreferredZones;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.enumeration.Platform;
import com.scb.rider.model.enumeration.RiderStatus;
import com.scb.rider.repository.RiderDeviceDetailRepository;
import com.scb.rider.repository.RiderProfileRepository;
import com.scb.rider.service.document.RiderDeviceService;


@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
public class RiderDeviceServiceTest {

    @Mock
    private RiderDeviceDetailRepository riderDeviceDetailRepository;
    @Mock
    private RiderProfileRepository riderProfileRepository;
    @Mock
    private NotificationFeignClient notificationFeignClient;
    @InjectMocks
    private RiderDeviceService riderDeviceService;
    @Mock
    private NewsPromotionFeignClient newsPromotionFeignClient;
    private String riderId = "1234";


    @Test
    public void saveRiderDeviceInfoTest() {

        RiderDeviceDetails request = RiderDeviceDetails.builder()
                .deviceToken("abc")
                .platform(Platform.GCM)
                .build();
        RiderDeviceDetails riderResponse = RiderDeviceDetails.builder()
                .deviceToken("abc")
                .platform(Platform.GCM)
                .arn("arn")
                .id("12344")
                .build();
        RiderProfile riderProfile = new RiderProfile();
        riderProfile.setId("1234");
        riderProfile.setPhoneNumber("1231313");
        riderProfile.setStatus(RiderStatus.AUTHORIZED);
        RiderPreferredZones riderPreferredZonesData = new RiderPreferredZones();
        riderPreferredZonesData.setPreferredZoneId("2");
        riderPreferredZonesData.setPreferredZoneName("test");
        riderProfile.setRiderPreferredZones(riderPreferredZonesData);
        when(riderProfileRepository.findById(riderId)).thenReturn(Optional.of(riderProfile));
        when(riderDeviceDetailRepository.findByProfileId(riderId)).thenReturn(Optional.of(riderResponse));
        
        when(riderProfileRepository.findByRiderId(anyString())).thenReturn(Optional.of(riderProfile));
        
      
        
        when(notificationFeignClient.getDeviceArn(request)).thenReturn(riderResponse);
        when(riderDeviceDetailRepository.save(riderResponse)).thenReturn(riderResponse);
        
        //0doNothing().when(newsPromotionFeignClient.registerDeviceToTopic(riderId, any()));
        
        RiderDeviceDetails result = riderDeviceService.saveRiderDeviceInfo(riderId, request);

        assertTrue(ObjectUtils.isNotEmpty(result));
    }

    @Test
    public void saveRiderDeviceInfoWhenDeviceNotExistInDbTest() {

        RiderDeviceDetails request = RiderDeviceDetails.builder()
                .deviceToken("abc")
                .platform(Platform.GCM)
                .build();
        RiderDeviceDetails riderResponse = RiderDeviceDetails.builder()
                .deviceToken("abc")
                .platform(Platform.GCM)
                .arn("arn")
                .id("12344")
                .build();
        RiderProfile riderProfile = new RiderProfile();
        riderProfile.setId("1234");
        riderProfile.setPhoneNumber("1231313");
        riderProfile.setStatus(RiderStatus.AUTHORIZED);
        RiderPreferredZones riderPreferredZonesData = new RiderPreferredZones();
        riderPreferredZonesData.setPreferredZoneId("2");
        riderPreferredZonesData.setPreferredZoneName("test");
        riderProfile.setRiderPreferredZones(riderPreferredZonesData);
        when(riderProfileRepository.findById(riderId)).thenReturn(Optional.of(riderProfile));
        when(riderDeviceDetailRepository.findByProfileId(riderId)).thenReturn(Optional.empty());
        when(riderProfileRepository.findByRiderId(anyString())).thenReturn(Optional.of(riderProfile));

        when(notificationFeignClient.getDeviceArn(request)).thenReturn(riderResponse);
        when(riderDeviceDetailRepository.save(any())).thenReturn(riderResponse);
        RiderDeviceDetails result = riderDeviceService.saveRiderDeviceInfo(riderId, request);

        assertTrue(ObjectUtils.isNotEmpty(result));
    }

    @Test
    public void saveRiderDeviceInfoNotSaveDataNotFoundTest() {

        RiderDeviceDetails request = RiderDeviceDetails.builder()
                .deviceToken("abc")
                .platform(Platform.GCM)
                .build();

        RiderProfile riderProfile = new RiderProfile();
        riderProfile.setId("1234");
        riderProfile.setPhoneNumber("1231313");
        riderProfile.setStatus(RiderStatus.AUTHORIZED);
        when(riderProfileRepository.findById(riderId)).thenReturn(Optional.empty());
        assertThrows(DataNotFoundException.class, () -> riderDeviceService.saveRiderDeviceInfo(riderId, request));

    }

    @Test
    public void fetchRiderDeviceInfoTest() {

        RiderDeviceDetails riderResponse = RiderDeviceDetails.builder()
                .deviceToken("abc")
                .platform(Platform.GCM)
                .arn("arn")
                .id("12344")
                .build();

        when(riderDeviceDetailRepository.findByProfileId(riderId)).thenReturn(Optional.of(riderResponse));
        Optional<RiderDeviceDetails> result = riderDeviceService.findRiderDeviceDetails(riderId);

        assertTrue(ObjectUtils.isNotEmpty(result.get()));
    }

    @Test
    public void fetchRiderDeviceInfoTestDataNotExistTest() {

        when(riderDeviceDetailRepository.findByProfileId(riderId)).thenReturn(Optional.empty());
        Optional<RiderDeviceDetails> result = riderDeviceService.findRiderDeviceDetails(riderId);

        assertThrows(NoSuchElementException.class, () -> result.get());

    }
}
