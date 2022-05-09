package com.scb.rider.controller;

import com.scb.rider.model.document.RiderCarrierDetails;
import com.scb.rider.model.dto.RiderCarrierDetailsRequestDto;
import com.scb.rider.service.RiderCarrierDetailsService;
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

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
public class RiderCarrierDetailsControllerTest {

    @InjectMocks
    private RiderCarrierDetailsController riderCarrierDetailsController;

    @Mock
    private RiderCarrierDetailsService riderCarrierDetailsService;


    @Test
    public void  saveCarrierDetailsTest(){
        when(riderCarrierDetailsService.saveCarrierDetails(anyString(), any())).thenReturn(getRiderCarrierDetails());
        RiderCarrierDetailsRequestDto updateRequestDto = getUpdateRequestDto();
        String riderId = "rider-1";
        ResponseEntity responseEntity = riderCarrierDetailsController.saveCarrierDetails(riderId, updateRequestDto);
        assertTrue(ObjectUtils.isNotEmpty(responseEntity.getBody()));
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    }

    @Test
    public void  getCarrierDetailsTest(){
        String riderId = "rider-1";
        when(riderCarrierDetailsService.getCarrierDetails(anyString())).thenReturn(getRiderCarrierDetails());
        ResponseEntity responseEntity = riderCarrierDetailsController.getCarrierDetails(riderId);
        assertTrue(ObjectUtils.isNotEmpty(responseEntity.getBody()));
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    private RiderCarrierDetails getRiderCarrierDetails() {
        return RiderCarrierDetails.builder()
                .riderId("rider-1")
                .name("name")
                .mobileNetworkCode("mobile-network-code")
                .mobileNetworkOperator("mobile-network-operator")
                .build();

    }

    private RiderCarrierDetailsRequestDto getUpdateRequestDto() {
        return  RiderCarrierDetailsRequestDto.builder()
                .name("name")
                .mobileNetworkCode("mobile-network-code")
                .mobileNetworkOperator("mobile-network-operator")
                .build();
    }
}
