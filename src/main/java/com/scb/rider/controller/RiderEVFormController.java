package com.scb.rider.controller;

import static com.scb.rider.constants.UrlMappings.RiderEVForm.RIDER_EV_FORM;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.scb.rider.model.document.RiderEVForm;
import com.scb.rider.model.dto.RiderEVFormDto;
import com.scb.rider.service.document.RiderEVFormService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping(RIDER_EV_FORM)
public class RiderEVFormController {

	@Autowired
	private RiderEVFormService evFormService;

	@ApiOperation(nickname = "get-rider-ev-form-details", value = "Get Call for Rider EV Form", response = RiderEVForm.class)
	@ApiResponses(value = { @ApiResponse(response = RiderEVForm.class, code = 200, message = "Rider EV Form fetched"),
			@ApiResponse(code = 500, message = "Could not update Rider EV Form") })
	@GetMapping(value = "/{riderProfileId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RiderEVForm> getRiderEVForm(@PathVariable("riderProfileId") String riderProfileId) {
		return ResponseEntity.ok(evFormService.getRiderEVForm(riderProfileId));
	}

	@ApiOperation(nickname = "save-rider-ev-form-details", value = "Save Call for Rider EV Form", response = RiderEVForm.class)
	@ApiResponses(value = { @ApiResponse(response = RiderEVForm.class, code = 200, message = "Rider EV Form fetched"),
			@ApiResponse(code = 500, message = "Could not save Rider EV Form") })
	@PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RiderEVForm> saveRiderEVForm(@RequestBody @Valid RiderEVFormDto evForm) {
		return ResponseEntity.ok(evFormService.saveRiderEVForm(evForm));
	}
}
