package com.scb.rider.bdd.stepdefinitons;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.Random;

import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.repository.RiderProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.rider.model.dto.RiderBackgroundVerificationDetailsRequest;
import com.scb.rider.model.dto.RiderBackgroundVerificationDetailsResponse;
import com.scb.rider.model.enumeration.MandatoryCheckStatus;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

@ActiveProfiles(value="test")
public class RiderBackgroundVerificationDetailsControllerStepDefinitions {

	@Autowired
	protected MockMvc mockMvc;
	@Autowired
	protected ObjectMapper objectMapper;
	MvcResult result;

	static final String URL = "/profile/";
	private static String POST_URL = "";
	private static String PUT_URL = "";
	private static String GET_URL_BY_PROFILE_ID = "";
	private static String riderId;
	private static Random r = new Random();
	private static final String ACCOUNT_NO = "121212121212121";

	@Autowired
	private RiderProfileRepository riderProfileRepository;
	private RiderProfile riderProfile;

	@Given("Set POST Rider Background Verification service api endpoint")
	public void set_post_rider_background_verification_service_api_endpoint() {
		Optional<RiderProfile> rider = riderProfileRepository.findByAccountNumber(ACCOUNT_NO);
		if(rider.isPresent()) {
			riderProfileRepository.delete(rider.get());
		}

		riderProfile = new RiderProfile();
		riderProfile.setAccountNumber(ACCOUNT_NO);
		riderProfile.setCreatedDate(LocalDateTime.now().minusDays(2));
		riderProfile = riderProfileRepository.save(riderProfile);

		riderId = riderProfile.getId();
		POST_URL = URL + riderId + "/background-details";
	}

	@When("Send a POST HTTP request for Background Verification Details")
	public void send_a_post_http_request_for_background_verification_details() throws Exception {
		RiderBackgroundVerificationDetailsRequest request = RiderBackgroundVerificationDetailsRequest.builder()
				.status(MandatoryCheckStatus.PENDING).dueDate(LocalDate.of(2022, 12, 30)).documentUrls(Arrays.asList("temp url"))
				.build();
		String json = objectMapper.writeValueAsString(request);
		result = mockMvc.perform(MockMvcRequestBuilders.post(POST_URL).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON).content(json)).andDo(print()).andReturn();
	}

	@Then("I receive valid HTTP Response Code {int} for Background Verification Details")
	public void i_receive_valid_http_response_code_for_background_verification_details(Integer statusCode)
			throws JsonMappingException, JsonProcessingException, UnsupportedEncodingException {
		int status = result.getResponse().getStatus();
		assertEquals(HttpStatus.CREATED.value(), status, "Incorrect Response Status");
		assertEquals(HttpStatus.CREATED.value(), statusCode, "Incorrect Response Status");

		RiderBackgroundVerificationDetailsResponse backgroundVerificationResponse = objectMapper
				.readValue(result.getResponse().getContentAsString(), RiderBackgroundVerificationDetailsResponse.class);
		assertNotNull(backgroundVerificationResponse);
		assertNotNull(backgroundVerificationResponse.getId());
		assertEquals(riderId, backgroundVerificationResponse.getRiderProfileId(), "Invalid RiderId");
		assertEquals(MandatoryCheckStatus.PENDING, backgroundVerificationResponse.getStatus(),
				"Invalid Background Verification Status");
	}

	@Given("Set PUT Rider Background Verification service api endpoint")
	public void set_put_rider_background_verification_service_api_endpoint() {
		PUT_URL = URL + riderId + "/background-details";
	}

	@When("Send a PUT HTTP request for Background Verification Details")
	public void send_a_put_http_request_for_background_verification_details() throws Exception {
		RiderBackgroundVerificationDetailsRequest request = RiderBackgroundVerificationDetailsRequest.builder()
				.status(MandatoryCheckStatus.APPROVED).dueDate(LocalDate.of(2022, 12, 30)).documentUrls(Arrays.asList("temp url"))
				.build();
		String json = objectMapper.writeValueAsString(request);
		result = mockMvc.perform(MockMvcRequestBuilders.put(PUT_URL).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON).content(json)).andDo(print()).andReturn();
	}

	@Then("I receive valid PUT HTTP Response Code {int} for Background Verification Details")
	public void i_receive_valid_put_http_response_code_for_background_verification_details(Integer statusCode)
			throws JsonMappingException, JsonProcessingException, UnsupportedEncodingException {
		int status = result.getResponse().getStatus();
		assertEquals(HttpStatus.OK.value(), status, "Incorrect Response Status");
		assertEquals(HttpStatus.OK.value(), statusCode, "Incorrect Response Status");

		RiderBackgroundVerificationDetailsResponse backgroundVerificationResponse = objectMapper
				.readValue(result.getResponse().getContentAsString(), RiderBackgroundVerificationDetailsResponse.class);
		assertNotNull(backgroundVerificationResponse);
		assertNotNull(backgroundVerificationResponse.getId());
		assertEquals(riderId, backgroundVerificationResponse.getRiderProfileId(), "Invalid RiderId");
		assertEquals(MandatoryCheckStatus.APPROVED, backgroundVerificationResponse.getStatus(),
				"Invalid Background Verification");
	}

	@Given("Set GET Rider  Background Verification Details By profile id service api endpoint")
	public void set_get_rider_background_verification_details_by_profile_id_service_api_endpoint() {
		GET_URL_BY_PROFILE_ID = URL + riderId + "/background-details";
	}

	@When("Send a GET HTTP request with to get Background Verification Details by profile Id")
	public void send_a_get_http_request_with_to_get_background_verification_details_by_profile_id() throws Exception {
		result = mockMvc.perform(get(GET_URL_BY_PROFILE_ID).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andDo(print())
				.andReturn();
	}

	@Then("I receive valid HTTP Response Code {int} and Rider Background Verification Details by profile id")
	public void i_receive_valid_http_response_code_and_rider_background_verification_details_by_profile_id(
			Integer statusCode) throws JsonMappingException, JsonProcessingException, UnsupportedEncodingException {
		int status = result.getResponse().getStatus();
		assertEquals(HttpStatus.OK.value(), status, "Incorrect Response Status");

		RiderBackgroundVerificationDetailsResponse backgroundVerificationDetailsResponse = objectMapper
				.readValue(result.getResponse().getContentAsString(), RiderBackgroundVerificationDetailsResponse.class);

		assertNotNull(backgroundVerificationDetailsResponse, "background Verification Response should not be null.");
		assertEquals(riderId, backgroundVerificationDetailsResponse.getRiderProfileId(), "Invalid RiderId");
		assertEquals(MandatoryCheckStatus.APPROVED, backgroundVerificationDetailsResponse.getStatus(),
				"Invalid Background Verification");
	}

}
