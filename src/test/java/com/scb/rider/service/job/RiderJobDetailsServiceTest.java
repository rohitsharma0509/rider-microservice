package com.scb.rider.service.job;

import com.scb.rider.client.JobServiceFeignClient;
import com.scb.rider.exception.JobNotFoundException;
import com.scb.rider.model.document.RiderJobDetails;
import com.scb.rider.model.dto.*;
import com.scb.rider.model.enumeration.RiderJobStatus;
import com.scb.rider.repository.RiderJobDetailsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RiderJobDetailsServiceTest {

    @InjectMocks
    private RiderJobDetailsService riderJobDetailsService;

    @Mock
    private RiderJobDetailsRepository riderJobDetailsRepository;

    @Mock
    private JobServiceFeignClient jobServiceFeignClient;

    @Test
    public void getRiderJobDetails(){
        String jobId = "12224";
        RiderJobDetails riderJobDetails = new RiderJobDetails();
        riderJobDetails.setId("job-1");
        riderJobDetails.setJobId("1234");
        riderJobDetails.setProfileId("123");

        when(riderJobDetailsRepository.findByJobId(jobId)).thenReturn(Optional.of(riderJobDetails));
        RiderJobDetailsDto jobDetails = riderJobDetailsService.getRiderJobDetails(jobId);
        assertNotNull(jobDetails.toString());
        assertNotNull(jobDetails);
    }

    @Test
    public void getRiderJobNotFoundExceptionTest() {
        String jobId = "12224";
        when(riderJobDetailsRepository.findByJobId(jobId)).thenReturn(Optional.empty());
        assertThrows(JobNotFoundException.class, () -> riderJobDetailsService.getRiderJobDetails(jobId));
    }

    @Test
    void test_FetchJobDetails() {
    	when(jobServiceFeignClient.getJobByJobId(Mockito.any())).thenReturn(ResponseEntity.ok(getCompleteJobDetails()));
    	JobDetails jobDetails = riderJobDetailsService.fetchJobDetails("123");
    	
    	assertNotNull(jobDetails);
    }
    
    @Test
    void getCompleteJobDetails_Success(){

        RiderJobDetails riderJobDetails = new RiderJobDetails();
        riderJobDetails.setId("512314");
        riderJobDetails.setJobId("1234");
        riderJobDetails.setProfileId("123");
        riderJobDetails.setJobStatus(RiderJobStatus.JOB_ACCEPTED);
        riderJobDetails.setRemarks("remark");
        JobDetails jobDetails = getCompleteJobDetails();

        RiderJobAcceptedDetails completeJobDetails = riderJobDetailsService.getCompleteJobDetails(riderJobDetails, jobDetails);
        
        assertNotNull(completeJobDetails);
    }

    @Test
    void test_fetchJobDetails_FailedCallToJobService(){
    	when(jobServiceFeignClient.getJobByJobId(Mockito.any())).thenThrow(RuntimeException.class);
    	assertThrows(RuntimeException.class, () -> riderJobDetailsService.fetchJobDetails("123"));
    }


    @Test
    void getCompleteJobDetails_NotCalledForOtherStatus(){
        RiderJobDetails riderJobDetails = new RiderJobDetails();
        riderJobDetails.setId("512314");
        riderJobDetails.setJobId("1234");
        riderJobDetails.setProfileId("123");
        riderJobDetails.setJobStatus(RiderJobStatus.CALLED_MERCHANT);
        riderJobDetails.setRemarks("remark");
        riderJobDetails.setParkingFee(new BigDecimal("120"));
        riderJobDetails.setMealPhotoUrl("meal_photo.jpg");
        riderJobDetails.setParkingPhotoUrl("parking_photo.jpg");
        
        JobDetails jobDetails = getCompleteJobDetails();

        RiderJobAcceptedDetails completeJobDetails = riderJobDetailsService.getCompleteJobDetails(riderJobDetails, jobDetails);

        assertNotNull(completeJobDetails);
    }

    private JobDetails getCompleteJobDetails() {
        List<JobLocation> locationList = Arrays.asList(
                JobLocation.builder().seq(1).contactName("Merchant name").contactPhone("9876543210").lat("123").lng("100").build(),
                JobLocation.builder().seq(2).contactName("Customer name").contactPhone("9876543210").lat("121").lng("101").build());
        JobDetails jobDetails =JobDetails.builder().orderId("1234")
                .totalDistance(123.0)
                .locationList(locationList)
                .orderItems(Arrays.asList(OrderItems.builder().name("Food").quantity(2).build()))
                .build();

    	return jobDetails;
    }
    
}
