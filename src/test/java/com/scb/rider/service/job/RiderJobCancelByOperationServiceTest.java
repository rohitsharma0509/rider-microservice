package com.scb.rider.service.job;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import com.scb.rider.service.ExcessiveWaitPaymentService;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.rider.client.JobServiceFeignClient;
import com.scb.rider.constants.Constants;
import com.scb.rider.exception.JobAlreadyCancelledException;
import com.scb.rider.exception.RiderJobStatusValidationException;
import com.scb.rider.kafka.KafkaPublisher;
import com.scb.rider.kafka.NotificationPublisher;
import com.scb.rider.model.RiderJobStatusEventModel;
import com.scb.rider.model.document.RiderDeviceDetails;
import com.scb.rider.model.document.RiderJobDetails;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.dto.JobDetails;
import com.scb.rider.model.enumeration.CancellationSource;
import com.scb.rider.model.enumeration.Platform;
import com.scb.rider.model.enumeration.RiderJobStatus;
import com.scb.rider.model.kafka.AndroidPayload;
import com.scb.rider.model.kafka.BroadcastNotification;
import com.scb.rider.repository.RiderDeviceDetailRepository;
import com.scb.rider.repository.RiderJobDetailsRepository;
import com.scb.rider.repository.RiderProfileRepository;
import com.scb.rider.service.document.RiderActiveTrackingZoneService;
import com.scb.rider.util.PropertyUtils;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
@ActiveProfiles(value = "test")
public class RiderJobCancelByOperationServiceTest {

    @Mock
    private RiderJobDetailsRepository riderJobDetailsRepository;

    @Mock
    private RiderProfileRepository riderProfileRepository;
    
    @Mock
    private RiderActiveTrackingZoneService riderInactiveTrackingService;
    
    @Mock
    private RiderDeviceDetailRepository riderDeviceDetailRepository;

    @Mock
    private KafkaPublisher kafkaPublisher;
    @Mock
    private NotificationPublisher notificationPublisher;

    @Mock
    private PropertyUtils propertyUtils;

    @Mock
    private ExcessiveWaitPaymentService excessiveWaitPaymentService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private RiderJobCancelByOperationService service;

    @Mock
    private JobServiceFeignClient jobServiceFeignClient;

    
    private MockMultipartFile mockMultipartFile;
    private RiderJobDetails riderJobDetails;
    private RiderProfile riderProfile;
    private RiderDeviceDetails riderDeviceDetails;
    @Before
    public void setUp() {
        mockMultipartFile
                = new MockMultipartFile(
                "file",
                "hello.png",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World!".getBytes()
        );
        riderJobDetails = new RiderJobDetails();
        riderJobDetails.setId("job-1");
        riderJobDetails.setJobId("1234");
        riderJobDetails.setProfileId("123");
        riderJobDetails.setJobStatus(RiderJobStatus.ORDER_CANCELLED_BY_OPERATOR);
        riderJobDetails.setRemarks("remark");
        riderJobDetails.setParkingFee(new BigDecimal("120"));
        riderJobDetails.setMealPhotoUrl("meal_photo.jpg");
        riderJobDetails.setParkingPhotoUrl("parking_photo.jpg");

        riderProfile = new RiderProfile();
        riderProfile.setId("12133");
        riderDeviceDetails = RiderDeviceDetails.builder()
                .build();
        riderDeviceDetails.setPlatform(Platform.GCM);
        riderDeviceDetails.setArn("Device ARN");
        riderDeviceDetails.setDeviceToken("Device Token");
        riderDeviceDetails.setProfileId("123");
        riderDeviceDetails.setId("1234567");
    }

    @Test
    public void riderJobCancelByOperationServiceGCMTest() throws JsonProcessingException {
        try {
			when(riderProfileRepository.findById("123")).thenReturn(Optional.of(riderProfile));
			when(riderProfileRepository.save(any())).thenReturn(riderProfile);
			riderDeviceDetails.setPlatform(Platform.APNS);

			when(riderJobDetailsRepository.findByJobId("1234")).thenReturn(Optional.of(riderJobDetails));
			when(riderJobDetailsRepository.save(any())).thenReturn(riderJobDetails);
			when(riderDeviceDetailRepository.findByProfileId(anyString())).thenReturn(Optional.of(riderDeviceDetails));
			when(objectMapper.writeValueAsString(any(AndroidPayload.class))).thenReturn("payload-json");
			doNothing().when(notificationPublisher).send(any(BroadcastNotification.class));
			
			RiderJobDetails riderJobResponse = service.performActionRiderJobStatus(mockMultipartFile, "123", "1234",
			        new BigDecimal("120"), new BigDecimal("0"), Boolean.valueOf(true), "remark", CancellationSource.OPS, LocalDateTime.now(), Constants.OPS_MEMBER);
			assertTrue(ObjectUtils.isNotEmpty(riderJobResponse));
		} catch (Exception e) {
		}
    }
    @Test
    public void riderJobCancelByOperationServiceAPNSTest()  {
        try {
			when(riderProfileRepository.findById("123")).thenReturn(Optional.of(riderProfile));
			when(riderProfileRepository.save(any())).thenReturn(riderProfile);
			when(riderJobDetailsRepository.findByJobId("1234")).thenReturn(Optional.of(riderJobDetails));
			when(riderJobDetailsRepository.save(any())).thenReturn(riderJobDetails);
			when(riderDeviceDetailRepository.findByProfileId(anyString())).thenReturn(Optional.of(riderDeviceDetails));
			when(objectMapper.writeValueAsString(any(AndroidPayload.class))).thenReturn("payload-json");
			doNothing().when(notificationPublisher).send(any(BroadcastNotification.class));
			RiderJobDetails riderJobResponse = service.performActionRiderJobStatus(mockMultipartFile, "123", "1234",
			        new BigDecimal("120"), new BigDecimal("0"), Boolean.valueOf(true), "remark", CancellationSource.OPS, LocalDateTime.now(), Constants.OPS_MEMBER);
			assertTrue(ObjectUtils.isNotEmpty(riderJobResponse));
            verifyZeroInteractions(excessiveWaitPaymentService);
		} catch (Exception e) {
			
		}
    }

    @Test
    public void riderJobCancelByOperationServiceWhenEligibleForEwt() throws JsonProcessingException {
        when(riderProfileRepository.findById("123")).thenReturn(Optional.of(riderProfile));
        when(riderProfileRepository.save(any())).thenReturn(riderProfile);
        riderJobDetails.setJobStatus(RiderJobStatus.ARRIVED_AT_MERCHANT);
        when(riderJobDetailsRepository.findByJobId("1234")).thenReturn(Optional.of(riderJobDetails));
        when(riderJobDetailsRepository.save(any())).thenReturn(riderJobDetails);
        when(riderDeviceDetailRepository.findByProfileId(anyString())).thenReturn(Optional.of(riderDeviceDetails));
        when(objectMapper.writeValueAsString(any(AndroidPayload.class))).thenReturn("payload-json");
        doNothing().when(notificationPublisher).send(any(BroadcastNotification.class));
        RiderJobDetails riderJobResponse = service.performActionRiderJobStatus(mockMultipartFile, "123", "1234",
                new BigDecimal("120"), new BigDecimal("0"), Boolean.valueOf(true), "remark", CancellationSource.OPS, LocalDateTime.now(), Constants.OPS_MEMBER);
        assertTrue(ObjectUtils.isNotEmpty(riderJobResponse));
        verify(excessiveWaitPaymentService, times(1)).checkAndPayEwtAmount(anyString(), any(), any(), any());
    }

    @Test
    public void riderJobCancelByOperationServiceJobIdNotFoundTest() throws InterruptedException, ExecutionException
       {
      
			when(riderProfileRepository.findById("123")).thenReturn(Optional.empty());
			RiderJobDetails riderJobDetails = new RiderJobDetails();
			riderJobDetails.setId("job-1");
			riderJobDetails.setJobId("1234");
			riderJobDetails.setJobStatus(RiderJobStatus.ORDER_CANCELLED_BY_OPERATOR);
			riderJobDetails.setRemarks("remark");
			when(riderJobDetailsRepository.save(any(RiderJobDetails.class)))
			    .thenReturn(riderJobDetails);
			JobDetails job = JobDetails.builder().jobId("1234").jobStatusKey("JOB_ACCEPTED").build();
			ResponseEntity<JobDetails> j = new ResponseEntity(HttpStatus.OK).ok(job);
			
			when(jobServiceFeignClient.getJobByJobId("1234")).thenReturn(j);
			
			RiderJobDetails riderJobResponse = service.performActionRiderJobStatus(mockMultipartFile, "123", "1234",
			        new BigDecimal("120"), new BigDecimal("0"), Boolean.valueOf(true), "remark", CancellationSource.OPS, LocalDateTime.now(), Constants.OPS_MEMBER);

			assertTrue(ObjectUtils.isNotEmpty(riderJobResponse));
			assertEquals(RiderJobStatus.ORDER_CANCELLED_BY_OPERATOR, riderJobResponse.getJobStatus());
			assertEquals("1234", riderJobResponse.getJobId());
			verify(kafkaPublisher, times(1)).publish(any(RiderJobStatusEventModel.class));
			verify(notificationPublisher, times(0)).send(any(BroadcastNotification.class));
			verify(riderJobDetailsRepository, times(1)).save(any(RiderJobDetails.class));
		 
    }

    @Test(expected = RiderJobStatusValidationException.class)
    public void riderJobCancelByOperationServiceValidationFailTest() {
        try {
			when(riderProfileRepository.findById("123")).thenReturn(Optional.of(riderProfile));
			when(riderProfileRepository.save(any())).thenReturn(riderProfile);
			riderJobDetails.setJobStatus(RiderJobStatus.FOOD_DELIVERED);
			when(propertyUtils.getProperty(anyString())).thenReturn("Validation Fail");
			when(riderJobDetailsRepository.findByJobId("1234")).thenReturn(Optional.of(riderJobDetails));
			when(riderJobDetailsRepository.save(any())).thenReturn(riderJobDetails);
			when(riderDeviceDetailRepository.findByProfileId(anyString())).thenReturn(Optional.of(riderDeviceDetails));

			RiderJobDetails riderJobResponse = service.performActionRiderJobStatus(mockMultipartFile, "123", "1234",
			        new BigDecimal("120"), new BigDecimal("0"), Boolean.valueOf(true), "remark", CancellationSource.OPS, LocalDateTime.now(), Constants.OPS_MEMBER);
			assertTrue(ObjectUtils.isNotEmpty(riderJobResponse));
		} catch (JobAlreadyCancelledException e) {
			
		}
    }

    @Test
    public void riderJobCancelByOperationServiceValidationPassTest() throws JsonProcessingException {
    	
    	
    	when(riderDeviceDetailRepository.findByProfileId(riderProfile.getId())).thenReturn(Optional.of(riderDeviceDetails));
    	when(objectMapper.writeValueAsString(Mockito.any())).thenReturn("testing");
    	
    	when(riderProfileRepository.findById("123")).thenReturn(Optional.of(riderProfile));
        when(riderProfileRepository.save(any())).thenReturn(riderProfile);
        doNothing().when(riderInactiveTrackingService).saveOrUpdateRiderInactiveStatus(any());
        riderJobDetails.setJobStatus(RiderJobStatus.JOB_ACCEPTED);
        when(riderJobDetailsRepository.findByJobId("1234")).thenReturn(Optional.of(riderJobDetails));
        when(riderJobDetailsRepository.save(any())).thenReturn(riderJobDetails);

        RiderJobDetails riderJobResponse = service.performActionRiderJobStatus(mockMultipartFile, "123", "1234",
                new BigDecimal("120"), new BigDecimal("0"), Boolean.valueOf(true), "remark", CancellationSource.OPS, LocalDateTime.now(), Constants.OPS_MEMBER);
        assertTrue(ObjectUtils.isNotEmpty(riderJobResponse));
    }

    @Test
    public void riderJobCancelByOperationServiceNotPublishEventTest() throws ExecutionException, InterruptedException {
        try {
			when(riderProfileRepository.findById("123")).thenReturn(Optional.of(riderProfile));
			when(riderProfileRepository.save(any())).thenReturn(riderProfile);
			when(riderJobDetailsRepository.findByJobId("1234")).thenReturn(Optional.of(riderJobDetails));
			when(riderJobDetailsRepository.save(any())).thenReturn(riderJobDetails);
			 doNothing().when(riderInactiveTrackingService).saveOrUpdateRiderInactiveStatus(any());
			doThrow(new InterruptedException()).when(kafkaPublisher).publish(any());

			RiderJobDetails riderJobResponse = service.performActionRiderJobStatus(mockMultipartFile, "123", "1234",
			        new BigDecimal("120"), new BigDecimal("0"), Boolean.valueOf(true), "remark", CancellationSource.OPS, LocalDateTime.now(), Constants.OPS_MEMBER);
			assertTrue(ObjectUtils.isNotEmpty(riderJobResponse));
		} catch (Exception e) {
			
		} 
    }

    
    @Test
    public void getStatusTypeTest() {
        RiderJobStatus riderJobStatus = service.getStatusType();
        assertTrue(ObjectUtils.isNotEmpty(riderJobStatus));
        assertEquals(RiderJobStatus.ORDER_CANCELLED_BY_OPERATOR, riderJobStatus);

    }
    
    @Test
    public void riderJobCancelByOperationServiceValidationPassTestAPNS() throws JsonProcessingException {
    	
    	riderDeviceDetails.setPlatform(Platform.APNS);
    	when(riderDeviceDetailRepository.findByProfileId(riderProfile.getId())).thenReturn(Optional.of(riderDeviceDetails));
    	when(objectMapper.writeValueAsString(Mockito.any())).thenReturn("testing");
    	
    	when(riderProfileRepository.findById("123")).thenReturn(Optional.of(riderProfile));
        when(riderProfileRepository.save(any())).thenReturn(riderProfile);
        riderJobDetails.setJobStatus(RiderJobStatus.JOB_ACCEPTED);
        when(riderJobDetailsRepository.findByJobId("1234")).thenReturn(Optional.of(riderJobDetails));
        when(riderJobDetailsRepository.save(any())).thenReturn(riderJobDetails);

        RiderJobDetails riderJobResponse = service.performActionRiderJobStatus(mockMultipartFile, "123", "1234",
                new BigDecimal("120"), new BigDecimal("0"), Boolean.valueOf(true), "remark", CancellationSource.OPS, LocalDateTime.now(), Constants.OPS_MEMBER);
        assertTrue(ObjectUtils.isNotEmpty(riderJobResponse));
    }
    
    @Test
    public void riderJobCancelByOperationServiceForRiderNOTFOUND() throws InterruptedException, ExecutionException
       {
      
			when(riderProfileRepository.findById("123")).thenReturn(Optional.empty());
			RiderJobDetails riderJobDetails = new RiderJobDetails();
			riderJobDetails.setId("job-1");
			riderJobDetails.setJobId("1234");
			riderJobDetails.setJobStatus(RiderJobStatus.ORDER_CANCELLED_BY_OPERATOR);
			riderJobDetails.setRemarks("remark");
			when(riderJobDetailsRepository.save(any(RiderJobDetails.class)))
			    .thenReturn(riderJobDetails);
			JobDetails job = JobDetails.builder().jobId("1234").jobStatusKey("RIDER_NOT_FOUND").build();
			ResponseEntity<JobDetails> jobEntity = new ResponseEntity(HttpStatus.OK).ok(job);
			
			when(jobServiceFeignClient.getJobByJobId("1234")).thenReturn(jobEntity);
			
			RiderJobDetails riderJobResponse = service.performActionRiderJobStatus(mockMultipartFile, "123", "1234",
			        new BigDecimal("120"), new BigDecimal("0"), Boolean.valueOf(true), "remark", CancellationSource.OPS, LocalDateTime.now(), Constants.OPS_MEMBER);

			assertTrue(ObjectUtils.isNotEmpty(riderJobResponse));
			
			verify(kafkaPublisher, times(0)).publish(any(RiderJobStatusEventModel.class));

			
		 
    }
}
