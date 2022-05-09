package com.scb.rider.interservice.controller;

import com.scb.rider.client.IncentivesServiceFeignClient;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Log4j2
@RequestMapping
public class RiderIncentiveProgressDetailsController {

    @Autowired
    private IncentivesServiceFeignClient incentivesServiceFeignClient;

    @GetMapping(value="/api/incentive/progress/over-time",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getIncentiveProgressDetails(
            @RequestParam(name = "riderId", required = true) String riderId,
            @RequestParam(name = "activeFlag", required = true) String activeFlag) {
        log.info("request received to get incentive progress for riderId {} and activeFlag {}", riderId, activeFlag);
        return ResponseEntity
                .ok(this.incentivesServiceFeignClient.getIncentiveProgressDetails(riderId, activeFlag));
    }

    @GetMapping(value = "/api/incentive/progress/top-up",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getIncentiveTopUpProgressDetails(
            @RequestParam(name = "riderId", required = true) String riderId) {
        return ResponseEntity
                .ok(this.incentivesServiceFeignClient.getIncentiveTopUpProgressDetails(riderId));
    }

    @GetMapping(value = "/api/incentive/earn-history/top-up",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getTopUpIncentiveEarnHistory(
            @RequestParam(name = "riderId", required = true) String riderId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity
                .ok(this.incentivesServiceFeignClient.getTopUPIncentiveEarnHistory(riderId,page,size));
    }

    @GetMapping(value = "/api/incentive/earn-history/over-time",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getOverTimeIncentiveEarnHistory(
            @RequestParam(name = "riderId", required = true) String riderId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity
                .ok(this.incentivesServiceFeignClient.getOverTimeIncentiveEarnHistory(riderId,page,size));
    }

    @GetMapping(value = "/api/incentive/earn-history/over-time/aggregate",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getOverTimeAggregateIncentiveEarnHistory(
            @RequestParam(name = "riderId", required = true) String riderId,
            @RequestParam(defaultValue = "0") Long skip,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity
                .ok(this.incentivesServiceFeignClient.getOverTimeAggregateIncentiveEarnHistory(riderId,skip,limit));
    }

    @GetMapping(value = "/api/incentive/earn-history/top-up/aggregate",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getTopUpAggregateIncentiveEarnHistory(
            @RequestParam(name = "riderId", required = true) String riderId,
            @RequestParam(defaultValue = "0") Long skip,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity
                .ok(this.incentivesServiceFeignClient.getTopUPAggregateIncentiveEarnHistory(riderId,skip,limit));
    }
}
