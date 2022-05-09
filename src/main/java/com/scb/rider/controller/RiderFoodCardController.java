package com.scb.rider.controller;

import com.scb.rider.model.document.RiderFoodCard;
import com.scb.rider.model.dto.RiderFoodCardRequest;
import com.scb.rider.model.dto.RiderFoodCardResponse;
import com.scb.rider.service.document.RiderFoodCardService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import static com.scb.rider.constants.UrlMappings.RIDER_API;
import static com.scb.rider.constants.UrlMappings.RiderVehicleRegistration.RIDER_FOODCARD_DETAILS;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(RIDER_API)
public class RiderFoodCardController {

    @Autowired
    private RiderFoodCardService foodCardService;

    @PostMapping(value = RIDER_FOODCARD_DETAILS, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(nickname = "add-food-card-details", value = "Add Rider food card Details", notes = "", response = RiderFoodCardResponse.class)
    @ApiResponses(value = {
            @ApiResponse(response = RiderFoodCardResponse.class, code = 201, message = "One records created successfully"),
            @ApiResponse(code = 400, message = "Could not create records for supplied input") })
    @ApiImplicitParam(name = "id", dataType = "String", paramType = "path", dataTypeClass = String.class, value = "Rider Profile ID ")
    @Valid
    public ResponseEntity<RiderFoodCardResponse> addRiderFoodCardDetails(
            final @PathVariable(name = "id", required = true) String id,
            @RequestBody @Valid @NotEmpty final @NotNull RiderFoodCardRequest riderFoodCardRequest) {
        RiderFoodCardResponse foodCardResponse = foodCardService
                .addFoodCardDetails(id, riderFoodCardRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(foodCardResponse);
    }

    @GetMapping(value = RIDER_FOODCARD_DETAILS, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(nickname = "get-rider-food-card-details-by-profile-id", produces = APPLICATION_JSON_VALUE, value = "Get rider food card details by ID", response = RiderFoodCardResponse.class, notes = "")
    @Valid
    public ResponseEntity<RiderFoodCardResponse> getFoodCardDetailsByProfileId(
            @ApiParam(value = "Profile id", example = "0a800160-6c23-121e-816c-2737d6610003", required = true) @PathVariable(name = "id", required = true) @NotEmpty final String id) {
        RiderFoodCard foodCardDocument = foodCardService
                .getFoodCardDetailsByProfileId(id);

        return ResponseEntity.ok(RiderFoodCardResponse.of(foodCardDocument));
    }

    @PutMapping(value = RIDER_FOODCARD_DETAILS, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(nickname = "update-rider-food-card-details", value = "Update Rider food card details", notes = "", response = RiderFoodCardResponse.class)
    @ApiResponses(value = {
            @ApiResponse(response = RiderFoodCardResponse.class, code = 201, message = "One records created successfully"),
            @ApiResponse(code = 400, message = "Could not update records for supplied input") })
    @ApiImplicitParam(name = "id", dataType = "String", paramType = "path", dataTypeClass = String.class, value = "Rider Profile ID Details")
    @Valid
    public ResponseEntity<RiderFoodCardResponse> updateRiderFoodCardDetails(
            final @PathVariable(name = "id", required = true) String id,
            @RequestBody @Valid @NotEmpty final @NotNull RiderFoodCardRequest request) {
        RiderFoodCard foodCardDocument = foodCardService
                .getFoodCardDetailsByProfileId(id);

        RiderFoodCard updatedFoodCardDocument = foodCardService
                .updateFoodCardDetails(request, foodCardDocument);

        return ResponseEntity.status(HttpStatus.OK)
                .body(RiderFoodCardResponse.of(updatedFoodCardDocument));

    }
}
