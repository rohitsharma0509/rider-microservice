package com.scb.rider.service.job;

import com.scb.rider.client.JobServiceFeignClient;
import com.scb.rider.exception.JobNotFoundException;
import com.scb.rider.model.document.RiderJobDetails;
import com.scb.rider.model.dto.JobDetails;
import com.scb.rider.model.dto.RiderJobAcceptedDetails;
import com.scb.rider.model.dto.RiderJobDetailsDto;
import com.scb.rider.model.enumeration.RiderJobStatus;
import com.scb.rider.repository.RiderJobDetailsRepository;
import java.util.Optional;

import com.scb.rider.util.CustomBeanUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class RiderJobDetailsService {

  @Autowired
  private RiderJobDetailsRepository riderJobDetailsRepository;

  @Autowired
  private JobServiceFeignClient jobServiceFeignClient;

  public RiderJobDetailsDto getRiderJobDetails(String jobId) {

    Optional<RiderJobDetails> riderJobDetails = riderJobDetailsRepository.findByJobId(jobId);

    if (riderJobDetails.isPresent())    {
      return RiderJobDetailsDto.of(riderJobDetails.get());
    }
    else {
      log.info("Job not found with jobId {}",jobId);
      throw new JobNotFoundException("Job not found with job id "+jobId);
    }

  }


  public RiderJobAcceptedDetails getCompleteJobDetails(RiderJobDetails riderJobDetails, JobDetails jobDetails) {
	RiderJobAcceptedDetails riderJobCompleteDetails = new RiderJobAcceptedDetails();
    if (RiderJobStatus.getStatusForCompleteJobResponse().contains(riderJobDetails.getJobStatus())) {
      riderJobCompleteDetails = RiderJobAcceptedDetails.of(jobDetails);
    }
    CustomBeanUtils.copyNonNullProperties(riderJobDetails, riderJobCompleteDetails);
    return riderJobCompleteDetails;
  }

  public JobDetails fetchJobDetails(String jobId) {
	  return jobServiceFeignClient.getJobByJobId(jobId).getBody();
  }
  
}
