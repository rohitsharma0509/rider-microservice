package com.scb.rider.service;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import com.scb.rider.client.LocationServiceFeignClient;
import com.scb.rider.client.OperationFeignClient;
import com.scb.rider.model.dto.ConfigDataResponse;
import com.scb.rider.model.dto.DistanceResponseEntity;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
class RiderLocationServiceTest {

	@Mock
	private LocationServiceFeignClient locationServiceFeignClient;

	@Mock
	private OperationFeignClient operationFeignClient;

	@InjectMocks
	private RiderLocationService riderLocationService;

	@Test
	  void getJobByIdTest() {
		  DistanceResponseEntity d= DistanceResponseEntity.builder().distance(85.0).build();
			ResponseEntity responseEntity = new ResponseEntity(HttpStatus.OK).ok(d);
			ConfigDataResponse c= new ConfigDataResponse();
			c.setValue("50.0");
			when(locationServiceFeignClient.getDistance(anyDouble(), anyDouble(), anyDouble(),anyDouble())).thenReturn(responseEntity);
			when(operationFeignClient.getConfigData("distanceFromMerchant")).thenReturn(c);
			Boolean b = riderLocationService.checkDistance(26.709756,24.074590,73.856773, 78.484636);

	assertNotNull(b);  }

	
	
	@Test
	  void getJobByIdTestNull() {
		  DistanceResponseEntity d= DistanceResponseEntity.builder().distance(85.0).build();
			ResponseEntity responseEntity = new ResponseEntity(HttpStatus.OK).ok(d);
			
			when(locationServiceFeignClient.getDistance(anyDouble(), anyDouble(), anyDouble(),anyDouble())).thenReturn(responseEntity);
			when(operationFeignClient.getConfigData("distanceFromMerchant")).thenReturn(null);
			Boolean b = riderLocationService.checkDistance(26.709756,24.074590,73.856773, 78.484636);

	assertNotNull(b);  }
}
