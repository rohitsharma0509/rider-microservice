package com.scb.rider.IntegrationTest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.dto.RiderDrivingLicenseRequest;
import com.scb.rider.model.dto.RiderDrivingLicenseResponse;
import com.scb.rider.model.enumeration.MandatoryCheckStatus;
import com.scb.rider.repository.RiderProfileRepository;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RiderDrivingLicenseControllerIntegrationTest {
    private static final String URL ="/profile/";
    private static final String RIDER_DRIVING_LICENSE_BY_ID = "/profile/license-details";
    private static RiderProfile riderProfile;
    private static RiderDrivingLicenseResponse drivingLicenseResponse;
    private static final String ACCOUNT_NO = "121212121212121";
    private static final String DRIVING_LICENSE_NO = "1234";
    private static final String TYPE_OF_LICENSE = "Class 1";
    private static final String TYPE_OF_LICENSE_NEW = "Class 2";

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
        log.info("Driving License integration tests are running for riderId: {}", riderProfile.getId());
    }

    @Test
    @Order(3)
    public void testCreateRiderDrivingLicenseRequest() throws Exception {
        RiderDrivingLicenseRequest drivingLicenseDocument = getRiderDrivingLicenseRequest();
        String json = objectMapper.writeValueAsString(drivingLicenseDocument);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(URL + riderProfile.getId() + "/license-details")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print()).andReturn();

        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.CREATED.value(), status, "Incorrect Response Status");
        drivingLicenseResponse = objectMapper.readValue(result.getResponse().getContentAsString(),
                RiderDrivingLicenseResponse.class);
        assertNotNull(drivingLicenseResponse);
        assertNotNull(drivingLicenseResponse.getId());
        assertEquals(DRIVING_LICENSE_NO, drivingLicenseResponse.getDrivingLicenseNumber());
    }

    @Test
    @Order(4)
    public void testGetDrivingLicenseById() throws Exception {
        MvcResult result = mockMvc.perform(get(RIDER_DRIVING_LICENSE_BY_ID + "/" + drivingLicenseResponse.getId())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andReturn();
        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.OK.value(), status, "Incorrect Response Status");
        @SuppressWarnings("unchecked")
        RiderDrivingLicenseResponse riderDrivingLicenseResponse = objectMapper
                .readValue(result.getResponse().getContentAsString(), RiderDrivingLicenseResponse.class);
        assertNotNull(riderDrivingLicenseResponse, "Driving License is not found");
    }

    @Test
    public void testGetRiderDrivingLicenseResponseByIdNotFound() throws Exception {
        MvcResult result = mockMvc.perform(get(RIDER_DRIVING_LICENSE_BY_ID + "/invalidDLid")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andReturn();
        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.NOT_FOUND.value(), status, "Incorrect Response Status");
    }

    @Test
    public void testUpdateRiderDrivingLicenseRequestWithReason() throws Exception {
        RiderDrivingLicenseRequest drivingLicenseDocument = getRiderDrivingLicenseRequestWithReason();
        drivingLicenseDocument.setTypeOfLicense(TYPE_OF_LICENSE_NEW);
        String json = objectMapper.writeValueAsString(drivingLicenseDocument);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(URL + riderProfile.getId() + "/license-details")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print()).andReturn();
        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.OK.value(), status, "Incorrect Response Status");
        RiderDrivingLicenseResponse drivingLicenseResponse = objectMapper.readValue(result.getResponse().getContentAsString(),
                RiderDrivingLicenseResponse.class);
        assertNotNull(drivingLicenseResponse);
        assertNotNull(drivingLicenseResponse.getId());
        assertEquals(TYPE_OF_LICENSE_NEW, drivingLicenseResponse.getTypeOfLicense());
    }
    @Test
    public void testUpdateRiderDrivingLicenseRequestWithoutReason() throws Exception {
        RiderDrivingLicenseRequest drivingLicenseDocument = getRiderDrivingLicenseRequestWithoutReason();
        drivingLicenseDocument.setTypeOfLicense(TYPE_OF_LICENSE_NEW);
        String json = objectMapper.writeValueAsString(drivingLicenseDocument);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(URL + riderProfile.getId() + "/license-details")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print()).andReturn();
        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.BAD_REQUEST.value(), status, "Incorrect Response Status");
    }
    @Test
    public void testUpdateRiderDrivingLicenseRequest() throws Exception {
        RiderDrivingLicenseRequest drivingLicenseDocument = getRiderDrivingLicenseRequest();
        drivingLicenseDocument.setTypeOfLicense(TYPE_OF_LICENSE_NEW);
        String json = objectMapper.writeValueAsString(drivingLicenseDocument);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(URL + riderProfile.getId() + "/license-details")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print()).andReturn();
        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.OK.value(), status, "Incorrect Response Status");
        RiderDrivingLicenseResponse drivingLicenseResponse = objectMapper.readValue(result.getResponse().getContentAsString(),
                RiderDrivingLicenseResponse.class);
        assertNotNull(drivingLicenseResponse);
        assertNotNull(drivingLicenseResponse.getId());
        assertEquals(TYPE_OF_LICENSE_NEW, drivingLicenseResponse.getTypeOfLicense());
    }
    @Test
    public void testThrowExceptionWhenProfileNotFound() throws Exception {
        RiderDrivingLicenseRequest drivingLicenseDocument = getRiderDrivingLicenseRequest();
        String json = objectMapper.writeValueAsString(drivingLicenseDocument);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(URL + "randomProfileId" + "/license-details")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print()).andReturn();
        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.NOT_FOUND.value(), status, "Incorrect Response Status");
    }

    @Test
    public void testGetRiderDrivingLicenseResponseByProfileIdSuccess() throws Exception {
        MvcResult result = mockMvc.perform(get(URL + riderProfile.getId() + "/license-details")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andReturn();
        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.OK.value(), status, "Incorrect Response Status");
        @SuppressWarnings("unchecked")
        RiderDrivingLicenseResponse riderDrivingLicenseResponse = objectMapper
                .readValue(result.getResponse().getContentAsString(), RiderDrivingLicenseResponse.class);
        assertNotNull(riderDrivingLicenseResponse, "Driving License is not found");
    }

    @Test
    public void testGetRiderDrivingLicenseResponseByProfileIdNotFound() throws Exception {
        MvcResult result = mockMvc.perform(get(URL + "/randomProfileId" + "/license-details")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andReturn();
        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.NOT_FOUND.value(), status, "Incorrect Response Status");
    }

    private RiderDrivingLicenseRequest getRiderDrivingLicenseRequest() {
        return RiderDrivingLicenseRequest.builder()
                .drivingLicenseNumber(DRIVING_LICENSE_NO)
                .dateOfExpiry(LocalDate.now())
                .dateOfIssue(LocalDate.now())
                .typeOfLicense(TYPE_OF_LICENSE)
                .documentUrl("test")
                .build();
    }
    private RiderDrivingLicenseRequest getRiderDrivingLicenseRequestWithReason() {
        return RiderDrivingLicenseRequest.builder()
                .status(MandatoryCheckStatus.REJECTED)
                .reason("Others")
                .drivingLicenseNumber(DRIVING_LICENSE_NO)
                .dateOfExpiry(LocalDate.now())
                .dateOfIssue(LocalDate.now())
                .typeOfLicense(TYPE_OF_LICENSE)
                .documentUrl("test")
                .build();
    }
    private RiderDrivingLicenseRequest getRiderDrivingLicenseRequestWithoutReason() {
        return RiderDrivingLicenseRequest.builder()
                .status(MandatoryCheckStatus.REJECTED)
                .drivingLicenseNumber(DRIVING_LICENSE_NO)
                .dateOfExpiry(LocalDate.now())
                .dateOfIssue(LocalDate.now())
                .typeOfLicense(TYPE_OF_LICENSE)
                .documentUrl("test")
                .build();
    }

}
