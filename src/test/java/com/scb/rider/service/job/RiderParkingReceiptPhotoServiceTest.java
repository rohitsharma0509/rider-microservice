package com.scb.rider.service.job;

import com.scb.rider.constants.Constants;
import com.scb.rider.constants.DocumentType;
import com.scb.rider.exception.DataNotFoundException;
import com.scb.rider.exception.FileConversionException;
import com.scb.rider.exception.InvalidImageExtensionException;
import com.scb.rider.model.document.RiderJobDetails;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.enumeration.CancellationSource;
import com.scb.rider.model.enumeration.RiderJobStatus;
import com.scb.rider.repository.RiderJobDetailsRepository;
import com.scb.rider.repository.RiderProfileRepository;
import com.scb.rider.service.AmazonS3ImageService;
import com.scb.rider.kafka.KafkaPublisher;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
public class RiderParkingReceiptPhotoServiceTest {

    @Mock
    private RiderJobDetailsRepository riderJobDetailsRepository;

    @Mock
    private RiderProfileRepository riderProfileRepository;

    @Mock
    private AmazonS3ImageService amazonS3ImageService;

    @Mock
    private KafkaPublisher kafkaPublisher;

    @InjectMocks
    private RiderParkingReceiptPhotoService service;

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
        riderJobDetails.setJobStatus(RiderJobStatus.MEAL_PICKED_UP);
        riderJobDetails.setRemarks("remark");
        riderJobDetails.setParkingFee(new BigDecimal("120"));
        riderJobDetails.setMealPhotoUrl("meal_photo.jpg");
        riderJobDetails.setParkingPhotoUrl("parking_photo.jpg");

        riderProfile = new RiderProfile();
        riderProfile.setId("12133");
    }

    @Test
    public void riderParkingReceiptPhotoServiceTest() throws InvalidImageExtensionException, FileConversionException {
        when(riderProfileRepository.findById("123")).thenReturn(Optional.of(riderProfile));
        when(riderProfileRepository.save(any())).thenReturn(riderProfile);
        when(riderJobDetailsRepository.findByJobIdAndProfileId("1234", "123")).thenReturn(Optional.of(riderJobDetails));
        when(riderJobDetailsRepository.save(any())).thenReturn(riderJobDetails);
        when(amazonS3ImageService.uploadMultipartFile(mockMultipartFile, "123", DocumentType.PARKING_RECIEPT)).thenReturn("ImageName");

        RiderJobDetails riderJobResponse = service.performActionRiderJobStatus(mockMultipartFile, "123", "1234",
                new BigDecimal("120"), null, null, "remark", CancellationSource.RBH, LocalDateTime.now(), Constants.OPS_MEMBER);
        assertTrue(ObjectUtils.isNotEmpty(riderJobResponse));
    }

    @Test(expected = DataNotFoundException.class)
    public void riderParkingReceiptPhotoServiceJobIdNotFoundTest() {
        when(riderProfileRepository.findById("123")).thenReturn(Optional.empty());

        RiderJobDetails riderJobResponse = service.performActionRiderJobStatus(mockMultipartFile, "123", "1234",
                new BigDecimal("120"), null, null, "remark", CancellationSource.RBH, LocalDateTime.now(), Constants.OPS_MEMBER);
        assertTrue(ObjectUtils.isNotEmpty(riderJobResponse));
    }

    @Test
    public void riderParkingReceiptPhotoServiceNotPublishEventTest() throws ExecutionException, InterruptedException,
            InvalidImageExtensionException, FileConversionException {
        when(riderProfileRepository.findById("123")).thenReturn(Optional.of(riderProfile));
        when(riderProfileRepository.save(any())).thenReturn(riderProfile);
        when(riderJobDetailsRepository.findByJobIdAndProfileId("1234", "123")).thenReturn(Optional.of(riderJobDetails));
        when(riderJobDetailsRepository.save(any())).thenReturn(riderJobDetails);
        when(amazonS3ImageService.uploadMultipartFile(mockMultipartFile, "123", DocumentType.PARKING_RECIEPT)).thenReturn("ImageName");

        doThrow(new InterruptedException()).when(kafkaPublisher).publish(any());

        RiderJobDetails riderJobResponse = service.performActionRiderJobStatus(mockMultipartFile, "123", "1234",
                new BigDecimal("120"), null, null, "remark", CancellationSource.RBH, LocalDateTime.now(), Constants.OPS_MEMBER);
        assertTrue(ObjectUtils.isNotEmpty(riderJobResponse));
    }

    @Test
    public void riderParkingReceiptPhotoServiceImageNotSaveTest() throws ExecutionException, InterruptedException,
            InvalidImageExtensionException, FileConversionException {
        when(riderProfileRepository.findById("123")).thenReturn(Optional.of(riderProfile));
        when(riderProfileRepository.save(any())).thenReturn(riderProfile);
        when(riderJobDetailsRepository.findByJobIdAndProfileId("1234", "123")).thenReturn(Optional.of(riderJobDetails));
        when(riderJobDetailsRepository.save(any())).thenReturn(riderJobDetails);
        when(amazonS3ImageService.uploadMultipartFile(mockMultipartFile, "123", DocumentType.PARKING_RECIEPT)).thenThrow(InvalidImageExtensionException.class);

        doThrow(new InterruptedException()).when(kafkaPublisher).publish(any());

        RiderJobDetails riderJobResponse = service.performActionRiderJobStatus(mockMultipartFile, "123", "1234",
                new BigDecimal("120"), null, null, "remark", CancellationSource.RBH, LocalDateTime.now(), Constants.OPS_MEMBER);
        assertTrue(ObjectUtils.isNotEmpty(riderJobResponse));
    }

    @Test
    public void getStatusTypeTest() {
        RiderJobStatus riderJobStatus = service.getStatusType();
        assertTrue(ObjectUtils.isNotEmpty(riderJobStatus));
        assertEquals(RiderJobStatus.PARKING_RECEIPT_PHOTO, riderJobStatus);

    }
}
