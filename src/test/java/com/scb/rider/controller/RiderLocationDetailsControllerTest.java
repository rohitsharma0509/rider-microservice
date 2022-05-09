package com.scb.rider.controller;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;

import com.scb.rider.service.RiderLocationService;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
public class RiderLocationDetailsControllerTest {

    @InjectMocks
    private RiderLocationDetailsController riderDeviceController;

   @Mock
	RiderLocationService riderLocationService;

    @Test
	public void getMerhcantDist() {
		when(riderLocationService.checkDistance(anyDouble(), anyDouble(), anyDouble(),anyDouble())).thenReturn(true);
		
		Object fetchedDto = riderDeviceController.isRiderInMerchantRange("123",26.709756,24.074590,73.856773, 78.484636);
		assertNotNull(fetchedDto.toString());
		verify(riderLocationService, times(1)).checkDistance(26.709756,24.074590,73.856773, 78.484636);
	}
	
}
