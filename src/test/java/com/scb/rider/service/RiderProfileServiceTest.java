package com.scb.rider.service;

import com.amazonaws.services.cognitoidp.model.AdminDeleteUserResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.UpdateResult;
import com.scb.rider.client.OperationFeignClient;
import com.scb.rider.client.PocketServiceFeignClient;
import com.scb.rider.constants.Constants;
import com.scb.rider.constants.DocumentType;
import com.scb.rider.constants.SmsConstants;
import com.scb.rider.exception.*;
import com.scb.rider.kafka.SmsPublisher;
import com.scb.rider.model.document.*;
import com.scb.rider.model.dto.*;
import com.scb.rider.model.enumeration.*;
import com.scb.rider.repository.*;
import com.scb.rider.service.cache.RiderProfileUpdaterService;
import com.scb.rider.service.document.RiderActiveTrackingZoneService;
import com.scb.rider.service.document.RiderProfileService;
import com.scb.rider.util.PropertyUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
@ActiveProfiles(value="test")
class RiderProfileServiceTest {

  @Mock
  private RiderProfileRepository riderProfileRepository;

  @Mock
  private AWSCognitoService awsCognitoService;

  @Mock
  private RiderJobDetailsRepository riderJobDetailsRepository;

  @Mock
  private RiderActiveStatusDetailsRepository riderActiveStatusDetailsRepository;

  @Mock
  private RiderActiveTrackingZoneService riderInactiveTrackingService;

  @Mock
  private RiderVehicleRegistrationRepository vehicleRegistrationRepository;

  @Mock
  private RiderDrivingLicenseDocumentRepository riderDrivingLicenseDocumentRepository;

  @Mock
  private RiderTrainingAppointmentRepository riderTrainingAppointmentRepository;

  @Mock
  private  PocketServiceFeignClient pocketServiceFeignClient;

  @Mock
  private PropertyUtils propertyUtils;

  @Mock
  private SmsPublisher smsPublisher;

  @Mock
  private OperationFeignClient operationFeignClient;

  @Mock
  private MongoTemplate mongoTemplate;

  @InjectMocks
  private RiderProfileService riderProfileService;

  @Mock
  private ObjectMapper mapper;

  @Mock
	private RiderProfileUpdaterService riderProfileUpdaterService;

  @Mock
  private RiderSuspendHistoryRepository riderSuspendHistoryRepository;
  
  private final static String userId = "av3d";
  private final static String RIDER_AUTHORIZED = "Rider has been authorized";
  private final static String RIDER_SUSPENDED = "Rider has been suspended";
  private final static String RIDER_REAUTHORIZED = "Rider has been reauthorized";
  private static final String TRAINING_CONFIGURABLE_WEEKS = "trainingConfigurableWeeks";
  private final static String RIDER_SELFIE_REJECTED = "Rider Selfie Rejected";


  private static RiderProfile riderProfile;

  private static RiderProfileDto riderProfileDto;

  private static ConfigDataResponse configDataResponse;

  private static RiderProfileUpdateRequestDto profileUpdateRequestDto;

  private static NationalAddressUpdateRequestDto  nationalAddressUpdateRequestDto;

  private static RiderSuspendHistory riderSuspendHistory;

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

      NationalAddressDto nationalAddressDto = NationalAddressDto.builder().alley("alley1").district("district")
              .floor("1").buildingName("building1").neighbourhood("neighbour").number("2")
              .postalCode("12345").subdistrict("sub")
              .district("dist").road("road").roomNumber("456").province("pro").build();

        riderProfileDto = RiderProfileDto.builder()
                .id(userId)
                .accountNumber("121212121212121")
                .address(addressDto)
                .nationalAddress(nationalAddressDto)
                .consentAcceptFlag(true)
                .dataSharedFlag(true)
                .firstName("Rohit").lastName("Sharma")
                .riderId("RR10001")
                .nationalID("1234567890")
                .dob("20/12/1988")
                .phoneNumber("9999999999")
                .riderId("RR123")
                .status(RiderStatus.UNAUTHORIZED)
                .nationalIdStatus(MandatoryCheckStatus.PENDING)
                .availabilityStatus(AvailabilityStatus.Inactive)
                .build();
        riderProfile = new RiderProfile();
        BeanUtils.copyProperties(riderProfileDto, riderProfile);
        profileUpdateRequestDto = RiderProfileUpdateRequestDto.builder()
                .id(userId)
                .phoneNumber("7777777777")
                .address(addressDto)
                .nationalAddress(nationalAddressDto)
                .build();

      nationalAddressUpdateRequestDto = NationalAddressUpdateRequestDto
              .builder()
              .nationalAddress(nationalAddressDto)
              .build();

        configDataResponse = ConfigDataResponse.builder().id("TEST")
            .key("trainingConfigurableWeeks")
            .value("2").build();

      riderSuspendHistory = RiderSuspendHistory.builder()
                .riderId(riderProfile.getRiderId())
                .suspensionReason(Arrays.asList("Dangerous driving"))
                .suspensionNote("Hit and run registered")
                .suspensionDuration(1)
                .createdBy("A")
                .build();
    }

  @Test
  void shouldCreateNewRider() {
    when(riderProfileRepository.save(any(RiderProfile.class))).thenReturn(riderProfile);
    RiderProfile fetchedProfile = riderProfileService.createRiderProfile(riderProfileDto);
    assertTrue(ObjectUtils.isNotEmpty(fetchedProfile));
    assertNotNull(fetchedProfile.getId());
  }

    @Test
    void shouldUpdateRider() {
        riderProfile.setStatus(RiderStatus.AUTHORIZED);
        riderProfile.setAvailabilityStatus(AvailabilityStatus.Inactive);
        when(riderProfileRepository.findById(profileUpdateRequestDto.getId())).thenReturn(Optional.of(riderProfile));
        when(riderProfileRepository.save(riderProfile)).thenReturn(riderProfile);
        RiderProfile fetchedProfile = riderProfileService.updateRiderProfile(profileUpdateRequestDto);
        assertTrue(ObjectUtils.isNotEmpty(fetchedProfile));
        assertNotNull(fetchedProfile.getId());
        riderProfile.setAvailabilityStatus(AvailabilityStatus.Active);
    }

  @Test
  void shouldUpdateRiderProfileByOps() {
    riderProfile.setStatus(RiderStatus.UNAUTHORIZED);
    riderProfile.setAvailabilityStatus(AvailabilityStatus.Inactive);
    when(riderProfileRepository.findById(profileUpdateRequestDto.getId())).thenReturn(Optional.of(riderProfile));
    when(riderProfileRepository.save(riderProfile)).thenReturn(riderProfile);
    RiderProfile fetchedProfile = riderProfileService.updateRiderProfileOpsMember(profileUpdateRequestDto);
    assertTrue(ObjectUtils.isNotEmpty(fetchedProfile));
    assertNotNull(fetchedProfile.getId());
    riderProfile.setAvailabilityStatus(AvailabilityStatus.Active);
  }


  @Test
    void shouldUpdateRiderWithException() {
        riderProfile.setAvailabilityStatus(AvailabilityStatus.Active);
        when(riderProfileRepository.findById(profileUpdateRequestDto.getId())).thenReturn(Optional.of(riderProfile));
        assertThrows(UpdatePhoneNumberException.class,
                () -> riderProfileService.updateRiderProfile(profileUpdateRequestDto));
    }

    @Test
    void shouldFetchRiderById() {
        when(riderProfileRepository.findById(userId)).thenReturn(Optional.of(riderProfile));
        RiderProfile fetchedProfile = riderProfileService.getRiderProfileById(userId);
        assertTrue(ObjectUtils.isNotEmpty(fetchedProfile));
        assertNotNull(fetchedProfile.getId());
    }

  @Test
  void throwExceptionFetchRiderById() {
    when(riderProfileRepository.findById(userId)).thenReturn(Optional.empty());
    assertThrows(DataNotFoundException.class,
        () -> riderProfileService.getRiderProfileById(userId));
  }

    @Test
    void shouldFetchRiderByPhoneNumber() {
        when(riderProfileRepository.findByPhoneNumber(riderProfileDto.getPhoneNumber())).thenReturn(Optional.of(riderProfile));
        RiderProfile fetchedProfile = riderProfileService.getRiderProfileByPhoneNumber(riderProfileDto.getPhoneNumber());
        assertTrue(ObjectUtils.isNotEmpty(fetchedProfile));
        assertNotNull(fetchedProfile.getId());
    }

    @Test
    void throwExceptionFetchRiderByPhoneNumber() {
        when(riderProfileRepository.findByPhoneNumber(riderProfileDto.getPhoneNumber())).thenReturn(Optional.empty());
        assertThrows(DataNotFoundException.class, () -> riderProfileService.getRiderProfileByPhoneNumber(riderProfileDto.getPhoneNumber()));
    }

    @Test
    void throwExceptionUpdateRider() {
        when(riderProfileRepository.findById(riderProfileDto.getId())).thenReturn(Optional.empty());
        assertThrows(DataNotFoundException.class, () -> riderProfileService.updateRiderProfile(profileUpdateRequestDto));
    }

    @Test
    void shouldSetRiderAvailabilityStatus() {
    	when(riderProfileRepository.findById(riderProfileDto.getId())).thenReturn(Optional.of(riderProfile));
    	when(mongoTemplate
                .updateFirst(any(Query.class), any(Update.class),  eq(RiderProfile.class)))
                .thenReturn(UpdateResult.acknowledged(1L,1L, null));
        when(riderJobDetailsRepository
        .findByProfileIdAndJobStatusNotIn(anyString(), any())).thenReturn(new ArrayList<RiderJobDetails>());

        RiderActiveStatusDetails det=RiderActiveStatusDetails.builder().activeTime(LocalDateTime.now()).build();
        Optional<RiderActiveStatusDetails> op=Optional.of(det);
        RiderProfile fetchedProfile = riderProfileService.setRiderStatus(riderProfile.getId(), AvailabilityStatus.Active);
        assertTrue(ObjectUtils.isNotEmpty(fetchedProfile));
        assertNotNull(fetchedProfile.getId());
        verify(mongoTemplate, times(1))
                .updateFirst(any(Query.class), any(Update.class), eq(RiderProfile.class));
    }

  @Test
  void throwExceptionSetRiderStatus() {
    when(riderProfileRepository.findById(riderProfileDto.getId())).thenReturn(Optional.empty());
    assertThrows(DataNotFoundException.class,
        () -> riderProfileService.setRiderStatus(riderProfile.getId(), AvailabilityStatus.Active));
  }

  @Test
  void throwExceptionUpdateRiderStatusWhenRiderNotPresent() {
    when(riderProfileRepository.findById(userId)).thenReturn(Optional.empty());
    RiderStatusDto riderStatusDto = RiderStatusDto.builder().profileId(userId).build();
    assertThrows(DataNotFoundException.class,
        () -> riderProfileService.updateRiderStatus(riderStatusDto));
  }

  @Test
  void throwExceptionUpdateRiderStatusWhenRiderAlreadyInGivenStatus() {
    riderProfile.setStatus(RiderStatus.UNAUTHORIZED);
    when(riderProfileRepository.findById(userId)).thenReturn(Optional.of(riderProfile));
    RiderStatusDto riderStatusDto =
            RiderStatusDto.builder().profileId(userId).status(RiderStatus.UNAUTHORIZED).build();
    assertThrows(StatusTransitionNotAllowedException.class,
            () -> riderProfileService.updateRiderStatus(riderStatusDto));
  }

  @Test
  void throwExceptionUpdateRiderStatusWhenStatusCannotBeChanged() {
    RiderProfile rider = new RiderProfile();
    rider.setId(userId);
    rider.setStatus(RiderStatus.AUTHORIZED);
    when(riderProfileRepository.findById(userId)).thenReturn(Optional.of(rider));
    RiderStatusDto riderStatusDto =
        RiderStatusDto.builder().profileId(userId).status(RiderStatus.UNAUTHORIZED).build();
    assertThrows(StatusTransitionNotAllowedException.class,
        () -> riderProfileService.updateRiderStatus(riderStatusDto));
  }

  @Test
  void throwExceptionUpdateRiderStatusWhenSuspendingAUnauthorizedRider() {
    RiderProfile rider = new RiderProfile();
    rider.setId(userId);
    rider.setStatus(RiderStatus.UNAUTHORIZED);
    when(riderProfileRepository.findById(userId)).thenReturn(Optional.of(rider));
    RiderStatusDto riderStatusDto =
        RiderStatusDto.builder().profileId(userId).status(RiderStatus.SUSPENDED).build();
    assertThrows(StatusTransitionNotAllowedException.class,
        () -> riderProfileService.updateRiderStatus(riderStatusDto));
  }

  @Test
  void throwExceptionUpdateRiderStatusWhenSuspendingRiderWithoutReasonIsMissing() {
    RiderProfile rider = new RiderProfile();
    rider.setId(userId);
    rider.setStatus(RiderStatus.AUTHORIZED);
    when(riderProfileRepository.findById(userId)).thenReturn(Optional.of(rider));
    RiderStatusDto riderStatusDto =
        RiderStatusDto.builder().profileId(userId).status(RiderStatus.SUSPENDED).build();
    assertThrows(MandatoryFieldMissingException.class,
        () -> riderProfileService.updateRiderStatus(riderStatusDto));
  }

//  @Test
//  void throwExceptionUpdateRiderStatusWhenSuspendingRiderWithReasonOthersAndWithoutRemarks() {
//    RiderProfile rider = new RiderProfile();
//    rider.setId(userId);
//    rider.setStatus(RiderStatus.AUTHORIZED);
//    when(riderProfileRepository.findById(userId)).thenReturn(Optional.of(rider));
//    RiderStatusDto riderStatusDto =
//        RiderStatusDto.builder().profileId(userId).status(RiderStatus.SUSPENDED).reason(Constants.OTHER).build();
//    assertThrows(MandatoryFieldMissingException.class,
//        () -> riderProfileService.updateRiderStatus(riderStatusDto));
//  }


  @Test
  void throwExceptionUpdateRiderStatusWhenNationalIdNotApproved() {
    riderProfile.setStatus(RiderStatus.UNAUTHORIZED);
    when(riderProfileRepository.findById(userId)).thenReturn(Optional.of(riderProfile));
    RiderStatusDto riderStatusDto =
        RiderStatusDto.builder().profileId(userId).status(RiderStatus.AUTHORIZED).build();
    assertThrows(MandatoryChecksMissingException.class,
        () -> riderProfileService.updateRiderStatus(riderStatusDto));
  }

  @Test
  void throwExceptionUpdateRiderStatusWhenSelfieNotApproved() {
    RiderProfile rider = new RiderProfile();
    rider.setId(userId);
    rider.setNationalIdStatus(MandatoryCheckStatus.APPROVED);
    rider.setProfilePhotoStatus(MandatoryCheckStatus.PENDING);
    rider.setStatus(RiderStatus.UNAUTHORIZED);
    when(riderProfileRepository.findById(riderProfileDto.getId())).thenReturn(Optional.of(rider));
    RiderStatusDto riderStatusDto =
        RiderStatusDto.builder().profileId(userId).status(RiderStatus.AUTHORIZED).build();
    assertThrows(MandatoryChecksMissingException.class,
        () -> riderProfileService.updateRiderStatus(riderStatusDto));
  }

  @Test
  void throwExceptionUpdateRiderStatusWhenDrivingLicenseNotApproved() {
    RiderProfile rider = new RiderProfile();
    rider.setId(userId);
    rider.setNationalIdStatus(MandatoryCheckStatus.APPROVED);
    rider.setProfilePhotoStatus(MandatoryCheckStatus.APPROVED);
    rider.setStatus(RiderStatus.UNAUTHORIZED);
    when(riderProfileRepository.findById(userId)).thenReturn(Optional.of(rider));
    when(riderDrivingLicenseDocumentRepository.findByRiderProfileId(userId))
        .thenReturn(Optional.empty());
    RiderStatusDto riderStatusDto =
        RiderStatusDto.builder().profileId(userId).status(RiderStatus.AUTHORIZED).build();
    assertThrows(MandatoryChecksMissingException.class,
        () -> riderProfileService.updateRiderStatus(riderStatusDto));
  }

  @Test
  void throwExceptionUpdateRiderStatusWhenVehicleRegistrationNotApproved() {
    RiderProfile rider = new RiderProfile();
    rider.setId(userId);
    rider.setNationalIdStatus(MandatoryCheckStatus.APPROVED);
    rider.setProfilePhotoStatus(MandatoryCheckStatus.APPROVED);
    rider.setStatus(RiderStatus.UNAUTHORIZED);
    when(riderProfileRepository.findById(riderProfileDto.getId())).thenReturn(Optional.of(rider));
    RiderDrivingLicenseDocument drivingLicense =
        RiderDrivingLicenseDocument.builder().status(MandatoryCheckStatus.APPROVED).build();
    when(riderDrivingLicenseDocumentRepository.findByRiderProfileId(userId))
        .thenReturn(Optional.of(drivingLicense));
    when(vehicleRegistrationRepository.findByRiderProfileId(userId)).thenReturn(Optional.empty());
    RiderStatusDto riderStatusDto =
        RiderStatusDto.builder().profileId(userId).status(RiderStatus.AUTHORIZED).build();
    assertThrows(MandatoryChecksMissingException.class,
        () -> riderProfileService.updateRiderStatus(riderStatusDto));
  }

  @Test
  void throwExceptionUpdateRiderStatusWhenTrainingNotCompleted() {
	RiderProfile rider = new RiderProfile();
	rider.setId(userId);
	rider.setNationalIdStatus(MandatoryCheckStatus.APPROVED);
	rider.setProfilePhotoStatus(MandatoryCheckStatus.APPROVED);
	rider.setStatus(RiderStatus.UNAUTHORIZED);
	when(riderProfileRepository.findById(riderProfileDto.getId())).thenReturn(Optional.of(rider));
	RiderDrivingLicenseDocument drivingLicense = RiderDrivingLicenseDocument.builder()
			.status(MandatoryCheckStatus.APPROVED).build();
	when(riderDrivingLicenseDocumentRepository.findByRiderProfileId(userId))
			.thenReturn(Optional.of(drivingLicense));
	RiderVehicleRegistrationDocument vehicleRegistration = RiderVehicleRegistrationDocument.builder()
			.status(MandatoryCheckStatus.APPROVED).build();
	when(vehicleRegistrationRepository.findByRiderProfileId(userId)).thenReturn(Optional.of(vehicleRegistration));
	when(riderTrainingAppointmentRepository.findByRiderIdAndTrainingType(userId, TrainingType.FOOD)).thenReturn(Optional.empty());
	RiderStatusDto riderStatusDto = RiderStatusDto.builder().profileId(userId).status(RiderStatus.AUTHORIZED)
			.build();
	assertThrows(MandatoryChecksMissingException.class,
			() -> riderProfileService.updateRiderStatus(riderStatusDto));
  }

  @Test
  void shouldUpdateRiderStatusFromUnauthorizedToAuthorizedWithSendingSmsEventAndInitializePocket() {
    RiderProfile rider = new RiderProfile();
    rider.setId(userId);
    rider.setNationalIdStatus(MandatoryCheckStatus.APPROVED);
    rider.setProfilePhotoStatus(MandatoryCheckStatus.APPROVED);
    rider.setStatus(RiderStatus.UNAUTHORIZED);
    when(riderProfileRepository.findById(riderProfileDto.getId())).thenReturn(Optional.of(rider));
    RiderDrivingLicenseDocument drivingLicense =
        RiderDrivingLicenseDocument.builder().status(MandatoryCheckStatus.APPROVED).build();
    when(riderDrivingLicenseDocumentRepository.findByRiderProfileId(userId))
        .thenReturn(Optional.of(drivingLicense));
    RiderVehicleRegistrationDocument vehicleRegistration =
        RiderVehicleRegistrationDocument.builder().status(MandatoryCheckStatus.APPROVED).build();
    when(vehicleRegistrationRepository.findByRiderProfileId(userId))
        .thenReturn(Optional.of(vehicleRegistration));
	RiderSelectedTrainingAppointment riderSelectedTrainingAppointment = RiderSelectedTrainingAppointment.builder()
			.status(RiderTrainingStatus.COMPLETED).build();
    when(riderTrainingAppointmentRepository.findByRiderIdAndTrainingType(userId, TrainingType.FOOD)).thenReturn(Optional.of(riderSelectedTrainingAppointment));
    ResponseEntity rs=new ResponseEntity(HttpStatus.OK);
    when(pocketServiceFeignClient.addRiderDataFromOpsPortal(null)).thenReturn(rs);
    when(propertyUtils.getProperty(eq(SmsConstants.RIDER_AUTHORIZED_MSG), any(Locale.class))).thenReturn(RIDER_AUTHORIZED);
    RiderStatusDto riderStatusDto =
        RiderStatusDto.builder().profileId(userId).status(RiderStatus.AUTHORIZED).build();
    RiderStatusDto result = riderProfileService.updateRiderStatus(riderStatusDto);
    assertEquals(RiderStatus.AUTHORIZED, result.getStatus());
    verify(smsPublisher).sendSmsNotificationEvent(any(RiderProfile.class), eq(RIDER_AUTHORIZED));
  }

  @Test
  void shouldUpdateRiderStatusFromAuthorizedToSuspendedWithSendingSmsEventAndShouldNotInitializePocket() {
    RiderProfile rider = new RiderProfile();
    rider.setId(userId);
    rider.setStatus(RiderStatus.AUTHORIZED);
    rider.setAvailabilityStatus(AvailabilityStatus.Active);
    when(riderProfileRepository.findById(riderProfileDto.getId())).thenReturn(Optional.of(rider));
    when(propertyUtils.getProperty(eq(SmsConstants.RIDER_TEMPORARILY_SUSPENDED_MSG), any(Locale.class))).thenReturn(RIDER_SUSPENDED);
    when(riderSuspendHistoryRepository.save(any(RiderSuspendHistory.class))).thenReturn(riderSuspendHistory);
    RiderStatusDto riderStatusDto = RiderStatusDto.builder().profileId(userId).reason("Hit and run registered")
            .status(RiderStatus.SUSPENDED).suspensionDuration(8).remarks("Dangerous driving").build();
    RiderStatusDto result = riderProfileService.updateRiderStatus(riderStatusDto);
    assertEquals(RiderStatus.SUSPENDED, result.getStatus());
    verify(smsPublisher).sendSmsNotificationEvent(any(RiderProfile.class), eq(RIDER_SUSPENDED));
    verifyZeroInteractions(pocketServiceFeignClient);
  }


  @Test
  void shouldUpdateRiderStatusFromAuthorizedToSuspendedWithReasonAsOthersAndSendingSmsEventAndShouldNotInitializePocket() {
    RiderProfile rider = new RiderProfile();
    rider.setId(userId);
    rider.setStatus(RiderStatus.AUTHORIZED);
    when(riderProfileRepository.findById(riderProfileDto.getId())).thenReturn(Optional.of(rider));
    when(propertyUtils.getProperty(eq(SmsConstants.RIDER_PERMANENT_SUSPENDED_MSG), any(Locale.class))).thenReturn(RIDER_SUSPENDED);
    RiderStatusDto riderStatusDto = RiderStatusDto.builder().profileId(userId).reason("Others")
            .status(RiderStatus.SUSPENDED).remarks("Dangerous driving").build();
    RiderStatusDto result = riderProfileService.updateRiderStatus(riderStatusDto);
    assertEquals(RiderStatus.SUSPENDED, result.getStatus());
    verify(smsPublisher).sendSmsNotificationEvent(any(RiderProfile.class), eq(RIDER_SUSPENDED));
    verifyZeroInteractions(pocketServiceFeignClient);
  }


  @Test
  void shouldUpdateRiderStatusFromSuspendedToAuthorizedWithSendingSmsEventAndShouldNotInitializePocket() {
    RiderProfile rider = new RiderProfile();
    rider.setId(userId);
    rider.setNationalIdStatus(MandatoryCheckStatus.APPROVED);
    rider.setProfilePhotoStatus(MandatoryCheckStatus.APPROVED);
    rider.setStatus(RiderStatus.SUSPENDED);
    when(riderProfileRepository.findById(riderProfileDto.getId())).thenReturn(Optional.of(rider));
    RiderDrivingLicenseDocument drivingLicense =
            RiderDrivingLicenseDocument.builder().status(MandatoryCheckStatus.APPROVED).build();
    when(riderDrivingLicenseDocumentRepository.findByRiderProfileId(userId))
            .thenReturn(Optional.of(drivingLicense));
    RiderVehicleRegistrationDocument vehicleRegistration =
            RiderVehicleRegistrationDocument.builder().status(MandatoryCheckStatus.APPROVED).build();
    when(vehicleRegistrationRepository.findByRiderProfileId(userId))
            .thenReturn(Optional.of(vehicleRegistration));
    RiderSelectedTrainingAppointment riderSelectedTrainingAppointment = RiderSelectedTrainingAppointment.builder()
            .status(RiderTrainingStatus.COMPLETED).build();
    when(riderTrainingAppointmentRepository.findByRiderIdAndTrainingType(userId, TrainingType.FOOD)).thenReturn(Optional.of(riderSelectedTrainingAppointment));
    when(propertyUtils.getProperty(eq(SmsConstants.RIDER_REAUTHORIZED_MSG), any(Locale.class))).thenReturn(RIDER_REAUTHORIZED);
    RiderStatusDto riderStatusDto = RiderStatusDto.builder().profileId(userId).reason("test")
            .status(RiderStatus.AUTHORIZED).build();
    RiderStatusDto result = riderProfileService.updateRiderStatus(riderStatusDto);
    assertEquals(RiderStatus.AUTHORIZED, result.getStatus());
    verify(smsPublisher).sendSmsNotificationEvent(any(RiderProfile.class), eq(RIDER_REAUTHORIZED));
    verifyZeroInteractions(pocketServiceFeignClient);
  }

  @Test
  void throwExceptionUpdateNationalIdStatusWhenRiderNotExists() {
    when(riderProfileRepository.findById(riderProfileDto.getId())).thenReturn(Optional.empty());
    assertThrows(DataNotFoundException.class, () -> riderProfileService
        .updateNationalIdStatus(riderProfileDto.getId(), MandatoryCheckStatus.APPROVED,"Reason","comment", Constants.OPS_MEMBER));
  }


  @Test
  void shouldUpdateNationalIdStatus() {
    RiderProfile rider = new RiderProfile();
    rider.setNationalIdStatus(MandatoryCheckStatus.PENDING);
    when(riderProfileRepository.findById(riderProfileDto.getId())).thenReturn(Optional.of(rider));
    boolean result = riderProfileService.updateNationalIdStatus(riderProfileDto.getId(),
        MandatoryCheckStatus.APPROVED,null,null, Constants.OPS_MEMBER);
    assertTrue(result);
    verify(riderProfileRepository, times(1)).save(any(RiderProfile.class));
  }

  @Test
  void throwExceptionUpdateProfilePhotoStatusWhenRiderNotExists() {
    when(riderProfileRepository.findById(riderProfileDto.getId())).thenReturn(Optional.empty());
    assertThrows(DataNotFoundException.class, () -> riderProfileService
        .updateProfilePhotoStatus(riderProfileDto.getId(), MandatoryCheckStatus.APPROVED,null,null, Constants.OPS_MEMBER));
  }

  @Test
  void shouldUpdateProfilePhotoStatusToRejectedWithSendingSmsEvent() {
    RiderProfile rider = new RiderProfile();
    rider.setProfilePhotoStatus(MandatoryCheckStatus.PENDING);
    when(riderProfileRepository.findById(riderProfileDto.getId())).thenReturn(Optional.of(rider));
    when(propertyUtils.getProperty(eq(SmsConstants.SELFIE_REJECTED_MSG), any(Locale.class))).thenReturn(RIDER_SELFIE_REJECTED);

    boolean result = riderProfileService.updateProfilePhotoStatus(riderProfileDto.getId(),
            MandatoryCheckStatus.REJECTED,"Document not properly centered in photo frame",null, Constants.OPS_MEMBER);
    assertTrue(result);
    verify(riderProfileRepository, times(1)).save(any(RiderProfile.class));
  }

  @Test
  void shouldUpdateProfilePhotoStatus() {
    RiderProfile rider = new RiderProfile();
    rider.setProfilePhotoStatus(MandatoryCheckStatus.PENDING);
    when(riderProfileRepository.findById(riderProfileDto.getId())).thenReturn(Optional.of(rider));
    boolean result = riderProfileService.updateProfilePhotoStatus(riderProfileDto.getId(),
        MandatoryCheckStatus.APPROVED,null,null, Constants.OPS_MEMBER);
    assertTrue(result);
    verify(riderProfileRepository, times(1)).save(any(RiderProfile.class));
  }

  @Test
  void shouldUpdateRiderAddress() {
    when(riderProfileRepository.findById(userId)).thenReturn(Optional.of(riderProfile));
    RiderProfile riderProfile = riderProfileService.updateRiderNationalAddress(userId, nationalAddressUpdateRequestDto);
    verify(riderProfileRepository, times(1)).save(any(RiderProfile.class));
  }

  @Test
  void shouldRejectNationalId() {
    RiderProfile rider = new RiderProfile();
    rider.setNationalIdStatus(MandatoryCheckStatus.REJECTED);
    when(riderProfileRepository.findById(riderProfileDto.getId())).thenReturn(Optional.of(rider));
    boolean result = riderProfileService.updateProfilePhotoStatus(riderProfileDto.getId(),
        MandatoryCheckStatus.REJECTED,"reason","comment", Constants.OPS_MEMBER);
    assertTrue(result);
    verify(riderProfileRepository, times(1)).save(any(RiderProfile.class));
  }

  @Test
  void shouldThrowExceptionUpdateProfilePhotoStatus() {
    RiderProfile rider = new RiderProfile();
    rider.setProfilePhotoStatus(MandatoryCheckStatus.APPROVED);
    when(riderProfileRepository.findById(riderProfileDto.getId())).thenReturn(Optional.of(rider));
    assertThrows(DocumentAlreadyApprovedException.class, () -> riderProfileService
            .updateProfilePhotoStatus(riderProfileDto.getId(), MandatoryCheckStatus.REJECTED,null,null, Constants.OPS_MEMBER));  }



  @Test
  void shouldThrowExceptionUpdateNationalIdStatus() {
    RiderProfile rider = new RiderProfile();
    rider.setNationalIdStatus(MandatoryCheckStatus.APPROVED);
    when(riderProfileRepository.findById(riderProfileDto.getId())).thenReturn(Optional.of(rider));
    assertThrows(DocumentAlreadyApprovedException.class, () -> riderProfileService
            .updateNationalIdStatus(riderProfileDto.getId(), MandatoryCheckStatus.REJECTED,"abc","acb", Constants.OPS_MEMBER));  }

  RiderProfile getRiderForProfileStage(){

	  RiderProfileDto  dto = RiderProfileDto.builder()
	          .id("12345")
	          .phoneNumber("1234667")
	          .build();
	  RiderProfile riderLocal = new RiderProfile();
	  riderLocal.setCreatedDate(LocalDateTime.now());
      BeanUtils.copyProperties(dto, riderLocal);
      return riderLocal;
  }


  @Test
  void returnProfileStageAsAuthorized_WhenRiderStatusIsAuthorised() {

	RiderProfile riderProfile =   getRiderForProfileStage();
    riderProfile.setStatus(RiderStatus.AUTHORIZED);
    riderProfile.setProfileStage(RiderProfileStage.STAGE_1);
    riderProfile.setCreatedDate(LocalDateTime.now());
    when(riderProfileRepository.findByPhoneNumber(riderProfileDto.getPhoneNumber())).thenReturn(Optional.of(riderProfile));
    String profileStage = riderProfileService.getRiderProfileStage(riderProfileDto.getPhoneNumber());
    assertEquals("RIDER_AUTHORIZED", profileStage);
  }

  @Test
  void returnProfileStageAsAuthorized_WhenRiderStatusIsSuspended() {
	  RiderProfile riderProfile =   getRiderForProfileStage();
    riderProfile.setStatus(RiderStatus.SUSPENDED);
    riderProfile.setProfileStage(RiderProfileStage.STAGE_1);
    riderProfile.setCreatedDate(LocalDateTime.now());

    when(riderProfileRepository.findByPhoneNumber(riderProfileDto.getPhoneNumber())).thenReturn(Optional.of(riderProfile));
    String profileStage = riderProfileService.getRiderProfileStage(riderProfileDto.getPhoneNumber());
    assertEquals("RIDER_AUTHORIZED", profileStage);
  }

  @Test
  void returnProfileStageAsRIDER_PROFILE_NOT_CREATED_WhenRiderIsNull() {
	  RiderProfile riderProfile =   getRiderForProfileStage();
    when(riderProfileRepository.findByPhoneNumber(riderProfileDto.getPhoneNumber())).thenReturn(Optional.ofNullable(null));
    String profileStage = riderProfileService.getRiderProfileStage(riderProfileDto.getPhoneNumber());
    assertEquals("RIDER_PROFILE_NOT_CREATED", profileStage);
  }

  @Test
  void testProfileStageWhenRiderStatusIsNull() {
	  RiderProfile riderProfile =   getRiderForProfileStage();
    riderProfile.setStatus(null);
    when(riderProfileRepository.findByPhoneNumber(riderProfileDto.getPhoneNumber())).thenReturn(Optional.of(riderProfile));
    String profileStage = riderProfileService.getRiderProfileStage(riderProfileDto.getPhoneNumber());
    assertEquals("RIDER_PROFILE_STATUS_NOT_PRESENT", profileStage);
  }

  @Test
  void testProfileStageWhenRiderProfileStageIsNull() {
	  RiderProfile riderProfile =   getRiderForProfileStage();
    riderProfile.setStatus(RiderStatus.UNAUTHORIZED);

    when(riderProfileRepository.findByPhoneNumber(riderProfileDto.getPhoneNumber())).thenReturn(Optional.of(riderProfile));
    String profileStage = riderProfileService.getRiderProfileStage(riderProfileDto.getPhoneNumber());
    assertEquals("RIDER_PROFILE_STAGE_NOT_PRESENT", profileStage);
  }

  @Test
  void testProfileStageWhenTrainingDateExceededTheDeadlineDate() {
	  RiderProfile riderProfile =   getRiderForProfileStage();
    riderProfile.setStatus(RiderStatus.UNAUTHORIZED);
    riderProfile.setCreatedDate(LocalDateTime.now().minus(3L ,ChronoUnit.WEEKS));
    riderProfile.setProfileStage(RiderProfileStage.STAGE_2);

    when(operationFeignClient.getConfigData(TRAINING_CONFIGURABLE_WEEKS)).thenReturn(configDataResponse);
    when(riderProfileRepository.findByPhoneNumber(riderProfileDto.getPhoneNumber())).thenReturn(Optional.of(riderProfile));
    String profileStage = riderProfileService.getRiderProfileStage(riderProfileDto.getPhoneNumber());
    assertEquals("RIDER_PROFILE_IN_DEACTIVE_STATUS", profileStage);
  }

  @Test
  void testProfileStage1() {
	  RiderProfile riderProfile =   getRiderForProfileStage();
    riderProfile.setStatus(RiderStatus.UNAUTHORIZED);
    riderProfile.setCreatedDate(LocalDateTime.now().plus(20L ,ChronoUnit.DAYS));
    riderProfile.setProfileStage(RiderProfileStage.STAGE_1);

    when(operationFeignClient.getConfigData(TRAINING_CONFIGURABLE_WEEKS)).thenReturn(configDataResponse);
    when(riderProfileRepository.findByPhoneNumber(riderProfileDto.getPhoneNumber())).thenReturn(Optional.of(riderProfile));
    String profileStage = riderProfileService.getRiderProfileStage(riderProfileDto.getPhoneNumber());
    assertEquals("RIDER_PROFILE_CREATED", profileStage);
  }

  @Test
  void testProfileStage2() {
	  RiderProfile riderProfile =   getRiderForProfileStage();
    riderProfile.setStatus(RiderStatus.UNAUTHORIZED);
    riderProfile.setCreatedDate(LocalDateTime.now().plus(20L ,ChronoUnit.DAYS));
    riderProfile.setProfileStage(RiderProfileStage.STAGE_2);

    when(operationFeignClient.getConfigData(TRAINING_CONFIGURABLE_WEEKS)).thenReturn(configDataResponse);
    when(riderProfileRepository.findByPhoneNumber(riderProfileDto.getPhoneNumber())).thenReturn(Optional.of(riderProfile));
    String profileStage = riderProfileService.getRiderProfileStage(riderProfileDto.getPhoneNumber());
    assertEquals("RIDER_MANDATORY_DOCS_UPLOADED", profileStage);
  }

  @Test
  void testProfileStage3() {
	  RiderProfile riderProfile =   getRiderForProfileStage();
    riderProfile.setStatus(RiderStatus.UNAUTHORIZED);
    riderProfile.setCreatedDate(LocalDateTime.now().plus(20L ,ChronoUnit.DAYS));
    riderProfile.setProfileStage(RiderProfileStage.STAGE_3);

    when(operationFeignClient.getConfigData(TRAINING_CONFIGURABLE_WEEKS)).thenReturn(configDataResponse);
    when(riderProfileRepository.findByPhoneNumber(riderProfileDto.getPhoneNumber())).thenReturn(Optional.of(riderProfile));
    String profileStage = riderProfileService.getRiderProfileStage(riderProfileDto.getPhoneNumber());
    assertEquals("RIDER_ALL_PROCESS_COMPLETED_BUT_STILL_UNAUTHORIZED", profileStage);
  }

  @Test
  void shouldGetRiderIds() {
    List<RiderProfile> riderProfiles = new ArrayList<>();
    riderProfiles.add(riderProfile);
    List<String> riderIds = new ArrayList<>();
    riderIds.add("RR10073");
    riderIds.add("RR10074");
    when(riderProfileRepository.findByRiderIdIn(riderIds)).thenReturn(riderProfiles);
    List<RiderProfile> fetchedProfiles = riderProfileService.getRiferProfilesByRiderIds(riderIds);
    assertTrue(ObjectUtils.isNotEmpty(fetchedProfiles));
  }

  @Test
  void processKafkaTopicTest() throws JsonProcessingException {
    when(riderProfileRepository.save(riderProfile)).thenReturn(riderProfile);
    when(riderProfileRepository.findByRiderId(anyString())).thenReturn(Optional.of(riderProfile));

    String message = "{\n" +
            "   \"riderId\":\"av3d\",\n" +
            "   \"tierName\":\"Hero\"\n" +
            "}";
    RiderProfileTierDto riderTierProfile = RiderProfileTierDto.builder()
            .riderId(userId)
            .tierName("Hero")
            .build();
    when(mapper.readValue(message, RiderProfileTierDto.class)).thenReturn(riderTierProfile);
    riderProfileService.processKafkaTopic(message);
    verify(riderProfileRepository, times(1)).save(any(RiderProfile.class));
  }

  @Test
  void shouldThrowExceptionProcessKafkaTopicTest() throws JsonProcessingException {

    String message = "{\n" +
            "   \"riderId\":\"av3d\",\n" +
            "   \"tierName\":\"Hero\"\n" +
            "}";
    RiderProfileTierDto riderTierProfile = RiderProfileTierDto.builder()
            .riderId(userId)
            .tierName("Hero")
            .build();
    when(mapper.readValue(message, RiderProfileTierDto.class)).thenReturn(riderTierProfile);
    when(riderProfileRepository.findByRiderId(anyString())).thenReturn(Optional.empty());
    assertThrows(DataNotFoundException.class, () -> riderProfileService.processKafkaTopic(message));
    }


	@Test
	void test_GetRiderShortProfile_ViaRiderId() {
		when(riderProfileRepository.findByRiderId(Mockito.anyString())).thenReturn(Optional.of(riderProfile));
		RiderShortProfile riderShortProfile = riderProfileService.getRiderShortProfile(Constants.RIDER_ID, "RR10001");
		assertEquals(Constants.CODE_100, riderShortProfile.getCode());
		assertEquals("RR123", riderShortProfile.getRiderInfo().getRiderId());
		assertEquals("9999999999", riderShortProfile.getRiderInfo().getPhoneNumber());
		assertEquals("1234567890", riderShortProfile.getRiderInfo().getNationalID());

	}

	@Test
	void test_GetRiderShortProfile_ViaPhoneNumber() {
        RiderProfile profile = new RiderProfile();
        BeanUtils.copyProperties(riderProfile, profile);
        profile.setPhoneNumber("9999999999");
		when(riderProfileRepository.findByPhoneNumber(Mockito.anyString())).thenReturn(Optional.of(profile));
		RiderShortProfile riderShortProfile = riderProfileService.getRiderShortProfile(Constants.PHONE_NUMBER, "9999999999");
		assertEquals(Constants.CODE_100, riderShortProfile.getCode());
		assertEquals("RR123", riderShortProfile.getRiderInfo().getRiderId());
		assertEquals("9999999999", riderShortProfile.getRiderInfo().getPhoneNumber());
		assertEquals("1234567890", riderShortProfile.getRiderInfo().getNationalID());

	}

	@Test
	void test_GetRiderShortProfile_ViaNationalId() {
		when(riderProfileRepository.findByNationalID(Mockito.anyString())).thenReturn(Optional.of(riderProfile));
		RiderShortProfile riderShortProfile = riderProfileService.getRiderShortProfile(Constants.NATIONAL_ID, "1234567890");
		assertEquals(Constants.CODE_100, riderShortProfile.getCode());
		assertEquals("RR123", riderShortProfile.getRiderInfo().getRiderId());
		assertEquals("9999999999", riderShortProfile.getRiderInfo().getPhoneNumber());
		assertEquals("1234567890", riderShortProfile.getRiderInfo().getNationalID());

	}


		@Test
		void test_GetRiderShortProfile_ViaRiderId_NotFOund() {
			when(riderProfileRepository.findByRiderId(Mockito.anyString())).thenReturn(Optional.empty());
			RiderShortProfile riderShortProfile = riderProfileService.getRiderShortProfile(Constants.RIDER_ID, "RR10001");
			assertEquals(Constants.ERROR_CODE_102, riderShortProfile.getCode());
		}

		@Test
		void test_GetRiderShortProfile_ViaPhoneNumber_NotFOund() {
			when(riderProfileRepository.findByPhoneNumber(Mockito.anyString())).thenReturn(Optional.empty());
			RiderShortProfile riderShortProfile = riderProfileService.getRiderShortProfile(Constants.PHONE_NUMBER, "9999999999");
			assertEquals(Constants.ERROR_CODE_102, riderShortProfile.getCode());
		}

		@Test
		void test_GetRiderShortProfile_ViaNationalId_NotFOund() {
			when(riderProfileRepository.findByNationalID(Mockito.anyString())).thenReturn(Optional.empty());
			RiderShortProfile riderShortProfile = riderProfileService.getRiderShortProfile(Constants.NATIONAL_ID, "1234567890");
			assertEquals(Constants.ERROR_CODE_102, riderShortProfile.getCode());
		}

		@Test
		void test_FailedGetRiderShortProfile_ViaInvalidKey() {
			RiderShortProfile riderShortProfile = riderProfileService.getRiderShortProfile("invalidKey", "1234567890");
			assertEquals(Constants.ERROR_CODE_101, riderShortProfile.getCode());
		}

		@Test
		void test_FailedGetRiderShortProfile_DueToGeneralException() {
			Mockito.lenient().when(riderProfileRepository.findByNationalID(Mockito.anyString())).thenThrow(DataNotFoundException.class);
			RiderShortProfile riderShortProfile = riderProfileService.getRiderShortProfile(Constants.NATIONAL_ID, "1234567890");
			assertEquals(Constants.ERROR_CODE_103, riderShortProfile.getCode());
		}

        @Test
        void test_updateRentingTodayFlagTrue() {
          RentingTodayRequest rentingTodayRequest = getRentingTodayRequest();
          rentingTodayRequest.setRentingToday(true);
          Mockito.when(riderProfileRepository.findByRiderIdInAndStatus(Mockito.anyList(), Mockito.any())).thenReturn(getAuthorizedEvRider());
          Mockito.when(riderProfileRepository.saveAll(Mockito.any())).thenReturn(getAuthorizedEvRider());
          List<RiderProfile> response = riderProfileService.updateRentingTodayFlag(rentingTodayRequest);
          Assert.assertNotNull(response);
          Assert.assertEquals("RR9999", response.get(0).getRiderId());
        }

        @Test
        void test_updateRentingTodayFlagFalse() {
          RentingTodayRequest rentingTodayRequest = getRentingTodayRequest();
          rentingTodayRequest.setRentingToday(false);
          Mockito.when(riderProfileRepository.findByRiderIdIn(Mockito.anyList())).thenReturn(getAuthorizedEvRider());
          Mockito.when(riderProfileRepository.saveAll(Mockito.any())).thenReturn(getAuthorizedEvRider());
          List<RiderProfile> response = riderProfileService.updateRentingTodayFlag(rentingTodayRequest);
          Assert.assertNotNull(response);
          Assert.assertEquals("RR9999", response.get(0).getRiderId());
        }

        @Test
        void test_updateRentingTodayFlagAsFalse() {
          riderProfileService.updateRentingTodayAsFalse();
          Mockito.verify(mongoTemplate, times(1)).updateMulti(Mockito.any(), Mockito.any(), eq(RiderProfile.class));
        }

        @Test
        void test_evRiders() {
          riderProfileService.evRidersList(LocalDateTime.now().minusDays(1), LocalDateTime.now());
          Mockito.verify(mongoTemplate, times(1)).find(Mockito.any(), Mockito.any());
        }

  @Test
  void getAllRiderProfileTest(){
    when(riderProfileRepository.findAllByStatusIn(any(), any(Pageable.class))).thenReturn(getRiderProfileDetails());
    PaginatedRiderDetailsList response = riderProfileService.getAllRiderProfile(null, 0,100);
    assertNotNull(response);
    assertNotNull(response.getRiders());
    assertEquals(1, response.getRiders().size());
  }

  @Test
  void getAllRiderProfileByZoneIdTest(){
	  List<RiderStatus> riderStatus = new ArrayList<>();
		riderStatus.add(RiderStatus.AUTHORIZED);
		riderStatus.add(RiderStatus.SUSPENDED);
    when(riderProfileRepository.findAllByRiderPreferredZones_PreferredZoneIdAndStatusIn(anyString(),anyList(), any(Pageable.class))).thenReturn(getRiderProfileDetails());
    PaginatedRiderDetailsList response = riderProfileService.getAllRiderProfile("1", 0,100);
    assertNotNull(response);
    assertNotNull(response.getRiders());
    assertEquals(1, response.getRiders().size());
  }
  
  @Test
  void documentUploadedFlagTest() {
    when(riderProfileRepository.findById(profileUpdateRequestDto.getId())).thenReturn(Optional.of(riderProfile));
    when(riderProfileRepository.save(any(RiderProfile.class))).thenReturn(riderProfile);
    riderProfileService.documentUploadedFlag(userId, DocumentType.PROFILE_PHOTO);
  }
  
  @Test
  void allDocumentUploadedFlagTest() {
    Map<DocumentType, Boolean> documentUploadFlag = new HashMap<>();
    documentUploadFlag.put(DocumentType.BACKGROUND_VERIFICATION_FORM, true);
    documentUploadFlag.put(DocumentType.DRIVER_LICENSE, true);
    documentUploadFlag.put(DocumentType.VEHICLE_REGISTRATION, true);
    documentUploadFlag.put(DocumentType.VEHICLE_WITH_FOOD_CARD, true);
    riderProfile.setRiderDocumentUpload(RiderDocumentUpload.builder().documentUploadedFlag(documentUploadFlag).build());
    when(riderProfileRepository.findById(profileUpdateRequestDto.getId())).thenReturn(Optional.of(riderProfile));
    when(riderProfileRepository.save(any(RiderProfile.class))).thenReturn(riderProfile);
    riderProfileService.documentUploadedFlag(userId, DocumentType.PROFILE_PHOTO);
  }

  @Test
  void allDocumentUpdateForNonEVRiders() {
    Map<DocumentType, Boolean> documentUploadFlag = new HashMap<>();
    documentUploadFlag.put(DocumentType.DRIVER_LICENSE, true);
    documentUploadFlag.put(DocumentType.VEHICLE_REGISTRATION, true);
    documentUploadFlag.put(DocumentType.VEHICLE_WITH_FOOD_CARD, true);
    riderProfile.setRiderDocumentUpload(RiderDocumentUpload.builder().documentUploadedFlag(documentUploadFlag).build());
    when(riderProfileRepository.findById(profileUpdateRequestDto.getId())).thenReturn(Optional.of(riderProfile));
    when(riderProfileRepository.save(any(RiderProfile.class))).thenReturn(riderProfile);
    riderProfileService.documentUploadedFlag(userId, DocumentType.PROFILE_PHOTO);
  }

  @Test
  void allDocumentUpdateForEVRiders() {
    Map<DocumentType, Boolean> documentUploadFlag = new HashMap<>();
    documentUploadFlag.put(DocumentType.DRIVER_LICENSE, true);
    documentUploadFlag.put(DocumentType.EV_FORM, true);
    riderProfile.setEvBikeUser(true);
    riderProfile.setRiderDocumentUpload(RiderDocumentUpload.builder().documentUploadedFlag(documentUploadFlag).build());
    when(riderProfileRepository.findById(profileUpdateRequestDto.getId())).thenReturn(Optional.of(riderProfile));
    when(riderProfileRepository.save(any(RiderProfile.class))).thenReturn(riderProfile);
    riderProfileService.documentUploadedFlag(userId, DocumentType.PROFILE_PHOTO);
  }

  @Test
  void saveToRedis(){
	
    when(riderProfileRepository.findAll(any(Pageable.class))).thenReturn(getRiderProfileDetails());
    riderProfileService.publishToKafka();
  }
  
  @Test
  void riderProfileChangePhoneNumberTest() {
    RiderProfile riderProfile = new RiderProfile();
    BeanUtils.copyProperties(riderProfileDto, riderProfile);
    riderProfile.setStatus(RiderStatus.AUTHORIZED);
    when(riderProfileRepository.findById(any())).thenReturn(Optional.of(riderProfile));
 //   when(riderProfileRepository.findByPhoneNumber(any())).thenReturn(null);
    AdminDeleteUserResult adminDeleteUserResult = Mockito.mock(AdminDeleteUserResult.class);
    when(awsCognitoService.deleteUserByPhoneNumber(any())).thenReturn(adminDeleteUserResult);
    RiderProfileUpdateRequestDto riderProfileUpdateRequestDto = RiderProfileUpdateRequestDto.builder()
            .phoneNumber("1245354")
            .consentAcceptFlag(Boolean.TRUE)
            .evBikeUser(Boolean.TRUE)
            .rentingToday(Boolean.TRUE)
            .build();
    riderProfileService.updateRiderPhoneNumber(riderProfileUpdateRequestDto);
  }

  private Page<RiderProfile> getRiderProfileDetails() {

      List<RiderProfile> list = new ArrayList<>();
      list.add(riderProfile);
      return new PageImpl<>(list);
  }
        private RentingTodayRequest getRentingTodayRequest() {
          List<String> riderIds = new ArrayList<>();
          riderIds.add("RR8888");
          return  RentingTodayRequest.builder().riders(riderIds).build();
        }

        private List<RiderProfile> getAuthorizedEvRider() {
          List<RiderProfile> riderProfiles = new ArrayList<>();
          RiderProfileDto profileDto = RiderProfileDto.builder()
                  .id(userId)
                  .accountNumber("121212121212121")
                  .consentAcceptFlag(true)
                  .dataSharedFlag(true)
                  .firstName("Rohit").lastName("Sharma")
                  .riderId("RR9999")
                  .nationalID("1234567890")
                  .dob("20/12/1988")
                  .phoneNumber("9999999999")
                  .status(RiderStatus.UNAUTHORIZED)
                  .nationalIdStatus(MandatoryCheckStatus.PENDING)
                  .availabilityStatus(AvailabilityStatus.Inactive)
                  .evBikeUser(true)
                  .build();
          RiderProfile rider = new RiderProfile();
          BeanUtils.copyProperties(profileDto, rider);
          riderProfiles.add(rider);
          return riderProfiles;
        }
}
