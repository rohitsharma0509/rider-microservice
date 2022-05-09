package com.scb.rider.controller;


import com.scb.rider.client.JobServiceFeignClient;
import com.scb.rider.constants.Constants;
import com.scb.rider.exception.FileConversionException;
import com.scb.rider.exception.InvalidImageExtensionException;
import com.scb.rider.model.document.RiderJobDetails;
import com.scb.rider.model.dto.RiderJobDetailsDto;
import com.scb.rider.model.dto.RiderSettlementDetails;
import com.scb.rider.model.enumeration.CancellationSource;
import com.scb.rider.model.enumeration.RiderJobStatus;
import com.scb.rider.service.RiderSettlementDetailsService;
import com.scb.rider.service.job.*;
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

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
public class RiderJobDetailsControllerTest {

    @InjectMocks
    private RiderJobDetailsController riderJobDetailsController;
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
    RiderJobDetailsService riderJobDetailsService;
    @Mock
    private JobServiceFeignClient jobServiceFeignClient;


    @Test
    public void riderJobCallMerchantTest() throws InvalidImageExtensionException, FileConversionException, IOException {
        MockMultipartFile mockMultipartFile
                = new MockMultipartFile(
                "file",
                "hello.png",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World!".getBytes()
        );
        RiderJobDetails riderJobDetails = new RiderJobDetails();
        riderJobDetails.setId("job-1");
        riderJobDetails.setJobId("1234");
        riderJobDetails.setProfileId("123");
        riderJobDetails.setJobStatus(RiderJobStatus.CALLED_MERCHANT);
        riderJobDetails.setRemarks("remark");
        riderJobDetails.setParkingFee(new BigDecimal("120"));
        riderJobDetails.setMealPhotoUrl("meal_photo.jpg");
        riderJobDetails.setParkingPhotoUrl("parking_photo.jpg");
        LocalDateTime dateTime = LocalDateTime.now();
        when(riderJobFactorySupplier.getRiderJob(RiderJobStatus.CALLED_MERCHANT)).thenReturn(riderCalledMerchantService);
        when(riderCalledMerchantService.performActionRiderJobStatus(mockMultipartFile, "123", "1234",
                new BigDecimal("120"), null, null, "remark", CancellationSource.RBH, dateTime, Constants.OPS_MEMBER)).thenReturn(riderJobDetails);

        ResponseEntity<RiderJobDetails> responseEntity = riderJobDetailsController.riderJobStatus(Constants.OPS_MEMBER, "123"
                , mockMultipartFile, "1234", RiderJobStatus.CALLED_MERCHANT, "remark", new BigDecimal("120"),
                null, null, CancellationSource.RBH, dateTime);

        assertTrue(ObjectUtils.isNotEmpty(responseEntity.getBody()));
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());

    }

    @Test
    public void riderJobArrivedAtMerchantServiceTest() throws InvalidImageExtensionException, FileConversionException, IOException {
        MockMultipartFile mockMultipartFile
                = new MockMultipartFile(
                "file",
                "hello.png",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World!".getBytes()
        );
        RiderJobDetails riderJobDetails = new RiderJobDetails();
        riderJobDetails.setId("job-1");
        riderJobDetails.setJobId("1234");
        riderJobDetails.setProfileId("123");
        riderJobDetails.setJobStatus(RiderJobStatus.ARRIVED_AT_MERCHANT);
        riderJobDetails.setRemarks("remark");
        riderJobDetails.setParkingFee(new BigDecimal("120"));
        riderJobDetails.setMealPhotoUrl("meal_photo.jpg");
        riderJobDetails.setParkingPhotoUrl("parking_photo.jpg");
        LocalDateTime dateTime = LocalDateTime.now();
        when(riderJobFactorySupplier.getRiderJob(RiderJobStatus.ARRIVED_AT_MERCHANT)).thenReturn(riderArrivedAtMerchantService);
        when(riderArrivedAtMerchantService.performActionRiderJobStatus(mockMultipartFile, "123", "1234",
                new BigDecimal("120"), null, null, "remark", CancellationSource.RBH, dateTime, Constants.OPS_MEMBER)).thenReturn(riderJobDetails);

        ResponseEntity<RiderJobDetails> responseEntity = riderJobDetailsController.riderJobStatus(Constants.OPS_MEMBER, "123"
                , mockMultipartFile, "1234", RiderJobStatus.ARRIVED_AT_MERCHANT, "remark",
                new BigDecimal("120"), null, null, CancellationSource.RBH, dateTime);

        assertTrue(ObjectUtils.isNotEmpty(responseEntity.getBody()));
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());

    }

    @Test
    public void riderJobMealPickedUpServiceTest() throws InvalidImageExtensionException, FileConversionException, IOException {
        MockMultipartFile mockMultipartFile
                = new MockMultipartFile(
                "file",
                "hello.png",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World!".getBytes()
        );
        RiderJobDetails riderJobDetails = new RiderJobDetails();
        riderJobDetails.setId("job-1");
        riderJobDetails.setJobId("1234");
        riderJobDetails.setProfileId("123");
        riderJobDetails.setJobStatus(RiderJobStatus.MEAL_PICKED_UP);
        riderJobDetails.setRemarks("remark");
        riderJobDetails.setParkingFee(new BigDecimal("120"));
        riderJobDetails.setMealPhotoUrl("meal_photo.jpg");
        riderJobDetails.setParkingPhotoUrl("parking_photo.jpg");
        LocalDateTime dateTime = LocalDateTime.now();

        when(riderJobFactorySupplier.getRiderJob(RiderJobStatus.MEAL_PICKED_UP)).thenReturn(riderMealPickedUpService);
        when(riderMealPickedUpService.performActionRiderJobStatus(mockMultipartFile, "123", "1234",
                new BigDecimal("120"), null, null, "remark", CancellationSource.RBH, dateTime, Constants.OPS_MEMBER)).thenReturn(riderJobDetails);

        ResponseEntity<RiderJobDetails> responseEntity = riderJobDetailsController.riderJobStatus(Constants.OPS_MEMBER, "123"
                , mockMultipartFile, "1234", RiderJobStatus.MEAL_PICKED_UP, "remark",
                new BigDecimal("120"), null, null, CancellationSource.RBH, dateTime);

        assertTrue(ObjectUtils.isNotEmpty(responseEntity.getBody()));
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());

    }

    @Test
    public void riderJobParkingReceiptPhotoServiceTest() throws InvalidImageExtensionException, FileConversionException, IOException {
        MockMultipartFile mockMultipartFile
                = new MockMultipartFile(
                "file",
                "hello.png",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World!".getBytes()
        );
        RiderJobDetails riderJobDetails = new RiderJobDetails();
        riderJobDetails.setId("job-1");
        riderJobDetails.setJobId("1234");
        riderJobDetails.setProfileId("123");
        riderJobDetails.setJobStatus(RiderJobStatus.PARKING_RECEIPT_PHOTO);
        riderJobDetails.setRemarks("remark");
        riderJobDetails.setParkingFee(new BigDecimal("120"));
        riderJobDetails.setMealPhotoUrl("meal_photo.jpg");
        riderJobDetails.setParkingPhotoUrl("parking_photo.jpg");
        LocalDateTime dateTime = LocalDateTime.now();

        when(riderJobFactorySupplier.getRiderJob(RiderJobStatus.PARKING_RECEIPT_PHOTO)).thenReturn(riderParkingReceiptPhotoService);
        when(riderParkingReceiptPhotoService.performActionRiderJobStatus(mockMultipartFile, "123", "1234",
                new BigDecimal("120"), null, null, "remark", CancellationSource.RBH, dateTime, Constants.OPS_MEMBER)).thenReturn(riderJobDetails);

        ResponseEntity<RiderJobDetails> responseEntity = riderJobDetailsController.riderJobStatus(Constants.OPS_MEMBER, "123"
                , mockMultipartFile, "1234", RiderJobStatus.PARKING_RECEIPT_PHOTO, "remark",
                new BigDecimal("120"), null, null, CancellationSource.RBH, dateTime);

        assertTrue(ObjectUtils.isNotEmpty(responseEntity.getBody()));
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());

    }

    @Test
    public void riderJobFoodDeliveredServiceTest() throws InvalidImageExtensionException, FileConversionException, IOException {
        MockMultipartFile mockMultipartFile
                = new MockMultipartFile(
                "file",
                "hello.png",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World!".getBytes()
        );
        RiderJobDetails riderJobDetails = new RiderJobDetails();
        riderJobDetails.setId("job-1");
        riderJobDetails.setJobId("1234");
        riderJobDetails.setProfileId("123");
        riderJobDetails.setJobStatus(RiderJobStatus.FOOD_DELIVERED);
        riderJobDetails.setRemarks("remark");
        riderJobDetails.setParkingFee(new BigDecimal("120"));
        riderJobDetails.setMealPhotoUrl("meal_photo.jpg");
        riderJobDetails.setParkingPhotoUrl("parking_photo.jpg");
        LocalDateTime dateTime = LocalDateTime.now();
        when(riderJobFactorySupplier.getRiderJob(RiderJobStatus.FOOD_DELIVERED)).thenReturn(riderFoodDeliveredService);
        when(riderFoodDeliveredService.performActionRiderJobStatus(mockMultipartFile, "123", "1234",
                new BigDecimal("120"), null, null, "remark", CancellationSource.RBH, dateTime, Constants.OPS_MEMBER)).thenReturn(riderJobDetails);

        ResponseEntity<RiderJobDetails> responseEntity = riderJobDetailsController.riderJobStatus(Constants.OPS_MEMBER, "123"
                , mockMultipartFile, "1234", RiderJobStatus.FOOD_DELIVERED, "remark",
                new BigDecimal("120"), null, null,  CancellationSource.RBH, dateTime);

        assertTrue(ObjectUtils.isNotEmpty(responseEntity.getBody()));
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());

    }

    @Test
    public void riderJobCancelByOperationServiceTest() throws InvalidImageExtensionException, FileConversionException, IOException {
        MockMultipartFile mockMultipartFile
                = new MockMultipartFile(
                "file",
                "hello.png",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World!".getBytes()
        );
        RiderJobDetails riderJobDetails = new RiderJobDetails();
        riderJobDetails.setId("job-1");
        riderJobDetails.setJobId("1234");
        riderJobDetails.setProfileId("123");
        riderJobDetails.setJobStatus(RiderJobStatus.ORDER_CANCELLED_BY_OPERATOR);
        riderJobDetails.setRemarks("remark");
        riderJobDetails.setParkingFee(new BigDecimal("120"));
        riderJobDetails.setMealPhotoUrl("meal_photo.jpg");
        riderJobDetails.setParkingPhotoUrl("parking_photo.jpg");
        LocalDateTime dateTime = LocalDateTime.now();
        when(riderJobFactorySupplier.getRiderJob(RiderJobStatus.ORDER_CANCELLED_BY_OPERATOR)).thenReturn(riderJobCancelByOperationService);
        when(riderJobCancelByOperationService.performActionRiderJobStatus(mockMultipartFile, "123", "1234",
                new BigDecimal("120"), new BigDecimal("0"), Boolean.valueOf(true), "remark", CancellationSource.OPS, dateTime, Constants.OPS_MEMBER)).thenReturn(riderJobDetails);

        ResponseEntity<RiderJobDetails> responseEntity = riderJobDetailsController.riderJobStatus(Constants.OPS_MEMBER, "123"
                , mockMultipartFile, "1234", RiderJobStatus.ORDER_CANCELLED_BY_OPERATOR, "remark",
                new BigDecimal("120"), new BigDecimal("0"), Boolean.valueOf(true), CancellationSource.OPS, dateTime);

        assertTrue(ObjectUtils.isNotEmpty(responseEntity.getBody()));
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());

    }

    @Test
    public void riderJobCancelByOperationServiceJobNotAcceptedYetTest() throws InvalidImageExtensionException, FileConversionException, IOException {
        MockMultipartFile mockMultipartFile
            = new MockMultipartFile(
            "file",
            "hello.png",
            MediaType.TEXT_PLAIN_VALUE,
            "Hello, World!".getBytes()
        );
        RiderJobDetails riderJobDetails = new RiderJobDetails();
        riderJobDetails.setId("job-1");
        riderJobDetails.setJobId("1234");
        riderJobDetails.setJobStatus(RiderJobStatus.ORDER_CANCELLED_BY_OPERATOR);
        riderJobDetails.setRemarks("remark");
        when(riderJobFactorySupplier.getRiderJob(RiderJobStatus.ORDER_CANCELLED_BY_OPERATOR)).thenReturn(riderJobCancelByOperationService);
        when(riderJobCancelByOperationService.performActionRiderJobStatus(null, null, "1234",
            null, new BigDecimal("0"), Boolean.valueOf(true), "remark", CancellationSource.RBH, null, Constants.OPS_MEMBER)).thenReturn(riderJobDetails);

        ResponseEntity<RiderJobDetails> responseEntity = riderJobDetailsController.riderJobStatus(Constants.OPS_MEMBER, null
            , null, "1234", RiderJobStatus.ORDER_CANCELLED_BY_OPERATOR, "remark",null, new BigDecimal("0"), Boolean.valueOf(true), CancellationSource.RBH, null);

        assertTrue(ObjectUtils.isNotEmpty(responseEntity.getBody()));
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertEquals(responseEntity.getBody().getJobStatus(), RiderJobStatus.ORDER_CANCELLED_BY_OPERATOR);

    }

    @Test
    public void riderJobAcceptedTest() throws InvalidImageExtensionException, FileConversionException, IOException {
        MockMultipartFile mockMultipartFile
                = new MockMultipartFile(
                "file",
                "hello.png",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World!".getBytes()
        );
        RiderJobDetails riderJobDetails = new RiderJobDetails();
        riderJobDetails.setId("job-1");
        riderJobDetails.setJobId("1234");
        riderJobDetails.setProfileId("123");
        riderJobDetails.setJobStatus(RiderJobStatus.JOB_ACCEPTED);
        riderJobDetails.setRemarks("remark");
        riderJobDetails.setParkingFee(new BigDecimal("120"));
        riderJobDetails.setMealPhotoUrl("meal_photo.jpg");
        riderJobDetails.setParkingPhotoUrl("parking_photo.jpg");
        LocalDateTime dateTime = LocalDateTime.now();
        when(riderJobFactorySupplier.getRiderJob(RiderJobStatus.JOB_ACCEPTED)).thenReturn(riderJobAcceptedService);
        when(riderJobAcceptedService.performActionRiderJobStatus(mockMultipartFile, "123", "1234",
                new BigDecimal("120"), null, null, "remark", CancellationSource.RBH, dateTime, Constants.OPS_MEMBER)).thenReturn(riderJobDetails);

        ResponseEntity<RiderJobDetails> responseEntity = riderJobDetailsController.riderJobStatus(Constants.OPS_MEMBER, "123"
                , mockMultipartFile, "1234", RiderJobStatus.JOB_ACCEPTED, "remark",
                new BigDecimal("120"), null, null, CancellationSource.RBH, dateTime);

        assertTrue(ObjectUtils.isNotEmpty(responseEntity.getBody()));
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());

    }


    @Test
    public void riderJobArrivedAtCustLocationServiceTest() throws InvalidImageExtensionException, FileConversionException, IOException {
        MockMultipartFile mockMultipartFile
                = new MockMultipartFile(
                "file",
                "hello.png",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World!".getBytes()
        );
        RiderJobDetails riderJobDetails = new RiderJobDetails();
        riderJobDetails.setId("job-1");
        riderJobDetails.setJobId("1234");
        riderJobDetails.setProfileId("123");
        riderJobDetails.setJobStatus(RiderJobStatus.ARRIVED_AT_CUST_LOCATION);
        riderJobDetails.setRemarks("remark");
        riderJobDetails.setParkingFee(new BigDecimal("120"));
        riderJobDetails.setMealPhotoUrl("meal_photo.jpg");
        riderJobDetails.setParkingPhotoUrl("parking_photo.jpg");
        LocalDateTime dateTime = LocalDateTime.now();

        when(riderJobFactorySupplier.getRiderJob(RiderJobStatus.ARRIVED_AT_CUST_LOCATION)).thenReturn(riderArrivedAtCustLocationService);
        when(riderArrivedAtCustLocationService.performActionRiderJobStatus(mockMultipartFile, "123", "1234",
                new BigDecimal("120"), null, null, "remark", CancellationSource.RBH, dateTime, Constants.OPS_MEMBER)).thenReturn(riderJobDetails);

        ResponseEntity<RiderJobDetails> responseEntity = riderJobDetailsController.riderJobStatus(Constants.OPS_MEMBER, "123"
                , mockMultipartFile, "1234", RiderJobStatus.ARRIVED_AT_CUST_LOCATION, "remark",
                new BigDecimal("120"), null, null, CancellationSource.RBH, dateTime);

        assertTrue(ObjectUtils.isNotEmpty(responseEntity.getBody()));
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());

    }

    @Test
    public void fetchJobDetailsTest() {
        RiderJobDetailsDto riderJobDetails = new RiderJobDetailsDto();
        String jobId = "job-1";
        riderJobDetails.setId(jobId);
        riderJobDetails.setJobId("1234");
        riderJobDetails.setProfileId("123");
        riderJobDetails.setJobStatus(RiderJobStatus.ARRIVED_AT_CUST_LOCATION);
        riderJobDetails.setRemarks("remark");
        riderJobDetails.setParkingFee(new BigDecimal("120"));
        riderJobDetails.setMealPhotoUrl("meal_photo.jpg");
        riderJobDetails.setParkingPhotoUrl("parking_photo.jpg");
        LocalDateTime dateTime = LocalDateTime.now();

        when(riderJobDetailsService.getRiderJobDetails(jobId)).thenReturn(riderJobDetails);

        ResponseEntity<RiderJobDetailsDto> responseEntity = riderJobDetailsController.fetchJobDetails(jobId);

        assertTrue(ObjectUtils.isNotEmpty(responseEntity.getBody()));
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

    }

    @Test
    public void fetchRiderJobDetailsStatusTest() throws InvalidImageExtensionException, FileConversionException, IOException {
        RiderSettlementDetails riderSettlementDetails = new RiderSettlementDetails();
        riderSettlementDetails.setJobId("1234");
        riderSettlementDetails.setAccountNumber("123898989");
        riderSettlementDetails.setJobStatus("COMPLETED");
        riderSettlementDetails.setNetPrice(new BigDecimal("123.00"));
        riderSettlementDetails.setJobPrice(new BigDecimal("120"));
        riderSettlementDetails.setOrderId("1920912");
        riderSettlementDetails.setRiderName("Kapil Dev");
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = LocalDateTime.now();

        List<RiderSettlementDetails> list = new ArrayList<>();
        list.add(riderSettlementDetails);

        when(riderSettlementDetailsService.getRiderSettlementDetails(startTime, endTime)).thenReturn(list);

        ResponseEntity<List<RiderSettlementDetails>> responseEntity = riderJobDetailsController
                .fetchRiderJobDetailsStatus(startTime,endTime);

        assertTrue(ObjectUtils.isNotEmpty(responseEntity.getBody()));
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(riderSettlementDetails.toString());
    }

}
