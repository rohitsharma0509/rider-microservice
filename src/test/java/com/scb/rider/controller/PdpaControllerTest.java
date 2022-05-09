package com.scb.rider.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.List;

import com.scb.rider.model.pdpa.ConsentManagementDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import com.scb.rider.model.pdpa.ConsentCollectionInfo;
import com.scb.rider.model.pdpa.ConsentUpdateDto;
import com.scb.rider.model.pdpa.ConsentUpdateResponse;
import com.scb.rider.model.pdpa.ManagementInquiryDto;
import com.scb.rider.model.pdpa.ManagementInquiryResponse;
import com.scb.rider.model.pdpa.PdpaValidateDto;
import com.scb.rider.model.pdpa.PdpaValidateResponse;
import com.scb.rider.service.PdpaService;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
class PdpaControllerTest {

	@InjectMocks
	private PdpaController pdpaController;

	@Mock
	private PdpaService pdpaService;

	@Test
	void test_RiderManagementInquiry() {

		ManagementInquiryResponse inquiryResponse = new ManagementInquiryResponse();
		Mockito.when(pdpaService.riderInquiryConsent(Mockito.any())).thenReturn(inquiryResponse);

		ResponseEntity<ManagementInquiryResponse> riderManagementInquiryResponse = pdpaController
				.riderManagementInquiry(getManagementInquiryDto());

		assertNotNull(riderManagementInquiryResponse);

	}

	@Test
	void test_RiderConsentSave() {

		ConsentUpdateResponse consentUpdateResponse = new ConsentUpdateResponse();
		Mockito.when(pdpaService.updateRiderConsent(Mockito.any())).thenReturn(consentUpdateResponse);

		ResponseEntity<ConsentUpdateResponse> riderConsentUpdateResponse = pdpaController
				.riderConsentUpdate(getConsentUpdateDto());

		assertNotNull(riderConsentUpdateResponse);

	}

	@Test
	void test_RiderConsentUpdation() {

		ConsentUpdateResponse consentUpdateResponse = new ConsentUpdateResponse();
		Mockito.when(pdpaService.riderConsentManagement(Mockito.any())).thenReturn(consentUpdateResponse);

		ResponseEntity<ConsentUpdateResponse> riderConsentUpdateResponse = pdpaController
				.riderConsentManagement(getConsentManagementDto());

		assertNotNull(riderConsentUpdateResponse);

	}

	@Test
	void test_RiderConsentValidation() {
		PdpaValidateResponse validationResponse = new PdpaValidateResponse();
		Mockito.when(pdpaService.riderValidateConsent(Mockito.any())).thenReturn(validationResponse);

		ResponseEntity<PdpaValidateResponse> riderValidationResponse = pdpaController
				.riderConsentValidation(getPdpaValidateDto());

		assertNotNull(riderValidationResponse);

	}

	private ManagementInquiryDto getManagementInquiryDto() {
		return ManagementInquiryDto.builder()
				.riderId("RR10001")
				.documentId("123987645")
				.build();
	}

	private ConsentUpdateDto getConsentUpdateDto() {
		return ConsentUpdateDto.builder().riderId("RR10000").dateOfBirth("2021-02-13").consentCollectionInfo(getConsentList()).build();
	}

	private PdpaValidateDto getPdpaValidateDto() {
		return PdpaValidateDto.builder().riderId("RR10000").dateOfBirth("2021-02-13").build();
	}

	private ConsentManagementDto getConsentManagementDto(){
		return ConsentManagementDto.builder()
				.riderId("RR10001")
				.consentCollectionInfo(getConsentList())
				.build();
	}

	private List<ConsentCollectionInfo> getConsentList(){
		ConsentCollectionInfo crossSellUpdate = new ConsentCollectionInfo("001", 1.01, true);
		ConsentCollectionInfo marketingUpdate = new ConsentCollectionInfo("002", 1.01, false);
		return Arrays.asList(marketingUpdate, crossSellUpdate);
	}
}
