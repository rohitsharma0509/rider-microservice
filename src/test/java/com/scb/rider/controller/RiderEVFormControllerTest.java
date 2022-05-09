package com.scb.rider.controller;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import com.scb.rider.model.document.RiderEVForm;
import com.scb.rider.model.dto.RiderEVFormDto;
import com.scb.rider.model.enumeration.MandatoryCheckStatus;
import com.scb.rider.service.document.RiderEVFormService;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
class RiderEVFormControllerTest {

	@Mock
	private RiderEVFormService evFormService;

	@InjectMocks
	private RiderEVFormController evFormController;

	private static RiderEVForm evForm;
	private static RiderEVFormDto evFormDto;

	@BeforeAll
	static void setup() {
		evForm = RiderEVForm.builder().riderProfileId("12345678").status(MandatoryCheckStatus.PENDING)
				.documentUrl("ftp://test.com/path").build();

		evFormDto = RiderEVFormDto.builder().riderProfileId(evForm.getRiderProfileId())
				.status(MandatoryCheckStatus.APPROVED).build();
	}

	@Test
	void test_GetRiderEvForm() {
		when(evFormService.getRiderEVForm(Mockito.anyString())).thenReturn(evForm);
		ResponseEntity<RiderEVForm> riderEVFormResponse = evFormController.getRiderEVForm("12345678");

		assertNotNull(riderEVFormResponse);
		RiderEVForm riderEVForm = riderEVFormResponse.getBody();
		assertEquals("12345678", riderEVForm.getRiderProfileId());
		assertNotNull(riderEVForm.getDocumentUrl());

	}

	@Test
	void test_SaveRiderEvForm() {
		when(evFormService.saveRiderEVForm(Mockito.any())).thenReturn(evForm);
		ResponseEntity<RiderEVForm> riderEVFormResponse = evFormController.saveRiderEVForm(evFormDto);

		assertNotNull(riderEVFormResponse);
		RiderEVForm riderEVForm = riderEVFormResponse.getBody();
		assertEquals("12345678", riderEVForm.getRiderProfileId());
		assertNotNull(riderEVForm.getDocumentUrl());

	}
}
