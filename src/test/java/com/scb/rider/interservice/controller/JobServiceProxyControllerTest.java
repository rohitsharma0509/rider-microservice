package com.scb.rider.interservice.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.io.IOException;

import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.scb.rider.client.JobServiceFeignClient;
import com.scb.rider.exception.FileConversionException;
import com.scb.rider.exception.InvalidImageExtensionException;
import com.scb.rider.model.document.RiderJobDetails;
import com.scb.rider.repository.RiderJobDetailsRepository;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JobServiceProxyControllerTest {

	@InjectMocks
    private JobServiceProxyController jobServiceProxyController;
    @Mock
    private JobServiceFeignClient jobServiceFeignClient;
    
    @Mock
	private RiderJobDetailsRepository riderJobDetailsCustomRepository;


    @Test
    public void getRunningJobsForRider() throws InvalidImageExtensionException, FileConversionException, IOException {
        String riderId = "RR12345";
        ResponseEntity<?> entity = ResponseEntity.ok(riderId);
        RiderJobDetails r= new RiderJobDetails();
        r.setJobId("123");
        doReturn(entity).when(jobServiceFeignClient).getRunningJobsForRider(r.getJobId());
        doReturn(r).when(riderJobDetailsCustomRepository).findRunningJobIdForRider(r.getJobId());
        ResponseEntity<?> responseEntity = jobServiceProxyController
                .getRunningJobsForRider(r.getJobId());

        assertTrue(ObjectUtils.isNotEmpty(responseEntity.getBody()));
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }



    @Test
    public void getJobDetailsByJobId() throws InvalidImageExtensionException, FileConversionException, IOException {
        String jobId = "S12345";
        ResponseEntity<?> entity = ResponseEntity.ok(jobId);
        doReturn(entity).when(jobServiceFeignClient).getJobByJobId(jobId);
        ResponseEntity<?> responseEntity = jobServiceProxyController
                .getJobDetailsByJobId(jobId, "riderId");

        assertTrue(ObjectUtils.isNotEmpty(responseEntity.getBody()));
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }
    
   
}
