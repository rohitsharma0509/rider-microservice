package com.scb.rider.controller;

import com.scb.rider.constants.Constants;
import com.scb.rider.model.document.RiderDrivingLicenseDocument;
import com.scb.rider.model.dto.RiderDrivingLicenseRequest;
import com.scb.rider.model.dto.RiderDrivingLicenseResponse;
import com.scb.rider.service.document.RiderDrivingLicenseDocumentService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
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

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Optional;

import static com.scb.rider.constants.UrlMappings.RIDER_API;
import static com.scb.rider.constants.UrlMappings.RiderDrivingLicense.RIDER_DRIVING_LICENSE;
import static com.scb.rider.constants.UrlMappings.RiderDrivingLicense.RIDER_DRIVING_LICENSE_BY_ID;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(RIDER_API)
public class RiderDrivingLicenseController {

    @Autowired
    private RiderDrivingLicenseDocumentService drivingLicenseDocumentService;


    @PostMapping(value = RIDER_DRIVING_LICENSE, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    // @formatter:off
    @ApiOperation(nickname = "create-rider-driving-license",
            value = "Creates Rider Driving License Details Records",
            notes = "",
            response = RiderDrivingLicenseResponse.class
    )
    @ApiResponses(value = {
            @ApiResponse(response = RiderDrivingLicenseResponse.class, code = 201, message = "One records created successfully"),
            @ApiResponse(code = 400, message = "Could not create records for supplied input")
    }
    )
    @ApiImplicitParam(name = "id", dataType = "String", paramType = "path",
            dataTypeClass = String.class,
            value = "Rider Profile ID Details"
    )
    @Valid
    public ResponseEntity<RiderDrivingLicenseResponse> createRiderDrivingLicenseDetails(
            @RequestAttribute(name = Constants.X_USER_ID, required = false) String userId,
            final @PathVariable(name = "id", required = true) String id,
            @RequestBody @Valid @NotEmpty final @NotNull RiderDrivingLicenseRequest licenseRequest) {
        //TO DO  FIRST CHECK Profile ID Exist or not
        licenseRequest.setUpdateBy(userId);
        RiderDrivingLicenseDocument  riderDrivingLicenseResponse;
        Optional<RiderDrivingLicenseDocument>  drivingLicenseDocument = drivingLicenseDocumentService.findRiderDrivingLicenseByProfileId(id);
        if(drivingLicenseDocument.isPresent()){
             riderDrivingLicenseResponse = drivingLicenseDocumentService
                    .updateRiderDrivingLicense(licenseRequest, drivingLicenseDocument.get());
        }else {
            RiderDrivingLicenseDocument drivingLicenseDocumentRequest = RiderDrivingLicenseDocument.builder()
                    .drivingLicenseNumber(licenseRequest.getDrivingLicenseNumber())
                    .dateOfExpiry(licenseRequest.getDateOfExpiry())
                    .dateOfIssue(licenseRequest.getDateOfIssue())
                    .riderProfileId(id)
                    .documentUrl(licenseRequest.getDocumentUrl())
                    .reason(licenseRequest.getReason())
                    .updatedBy(licenseRequest.getUpdateBy())
                    .typeOfLicense(licenseRequest.getTypeOfLicense()).build();

             riderDrivingLicenseResponse = drivingLicenseDocumentService
                    .createRiderDrivingLicense(drivingLicenseDocumentRequest);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(RiderDrivingLicenseResponse.of(riderDrivingLicenseResponse));

    }

    @GetMapping(value = RIDER_DRIVING_LICENSE, produces = MediaType.APPLICATION_JSON_VALUE)
    // @formatter:off
    @ApiOperation(nickname = "get-rider-driving-license-by-profile-id",
            consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE,
            value = "Gets Rider Driving License Details record by ID",
            response = RiderDrivingLicenseResponse.class,
            notes = ""
    )
    @Valid
    public ResponseEntity<RiderDrivingLicenseResponse> getDrivingLicenseProfileById(
            @ApiParam(value = "Profile id", example = "0a800160-6c23-121e-816c-2737d6610003", required = true)
            @PathVariable(name = "id", required = true) @NotEmpty final String id) {
        // @formatter:on
        Optional<RiderDrivingLicenseDocument> drivingLicenseDocument = this.drivingLicenseDocumentService.findRiderDrivingLicenseByProfileId(id);
        if (!drivingLicenseDocument.isPresent()) {
            HttpHeaders headers = new HttpHeaders();
            headers.add(Constants.X_INFO, "Rider Driving License Details not found with given id");
            return ResponseEntity.notFound().headers(headers).build();
        } else {
            return ResponseEntity.ok(RiderDrivingLicenseResponse.of(drivingLicenseDocument.get()));
        }
    }


    @GetMapping(value = RIDER_DRIVING_LICENSE_BY_ID, produces = MediaType.APPLICATION_JSON_VALUE)
    // @formatter:off
    @ApiOperation(nickname = "get-rider-driving-license-by-id",
            consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE,
            value = "Gets Rider Driving License Details record by ID",
            response = RiderDrivingLicenseResponse.class,
            notes = ""
    )
    @ApiImplicitParam(name = "id", dataType = "String", paramType = "path",
            dataTypeClass = String.class,
            value = "Rider Profile ID Details"
    )
    @Valid
    public ResponseEntity<RiderDrivingLicenseResponse> getDrivingLicenseById(
            @ApiParam(value = "Record id", example = "0a800160-6c23-121e-816c-2737d6610003", required = true)
            @PathVariable(name = "id", required = true) @NotEmpty final String id) {
        // @formatter:on
        Optional<RiderDrivingLicenseDocument> drivingLicenseDocument = this.drivingLicenseDocumentService.findRiderDrivingLicenseById(id);
        if (!drivingLicenseDocument.isPresent()) {
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-info", "Rider Driving License Details not found with given id");
            return ResponseEntity.notFound().headers(headers).build();
        } else {
            return ResponseEntity.ok(RiderDrivingLicenseResponse.of(drivingLicenseDocument.get()));
        }
    }

    @PutMapping(value = RIDER_DRIVING_LICENSE, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    // @formatter:off
    @ApiOperation(nickname = "update-rider-driving-license",
            value = "Update Rider Driving License Details Records",
            notes = "",
            response = RiderDrivingLicenseResponse.class
    )
    @ApiResponses(value = {
            @ApiResponse(response = Void.class, code = 201, message = "One records created successfully"),
            @ApiResponse(code = 400, message = "Could not update records for supplied input")
    }
    )
    @ApiImplicitParam(name = "id", dataType = "String", paramType = "path",
            dataTypeClass = String.class,
            value = "Rider Profile ID Details"
    )
    @Valid
    public ResponseEntity<RiderDrivingLicenseResponse> updateRiderDrivingLicenseDetails(
            @RequestAttribute(name = Constants.X_USER_ID, required = false) String userId,
            final @PathVariable(name = "id", required = true) String id,
            @RequestBody @Valid @NotEmpty final @NotNull RiderDrivingLicenseRequest licenseRequest) {
        //TO DO  FIRST CHECK Profile ID Exist or not
        licenseRequest.setUpdateBy(userId);
        Optional<RiderDrivingLicenseDocument>  drivingLicenseDocument = drivingLicenseDocumentService.findRiderDrivingLicenseByProfileId(id);

        if(!drivingLicenseDocument.isPresent()){
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-info", "Rider Driving License Details not found with given profile id");
            return ResponseEntity.notFound().headers(headers).build();
        }

        RiderDrivingLicenseDocument riderDrivingLicenseResponse = drivingLicenseDocumentService
                .updateRiderDrivingLicense(licenseRequest, drivingLicenseDocument.get());

        return ResponseEntity.status(HttpStatus.OK).body(RiderDrivingLicenseResponse.of(riderDrivingLicenseResponse));

    }
}
