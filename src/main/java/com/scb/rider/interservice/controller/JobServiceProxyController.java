package com.scb.rider.interservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.scb.rider.client.JobServiceFeignClient;
import com.scb.rider.model.document.RiderJobDetails;
import com.scb.rider.repository.RiderJobDetailsRepository;

import lombok.extern.log4j.Log4j2;


@RestController
@RequestMapping("/profile/job")
@Log4j2
public class JobServiceProxyController {

	@Autowired
	JobServiceFeignClient jobServiceFeignClient;
	
	
	@Autowired
	private RiderJobDetailsRepository riderJobDetailsRepository;
	
	@GetMapping(value = "/running/rider/{riderId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getRunningJobsForRider(@PathVariable(required = true, name = "riderId") String riderId) {
		log.info("RiderJobServiceProxyController - GetRunningJobsForRider - riderId:{}",riderId);
		
		RiderJobDetails jobDetails = riderJobDetailsRepository.findRunningJobIdForRider(riderId);
				
		return jobServiceFeignClient.getRunningJobsForRider(jobDetails.getJobId());
	}
	
	@GetMapping(value ="/{jobId}/job-details/rider/{riderId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public  ResponseEntity<?> getJobDetailsByJobId(@PathVariable(required = true, name = "jobId") String jobId,
			@PathVariable(required = true, name = "riderId") String riderId)
	{
		log.info("Fetching jobDetails for jobId:{} riderId:{}", jobId, riderId);
	  return jobServiceFeignClient.getJobByJobId(jobId);
	}
}
