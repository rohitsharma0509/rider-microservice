package com.scb.rider.IntegrationTest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.dto.RiderFoodCardRequest;
import com.scb.rider.model.dto.RiderFoodCardResponse;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RiderFoodCardControllerIntegrationTest {

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
    void testCreateRiderFoodCardDocumentRequest() throws Exception {
        String json = objectMapper.writeValueAsString(getRiderFoodCardDetailsRequest());
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(URL + riderProfile.getId() + "/foodcard-details")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print()).andReturn();

        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.CREATED.value(), status, "Incorrect Response Status");

        RiderFoodCardResponse response = objectMapper.readValue(result.getResponse().getContentAsString(),
                RiderFoodCardResponse.class);
        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals(riderProfile.getId(), response.getRiderProfileId(), "Invalid riderId");
    }

    @Test
    void testUpdateRiderFoodCardDocumentRequest() throws Exception {
        RiderFoodCardRequest riderFoodCardRequest = getRiderFoodCardDetailsRequest();
                String json = objectMapper.writeValueAsString(riderFoodCardRequest);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(URL + riderProfile.getId() + "/foodcard-details")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print()).andReturn();

        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.OK.value(), status, "Incorrect Response Status");

        RiderFoodCardResponse response = objectMapper.readValue(result.getResponse().getContentAsString(),
                RiderFoodCardResponse.class);
        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals(riderProfile.getId(), response.getRiderProfileId(), "Invalid riderId");
        assertNotNull(riderFoodCardRequest.toString());
    }
    @Test
    void testUpdateRiderFoodCardDocumentRejectedWithReasonRequest() throws Exception {
        RiderFoodCardRequest riderFoodCardRequest = getRiderFoodCardDetailsWithReasonRequest();
        String json = objectMapper.writeValueAsString(riderFoodCardRequest);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(URL + riderProfile.getId() + "/foodcard-details")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print()).andReturn();

        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.OK.value(), status, "Incorrect Response Status");

        RiderFoodCardResponse response = objectMapper.readValue(result.getResponse().getContentAsString(),
                RiderFoodCardResponse.class);
        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals(riderProfile.getId(), response.getRiderProfileId(), "Invalid riderId");
        assertNotNull(riderFoodCardRequest.toString());
    }
    @Test
    void testUpdateRiderFoodCardDocumentRejectedWithoutReasonRequest() throws Exception {
        RiderFoodCardRequest riderFoodCardRequest = getRiderFoodCardDetailsWithoutReasonRequest();
        String json = objectMapper.writeValueAsString(riderFoodCardRequest);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(URL + riderProfile.getId() + "/foodcard-details")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print()).andReturn();

        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.BAD_REQUEST.value(), status, "Incorrect Response Status");
    }
    @Test
    void testUpdateFoodCardDetailsNotFound() throws Exception {
        String json = objectMapper.writeValueAsString(getRiderFoodCardDetailsRequest());
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(URL + "non-existing-rider-id" + "/foodcard-details")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print()).andReturn();
        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.NOT_FOUND.value(), status, "Incorrect Response Status");
    }

    @Test
    void testGetRiderFoodCardByProfileIdNotFound() throws Exception {
        MvcResult result = mockMvc.perform(get(URL + "non-existing-rider-id" + "/foodcard-details")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON)).andDo(print()).andReturn();
        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.NOT_FOUND.value(), status, "Incorrect Response Status");
    }

    private RiderFoodCardRequest getRiderFoodCardDetailsRequest() {
        return RiderFoodCardRequest.builder()
                .status(MandatoryCheckStatus.APPROVED).documentUrl("localhost/").build();
    }
    private RiderFoodCardRequest getRiderFoodCardDetailsWithReasonRequest() {
        return RiderFoodCardRequest.builder()
                .reason("Other")
                .status(MandatoryCheckStatus.REJECTED).documentUrl("localhost/").build();
    }
    private RiderFoodCardRequest getRiderFoodCardDetailsWithoutReasonRequest() {
        return RiderFoodCardRequest.builder()
                .status(MandatoryCheckStatus.REJECTED).documentUrl("localhost/").build();
    }
}
