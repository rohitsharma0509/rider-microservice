package com.scb.rider.IntegrationTest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.dto.RiderPreferredZoneDto;
import com.scb.rider.repository.RiderProfileRepository;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RiderPreferredZoneControllerIntegrationTest {

	@LocalServerPort
	private int port;

	private String URL = "/profile/preferred-zone";

	private static RiderPreferredZoneDto riderPreferredZoneDto;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private RiderProfileRepository riderProfileRepository;

	@BeforeAll
	static void setUp() {
		riderPreferredZoneDto = RiderPreferredZoneDto.builder().preferredZoneId("1")
				.preferredZoneName("Bangkok").build();
	}

	@Test
	@Order(1)
	void save_thenReturns200() throws Exception {
		RiderProfile riderprofile = riderProfileRepository.findFirstByOrderByPhoneNumberAsc();
		if(ObjectUtils.isNotEmpty(riderprofile)){
			riderPreferredZoneDto.setRiderProfileId(riderprofile.getId());
		}
		Assumptions.assumeTrue(StringUtils.isNotBlank(riderPreferredZoneDto.getRiderProfileId()));
		String json = objectMapper.writeValueAsString(riderPreferredZoneDto);
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(URL).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON).content(json)).andDo(print()).andReturn();		

	}

}
