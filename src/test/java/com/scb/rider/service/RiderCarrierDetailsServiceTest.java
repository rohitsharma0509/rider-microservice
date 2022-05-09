package com.scb.rider.service;

import com.scb.rider.exception.DataNotFoundException;
import com.scb.rider.model.document.RiderCarrierDetails;
import com.scb.rider.model.dto.RiderCarrierDetailsRequestDto;
import com.scb.rider.repository.RiderCarrierDetailsRepository;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
public class RiderCarrierDetailsServiceTest {

    @InjectMocks
    private RiderCarrierDetailsService riderCarrierDetailsService;

    @Mock
    private RiderCarrierDetailsRepository riderCarrierDetailsRepository;

    @Test
    public void getValidCarrierDetailsTest(){
        when(riderCarrierDetailsRepository.findByRiderId(anyString())).thenReturn(getRiderCarrierDetails());
        RiderCarrierDetails carrierDetails = riderCarrierDetailsService.getCarrierDetails("rider-1");
        assertNotNull(carrierDetails);
        assertEquals("rider-1", carrierDetails.getRiderId());
    }


    @Test(expected = DataNotFoundException.class)
    public void getInvalidCarrierDetailsTest(){
        when(riderCarrierDetailsRepository.findByRiderId(anyString())).thenReturn(null);
        RiderCarrierDetails carrierDetails = riderCarrierDetailsService.getCarrierDetails("rider-1");
        assertTrue(ObjectUtils.isEmpty(carrierDetails));
    }
    @Test
    public void setCarrierDetailsForNewRiderTest(){
        when(riderCarrierDetailsRepository.findByRiderId(anyString())).thenReturn(null);
        when(riderCarrierDetailsRepository.save(any())).thenReturn(getRiderCarrierDetails());
        RiderCarrierDetails carrierDetails = riderCarrierDetailsService.saveCarrierDetails("rider-1", getUpdateRequestDto());
        assertNotNull(carrierDetails);
        assertEquals("rider-1", carrierDetails.getRiderId());
    }


    @Test
    public void setCarrierDetailsForExistingRiderTest(){
        when(riderCarrierDetailsRepository.findByRiderId(anyString())).thenReturn(getRiderCarrierDetails());
        when(riderCarrierDetailsRepository.save(any())).thenReturn(getRiderCarrierDetails());
        RiderCarrierDetails carrierDetails = riderCarrierDetailsService.saveCarrierDetails("rider-1", getUpdateRequestDto());
        assertNotNull(carrierDetails);
        assertEquals("rider-1", carrierDetails.getRiderId());
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
