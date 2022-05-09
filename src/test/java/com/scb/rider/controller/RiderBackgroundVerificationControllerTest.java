package com.scb.rider.controller;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;

import com.scb.rider.constants.Constants;
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

import com.scb.rider.model.document.RiderBackgroundVerificationDocument;
import com.scb.rider.model.dto.RiderBackgroundVerificationDetailsRequest;
import com.scb.rider.model.dto.RiderBackgroundVerificationDetailsResponse;
import com.scb.rider.model.enumeration.MandatoryCheckStatus;
import com.scb.rider.service.document.RiderBackgroundVerificationDocumentService;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
public class RiderBackgroundVerificationControllerTest {

	@InjectMocks
	RiderBackgroundVerificationController riderBackgroundVerificationController;

	@Mock
	RiderBackgroundVerificationDocumentService riderBackgroundService;

	@Test
	public void testCreateRiderVehicleRegistrationRequest() throws Exception {
		RiderBackgroundVerificationDetailsRequest request = getBackgroundRequestData();
		RiderBackgroundVerificationDetailsResponse response = getBackgroundResponseData();
		when(riderBackgroundService.addBackgroundVerificationDetails(any(String.class),
				any(RiderBackgroundVerificationDetailsRequest.class), eq(Constants.OPS))).thenReturn(response);
		ResponseEntity<RiderBackgroundVerificationDetailsResponse> apiResponse = riderBackgroundVerificationController
				.addRiderBackgroundVerificationDetails(Constants.OPS_MEMBER, "1", request);
		assertTrue(ObjectUtils.isNotEmpty(apiResponse.getBody()));
		assertEquals(HttpStatus.CREATED, apiResponse.getStatusCode());

		assertEquals("1", response.getId(), "Invalid Response");
		assertEquals(MandatoryCheckStatus.APPROVED, response.getStatus(), "Invalid Response");
		assertNotNull(response.getDueDate());
		assertNotNull(response.getDocumentUrls());
	}

	@Test
	public void getBackgroundVerificationDetailsByProfileId() {
		when(riderBackgroundService.getBackgroundVerificationDetailsByProfileId("12314"))
				.thenReturn(getBackgroundDocumentData());
		ResponseEntity<RiderBackgroundVerificationDetailsResponse> result = riderBackgroundVerificationController
				.getBackgroundVerificationDetailsByProfileId("12314");
		assertEquals("12314", result.getBody().getRiderProfileId());
		assertEquals(MandatoryCheckStatus.APPROVED, result.getBody().getStatus());
	}

	@Test
	public void updateRiderBackgroundVerificationDetails() {
		when(riderBackgroundService.getBackgroundVerificationDetailsByProfileId("12314"))
				.thenReturn(getBackgroundDocumentData());

		when(riderBackgroundService.updateBackgroundVerificationDetails(
				any(RiderBackgroundVerificationDetailsRequest.class), any(RiderBackgroundVerificationDocument.class), eq(Constants.OPS)))
						.thenReturn(getBackgroundDocumentData());

		ResponseEntity<RiderBackgroundVerificationDetailsResponse> result = riderBackgroundVerificationController
				.updateRiderBackgroundVerificationDetails(Constants.OPS_MEMBER, "12314", getBackgroundRequestData());
		assertEquals("12314", result.getBody().getRiderProfileId());
		assertEquals(MandatoryCheckStatus.APPROVED, result.getBody().getStatus());
	}

	@Test
	public void addRiderBackgroundVerificationDetailsByRider() {
		RiderBackgroundVerificationDetailsRequest request = getBackgroundRequestData();
		RiderBackgroundVerificationDetailsResponse response = getBackgroundResponseData();
		when(riderBackgroundService.addBackgroundVerificationDetails(eq("12314"), any(RiderBackgroundVerificationDetailsRequest.class),
				eq(Constants.RIDER))).thenReturn(response);
		ResponseEntity<RiderBackgroundVerificationDetailsResponse> result = riderBackgroundVerificationController
				.addRiderBackgroundVerificationDetailsByRider(Constants.OPS_MEMBER, "12314", request);
		assertEquals("12314", result.getBody().getRiderProfileId());
		assertEquals(MandatoryCheckStatus.APPROVED, result.getBody().getStatus());
	}

	private static RiderBackgroundVerificationDetailsResponse getBackgroundResponseData() {
		RiderBackgroundVerificationDetailsResponse response = RiderBackgroundVerificationDetailsResponse.builder()
				.id("1").riderProfileId("12314").status(MandatoryCheckStatus.APPROVED)
				.dueDate(LocalDate.of(2022, 12, 11)).reason("Test").documentUrls(Arrays.asList("localhost/")).build();
		return response;
	}

	private static RiderBackgroundVerificationDetailsRequest getBackgroundRequestData() {
		RiderBackgroundVerificationDetailsRequest request = RiderBackgroundVerificationDetailsRequest.builder()
				.status(MandatoryCheckStatus.APPROVED).dueDate(LocalDate.of(2022, 12, 11)).reason("Test")
				.documentUrls(Arrays.asList("localhost/")).build();
		return request;
	}

	private static RiderBackgroundVerificationDocument getBackgroundDocumentData() {
		RiderBackgroundVerificationDocument request = RiderBackgroundVerificationDocument.builder().id("1")
				.riderProfileId("12314").status(MandatoryCheckStatus.APPROVED).dueDate(LocalDate.of(2022, 12, 11))
				.reason("Test").documentUrls(Arrays.asList("localhost/")).build();
		return request;
	}
}
