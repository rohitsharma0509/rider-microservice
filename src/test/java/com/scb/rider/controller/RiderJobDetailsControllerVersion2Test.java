package com.scb.rider.controller;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Base64;

import com.scb.rider.model.dto.RiderJobAcceptedDetails;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import com.scb.rider.client.JobServiceFeignClient;
import com.scb.rider.constants.Constants;
import com.scb.rider.exception.FileConversionException;
import com.scb.rider.exception.InvalidImageExtensionException;
import com.scb.rider.model.document.RiderJobDetails;
import com.scb.rider.model.dto.ImageDto;
import com.scb.rider.model.dto.JobDetails;
import com.scb.rider.model.enumeration.CancellationSource;
import com.scb.rider.model.enumeration.RiderJobStatus;
import com.scb.rider.service.RiderSettlementDetailsService;
import com.scb.rider.service.job.RiderArrivedAtCustLocationService;
import com.scb.rider.service.job.RiderArrivedAtMerchantService;
import com.scb.rider.service.job.RiderCalledMerchantService;
import com.scb.rider.service.job.RiderFoodDeliveredService;
import com.scb.rider.service.job.RiderJobAcceptedService;
import com.scb.rider.service.job.RiderJobCancelByOperationService;
import com.scb.rider.service.job.RiderJobDetailsService;
import com.scb.rider.service.job.RiderJobFactory;
import com.scb.rider.service.job.RiderMealPickedUpService;
import com.scb.rider.service.job.RiderParkingReceiptPhotoService;
import com.scb.rider.util.CustomMultipartFile;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
public class RiderJobDetailsControllerVersion2Test {

    @InjectMocks
    private RiderJobDetailsControllerVersion2 riderJobDetailsController;
    @Mock
    private RiderJobFactory riderJobFactorySupplier;
    @Mock
    private RiderJobAcceptedService riderJobAcceptedService;
    @Mock
    private RiderCalledMerchantService riderCalledMerchantService;
    @Mock
    private RiderArrivedAtMerchantService riderArrivedAtMerchantService;
    @Mock
    private RiderMealPickedUpService riderMealPickedUpService;
    @Mock
    private RiderParkingReceiptPhotoService riderParkingReceiptPhotoService;
    @Mock
    private RiderFoodDeliveredService riderFoodDeliveredService;
    @Mock
    private RiderJobCancelByOperationService riderJobCancelByOperationService;
    @Mock
    private RiderArrivedAtCustLocationService riderArrivedAtCustLocationService;
    @Mock
    private RiderSettlementDetailsService riderSettlementDetailsService;
    @Mock
    private RiderJobDetailsService riderJobDetailsService;
    @Mock
    private JobServiceFeignClient jobServiceFeignClient;


    @Test
    public void riderJobCallMerchantTest() throws InvalidImageExtensionException, FileConversionException, IOException {
        RiderJobDetails riderJobDetails = new RiderJobDetails();
        riderJobDetails.setId("job-1");
        riderJobDetails.setJobId("1234");
        riderJobDetails.setProfileId("123");
        riderJobDetails.setJobStatus(RiderJobStatus.CALLED_MERCHANT);
        riderJobDetails.setRemarks("remark");
        riderJobDetails.setParkingFee(new BigDecimal("120"));
        riderJobDetails.setMealPhotoUrl("meal_photo.jpg");
        riderJobDetails.setParkingPhotoUrl("parking_photo.jpg");

        RiderJobAcceptedDetails riderJobCompleteDetails = RiderJobAcceptedDetails.builder()
                .orderId("1234").build();

        LocalDateTime dateTime = LocalDateTime.now();
        when(riderJobFactorySupplier.getRiderJob(RiderJobStatus.CALLED_MERCHANT)).thenReturn(riderCalledMerchantService);
        ImageDto imageDto = ImageDto.builder().imageValue("test").imageExt("jpg").imageName("test").build();
        
        when(riderCalledMerchantService.performActionRiderJobStatus(any(), anyString(), anyString(),
                any(), any(), any(), anyString(), any(), any(), any()))
        .thenReturn(riderJobDetails);
        when(riderJobDetailsService.getCompleteJobDetails(any(), any()))
                .thenReturn(riderJobCompleteDetails);
        JobDetails jobDetails = JobDetails.builder()
        		.totalDistance(100.0).build();
		when(riderJobDetailsService.fetchJobDetails(anyString()))
        .thenReturn(jobDetails);

        ResponseEntity<RiderJobAcceptedDetails> responseEntity = riderJobDetailsController.riderJobStatus(Constants.OPS_MEMBER, "123"
                ,  "1234", RiderJobStatus.CALLED_MERCHANT, "remark", new BigDecimal("120"),
                null, null, CancellationSource.RBH, dateTime,imageDto);

        assertTrue(ObjectUtils.isNotEmpty(responseEntity.getBody()));
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());

    }
    

}
