package com.scb.rider.bdd.stepdefinitons;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.rider.model.document.RiderDeviceDetails;
import com.scb.rider.model.document.RiderPreferredZones;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.dto.AddressDto;
import com.scb.rider.model.dto.RiderProfileDto;
import com.scb.rider.model.enumeration.Platform;
import com.scb.rider.repository.RiderProfileRepository;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.UnsupportedEncodingException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@Log4j2
@ActiveProfiles(value="test")
public class RiderDeviceControllerStepDefinitions {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    static final String URL = "/profile";
    private String GET_URL = "";
    private String POST_URL = "";
    private MvcResult result;
    @Autowired
    private RiderProfileRepository riderProfileRepository;
    private String profileId;
    private String id;

    @Before
    public void setUp() {
        Optional<RiderProfile> riderProfile = riderProfileRepository.findByPhoneNumber("9718456515");
        if (!riderProfile.isPresent()) {
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
            long number = (long) Math.floor(Math.random() * 9_000_000_000L) + 1_000_000_000L;
            long accNo = (long) Math.floor(Math.random() * 9_000_000_000L) + 1_000_000_000L;
            
            RiderProfileDto riderProfileDto = RiderProfileDto.builder()
                    .accountNumber(accNo+"")
                    .address(addressDto)
                    .consentAcceptFlag(true)
                    .dataSharedFlag(true)
                    .firstName("Rohit").lastName("Sharma")
                    .nationalID("97184565156")
                    .dob("20/12/1988")
                    .phoneNumber(number+"")
                    .build();
            
            RiderPreferredZones riderPreferredZones = new RiderPreferredZones();
            riderPreferredZones.setPreferredZoneId("2");
            riderPreferredZones.setPreferredZoneName("Bangkok");
            riderProfileDto.setRiderPreferredZones(riderPreferredZones);
            
            RiderProfile riderProfileSave = new RiderProfile();
            BeanUtils.copyProperties(riderProfileDto, riderProfileSave);
            riderProfileSave = riderProfileRepository.save(riderProfileSave);
            profileId = riderProfileSave.getId();

        }else {
            profileId = riderProfile.get().getId();
        }
    }

    @Given("Set End Point to create Rider Device Information")
    public void set_end_point_to_create_rider_device_information() {
        POST_URL = URL + "/" + profileId + "/device";
    }

    @When("Send a POST HTTP request with profile id and rider device information")
    public void send_a_post_http_request_with_profile_id_and_rider_device_information() throws Exception {
        RiderDeviceDetails riderDevice = RiderDeviceDetails.builder()
                .deviceToken("eeQxRHRhTqWVqEfDdWY5OS:APA91bHUKAw5jrMiYmpw-YVQEm7VNEL-_PrsONhBAMmGIbdnX8GNqKIiCyA_Q3Rcz7AgoPXJyF8P9knJJWoV_VMdOOirrzUPJuE_B6DbeSYEOYNRB78ZoZLij05N1KI_Aj8f5HZyeP2I")
                .platform(Platform.GCM)
                .build();
        String json = objectMapper.writeValueAsString(riderDevice);
        // execute
        result = mockMvc.perform(MockMvcRequestBuilders.post(POST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print()).andReturn();
    }

    @Then("I receive valid HTTP Response Code {int} and Rider Device information with id")
    public void i_receive_valid_http_response_code_and_rider_device_information_with_id(Integer int1) throws UnsupportedEncodingException, JsonProcessingException {
        int status = result.getResponse().getStatus();
        assertEquals(status, int1, "Incorrect Response Status");
        assertEquals(HttpStatus.CREATED.value(), int1, "Incorrect Response Status");

        RiderDeviceDetails riderDeviceDetails = objectMapper.readValue(result.getResponse().getContentAsString(),
                RiderDeviceDetails.class);
        assertNotNull(riderDeviceDetails);
        assertNotNull(riderDeviceDetails.getId());
        id = riderDeviceDetails.getId();
        log.info("license id " + id);
    }

    @Given("Set GET End point for Rider Device by rider profile id")
    public void set_get_end_point_for_rider_device_by_rider_profile_id() {
        GET_URL = URL + "/" + profileId + "/device";

    }

    @When("Send a GET HTTP request with rider profile id to fetch rider device information")
    public void send_a_get_http_request_with_rider_profile_id_to_fetch_rider_device_information() throws Exception {
        result = mockMvc
                .perform(
                        get(GET_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andReturn();
    }
    @Then("I receive valid HTTP GET Response Code {int} and Rider Device information with id")
    public void i_receive_valid_http_get_response_code_and_rider_device_information_with_id(Integer int1) throws UnsupportedEncodingException, JsonProcessingException {
        int status = result.getResponse().getStatus();
        assertEquals(status, int1, "Incorrect Response Status");
        assertEquals(HttpStatus.OK.value(), int1, "Incorrect Response Status");

        RiderDeviceDetails riderDeviceDetails = objectMapper.readValue(result.getResponse().getContentAsString(),
                RiderDeviceDetails.class);
        assertNotNull(riderDeviceDetails);
        assertNotNull(riderDeviceDetails.getId());
        id = riderDeviceDetails.getId();
        log.info("license id " + id);
    }
}
