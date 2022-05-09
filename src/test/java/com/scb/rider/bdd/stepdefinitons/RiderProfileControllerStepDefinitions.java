package com.scb.rider.bdd.stepdefinitons;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.rider.model.document.AppConfig;
import com.scb.rider.model.document.RiderDrivingLicenseDocument;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.document.RiderSelectedTrainingAppointment;
import com.scb.rider.model.document.RiderVehicleRegistrationDocument;
import com.scb.rider.model.dto.AddressDto;
import com.scb.rider.model.dto.NationalAddressDto;
import com.scb.rider.model.dto.RiderIdList;
import com.scb.rider.model.dto.RiderProfileDto;
import com.scb.rider.model.dto.RiderStatusDto;
import com.scb.rider.model.enumeration.AvailabilityStatus;
import com.scb.rider.model.enumeration.MandatoryCheckStatus;
import com.scb.rider.model.enumeration.RiderStatus;
import com.scb.rider.model.enumeration.RiderTrainingStatus;
import com.scb.rider.model.enumeration.TrainingType;
import com.scb.rider.repository.AppConfigRepository;
import com.scb.rider.repository.RiderDrivingLicenseDocumentRepository;
import com.scb.rider.repository.RiderProfileRepository;
import com.scb.rider.repository.RiderTrainingAppointmentRepository;
import com.scb.rider.repository.RiderVehicleRegistrationRepository;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.log4j.Log4j2;

@Log4j2
@ActiveProfiles(value="test")
@AutoConfigureMockMvc(addFilters = false)
public class RiderProfileControllerStepDefinitions {
    private String RIDER_PROFILE_ID = "";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    static final String URL = "/";
    private String POST_URL = "";
    private String PUT_URL = "";
    private String GET_URL = "";
    private String UPDATE_RIDER_STATUS_PUT_URL = "";
    private String UPDATE_NATIONAL_ID_STATUS_PUT_URL = "";
    private String UPDATE_PROFILE_PHOTO_STATUS_PUT_URL = "";
    public static String id = "";
    private static final int CURRENT_APP_VERSION = 1;
    MvcResult result;
    private RiderProfileDto riderProfileDto;
    @Autowired
    private RiderProfileRepository riderProfileRepository;
    @Autowired
    private RiderVehicleRegistrationRepository riderVehicleRegistrationRepository;
    @Autowired
    private RiderDrivingLicenseDocumentRepository riderDrivingLicenseDocumentRepository;
    @Autowired
    private RiderTrainingAppointmentRepository riderTrainingAppointmentRepository;
    @Autowired
    private AppConfigRepository appConfigRepository;

    @Before
    public void setUp() {
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

        NationalAddressDto nationalAddressDto = NationalAddressDto.builder()
                .alley("alley1").district("district")
                .floor("1").buildingName("building1")
                .neighbourhood("neighbour").number("2")
                .postalCode("12345").subdistrict("sub")
                .district("dist").road("road")
                .roomNumber("456").province("pro").build();

        riderProfileDto = RiderProfileDto.builder()
                .accountNumber("1212121212121212")
                .address(addressDto)
                .nationalAddress(nationalAddressDto)
                .consentAcceptFlag(true)
                .dataSharedFlag(true)
                .firstName("Rohit").lastName("Sharma")
                .nationalID("1234567899")
                .dob("28/12/1988")
                .phoneNumber("9999999998")
                .status(RiderStatus.AUTHORIZED)
                .availabilityStatus(AvailabilityStatus.Inactive)
                .build();

        appConfigRepository.deleteAll();
        appConfigRepository.save(AppConfig.builder().version(CURRENT_APP_VERSION).forceUpdate(Boolean.TRUE).build());
    }


    @Given("Set POST Rider Profile service api endpoint")
    public void set_post_rider_profile_service_api_endpoint() {
        POST_URL = URL + "profile";
    }

    @When("Send a POST request with rider profile")
    public void send_a_post_http_request() throws Exception {
        String json = objectMapper.writeValueAsString(riderProfileDto);
        // execute
        result = mockMvc.perform(MockMvcRequestBuilders.post(POST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print()).andReturn();
    }

    @Then("I receive valid HTTP Status Code {int}")
    public void i_receive_valid_http_response_code(Integer int1) throws UnsupportedEncodingException, JsonProcessingException {
        int status = result.getResponse().getStatus();
        assertEquals(status, int1, "Incorrect Response Status");
        assertEquals(HttpStatus.CREATED.value(), int1, "Incorrect Response Status");

        // verify that service method was called once
        //verify(percentageConfigureService).save(any(PercentageConfigure.class));

        RiderProfileDto riderProfileResponse = objectMapper.readValue(result.getResponse().getContentAsString(),
                RiderProfileDto.class);
        assertNotNull(riderProfileResponse);
        assertNotNull(riderProfileResponse.getId());
        assertNotNull(riderProfileResponse.getRiderId());
        id = riderProfileResponse.getId();
        log.info("license id " + id);

    }

    @Given("Set PUT Rider Profile service api endpoint")
    public void set_put_rider_profile_service_api_endpoint() {
        // Write code here that turns the phrase above into concrete actions
        riderProfileDto.setId(id);
        PUT_URL = URL + "profile";
    }

    @When("Send a PUT request")
    public void send_a_put_http_request() throws Exception {
        String json = objectMapper.writeValueAsString(riderProfileDto);
        // execute
        result = mockMvc.perform(MockMvcRequestBuilders.put(PUT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print()).andReturn();


    }

    @Then("I receive valid Updated Details and HTTP Status Code {int}")
    public void i_receive_valid_updated_details_and_http_response_code(Integer int1) throws UnsupportedEncodingException, JsonProcessingException {
        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.OK.value(), status, "Incorrect Response Status");
        assertEquals(HttpStatus.OK.value(), int1, "Incorrect Response Status");

        // verify that service methoCREATEDd was called once
        //verify(percentageConfigureService).save(any(PercentageConfigure.class));

        RiderProfileDto riderProfileResponse = objectMapper.readValue(result.getResponse().getContentAsString(),
                RiderProfileDto.class);
        assertNotNull(riderProfileResponse);
        assertNotNull(riderProfileResponse.getId());
        id = riderProfileResponse.getId();
        log.info("license id " + id);

    }

    @Given("Set GET Rider Profile by id service api endpoint")
    public void set_get_rider_profile_by_id_service_api_endpoint() {
        RIDER_PROFILE_ID = URL + "profile" + "/" + id;
    }

    @When("Send a GET HTTP request with profile id")
    public void send_a_get_http_request_with_profile_id() throws Exception {
        // Write code here that turns the phrase above into concrete actions
        log.info("license id " + id);
        result = mockMvc
                .perform(
                        get(RIDER_PROFILE_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andReturn();
    }

    @Then("I receive valid HTTP Response Code {int} and Rider Profile by Id")
    public void i_receive_valid_http_response_code_and_rider_profile_by_id(Integer int1) throws Exception {
        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.OK.value(), status, "Incorrect Response Status");
        assertEquals(HttpStatus.OK.value(), int1, "Incorrect Response Status");

        @SuppressWarnings("unchecked")
        RiderProfileDto riderProfileResponse = objectMapper
                .readValue(result.getResponse().getContentAsString(), RiderProfileDto.class);

        assertNotNull(riderProfileResponse, "Rider Profile is not found");
    }

    @Given("Set GET Rider Status by id service api endpoint")
    public void set_status_service_api_endpoint() {
        PUT_URL = "";
        PUT_URL = URL + "profile/" + id + "/status/" + AvailabilityStatus.Active.name();
    }

    @When("Send a GET HTTP request with status")
    public void send_a_get_http_request_with_status() throws Exception {
        // Write code here that turns the phrase above into concrete actions
        log.info("license id " + id);
        result = mockMvc
                .perform(
                        put(PUT_URL)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("appVersion", CURRENT_APP_VERSION)
                                .content(""))
                .andDo(print()).andReturn();
    }

    @Then("I receive valid HTTP Response Code {int} for status")
    public void i_receive_valid_http_response_code_for_status(Integer int1) throws Exception {
        int status = result.getResponse().getStatus();
        assertEquals(HttpStatus.OK.value(), status, "Incorrect Response Status");
    }

    @Given("Set GET End Point for Riders Ids on the basis of availability status and zone")
    public void set_get_end_point_for_riders_ids_on_the_basis_of_availability_status_and_zone() {
        GET_URL = URL + "profile";
    }

    @When("Send a GET HTTP request with status and zone")
    public void send_a_get_http_request_with_status_and_zone() throws Exception {
        result = mockMvc
                .perform(
                        get(GET_URL)
                                .param("status", "Active")
                                .param("zoneId", "1234")
                                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andReturn();
    }

    @Then("I receive valid HTTP Response Code {int} for status and response list")
    public void i_receive_valid_http_response_code_for_status_and_response_list(Integer int1) throws UnsupportedEncodingException, JsonProcessingException {
        int status = result.getResponse().getStatus();
        assertEquals(status, int1, "Incorrect Response Status");
        assertEquals(HttpStatus.OK.value(), int1, "Incorrect Response Status");

        RiderIdList riderDeviceDetails = objectMapper.readValue(result.getResponse().getContentAsString(),
                RiderIdList.class);
        assertNotNull(riderDeviceDetails);
    }

    @Given("Set PUT Update Rider Status")
    public void set_put_update_rider_status() {
        UPDATE_RIDER_STATUS_PUT_URL = URL + "profile/status";
    }

    @When("Send a PUT HTTP request with riderId and status")
    public void send_a_put_http_request_with_rider_id_and_status() throws Exception {
        Optional<RiderProfile> riderProfile = riderProfileRepository.findById(id);

        if(riderProfile.isPresent()) {
            riderProfile.get().setNationalIdStatus(MandatoryCheckStatus.APPROVED);
            riderProfile.get().setProfilePhotoStatus(MandatoryCheckStatus.APPROVED);
            riderProfileRepository.save(riderProfile.get());

            Random r = new Random();
            Optional<RiderDrivingLicenseDocument> drivingLicense = riderDrivingLicenseDocumentRepository.findByRiderProfileId(id);
            if(drivingLicense.isPresent()) {
                drivingLicense.get().setStatus(MandatoryCheckStatus.APPROVED);
                riderDrivingLicenseDocumentRepository.save(drivingLicense.get());
            } else {
                String drivingLicenseNo = String.format("%d", r.nextInt(999999999));
                RiderDrivingLicenseDocument riderDrivingLicenseDocument = RiderDrivingLicenseDocument.builder()
                        .riderProfileId(id)
                        .status(MandatoryCheckStatus.APPROVED)
                        .documentUrl("random url")
                        .dateOfIssue(LocalDate.now())
                        .dateOfExpiry(LocalDate.now().plusYears(10l))
                        .drivingLicenseNumber(drivingLicenseNo)
                        .build();
                riderDrivingLicenseDocumentRepository.save(riderDrivingLicenseDocument);
            }

            Optional<RiderVehicleRegistrationDocument> vehicleRegistration = riderVehicleRegistrationRepository.findByRiderProfileId(id);
            if(vehicleRegistration.isPresent()) {
                vehicleRegistration.get().setStatus(MandatoryCheckStatus.APPROVED);
                riderVehicleRegistrationRepository.save(vehicleRegistration.get());
            } else {
                String registrationNo = String.format("%d", r.nextInt(999999999));
                String registrationCardId = String.format("%d", r.nextInt(999999999));
                RiderVehicleRegistrationDocument riderVehicleRegistrationDocument = RiderVehicleRegistrationDocument.builder()
                        .riderProfileId(id)
                        .status(MandatoryCheckStatus.APPROVED)
                        .registrationNo(registrationNo)
                        .registrationCardId(registrationCardId)
                        .registrationDate(LocalDate.now())
                        .expiryDate(LocalDate.now().plusYears(10l))
                        .build();
                riderVehicleRegistrationRepository.save(riderVehicleRegistrationDocument);
            }
            
            Optional<RiderSelectedTrainingAppointment> riderTrainingAppointment = riderTrainingAppointmentRepository.findByRiderIdAndTrainingType(id, TrainingType.FOOD);
            if(riderTrainingAppointment.isPresent()) {
                riderTrainingAppointment.get().setTrainingType(TrainingType.FOOD);
            	riderTrainingAppointment.get().setStatus(RiderTrainingStatus.COMPLETED);
                riderTrainingAppointmentRepository.save(riderTrainingAppointment.get());
            } else {
                RiderSelectedTrainingAppointment riderSelectedTrainingAppointment = RiderSelectedTrainingAppointment.builder()
                        .riderId(id)
                        .trainingType(TrainingType.FOOD)
                        .status(RiderTrainingStatus.COMPLETED)
                        .appointmentId("123")
                        .venue("Australia")
                        .startTime(LocalTime.now())
                        .endTime(LocalTime.now().plusHours(1l))
                        .date(LocalDate.now())
                        .build();
                riderTrainingAppointmentRepository.save(riderSelectedTrainingAppointment);
            }
        }

        RiderStatusDto riderStatusDto = RiderStatusDto.builder().profileId(id).status(RiderStatus.AUTHORIZED).build();
        String json = objectMapper.writeValueAsString(riderStatusDto);
        result = mockMvc.perform(put(UPDATE_RIDER_STATUS_PUT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print()).andReturn();
    }

    @Then("I receive valid HTTP Response Code {int} and rider status details")
    public void i_receive_valid_http_response_code_and_rider_status_details(Integer statusCode) throws UnsupportedEncodingException, JsonProcessingException {
        int status = result.getResponse().getStatus();
        assertEquals(status, statusCode, "Incorrect Response Status");
        assertEquals(HttpStatus.OK.value(), statusCode, "Incorrect Response Status");
        RiderStatusDto riderResponse = objectMapper.readValue(result.getResponse().getContentAsString(), RiderStatusDto.class);
        assertNotNull(riderResponse);
        assertNotNull(riderResponse.getProfileId());
        id = riderResponse.getProfileId();
    }

    @Given("Set PUT Update Rider National Id status")
    public void set_put_update_rider_national_id_status() {
        UPDATE_NATIONAL_ID_STATUS_PUT_URL = URL + "profile/"+id+"/national-id-status/"+MandatoryCheckStatus.APPROVED;
    }

    @When("Send a PUT HTTP request with riderId and nationalId status")
    public void send_a_put_http_request_with_rider_id_and_national_id_status() throws Exception {
        Optional<RiderProfile> riderProfile = riderProfileRepository.findById(id);
        riderProfile.get().setNationalIdStatus(MandatoryCheckStatus.PENDING);
        riderProfileRepository.save(riderProfile.get());

        result = mockMvc.perform(put(UPDATE_NATIONAL_ID_STATUS_PUT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andReturn();
    }

    @Then("I receive valid HTTP Response Code {int} and isUpdated flag")
    public void i_receive_valid_http_response_code_and_is_updated_flag(Integer statusCode) {
        int status = result.getResponse().getStatus();
        assertEquals(status, statusCode, "Incorrect Response Status");
        assertEquals(HttpStatus.OK.value(), statusCode, "Incorrect Response Status");
    }

    @Given("Set PUT Update Rider profile photo status")
    public void set_put_update_rider_profile_photo_status() {
        UPDATE_PROFILE_PHOTO_STATUS_PUT_URL = URL + "profile/"+id+"/profile-photo-status/"+MandatoryCheckStatus.APPROVED;
    }

    @When("Send a PUT HTTP request with riderId and profile photo status")
    public void send_a_put_http_request_with_rider_id_and_profile_photo_status() throws Exception {
        Optional<RiderProfile> riderProfile = riderProfileRepository.findById(id);
        riderProfile.get().setProfilePhotoStatus(MandatoryCheckStatus.PENDING);
        riderProfileRepository.save(riderProfile.get());
        result = mockMvc.perform(put(UPDATE_PROFILE_PHOTO_STATUS_PUT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andReturn();
    }

    @Then("I receive valid HTTP Response Code {int} and isStatusUpdated flag")
    public void i_receive_valid_http_response_code_and_is_status_updated_flag(Integer statusCode) {
        int status = result.getResponse().getStatus();
        assertEquals(status, statusCode, "Incorrect Response Status");
        assertEquals(HttpStatus.OK.value(), statusCode, "Incorrect Response Status");
    }

}
