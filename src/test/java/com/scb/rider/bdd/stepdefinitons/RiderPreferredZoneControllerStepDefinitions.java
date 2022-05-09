package com.scb.rider.bdd.stepdefinitons;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.rider.model.document.RiderPreferredZones;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.dto.RiderPreferredZoneDto;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@Log4j2
@ActiveProfiles(value="test")
public class RiderPreferredZoneControllerStepDefinitions {

	@Autowired
	protected MockMvc mockMvc;
	@Autowired
	protected ObjectMapper objectMapper;
	static final String URL = "/";
	private String POST_URL = "";
	@Autowired
	private RiderProfileRepository riderProfileRepository;

	MvcResult result;
	private static RiderPreferredZoneDto preferredZoneDto;

	@Before
	public void setUp() {
		preferredZoneDto = RiderPreferredZoneDto.builder().riderProfileId("123").preferredZoneId("1")
				.preferredZoneName("Bangkok").build();
	}

	@Given("Set POST Rider Preferred Zone service api endpoint")
	public void set_post_rider_preferred_zone_service_api_endpoint() {
		RiderPreferredZones riderPreferredZones = RiderPreferredZones.builder()
				.preferredZoneName("")
				.build();
		RiderProfile rider = riderProfileRepository.findFirstByOrderByPhoneNumberAsc();
		rider.setRiderPreferredZones(riderPreferredZones);
		riderProfileRepository.save(rider);
		preferredZoneDto.setRiderProfileId(rider.getId());
		POST_URL = URL + "profile/preferred-zone";
	}

	@When("Send a POST request for Preferred Zone")
	public void send_a_post_http_request() throws Exception {
		String json = objectMapper.writeValueAsString(preferredZoneDto);
		// execute
		result = mockMvc.perform(MockMvcRequestBuilders.post(POST_URL).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON).content(json)).andDo(print()).andReturn();
	}

	@Then("I receive valid HTTP Status Code for Preferred Zone {int}")
	public void i_receive_valid_http_response_code(Integer int1)
			throws UnsupportedEncodingException, JsonProcessingException {
		int status = result.getResponse().getStatus();
		assertEquals(status, int1, "Incorrect Response Status");
		assertEquals(HttpStatus.OK.value(), int1, "Incorrect Response Status");

		RiderPreferredZoneDto riderPreferredZoneResponse = objectMapper
				.readValue(result.getResponse().getContentAsString(), RiderPreferredZoneDto.class);
		assertNotNull(riderPreferredZoneResponse);
		assertNotNull(riderPreferredZoneResponse.getPreferredZoneId());

	}

}
