package com.scb.rider.controller;

import java.util.Optional;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.annotation.JsonView;
import com.scb.rider.model.document.RiderDeviceDetails;
import com.scb.rider.model.document.RiderLoginDeviceDetails;
import com.scb.rider.service.document.RiderLoginDeviceService;
import com.scb.rider.view.View;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.log4j.Log4j2;

@RestController
@Log4j2
@RequestMapping("/profile")
@Api(value = "Rider Profile Endpoints")
public class RiderLoginDeviceController {

  @Autowired
  private RiderLoginDeviceService riderLoginDeviceService;

  @ApiOperation(nickname = "save-rider-login-device-details",
      consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE,
      value = "Returns Rider Login Device with updated Details",
      response = RiderDeviceDetails.class)
  @JsonView(value = View.RiderDevice.RESPONSE.class)
  @PostMapping(value = "{phoneNumber}/login-device", produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<RiderLoginDeviceDetails> saveRiderLoginDeviceDetails(
      @ApiParam(value = "phoneNumber", example = "1111011110",
          required = true) @PathVariable("phoneNumber") String phoneNumber,
      @JsonView(
          value = View.RiderDevice.REQUEST.class) @RequestBody @Valid RiderLoginDeviceDetails riderLoginDeviceDetails) {
    log.info("Saving Rider Login Device Info with rider id {}", riderLoginDeviceDetails);
    return ResponseEntity.status(HttpStatus.CREATED).body(
        riderLoginDeviceService.saveRiderLoginDeviceInfo(phoneNumber,
            riderLoginDeviceDetails));
  }

  @ApiOperation(nickname = "fetch-rider-login-device-details",
      produces = MediaType.APPLICATION_JSON_VALUE,
      value = "Returns Rider Login Device with Details", response = RiderDeviceDetails.class)
  @JsonView(value = View.RiderDevice.RESPONSE.class)
  @GetMapping(value = "{phoneNumber}/login-device", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<RiderLoginDeviceDetails> fetchRiderDeviceDetails(
      @ApiParam(value = "phoneNumber", example = "1111011110",
          required = true) @PathVariable("phoneNumber") String phoneNumber) {
    log.info("Fetching login device details with phoneNumber {}", phoneNumber);
    Optional<RiderLoginDeviceDetails> riderLoginDeviceDetails =
        riderLoginDeviceService.findRiderLoginDeviceDetails(phoneNumber);

    if (!riderLoginDeviceDetails.isPresent()) {
      HttpHeaders headers = new HttpHeaders();
      headers.add("X-info", "Rider Login Device Details not found with given phoneNumber");
      return ResponseEntity.notFound().headers(headers).build();
    } else {
      return ResponseEntity.ok(riderLoginDeviceDetails.get());
    }
  }
}
