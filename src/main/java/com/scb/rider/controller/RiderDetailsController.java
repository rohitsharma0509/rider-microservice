package com.scb.rider.controller;

import com.scb.rider.model.dto.RiderDetailsDto;
import com.scb.rider.model.dto.RiderProfileDto;
import com.scb.rider.service.RiderDetailsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Log4j2
@RequestMapping("/profile/details")
@Api(value = "Rider Profile Endpoints")
public class RiderDetailsController {
    @Autowired
    private RiderDetailsService riderDetailsService;

    @ApiOperation(nickname = "get-rider-details-by-id",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE,
            value = "Gets Rider details by ID", response = RiderDetailsDto.class
    )
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RiderDetailsDto> getRiderDetailsById(
            @ApiParam(value = "id", example = "5fc35ef7af8a144ac42a0a54", required = true)
            @PathVariable("id") String id,
            @RequestParam(required = false) String[] filters) {
        log.info("Getting Rider details by id = {}", id);
        return ResponseEntity.ok(this.riderDetailsService.getRiderDetailsById(id, filters));
    }

    @ApiOperation(nickname = "get-rider-details-by-id",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE,
            value = "Gets Rider details by Phone Number", response = RiderDetailsDto.class
    )
    @GetMapping(value = "/mob/{phoneNumber}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RiderDetailsDto> getRiderDetailsByPhoneNumber(
            @ApiParam(value = "phoneNumber", example = "999999999", required = true)
            @PathVariable("phoneNumber") String phoneNumber) {
        log.info("Getting Rider details by id = {}", phoneNumber);
        return ResponseEntity.ok(this.riderDetailsService.getRiderDetailsByPhoneNumber(phoneNumber));
    }

    @ApiOperation(nickname = "get-rider-profile-docs-details",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE,
            value = "Gets Rider Profile documents details by rider id", response = RiderDetailsDto.class
    )
    @GetMapping(value = "/riderDocsDetails/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RiderDetailsDto> getRiderDocsDetails(
            @ApiParam(value = "id", example = "5fc35ef7af8a144ac42a0a54", required = true)
            @PathVariable("id") String id) {

        return ResponseEntity.ok(this.riderDetailsService.getRiderDocumentDetails(id));
    }

    @DeleteMapping(value = "/{mobileNum}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteRiderProfile(@PathVariable("mobileNum") String mobileNum) throws Exception {
        return ResponseEntity.status(HttpStatus.OK)
                .body(RiderProfileDto.of(this.riderDetailsService.deleteRiderProfileByMobileNumber(mobileNum)));
    }

}