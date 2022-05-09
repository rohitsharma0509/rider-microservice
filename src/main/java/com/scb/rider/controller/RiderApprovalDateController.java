package com.scb.rider.controller;

import com.scb.rider.service.RiderApprovalDateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/update-approval-date")
public class RiderApprovalDateController {

    @Autowired
    private RiderApprovalDateService riderApprovalDateService;

    @GetMapping
    public ResponseEntity<Long> updateApprovalDate(){
        return ResponseEntity.ok(riderApprovalDateService.update());
    }
}
