package com.scb.rider.controller;

import static com.scb.rider.constants.UrlMappings.RIDER_API;
import static com.scb.rider.constants.UrlMappings.RiderBackgroundVerification.RIDER_BACKGROUND_VERIFICATION;
import static com.scb.rider.constants.UrlMappings.RiderBackgroundVerification.RIDER_BACKGROUND_VERIFICATION_BY_RIDER;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.scb.rider.constants.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.scb.rider.model.document.RiderBackgroundVerificationDocument;
import com.scb.rider.model.dto.RiderBackgroundVerificationDetailsRequest;
import com.scb.rider.model.dto.RiderBackgroundVerificationDetailsResponse;
import com.scb.rider.model.dto.RiderDrivingLicenseResponse;
import com.scb.rider.service.document.RiderBackgroundVerificationDocumentService;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping(RIDER_API)
public class RiderBackgroundVerificationController {

	@Autowired
	private RiderBackgroundVerificationDocumentService backgroundVerificationDocumentService;

	@PostMapping(value = RIDER_BACKGROUND_VERIFICATION, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(nickname = "add-background-verification-details", value = "Add Rider Background Verification Details", notes = "", response = RiderBackgroundVerificationDetailsResponse.class)
	@ApiResponses(value = {
			@ApiResponse(response = RiderBackgroundVerificationDetailsResponse.class, code = 201, message = "One records created successfully"),
			@ApiResponse(code = 400, message = "Could not create records for supplied input") })
	@ApiImplicitParam(name = "id", dataType = "String", paramType = "path", dataTypeClass = String.class, value = "Rider Profile ID ")
	@Valid
	public ResponseEntity<RiderBackgroundVerificationDetailsResponse> addRiderBackgroundVerificationDetails(
			@RequestAttribute(name = Constants.X_USER_ID, required = false) String userId,
			final @PathVariable(name = "id", required = true) String id,
			@RequestBody @Valid @NotEmpty final @NotNull RiderBackgroundVerificationDetailsRequest riderBackgroundVerificationDetailsRequest) {
		riderBackgroundVerificationDetailsRequest.setUpdatedBy(userId);
		RiderBackgroundVerificationDetailsResponse riderBackgroundVerificationDetailsResponse = backgroundVerificationDocumentService
				.addBackgroundVerificationDetails(id, riderBackgroundVerificationDetailsRequest, Constants.OPS);

		return ResponseEntity.status(HttpStatus.CREATED).body(riderBackgroundVerificationDetailsResponse);
	}

	@GetMapping(value = RIDER_BACKGROUND_VERIFICATION, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(nickname = "get-rider-background-verification-details-by-profile-id", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE, value = "Get rider background verification details by ID", response = RiderBackgroundVerificationDetailsResponse.class, notes = "")
	@Valid
	public ResponseEntity<RiderBackgroundVerificationDetailsResponse> getBackgroundVerificationDetailsByProfileId(
			@ApiParam(value = "Profile id", example = "0a800160-6c23-121e-816c-2737d6610003", required = true) @PathVariable(name = "id", required = true) @NotEmpty final String id) {
		RiderBackgroundVerificationDocument backgroundVerificationDocument = backgroundVerificationDocumentService
				.getBackgroundVerificationDetailsByProfileId(id);

		return ResponseEntity.ok(RiderBackgroundVerificationDetailsResponse.of(backgroundVerificationDocument));
	}

	@PutMapping(value = RIDER_BACKGROUND_VERIFICATION, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(nickname = "update-rider-background-verification-details", value = "Update Rider Background Verification details", notes = "", response = RiderDrivingLicenseResponse.class)
	@ApiResponses(value = {
			@ApiResponse(response = Void.class, code = 201, message = "One records created successfully"),
			@ApiResponse(code = 400, message = "Could not update records for supplied input") })
	@ApiImplicitParam(name = "id", dataType = "String", paramType = "path", dataTypeClass = String.class, value = "Rider Profile ID Details")
	@Valid
	public ResponseEntity<RiderBackgroundVerificationDetailsResponse> updateRiderBackgroundVerificationDetails(
			@RequestAttribute(name = Constants.X_USER_ID, required = false) String userId,
			final @PathVariable(name = "id", required = true) String id,
			@RequestBody @Valid @NotEmpty final @NotNull RiderBackgroundVerificationDetailsRequest request) {
		request.setUpdatedBy(userId);
		RiderBackgroundVerificationDocument backgroundVerificationDocument = backgroundVerificationDocumentService
				.getBackgroundVerificationDetailsByProfileId(id);

		RiderBackgroundVerificationDocument updatedBackgroundVerificationDocument = backgroundVerificationDocumentService
				.updateBackgroundVerificationDetails(request, backgroundVerificationDocument, Constants.OPS);

		return ResponseEntity.status(HttpStatus.OK)
				.body(RiderBackgroundVerificationDetailsResponse.of(updatedBackgroundVerificationDocument));

	}

	@PostMapping(value = RIDER_BACKGROUND_VERIFICATION_BY_RIDER, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(nickname = "add-background-verification-details-by-rider", value = "Add Rider Background Verification Details by Rider", notes = "", response = RiderBackgroundVerificationDetailsResponse.class)
	@ApiResponses(value = {
			@ApiResponse(response = RiderBackgroundVerificationDetailsResponse.class, code = 201, message = "One records created successfully"),
			@ApiResponse(code = 400, message = "Could not create records for supplied input") })
	@ApiImplicitParam(name = "id", dataType = "String", paramType = "path", dataTypeClass = String.class, value = "Rider Profile ID ")
	@Valid
	public ResponseEntity<RiderBackgroundVerificationDetailsResponse> addRiderBackgroundVerificationDetailsByRider(
			@RequestAttribute(name = Constants.X_USER_ID, required = false) String userId,
			final @PathVariable(name = "id") String id,
			@RequestBody @Valid @NotEmpty final @NotNull RiderBackgroundVerificationDetailsRequest riderBackgroundVerificationDetailsRequest) {
		riderBackgroundVerificationDetailsRequest.setUpdatedBy(userId);
		RiderBackgroundVerificationDetailsResponse riderBackgroundVerificationDetailsResponse = backgroundVerificationDocumentService
				.addBackgroundVerificationDetails(id, riderBackgroundVerificationDetailsRequest, Constants.RIDER);

		return ResponseEntity.status(HttpStatus.CREATED).body(riderBackgroundVerificationDetailsResponse);
	}
}
