package com.scb.rider.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import com.scb.rider.model.pdpa.ConsentManagementDto;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.scb.rider.exception.PdpaException;
import com.scb.rider.model.pdpa.ConsentCollectionInfo;
import com.scb.rider.model.pdpa.ConsentUpdateDto;
import com.scb.rider.model.pdpa.ConsentUpdateResponse;
import com.scb.rider.model.pdpa.ManagementInquiryDto;
import com.scb.rider.model.pdpa.ManagementInquiryResponse;
import com.scb.rider.model.pdpa.PdpaInquiryData;
import com.scb.rider.model.pdpa.PdpaValidateData;
import com.scb.rider.model.pdpa.PdpaValidateDto;
import com.scb.rider.model.pdpa.PdpaValidateResponse;
import com.scb.rider.model.pdpa.Status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class PdpaServiceTest {

	@Mock
	RestTemplate restTemplate;

	@InjectMocks
	PdpaService pdpaService;

	HttpHeaders httpHeaders = new HttpHeaders();

	@BeforeEach
	void setUp() {
		pdpaService = new PdpaService();
		ReflectionTestUtils.setField(pdpaService, "pdpaBaseUrl", "baseUrl");
		ReflectionTestUtils.setField(pdpaService, "pdpaInquiryPath", "/inquiry");
		ReflectionTestUtils.setField(pdpaService, "pdpaValidatePath", "/validate");
		ReflectionTestUtils.setField(pdpaService, "pdpaConsentPath", "/consent");
		ReflectionTestUtils.setField(pdpaService, "pdpaManagementPath", "/management");
		ReflectionTestUtils.setField(pdpaService, "restTemplate", restTemplate);
	}

	@Test
	void riderInquiryInPdpaTest() {
		when(restTemplate.postForEntity(anyString(), any(), any()))
				.thenReturn(ResponseEntity.ok().headers(httpHeaders).body(getManagementInquiryResponse()));
		ManagementInquiryResponse managementInquiryResponse = pdpaService
				.riderInquiryConsent(getManagementInquiryRequest());
		Assert.assertNotNull(managementInquiryResponse);
	}

	@Test
	void updateRiderConsentInPdpaTest() {
		when(restTemplate.postForEntity(anyString(), any(), any()))
				.thenReturn(ResponseEntity.ok().headers(httpHeaders).body(getConsentUpdateResponse()));
		ConsentUpdateResponse consentUpdateResponse = pdpaService.updateRiderConsent(getConsentUpdateRequest());
		Assert.assertNotNull(consentUpdateResponse);
	}

	@Test
	void updateRiderConsentInManagementPdpaTest() {
		String uri = "baseUrl/management".concat("a");
		when(restTemplate.exchange(anyString(), eq(HttpMethod.PUT), any(), eq(ConsentUpdateResponse.class)))
				.thenReturn(ResponseEntity.ok().headers(httpHeaders).body(getConsentUpdateResponse()));
		ConsentUpdateResponse consentUpdateResponse = pdpaService.riderConsentManagement(getConsentManagementDto());
		Assert.assertNotNull(consentUpdateResponse);
	}

	@Test
	void riderValidateAgeInPdpaTest() {
		when(restTemplate.postForEntity(anyString(), any(), any()))
				.thenReturn(ResponseEntity.ok().headers(httpHeaders).body(getPdpaValidationResponse()));
		PdpaValidateResponse validationResponse = pdpaService.riderValidateConsent(getPdpaValidateRequest());
		Assert.assertNotNull(validationResponse);
	}

	@Test
	void riderInquiryInPdpaTest_FailedCallToPdpa() {
		when(restTemplate.postForEntity(anyString(), any(), any())).thenThrow(HttpClientErrorException.class);
		assertThrows(PdpaException.class, () -> pdpaService.riderInquiryConsent(getManagementInquiryRequest()));
	}

	@Test
	void updateRiderConsentInPdpaTest_FailedCallToPdpa() {
		when(restTemplate.postForEntity(anyString(), any(), any())).thenThrow(HttpClientErrorException.class);
		assertThrows(PdpaException.class, () -> pdpaService.updateRiderConsent(getConsentUpdateRequest()));
	}

	@Test
	void updateRiderConsentInManagementPdpaTest_FailedCallToPdpa() {
		when(restTemplate.postForEntity(anyString(), any(), any())).thenThrow(HttpClientErrorException.class);
		assertThrows(PdpaException.class, () -> pdpaService.riderConsentManagement(getConsentManagementDto()));
	}

	@Test
	void riderValidateAgeInPdpaTest_FailedCallToPdpa() {
		when(restTemplate.postForEntity(anyString(), any(), any())).thenThrow(HttpClientErrorException.class);
		assertThrows(PdpaException.class, () -> pdpaService.riderValidateConsent(getPdpaValidateRequest()));
	}

	@Test
	void riderInquiryInPdpaTest_EmptyResponseFromPdpa() {
		when(restTemplate.postForEntity(anyString(), any(), any())).thenReturn(null);
		assertThrows(PdpaException.class, () -> pdpaService.riderInquiryConsent(getManagementInquiryRequest()));
	}

	@Test
	void updateRiderConsentInPdpaTest_EmptyResponseFromPdpa() {
		when(restTemplate.postForEntity(anyString(), any(), any())).thenReturn(null);
		assertThrows(PdpaException.class, () -> pdpaService.updateRiderConsent(getConsentUpdateRequest()));
	}

	@Test
	void updateRiderConsentInManagementPdpaTest_EmptyResponseFromPdpa() {
		when(restTemplate.postForEntity(anyString(), any(), any())).thenReturn(null);
		assertThrows(PdpaException.class, () -> pdpaService.riderConsentManagement(getConsentManagementDto()));
	}

	@Test
	void riderValidateAgeInPdpaTest_EmptyResponseFromPdpa() {
		when(restTemplate.postForEntity(anyString(), any(), any())).thenReturn(null);
		assertThrows(PdpaException.class, () -> pdpaService.riderValidateConsent(getPdpaValidateRequest()));
	}

	@Test
	void riderInquiryInPdpaTest_EmptyBodyFromPdpa() {
		when(restTemplate.postForEntity(anyString(), any(), any()))
				.thenReturn(ResponseEntity.ok().headers(httpHeaders).body(null));
		assertThrows(PdpaException.class, () -> pdpaService.riderInquiryConsent(getManagementInquiryRequest()));
	}

	@Test
	void updateRiderConsentInPdpaTest_EmptyBodyFromPdpa() {
		when(restTemplate.postForEntity(anyString(), any(), any()))
				.thenReturn(ResponseEntity.ok().headers(httpHeaders).body(null));
		assertThrows(PdpaException.class, () -> pdpaService.updateRiderConsent(getConsentUpdateRequest()));
	}

	@Test
	void updateRiderConsentInManagementPdpaTest_EmptyBodyFromPdpa() {
		when(restTemplate.postForEntity(anyString(), any(), any()))
				.thenReturn(ResponseEntity.ok().headers(httpHeaders).body(null));
		assertThrows(PdpaException.class, () -> pdpaService.riderConsentManagement(getConsentManagementDto()));
	}

	@Test
	void riderValidateAgeInPdpaTest_EmptyBodyFromPdpa() {
		when(restTemplate.postForEntity(anyString(), any(), any()))
				.thenReturn(ResponseEntity.ok().headers(httpHeaders).body(null));
		assertThrows(PdpaException.class, () -> pdpaService.riderValidateConsent(getPdpaValidateRequest()));
	}

	private ManagementInquiryDto getManagementInquiryRequest() {
		return ManagementInquiryDto.builder()
				.riderId("RR10001")
				.documentId("123987645")
				.build();
	}
	
	private ConsentUpdateDto getConsentUpdateRequest() {
		return ConsentUpdateDto.builder()
				.riderId("RR10001")
				.dateOfBirth("2021-02-13")
				.consentCollectionInfo(getConsentList())
				.build();
	}

	private ConsentManagementDto getConsentManagementDto(){
		return ConsentManagementDto.builder()
				.riderId("RR10001")
				.consentCollectionInfo(getConsentList())
				.build();
	}

	private PdpaValidateDto getPdpaValidateRequest() {
		return PdpaValidateDto.builder()
				.riderId("RR10001")
				.dateOfBirth("2021-02-13")
				.build();
	}
	
	private ManagementInquiryResponse getManagementInquiryResponse() {
		PdpaInquiryData inquiryData = PdpaInquiryData.builder()
				.customerId("RR10001")
				.build();
		return ManagementInquiryResponse.builder().data(inquiryData)
				.status(new Status(1000, "success", "desc of inq call")).build();
	}

	private ConsentUpdateResponse  getConsentUpdateResponse() {
		return ConsentUpdateResponse.builder().status(new Status(1000, "success", "desc of inq call")).build();
	}

	private PdpaValidateResponse getPdpaValidationResponse() {
		PdpaValidateData validateData = PdpaValidateData.builder()
				.customerId("RR10001")
				.build();
		return PdpaValidateResponse.builder().data(validateData).status(new Status(1000, "success", "desc of inq call"))
				.build();
	}
	
	private List<ConsentCollectionInfo> getConsentList(){
		ConsentCollectionInfo crossSellUpdate = new ConsentCollectionInfo("001", 1.01, true);
		ConsentCollectionInfo marketingUpdate = new ConsentCollectionInfo("002", 1.01, false);
		return Arrays.asList(marketingUpdate, crossSellUpdate);
	}
}
