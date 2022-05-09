package com.scb.rider.IntegrationTest.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.dto.AddressDto;
import com.scb.rider.model.dto.RiderEVFormDto;
import com.scb.rider.model.dto.RiderProfileDto;
import com.scb.rider.model.enumeration.AvailabilityStatus;
import com.scb.rider.model.enumeration.MandatoryCheckStatus;
import com.scb.rider.model.enumeration.RiderStatus;
import com.scb.rider.repository.RiderProfileRepository;
import com.scb.rider.util.CustomBeanUtils;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RiderEVFormControllerIntegrationTest {

	@LocalServerPort
	private int port;

	public String URL = "/profile";
	private static RiderProfileDto riderProfileDto;
	private static RiderProfile riderProfile;
	private RiderEVFormDto evFormDto;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private RiderProfileRepository riderProfileRepository;

	@BeforeAll
	static void setUp() {
		AddressDto addressDto = AddressDto.builder().city("Bangkok").country("Thailand").countryCode("TH")
				.district("district").floorNumber("1234").landmark("landmark").state("state").unitNumber("unitNumber")
				.village("village").zipCode("203205").build();
		riderProfileDto = RiderProfileDto.builder().accountNumber("12121212121").address(addressDto)
				.consentAcceptFlag(true).dataSharedFlag(true).firstName("Rohit").lastName("Sharma")
				.nationalID("1234567890").dob("20/12/1988").phoneNumber("5555555555").status(RiderStatus.AUTHORIZED)
				.availabilityStatus(AvailabilityStatus.Inactive).build();
		riderProfile = new RiderProfile();
		CustomBeanUtils.copyNonNullProperties(riderProfileDto, riderProfile);
		riderProfile.setFirstName("Sachin");
		riderProfile.setNationalID("123456789");
		riderProfile.setPhoneNumber("5555566");
		riderProfile.setAvailabilityStatus(AvailabilityStatus.Active);
	}

	@Test
	@Order(1)
	public void testSaveRiderEVFormSuccess() throws Exception {
		riderProfile = riderProfileRepository.save(riderProfile);
		evFormDto = RiderEVFormDto.builder().riderProfileId(riderProfile.getId()).documentUrl("documentUrl")
				.status(MandatoryCheckStatus.PENDING).evRentalAgreementNumber("123").build();
		String json = objectMapper.writeValueAsString(evFormDto);
		String url = URL + "/ev-form";
		mockMvc.perform(MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON).content(json)).andDo(print()).andExpect(status().isOk());
	}

	@Test
	@Order(2)
	public void testGetRiderEVFormSuccess() throws Exception {
		String url = URL + "/ev-form/" + riderProfile.getId();
		mockMvc.perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.riderProfileId").value(riderProfile.getId()));
	}
}
