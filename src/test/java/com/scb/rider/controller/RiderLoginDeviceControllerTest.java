package com.scb.rider.controller;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import java.util.Optional;
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
import com.scb.rider.model.document.RiderLoginDeviceDetails;
import com.scb.rider.service.document.RiderLoginDeviceService;


@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
public class RiderLoginDeviceControllerTest {

  @InjectMocks
  private RiderLoginDeviceController riderLoginDeviceController;

  @Mock
  private RiderLoginDeviceService riderLoginDeviceService;

  @Test
    public void saveRiderDeviceInfoTest() {

        // prepare data and mock's behaviour
        RiderLoginDeviceDetails request = RiderLoginDeviceDetails.builder()
                .deviceId("aaa")
                .phoneNumber("1234")
                .build();

        RiderLoginDeviceDetails response = RiderLoginDeviceDetails.builder()
                .deviceId("aaa")
                .phoneNumber("1234")
                .build();

        when(riderLoginDeviceService.saveRiderLoginDeviceInfo("1213123", request))
                .thenReturn(response);


        ResponseEntity<RiderLoginDeviceDetails> apiResponse = riderLoginDeviceController
                .saveRiderLoginDeviceDetails("1213123", request);

        assertTrue(ObjectUtils.isNotEmpty(apiResponse.getBody()));
        assertEquals(HttpStatus.CREATED, apiResponse.getStatusCode());
        assertNotNull(apiResponse.toString());
        assertNotNull(apiResponse.toString());
    }
    
    @Test
    public void fetchRiderDeviceDetailsTest() {

        // prepare data and mock's behaviour
        Optional<RiderLoginDeviceDetails> response = Optional.of(RiderLoginDeviceDetails.builder()
                .deviceId("aaa")
                .phoneNumber("1234")
                .build());

        when(riderLoginDeviceService.findRiderLoginDeviceDetails(anyString()))
                .thenReturn(response);

        ResponseEntity<RiderLoginDeviceDetails> apiResponse = riderLoginDeviceController
                .fetchRiderDeviceDetails("1234");

        assertTrue(ObjectUtils.isNotEmpty(apiResponse.getBody()));
        assertEquals(HttpStatus.OK, apiResponse.getStatusCode());
        assertNotNull(apiResponse.toString());
        assertNotNull(apiResponse.toString());
    }
    
    @Test
    public void fetchRiderDeviceDetailsNoRecordTest() {

        // prepare data and mock's behaviour
        Optional<RiderLoginDeviceDetails> response = Optional.of(RiderLoginDeviceDetails.builder()
                .deviceId("aaa")
                .phoneNumber("1234")
                .build());

        when(riderLoginDeviceService.findRiderLoginDeviceDetails(anyString()))
                .thenReturn(Optional.empty());

        ResponseEntity<RiderLoginDeviceDetails> apiResponse = riderLoginDeviceController
                .fetchRiderDeviceDetails("1234");
        assertEquals(HttpStatus.NOT_FOUND, apiResponse.getStatusCode());
        assertNotNull(apiResponse.toString());
        assertNotNull(apiResponse.toString());
    }
}
