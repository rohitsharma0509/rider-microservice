package com.scb.rider.controller;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.apache.commons.lang3.ObjectUtils;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.scb.rider.model.document.RiderRemarksDetails;
import com.scb.rider.model.dto.SearchResponseDto;
import com.scb.rider.service.document.RiderRemarksService;



@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
public class RiderRemarkControllerTest {

	@InjectMocks
	private RiderRemarksController riderRemarksController;

	@Mock
	private RiderRemarksService riderRemarksService;

	@Test
	public void saveRiderRemarkTest() {

		RiderRemarksDetails rTest=RiderRemarksDetails.builder().riderId("213").build();
		
		
		when(riderRemarksService.saveRiderRemarksInfo("213", rTest))
				.thenReturn(rTest);

		ResponseEntity<RiderRemarksDetails> apiResponse = riderRemarksController.saveRemark("213",
				rTest, "213");

		assertTrue(ObjectUtils.isNotEmpty(apiResponse.getBody()));
		assertEquals(HttpStatus.CREATED, apiResponse.getStatusCode());
		assertNotNull(apiResponse.toString());
		assertNotNull(apiResponse.toString());
	}

	@Test
	public void deleteRiderRemarkTest() {

		ResponseEntity<Boolean> apiResponse = riderRemarksController.deleteRemark("123", "123");

		assertTrue(ObjectUtils.isNotEmpty(apiResponse.getBody()));
		assertEquals(HttpStatus.OK, apiResponse.getStatusCode());
		assertNotNull(apiResponse.toString());
		assertNotNull(apiResponse.toString());
	}

	
	@Test
	public void getRiderRemarkTest() {

		Pageable p = PageRequest.of(1,2);

		SearchResponseDto sTest= SearchResponseDto.builder().build();
		when(riderRemarksService.getRemarksBySearchTermWithFilter("123", new ArrayList<>(), p))
		.thenReturn(sTest);

		
		ResponseEntity<SearchResponseDto> apiResponse = riderRemarksController.getRemarksBySearchTerm("123", new ArrayList<>(), p);

		assertTrue(ObjectUtils.isNotEmpty(apiResponse.getBody()));
		assertEquals(HttpStatus.OK, apiResponse.getStatusCode());
		assertNotNull(apiResponse.toString());
		assertNotNull(apiResponse.toString());
	}
}
