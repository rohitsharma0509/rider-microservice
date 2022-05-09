package com.scb.rider.service.job;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import com.scb.rider.service.document.RiderProfileService;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import com.mongodb.DuplicateKeyException;
import com.scb.rider.client.BroadCastServiceFeignClient;
import com.scb.rider.constants.Constants;
import com.scb.rider.constants.DocumentType;
import com.scb.rider.exception.DataNotFoundException;
import com.scb.rider.exception.JobAlreadyAcceptedException;
import com.scb.rider.exception.JobNotAcceptedException;
import com.scb.rider.exception.JobTimeOutException;
import com.scb.rider.kafka.KafkaPublisher;
import com.scb.rider.model.document.RiderJobDetails;
import com.scb.rider.model.document.RiderPreferredZones;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.document.RiderUploadedDocument;
import com.scb.rider.model.dto.BroadcastJobResponse;
import com.scb.rider.model.enumeration.AvailabilityStatus;
import com.scb.rider.model.enumeration.CancellationSource;
import com.scb.rider.model.enumeration.RiderJobStatus;
import com.scb.rider.model.enumeration.RiderStatus;
import com.scb.rider.repository.RiderJobDetailsRepository;
import com.scb.rider.repository.RiderProfileRepository;
import com.scb.rider.service.cache.RiderProfileUpdaterService;
import com.scb.rider.repository.RiderUploadedDocumentRepository;
import com.scb.rider.service.document.RiderActiveTrackingZoneService;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
public class RiderJobAcceptedServiceTest {

    @Mock
    private RiderJobDetailsRepository riderJobDetailsRepository;

    @Mock
    private RiderProfileRepository riderProfileRepository;

    @Mock
    private BroadCastServiceFeignClient broadCastServiceFeignClient;

    @Mock
    private KafkaPublisher kafkaPublisher;

    @Mock
    private RiderActiveTrackingZoneService riderInactiveTrackingService;

    @Mock
	private RiderProfileUpdaterService riderProfileUpdaterService;


    @Mock
    private RiderUploadedDocumentRepository uploadedDocumentRepository;

    @Mock
    private RiderProfileService riderProfileService;

    @InjectMocks
    private RiderJobAcceptedService service;

    private MockMultipartFile mockMultipartFile;
    private RiderJobDetails riderJobDetails;
    private RiderProfile riderProfile;

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
        riderJobDetails.setJobStatus(RiderJobStatus.JOB_ACCEPTED);
        riderJobDetails.setRemarks("remark");
        riderJobDetails.setParkingFee(new BigDecimal("120"));
        riderJobDetails.setMealPhotoUrl("meal_photo.jpg");
        riderJobDetails.setParkingPhotoUrl("parking_photo.jpg");

        riderProfile = new RiderProfile();
		riderProfile.setId("123");
		RiderPreferredZones zones = new RiderPreferredZones();
		zones.setPreferredZoneId("1");
		riderProfile.setRiderPreferredZones(zones);
		riderProfile.setAvailabilityStatus(AvailabilityStatus.Active);
		riderProfile.setStatus(RiderStatus.AUTHORIZED);
    }

    @Test(expected = JobAlreadyAcceptedException.class)
    public void riderJobAcceptedServiceTestAlreadyJobAssigned() {
        when(riderProfileRepository.findById("123")).thenReturn(Optional.of(riderProfile));
       // when(riderProfileRepository.save(any())).thenReturn(riderProfile);
        when(riderJobDetailsRepository.findByJobId("1234")).thenReturn(Optional.of(riderJobDetails));
        //when(riderJobDetailsRepository.save(any())).thenReturn(riderJobDetails);

        RiderJobDetails riderJobResponse = service.performActionRiderJobStatus(mockMultipartFile, "123", "1234",
                new BigDecimal("120"), null, null, "remark", CancellationSource.RBH, LocalDateTime.now(), Constants.OPS_MEMBER);
        assertTrue(ObjectUtils.isNotEmpty(riderJobResponse));
    }

    @Test
    public void riderJobAcceptedServiceTest() {
        when(riderProfileRepository.findById("123")).thenReturn(Optional.of(riderProfile));
        //when(riderProfileRepository.save(any())).thenReturn(riderProfile);
        when(riderJobDetailsRepository.findByJobId("1234")).thenReturn(Optional.empty());
        when(riderJobDetailsRepository.save(any())).thenReturn(riderJobDetails);
        doNothing().when(riderInactiveTrackingService).saveOrUpdateRiderInactiveStatus(any());
        BroadcastJobResponse broadcastJobResponse = BroadcastJobResponse.builder()
                .broadcastStatus(Constants.BROADCASTING).jobId("1234").build();
        when(broadCastServiceFeignClient.getBroadcastData("1234")).thenReturn(broadcastJobResponse);
        RiderUploadedDocument doc= RiderUploadedDocument.builder().imageExternalUrl("test").riderProfileId("23").build();

        when(uploadedDocumentRepository
                .findByRiderProfileIdAndDocumentType("123", DocumentType.PROFILE_PHOTO)).thenReturn(Optional.of(doc));
        RiderJobDetails riderJobResponse = service.performActionRiderJobStatus(mockMultipartFile, "123", "1234",
                new BigDecimal("120"), null, null, "remark", CancellationSource.RBH, LocalDateTime.now(), Constants.OPS_MEMBER);
        assertTrue(ObjectUtils.isNotEmpty(riderJobResponse));
    }

    @Test
    public void riderJobAcceptedServiceTestWithNullTimeStamp() {
        when(riderProfileRepository.findById("123")).thenReturn(Optional.of(riderProfile));
        //when(riderProfileRepository.save(any())).thenReturn(riderProfile);
        when(riderJobDetailsRepository.findByJobId("1234")).thenReturn(Optional.empty());
        when(riderJobDetailsRepository.save(any())).thenReturn(riderJobDetails);
        BroadcastJobResponse broadcastJobResponse = BroadcastJobResponse.builder()
                .broadcastStatus(Constants.BROADCASTING).jobId("1234").build();
        when(broadCastServiceFeignClient.getBroadcastData("1234")).thenReturn(broadcastJobResponse);

        RiderJobDetails riderJobResponse = service.performActionRiderJobStatus(mockMultipartFile, "123", "1234",
                new BigDecimal("120"), null, null, "remark", CancellationSource.RBH, null, Constants.OPS_MEMBER);
        assertTrue(ObjectUtils.isNotEmpty(riderJobResponse));
    }

    @Test(expected = JobAlreadyAcceptedException.class)
    public void riderJobAcceptedServiceTestWithJobInProgress() {
        riderProfile.setAvailabilityStatus(AvailabilityStatus.JobInProgress);
        when(riderProfileRepository.findById("123")).thenReturn(Optional.of(riderProfile));
        //when(riderProfileRepository.save(any())).thenReturn(riderProfile);
        when(riderJobDetailsRepository.findByJobId("1234")).thenReturn(Optional.empty());
        //when(riderJobDetailsRepository.save(any())).thenReturn(riderJobDetails);
        BroadcastJobResponse broadcastJobResponse = BroadcastJobResponse.builder()
                .broadcastStatus(Constants.BROADCASTING).jobId("1234").build();
        when(broadCastServiceFeignClient.getBroadcastData("1234")).thenReturn(broadcastJobResponse);

        RiderJobDetails riderJobResponse = service.performActionRiderJobStatus(mockMultipartFile, "123", "1234",
                new BigDecimal("120"), null, null, "remark", CancellationSource.RBH, LocalDateTime.now(), Constants.OPS_MEMBER);

    }

    @Test(expected = DuplicateKeyException.class)
    public void riderJobAcceptedServiceTestWithDuplicateKeyException() {
        when(riderProfileRepository.findById("123")).thenReturn(Optional.of(riderProfile));
        //when(riderProfileRepository.save(any())).thenThrow(DuplicateKeyException.class);
        when(riderJobDetailsRepository.findByJobId("1234")).thenReturn(Optional.empty());
        when(riderJobDetailsRepository.save(any())).thenThrow(DuplicateKeyException.class);
        BroadcastJobResponse broadcastJobResponse = BroadcastJobResponse.builder()
                .broadcastStatus(Constants.BROADCASTING).jobId("1234").build();
        when(broadCastServiceFeignClient.getBroadcastData("1234")).thenReturn(broadcastJobResponse);

        RiderJobDetails riderJobResponse = service.performActionRiderJobStatus(mockMultipartFile, "123", "1234",
                new BigDecimal("120"), null, null, "remark", CancellationSource.RBH, LocalDateTime.now(), Constants.OPS_MEMBER);

    }

    @Test(expected = DataNotFoundException.class)
    public void riderJobAcceptedServiceJobIdNotFoundTest() {
        when(riderProfileRepository.findById("123")).thenReturn(Optional.empty());

        RiderJobDetails riderJobResponse = service.performActionRiderJobStatus(mockMultipartFile, "123", "1234",
                new BigDecimal("120"), null, null, "remark", CancellationSource.RBH, LocalDateTime.now(), Constants.OPS_MEMBER);
        assertTrue(ObjectUtils.isNotEmpty(riderJobResponse));
    }

    @Test
    public void riderJobAcceptedServiceNotPublishEventTest() throws ExecutionException, InterruptedException {
        when(riderProfileRepository.findById("123")).thenReturn(Optional.of(riderProfile));
        //when(riderProfileRepository.save(any())).thenReturn(riderProfile);
        when(riderJobDetailsRepository.findByJobId("1234")).thenReturn(Optional.empty());
        when(riderJobDetailsRepository.save(any())).thenReturn(riderJobDetails);
        doThrow(new InterruptedException()).when(kafkaPublisher).publish(any());
        BroadcastJobResponse broadcastJobResponse = BroadcastJobResponse.builder()
                .broadcastStatus(Constants.BROADCASTING).jobId("1234").build();
        when(broadCastServiceFeignClient.getBroadcastData("1234")).thenReturn(broadcastJobResponse);

        RiderJobDetails riderJobResponse = service.performActionRiderJobStatus(mockMultipartFile, "123", "1234",
                new BigDecimal("120"), null, null, "remark", CancellationSource.RBH, LocalDateTime.now(), Constants.OPS_MEMBER);
        assertTrue(ObjectUtils.isNotEmpty(riderJobResponse));
    }

    @Test(expected = JobTimeOutException.class)
    public void riderJobAcceptedServiceWithoutBroadcastingStatus() throws ExecutionException, InterruptedException {
        when(riderProfileRepository.findById("123")).thenReturn(Optional.of(riderProfile));
        when(riderJobDetailsRepository.findByJobId("1234")).thenReturn(Optional.empty());
        doNothing().when(riderInactiveTrackingService).saveOrUpdateRiderInactiveStatus(any());
        BroadcastJobResponse broadcastJobResponse = BroadcastJobResponse.builder()
                .broadcastStatus("CANCELLED").jobId("1234").build();
        when(broadCastServiceFeignClient.getBroadcastData("1234")).thenReturn(broadcastJobResponse);

        RiderJobDetails riderJobResponse = service.performActionRiderJobStatus(mockMultipartFile, "123", "1234",
                new BigDecimal("120"), null, null, "remark", CancellationSource.RBH, LocalDateTime.now(), Constants.OPS_MEMBER);
        assertTrue(ObjectUtils.isNotEmpty(riderJobResponse));
    }

    @Test(expected = JobNotAcceptedException.class)
    public void riderJobAcceptedServiceWithJobNotFound() throws ExecutionException, InterruptedException {
        when(riderProfileRepository.findById("123")).thenReturn(Optional.of(riderProfile));
        when(riderJobDetailsRepository.findByJobId("1234")).thenReturn(Optional.empty());
        when(broadCastServiceFeignClient.getBroadcastData("1234")).thenThrow(JobNotAcceptedException.class);
        doNothing().when(riderInactiveTrackingService).saveOrUpdateRiderInactiveStatus(any());
        RiderJobDetails riderJobResponse = service.performActionRiderJobStatus(mockMultipartFile, "123", "1234",
                new BigDecimal("120"), null, null, "remark", CancellationSource.RBH, LocalDateTime.now(), Constants.OPS_MEMBER);
        assertTrue(ObjectUtils.isNotEmpty(riderJobResponse));

    }

    @Test
    public void getStatusTypeTest() {
        RiderJobStatus riderJobStatus = service.getStatusType();
        assertTrue(ObjectUtils.isNotEmpty(riderJobStatus));
        assertEquals(RiderJobStatus.JOB_ACCEPTED, riderJobStatus);

    }

    @Test(expected = JobAlreadyAcceptedException.class)
    public void riderJobAcceptedWithWhenJobAlredayExistTest() {
        when(riderProfileRepository.findById("123")).thenReturn(Optional.of(riderProfile));
        //when(riderProfileRepository.save(any())).thenReturn(riderProfile);
        when(riderJobDetailsRepository.findByJobId("1234")).thenReturn(Optional.of(riderJobDetails));
        //when(riderJobDetailsRepository.save(any())).thenThrow(DuplicateKeyException.class);
        doNothing().when(riderInactiveTrackingService).saveOrUpdateRiderInactiveStatus(any());
        RiderJobDetails riderJobResponse = service.performActionRiderJobStatus(mockMultipartFile, "123", "1234",
                new BigDecimal("120"), null, null, "remark", CancellationSource.RBH, LocalDateTime.now(), Constants.OPS_MEMBER);
        assertTrue(ObjectUtils.isNotEmpty(riderJobResponse));
    }

}
