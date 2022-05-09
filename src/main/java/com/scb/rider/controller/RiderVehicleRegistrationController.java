package com.scb.rider.controller;

import com.scb.rider.constants.Constants;
import com.scb.rider.model.document.RiderVehicleRegistrationDocument;
import com.scb.rider.model.dto.FoodCartUpdateRequest;
import com.scb.rider.model.dto.RiderVehicleStatusRequest;
import com.scb.rider.model.dto.RiderDrivingLicenseResponse;
import com.scb.rider.model.dto.RiderVehicleRegistrationDetailsRequest;
import com.scb.rider.model.dto.RiderVehicleRegistrationDetailsResponse;
import com.scb.rider.service.document.RiderVehicleRegistrationService;
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
import static com.scb.rider.constants.UrlMappings.RiderVehicleRegistration.RIDER_FOODCARD_SIZE_UPDATE;
import static com.scb.rider.constants.UrlMappings.RiderVehicleRegistration.RIDER_VEHICLE_REGISTRATION;
import static com.scb.rider.constants.UrlMappings.RiderVehicleRegistration.RIDER_VEHICLE_STATUS_UPDATE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(RIDER_API)
public class RiderVehicleRegistrationController {

    @Autowired
    private RiderVehicleRegistrationService vehicleRegistrationService;


    @PostMapping(value = RIDER_VEHICLE_REGISTRATION, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    // @formatter:off
    @ApiOperation(nickname = "create-rider-vehicle-registration",
            value = "Creates Rider Vehicle Registration Details Records",
            notes = "",
            response = RiderVehicleRegistrationDetailsResponse.class
    )
    @ApiResponses(value = {
            @ApiResponse(response = RiderVehicleRegistrationDetailsResponse.class, code = 201, message = "One records created successfully"),
            @ApiResponse(code = 400, message = "Could not create records for supplied input")
    }
    )
    @ApiImplicitParam(name = "id", dataType = "String", paramType = "path",
            dataTypeClass = String.class,
            value = "Rider Profile ID "
    )
    @Valid
    public ResponseEntity<RiderVehicleRegistrationDetailsResponse> createRiderVehicleRegistrationDetails(
            @RequestAttribute(name = Constants.X_USER_ID, required = false) String userId,
                       final @PathVariable(name = "id", required = true) String id,
                       @RequestBody @Valid @NotEmpty final @NotNull RiderVehicleRegistrationDetailsRequest riderVehicleRegistrationDetailsRequest) {
        riderVehicleRegistrationDetailsRequest.setUpdatedBy(userId);
        RiderVehicleRegistrationDetailsResponse riderVehicleRegistrationResponse = vehicleRegistrationService
                .createRiderVehicleRegistrationDetails(id,riderVehicleRegistrationDetailsRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(riderVehicleRegistrationResponse);

    }

    @GetMapping(value = RIDER_VEHICLE_REGISTRATION, produces = MediaType.APPLICATION_JSON_VALUE)
    // @formatter:off
    @ApiOperation(nickname = "get-rider-vehicle-registration-by-profile-id",
            consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE,
            value = "Gets Rider Driving License Details record by ID",
            response = RiderDrivingLicenseResponse.class,
            notes = ""
    )
    @Valid
    public ResponseEntity<RiderVehicleRegistrationDetailsResponse> getVehicleRegistrationByProfileId(
            @ApiParam(value = "Profile id", example = "0a800160-6c23-121e-816c-2737d6610003", required = true)
            @PathVariable(name = "id", required = true) @NotEmpty final String id) {
        // @formatter:on
        Optional<RiderVehicleRegistrationDocument> vehicleRegistrationDocument = this.vehicleRegistrationService
                .findRiderVehicleRegistrationDetailsByProfileId(id);
        if (!vehicleRegistrationDocument.isPresent()) {
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-info", "Rider Vehicle Registration Details not found with given id");
            return ResponseEntity.notFound().headers(headers).build();
        } else {
            return ResponseEntity.ok(RiderVehicleRegistrationDetailsResponse.of(vehicleRegistrationDocument.get()));
        }
    }

    @PutMapping(value = RIDER_VEHICLE_REGISTRATION, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    // @formatter:off
    @ApiOperation(nickname = "update-rider-vehicle-registration-details",
            value = "Update Rider Vehicle Registration Details Records",
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
    public ResponseEntity<RiderVehicleRegistrationDetailsResponse> updateVehicleRegistrationDetails(
            @RequestAttribute(name = Constants.X_USER_ID, required = false) String userId,
            final @PathVariable(name = "id", required = true) String id,
            @RequestBody @Valid @NotEmpty final @NotNull RiderVehicleRegistrationDetailsRequest request) {
        request.setUpdatedBy(userId);
        //TO DO  FIRST CHECK Profile ID Exist or not
        Optional<RiderVehicleRegistrationDocument>  riderVehicleRegistrationDocument = vehicleRegistrationService.findRiderVehicleRegistrationDetailsByProfileId(id);

        if(!riderVehicleRegistrationDocument.isPresent()){
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-info", "Rider Vehicle Registration Details not found with given profile id");
            return ResponseEntity.notFound().headers(headers).build();
        }
        RiderVehicleRegistrationDocument vehicleRegistrationDocument  = vehicleRegistrationService
                .updateRiderVehicleRegistrationDetails(request, riderVehicleRegistrationDocument.get());

        return ResponseEntity.status(HttpStatus.OK).body(RiderVehicleRegistrationDetailsResponse.of(vehicleRegistrationDocument));

    }

    @PutMapping(value = RIDER_VEHICLE_STATUS_UPDATE, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    // @formatter:off
    @ApiOperation(nickname = "update-rider-vehicle-status",
            value = "Update Rider Vehicle Registration status and food card status",
            notes = "",
            response = RiderVehicleRegistrationDetailsResponse.class
    )
    @ApiResponses(value = {
            @ApiResponse(response = Void.class, code = 200, message = "One records updated successfully"),
            @ApiResponse(code = 400, message = "Could not update records for supplied input")
    }
    )
    @ApiImplicitParam(name = "id", dataType = "String", paramType = "path",
            dataTypeClass = String.class,
            value = "Rider Profile ID Details"
    )
    @Valid
    public ResponseEntity<RiderVehicleRegistrationDetailsResponse> updateVehicleRegistrationStatus(final @PathVariable(name = "id", required = true) String id,
                                                                                                   @RequestBody @Valid @NotEmpty final RiderVehicleStatusRequest riderVehicleStatusRequest){

        Optional<RiderVehicleRegistrationDocument>  riderVehicleRegistrationDocument = vehicleRegistrationService.findRiderVehicleRegistrationDetailsByProfileId(id);

        if(!riderVehicleRegistrationDocument.isPresent()){
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-info", "Rider Vehicle Registration Details not found with given profile id");
            return ResponseEntity.notFound().headers(headers).build();
        }

        RiderVehicleRegistrationDocument vehicleRegistrationDocument = vehicleRegistrationService.updateVehicleRegistrationStatus(riderVehicleStatusRequest, riderVehicleRegistrationDocument.get());
        return ResponseEntity.status(HttpStatus.OK).body(RiderVehicleRegistrationDetailsResponse.of(vehicleRegistrationDocument));
    }

    @PutMapping(value = RIDER_FOODCARD_SIZE_UPDATE, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RiderVehicleRegistrationDetailsResponse> updateFoodBoxSize(
            @RequestAttribute(name = Constants.X_USER_ID, required = false) String userId,
            @PathVariable(name = "id") String profileId,
            @RequestBody @Valid @NotEmpty FoodCartUpdateRequest foodCartUpdateRequest) {
        RiderVehicleRegistrationDocument document = vehicleRegistrationService.updateFoodBoxSize(profileId, userId, foodCartUpdateRequest);
        return ResponseEntity.ok(RiderVehicleRegistrationDetailsResponse.of(document));
    }
}
