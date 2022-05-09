package com.scb.rider.controller;

import static com.scb.rider.constants.UrlMappings.RIDER_API;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.scb.rider.exception.ApiError;
import com.scb.rider.service.RiderLocationService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.log4j.Log4j2;

@RestController
@RequestMapping(RIDER_API)
@Log4j2
public class RiderLocationDetailsController {

	@Autowired
	RiderLocationService riderLocationService;
	
	@ApiOperation(nickname = "Get distance difference details", value = "get-distance-difference")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Data retrieved successfully"),
            @ApiResponse(response = ApiError.class, code = 400, message = "distance difference can not be fetched") })
    @GetMapping(value = "/{riderId}/location/get-distance",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> isRiderInMerchantRange(@PathVariable(name = "riderId") String riderId ,@RequestParam("longitudeFrom") Double longitudeFrom, @RequestParam("latitudeFrom") Double latitudeFrom,
                           @RequestParam("longitudeTo") Double longitudeTo, @RequestParam("latitudeTo") Double latitudeTo){
        log.info("inside isRiderInMerchantRange for riderId-{}",riderId);
        return ResponseEntity.ok(riderLocationService.checkDistance(longitudeFrom, latitudeFrom, longitudeTo, latitudeTo));
    }
}
