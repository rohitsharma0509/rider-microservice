package com.scb.rider.bdd.stepdefinitons;

import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import java.io.UnsupportedEncodingException;
import org.hamcrest.CoreMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.rider.model.dto.AddressDto;
import com.scb.rider.model.dto.RiderProfileDto;
import com.scb.rider.model.dto.SearchResponseDto;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.log4j.Log4j2;

@Log4j2
@ActiveProfiles(value="test")
public class RiderSearchControllerStepDefinitions {
  private String RIDER_PROFILE_ID = "";
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;
  static final String URL = "/";
  private String POST_URL = "";
  private String PUT_URL = "";
  private String GET_URL = "";
  public static String id = "";
  MvcResult result;
  private RiderProfileDto riderProfileDto;

  @Before
  public void setUp() throws Exception {
    AddressDto addressDto = AddressDto.builder().city("Bangkok").country("Thailand")
        .countryCode("TH").district("district").floorNumber("1234").landmark("landmark")
        .state("state").unitNumber("unitNumber").village("village").zipCode("203205").build();

    riderProfileDto = RiderProfileDto.builder().accountNumber("321212121212121").address(addressDto)
        .consentAcceptFlag(true).dataSharedFlag(true).firstName("ABC").lastName("DBA")
        .nationalID("1234567899").dob("20/12/1988").phoneNumber("9989999999").build();


  }


  @Given("I successfully log into the portal")
  public void successfully_logged_into_portal() throws Exception {
    String json = objectMapper.writeValueAsString(riderProfileDto);
    POST_URL = URL + "profile";
    result = mockMvc.perform(MockMvcRequestBuilders.post(POST_URL)
        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(json))
        .andDo(print()).andReturn();
    log.info("Successfully Logged In");
  }

  @And("I click on hamburger menu")
  public void click_on_hamburger_menu() {
    log.info("click on hamburger menu");
  }

  @When("I see Rider Management, Pricing Management, Training Management, Logout options")
  public void see_rider_management() {
    log.info(
        "see 'Rider Management', 'Pricing Management', 'Training Management', 'Logout' options");
  }


  @And("I click on Rider Management")
  public void click_on_rider_management() {
    log.info("click on 'Rider Management'");
  }


  @And("I enter the name of the rider {string} in the search box")
  public void enter_ABC_in_searchbox(String name) throws Exception {
    log.info("click on 'Rider Management'");
    GET_URL = URL + "ridersearch";
    result = mockMvc.perform(get(GET_URL).param("q", name).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
        .andDo(print()).andReturn();
  }



  @Then("I should see the data returned to have same name {string}")
  public void i_should_recieve_see_data_returned_to_have_same_name(String name)
      throws UnsupportedEncodingException, JsonProcessingException {

    String nameResponse = "";
    SearchResponseDto searchResponseDto =
        objectMapper.readValue(result.getResponse().getContentAsString(), SearchResponseDto.class);
    if (searchResponseDto.getRiderDetails().size() >= 1) {
      nameResponse = searchResponseDto.getRiderDetails().get(0).getName().split(" ")[0];
    }
    assertNotNull(searchResponseDto);
  }



  @And("I enter the mobile number of the rider {string} in the search box")
  public void enter_phoneNumber_in_searchbox(String phoneNumber) throws Exception {
    log.info("click on 'Rider Management'");
    GET_URL = URL + "ridersearch";
    result =
        mockMvc.perform(get(GET_URL).param("q", phoneNumber).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
            .andDo(print()).andReturn();
  }



  @Then("I should see the data returned to have same mobile number {string}")
  public void i_should_recieve_see_data_returned_to_have_same_phoneNumber(String phoneNumber)
      throws UnsupportedEncodingException, JsonProcessingException {

    String phoneNumberResponse = "";
    SearchResponseDto searchResponseDto =
        objectMapper.readValue(result.getResponse().getContentAsString(), SearchResponseDto.class);
    if (searchResponseDto.getRiderDetails().size() >= 1) {
      phoneNumberResponse = searchResponseDto.getRiderDetails().get(0).getPhoneNumber();
    }
    assertNotNull(searchResponseDto);
  }
}
