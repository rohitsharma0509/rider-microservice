package com.scb.rider.service.job;

import com.scb.rider.constants.Constants;
import com.scb.rider.exception.DataNotFoundException;
import com.scb.rider.model.document.RiderJobDetails;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.enumeration.CancellationSource;
import com.scb.rider.model.enumeration.RiderJobStatus;
import com.scb.rider.repository.RiderJobDetailsRepository;
import com.scb.rider.kafka.KafkaPublisher;
import com.scb.rider.repository.RiderProfileRepository;
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
public class RiderCalledMerchantServiceTest {

    @Mock
    private RiderJobDetailsRepository riderJobDetailsRepository;

    @Mock
    private KafkaPublisher kafkaPublisher;

    @Mock
    private RiderProfileRepository riderProfileRepository;

    @InjectMocks
    private RiderCalledMerchantService service;

    MockMultipartFile mockMultipartFile;
    RiderJobDetails riderJobDetails;
    RiderProfile  riderProfile;

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
        riderJobDetails.setJobStatus(RiderJobStatus.CALLED_MERCHANT);
        riderJobDetails.setRemarks("remark");
        riderJobDetails.setParkingFee(new BigDecimal("120"));
        riderJobDetails.setMealPhotoUrl("meal_photo.jpg");
        riderJobDetails.setParkingPhotoUrl("parking_photo.jpg");

        riderProfile = new RiderProfile();
        riderProfile.setRiderId("RR00012");
        riderProfile.setId("1213123");
    }

    @Test
    public void riderCalledMerchantServiceTest() {
        riderJobDetails.setJobStatus(RiderJobStatus.JOB_ACCEPTED);

        when(riderJobDetailsRepository.findByJobIdAndProfileId("1234","123")).thenReturn(Optional.of(riderJobDetails));
        when(riderJobDetailsRepository.save(any())).thenReturn(riderJobDetails);
        when(riderProfileRepository.findById(any())).thenReturn(Optional.of(riderProfile));

        RiderJobDetails riderJobResponse = service.performActionRiderJobStatus(mockMultipartFile, "123", "1234",
                new BigDecimal("120"), null, null, "remark", CancellationSource.RBH, LocalDateTime.now(), Constants.OPS_MEMBER);
        assertTrue(ObjectUtils.isNotEmpty(riderJobResponse));
    }
    
    @Test
    public void riderSameStatusExceptionTest() {
        riderJobDetails.setJobStatus(RiderJobStatus.CALLED_MERCHANT);

        when(riderJobDetailsRepository.findByJobIdAndProfileId("1234","123")).thenReturn(Optional.of(riderJobDetails));
        when(riderJobDetailsRepository.save(any())).thenReturn(riderJobDetails);
        when(riderProfileRepository.findById(any())).thenReturn(Optional.of(riderProfile));

        RiderJobDetails riderJobResponse = service.performActionRiderJobStatus(mockMultipartFile, "123", "1234",
                new BigDecimal("120"), null, null, "remark", CancellationSource.RBH, LocalDateTime.now(), Constants.OPS_MEMBER);
        assertTrue(ObjectUtils.isNotEmpty(riderJobResponse));
    }

    @Test(expected = DataNotFoundException.class)
    public void riderCalledMerchantServiceJobIdNotFoundTest() {
        when(riderJobDetailsRepository.findByJobIdAndProfileId("1234","123")).thenReturn(Optional.empty());
        when(riderJobDetailsRepository.save(any())).thenReturn(riderJobDetails);
        when(riderProfileRepository.findById(any())).thenReturn(Optional.of(riderProfile));

        RiderJobDetails riderJobResponse = service.performActionRiderJobStatus(mockMultipartFile, "123", "1234",
                new BigDecimal("120"), null, null, "remark",CancellationSource.RBH, LocalDateTime.now(), Constants.OPS_MEMBER);
        assertTrue(ObjectUtils.isNotEmpty(riderJobResponse));
    }

    @Test
    public void riderCalledMerchantServiceNotPublishEventTest() throws ExecutionException, InterruptedException {
        riderJobDetails.setJobStatus(RiderJobStatus.JOB_ACCEPTED);

        when(riderJobDetailsRepository.findByJobIdAndProfileId("1234","123")).thenReturn(Optional.of(riderJobDetails));
        when(riderJobDetailsRepository.save(any())).thenReturn(riderJobDetails);
        when(riderProfileRepository.findById(any())).thenReturn(Optional.of(riderProfile));

        doThrow(new InterruptedException()).when(kafkaPublisher).publish(any());
        RiderJobDetails riderJobResponse = service.performActionRiderJobStatus(mockMultipartFile, "123", "1234",
                new BigDecimal("120"), null, null, "remark",CancellationSource.RBH, LocalDateTime.now(), Constants.OPS_MEMBER);
        assertTrue(ObjectUtils.isNotEmpty(riderJobResponse));
    }

    @Test
    public void getStatusTypeTest(){
        RiderJobStatus riderJobStatus = service.getStatusType();
        assertTrue(ObjectUtils.isNotEmpty(riderJobStatus));
        assertEquals(RiderJobStatus.CALLED_MERCHANT, riderJobStatus);

    }
}
