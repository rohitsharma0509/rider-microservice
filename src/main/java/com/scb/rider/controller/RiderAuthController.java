package com.scb.rider.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.scb.rider.model.dto.RiderProfileDto;
import com.scb.rider.model.redis.RiderAuthDto;
import com.scb.rider.service.redis.RiderTokenCacheService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;

@RestController
@Log4j2
@RequestMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@Api(value = "Rider Search Endpoints")
public class RiderAuthController {
  
  @Autowired
  private RiderTokenCacheService riderTokenCacheService;
  
  @ApiOperation(nickname = "login-rider",
      consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE,
      value = "Login Rider", response = RiderProfileDto.class)
  @PostMapping("login")
  public ResponseEntity<String> loginRider(@RequestBody RiderAuthDto riderAuthDto) {
    log.info("Login->" + riderAuthDto.toString());
    riderTokenCacheService.insertRiderEventIdToRedis(riderAuthDto);
    
    return ResponseEntity.ok("Logged In Succesfully");
  }
  
  @ApiOperation(nickname = "logout-rider",
      consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE,
      value = "Logout Rider", response = RiderProfileDto.class)
  @PostMapping("logout")
  public ResponseEntity<String> logoutRider(@RequestBody RiderAuthDto riderAuthDto) {
    log.info("Logout->" + riderAuthDto.toString());
    riderTokenCacheService.logoutRiderFromRedis(riderAuthDto);
    
    return ResponseEntity.ok("Logged Out Succesfully");
  }
}
