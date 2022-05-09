package com.scb.rider.controller;

import com.scb.rider.model.dto.RiderEmergencyContactDto;
import com.scb.rider.service.document.RiderEmergencyContactService;
import io.swagger.annotations.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@Log4j2
@RequestMapping("/profile/emergency-contact")
@Api(value = "Rider Profile Emergency Contact Endpoints")
public class RiderEmergencyContactController {
    @Autowired
    private RiderEmergencyContactService emergencyContactService;

    @ApiOperation(nickname = "create-rider-profile-emergency-contact-details", value = "Creates Rider Profile emergency contact Details", response = RiderEmergencyContactDto.class)
    @ApiResponses(value = {
            @ApiResponse(response = RiderEmergencyContactDto.class, code = 200, message = "One record created successfully"),
            @ApiResponse(code = 400, message = "Could not create records for supplied input")
    })
    @ApiImplicitParam(name = "id", dataType = "String", paramType = "path", dataTypeClass = RiderEmergencyContactDto.class, value = "Rider Profile Emergency Contact Details")
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RiderEmergencyContactDto> saveEmergencyContact(@RequestBody @Valid final RiderEmergencyContactDto emergencyContactDto) {
        log.info("Creating rider with id {} name = {}", emergencyContactDto.getProfileId(), emergencyContactDto.getName());
        return ResponseEntity.ok(RiderEmergencyContactDto.of(this.emergencyContactService.saveEmergencyContact(emergencyContactDto)));
    }

    @ApiOperation(nickname = "get-rider-profile-emergency-contact-by-id",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE,
            value = "Gets Rider profile emergency contact by ID", response = RiderEmergencyContactDto.class
    )
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RiderEmergencyContactDto> getRiderEmergencyContactByProfileId(
            @ApiParam(value = "id", example = "5fc35ef7af8a144ac42a0a54", required = true)
            @PathVariable("id") String id) {
        log.info("Getting Rider Emergency Contact by id = {}", id);
        return ResponseEntity.ok(RiderEmergencyContactDto.of(this.emergencyContactService.getEmergencyContactByProfileId(id)));
    }

}