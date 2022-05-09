package com.scb.rider.IntegrationTest.controller;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.util.Optional;

import com.scb.rider.model.dto.NationalAddressDto;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.rider.model.document.RiderPreferredZones;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.dto.AddressDto;
import com.scb.rider.model.dto.RiderProfileDto;
import com.scb.rider.model.dto.SearchResponseDto;
import com.scb.rider.model.enumeration.AvailabilityStatus;
import com.scb.rider.repository.RiderProfileRepository;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RiderSearchControllerIntegrationTest {

  private static RiderProfileDto riderProfileDto;
  private static RiderProfileDto riderProfileDto2;
  private static RiderProfile riderProfile;
  private static RiderPreferredZones riderPreferredZone;
  @LocalServerPort
  private int port;
  @Autowired
  private RiderProfileRepository riderProfileRepository;
  private String URL = "/profile";
  private String URLSearch = "/ridersearch";
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private MockMvc mockMvc;

  @BeforeAll
  static void setUp() {
    AddressDto addressDto = AddressDto.builder().city("Bangkok").country("Thailand")
        .countryCode("TH").district("district").floorNumber("1234").landmark("landmark")
        .state("state").unitNumber("unitNumber").village("village").zipCode("203205").build();


    NationalAddressDto nationalAddressDto = NationalAddressDto.builder().alley("alley1").district("district")
            .floor("1").buildingName("building1").neighbourhood("neighbour").number("2").postalCode("12345").subdistrict("sub")
            .district("dist").road("road").roomNumber("456").province("pro").build();


    riderProfileDto = RiderProfileDto.builder().accountNumber("121212121212121").address(addressDto)
            .nationalAddress(nationalAddressDto)
        .consentAcceptFlag(true).dataSharedFlag(true).firstName("John").lastName("Smith")
        .nationalID("1234567890").dob("20/12/1988").phoneNumber("5555555555").build();

    riderProfileDto2 = RiderProfileDto.builder().accountNumber("2121212121").address(addressDto)
            .nationalAddress(nationalAddressDto)
        .consentAcceptFlag(true).dataSharedFlag(true).firstName("Steven").lastName("Smith")
        .nationalID("14567890").dob("20/12/1988").phoneNumber("55555555").build();

    riderProfile = new RiderProfile();
    riderProfile.setFirstName("John");
    riderProfile.setNationalID("123456789000");
    riderProfile.setPhoneNumber("5555555566");
    riderProfile.setAvailabilityStatus(AvailabilityStatus.Active);

  }

  @Test
  void whenValidInput_thenReturns200() throws Exception {
    MvcResult result =
        mockMvc.perform(get(URL + "/" + riderProfileDto.getId()).contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON))
            .andDo(print()).andReturn();
    assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
  }

  @Test
  @Order(1)
  void deleteData() throws JsonProcessingException {
    Optional<RiderProfile> rider = riderProfileRepository.findByAccountNumber("121212121212121");
    if (rider.isPresent() && StringUtils.isNotBlank(rider.get().getId())) {
      riderProfileRepository.delete(rider.get());
    }
    assertNotNull(riderProfileDto);
  }

  @Test
  @Order(2)
  void save_thenReturns201() throws Exception {
    String json = objectMapper.writeValueAsString(riderProfileDto);
    MvcResult result =
        mockMvc.perform(MockMvcRequestBuilders.post(URL).contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON).content(json)).andDo(print()).andReturn();
    assertEquals(HttpStatus.CREATED.value(), result.getResponse().getStatus());
    
    RiderProfileDto profileResult =
        objectMapper.readValue(result.getResponse().getContentAsString(), RiderProfileDto.class);
    assertNotNull(profileResult.getId());
    riderProfileDto.setId(profileResult.getId());
  }

  @Test
  @Order(3)
  void save_thenReturns201Second() throws Exception {
    String json = objectMapper.writeValueAsString(riderProfileDto2);
    MvcResult result =
        mockMvc.perform(MockMvcRequestBuilders.post(URL).contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON).content(json)).andDo(print()).andReturn();
    assertTrue(HttpStatus.CREATED.value() == result.getResponse().getStatus()
        || HttpStatus.NOT_FOUND.value() == result.getResponse().getStatus());
  }


  @ParameterizedTest
  @CsvSource(value = {"Steven,1", "Smith,2"})
  void get_Rider_Profile_Using_Term(String term, String totalCount) throws Exception {

    String statusURl = URLSearch + "/" + "?q=" + term;

    MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(statusURl)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(""))
        .andDo(print()).andReturn();
    SearchResponseDto searchResult =
        objectMapper.readValue(result.getResponse().getContentAsString(), SearchResponseDto.class);

    assertNotNull(searchResult.getTotalCount());
    assertEquals(searchResult.getTotalCount(), Long.valueOf(totalCount));
    assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());

  }

  @ParameterizedTest
  @ValueSource(strings = {"viewby:unAuthorized", "viewby:allRiders"})
  void get_Rider_Profile_Using_Filters(String filter) throws Exception {

    String statusURl = URLSearch + "/" + "?q=";

    MvcResult result = mockMvc
        .perform(MockMvcRequestBuilders.get(statusURl).param("filterquery", filter)
            .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(""))
        .andDo(print()).andReturn();
    SearchResponseDto searchResult =
        objectMapper.readValue(result.getResponse().getContentAsString(), SearchResponseDto.class);

    assertNotNull(searchResult.getTotalCount());
    assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());

  }

  @ParameterizedTest
  @ValueSource(strings = {"viewby:suspended", "viewby:authorized","viewby:riderOnActiveJob","viewby:appointmentId,name:abc xyz","viewby:riderOnTrainingToday"})
  void get_Rider_Profile_Using_StatusFilters(String filter) throws Exception {

    String statusURl = URLSearch + "/" + "?q=";

    MvcResult result = mockMvc
        .perform(MockMvcRequestBuilders.get(statusURl).param("filterquery", filter)
            .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(""))
        .andDo(print()).andReturn();
    SearchResponseDto searchResult =
        objectMapper.readValue(result.getResponse().getContentAsString(), SearchResponseDto.class);

    assertNotNull(searchResult.getTotalCount());
    assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());

  }
  
  @ParameterizedTest
  @ValueSource(strings = {"viewby:riderOnActiveJob,date:20/2"})
  void get_Rider_Profile_with_invalid_date_format(String filter) throws Exception {

    String statusURl = URLSearch + "/" + "?q=";

    MvcResult result = mockMvc
        .perform(MockMvcRequestBuilders.get(statusURl).param("filterquery", filter)
            .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(""))
        .andDo(print()).andReturn();
   
    assertEquals("Invalid Date Format", result.getResolvedException().getMessage());

  }
}
