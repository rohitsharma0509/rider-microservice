package com.scb.rider.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

import com.scb.rider.service.document.RiderProfileService;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
public class RedisCacheControllerTest {

	@InjectMocks
	private RedisCacheController redisCacheController;

	@Mock
	private RiderProfileService riderProfileService;

	@Test
	public void testFileUploadTest() throws Exception {
		ResponseEntity<Boolean> responseEntity = redisCacheController.saveToRedis();
		assertTrue(ObjectUtils.isNotEmpty(responseEntity.getBody()));
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

	}


}
