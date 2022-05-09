package com.scb.rider.service;


import com.scb.rider.exception.DataNotFoundException;
import com.scb.rider.exception.FileConversionException;
import com.scb.rider.exception.InvalidImageExtensionException;
import com.scb.rider.model.document.RiderCovidSelfie;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.dto.AddressDto;
import com.scb.rider.model.dto.RiderProfileDto;
import com.scb.rider.model.enumeration.AvailabilityStatus;
import com.scb.rider.model.enumeration.MandatoryCheckStatus;
import com.scb.rider.model.enumeration.RiderStatus;
import com.scb.rider.repository.RiderCovidSelfieRepository;
import com.scb.rider.repository.RiderProfileRepository;
import com.scb.rider.service.document.RiderCovidSelfieService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
@ActiveProfiles(value="test")
class RiderCovidSelfieServiceTest {

    @Mock
    private RiderCovidSelfieRepository riderCovidSelfieRepository;
    @Mock
    private AmazonS3ImageService s3ImageService;
    @InjectMocks
    private RiderCovidSelfieService riderCovidSelfieService;
    @Mock
    private RiderProfileRepository riderProfileRepository;
    private final static String userId = "av3d";

    private static RiderProfile riderProfile;

    private static RiderProfileDto riderProfileDto;

    @BeforeAll
    static void setUp() {
        AddressDto addressDto = AddressDto.builder()
                .city("Bangkok")
                .country("Thailand")
                .countryCode("TH")
                .district("district")
                .floorNumber("1234")
                .landmark("landmark")
                .state("state")
                .unitNumber("unitNumber")
                .village("village")
                .zipCode("203205").build();

        riderProfileDto = RiderProfileDto.builder()
                .id(userId)
                .accountNumber("121212121212121")
                .address(addressDto)
                .consentAcceptFlag(true)
                .dataSharedFlag(true)
                .firstName("Rohit").lastName("Sharma")
                .nationalID("1234567890")
                .dob("20/12/1988")
                .phoneNumber("9999999999")
                .status(RiderStatus.UNAUTHORIZED)
                .nationalIdStatus(MandatoryCheckStatus.PENDING)
                .availabilityStatus(AvailabilityStatus.Inactive)
                .build();
        riderProfile = new RiderProfile();
        BeanUtils.copyProperties(riderProfileDto, riderProfile);
    }

    @Test
    public void uploadCovidSelfieTest()
            throws InvalidImageExtensionException, FileConversionException, IOException {

        LocalDateTime localDateTime = LocalDateTime.now();
        riderProfile.setStatus(RiderStatus.AUTHORIZED);
        riderProfile.setAvailabilityStatus(AvailabilityStatus.Inactive);
        when(riderProfileRepository.findById(userId)).thenReturn(Optional.of(riderProfile));

        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "hello.png",
                MediaType.TEXT_PLAIN_VALUE, "Hello, World!".getBytes());

        RiderCovidSelfie riderCovidSelfie = RiderCovidSelfie.builder()
                .riderId(riderProfile.getId())
                .mimeType(mockMultipartFile.getContentType())
                .fileName("hello.png")
                .uploadedTime(localDateTime).build();

        when(s3ImageService.uploadFile(any(File.class), any(), any())).thenReturn("hello.png");

        when(riderCovidSelfieRepository.save(any())).thenReturn(riderCovidSelfie);


        RiderCovidSelfie response = riderCovidSelfieService.uploadCovidSelfie(mockMultipartFile,
                userId, localDateTime);

        assertEquals(userId, response.getRiderId(), "Invalid Response");

    }
    @Test
    public void uploadCovidSelfieInvalidImageExtensionTest()
            throws InvalidImageExtensionException, FileConversionException, IOException {

        LocalDateTime localDateTime = LocalDateTime.now();

        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "hello.text",
                MediaType.TEXT_PLAIN_VALUE, "Hello, World!".getBytes());


        Assertions.assertThrows(InvalidImageExtensionException.class, () -> {
             riderCovidSelfieService.uploadCovidSelfie(mockMultipartFile,
                    userId, localDateTime);
        });

    }

    @Test
    public void uploadCovidSelfieRiderNotFoundTest()
            throws InvalidImageExtensionException, FileConversionException, IOException {

        LocalDateTime localDateTime = LocalDateTime.now();
        riderProfile.setStatus(RiderStatus.AUTHORIZED);
        riderProfile.setAvailabilityStatus(AvailabilityStatus.Inactive);
        when(riderProfileRepository.findById(userId)).thenReturn(Optional.empty());

        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "hello.png",
                MediaType.TEXT_PLAIN_VALUE, "Hello, World!".getBytes());


        Assertions.assertThrows(DataNotFoundException.class, () -> {
            riderCovidSelfieService.uploadCovidSelfie(mockMultipartFile,
                    userId, localDateTime);
        });

    }

}
