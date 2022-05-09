package com.scb.rider.controller;

import static com.scb.rider.constants.UrlMappings.RIDER_DASHBOARD_API;
import static com.scb.rider.constants.UrlMappings.RiderManagementDashBoard.SUMMARY;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.scb.rider.model.dto.RiderManagementDashBoardResponseDto;
import com.scb.rider.service.document.RiderManagementDashBoardService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;

@RestController
@Log4j2
@RequestMapping(RIDER_DASHBOARD_API)
@Api(value = "Rider Management DashBoard Endpoints")
public class RiderManagementDashBoardController {


  @Autowired
  private RiderManagementDashBoardService riderManagementDashBoardService;

  @ApiOperation(nickname = "get-rider-management-dashboard-summary",
      consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE,
      value = "Gets Rider Status Summary Count")
  @GetMapping(value = SUMMARY, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<RiderManagementDashBoardResponseDto> getRiderManagementDashBoardSummary() {
    String requestId = UUID.randomUUID().toString();
    log.info(String.format("Rider Management DashBoard Request Id - %s", requestId));
    return ResponseEntity.ok(riderManagementDashBoardService.getRiderManagementDashBoardSummary(requestId));
  }


}
