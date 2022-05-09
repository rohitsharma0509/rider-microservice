package com.scb.rider.service.job;


import com.scb.rider.constants.Constants;
import com.scb.rider.constants.DocumentType;
import com.scb.rider.model.document.RiderJobDetails;
import com.scb.rider.model.document.RiderPreferredZones;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.enumeration.AvailabilityStatus;
import com.scb.rider.model.enumeration.CancellationSource;
import com.scb.rider.model.enumeration.RiderJobStatus;
import com.scb.rider.model.enumeration.RiderStatus;
import com.scb.rider.repository.RiderJobDetailsRepository;
import com.scb.rider.repository.RiderProfileRepository;
import com.scb.rider.service.AmazonS3ImageService;
import com.scb.rider.service.cache.RiderProfileUpdaterService;
import com.scb.rider.kafka.KafkaPublisher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import com.scb.rider.exception.DataNotFoundException;
import com.scb.rider.exception.FileConversionException;
import com.scb.rider.exception.InvalidImageExtensionException;

import com.scb.rider.service.document.RiderActiveTrackingZoneService;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
public class RiderFoodDeliveredServiceTest {

    @Mock
    private RiderJobDetailsRepository riderJobDetailsRepository;
    @Mock
    private AmazonS3ImageService amazonS3ImageService;

    @Mock
    private RiderProfileRepository riderProfileRepository;

    @Mock
    private RiderActiveTrackingZoneService riderInactiveTrackingService;
    
    @Mock
    private KafkaPublisher kafkaPublisher;

    @Mock
   	private RiderProfileUpdaterService riderProfileUpdaterService;

    @Mock
    private RiderProfileService riderProfileService;
    
    @InjectMocks
    private RiderFoodDeliveredService service;

    MockMultipartFile mockMultipartFile;
    RiderJobDetails riderJobDetails;
    RiderProfile riderProfile;

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
        riderJobDetails.setJobStatus(RiderJobStatus.FOOD_DELIVERED);
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

    @Test
    public void riderFoodDeliveredServiceTest() throws InvalidImageExtensionException, FileConversionException {
        riderJobDetails.setJobStatus(RiderJobStatus.ARRIVED_AT_CUST_LOCATION);
        when(riderJobDetailsRepository.findByJobIdAndProfileId("1234", "123")).thenReturn(Optional.of(riderJobDetails));
        //when(riderProfileRepository.save(any())).thenReturn(riderProfile);
        when(riderProfileRepository.findById(any())).thenReturn(Optional.of(riderProfile));
        //when(riderJobDetailsRepository.findByJobId("1234")).thenReturn(Optional.of(riderJobDetails));
        when(riderJobDetailsRepository.save(any())).thenReturn(riderJobDetails);
        when(amazonS3ImageService.uploadMultipartFile(mockMultipartFile, "123", DocumentType.FOOD_DELIVERED_PHOTO)).thenReturn("ImageName");

        RiderJobDetails riderJobResponse = service.performActionRiderJobStatus(mockMultipartFile, "123", "1234",
               new BigDecimal("120"), null, null, "remark", CancellationSource.RBH, LocalDateTime.now(), Constants.OPS_MEMBER);
        assertTrue(ObjectUtils.isNotEmpty(riderJobResponse));
    }

    @Test(expected = DataNotFoundException.class)
    public void riderFoodDeliveredServiceJobIdNotFoundTest() {
        //when(riderJobDetailsRepository.findByJobIdAndProfileId("1234", "123")).thenReturn(Optional.empty());
        //when(riderJobDetailsRepository.save(any())).thenReturn(riderJobDetails);

        RiderJobDetails riderJobResponse = service.performActionRiderJobStatus(mockMultipartFile, "123", "1234",
                new BigDecimal("120"), null, null, "remark", CancellationSource.RBH, LocalDateTime.now(), Constants.OPS_MEMBER);
        assertTrue(ObjectUtils.isNotEmpty(riderJobResponse));
    }

    @Test
    public void riderFoodDeliveredServiceNotPublishEventTest() throws ExecutionException, InterruptedException, InvalidImageExtensionException, FileConversionException {
        riderJobDetails.setJobStatus(RiderJobStatus.ARRIVED_AT_CUST_LOCATION);
        when(riderProfileRepository.findById("123")).thenReturn(Optional.of(riderProfile));
        //when(riderProfileRepository.save(any())).thenReturn(riderProfile);
        when(riderJobDetailsRepository.findByJobIdAndProfileId("1234", "123")).thenReturn(Optional.of(riderJobDetails));
        when(riderJobDetailsRepository.save(any())).thenReturn(riderJobDetails);
        when(amazonS3ImageService.uploadMultipartFile(mockMultipartFile, "123", DocumentType.FOOD_DELIVERED_PHOTO)).thenReturn("ImageName");

        doThrow(new InterruptedException()).when(kafkaPublisher).publish(any());
        RiderJobDetails riderJobResponse = service.performActionRiderJobStatus(mockMultipartFile, "123", "1234",
                new BigDecimal("120"), null, null, "remark", CancellationSource.RBH, LocalDateTime.now(), Constants.OPS_MEMBER);
        assertTrue(ObjectUtils.isNotEmpty(riderJobResponse));
    }

    @Test
    public void getStatusTypeTest() {
        RiderJobStatus riderJobStatus = service.getStatusType();
        assertTrue(ObjectUtils.isNotEmpty(riderJobStatus));
        assertEquals(RiderJobStatus.FOOD_DELIVERED, riderJobStatus);

    }
    
    @Test
    public void checkFoodAlreadyDeliveredTest() throws InvalidImageExtensionException, FileConversionException {
        riderJobDetails.setJobStatus(RiderJobStatus.FOOD_DELIVERED);
        when(riderProfileRepository.findById(anyString())).thenReturn(Optional.of(riderProfile));
        when(riderJobDetailsRepository.findByJobIdAndProfileId(anyString(),anyString()
        )).thenReturn(Optional.of(riderJobDetails));
        RiderJobDetails riderJobResponse = service.performActionRiderJobStatus(mockMultipartFile, "123", "1234",
            new BigDecimal("120"), null, null, "remark", CancellationSource.RBH, LocalDateTime.now(), Constants.OPS_MEMBER);
        assertTrue(ObjectUtils.isNotEmpty(riderJobResponse));
    }
}
