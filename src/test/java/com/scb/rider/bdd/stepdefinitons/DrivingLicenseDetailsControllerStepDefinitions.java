package com.scb.rider.bdd.stepdefinitons;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.rider.model.document.RiderDrivingLicenseDocument;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.dto.RiderDrivingLicenseRequest;
import com.scb.rider.model.dto.RiderDrivingLicenseResponse;
import com.scb.rider.repository.RiderDrivingLicenseDocumentRepository;
import com.scb.rider.repository.RiderProfileRepository;
import io.cucumber.java.Before;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@Log4j2
@ActiveProfiles(value="test")
public class DrivingLicenseDetailsControllerStepDefinitions {
    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    static final String URL ="/profile/";
    private  String POST_URL ="";
    private  String PUT_URL ="";
    private String LICENSE_BY_ID = "";
    private  String LICENSE_BY_PROFILE_ID ="";
    private String id="";
    private static final String ACCOUNT_NO = "121212121212121";
    private static final String DRIVING_LICENSE_NO = "1234";
    MvcResult result;

    @Autowired
    private RiderProfileRepository riderProfileRepository;
    @Autowired
    private RiderDrivingLicenseDocumentRepository drivingLicenseRepository;
    private static String riderId;

    @Given("Set POST Rider Driving License Details service api endpoint")
    public void set_post_rider_driving_license_details_service_api_endpoint() {
        RiderDrivingLicenseDocument riderDrivingLicenseDocument = drivingLicenseRepository.findByDrivingLicenseNumber(DRIVING_LICENSE_NO);
        if(Objects.nonNull(riderDrivingLicenseDocument)) {
            drivingLicenseRepository.deleteByRiderProfileId(riderDrivingLicenseDocument.getRiderProfileId());
        }

        Optional<RiderProfile> rider = riderProfileRepository.findByAccountNumber(ACCOUNT_NO);
        if(rider.isPresent()) {
            riderProfileRepository.delete(rider.get());
        }

        RiderProfile riderProfile = new RiderProfile();
        riderProfile.setAccountNumber(ACCOUNT_NO);
        riderProfile.setCreatedDate(LocalDateTime.now().minusDays(2));
        riderProfile = riderProfileRepository.save(riderProfile);
        riderId = riderProfile.getId();
        POST_URL = URL + riderId + "/license-details";
    }

    @When("Send a POST HTTP request")
    public void send_a_post_http_request() throws Exception {
        // prepare data and mock's behaviour
        RiderDrivingLicenseRequest drivingLicenseDocument = RiderDrivingLicenseRequest.builder()
                .drivingLicenseNumber(DRIVING_LICENSE_NO)
                .dateOfExpiry(LocalDate.now())
                .dateOfIssue(LocalDate.now())
                .typeOfLicense("Permanent")
                .documentUrl("testpng")
                .build();

        String json = objectMapper.writeValueAsString(drivingLicenseDocument);
        // execute
         result = mockMvc.perform(MockMvcRequestBuilders.post(POST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print()).andReturn();
    }

    @Then("I receive valid HTTP Response Code {int}")
    public void i_receive_valid_http_response_code(Integer int1) throws UnsupportedEncodingException, JsonProcessingException {
        int status = result.getResponse().getStatus();
        assertEquals(status, int1, "Incorrect Response Status");
        assertEquals(HttpStatus.CREATED.value(), int1, "Incorrect Response Status");

        RiderDrivingLicenseResponse drivingLicenseResponse = objectMapper.readValue(result.getResponse().getContentAsString(),
                RiderDrivingLicenseResponse.class);
        assertNotNull(drivingLicenseResponse);
        assertNotNull(drivingLicenseResponse.getId());
        assertEquals(DRIVING_LICENSE_NO, drivingLicenseResponse.getDrivingLicenseNumber());
        id = drivingLicenseResponse.getId();
        log.info("license id "+id);

    }

    @Given("Set PUT Rider Driving License Details service api endpoint")
    public void set_put_rider_driving_license_details_service_api_endpoint() {
        // Write code here that turns the phrase above into concrete actions
        PUT_URL =  URL + riderId + "/license-details";
    }

    @When("Send a PUT HTTP request")
    public void send_a_put_http_request() throws Exception {
        // Write code here that turns the phrase above into concrete actions
        RiderDrivingLicenseRequest drivingLicenseDocument = RiderDrivingLicenseRequest.builder()
                .drivingLicenseNumber("1234567")
                .dateOfExpiry(LocalDate.of(2022,12,30))
                .dateOfIssue(LocalDate.of(2012,12,30))
                .typeOfLicense("Permanent")
                .documentUrl("testpng")
                .build();

        String json = objectMapper.writeValueAsString(drivingLicenseDocument);
        // execute
         result = mockMvc.perform(MockMvcRequestBuilders.put(PUT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print()).andReturn();
    }

    @Then("I receive valid Updated Details and HTTP Response Code {int}")
    public void i_receive_valid_updated_details_and_http_response_code(Integer int1) throws UnsupportedEncodingException, JsonProcessingException {
        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.OK.value(), status, "Incorrect Response Status");
        assertEquals(HttpStatus.OK.value(), int1, "Incorrect Response Status");

        RiderDrivingLicenseResponse drivingLicenseResponse = objectMapper.readValue(result.getResponse().getContentAsString(),
                RiderDrivingLicenseResponse.class);
        assertNotNull(drivingLicenseResponse);
        assertNotNull(drivingLicenseResponse.getId());
        assertEquals("1234567", drivingLicenseResponse.getDrivingLicenseNumber(),"Invalid Teacher Name");
        id = drivingLicenseResponse.getId();
        log.info("license id "+id);
    }

    @Given("Set GET Rider Driving License Details by id service api endpoint")
    public void set_get_rider_driving_license_details_by_id_service_api_endpoint() throws Exception {
        LICENSE_BY_PROFILE_ID = URL + riderId + "/license-details";
        result = mockMvc.perform(get(LICENSE_BY_PROFILE_ID).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andReturn();
        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.OK.value(), status, "Incorrect Response Status");

        @SuppressWarnings("unchecked")
        RiderDrivingLicenseResponse riderDrivingLicenseResponse = objectMapper
                .readValue(result.getResponse().getContentAsString(), RiderDrivingLicenseResponse.class);

        LICENSE_BY_ID = URL + "license-details/" + riderDrivingLicenseResponse.getId();
    }

    @When("Send a GET HTTP request with driving license id")
    public void send_a_get_http_request_with_driving_license_id() throws Exception {
        // Write code here that turns the phrase above into concrete actions
        log.info("license id "+id);
         result = mockMvc.perform(get(LICENSE_BY_ID)
                 .contentType(MediaType.APPLICATION_JSON)
                 .accept(MediaType.APPLICATION_JSON))
                 .andDo(print()).andReturn();
    }

    @Then("I receive valid HTTP Response Code {int} and Rider Retails by Id")
    public void i_receive_valid_http_response_code_and_rider_retails_by_id(Integer int1) throws Exception {
        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.OK.value(), status, "Incorrect Response Status");
        assertEquals(HttpStatus.OK.value(), int1, "Incorrect Response Status");

        @SuppressWarnings("unchecked")
        RiderDrivingLicenseResponse riderDrivingLicenseResponse = objectMapper
                .readValue(result.getResponse().getContentAsString(), RiderDrivingLicenseResponse.class);

        assertNotNull(riderDrivingLicenseResponse, "Driving License is not found");
    }

    @Given("Set GET Rider Driving License Details By profile id service api endpoint")
    public void set_get_rider_driving_license_details_by_profile_id_service_api_endpoint() throws UnsupportedEncodingException, JsonProcessingException {
      LICENSE_BY_PROFILE_ID = URL + riderId + "/license-details";
    }

    @When("Send a GET HTTP request with rider profile id")
    public void send_a_get_http_request_with_rider_profile_id() throws Exception {
         result = mockMvc.perform(get(LICENSE_BY_PROFILE_ID)
                .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andReturn();

    }
    @Then("I receive valid HTTP Response Code {int} and Rider Driving License Details by profile id")
    public void i_receive_valid_http_response_code_and_rider_driving_license_details_by_profile_id(Integer int1) throws Exception {
        // verify
        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.OK.value(), status, "Incorrect Response Status");
        assertEquals(HttpStatus.OK.value(), int1, "Incorrect Response Status");

        @SuppressWarnings("unchecked")
        RiderDrivingLicenseResponse riderDrivingLicenseResponse = objectMapper
                .readValue(result.getResponse().getContentAsString(), RiderDrivingLicenseResponse.class);

        assertNotNull(riderDrivingLicenseResponse, "Driving License is not found");
    }
}
