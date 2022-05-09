package com.scb.rider.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.scb.rider.model.document.RiderDeviceDetails;
import com.scb.rider.service.document.RiderDeviceService;
import com.scb.rider.view.View;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

@RestController
@Log4j2
@RequestMapping("/profile")
@Api(value = "Rider Profile Endpoints")
public class RiderDeviceController {

    @Autowired
    private RiderDeviceService riderDeviceService;

    @ApiOperation(nickname = "save-rider-device-details",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE,
            value = "Returns Rider Profile with updated Rider Profile ", response = RiderDeviceDetails.class
    )
    @JsonView(value = View.RiderDevice.RESPONSE.class)
    @PostMapping(value = "/{id}/device", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RiderDeviceDetails> saveRiderDeviceDetails(
            @ApiParam(value = "id", example = "5fc35ef7af8a144ac42a0a54", required = true)
            @PathVariable("id") String riderId,
            @JsonView(value = View.RiderDevice.REQUEST.class)
            @RequestBody @Valid RiderDeviceDetails riderDevice) {
        log.info("Saving Rider Device Info with rider id {}", riderDevice.toString());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.riderDeviceService.saveRiderDeviceInfo(riderId, riderDevice));
    }

    @ApiOperation(nickname = "fetch-rider-device-details", produces = MediaType.APPLICATION_JSON_VALUE,
            value = "Returns List of Rider Profile with updated Rider Profile ", response = RiderDeviceDetails.class
    )
    @JsonView(value = View.RiderDevice.RESPONSE.class)
    @GetMapping(value = "/{id}/device", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RiderDeviceDetails> fetchRiderDeviceDetails(
            @ApiParam(value = "id", example = "5fc35ef7af8a144ac42a0a54", required = true)
            @PathVariable("id") String riderId) {
        log.info("Fetching preferred zone with rider id {}", riderId);
          Optional<RiderDeviceDetails> riderDeviceDetails = this.riderDeviceService.findRiderDeviceDetails(riderId);

        if (!riderDeviceDetails.isPresent()) {
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-info", "Rider Driving License Details not found with given id");
            return ResponseEntity.notFound().headers(headers).build();
        } else {
            return ResponseEntity.ok(riderDeviceDetails.get());
        }
    }
}
