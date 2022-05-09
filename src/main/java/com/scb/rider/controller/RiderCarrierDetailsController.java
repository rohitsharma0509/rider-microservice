package com.scb.rider.controller;

import com.scb.rider.model.dto.RiderCarrierDetailsRequestDto;
import com.scb.rider.model.document.RiderCarrierDetails;
import com.scb.rider.service.RiderCarrierDetailsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.scb.rider.constants.UrlMappings.PATH_VARIABLE_RIDER_ID;

@RestController
@Log4j2
@RequestMapping("/carrier")
@Api(value = "Rider network carrier Endpoints")
public class RiderCarrierDetailsController {

    @Autowired
    private RiderCarrierDetailsService riderCarrierDetailsService;

    @ApiOperation(nickname = "save-carrier-details-for-rider",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE,
            value = "Returns carrier details for rider ", response = RiderCarrierDetails.class
    )
    @PostMapping(value = PATH_VARIABLE_RIDER_ID, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RiderCarrierDetails> saveCarrierDetails(@PathVariable("riderId") String riderId, @RequestBody @Valid RiderCarrierDetailsRequestDto updateRequestDto){
        log.info("Saving carrier details with rider id {}",riderId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.riderCarrierDetailsService.saveCarrierDetails(riderId, updateRequestDto));
    }

    @ApiOperation(nickname = "get-carrier-details-for-rider",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE,
            value = "Returns carrier details for rider ", response = RiderCarrierDetails.class
    )
    @GetMapping(value = PATH_VARIABLE_RIDER_ID, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RiderCarrierDetails> getCarrierDetails(@PathVariable("riderId") String riderId){
        log.info("Getting carrier details for rider id {}",riderId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(this.riderCarrierDetailsService.getCarrierDetails(riderId));
    }
}
