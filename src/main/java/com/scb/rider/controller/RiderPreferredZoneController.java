package com.scb.rider.controller;

import com.scb.rider.constants.Constants;
import com.scb.rider.constants.UrlMappings.RiderZone;
import com.scb.rider.model.dto.RiderPreferredZoneDto;
import com.scb.rider.service.document.RiderPreferredZoneService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;
import java.nio.file.AccessDeniedException;

@RestController
@Log4j2
@RequestMapping("/profile")
@Api(value = "Rider Zone Endpoints")
public class RiderPreferredZoneController {
    @Autowired
    private RiderPreferredZoneService riderPreferredZoneService;

    
    @ApiOperation(nickname = "save-zone-for-rider",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE,
            value = "Returns preferred saved zone for rider ", response = RiderPreferredZoneDto.class
    )
    @PostMapping(value = RiderZone.ZONE, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RiderPreferredZoneDto> savePreferredZone(@RequestBody  @Valid RiderPreferredZoneDto zone) throws AccessDeniedException {
      log.info("Saving preferred zone with rider id {}",zone.getRiderProfileId());
    	return ResponseEntity.status(HttpStatus.OK)
                .body(RiderPreferredZoneDto.of(this.riderPreferredZoneService.savePreferredZone(zone)));
    }

    @ApiOperation(nickname = "save-zone-for-rider-ops-member",
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE,
        value = "Returns preferred saved zone for rider ", response = RiderPreferredZoneDto.class
    )
    @PostMapping(value = RiderZone.ZONE_OPS, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RiderPreferredZoneDto> savePreferredZoneOpsMember(
            @RequestAttribute(name = Constants.X_USER_ID, required = false) String userId,
            @RequestBody  @Valid RiderPreferredZoneDto zone) {
        log.info("Saving preferred zone with rider id {} by Ops member",zone.getRiderProfileId());
        zone.setUpdatedBy(userId);
        return ResponseEntity.status(HttpStatus.OK)
            .body(RiderPreferredZoneDto.of(this.riderPreferredZoneService.savePreferredZoneOpsMember(zone)));
    }

}