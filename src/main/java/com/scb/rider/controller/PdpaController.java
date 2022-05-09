package com.scb.rider.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.scb.rider.model.pdpa.ConsentManagementDto;
import com.scb.rider.model.pdpa.ConsentUpdateDto;
import com.scb.rider.model.pdpa.ConsentUpdateResponse;
import com.scb.rider.model.pdpa.ManagementInquiryDto;
import com.scb.rider.model.pdpa.ManagementInquiryResponse;
import com.scb.rider.model.pdpa.PdpaValidateDto;
import com.scb.rider.model.pdpa.PdpaValidateResponse;
import com.scb.rider.service.PdpaService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/pdpa/consent")
@Api(value = "PDPA Controller")
public class PdpaController {

	@Autowired
	private PdpaService pdpaService;

	@ApiOperation(nickname = "rider-pdpa-inquiry", value = "PDPA Inquiry Call", response = ManagementInquiryResponse.class)
	@ApiResponses(value = {
			@ApiResponse(response = ManagementInquiryResponse.class, code = 200, message = "Inquired consent"),
			@ApiResponse(code = 500, message = "Could not inquire consent for supplied input") })
	@PostMapping(value = "/inquiry", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ManagementInquiryResponse> riderManagementInquiry(
			@RequestBody @Valid ManagementInquiryDto inquiryRequest) {
		return ResponseEntity.status(HttpStatus.OK).body(pdpaService.riderInquiryConsent(inquiryRequest));
	}

	@ApiOperation(nickname = "rider-pdpa-validate", value = "PDPA validate Call", response = PdpaValidateResponse.class)
	@ApiResponses(value = {
			@ApiResponse(response = PdpaValidateResponse.class, code = 200, message = "validated consent"),
			@ApiResponse(code = 500, message = "Could not validate consent for supplied input") })
	@PostMapping(value = "/validate", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<PdpaValidateResponse> riderConsentValidation(
			@RequestBody @Valid PdpaValidateDto validationRequest) {
		return ResponseEntity.status(HttpStatus.OK).body(pdpaService.riderValidateConsent(validationRequest));
	}

	@ApiOperation(nickname = "rider-pdpa-consent-save", value = "PDPA consent save Call", response = ConsentUpdateResponse.class)
	@ApiResponses(value = {
			@ApiResponse(response = ConsentUpdateResponse.class, code = 200, message = "Updated consent"),
			@ApiResponse(code = 500, message = "Could not update consent for supplied input") })
	@PostMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ConsentUpdateResponse> riderConsentUpdate(
			@RequestBody @Valid ConsentUpdateDto updateRequest) {
		return ResponseEntity.status(HttpStatus.OK).body(pdpaService.updateRiderConsent(updateRequest))	;
	}
	
	@ApiOperation(nickname = "rider-pdpa-consent-modify", value = "PDPA consent modify Call", response = ConsentUpdateResponse.class)
	@ApiResponses(value = {
			@ApiResponse(response = ConsentUpdateResponse.class, code = 200, message = "Modified consent"),
			@ApiResponse(code = 500, message = "Could not modify consent for supplied input") })
	@PutMapping(value = "/management", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ConsentUpdateResponse> riderConsentManagement(
			@RequestBody @Valid ConsentManagementDto updateRequest) {
		return ResponseEntity.status(HttpStatus.OK).body(pdpaService.riderConsentManagement(updateRequest))	;
	}
}
