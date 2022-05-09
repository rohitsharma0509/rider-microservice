package com.scb.rider.controller;

import static com.scb.rider.constants.UrlMappings.RIDER_API;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.scb.rider.service.document.RiderProfileService;


@RestController
@RequestMapping("/" + RIDER_API)
public class RedisCacheController {

	@Autowired
	RiderProfileService riderProfileService;
	
	@GetMapping("/save-to-redis")
	public ResponseEntity<Boolean> saveToRedis()
	{
		riderProfileService.publishToKafka();
		return ResponseEntity.ok(Boolean.TRUE);
		
	}
	
	

}
