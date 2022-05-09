package com.scb.rider.controller;

import com.scb.rider.exception.DataNotFoundException;
import com.scb.rider.model.document.RiderDeviceDetails;
import com.scb.rider.model.enumeration.Platform;
import com.scb.rider.service.document.RiderDeviceService;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
public class RiderDeviceControllerTest {

    @InjectMocks
    private RiderDeviceController riderDeviceController;

    @Mock
    private RiderDeviceService riderDeviceService;

    @Test
    public void saveRiderDeviceInfoTest() {

        // prepare data and mock's behaviour
        RiderDeviceDetails request = RiderDeviceDetails.builder()
                .deviceToken("1234")
                .platform(Platform.GCM)
                .build();

        RiderDeviceDetails response = RiderDeviceDetails.builder()
                .deviceToken("1234")
                .platform(Platform.GCM)
                .id("1234")
                .arn("12132")
                .profileId("1213123")
                .build();

        when(riderDeviceService.saveRiderDeviceInfo("1213123", request))
                .thenReturn(response);


        ResponseEntity<RiderDeviceDetails> apiResponse = riderDeviceController
                .saveRiderDeviceDetails("1213123", request);

        assertTrue(ObjectUtils.isNotEmpty(apiResponse.getBody()));
        assertEquals(HttpStatus.CREATED, apiResponse.getStatusCode());
        assertNotNull(apiResponse.toString());
        assertNotNull(apiResponse.toString());
    }

    @Test(expected = DataNotFoundException.class)
    public void saveRiderDeviceInfoDataNotFoundTest() {

        // prepare data and mock's behaviour
        RiderDeviceDetails request = RiderDeviceDetails.builder()
                .deviceToken("1234")
                .platform(Platform.GCM)
                .build();

        when(riderDeviceService.saveRiderDeviceInfo("1213123", request))
                .thenThrow(DataNotFoundException.class);

        ResponseEntity<RiderDeviceDetails> apiResponse = riderDeviceController
                .saveRiderDeviceDetails("1213123", request);

        assertTrue(ObjectUtils.isEmpty(apiResponse.getBody()));
    }

    @Test
    public void fetchRiderDeviceInfoTest() {

        // prepare data and mock's behaviour
        RiderDeviceDetails response = RiderDeviceDetails.builder()
                .deviceToken("1234")
                .platform(Platform.GCM)
                .id("1234")
                .arn("12132")
                .profileId("1213123")
                .build();

        when(riderDeviceService.findRiderDeviceDetails("1213123"))
                .thenReturn(Optional.of(response));

        ResponseEntity<RiderDeviceDetails> apiResponse = riderDeviceController
                .fetchRiderDeviceDetails("1213123");

        assertTrue(ObjectUtils.isNotEmpty(apiResponse.getBody()));
        assertEquals(HttpStatus.OK, apiResponse.getStatusCode());
        assertNotNull(apiResponse.toString());
        assertNotNull(apiResponse.toString());
    }

    @Test
    public void fetchRiderDeviceInfoNotFoundTest() {

        // prepare data and mock's behaviour
        when(riderDeviceService.findRiderDeviceDetails("1213123"))
                .thenReturn(Optional.empty());

        ResponseEntity<RiderDeviceDetails> apiResponse = riderDeviceController
                .fetchRiderDeviceDetails("1213123");

        assertTrue(ObjectUtils.isEmpty(apiResponse.getBody()));
        assertEquals(HttpStatus.NOT_FOUND, apiResponse.getStatusCode());
        assertNotNull(apiResponse.toString());
        assertNotNull(apiResponse.toString());
    }

}
