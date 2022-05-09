package com.scb.rider.IntegrationTest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.rider.model.document.RiderProfile;
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

import com.scb.rider.model.dto.RiderBackgroundVerificationDetailsRequest;
import com.scb.rider.model.dto.RiderBackgroundVerificationDetailsResponse;
import com.scb.rider.model.enumeration.MandatoryCheckStatus;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RiderBackgroundVerificationControllerIntegrationTest {

    static final String URL = "/profile/";
    private static RiderProfile riderProfile;
    private static final String ACCOUNT_NO = "121212121212121";

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
    void testCreateRiderBackgroundDocumentRequest() throws Exception {
        String json = objectMapper.writeValueAsString(getRiderBackgroundVerificationDetailsRequest());
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(URL + riderProfile.getId() + "/background-details")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print()).andReturn();

        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.CREATED.value(), status, "Incorrect Response Status");

        RiderBackgroundVerificationDetailsResponse backgroundResponse = objectMapper.readValue(result.getResponse().getContentAsString(),
        		RiderBackgroundVerificationDetailsResponse.class);
        assertNotNull(backgroundResponse);
        assertNotNull(backgroundResponse.getId());
        assertEquals(riderProfile.getId(), backgroundResponse.getRiderProfileId(), "Invalid riderId");
    }
    @Test
    void testCreateRiderBackgroundDocumentRejectRequest() throws Exception {
        String json = objectMapper.writeValueAsString(getRiderBackgroundVerificationDetailsRejectRequest());
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(URL + riderProfile.getId() + "/background-details")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print()).andReturn();

        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.OK.value(), status, "Incorrect Response Status");

        RiderBackgroundVerificationDetailsResponse backgroundResponse = objectMapper.readValue(result.getResponse().getContentAsString(),
                RiderBackgroundVerificationDetailsResponse.class);
        assertNotNull(backgroundResponse);
        assertNotNull(backgroundResponse.getId());
        assertEquals(riderProfile.getId(), backgroundResponse.getRiderProfileId(), "Invalid riderId");
    }
    @Test
    void testCreateRiderBackgroundDocumentRejectWithoutReasonRequest() throws Exception {
        String json = objectMapper.writeValueAsString(getRiderBackgroundVerificationDetailsRejectWithoutReasonRequest());
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(URL + riderProfile.getId() + "/background-details")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print()).andReturn();

        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.BAD_REQUEST.value(), status, "Incorrect Response Status");

    }

    @Test
    void testUpdateBackgroundVerificationDetailsRequest() throws Exception {
        String json = objectMapper.writeValueAsString(getRiderBackgroundVerificationDetailsRequest());
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(URL + riderProfile.getId() + "/background-details")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print()).andReturn();

        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.OK.value(), status, "Incorrect Response Status");

        RiderBackgroundVerificationDetailsResponse response = objectMapper.readValue(result.getResponse().getContentAsString(),
        		RiderBackgroundVerificationDetailsResponse.class);
        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals(riderProfile.getId(), response.getRiderProfileId());
    }

    
    @Test
    void testUpdateBackgroundVerificationDetailsNotFound() throws Exception {
        String json = objectMapper.writeValueAsString(getRiderBackgroundVerificationDetailsRequest());
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(URL + "non-existing-rider-id" + "/background-details")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print()).andReturn();
        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.NOT_FOUND.value(), status, "Incorrect Response Status");
    }

    @Test
    void testGetRiderBackgroundResponseByProfileIdSuccess() throws Exception {
        MvcResult result = mockMvc.perform(get(URL + riderProfile.getId() + "/background-details")
                .contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON)).andDo(print()).andReturn();
        
        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.OK.value(), status, "Incorrect Response Status");

        RiderBackgroundVerificationDetailsResponse response = objectMapper
                .readValue(result.getResponse().getContentAsString(), RiderBackgroundVerificationDetailsResponse.class);

        assertNotNull(response, "Rider background details not found");
        assertEquals(riderProfile.getId(), response.getRiderProfileId());

    }

    @Test
    void testGetRiderBackgroundVerificationByProfileIdNotFound() throws Exception {
        MvcResult result = mockMvc.perform(get(URL + "non-existing-rider-id" + "/background-details")
                          .contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON)).andDo(print()).andReturn();
        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.NOT_FOUND.value(), status, "Incorrect Response Status");
    }

    private RiderBackgroundVerificationDetailsRequest getRiderBackgroundVerificationDetailsRequest() {
        return RiderBackgroundVerificationDetailsRequest.builder()
                .status(MandatoryCheckStatus.APPROVED).dueDate(LocalDate.of(2022, 12, 11))
                .reason("Test").documentUrls(Arrays.asList("localhost/")).build();
    }

    private RiderBackgroundVerificationDetailsRequest getRiderBackgroundVerificationDetailsRejectRequest() {
        return RiderBackgroundVerificationDetailsRequest.builder()
                .status(MandatoryCheckStatus.REJECTED).dueDate(LocalDate.of(2022, 12, 11))
                .reason("Other")
                .comment("Provide correct Information")
                .reason("Test").documentUrls(Arrays.asList("localhost/")).build();
    }

    private RiderBackgroundVerificationDetailsRequest getRiderBackgroundVerificationDetailsRejectWithoutReasonRequest() {
        return RiderBackgroundVerificationDetailsRequest.builder()
                .status(MandatoryCheckStatus.REJECTED).dueDate(LocalDate.of(2022, 12, 11))
                .comment("Provide correct Information")
                .documentUrls(Arrays.asList("localhost/")).build();
    }


}
