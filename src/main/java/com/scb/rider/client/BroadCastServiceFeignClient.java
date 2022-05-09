package com.scb.rider.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.scb.rider.model.dto.BroadcastJobResponse;

@FeignClient(name = "broadcastServiceFeignClient", url = "${rider.client.broadcast-service}")
public interface BroadCastServiceFeignClient {

    @GetMapping(value="/broadcast/jobs/{jobId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public BroadcastJobResponse getBroadcastData(
        @PathVariable("jobId") String jobId);

}
