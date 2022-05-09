package com.scb.rider.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.scb.rider.model.dto.RiderExcessiveWaitDetailsDto;

@FeignClient(name = "pocketServiceFeignClient", url = "${rider.client.pocket-service}")
public interface PocketServiceFeignClient {

    public static final String POCKET_SERVICE_BASE_PATH = "/pocket/initialize";
	
	@PutMapping(value =POCKET_SERVICE_BASE_PATH + "/{riderId}")
	public ResponseEntity<Boolean> addRiderDataFromOpsPortal(@PathVariable("riderId") String riderId);
	
	
	@PutMapping("/pocket/add-excessive-wait-amount")
	public Object addRiderExcessWaitTopup(
			@RequestBody RiderExcessiveWaitDetailsDto riderTopupDetails) ;
}

