package com.scb.rider.client;

import com.scb.rider.model.dto.ExcessiveWaitingTimeDetailsEntity;
import com.scb.rider.model.dto.JobDetails;
import com.scb.rider.model.dto.JobSettlementDetails;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "jobServiceFeignClient", url = "${rider.client.job-service}")
public interface JobServiceFeignClient {

    @PostMapping(value = "/jobSearch/byJobIds", produces = MediaType.APPLICATION_JSON_VALUE)
    List<JobSettlementDetails> getJobDetails(@RequestBody List<String> jobIds);

    @GetMapping(value="/job/running/rider/job/{jobId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<?> getRunningJobsForRider(@PathVariable(name = "jobId") String jobId);

    @GetMapping(value="/job/{jobId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<JobDetails> getJobByJobId(@PathVariable(name = "jobId") String jobId);

    @PutMapping(value = "/job/{jobId}/ewt")
    ResponseEntity<Void> updateEwtAmount(@PathVariable("jobId") String jobId, @RequestBody ExcessiveWaitingTimeDetailsEntity ewt);

}
