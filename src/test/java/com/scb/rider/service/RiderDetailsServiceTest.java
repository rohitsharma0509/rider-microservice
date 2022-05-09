package com.scb.rider.service;

import com.scb.rider.client.OperationFeignClient;
import com.scb.rider.constants.Constants;
import com.scb.rider.constants.DocumentType;
import com.scb.rider.exception.DataNotFoundException;
import com.scb.rider.model.document.RiderBackgroundVerificationDocument;
import com.scb.rider.model.document.RiderCovidSelfie;
import com.scb.rider.model.document.RiderDeviceDetails;
import com.scb.rider.model.document.RiderDrivingLicenseDocument;
import com.scb.rider.model.document.RiderEmergencyContact;
import com.scb.rider.model.document.RiderFoodCard;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.document.RiderPreferredZones;
import com.scb.rider.model.document.RiderVehicleRegistrationDocument;
import com.scb.rider.model.document.RiderUploadedDocument;
import com.scb.rider.model.document.RiderSelectedTrainingAppointment;
import com.scb.rider.model.dto.ConfigDataResponse;
import com.scb.rider.model.dto.RiderDetailsDto;
import com.scb.rider.model.enumeration.Platform;
import com.scb.rider.model.enumeration.RiderProfileFilters;
import com.scb.rider.model.enumeration.RiderStatus;
import com.scb.rider.model.enumeration.TrainingType;
import com.scb.rider.repository.RiderBackgroundVerificationDocumentRepository;
import com.scb.rider.repository.RiderCovidSelfieRepository;
import com.scb.rider.repository.RiderDeviceDetailRepository;
import com.scb.rider.repository.RiderDrivingLicenseDocumentRepository;
import com.scb.rider.repository.RiderEmergencyContactRepository;
import com.scb.rider.repository.RiderEVFormRepository;
import com.scb.rider.repository.RiderFoodCardRepository;
import com.scb.rider.repository.RiderJobDetailsRepository;
import com.scb.rider.repository.RiderProfileRepository;
import com.scb.rider.repository.RiderTrainingAppointmentRepository;
import com.scb.rider.repository.RiderVehicleRegistrationRepository;
import com.scb.rider.repository.RiderUploadedDocumentRepository;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.ws.rs.InternalServerErrorException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
class RiderDetailsServiceTest {

    @Mock
    private RiderProfileRepository riderProfileRepository;
    @Mock
    private RiderEmergencyContactRepository riderEmergencyContactRepository;
    @Mock
    private RiderDrivingLicenseDocumentRepository riderDrivingLicenseDocumentRepository;
    @Mock
    private RiderVehicleRegistrationRepository riderVehicleRegistrationRepository;
    @Mock
    private RiderUploadedDocumentRepository riderUploadedDocumentRepository;
    @Mock
    private RiderTrainingAppointmentRepository trainingAppointmentRepository;
    @Mock
    private RiderBackgroundVerificationDocumentRepository riderBackgroundDetailsRepository;
    @Mock
    private RiderDeviceDetailRepository riderDeviceDetailRepository;
	@Mock
    private RiderJobDetailsRepository riderJobDetailsRepository;
	@Mock
    private RiderCovidSelfieRepository riderCovidSelfieRepository;
	@Mock
    private RiderEVFormRepository evFormRepository;

    @Mock
    private RiderFoodCardRepository riderFoodCardRepository;

    @Mock
    private OperationFeignClient operationFeignClient;

    @Mock
    private RiderLocationService riderLocationService;

    @InjectMocks
    private RiderDetailsService riderDetailsService;
    private String userId = "1234567890Id";

    @BeforeAll
    static void setUp() {

    }

    @Test
    void shouldFetchRiderById() {
        RiderProfile riderProfile = new RiderProfile();
        riderProfile.setId("12345");
        RiderPreferredZones riderPreferredZones = new RiderPreferredZones();
        riderPreferredZones.setPreferredZoneId("1");
        riderPreferredZones.setPreferredZoneName("Bangkok");
        riderProfile.setRiderPreferredZones(riderPreferredZones);
        ConfigDataResponse configDataResponse = new ConfigDataResponse();
        configDataResponse.setId("1");
        configDataResponse.setKey(Constants.MANNER_SCORE_POSSIBLE_MAX);
        configDataResponse.setValue("10");

        RiderUploadedDocument uploadedDocument =  RiderUploadedDocument.builder().imageUrl("/abc/image.jpg").build();
        when(riderProfileRepository.findById(userId)).thenReturn(Optional.of(riderProfile));
        when(riderEmergencyContactRepository.findByProfileId(riderProfile.getId())).thenReturn(Optional.of(new RiderEmergencyContact()));
        when(riderDrivingLicenseDocumentRepository.findByRiderProfileId(riderProfile.getId())).thenReturn(Optional.of(RiderDrivingLicenseDocument.builder().build()));
        when(riderVehicleRegistrationRepository.findByRiderProfileId(riderProfile.getId())).thenReturn(Optional.of(RiderVehicleRegistrationDocument.builder().build()));
        when(riderUploadedDocumentRepository.findByRiderProfileIdAndDocumentType(any(String.class), any(DocumentType.class))).thenReturn(Optional.of(uploadedDocument));
        RiderSelectedTrainingAppointment training = RiderSelectedTrainingAppointment.builder().riderId(riderProfile.getId()).trainingType(TrainingType.FOOD).build();
        when(trainingAppointmentRepository.findByRiderId(riderProfile.getId())).thenReturn(Arrays.asList(training));
        when(riderBackgroundDetailsRepository.findByRiderProfileId(riderProfile.getId())).thenReturn(Optional.of(RiderBackgroundVerificationDocument.builder().build()));
        when(riderFoodCardRepository.findByRiderProfileId(riderProfile.getId())).thenReturn(Optional.of(RiderFoodCard.builder().build()));
        when(riderDeviceDetailRepository.findByProfileId(riderProfile.getId())).thenReturn(Optional.of(RiderDeviceDetails.builder().build()));
        when(riderCovidSelfieRepository.findFirstByRiderIdOrderByUploadedTimeDesc(anyString())).thenReturn(Optional.empty());
        when(evFormRepository.findByRiderProfileId(anyString())).thenReturn(Optional.empty());
        when(operationFeignClient.getConfigData(anyString())).thenReturn(ConfigDataResponse.builder().build());
        when(operationFeignClient.getConfigData(anyString())).thenReturn(configDataResponse);
        RiderDetailsDto fetchedResult = riderDetailsService.getRiderDetailsById(userId, new String[]{});
        assertTrue(ObjectUtils.isNotEmpty(fetchedResult));
    }

    @Test
    void shouldFetchRiderByPhoneNumber() {
        RiderProfile riderProfile = new RiderProfile();
        riderProfile.setId("12345");
        RiderPreferredZones riderPreferredZones = new RiderPreferredZones();
        riderPreferredZones.setPreferredZoneId("1");
        riderPreferredZones.setPreferredZoneName("Bangkok");
        riderProfile.setRiderPreferredZones(riderPreferredZones);
        ConfigDataResponse configDataResponse = new ConfigDataResponse();
        configDataResponse.setId("1");
        configDataResponse.setKey(Constants.MANNER_SCORE_POSSIBLE_MAX);
        configDataResponse.setValue("10");

        RiderUploadedDocument uploadedDocument =  RiderUploadedDocument.builder().imageUrl("/abc/image.jpg").build();
        when(riderProfileRepository.findByPhoneNumber(userId)).thenReturn(Optional.of(riderProfile));
        when(riderEmergencyContactRepository.findByProfileId(riderProfile.getId())).thenReturn(Optional.of(new RiderEmergencyContact()));
        when(riderDrivingLicenseDocumentRepository.findByRiderProfileId(riderProfile.getId())).thenReturn(Optional.of(RiderDrivingLicenseDocument.builder().build()));
        when(riderVehicleRegistrationRepository.findByRiderProfileId(riderProfile.getId())).thenReturn(Optional.of(RiderVehicleRegistrationDocument.builder().build()));
        when(riderUploadedDocumentRepository.findByRiderProfileIdAndDocumentType(any(String.class), any(DocumentType.class))).thenReturn(Optional.of(uploadedDocument));
        when(trainingAppointmentRepository.findByRiderId(riderProfile.getId())).thenReturn(Arrays.asList(new RiderSelectedTrainingAppointment()));
        when(riderBackgroundDetailsRepository.findByRiderProfileId(riderProfile.getId())).thenReturn(Optional.of(RiderBackgroundVerificationDocument.builder().build()));
        when(riderFoodCardRepository.findByRiderProfileId(riderProfile.getId())).thenReturn(Optional.of(RiderFoodCard.builder().build()));
        when(riderDeviceDetailRepository.findByProfileId(riderProfile.getId())).thenReturn(Optional.of(RiderDeviceDetails.builder().build()));
        when(riderCovidSelfieRepository.findFirstByRiderIdOrderByUploadedTimeDesc(anyString())).thenReturn(Optional.empty());
        when(operationFeignClient.getConfigData(anyString())).thenReturn(configDataResponse);
        RiderDetailsDto fetchedResult = riderDetailsService.getRiderDetailsByPhoneNumber(userId);
        assertTrue(ObjectUtils.isNotEmpty(fetchedResult));
        assertTrue(ObjectUtils.isEmpty(fetchedResult.getLastUploadedCovidSelfieTime()));
    }
    @Test
    void shouldFetchRiderByPhoneNumberWhenCovidSelfieExist() {
        RiderProfile riderProfile = new RiderProfile();
        riderProfile.setId("12345");
        RiderPreferredZones riderPreferredZones = new RiderPreferredZones();
        riderPreferredZones.setPreferredZoneId("1");
        riderPreferredZones.setPreferredZoneName("Bangkok");
        riderProfile.setRiderPreferredZones(riderPreferredZones);
        ConfigDataResponse configDataResponse = new ConfigDataResponse();
        configDataResponse.setId("1");
        configDataResponse.setKey(Constants.MANNER_SCORE_POSSIBLE_MAX);
        configDataResponse.setValue("10");

        RiderCovidSelfie riderCovidSelfie= RiderCovidSelfie.builder().riderId("123").uploadedTime(LocalDateTime.now()).build();
        RiderUploadedDocument uploadedDocument =  RiderUploadedDocument.builder().imageUrl("/abc/image.jpg").build();
        when(riderProfileRepository.findByPhoneNumber(userId)).thenReturn(Optional.of(riderProfile));
        when(riderEmergencyContactRepository.findByProfileId(riderProfile.getId())).thenReturn(Optional.of(new RiderEmergencyContact()));
        when(riderDrivingLicenseDocumentRepository.findByRiderProfileId(riderProfile.getId())).thenReturn(Optional.of(RiderDrivingLicenseDocument.builder().build()));
        when(riderVehicleRegistrationRepository.findByRiderProfileId(riderProfile.getId())).thenReturn(Optional.of(RiderVehicleRegistrationDocument.builder().build()));
        when(riderUploadedDocumentRepository.findByRiderProfileIdAndDocumentType(any(String.class), any(DocumentType.class))).thenReturn(Optional.of(uploadedDocument));
        when(trainingAppointmentRepository.findByRiderId(riderProfile.getId())).thenReturn(Arrays.asList(new RiderSelectedTrainingAppointment()));
        when(riderBackgroundDetailsRepository.findByRiderProfileId(riderProfile.getId())).thenReturn(Optional.of(RiderBackgroundVerificationDocument.builder().build()));
        when(riderFoodCardRepository.findByRiderProfileId(riderProfile.getId())).thenReturn(Optional.of(RiderFoodCard.builder().build()));
        when(riderDeviceDetailRepository.findByProfileId(riderProfile.getId())).thenReturn(Optional.of(RiderDeviceDetails.builder().build()));
        when(riderCovidSelfieRepository.findFirstByRiderIdOrderByUploadedTimeDesc(anyString())).thenReturn(Optional.of(riderCovidSelfie));
        when(operationFeignClient.getConfigData(anyString())).thenReturn(configDataResponse);
        RiderDetailsDto fetchedResult = riderDetailsService.getRiderDetailsByPhoneNumber(userId);
        assertTrue(ObjectUtils.isNotEmpty(fetchedResult));
        assertTrue(ObjectUtils.isNotEmpty(fetchedResult.getLastUploadedCovidSelfieTime()));
    }
    @Test
    void shouldFetchRiderDetailsWithDocumentsById() {
        RiderProfile riderProfile = new RiderProfile();
        riderProfile.setId("1234567890Id");
        riderProfile.setAccountNumber("1234567890");
        riderProfile.setNationalID("1234567890");
        RiderPreferredZones riderPreferredZones = new RiderPreferredZones();
        riderPreferredZones.setPreferredZoneId("1");
        riderPreferredZones.setPreferredZoneName("Bangkok");
        riderProfile.setRiderPreferredZones(riderPreferredZones);
        ConfigDataResponse configDataResponse = new ConfigDataResponse();
        configDataResponse.setId("1");
        configDataResponse.setKey(Constants.MANNER_SCORE_POSSIBLE_MAX);
        configDataResponse.setValue("10");
        RiderUploadedDocument uploadedDocument =  RiderUploadedDocument.builder().imageUrl("/abc/image.jpg").build();
        when(riderProfileRepository.findByIdInAndStatusNot(userId, RiderStatus.UNAUTHORIZED)).thenReturn(Optional.of(riderProfile));
        when(riderEmergencyContactRepository.findByProfileId(riderProfile.getId())).thenReturn(Optional.of(new RiderEmergencyContact()));
        when(riderDrivingLicenseDocumentRepository.findByRiderProfileId(riderProfile.getId())).thenReturn(Optional.of(RiderDrivingLicenseDocument.builder().build()));
        when(riderVehicleRegistrationRepository.findByRiderProfileId(riderProfile.getId())).thenReturn(Optional.of(RiderVehicleRegistrationDocument.builder().build()));
        when(riderUploadedDocumentRepository.findByRiderProfileIdAndDocumentType(any(String.class), any(DocumentType.class))).thenReturn(Optional.of(uploadedDocument));
        when(riderBackgroundDetailsRepository.findByRiderProfileId(riderProfile.getId())).thenReturn(Optional.of(RiderBackgroundVerificationDocument.builder().build()));
        when(riderFoodCardRepository.findByRiderProfileId(riderProfile.getId())).thenReturn(Optional.of(RiderFoodCard.builder().build()));
        when(riderDeviceDetailRepository.findByProfileId(riderProfile.getId())).thenReturn(Optional.of(RiderDeviceDetails.builder().build()));
        when(riderCovidSelfieRepository.findFirstByRiderIdOrderByUploadedTimeDesc(anyString())).thenReturn(Optional.empty());
        when(operationFeignClient.getConfigData(anyString())).thenReturn(configDataResponse);
        RiderDetailsDto fetchedResult = riderDetailsService.getRiderDocumentDetails(userId);
        assertTrue(ObjectUtils.isNotEmpty(fetchedResult));
    }
    
    @Test
    void deleteRiderProfileByMobileNumber() {
        RiderProfile riderProfile = new RiderProfile();
        riderProfile.setId("1234567890Id");
        when(riderProfileRepository.findByPhoneNumber(Mockito.anyString())).thenReturn(Optional.of(riderProfile));
        when(this.riderJobDetailsRepository.deleteByProfileId(Mockito.anyString())).thenReturn(1L);
        RiderProfile fetchedResult = riderDetailsService.deleteRiderProfileByMobileNumber(userId);
        assertTrue(ObjectUtils.isNotEmpty(fetchedResult));
    }
    

    @Test
    void throwExceptionFetchRiderById() {
        when(this.riderProfileRepository.findByPhoneNumber(Mockito.anyString())).thenReturn(Optional.empty());
        assertThrows(DataNotFoundException.class, ()-> riderDetailsService.deleteRiderProfileByMobileNumber(userId));
    }

    @Test
    void throwExceptiondeleteRiderProfileByMobileNumber() {
        when(riderProfileRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(DataNotFoundException.class, ()-> riderDetailsService.getRiderDetailsById(userId, new String[]{}));
    }
    @Test
    void throwExceptionFetchRiderByPhoneNumber() {
        when(riderProfileRepository.findByPhoneNumber(userId)).thenReturn(Optional.empty());
        assertThrows(DataNotFoundException.class, ()-> riderDetailsService.getRiderDetailsByPhoneNumber(userId));
    }

    @Test
    void RDTC_509_throwExceptionFetchRiderByPhoneNumber_WhenWithOutPhoneNumber() {
        when(riderProfileRepository.findByPhoneNumber("")).thenReturn(Optional.empty());
        assertThrows(DataNotFoundException.class, ()-> riderDetailsService.getRiderDetailsByPhoneNumber(""));
    }

    @Test
    void RDTC_511_throwExceptionFetchRiderByPhoneNumber_WhenPhoneNumberIsEmpty() {
        when(riderProfileRepository.findByPhoneNumber("")).thenReturn(Optional.empty());
        assertThrows(DataNotFoundException.class, ()-> riderDetailsService.getRiderDetailsByPhoneNumber(""));
    }

    @Test
    void RDTC_512_throwExceptionFetchRiderByPhoneNumber_WhenPhoneNumberWrongLengthFormat() {
        when(riderProfileRepository.findByPhoneNumber("1234567890sadsad")).thenReturn(Optional.empty());
        assertThrows(DataNotFoundException.class, ()-> riderDetailsService.getRiderDetailsByPhoneNumber("1234567890sadsad"));
    }

    @Test
    void RDTC_513_throwExceptionFetchRiderByPhoneNumber_WhenPhoneNumberWrongFormat() {
        when(riderProfileRepository.findByPhoneNumber("1234sadawd")).thenReturn(Optional.empty());
        assertThrows(DataNotFoundException.class, ()-> riderDetailsService.getRiderDetailsByPhoneNumber("1234sadawd"));
    }

    @Test
    void RDTC_514_throwExceptionFetchRiderByPhoneNumber_WhenPhoneNumberIsNotExist() {
        when(riderProfileRepository.findByPhoneNumber("123456789")).thenReturn(Optional.empty());
        assertThrows(DataNotFoundException.class, ()-> riderDetailsService.getRiderDetailsByPhoneNumber("123456789"));
    }

    @Test
    void RDTC_515_throwExceptionFetchRiderByPhoneNumber_WhenReturnInternal500() {
        when(riderProfileRepository.findByPhoneNumber("123456789")).thenThrow(InternalServerErrorException.class);
        assertThrows(InternalServerErrorException.class, ()-> riderDetailsService.getRiderDetailsByPhoneNumber("123456789"));
    }

    @Test
    void throwExceptionFetchRiderDetailsWithDocumentsById() {
        when(riderProfileRepository.findByIdInAndStatusNot(userId, RiderStatus.UNAUTHORIZED)).thenReturn(Optional.empty());
        assertThrows(DataNotFoundException.class, ()-> riderDetailsService.getRiderDocumentDetails(userId));
    }

    @Test
    void shouldFetchRiderProfileWithDeviceDetails() {
        RiderProfile riderProfile = new RiderProfile();
        riderProfile.setId(userId);
        when(riderDeviceDetailRepository.findByProfileId(riderProfile.getId())).thenReturn(Optional.of(RiderDeviceDetails.builder().platform(Platform.GCM).build()));
        RiderDetailsDto fetchedResult = riderDetailsService.getFilteredRiderProfile( riderProfile, new String[] {RiderProfileFilters.RIDER_DEVICE_DETAILS.name()});
        assertTrue(ObjectUtils.isNotEmpty(fetchedResult));
        assertTrue(ObjectUtils.isNotEmpty(fetchedResult.getRiderDeviceDetails().getPlatform()));
        assertTrue(ObjectUtils.isNotEmpty(fetchedResult.getRiderProfileDto().getId()));
    }

    @Test
    void shouldFetchRiderDetailsUsingFilters(){
        String[] filters = new String[] {RiderProfileFilters.RIDER_DEVICE_DETAILS.name()};
        RiderProfile riderProfile = new RiderProfile();
        riderProfile.setId(userId);

        when(riderProfileRepository.findById(userId)).thenReturn(Optional.of(riderProfile));

        RiderDetailsDto fetchedResult = riderDetailsService.getRiderDetailsById(userId, filters);
        assertTrue(ObjectUtils.isNotEmpty(fetchedResult));
        verify(riderDeviceDetailRepository,times(1)).findByProfileId(userId);
        verify(riderProfileRepository,times(1)).findById(userId);
        verify(riderEmergencyContactRepository,times(0)).findByProfileId(userId);
        verify(riderDrivingLicenseDocumentRepository,times(0)).findByRiderProfileId(userId);

    }
}