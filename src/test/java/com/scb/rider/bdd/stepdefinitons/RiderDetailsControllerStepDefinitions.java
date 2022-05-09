package com.scb.rider.bdd.stepdefinitons;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.dto.AddressDto;
import com.scb.rider.model.dto.RiderDetailsDto;
import com.scb.rider.model.dto.RiderProfileDto;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@Log4j2
@ActiveProfiles(value="test")
public class RiderDetailsControllerStepDefinitions {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    static final String URL = "/";
    private String GET_URL = "";
    private MvcResult result;
    @Autowired
    private RiderProfileRepository riderProfileRepository;
    private RiderProfile riderProfile;
    private String riderId;

    @Before
    public void setUp() {
        Optional<RiderProfile> checkriderProfile = riderProfileRepository.findByPhoneNumber("9718456515");
        if (!checkriderProfile.isPresent()) {
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

            RiderProfileDto riderProfileDto = RiderProfileDto.builder()
                    .accountNumber("9718456515")
                    .address(addressDto)
                    .consentAcceptFlag(true)
                    .dataSharedFlag(true)
                    .firstName("Rohit").lastName("Sharma")
                    .nationalID("9718456515")
                    .dob("20/12/1988")
                    .phoneNumber("9718456515")
                    .build();
            RiderProfile riderProfileSave = new RiderProfile();
            BeanUtils.copyProperties(riderProfileDto, riderProfileSave);
            riderProfile = riderProfileRepository.save(riderProfileSave);
        } else {
            riderProfile = checkriderProfile.get();
        }
    }

    @Given("Set GET Rider Details by id service api endpoint")
    public void set_get_rider_details_by_id_service_api_endpoint() {
        GET_URL = URL + "profile/details/" + riderProfile.getId();
    }

    @When("Send a GET HTTP request with profile id for rider details")
    public void send_a_get_http_request_with_profile_id_for_rider_details() throws Exception {
        result = mockMvc
                .perform(
                        get(GET_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andReturn();
    }

    @Then("I receive valid HTTP Response Code {int} and Rider Details by Id")
    public void i_receive_valid_http_response_code_and_rider_details_by_id(Integer int1) throws Exception {
        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.OK.value(), status, "Incorrect Response Status");
        assertEquals(HttpStatus.OK.value(), int1, "Incorrect Response Status");

        @SuppressWarnings("unchecked")
        RiderDetailsDto riderResponse = objectMapper
                .readValue(result.getResponse().getContentAsString(), RiderDetailsDto.class);

        assertNotNull(riderResponse, "Rider Details is not found");
    }

    @Given("Set GET Rider Details by phoneNumber service api endpoint")
    public void set_get_rider_profile_by_id_service_api_endpoint() {
        GET_URL = URL + "profile/details/mob/" + riderProfile.getPhoneNumber();
    }

    @When("Send a GET HTTP request with rider phoneNumber for rider details")
    public void send_a_get_http_request_with_profile_id() throws Exception {
        // Write code here that turns the phrase above into concrete actions
        log.info("phoneNumber " + riderProfile.getPhoneNumber());
        result = mockMvc
                .perform(
                        get(GET_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andReturn();
    }

    @Then("I receive valid HTTP Response Code {int} and Rider Details by phoneNumber")
    public void i_receive_valid_http_response_code_and_rider_profile_by_id(Integer int1) throws Exception {
        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.OK.value(), status, "Incorrect Response Status");
        assertEquals(HttpStatus.OK.value(), int1, "Incorrect Response Status");

        RiderDetailsDto response = objectMapper.readValue(result.getResponse().getContentAsString(),
                RiderDetailsDto.class);

        assertNotNull(response, "Rider Details is not found");
    }

    @Given("Set GET Rider Details with Documents by id service api endpoint")
    public void set_get_rider_details_with_documents_by_id_service_api_endpoint() {
        GET_URL = URL + "profile/details/riderDocsDetails/" + riderProfile.getId();
    }

    @When("Send a GET HTTP request with rider id for rider details with documents")
    public void send_a_get_http_request_with_rider_id_for_document_details() throws Exception {
        // Write code here that turns the phrase above into concrete actions
        log.info("id " + riderProfile.getId());
        result = mockMvc
                .perform(
                        get(GET_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andReturn();
    }

    @Then("I receive valid HTTP Response Code {int} and Rider Details with documents by id")
    public void i_receive_valid_http_response_code_and_rider_details_with_documents_by_id(Integer int1) throws Exception {
        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.OK.value(), status, "Incorrect Response Status");
        assertEquals(HttpStatus.OK.value(), int1, "Incorrect Response Status");

        RiderDetailsDto response = objectMapper.readValue(result.getResponse().getContentAsString(),
                RiderDetailsDto.class);

        assertNotNull(response, "Rider Details are not found");
    }
}
