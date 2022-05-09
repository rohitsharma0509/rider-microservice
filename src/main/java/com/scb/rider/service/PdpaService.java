package com.scb.rider.service;

import java.util.Arrays;
import java.util.List;
import java.util.Locale.LanguageRange;
import java.util.Objects;
import java.util.UUID;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.scb.rider.constants.Constants;
import com.scb.rider.exception.PdpaException;
import com.scb.rider.model.pdpa.ConsentManagementDto;
import com.scb.rider.model.pdpa.ConsentManagementRequest;
import com.scb.rider.model.pdpa.ConsentUpdateDto;
import com.scb.rider.model.pdpa.ConsentUpdateRequest;
import com.scb.rider.model.pdpa.ConsentUpdateResponse;
import com.scb.rider.model.pdpa.ManagementInquiryDto;
import com.scb.rider.model.pdpa.ManagementInquiryRequest;
import com.scb.rider.model.pdpa.ManagementInquiryResponse;
import com.scb.rider.model.pdpa.PdpaValidateDto;
import com.scb.rider.model.pdpa.PdpaValidateRequest;
import com.scb.rider.model.pdpa.PdpaValidateResponse;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PdpaService {

	@Value("${pdpa.baseUrl}")
	private String pdpaBaseUrl;

	@Value("${pdpa.uri.inquiry}")
	private String pdpaInquiryPath;

	@Value("${pdpa.uri.management}")
	private String pdpaManagementPath;

	@Value("${pdpa.uri.validate}")
	private String pdpaValidatePath;

	@Value("${pdpa.uri.consent}")
	private String pdpaConsentPath;

	private RestTemplate restTemplate;

	@Autowired
	public PdpaService() {
		this.restTemplate = new RestTemplate();
	}

	public ManagementInquiryResponse riderInquiryConsent(ManagementInquiryDto inquiryDto) {
		String uri = pdpaBaseUrl.concat(pdpaInquiryPath);
		ManagementInquiryRequest request = ManagementInquiryRequest.builder().channelCode(Constants.RIDER_CHANNEL_CODE)
				.customerId(inquiryDto.getRiderId()).referenceType(Constants.THAI_NATIONAL_ID_CODE)
				.documentId(inquiryDto.getDocumentId())
				.consentType(Arrays.asList(Constants.MARKETING_CONSENT_CODE, Constants.CROSS_SELL_CONSENT_CODE))
				.build();

		HttpHeaders header = generateHeaders();

		HttpEntity<ManagementInquiryRequest> entity = new HttpEntity<>(request, header);

		ResponseEntity<ManagementInquiryResponse> riderInquiryResponse = null;
		try {
			riderInquiryResponse = restTemplate.postForEntity(uri, entity, ManagementInquiryResponse.class);
		} catch (Exception ex) {
			throw new PdpaException("Exception calling inquiry() to centralised PDPA Server", ex);
		}
		if (Objects.isNull(riderInquiryResponse) || Objects.isNull(riderInquiryResponse.getBody())) {
			throw new PdpaException("Inquiry Response is null for Rider : " + inquiryDto.getRiderId());
		}
		log.info("Response:{} For Rider:- {} Inquiry response status:- {}", riderInquiryResponse.getStatusCode(),
				inquiryDto.getRiderId(), riderInquiryResponse.getBody().getStatus());
		return riderInquiryResponse.getBody();
	}

	public ConsentUpdateResponse riderConsentManagement(@Valid ConsentManagementDto consentManagementDto) {
		String uri = pdpaBaseUrl.concat(pdpaManagementPath);

		ConsentManagementRequest request = ConsentManagementRequest.builder().channelCode(Constants.RIDER_CHANNEL_CODE)
				.customerId(consentManagementDto.getRiderId())
				.consentCollectionInfo(consentManagementDto.getConsentCollectionInfo()).build();

		HttpHeaders header = generateHeaders();

		HttpEntity<ConsentManagementRequest> entity = new HttpEntity<>(request, header);

		ResponseEntity<ConsentUpdateResponse> consentManagementResponse = null;
		try {
			consentManagementResponse = restTemplate.exchange(uri, HttpMethod.PUT, entity, ConsentUpdateResponse.class);
		} catch (Exception ex) {
			throw new PdpaException("Exception calling management() to centralised PDPA Server", ex);
		}
		if (Objects.isNull(consentManagementResponse) || Objects.isNull(consentManagementResponse.getBody())) {
			throw new PdpaException("Consent Update Response is null for Rider " + consentManagementDto.getRiderId());
		}
		log.info("Response:{} For Rider:- {} Consent Update status:- {}", consentManagementResponse.getStatusCode(),
				consentManagementDto.getRiderId(), consentManagementResponse.getBody().getStatus());
		return consentManagementResponse.getBody();
	}

	public ConsentUpdateResponse updateRiderConsent(ConsentUpdateDto consentUpdateDto) {
		String uri = pdpaBaseUrl.concat(pdpaConsentPath);

		ConsentUpdateRequest request = ConsentUpdateRequest.builder().channelCode(Constants.RIDER_CHANNEL_CODE)
				.customerId(consentUpdateDto.getRiderId()).referenceType(Constants.THAI_NATIONAL_ID_CODE)
				.documentId(consentUpdateDto.getDocumentId()).dateOfBirth(consentUpdateDto.getDateOfBirth())
				.consentCollectionInfo(consentUpdateDto.getConsentCollectionInfo()).callBackURL(null).build();

		HttpHeaders header = generateHeaders();

		HttpEntity<ConsentUpdateRequest> entity = new HttpEntity<>(request, header);

		ResponseEntity<ConsentUpdateResponse> consentUpdateResponse = null;
		try {
			consentUpdateResponse = restTemplate.postForEntity(uri, entity, ConsentUpdateResponse.class);
		} catch (Exception ex) {
			throw new PdpaException("Exception calling update() to centralised PDPA Server", ex);
		}
		if (Objects.isNull(consentUpdateResponse) || Objects.isNull(consentUpdateResponse.getBody())) {
			throw new PdpaException("Consent Update Response is null for Rider " + consentUpdateDto.getRiderId());
		}
		log.info("Response:{} For Rider:- {} Consent Update status:- {}", consentUpdateResponse.getStatusCode(),
				consentUpdateDto.getRiderId(), consentUpdateResponse.getBody().getStatus());
		return consentUpdateResponse.getBody();
	}

	public PdpaValidateResponse riderValidateConsent(PdpaValidateDto pdpaValidateDto) {
		String uri = pdpaBaseUrl.concat(pdpaValidatePath);
		PdpaValidateRequest request = PdpaValidateRequest.builder().channelCode(Constants.RIDER_CHANNEL_CODE)
				.customerId(pdpaValidateDto.getRiderId()).referenceType(Constants.THAI_NATIONAL_ID_CODE)
				.documentId(pdpaValidateDto.getDocumentId())
				.consentType(Arrays.asList(Constants.MARKETING_CONSENT_CODE, Constants.CROSS_SELL_CONSENT_CODE))
				.dateOfBirth(pdpaValidateDto.getDateOfBirth()).build();

		HttpHeaders header = generateHeaders();

		HttpEntity<PdpaValidateRequest> entity = new HttpEntity<>(request, header);

		ResponseEntity<PdpaValidateResponse> riderValidationResponse = null;
		try {
			riderValidationResponse = restTemplate.postForEntity(uri, entity, PdpaValidateResponse.class);
		} catch (Exception ex) {
			throw new PdpaException("Exception calling validate() to centralised PDPA Server", ex);
		}
		if (Objects.isNull(riderValidationResponse) || Objects.isNull(riderValidationResponse.getBody())) {
			throw new PdpaException("Validate Response is null for Rider : " + pdpaValidateDto.getRiderId());
		}
		log.info("Response:{} For Rider:- {} Validate response status:- {}", riderValidationResponse.getStatusCode(),
				pdpaValidateDto.getRiderId(), riderValidationResponse.getBody().getStatus());
		return riderValidationResponse.getBody();
	}

	private HttpHeaders generateHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("requestuid", UUID.randomUUID().toString());
		List<LanguageRange> languages = Arrays.asList(new LanguageRange("th"));
		headers.setAcceptLanguage(languages);
		log.info("Generated headers: {}", headers);
		return headers;
	}

}