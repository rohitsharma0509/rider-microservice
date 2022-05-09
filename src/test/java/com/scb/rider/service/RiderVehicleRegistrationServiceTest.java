package com.scb.rider.service;

import com.scb.rider.client.LocationServiceFeignClient;
import com.scb.rider.constants.Constants;
import com.scb.rider.constants.DocumentType;
import com.scb.rider.constants.ErrorConstants;
import com.scb.rider.constants.SmsConstants;
import com.scb.rider.exception.DataNotFoundException;
import com.scb.rider.exception.DocumentAlreadyApprovedException;
import com.scb.rider.exception.RiderAlreadyExistsException;
import com.scb.rider.kafka.SmsPublisher;
import com.scb.rider.model.RiderFoodBoxSize;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.document.RiderUploadedDocument;
import com.scb.rider.model.document.RiderVehicleRegistrationDocument;
import com.scb.rider.model.dto.FoodCartUpdateRequest;
import com.scb.rider.model.dto.RiderVehicleRegistrationDetailsRequest;
import com.scb.rider.model.dto.RiderVehicleRegistrationDetailsResponse;
import com.scb.rider.model.dto.RiderVehicleStatusRequest;
import com.scb.rider.model.enumeration.FoodBoxSize;
import com.scb.rider.model.enumeration.MandatoryCheckStatus;
import com.scb.rider.repository.RiderProfileRepository;
import com.scb.rider.repository.RiderUploadedDocumentRepository;
import com.scb.rider.repository.RiderVehicleRegistrationRepository;
import com.scb.rider.service.document.RiderVehicleRegistrationService;
import com.scb.rider.util.PropertyUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RiderVehicleRegistrationServiceTest {

    private static final String ID = "1";
    private static final String PROFILE_ID = "123";
    private static final String REGISTRATION_NO = "12345";
    private static final String NEW_REGISTRATION_NO = "123456";
    private static final String REGISTRATION_CARD_ID = "71218";
    private static final int INVOKED_ONCE = 1;
    private static final String TEST_SMS = "Test Sms";
    private static final String PROVINCE = "Bangkok";

    @Mock
    private RiderVehicleRegistrationRepository vehicleRegistrationRepository;

    @Mock
    private RiderProfileRepository riderProfileRepository;

    @Mock
    private LocationServiceFeignClient locationServiceFeignClient;

    @Mock
    private PropertyUtils propertyUtils;

    @Mock
    private SmsPublisher smsPublisher;

    @Mock
    private RiderUploadedDocumentRepository riderUploadedDocumentRepository;

    @InjectMocks
    private RiderVehicleRegistrationService service;

    @Test
    void shouldCreateRiderVehicleRegistrationDetails() {
        RiderVehicleRegistrationDetailsRequest request = getRiderVehicleRegistrationDetailsRequest(REGISTRATION_NO, MandatoryCheckStatus.PENDING, MandatoryCheckStatus.PENDING);
        RiderVehicleRegistrationDocument document = getRiderVehicleRegistrationDocument(MandatoryCheckStatus.PENDING, MandatoryCheckStatus.PENDING);
        RiderUploadedDocument riderUploadedDocument = RiderUploadedDocument.builder()
                .imageUrl("foodCardUrl").riderProfileId(PROFILE_ID).build();
        when(vehicleRegistrationRepository.findByRiderProfileId(eq(PROFILE_ID))).thenReturn(Optional.empty());
        when(vehicleRegistrationRepository.findByRegistrationNoAndProvince(eq(REGISTRATION_NO),any())).thenReturn(null);
        when(riderUploadedDocumentRepository.findByRiderProfileIdAndDocumentType(eq(PROFILE_ID), eq(DocumentType.VEHICLE_WITH_FOOD_CARD))).thenReturn(Optional.of(riderUploadedDocument));
        when(vehicleRegistrationRepository.save(any(RiderVehicleRegistrationDocument.class))).thenReturn(document);
        RiderVehicleRegistrationDetailsResponse response = service.createRiderVehicleRegistrationDetails(PROFILE_ID, request);
        assertEquals(PROFILE_ID, response.getRiderProfileId());
        assertEquals(MandatoryCheckStatus.PENDING, response.getStatus());
        assertEquals(MandatoryCheckStatus.PENDING, response.getFoodCardStatus());
    }

    @Test
    void shouldUpdateVehicleRegistrationDocumentWhenReceivedCreateRequestForExistingRecord() {
        RiderVehicleRegistrationDetailsRequest request = getRiderVehicleRegistrationDetailsRequest(REGISTRATION_NO, MandatoryCheckStatus.PENDING, MandatoryCheckStatus.PENDING);
        RiderVehicleRegistrationDocument document = getRiderVehicleRegistrationDocument(MandatoryCheckStatus.PENDING, MandatoryCheckStatus.PENDING);
        when(vehicleRegistrationRepository.findByRiderProfileId(eq(PROFILE_ID))).thenReturn(Optional.of(document));
        when(riderProfileRepository.findById(eq(PROFILE_ID))).thenReturn(Optional.of(getRiderProfile()));
        when(vehicleRegistrationRepository.save(any(RiderVehicleRegistrationDocument.class))).thenReturn(document);
        RiderVehicleRegistrationDetailsResponse response = service.createRiderVehicleRegistrationDetails(PROFILE_ID, request);
        assertEquals(PROFILE_ID, response.getRiderProfileId());
        assertEquals(MandatoryCheckStatus.PENDING, response.getStatus());
        assertEquals(MandatoryCheckStatus.PENDING, response.getFoodCardStatus());
    }

    @Test
    void createRiderVehicleRegistrationDetailsThrowExceptionWhenRegistrationNoIsAlreadyExist() {
        RiderVehicleRegistrationDetailsRequest request = getRiderVehicleRegistrationDetailsRequest(NEW_REGISTRATION_NO, MandatoryCheckStatus.PENDING, MandatoryCheckStatus.PENDING);
        RiderVehicleRegistrationDocument document = getRiderVehicleRegistrationDocument(MandatoryCheckStatus.PENDING, MandatoryCheckStatus.PENDING);
        when(vehicleRegistrationRepository.findByRiderProfileId(eq(PROFILE_ID))).thenReturn(Optional.empty());
        when(vehicleRegistrationRepository.findByRegistrationNoAndProvince(eq(NEW_REGISTRATION_NO),any())).thenReturn(document);
        assertThrows(RiderAlreadyExistsException.class, () -> service.createRiderVehicleRegistrationDetails(PROFILE_ID, request));
    }

    @Test
    public void shouldFindRiderVehicleRegistrationDetailsByProfileId() {
        RiderVehicleRegistrationDocument mappedDocument = getRiderVehicleRegistrationDocument(MandatoryCheckStatus.PENDING, MandatoryCheckStatus.PENDING);
        when(vehicleRegistrationRepository.findByRiderProfileId(eq(PROFILE_ID))).thenReturn(Optional.of(mappedDocument));
        Optional<RiderVehicleRegistrationDocument> response = service.findRiderVehicleRegistrationDetailsByProfileId(PROFILE_ID);
        assertEquals(PROFILE_ID, response.get().getRiderProfileId());
        assertEquals(MandatoryCheckStatus.PENDING, response.get().getStatus());
        assertEquals(MandatoryCheckStatus.PENDING, response.get().getFoodCardStatus());
    }

    @Test
    void updateRiderVehicleRegistrationDetailsThrowExceptionWhenRiderNotExist() {
        RiderVehicleRegistrationDetailsRequest request = getRiderVehicleRegistrationDetailsRequest(REGISTRATION_NO, MandatoryCheckStatus.PENDING, MandatoryCheckStatus.PENDING);
        RiderVehicleRegistrationDocument mappedDocument = getRiderVehicleRegistrationDocument(MandatoryCheckStatus.PENDING, MandatoryCheckStatus.PENDING);
        when(riderProfileRepository.findById(eq(PROFILE_ID))).thenReturn(Optional.empty());
        assertThrows(DataNotFoundException.class, () -> service.updateRiderVehicleRegistrationDetails(request, mappedDocument));
    }

    @Test
    void updateRiderVehicleRegistrationDetailsThrowExceptionWhenRegistrationNoIsAlreadyExist() {
        RiderVehicleRegistrationDetailsRequest request = getRiderVehicleRegistrationDetailsRequest(NEW_REGISTRATION_NO, MandatoryCheckStatus.PENDING, MandatoryCheckStatus.PENDING);
        RiderVehicleRegistrationDocument mappedDocument = getRiderVehicleRegistrationDocument(MandatoryCheckStatus.PENDING, MandatoryCheckStatus.PENDING);
        when(riderProfileRepository.findById(eq(PROFILE_ID))).thenReturn(Optional.of(getRiderProfile()));
        when(vehicleRegistrationRepository.findByRegistrationNoAndProvince(eq(NEW_REGISTRATION_NO),any())).thenReturn(mappedDocument);
        when(propertyUtils.getProperty(eq(ErrorConstants.RIDER_VEHICLE_REG_NUMBER_ALREADY_EXIST))).thenReturn("already exist");
        assertThrows(RiderAlreadyExistsException.class, () -> service.updateRiderVehicleRegistrationDetails(request, mappedDocument));
    }

    @Test
    void shouldThrowExceptionWhenTryToRejectVehicleRegistrationWhileItsAlreadyApproved() {
        RiderVehicleRegistrationDetailsRequest request = getRiderVehicleRegistrationDetailsRequest(REGISTRATION_NO, MandatoryCheckStatus.REJECTED, MandatoryCheckStatus.REJECTED);
        RiderVehicleRegistrationDocument mappedDocument = getRiderVehicleRegistrationDocument(MandatoryCheckStatus.APPROVED, MandatoryCheckStatus.APPROVED);
        when(riderProfileRepository.findById(eq(PROFILE_ID))).thenReturn(Optional.of(getRiderProfile()));
        assertThrows(DocumentAlreadyApprovedException.class, () -> service.updateRiderVehicleRegistrationDetails(request, mappedDocument));
    }

    @Test
    void shouldThrowExceptionWhenTryToRejectFoodCardWhileItsAlreadyApproved() {
        RiderVehicleRegistrationDetailsRequest request = getRiderVehicleRegistrationDetailsRequest(REGISTRATION_NO, MandatoryCheckStatus.REJECTED, MandatoryCheckStatus.REJECTED);
        RiderVehicleRegistrationDocument mappedDocument = getRiderVehicleRegistrationDocument(MandatoryCheckStatus.REJECTED, MandatoryCheckStatus.APPROVED);
        when(riderProfileRepository.findById(eq(PROFILE_ID))).thenReturn(Optional.of(getRiderProfile()));
        assertThrows(DocumentAlreadyApprovedException.class, () -> service.updateRiderVehicleRegistrationDetails(request, mappedDocument));
    }

    @Test
    public void updateRiderVehicleRegistrationDetailsAndShouldSendVehicleRegistrationRejectionSmsEvent() {
        RiderVehicleRegistrationDetailsRequest request = getRiderVehicleRegistrationDetailsRequest(REGISTRATION_NO, MandatoryCheckStatus.REJECTED, MandatoryCheckStatus.PENDING);
        RiderVehicleRegistrationDocument existingDocument = getRiderVehicleRegistrationDocument(MandatoryCheckStatus.PENDING, MandatoryCheckStatus.PENDING);
        RiderVehicleRegistrationDocument updatedDocument = getRiderVehicleRegistrationDocument(MandatoryCheckStatus.REJECTED, MandatoryCheckStatus.PENDING);
        when(riderProfileRepository.findById(eq(PROFILE_ID))).thenReturn(Optional.of(getRiderProfile()));
        when(propertyUtils.getProperty(eq(SmsConstants.VEHICLE_REGISTRATION_REJECTED_MSG), any(Locale.class))).thenReturn(TEST_SMS);
        when(vehicleRegistrationRepository.save(any(RiderVehicleRegistrationDocument.class))).thenReturn(updatedDocument);
        RiderVehicleRegistrationDocument response = service.updateRiderVehicleRegistrationDetails(request, existingDocument);
        assertEquals(PROFILE_ID, response.getRiderProfileId());
        assertEquals(MandatoryCheckStatus.REJECTED, response.getStatus());
        assertEquals(MandatoryCheckStatus.PENDING, response.getFoodCardStatus());
        verify(smsPublisher, times(INVOKED_ONCE)).sendSmsNotificationEvent(any(RiderProfile.class), eq(TEST_SMS));
    }

    @Test
    public void updateRiderVehicleRegistrationDetailsAndShouldSendFoodCardRejectionSmsEvent() {
        RiderVehicleRegistrationDetailsRequest request = getRiderVehicleRegistrationDetailsRequest(REGISTRATION_NO, MandatoryCheckStatus.PENDING, MandatoryCheckStatus.REJECTED);
        RiderVehicleRegistrationDocument existingDocument = getRiderVehicleRegistrationDocument(MandatoryCheckStatus.PENDING, MandatoryCheckStatus.PENDING);
        RiderVehicleRegistrationDocument updatedDocument = getRiderVehicleRegistrationDocument(MandatoryCheckStatus.PENDING, MandatoryCheckStatus.REJECTED);
        when(riderProfileRepository.findById(eq(PROFILE_ID))).thenReturn(Optional.of(getRiderProfile()));
        when(propertyUtils.getProperty(eq(SmsConstants.FOOD_CART_REJECTED_MSG), any(Locale.class))).thenReturn(TEST_SMS);
        when(vehicleRegistrationRepository.save(any(RiderVehicleRegistrationDocument.class))).thenReturn(updatedDocument);
        RiderVehicleRegistrationDocument response = service.updateRiderVehicleRegistrationDetails(request, existingDocument);
        assertEquals(PROFILE_ID, response.getRiderProfileId());
        assertEquals(MandatoryCheckStatus.PENDING, response.getStatus());
        assertEquals(MandatoryCheckStatus.REJECTED, response.getFoodCardStatus());
        verify(smsPublisher, times(INVOKED_ONCE)).sendSmsNotificationEvent(any(RiderProfile.class), eq(TEST_SMS));
    }

    @Test
    void updateVehicleRegistrationStatusThrowExceptionWhenRiderNotExist() {
        RiderVehicleStatusRequest request = RiderVehicleStatusRequest.builder().build();
        RiderVehicleRegistrationDocument mappedDocument = getRiderVehicleRegistrationDocument(MandatoryCheckStatus.PENDING, MandatoryCheckStatus.PENDING);
        when(riderProfileRepository.findById(eq(PROFILE_ID))).thenReturn(Optional.empty());
        assertThrows(DataNotFoundException.class, () -> service.updateVehicleRegistrationStatus(request, mappedDocument));
    }

    @Test
    void shouldUpdateVehicleRegistrationStatusAndSendVehicleRegistrationRejectionSmsEvent() {
        RiderVehicleStatusRequest request = RiderVehicleStatusRequest.builder()
                .status(MandatoryCheckStatus.REJECTED).documentType(DocumentType.VEHICLE_REGISTRATION).build();
        RiderVehicleRegistrationDocument existingDocument = getRiderVehicleRegistrationDocument(MandatoryCheckStatus.PENDING, MandatoryCheckStatus.PENDING);
        RiderVehicleRegistrationDocument updatedDocument = getRiderVehicleRegistrationDocument(MandatoryCheckStatus.REJECTED, MandatoryCheckStatus.PENDING);
        when(riderProfileRepository.findById(eq(PROFILE_ID))).thenReturn(Optional.of(getRiderProfile()));
        when(vehicleRegistrationRepository.save(any(RiderVehicleRegistrationDocument.class))).thenReturn(updatedDocument);
        when(propertyUtils.getProperty(eq(SmsConstants.VEHICLE_REGISTRATION_REJECTED_MSG), any(Locale.class))).thenReturn(TEST_SMS);
        RiderVehicleRegistrationDocument response = service.updateVehicleRegistrationStatus(request, existingDocument);
        assertEquals(PROFILE_ID, response.getRiderProfileId());
        assertEquals(MandatoryCheckStatus.REJECTED, response.getStatus());
        assertEquals(MandatoryCheckStatus.PENDING, response.getFoodCardStatus());
        verify(smsPublisher, times(INVOKED_ONCE)).sendSmsNotificationEvent(any(RiderProfile.class), eq(TEST_SMS));
    }

    @Test
    void shouldUpdateVehicleFoodCardStatusAndSendFoodCardRejectionSmsEvent() {
        RiderVehicleStatusRequest request = RiderVehicleStatusRequest.builder()
                .status(MandatoryCheckStatus.REJECTED).documentType(DocumentType.VEHICLE_WITH_FOOD_CARD).build();
        RiderVehicleRegistrationDocument existingDocument = getRiderVehicleRegistrationDocument(MandatoryCheckStatus.PENDING, MandatoryCheckStatus.PENDING);
        RiderVehicleRegistrationDocument updatedDocument = getRiderVehicleRegistrationDocument(MandatoryCheckStatus.PENDING, MandatoryCheckStatus.REJECTED);
        when(riderProfileRepository.findById(eq(PROFILE_ID))).thenReturn(Optional.of(getRiderProfile()));
        when(vehicleRegistrationRepository.save(any(RiderVehicleRegistrationDocument.class))).thenReturn(updatedDocument);
        when(propertyUtils.getProperty(eq(SmsConstants.FOOD_CART_REJECTED_MSG), any(Locale.class))).thenReturn(TEST_SMS);
        RiderVehicleRegistrationDocument response = service.updateVehicleRegistrationStatus(request, existingDocument);
        assertEquals(PROFILE_ID, response.getRiderProfileId());
        assertEquals(MandatoryCheckStatus.PENDING, response.getStatus());
        assertEquals(MandatoryCheckStatus.REJECTED, response.getFoodCardStatus());
        verify(smsPublisher, times(INVOKED_ONCE)).sendSmsNotificationEvent(any(RiderProfile.class), eq(TEST_SMS));
    }

    @Test
    void shouldUpdateVehicleFoodCardStatusWithReasonAsOtherAndSendFoodCardRejectionSmsEvent() {
        RiderVehicleStatusRequest request = RiderVehicleStatusRequest.builder().status(MandatoryCheckStatus.REJECTED)
                .foodCardRejectionReason(Constants.OTHER).documentType(DocumentType.VEHICLE_WITH_FOOD_CARD).build();
        RiderVehicleRegistrationDocument existingDocument = getRiderVehicleRegistrationDocument(MandatoryCheckStatus.PENDING, MandatoryCheckStatus.PENDING);
        RiderVehicleRegistrationDocument updatedDocument = getRiderVehicleRegistrationDocument(MandatoryCheckStatus.PENDING, MandatoryCheckStatus.REJECTED);
        when(riderProfileRepository.findById(eq(PROFILE_ID))).thenReturn(Optional.of(getRiderProfile()));
        when(vehicleRegistrationRepository.save(any(RiderVehicleRegistrationDocument.class))).thenReturn(updatedDocument);
        when(propertyUtils.getProperty(eq(SmsConstants.FOOD_CART_REJECTED_MSG), any(Locale.class))).thenReturn(TEST_SMS);
        RiderVehicleRegistrationDocument response = service.updateVehicleRegistrationStatus(request, existingDocument);
        assertEquals(PROFILE_ID, response.getRiderProfileId());
        assertEquals(MandatoryCheckStatus.PENDING, response.getStatus());
        assertEquals(MandatoryCheckStatus.REJECTED, response.getFoodCardStatus());
        verify(smsPublisher, times(INVOKED_ONCE)).sendSmsNotificationEvent(any(RiderProfile.class), eq(TEST_SMS));
    }

    @Test
    void updateFoodBoxSizeWhenRiderNotExist() {
        when(riderProfileRepository.findById(eq(PROFILE_ID))).thenReturn(Optional.empty());
        FoodCartUpdateRequest request = FoodCartUpdateRequest.builder().foodBoxSize(FoodBoxSize.SMALL).build();
        assertThrows(DataNotFoundException.class, () -> service.updateFoodBoxSize(PROFILE_ID, Constants.OPS_MEMBER, request));
    }

    @Test
    void updateFoodBoxSizeWhenFoodCartDocumentNotExist() {
        when(riderProfileRepository.findById(eq(PROFILE_ID))).thenReturn(Optional.of(getRiderProfile()));
        when(vehicleRegistrationRepository.findByRiderProfileId(eq(PROFILE_ID))).thenReturn(Optional.empty());
        FoodCartUpdateRequest request = FoodCartUpdateRequest.builder().foodBoxSize(FoodBoxSize.SMALL).build();
        assertThrows(DataNotFoundException.class, () -> service.updateFoodBoxSize(PROFILE_ID, Constants.OPS_MEMBER, request));
    }

    @Test
    void updateFoodBoxSize() {
        when(riderProfileRepository.findById(eq(PROFILE_ID))).thenReturn(Optional.of(getRiderProfile()));
        when(vehicleRegistrationRepository.findByRiderProfileId(eq(PROFILE_ID))).thenReturn(Optional.of(
                getRiderVehicleRegistrationDocument(MandatoryCheckStatus.PENDING, MandatoryCheckStatus.PENDING)));
        when(vehicleRegistrationRepository.save(any(RiderVehicleRegistrationDocument.class))).thenReturn(
                getRiderVehicleRegistrationDocument(MandatoryCheckStatus.PENDING, MandatoryCheckStatus.PENDING));
        when(locationServiceFeignClient.updateRiderFoodBoxSize(any())).thenReturn(RiderFoodBoxSize.builder()
                .riderId("riderId")
                .foodBoxSize("LARGE")
                .build());
        FoodCartUpdateRequest request = FoodCartUpdateRequest.builder().foodBoxSize(FoodBoxSize.SMALL).build();
        RiderVehicleRegistrationDocument response = service.updateFoodBoxSize(PROFILE_ID, Constants.OPS_MEMBER, request);
        verify(vehicleRegistrationRepository, times(INVOKED_ONCE)).save(any(RiderVehicleRegistrationDocument.class));
        assertEquals(PROFILE_ID, response.getRiderProfileId());
    }

    private RiderVehicleRegistrationDetailsRequest getRiderVehicleRegistrationDetailsRequest(String registrationNo,
        MandatoryCheckStatus vehicleRegistrationStatus, MandatoryCheckStatus foodCardStatus) {
        return RiderVehicleRegistrationDetailsRequest.builder()
                .registrationNo(registrationNo)
                .registrationCardId(REGISTRATION_CARD_ID)
                .expiryDate(LocalDate.now())
                .registrationDate(LocalDate.now())
                .makerModel("BMW")
                .province("BBB")
                .uploadedVehicleDocUrl("1234")
                .status(vehicleRegistrationStatus)
                .foodCardStatus(foodCardStatus)
                .build();
    }

    private RiderVehicleRegistrationDocument getRiderVehicleRegistrationDocument(MandatoryCheckStatus vehicleStatus, MandatoryCheckStatus foodCardStatus) {
        return RiderVehicleRegistrationDocument.builder()
                .id(ID)
                .registrationNo(REGISTRATION_NO)
                .registrationCardId(REGISTRATION_CARD_ID)
                .expiryDate(LocalDate.now())
                .registrationDate(LocalDate.now())
                .makerModel("BMW")
                .province("BBB")
                .riderProfileId(PROFILE_ID)
                .uploadedVehicleDocUrl("1234")
                .status(vehicleStatus)
                .foodCardStatus(foodCardStatus)
                .build();
    }

    private RiderProfile getRiderProfile() {
        RiderProfile riderProfile = new RiderProfile();
        riderProfile.setId(PROFILE_ID);
        riderProfile.setPhoneNumber("8888888888");
        riderProfile.setCountryCode(Constants.IN_COUNTRY_CODE);
        return riderProfile;
    }
}
