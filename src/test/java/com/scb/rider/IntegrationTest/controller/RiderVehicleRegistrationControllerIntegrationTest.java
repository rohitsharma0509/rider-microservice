package com.scb.rider.IntegrationTest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.dto.RiderVehicleRegistrationDetailsRequest;
import com.scb.rider.model.dto.RiderVehicleRegistrationDetailsResponse;
import com.scb.rider.model.enumeration.MandatoryCheckStatus;
import com.scb.rider.repository.RiderProfileRepository;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RiderVehicleRegistrationControllerIntegrationTest {
    private static final String URL = "/profile/";
    private static RiderProfile riderProfile;
    private static final String ACCOUNT_NO = "121212121212121";
    private static final String REGISTRATION_NO = "1234";
    private static final String REGISTRATION_CARD_ID = "VRC1234";
    private static final String NEW_REGISTRATION_CARD_ID = "VRC12345";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RiderProfileRepository profileRepository;

    @Test
    @Order(1)
    void deleteData() {
        Optional<RiderProfile> rider = profileRepository.findByAccountNumber(ACCOUNT_NO);
        if(rider.isPresent()) {
            profileRepository.delete(rider.get());
        }
    }

    @Test
    @Order(2)
    void createRiderDetails() {
        riderProfile = new RiderProfile();
        riderProfile.setAccountNumber(ACCOUNT_NO);
        riderProfile.setCreatedDate(LocalDateTime.now().minusDays(2));
        riderProfile = profileRepository.save(riderProfile);
    }

    @Test
    @Order(3)
    public void testCreateRiderVehicleRegistrationDocumentRequest() throws Exception {
        RiderVehicleRegistrationDetailsRequest request = getRiderVehicleRegistrationDetailsRequest();
        String json = objectMapper.writeValueAsString(request);
        // execute
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(URL + riderProfile.getId() + "/vehicle-details")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print()).andReturn();

        // verify
        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.CREATED.value(), status, "Incorrect Response Status");
        RiderVehicleRegistrationDetailsResponse vehicleResponse = objectMapper.readValue(result.getResponse().getContentAsString(),
                RiderVehicleRegistrationDetailsResponse.class);
        assertNotNull(vehicleResponse);
        assertNotNull(vehicleResponse.getId());
        assertEquals(REGISTRATION_NO, vehicleResponse.getRegistrationNo());
    }

    @Test
    public void testUpdateVehicleRegistrationDetailsRequest() throws Exception {
        RiderVehicleRegistrationDetailsRequest request = getRiderVehicleRegistrationDetailsRequest();
        request.setRegistrationCardId(NEW_REGISTRATION_CARD_ID);
        String json = objectMapper.writeValueAsString(request);
        // execute
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(URL + riderProfile.getId() + "/vehicle-details")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print()).andReturn();

        // verify
        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.OK.value(), status, "Incorrect Response Status");
        RiderVehicleRegistrationDetailsResponse vehicleResponse = objectMapper.readValue(result.getResponse().getContentAsString(),
                RiderVehicleRegistrationDetailsResponse.class);
        assertNotNull(vehicleResponse);
        assertNotNull(vehicleResponse.getId());
        assertEquals(NEW_REGISTRATION_CARD_ID, vehicleResponse.getRegistrationCardId());
    }

    @Test
    public void testUpdateVehicleRegistrationDetailsRejectRequestWithReasonRequest() throws Exception {
        RiderVehicleRegistrationDetailsRequest request = updateRiderVehicleRegistrationDetailsWithReasonRequest();
        request.setRegistrationCardId(NEW_REGISTRATION_CARD_ID);
        String json = objectMapper.writeValueAsString(request);
        // execute
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(URL + riderProfile.getId() + "/vehicle-details")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print()).andReturn();

        // verify
        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.OK.value(), status, "Incorrect Response Status");
        RiderVehicleRegistrationDetailsResponse vehicleResponse = objectMapper.readValue(result.getResponse().getContentAsString(),
                RiderVehicleRegistrationDetailsResponse.class);
        assertNotNull(vehicleResponse);
        assertNotNull(vehicleResponse.getId());
        assertEquals(NEW_REGISTRATION_CARD_ID, vehicleResponse.getRegistrationCardId());
    }

    @Test
    public void testUpdateVehicleRegistrationDetailsRejectRequestWithoutReasonRequest() throws Exception {
        RiderVehicleRegistrationDetailsRequest request = updateRiderVehicleRegistrationDetailsWithoutReasonRequest();
        request.setRegistrationCardId(NEW_REGISTRATION_CARD_ID);
        String json = objectMapper.writeValueAsString(request);
        // execute
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(URL + riderProfile.getId() + "/vehicle-details")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print()).andReturn();

        // verify
        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.BAD_REQUEST.value(), status, "Incorrect Response Status");
    }

    @Test
    public void testUpdateFoodCardRegistrationDetailsRejectRequestWithReasonRequest() throws Exception {
        RiderVehicleRegistrationDetailsRequest request = updateRiderFoodCardRegistrationDetailsWithReasonRequest();
        request.setRegistrationCardId(NEW_REGISTRATION_CARD_ID);
        String json = objectMapper.writeValueAsString(request);
        // execute
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(URL + riderProfile.getId() + "/vehicle-details")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print()).andReturn();

        // verify
        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.OK.value(), status, "Incorrect Response Status");
        RiderVehicleRegistrationDetailsResponse vehicleResponse = objectMapper.readValue(result.getResponse().getContentAsString(),
                RiderVehicleRegistrationDetailsResponse.class);
        assertNotNull(vehicleResponse);
        assertNotNull(vehicleResponse.getId());
        assertEquals(NEW_REGISTRATION_CARD_ID, vehicleResponse.getRegistrationCardId());
    }

    @Test
    public void testUpdateFoodCardRegistrationDetailsRejectRequestWithoutReasonRequest() throws Exception {
        RiderVehicleRegistrationDetailsRequest request = updateRiderFoodCardRegistrationDetailsWithoutReasonRequest();
        request.setRegistrationCardId(NEW_REGISTRATION_CARD_ID);
        String json = objectMapper.writeValueAsString(request);
        // execute
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(URL + riderProfile.getId() + "/vehicle-details")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print()).andReturn();

        // verify
        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.BAD_REQUEST.value(), status, "Incorrect Response Status");
    }

    @Test
    public void testThrowExceptionWhenProfileNotFound() throws Exception {
        RiderVehicleRegistrationDetailsRequest request = getRiderVehicleRegistrationDetailsRequest();

        String json = objectMapper.writeValueAsString(request);
        // execute
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(URL + "randomProfileId" + "/vehicle-details")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print()).andReturn();

        // verify
        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.NOT_FOUND.value(), status, "Incorrect Response Status");
    }

    @Test
    public void testGetRiderVehicleRegistrationResponseByProfileIdSuccess() throws Exception {
        MvcResult result = mockMvc.perform(get(URL + riderProfile.getId() + "/vehicle-details")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andReturn();
        // verify
        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.OK.value(), status, "Incorrect Response Status");

        @SuppressWarnings("unchecked")
        RiderVehicleRegistrationDetailsResponse vehicleRegistrationDetailsResponse = objectMapper
                .readValue(result.getResponse().getContentAsString(), RiderVehicleRegistrationDetailsResponse.class);
        assertNotNull(vehicleRegistrationDetailsResponse, "Vehicle Registration details is not found");
    }

    @Test
    public void testGetRiderDrivingLicenseResponseByIdNotFound() throws Exception {
        MvcResult result = mockMvc.perform(get(URL + "randomProfileId" + "/vehicle-details")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andReturn();
        // verify
        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.NOT_FOUND.value(), status, "Incorrect Response Status");
    }

    private RiderVehicleRegistrationDetailsRequest getRiderVehicleRegistrationDetailsRequest() {
        return RiderVehicleRegistrationDetailsRequest.builder()
                .registrationNo(REGISTRATION_NO)
                .expiryDate(LocalDate.now())
                .registrationDate(LocalDate.now())
                .registrationCardId(REGISTRATION_CARD_ID)
                .province("Test")
                .makerModel("AUDI")
                .uploadedVehicleDocUrl("98121")
                .build();
    }
    private RiderVehicleRegistrationDetailsRequest updateRiderVehicleRegistrationDetailsWithReasonRequest() {
        return RiderVehicleRegistrationDetailsRequest.builder()
                .registrationNo(REGISTRATION_NO)
                .status(MandatoryCheckStatus.REJECTED)
                .vehicleRejectionReason("Other")
                .expiryDate(LocalDate.now())
                .registrationDate(LocalDate.now())
                .registrationCardId(REGISTRATION_CARD_ID)
                .province("Test")
                .makerModel("AUDI")
                .uploadedVehicleDocUrl("98121")
                .build();
    }
    private RiderVehicleRegistrationDetailsRequest updateRiderVehicleRegistrationDetailsWithoutReasonRequest() {
        return RiderVehicleRegistrationDetailsRequest.builder()
                .registrationNo(REGISTRATION_NO)
                .status(MandatoryCheckStatus.REJECTED)
                .expiryDate(LocalDate.now())
                .registrationDate(LocalDate.now())
                .registrationCardId(REGISTRATION_CARD_ID)
                .province("Test")
                .makerModel("AUDI")
                .uploadedVehicleDocUrl("98121")
                .build();
    }
    private RiderVehicleRegistrationDetailsRequest updateRiderFoodCardRegistrationDetailsWithReasonRequest() {
        return RiderVehicleRegistrationDetailsRequest.builder()
                .registrationNo(REGISTRATION_NO)
                .foodCardStatus(MandatoryCheckStatus.REJECTED)
                .foodCardRejectionReason("Other")
                .expiryDate(LocalDate.now())
                .registrationDate(LocalDate.now())
                .registrationCardId(REGISTRATION_CARD_ID)
                .province("Test")
                .makerModel("AUDI")
                .uploadedVehicleDocUrl("98121")
                .build();
    }
    private RiderVehicleRegistrationDetailsRequest updateRiderFoodCardRegistrationDetailsWithoutReasonRequest() {
        return RiderVehicleRegistrationDetailsRequest.builder()
                .registrationNo(REGISTRATION_NO)
                .foodCardStatus(MandatoryCheckStatus.REJECTED)
                .expiryDate(LocalDate.now())
                .registrationDate(LocalDate.now())
                .registrationCardId(REGISTRATION_CARD_ID)
                .province("Test")
                .makerModel("AUDI")
                .uploadedVehicleDocUrl("98121")
                .build();
    }


}
