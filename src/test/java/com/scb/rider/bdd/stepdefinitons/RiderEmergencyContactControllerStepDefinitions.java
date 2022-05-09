package com.scb.rider.bdd.stepdefinitons;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.dto.RiderEmergencyContactDto;
import com.scb.rider.repository.RiderProfileRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@Log4j2
@ActiveProfiles(value="test")
public class RiderEmergencyContactControllerStepDefinitions {
    private String RIDER_END_ID = "";
    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    static final String URL ="/";
    private  String POST_URL ="";
    private static String id="";
    MvcResult result;
    private static final String ACCOUNT_NO = "121212121212121";

    @Autowired
    private RiderProfileRepository riderProfileRepository;
    private RiderProfile riderProfile;
    private RiderEmergencyContactDto emergencyContactDto;

    @Given("Set POST Rider Emergency Contact service api endpoint")
    public void set_post_rider_profile_service_api_endpoint() {
        Optional<RiderProfile> rider = riderProfileRepository.findByAccountNumber(ACCOUNT_NO);
        if(rider.isPresent()) {
            riderProfileRepository.delete(rider.get());
        }

        riderProfile = new RiderProfile();
        riderProfile.setAccountNumber(ACCOUNT_NO);
        riderProfile.setCreatedDate(LocalDateTime.now().minusDays(2));
        riderProfile = riderProfileRepository.save(riderProfile);

        emergencyContactDto = RiderEmergencyContactDto.builder()
                .address1("address1")
                .name("Micheal")
                .profileId(riderProfile.getId())
                .relationship("Father")
                .zipCode("12345")
                .district("Bulandshahr")
                .homePhoneNumber("9999999999")
                .mobilePhoneNumber("9999999999")
                .build();
        POST_URL = URL+ "profile/emergency-contact";
    }

    @When("Send a POST Emergency Contact request")
    public void send_a_post_http_request() throws Exception {
        String json = objectMapper.writeValueAsString(emergencyContactDto);
         result = mockMvc.perform(MockMvcRequestBuilders.post(POST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print()).andReturn();
    }

    @Then("I receive valid HTTP Status Code for emergency contact {int}")
    public void i_receive_valid_http_response_code(Integer int1) throws UnsupportedEncodingException, JsonProcessingException {
        int status = result.getResponse().getStatus();
        assertEquals(status, int1, "Incorrect Response Status");
        assertEquals(HttpStatus.OK.value(), int1, "Incorrect Response Status");

        RiderEmergencyContactDto riderResponse = objectMapper.readValue(result.getResponse().getContentAsString(),
                RiderEmergencyContactDto.class);
        assertNotNull(riderResponse);
        assertNotNull(riderResponse.getProfileId());
        id = riderResponse.getProfileId();
        log.info("license id "+id);

    }

    @Given("Set GET Rider Emergency Contact by id service api endpoint")
    public void set_get_rider_profile_by_id_service_api_endpoint() {
        RIDER_END_ID = URL +"profile"+"/emergency-contact/" + id;
    }

    @When("Send a GET HTTP request with Emergency Contact id")
    public void send_a_get_http_request_with_profile_id() throws Exception {
        // Write code here that turns the phrase above into concrete actions
        log.info("license id "+id);
         result = mockMvc
                .perform(
                        get(RIDER_END_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andReturn();
    }

    @Then("I receive valid HTTP Response Code {int} and Rider Emergency Contact by Id")
    public void i_receive_valid_http_response_code_and_rider_profile_by_id(Integer int1) throws Exception {
        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.OK.value(), status, "Incorrect Response Status");
        assertEquals(HttpStatus.OK.value(), int1, "Incorrect Response Status");

        RiderEmergencyContactDto riderResponse = objectMapper
                .readValue(result.getResponse().getContentAsString(), RiderEmergencyContactDto.class);

        assertNotNull(riderResponse, "Rider Emergency Contact is not found");
    }

}
