package com.scb.rider.controller;

import com.scb.rider.constants.Constants;
import com.scb.rider.constants.DocumentType;
import com.scb.rider.model.document.RiderVehicleRegistrationDocument;
import com.scb.rider.model.dto.FoodCartUpdateRequest;
import com.scb.rider.model.dto.RiderVehicleRegistrationDetailsRequest;
import com.scb.rider.model.dto.RiderVehicleRegistrationDetailsResponse;
import com.scb.rider.model.dto.RiderVehicleStatusRequest;
import com.scb.rider.model.enumeration.FoodBoxSize;
import com.scb.rider.model.enumeration.MandatoryCheckStatus;
import com.scb.rider.service.document.RiderVehicleRegistrationService;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
public class RiderVehicleRegistrationControllerTest {

    private static final String REGISTRATION_NO =  "REG123";
    private static final String PROFILE_ID =  "12345";

    @InjectMocks
    private RiderVehicleRegistrationController vehicleRegistrationController;

    @Mock
    private RiderVehicleRegistrationService vehicleRegistrationService;

    @Test
    public void testCreateRiderVehicleRegistrationRequest() throws Exception {

        // prepare data and mock's behaviour
        RiderVehicleRegistrationDetailsRequest request = RiderVehicleRegistrationDetailsRequest.builder()
                .registrationNo(REGISTRATION_NO)
                .registrationCardId("71218")
                .expiryDate(LocalDate.now())
                .registrationDate(LocalDate.now())
                .makerModel("BMW")
                .province("BBB")
                .uploadedVehicleDocUrl("1234")
                .build();

        RiderVehicleRegistrationDocument mappedDocument = RiderVehicleRegistrationDocument.builder()
                .id("12345")
                .registrationNo(REGISTRATION_NO)
                .registrationCardId("71218")
                .expiryDate(LocalDate.now())
                .registrationDate(LocalDate.now())
                .makerModel("BMW")
                .province("BBB")
                .riderProfileId(PROFILE_ID)
                .uploadedVehicleDocUrl("1234")
                .build();

        RiderVehicleRegistrationDetailsResponse response = RiderVehicleRegistrationDetailsResponse.builder()
                .id("12345")
                .registrationNo(REGISTRATION_NO)
                .registrationCardId("71218")
                .expiryDate(LocalDate.now())
                .registrationDate(LocalDate.now())
                .makerModel("BMW")
                .province("BBB")
                .riderProfileId(PROFILE_ID)
                .uploadedVehicleDocUrl("1234")
                .build();

        when(vehicleRegistrationService.createRiderVehicleRegistrationDetails(any(String.class),
                any(RiderVehicleRegistrationDetailsRequest.class)))
                .thenReturn(response);


        ResponseEntity<RiderVehicleRegistrationDetailsResponse> apiResponse = vehicleRegistrationController
                .createRiderVehicleRegistrationDetails(Constants.OPS_MEMBER, "1",request);

        assertTrue(ObjectUtils.isNotEmpty(apiResponse.getBody()));
        assertEquals(HttpStatus.CREATED, apiResponse.getStatusCode());

        assertEquals(PROFILE_ID, response.getRiderProfileId(), "Invalid Response");
        assertEquals("12345", response.getId(), "Invalid Response");
        assertEquals(REGISTRATION_NO, response.getRegistrationNo(), "Invalid Response");
        assertEquals("71218", response.getRegistrationCardId(), "Invalid Response");
        assertEquals("BMW", response.getMakerModel(), "Invalid Response");
        assertEquals("BBB", response.getProvince(), "Invalid Response");
        assertEquals("1234", response.getUploadedVehicleDocUrl(), "Invalid Response");
        assertNotNull(response.getExpiryDate());
        assertNotNull(response.getRegistrationDate());
        assertNotNull(response.toString());
        assertNotNull(request.toString());
    }

    @Test
    public void testUpdateRiderVehicleRegistrationRequest() throws Exception {

        // prepare data and mock's behaviour
        RiderVehicleRegistrationDetailsRequest request = RiderVehicleRegistrationDetailsRequest.builder()
                .registrationNo(REGISTRATION_NO)
                .registrationCardId("71218")
                .expiryDate(LocalDate.now())
                .registrationDate(LocalDate.now())
                .makerModel("BMW")
                .province("BBB")
                .uploadedVehicleDocUrl("1234")
                .build();

        RiderVehicleRegistrationDocument mappedDocument = RiderVehicleRegistrationDocument.builder()
                .id("12345")
                .registrationNo(REGISTRATION_NO)
                .registrationCardId("71218")
                .expiryDate(LocalDate.now())
                .registrationDate(LocalDate.now())
                .makerModel("BMW")
                .province("BBB")
                .riderProfileId(PROFILE_ID)
                .uploadedVehicleDocUrl("1234")
                .build();

        RiderVehicleRegistrationDetailsResponse response = RiderVehicleRegistrationDetailsResponse.builder()
                .id("12345")
                .registrationNo(REGISTRATION_NO)
                .registrationCardId("71218")
                .expiryDate(LocalDate.now())
                .registrationDate(LocalDate.now())
                .makerModel("BMW")
                .province("BBB")
                .riderProfileId(PROFILE_ID)
                .uploadedVehicleDocUrl("1234")
                .build();


        when(vehicleRegistrationService.findRiderVehicleRegistrationDetailsByProfileId(any(String.class))).thenReturn(
                Optional.of(mappedDocument));

        when(vehicleRegistrationService.updateRiderVehicleRegistrationDetails(
                any(RiderVehicleRegistrationDetailsRequest.class),
                any(RiderVehicleRegistrationDocument.class))).thenReturn(mappedDocument);

        ResponseEntity<RiderVehicleRegistrationDetailsResponse> apiResponse = vehicleRegistrationController
                .updateVehicleRegistrationDetails(Constants.OPS_MEMBER, "1",request);

        assertTrue(ObjectUtils.isNotEmpty(apiResponse.getBody()));
        assertEquals(HttpStatus.OK, apiResponse.getStatusCode());
    }

    @Test
    public void testUpdateRiderVehicleRegistrationRequestNotFound() throws Exception {

        // prepare data and mock's behaviour
        RiderVehicleRegistrationDetailsRequest request = RiderVehicleRegistrationDetailsRequest.builder()
                .registrationNo(REGISTRATION_NO)
                .registrationCardId("71218")
                .expiryDate(LocalDate.now())
                .registrationDate(LocalDate.now())
                .makerModel("BMW")
                .province("BBB")
                .uploadedVehicleDocUrl("1234")
                .build();

        RiderVehicleRegistrationDocument mappedDocument = RiderVehicleRegistrationDocument.builder()
                .id("12345")
                .registrationNo(REGISTRATION_NO)
                .registrationCardId("71218")
                .expiryDate(LocalDate.now())
                .registrationDate(LocalDate.now())
                .makerModel("BMW")
                .province("BBB")
                .riderProfileId(PROFILE_ID)
                .uploadedVehicleDocUrl("1234")
                .build();

        RiderVehicleRegistrationDetailsResponse response = RiderVehicleRegistrationDetailsResponse.builder()
                .id("12345")
                .registrationNo(REGISTRATION_NO)
                .registrationCardId("71218")
                .expiryDate(LocalDate.now())
                .registrationDate(LocalDate.now())
                .makerModel("BMW")
                .province("BBB")
                .riderProfileId(PROFILE_ID)
                .uploadedVehicleDocUrl("1234")
                .build();


        when(vehicleRegistrationService.findRiderVehicleRegistrationDetailsByProfileId(any(String.class))).thenReturn(
                Optional.empty());

        when(vehicleRegistrationService.updateRiderVehicleRegistrationDetails(
                any(RiderVehicleRegistrationDetailsRequest.class),
                any(RiderVehicleRegistrationDocument.class))).thenReturn(mappedDocument);

        ResponseEntity<RiderVehicleRegistrationDetailsResponse> apiResponse = vehicleRegistrationController
                .updateVehicleRegistrationDetails(Constants.OPS_MEMBER, "1",request);

        assertFalse(ObjectUtils.isNotEmpty(apiResponse.getBody()));
        assertEquals(HttpStatus.NOT_FOUND, apiResponse.getStatusCode());
    }
    @Test
    public void testNotFoundThrowExceptionWhenProfileNotFound() throws Exception {

        // prepare data and mock's behaviour
        RiderVehicleRegistrationDetailsRequest request = RiderVehicleRegistrationDetailsRequest.builder()
                .registrationNo(REGISTRATION_NO)
                .registrationCardId("71218")
                .expiryDate(LocalDate.now())
                .registrationDate(LocalDate.now())
                .makerModel("BMW")
                .province("BBB")
                .uploadedVehicleDocUrl("1234")
                .build();

        RiderVehicleRegistrationDocument mappedDocument = RiderVehicleRegistrationDocument.builder()
                .id("12345")
                .registrationNo(REGISTRATION_NO)
                .registrationCardId("71218")
                .expiryDate(LocalDate.now())
                .registrationDate(LocalDate.now())
                .makerModel("BMW")
                .province("BBB")
                .riderProfileId(PROFILE_ID)
                .uploadedVehicleDocUrl("1234")
                .build();

        RiderVehicleRegistrationDetailsResponse response = RiderVehicleRegistrationDetailsResponse.builder()
                .id("12345")
                .registrationNo(REGISTRATION_NO)
                .registrationCardId("71218")
                .expiryDate(LocalDate.now())
                .registrationDate(LocalDate.now())
                .makerModel("BMW")
                .province("BBB")
                .riderProfileId(PROFILE_ID)
                .uploadedVehicleDocUrl("1234")
                .build();


        when(vehicleRegistrationService.findRiderVehicleRegistrationDetailsByProfileId(any(String.class)))
                .thenReturn(Optional.empty());


        // verify
        ResponseEntity<RiderVehicleRegistrationDetailsResponse> apiResponse = vehicleRegistrationController
                .getVehicleRegistrationByProfileId(PROFILE_ID);

        assertTrue(ObjectUtils.isEmpty(apiResponse.getBody()));
        assertEquals(HttpStatus.NOT_FOUND, apiResponse.getStatusCode());

    }


    @Test
    public void testGetRiderVehicleDetailsResponseByProfileIdSuccess() throws Exception {
        RiderVehicleRegistrationDetailsRequest request = RiderVehicleRegistrationDetailsRequest.builder()
                .registrationNo(REGISTRATION_NO)
                .registrationCardId("71218")
                .expiryDate(LocalDate.now())
                .registrationDate(LocalDate.now())
                .makerModel("BMW")
                .province("BBB")
                .uploadedVehicleDocUrl("1234")
                .build();

        RiderVehicleRegistrationDocument mappedDocument = RiderVehicleRegistrationDocument.builder()
                .id("12345")
                .registrationNo(REGISTRATION_NO)
                .registrationCardId("71218")
                .expiryDate(LocalDate.now())
                .registrationDate(LocalDate.now())
                .makerModel("BMW")
                .province("BBB")
                .riderProfileId(PROFILE_ID)
                .uploadedVehicleDocUrl("1234")
                .build();

        RiderVehicleRegistrationDetailsResponse response = RiderVehicleRegistrationDetailsResponse.builder()
                .id("12345")
                .registrationNo(REGISTRATION_NO)
                .registrationCardId("71218")
                .expiryDate(LocalDate.now())
                .registrationDate(LocalDate.now())
                .makerModel("BMW")
                .province("BBB")
                .riderProfileId(PROFILE_ID)
                .uploadedVehicleDocUrl("1234")
                .build();

        when(vehicleRegistrationService.findRiderVehicleRegistrationDetailsByProfileId(any(String.class)))
                .thenReturn(Optional.of(mappedDocument));


        // verify
        ResponseEntity<RiderVehicleRegistrationDetailsResponse> apiResponse = vehicleRegistrationController
                .getVehicleRegistrationByProfileId(REGISTRATION_NO);

        assertTrue(ObjectUtils.isNotEmpty(apiResponse.getBody()));
        assertEquals(HttpStatus.OK, apiResponse.getStatusCode());
    }

    @Test
    public void testGetRiderVehicleRegistrationResponseByProfileIdNotFound() throws Exception {
        RiderVehicleRegistrationDetailsRequest request = RiderVehicleRegistrationDetailsRequest.builder()
                .registrationNo(REGISTRATION_NO)
                .registrationCardId("71218")
                .expiryDate(LocalDate.now())
                .registrationDate(LocalDate.now())
                .makerModel("BMW")
                .province("BBB")
                .uploadedVehicleDocUrl("1234")
                .build();

        RiderVehicleRegistrationDocument mappedDocument = RiderVehicleRegistrationDocument.builder()
                .id("12345")
                .registrationNo(REGISTRATION_NO)
                .registrationCardId("71218")
                .expiryDate(LocalDate.now())
                .registrationDate(LocalDate.now())
                .makerModel("BMW")
                .province("BBB")
                .riderProfileId(PROFILE_ID)
                .uploadedVehicleDocUrl("1234")
                .build();

        RiderVehicleRegistrationDetailsResponse response = RiderVehicleRegistrationDetailsResponse.builder()
                .id("12345")
                .registrationNo(REGISTRATION_NO)
                .registrationCardId("71218")
                .expiryDate(LocalDate.now())
                .registrationDate(LocalDate.now())
                .makerModel("BMW")
                .province("BBB")
                .riderProfileId(PROFILE_ID)
                .uploadedVehicleDocUrl("1234")
                .build();

        when(vehicleRegistrationService.findRiderVehicleRegistrationDetailsByProfileId(any(String.class)))
                .thenReturn(Optional.empty());


        // verify
        ResponseEntity<RiderVehicleRegistrationDetailsResponse> apiResponse = vehicleRegistrationController
                .getVehicleRegistrationByProfileId(REGISTRATION_NO);

        assertTrue(ObjectUtils.isEmpty(apiResponse.getBody()));
        assertEquals(HttpStatus.NOT_FOUND, apiResponse.getStatusCode());


    }

    @Test
    public void testupdateVehicleRegistrationStatusByProfileIdNotFound() throws Exception {

        RiderVehicleStatusRequest request = RiderVehicleStatusRequest.builder()
                .status(MandatoryCheckStatus.REJECTED)
                .documentType(DocumentType.VEHICLE_WITH_FOOD_CARD)
                .build();

        when(vehicleRegistrationService.findRiderVehicleRegistrationDetailsByProfileId(any(String.class)))
                .thenReturn(Optional.empty());


        // verify
        ResponseEntity<RiderVehicleRegistrationDetailsResponse> apiResponse = vehicleRegistrationController
                .updateVehicleRegistrationStatus("1", request);

        assertTrue(ObjectUtils.isEmpty(apiResponse.getBody()));
        assertEquals(HttpStatus.NOT_FOUND, apiResponse.getStatusCode());


    }

    @Test
    public void testupdateVehicleRegistrationStatusSuccess() throws Exception {

        RiderVehicleStatusRequest request = RiderVehicleStatusRequest.builder()
                .status(MandatoryCheckStatus.PENDING)
                .documentType(DocumentType.VEHICLE_WITH_FOOD_CARD)
                .build();
        RiderVehicleRegistrationDocument riderVehicleRegistrationDocument = RiderVehicleRegistrationDocument.builder()
                .foodCardStatus(MandatoryCheckStatus.PENDING)
                .build();


        when(vehicleRegistrationService.findRiderVehicleRegistrationDetailsByProfileId(any(String.class)))
                .thenReturn(Optional.ofNullable(riderVehicleRegistrationDocument));
        when(vehicleRegistrationService.updateVehicleRegistrationStatus(any(RiderVehicleStatusRequest.class), any(RiderVehicleRegistrationDocument.class))).thenReturn(riderVehicleRegistrationDocument);

        // verify
        ResponseEntity<RiderVehicleRegistrationDetailsResponse> apiResponse = vehicleRegistrationController
                .updateVehicleRegistrationStatus("1", request);

        assertEquals(HttpStatus.OK, apiResponse.getStatusCode());


    }

    @Test
    public void updateFoodBoxSize() {
        RiderVehicleRegistrationDocument document = RiderVehicleRegistrationDocument.builder().foodCardStatus(MandatoryCheckStatus.PENDING).build();
        when(vehicleRegistrationService.updateFoodBoxSize(eq(PROFILE_ID), eq(Constants.OPS_MEMBER), any(FoodCartUpdateRequest.class))).thenReturn(document);
        FoodCartUpdateRequest request = FoodCartUpdateRequest.builder().foodBoxSize(FoodBoxSize.SMALL).build();
        ResponseEntity<RiderVehicleRegistrationDetailsResponse> result = vehicleRegistrationController.updateFoodBoxSize(Constants.OPS_MEMBER, PROFILE_ID, request);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(MandatoryCheckStatus.PENDING, result.getBody().getFoodCardStatus());
    }

}
