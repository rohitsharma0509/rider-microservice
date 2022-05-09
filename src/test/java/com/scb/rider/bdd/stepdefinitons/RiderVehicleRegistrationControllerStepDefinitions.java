package com.scb.rider.bdd.stepdefinitons;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.dto.RiderVehicleRegistrationDetailsRequest;
import com.scb.rider.model.dto.RiderVehicleRegistrationDetailsResponse;
import com.scb.rider.repository.RiderProfileRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@ActiveProfiles(value="test")
public class RiderVehicleRegistrationControllerStepDefinitions {
    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    private RiderProfileRepository riderProfileRepository;
    static final String URL ="/profile/";
    private  String POST_URL ="";
    private  String PUT_URL ="";
    private String GET_URL_BY_PROFILE_ID ="";
    private String id="";
    private static RiderProfile riderProfile = null;
    MvcResult result;

    @Given("Set POST Rider Vehicle Registration Details service api endpoint")
    public void set_post_rider_vehicle_registration_details_service_api_endpoint() {
        riderProfile = riderProfileRepository.findFirstByOrderByPhoneNumberAsc();
        POST_URL = URL+ riderProfile.getId()+"/vehicle-details";

    }

    @When("Send a POST HTTP request for Vehicle Registration Details")
    public void send_a_post_http_request_for_vehicle_registration_details() throws Exception {
        RiderVehicleRegistrationDetailsRequest request = RiderVehicleRegistrationDetailsRequest.builder()
                .registrationNo("1234")
                .expiryDate(LocalDate.of(2022, 12, 30))
                .registrationDate(LocalDate.of(2012, 12, 30))
                .registrationCardId("12344")
                .province("abcd")
                .makerModel("AUDI")
                .uploadedVehicleDocUrl("98121")
                .build();
        String json = objectMapper.writeValueAsString(request);
        // execute
        result = mockMvc.perform(MockMvcRequestBuilders.post(POST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print()).andReturn();

    }

    @Then("I receive valid HTTP Response Code {int} for Vehicle Registration Details")
    public void i_receive_valid_http_response_code_for_vehicle_registration_details(Integer int1) throws UnsupportedEncodingException, JsonProcessingException {
        // verify
        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.CREATED.value(), status, "Incorrect Response Status");
        assertEquals(HttpStatus.CREATED.value(), int1, "Incorrect Response Status");

        RiderVehicleRegistrationDetailsResponse vehicleResponse = objectMapper.readValue(result.getResponse().getContentAsString(),
                RiderVehicleRegistrationDetailsResponse.class);
        assertNotNull(vehicleResponse);
        assertNotNull(vehicleResponse.getId());
        assertEquals("1234", vehicleResponse.getRegistrationNo(), "Invalid Teacher Name");
    }

    @Given("Set PUT Rider Vehicle Registration Details service api endpoint")
    public void set_put_rider_vehicle_registration_details_service_api_endpoint() {
        PUT_URL =  URL + riderProfile.getId()+"/vehicle-details";

    }

    @When("Send a PUT HTTP request for Vehicle Registration Details")
    public void send_a_put_http_request_for_vehicle_registration_details() throws Exception {
        RiderVehicleRegistrationDetailsRequest request = RiderVehicleRegistrationDetailsRequest.builder()
                .registrationNo("1234567")
                .expiryDate(LocalDate.of(2022, 12, 30))
                .registrationDate(LocalDate.of(2012, 12, 30))
                .registrationCardId("12344")
                .province("abcd")
                .makerModel("AUDI")
                .uploadedVehicleDocUrl("98121")
                .build();
        String json = objectMapper.writeValueAsString(request);
        // execute
         result = mockMvc.perform(MockMvcRequestBuilders.put(PUT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print()).andReturn();


    }
    @Then("I receive valid PUT HTTP Response Code {int} for Vehicle Registration Details")
    public void i_receive_valid_put_http_response_code_for_vehicle_registration_details(Integer int1) throws UnsupportedEncodingException, JsonProcessingException {

        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.OK.value(), status, "Incorrect Response Status");
        assertEquals(HttpStatus.OK.value(), int1, "Incorrect Response Status");

        RiderVehicleRegistrationDetailsResponse vehicleResponse = objectMapper.readValue(result.getResponse().getContentAsString(),
                RiderVehicleRegistrationDetailsResponse.class);
        assertNotNull(vehicleResponse);
        assertNotNull(vehicleResponse.getId());
        assertEquals("1234567", vehicleResponse.getRegistrationNo(), "Invalid Teacher Name");

    }
    @Given("Set GET Rider  Vehicle Registration Details By profile id service api endpoint")
    public void set_get_rider_vehicle_registration_details_by_profile_id_service_api_endpoint() {
        GET_URL_BY_PROFILE_ID = URL + riderProfile.getId()+"/vehicle-details";
    }

    @When("Send a GET HTTP request with to get Vehicle Registration Details by profile Id")
    public void send_a_get_http_request_with_to_get_vehicle_registration_details_by_profile_id() throws Exception {
         result = mockMvc
                .perform(
                        get(GET_URL_BY_PROFILE_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andReturn();

    }

    @Then("I receive valid HTTP Response Code {int} and Rider Vehicle Registration Details by profile id")
    public void i_receive_valid_http_response_code_and_rider_vehicle_registration_details_by_profile_id(Integer int1) throws UnsupportedEncodingException, JsonProcessingException {
        // verify
        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.OK.value(), status, "Incorrect Response Status");

        @SuppressWarnings("unchecked")
        RiderVehicleRegistrationDetailsResponse vehicleRegistrationDetailsResponse = objectMapper
                .readValue(result.getResponse().getContentAsString(), RiderVehicleRegistrationDetailsResponse.class);

        assertNotNull(vehicleRegistrationDetailsResponse, "Driving License is not found");

    }


}
