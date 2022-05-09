package com.scb.rider.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "incentivesServiceFeignClient", url = "${rider.client.incentives-service}")
public interface IncentivesServiceFeignClient {

    @GetMapping(value="/api/incentive/progress/over-time", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object getIncentiveProgressDetails(
            @RequestParam(name = "riderId", required = true) String riderId,
            @RequestParam(name = "activeFlag", required = true) String activeFlag);

    @GetMapping(value="/api/incentive/progress/top-up", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object getIncentiveTopUpProgressDetails(
            @RequestParam(name = "riderId", required = true) String riderId);

    @GetMapping(value="/api/incentive/earn-history/top-up", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object getTopUPIncentiveEarnHistory(
            @RequestParam(name = "riderId", required = true) String riderId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size);

    @GetMapping(value="/api/incentive/earn-history/over-time", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object getOverTimeIncentiveEarnHistory(
            @RequestParam(name = "riderId", required = true) String riderId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size);

    @GetMapping(value="/api/incentive/earn-history/over-time/aggregate", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object getOverTimeAggregateIncentiveEarnHistory(
            @RequestParam(name = "riderId", required = true) String riderId,
            @RequestParam(defaultValue = "0") Long skip,
            @RequestParam(defaultValue = "20") int limit);

    @GetMapping(value="/api/incentive/earn-history/top-up/aggregate", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object getTopUPAggregateIncentiveEarnHistory(
            @RequestParam(name = "riderId", required = true) String riderId,
            @RequestParam(defaultValue = "0") Long skip,
            @RequestParam(defaultValue = "20") int limit);

}
