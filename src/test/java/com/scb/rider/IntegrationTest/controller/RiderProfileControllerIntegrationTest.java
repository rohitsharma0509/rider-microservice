package com.scb.rider.IntegrationTest.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.rider.client.PocketServiceFeignClient;
import com.scb.rider.constants.Constants;
import com.scb.rider.model.document.*;
import com.scb.rider.model.dto.*;
import com.scb.rider.model.enumeration.*;
import com.scb.rider.repository.*;
import feign.FeignException;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RiderProfileControllerIntegrationTest {
    private static final int CURRENT_APP_VERSION = 1;
    private static RiderProfileDto riderProfileDto;
    private static RiderProfile riderProfile;
    private static RiderDrivingLicenseDocument riderDrivingLicenseDocument;
    private static RiderVehicleRegistrationDocument riderVehicleRegistrationDocument;
    private static RiderSelectedTrainingAppointment riderSelectedTrainingAppointment;
    private static RiderPreferredZones riderPreferredZone;
    private static RiderEVForm riderEVForm;
    @LocalServerPort
    private int port;
    @Autowired
    private AppConfigRepository appConfigRepository;
    @Autowired
    private RiderProfileRepository riderProfileRepository;
    @Autowired
    private RiderVehicleRegistrationRepository vehicleRegistrationRepository;
    @Autowired
    private RiderDrivingLicenseDocumentRepository riderDrivingLicenseDocumentRepository;
    @Autowired
    private RiderTrainingAppointmentRepository riderTrainingAppointmentRepository;
    @Autowired
    private RiderEVFormRepository riderEVFormRepository;
    private String URL = "/profile";
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private PocketServiceFeignClient pocketServiceFeignClient;

    @BeforeAll
    static void setUp() {

        NationalAddressDto nationalAddressDto = NationalAddressDto.builder().alley("alley1").district("district")
                .floor("1").buildingName("building1").neighbourhood("neighbour").number("2").postalCode("12345").subdistrict("sub")
                .district("dist").road("road").roomNumber("456").province("pro").build();

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

        riderProfileDto = RiderProfileDto.builder()
                .accountNumber("121212121212121")
                .address(addressDto)
                .nationalAddress(nationalAddressDto)
                .consentAcceptFlag(true)
                .dataSharedFlag(true)
                .firstName("Rohit")
                .lastName("Sharma")
                .nationalID("1234567890")
                .dob("20/12/1988")
                .phoneNumber("5555555555")
                .status(RiderStatus.AUTHORIZED)
                .availabilityStatus(AvailabilityStatus.Inactive)
                .build();

        riderProfile = new RiderProfile();
        riderProfile.setFirstName("Sachin");
        riderProfile.setNationalID("123456789000");
        riderProfile.setPhoneNumber("5555555566");
        riderProfile.setAvailabilityStatus(AvailabilityStatus.Active);
        riderProfile.setMannerScoreCurrent(10);
        riderProfile.setMannerScoreInitial(10);
    }

    @Test
    void whenValidInput_thenReturns200() throws Exception {
    	MvcResult result = mockMvc
                .perform(
                        get(URL + "/" + riderProfileDto.getId())
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andReturn();
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }

    @Test
    @Order(1)
    void deleteData() throws JsonProcessingException {
        Optional<RiderProfile> rider = riderProfileRepository.findByAccountNumber("121212121212121");
        if (rider.isPresent() && StringUtils.isNotBlank(rider.get().getId())) {
            riderProfileRepository.delete(rider.get());
            riderDrivingLicenseDocumentRepository.deleteByRiderProfileId(rider.get().getId());
            vehicleRegistrationRepository.deleteByRiderProfileId(rider.get().getId());
            riderTrainingAppointmentRepository.deleteByRiderId(rider.get().getId());
            riderEVFormRepository.findByRiderProfileId(rider.get().getId());
        }
        assertNotNull(riderProfileDto);
        appConfigRepository.deleteAll();
    }

    @Test
    @Order(2)
    void save_thenReturns201() throws Exception {
        String json = objectMapper.writeValueAsString(riderProfileDto);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print()).andReturn();
        assertEquals(HttpStatus.CREATED.value(), result.getResponse().getStatus());
        RiderProfileDto profileResult = objectMapper.readValue(result.getResponse().getContentAsString(),
                RiderProfileDto.class);
        assertNotNull(profileResult.getId());
        assertNotNull(profileResult.getRiderId());
        riderProfileDto.setId(profileResult.getId());
        riderProfile.setId(profileResult.getId());
        appConfigRepository.save(AppConfig.builder().version(CURRENT_APP_VERSION).forceUpdate(Boolean.TRUE).build());
    }

    @Test
    @Order(3)
    void saveRiderDrivingLicenseDetails() {
        Random r = new java.util.Random();
        String drivingLicenseNo = String.format("%d", r.nextInt(999999999));
        riderDrivingLicenseDocument = RiderDrivingLicenseDocument.builder()
                .riderProfileId(riderProfileDto.getId())
                .status(MandatoryCheckStatus.PENDING)
                .documentUrl("random url")
                .dateOfIssue(LocalDate.now())
                .dateOfExpiry(LocalDate.now().plusYears(10l))
                .drivingLicenseNumber(drivingLicenseNo)
                .build();
        riderDrivingLicenseDocument = riderDrivingLicenseDocumentRepository.save(riderDrivingLicenseDocument);
    }

    @Test
    @Order(4)
    void saveRiderVehicleRegistrationDetails() {
        Random r = new Random();
        String registrationNo = String.format("%d", r.nextInt(999999999));
        String registrationCardId = String.format("%d", r.nextInt(999999999));
        riderVehicleRegistrationDocument = RiderVehicleRegistrationDocument.builder()
                .riderProfileId(riderProfileDto.getId())
                .status(MandatoryCheckStatus.PENDING)
                .registrationNo(registrationNo)
                .registrationCardId(registrationCardId)
                .registrationDate(LocalDate.now())
                .expiryDate(LocalDate.now().plusYears(10l))
                .build();
        riderVehicleRegistrationDocument = vehicleRegistrationRepository.save(riderVehicleRegistrationDocument);
    }
    
    @Test
    @Order(5)
    void saveRiderTrainingDetails() {
        riderSelectedTrainingAppointment = RiderSelectedTrainingAppointment.builder()
                .riderId(riderProfileDto.getId())
                .trainingType(TrainingType.FOOD)
                .status(RiderTrainingStatus.COMPLETED)
                .appointmentId("123")
                .venue("Australia")
                .startTime(LocalTime.now())
                .endTime(LocalTime.now().plusHours(1l))
                .date(LocalDate.now())
                .build();
        riderSelectedTrainingAppointment = riderTrainingAppointmentRepository.save(riderSelectedTrainingAppointment);
    }

    @Test
    @Order(6)
    void saveRiderEVForm() {
        Random r = new java.util.Random();
        riderEVForm = RiderEVForm.builder()
                .riderProfileId(riderProfileDto.getId())
                .status(MandatoryCheckStatus.APPROVED)
                .documentUrl("random url")
                .build();
        riderEVForm = riderEVFormRepository.save(riderEVForm);
    }

    @Test
    void update_thenReturns200() throws Exception {
        riderProfileDto.setPhoneNumber(null);
        String json = objectMapper.writeValueAsString(riderProfileDto);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print()).andReturn();
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        RiderProfileDto profileResult = objectMapper.readValue(result.getResponse().getContentAsString(),
                RiderProfileDto.class);
        assertNotNull(profileResult.getId());
        riderProfileDto.setPhoneNumber("5555555555");
    }

    @Test
    void update_status_thenReturns200() throws Exception {
        String statusURl = URL + "/" + riderProfileDto.getId() + "/status/" + AvailabilityStatus.Active.name();
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(statusURl)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("appVersion", CURRENT_APP_VERSION)
                .content(""))
                .andDo(print()).andReturn();
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }

    @Test
    void get_Rider_Id_List_By_Availability_Status_Success200() throws Exception {
        riderPreferredZone = new RiderPreferredZones();
        riderPreferredZone.setPreferredZoneId("123");
        riderPreferredZone.setPreferredZoneName("Zone-1");
        riderProfile.setRiderPreferredZones(riderPreferredZone);
        riderProfile = riderProfileRepository.save(riderProfile);


        String statusURl = URL + "/" + "?zoneId=" + "123";

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(statusURl)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(""))
                .andDo(print()).andReturn();

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());

    }

    @Test
    void get_Rider_Id_List_By_Availability_Status_Success_WithStatus() throws Exception {

        riderPreferredZone = new RiderPreferredZones();
        riderPreferredZone.setPreferredZoneId("123");
        riderPreferredZone.setPreferredZoneName("Zone-1");
        riderProfile.setRiderPreferredZones(riderPreferredZone);
        riderProfile = riderProfileRepository.save(riderProfile);

        String statusURl = URL + "/" + "?zoneId=" + "123" + "&status=" + AvailabilityStatus.Active;

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(statusURl)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(""))
                .andDo(print()).andReturn();

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());

    }

    @Test
    void get_Rider_Id_List_By_Availability_Status_Success_WithStatusInactive() throws Exception {

        riderPreferredZone = new RiderPreferredZones();
        riderPreferredZone.setPreferredZoneId("123");
        riderPreferredZone.setPreferredZoneName("Zone-1");
        riderProfile.setRiderPreferredZones(riderPreferredZone);
        riderProfile = riderProfileRepository.save(riderProfile);

        String statusURl = URL + "/" + "?zoneId=" + "123" + "&status=" + AvailabilityStatus.Inactive;

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(statusURl)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(""))
                .andDo(print()).andReturn();

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());

    }

    @Test
    void get_Rider_Id_List_By_Availability_Status_Success_With_No_Jone_IdAvailable() throws Exception {

        riderPreferredZone = new RiderPreferredZones();
        riderPreferredZone.setPreferredZoneId("123");
        riderPreferredZone.setPreferredZoneName("Zone-1");
        riderProfile.setRiderPreferredZones(riderPreferredZone);
        riderProfile = riderProfileRepository.save(riderProfile);

        String statusURl = URL + "/" + "?zoneId=" + "1234" + "&status=" + AvailabilityStatus.Inactive;

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(statusURl)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(""))
                .andDo(print()).andReturn();

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());

    }

    @Test
    void get_Rider_Id_List_By_Availability_Status_Failure_400() throws Exception {

        riderPreferredZone = new RiderPreferredZones();
        riderPreferredZone.setPreferredZoneId("123");
        riderPreferredZone.setPreferredZoneName("Zone-1");
        riderProfile.setRiderPreferredZones(riderPreferredZone);
        riderProfile = riderProfileRepository.save(riderProfile);

        String statusURl = URL;

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(statusURl)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(""))
                .andDo(print()).andReturn();

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());

    }

    @Test
    void updateRiderStatusWhenRiderNotExistThenReturn404() throws Exception {
        String statusUrl = URL + "/status";
        RiderStatusDto riderStatusDto = RiderStatusDto.builder().profileId("randomProfileId").status(RiderStatus.AUTHORIZED).build();
        String json = objectMapper.writeValueAsString(riderStatusDto);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print()).andReturn();
        assertEquals(HttpStatus.NOT_FOUND.value(), result.getResponse().getStatus());
    }

    @Test
    void updateRiderStatusWhenSuspendingAUnauthorizedRider() throws Exception {
        String statusUrl = URL + "/status";

        riderProfile.setStatus(RiderStatus.UNAUTHORIZED);
        riderProfile = riderProfileRepository.save(riderProfile);

        RiderStatusDto riderStatusDto = RiderStatusDto.builder().profileId(riderProfileDto.getId()).status(RiderStatus.SUSPENDED).build();
        String json = objectMapper.writeValueAsString(riderStatusDto);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print()).andReturn();
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
    }

    @Test
    void updateRiderStatusWhenStatusCannotBeChangedThenReturn400() throws Exception {
        String statusUrl = URL + "/status";

        riderProfile.setStatus(RiderStatus.AUTHORIZED);
        riderProfile = riderProfileRepository.save(riderProfile);

        RiderStatusDto riderStatusDto = RiderStatusDto.builder().profileId(riderProfileDto.getId()).status(RiderStatus.UNAUTHORIZED).build();
        String json = objectMapper.writeValueAsString(riderStatusDto);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print()).andReturn();
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
    }

    @Test
    void updateRiderStatusWhenRiderAlreadyInGivenStatusThenReturn400() throws Exception {
        String statusUrl = URL + "/status";

        riderProfile.setStatus(RiderStatus.AUTHORIZED);
        riderProfile = riderProfileRepository.save(riderProfile);

        RiderStatusDto riderStatusDto = RiderStatusDto.builder().profileId(riderProfileDto.getId()).status(RiderStatus.AUTHORIZED).build();
        String json = objectMapper.writeValueAsString(riderStatusDto);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print()).andReturn();
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
    }

    @Test
    void updateRiderStatusWhenSuspendingRiderWithoutReasonThenReturn400() throws Exception {
        String statusUrl = URL + "/status";

        riderProfile.setStatus(RiderStatus.AUTHORIZED);
        riderProfile = riderProfileRepository.save(riderProfile);

        RiderStatusDto riderStatusDto = RiderStatusDto.builder().profileId(riderProfileDto.getId()).status(RiderStatus.SUSPENDED).build();
        String json = objectMapper.writeValueAsString(riderStatusDto);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print()).andReturn();
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
    }

    @Test
    void RDTC_640_updateRiderStatusWhenNationalIdNotApprovedThenReturn400() throws Exception {
        String statusUrl = URL + "/status";

        riderProfile.setStatus(RiderStatus.UNAUTHORIZED);
        riderProfile.setNationalIdStatus(MandatoryCheckStatus.PENDING);
        riderProfile = riderProfileRepository.save(riderProfile);

        RiderStatusDto riderStatusDto = RiderStatusDto.builder().profileId(riderProfileDto.getId()).status(RiderStatus.AUTHORIZED).build();
        String json = objectMapper.writeValueAsString(riderStatusDto);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("NationalID not approved for id " + riderProfileDto.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0]").value("Mandatory checks are missing"))
                .andDo(print()).andReturn();
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
    }

    @Test
    void RDTC_641_updateRiderStatusWhenSelfieNotApprovedThenReturn400() throws Exception {
        String statusUrl = URL + "/status";

        riderProfile.setStatus(RiderStatus.UNAUTHORIZED);
        riderProfile.setNationalIdStatus(MandatoryCheckStatus.APPROVED);
        riderProfile.setProfilePhotoStatus(MandatoryCheckStatus.PENDING);
        riderProfile = riderProfileRepository.save(riderProfile);

        RiderStatusDto riderStatusDto = RiderStatusDto.builder().profileId(riderProfileDto.getId()).status(RiderStatus.AUTHORIZED).build();
        String json = objectMapper.writeValueAsString(riderStatusDto);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Selfie not approved for id " + riderProfileDto.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0]").value("Mandatory checks are missing"))
                .andDo(print()).andReturn();
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
    }

    @Test
    void RDTC_644_updateRiderStatusWhenDrivingLicenseNotApprovedThenReturn400() throws Exception {
        String statusUrl = URL + "/status";

        riderProfile.setStatus(RiderStatus.UNAUTHORIZED);
        riderProfile.setNationalIdStatus(MandatoryCheckStatus.APPROVED);
        riderProfile.setProfilePhotoStatus(MandatoryCheckStatus.APPROVED);
        riderProfile = riderProfileRepository.save(riderProfile);

        riderDrivingLicenseDocument.setStatus(MandatoryCheckStatus.PENDING);
        riderDrivingLicenseDocument = riderDrivingLicenseDocumentRepository.save(riderDrivingLicenseDocument);

        RiderStatusDto riderStatusDto = RiderStatusDto.builder().profileId(riderProfileDto.getId()).status(RiderStatus.AUTHORIZED).build();
        String json = objectMapper.writeValueAsString(riderStatusDto);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Driving License not approved for id " + riderProfileDto.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0]").value("Mandatory checks are missing"))
                .andDo(print()).andReturn();
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
    }

    @Test
    void RDTC_651_updateRiderStatusWhenVehicleRegistrationNotApprovedThenReturn400() throws Exception {
        String statusUrl = URL + "/status";

        riderProfile.setStatus(RiderStatus.UNAUTHORIZED);
        riderProfile.setNationalIdStatus(MandatoryCheckStatus.APPROVED);
        riderProfile.setProfilePhotoStatus(MandatoryCheckStatus.APPROVED);
        riderProfile = riderProfileRepository.save(riderProfile);

        riderDrivingLicenseDocument.setStatus(MandatoryCheckStatus.APPROVED);
        riderDrivingLicenseDocument = riderDrivingLicenseDocumentRepository.save(riderDrivingLicenseDocument);
        riderVehicleRegistrationDocument.setStatus(MandatoryCheckStatus.PENDING);
        riderVehicleRegistrationDocument = vehicleRegistrationRepository.save(riderVehicleRegistrationDocument);

        RiderStatusDto riderStatusDto = RiderStatusDto.builder().profileId(riderProfileDto.getId()).status(RiderStatus.AUTHORIZED).build();
        String json = objectMapper.writeValueAsString(riderStatusDto);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Vehicle Registration not approved for id " + riderProfileDto.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0]").value("Mandatory checks are missing"))
                .andDo(print()).andReturn();
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
    }
    
    @Test
    void RDTC_649_updateRiderStatusWhenTrainingNotCompletedThenReturn200() throws Exception {
        String statusUrl = URL + "/status";

        riderProfile.setStatus(RiderStatus.UNAUTHORIZED);
        riderProfile.setNationalIdStatus(MandatoryCheckStatus.APPROVED);
        riderProfile.setProfilePhotoStatus(MandatoryCheckStatus.APPROVED);
        riderProfile = riderProfileRepository.save(riderProfile);

        riderDrivingLicenseDocument.setStatus(MandatoryCheckStatus.APPROVED);
        riderDrivingLicenseDocument = riderDrivingLicenseDocumentRepository.save(riderDrivingLicenseDocument);
        riderVehicleRegistrationDocument.setStatus(MandatoryCheckStatus.APPROVED);
        riderVehicleRegistrationDocument = vehicleRegistrationRepository.save(riderVehicleRegistrationDocument);
        riderSelectedTrainingAppointment.setStatus(RiderTrainingStatus.NOT_COMPLETED);
        riderSelectedTrainingAppointment = riderTrainingAppointmentRepository.save(riderSelectedTrainingAppointment);
        
        RiderStatusDto riderStatusDto = RiderStatusDto.builder().profileId(riderProfileDto.getId()).status(RiderStatus.AUTHORIZED).build();
        String json = objectMapper.writeValueAsString(riderStatusDto);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Basic Training not completed for id " + riderProfileDto.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0]").value("Mandatory checks are missing"))
                .andDo(print()).andReturn();
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
    }

    @Test
    void updateRiderStatusThenReturn200() throws Exception {
        String statusUrl = URL + "/status";

        riderProfile.setStatus(RiderStatus.UNAUTHORIZED);
        riderProfile.setNationalIdStatus(MandatoryCheckStatus.APPROVED);
        riderProfile.setProfilePhotoStatus(MandatoryCheckStatus.APPROVED);
        riderProfile = riderProfileRepository.save(riderProfile);

        riderDrivingLicenseDocument.setStatus(MandatoryCheckStatus.APPROVED);
        riderDrivingLicenseDocument = riderDrivingLicenseDocumentRepository.save(riderDrivingLicenseDocument);
        riderVehicleRegistrationDocument.setStatus(MandatoryCheckStatus.APPROVED);
        riderVehicleRegistrationDocument = vehicleRegistrationRepository.save(riderVehicleRegistrationDocument);
        riderSelectedTrainingAppointment.setStatus(RiderTrainingStatus.COMPLETED);
        riderSelectedTrainingAppointment = riderTrainingAppointmentRepository.save(riderSelectedTrainingAppointment);

        RiderStatusDto riderStatusDto = RiderStatusDto.builder().profileId(riderProfileDto.getId()).status(RiderStatus.AUTHORIZED).build();
        String json = objectMapper.writeValueAsString(riderStatusDto);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print()).andReturn();
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }

    @Test
    void updateNationalIdStatusWhenRiderNotExists() throws Exception {
        String statusUrl = URL + "/randomRiderId/national-id-status/APPROVED";
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andReturn();
        assertEquals(HttpStatus.NOT_FOUND.value(), result.getResponse().getStatus());
    }

    @Test
    void updateNationalIdStatusThenReturn200() throws Exception {
        riderProfile.setNationalIdStatus(MandatoryCheckStatus.PENDING);
        riderProfile = riderProfileRepository.save(riderProfile);
        String statusUrl = URL + "/"+riderProfileDto.getId()+"/national-id-status/APPROVED";
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andReturn();
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }

    @Test
    void updateProfilePhotoStatusWhenRiderNotExists() throws Exception {
        String statusUrl = URL + "/randomRiderId/profile-photo-status/APPROVED";
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andReturn();
        assertEquals(HttpStatus.NOT_FOUND.value(), result.getResponse().getStatus());
    }

    @Test
    void updateProfilePhotoStatusThenReturn200() throws Exception {
        riderProfile.setProfilePhotoStatus(MandatoryCheckStatus.PENDING);
        riderProfile = riderProfileRepository.save(riderProfile);
        String statusUrl = URL + "/"+riderProfileDto.getId()+"/profile-photo-status/APPROVED";
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andReturn();
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }

    @Test
    void updateProfileRejectWithReasonPhotoStatusThenReturn200() throws Exception {
        riderProfile.setProfilePhotoStatus(MandatoryCheckStatus.PENDING);
        riderProfile = riderProfileRepository.save(riderProfile);
        String statusUrl = URL + "/"+riderProfileDto.getId()+"/profile-photo-status/REJECTED";
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                .param("reason","Other")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andReturn();
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }

    @Test
    void updateProfileRejectWithoutReasonPhotoStatusThenReturn200() throws Exception {
        String statusUrl = URL + "/"+riderProfileDto.getId()+"/profile-photo-status/REJECTED";
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andReturn();
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
    }
    
    @Test
    void getProfileStageIntTest() throws Exception {
        String url = URL + "/"+riderProfileDto.getPhoneNumber()+"/stage";
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(url)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andReturn();
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }

    @Test
    void getRiderShortProfile_ViaPhoneNumber() throws Exception{
        String url = URL + "/shortProfile/"+ Constants.PHONE_NUMBER + "/" + riderProfile.getPhoneNumber();
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(100));
    }


    @Test
    void getRiderShortProfile_ViaRiderId() throws Exception{
        String url = URL + "/shortProfile/"+ Constants.RIDER_ID + "/" + riderProfile.getRiderId();
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(100));
    }

    @Test
    void getRiderShortProfile_ViaNationalId() throws Exception{
        String url = URL + "/shortProfile/"+ Constants.NATIONAL_ID + "/" + riderProfile.getNationalID();
        mockMvc.perform(MockMvcRequestBuilders.get(url)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(100));
    }

    @Test
    void updateProfileMannerScoreSuccess() throws Exception {
        riderProfile = riderProfileRepository.save(riderProfile);
        List<String> reasonList = Arrays.asList("แต่กายไม่เหมาะสม", "พูดจาไม่สุภาพ", "ไม่สวมแมส");
        RiderProfileUpdateMannerScoreDto riderMannerScore = RiderProfileUpdateMannerScoreDto.builder()
                .riderId(riderProfile.getRiderId()).actionType("ADD").reason(reasonList).actionScore(2).additionalComment(null).build();
        String json = objectMapper.writeValueAsString(riderMannerScore);
        String statusUrl = URL + "/mannerScore";
        mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void updateProfileMannerScoreWhenWithOutRiderId_RDTC_530() throws Exception {
        String json = "{ \"actionType\": \"ADD\", \"reason\": [\"แต่กายไม่เหมาะสม\", \"พูดจาไม่สุภาพ\", \"ไม่สวมแมส\"],\"actionScore\": 2, \"additionalComment\": null }";
        String statusUrl = URL + "/mannerScore";
        mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Method Argument not valid"));
    }

    @Test
    void updateProfileMannerScoreWhenRiderIdIsNull_RDTC_531() throws Exception {
        String json = "{ \"riderId\": null, \"actionType\": \"ADD\", \"reason\": [\"แต่กายไม่เหมาะสม\", \"พูดจาไม่สุภาพ\", \"ไม่สวมแมส\"],\"actionScore\": 2, \"additionalComment\": null }";
        String statusUrl = URL + "/mannerScore";
        mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Method Argument not valid"));
    }

    @Test
    void updateProfileMannerScoreWhenRiderIdIsEmpty_RDTC_532() throws Exception {
        String json = "{ \"riderId\": \"\", \"actionType\": \"ADD\", \"reason\": [\"แต่กายไม่เหมาะสม\", \"พูดจาไม่สุภาพ\", \"ไม่สวมแมส\"],\"actionScore\": 2, \"additionalComment\": null }";
        String statusUrl = URL + "/mannerScore";
        mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Method Argument not valid"));
    }

    @Test
    void updateProfileMannerScoreWhenRiderIdIsSpace_RDTC_533() throws Exception {
        String json = "{ \"riderId\": \" \", \"actionType\": \"ADD\", \"reason\": [\"แต่กายไม่เหมาะสม\", \"พูดจาไม่สุภาพ\", \"ไม่สวมแมส\"],\"actionScore\": 2, \"additionalComment\": null }";
        String statusUrl = URL + "/mannerScore";
        mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Method Argument not valid"));
    }

    @Test
    void updateProfileMannerScoreWhenWithOutActionType_RDTC_535() throws Exception {
        String json = "{ \"riderId\": \"RR10031\", \"reason\": [\"แต่กายไม่เหมาะสม\", \"พูดจาไม่สุภาพ\", \"ไม่สวมแมส\"],\"actionScore\": 2, \"additionalComment\": null }";
        String statusUrl = URL + "/mannerScore";
        mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Method Argument not valid"));
    }

    @Test
    void updateProfileMannerScoreWhenActionTypeIsNull_RDTC_536() throws Exception {
        String json = "{ \"riderId\": \"RR10031\", \"actionType\": null, \"reason\": [\"แต่กายไม่เหมาะสม\", \"พูดจาไม่สุภาพ\", \"ไม่สวมแมส\"],\"actionScore\": 2, \"additionalComment\": null }";
        String statusUrl = URL + "/mannerScore";
        mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Method Argument not valid"));
    }

    @Test
    void updateProfileMannerScoreWhenActionTypeIsEmpty_RDTC_537() throws Exception {
        String json = "{ \"riderId\": \"RR10031\", \"actionType\": \"\", \"reason\": [\"แต่กายไม่เหมาะสม\", \"พูดจาไม่สุภาพ\", \"ไม่สวมแมส\"],\"actionScore\": 2, \"additionalComment\": null }";
        String statusUrl = URL + "/mannerScore";
        mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Method Argument not valid"));
    }

    @Test
    void updateProfileMannerScoreWhenActionTypeIsADDSUBTRACT_RDTC_544() throws Exception {
        String json = "{ \"riderId\": \"RR10031\", \"actionType\": \"ADDSUBTRACT\", \"reason\": [\"แต่กายไม่เหมาะสม\", \"พูดจาไม่สุภาพ\", \"ไม่สวมแมส\"],\"actionScore\": 2, \"additionalComment\": null }";
        String statusUrl = URL + "/mannerScore";
        mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("actionMannerScore must be [\"ADD\", \"SUBTRACT\"]"));
    }

    @Test
    void updateProfileMannerScoreWhenWithOutReason_RDTC_545() throws Exception {
        String json = "{ \"riderId\": \"RR10031\", \"actionType\": \"ADD\",\"actionScore\": 2, \"additionalComment\": null }";
        String statusUrl = URL + "/mannerScore";
        mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Method Argument not valid"));
    }

    @Test
    void updateProfileMannerScoreWhenReasonIsNull_RDTC_546() throws Exception {
        String json = "{ \"riderId\": \"RR10031\", \"actionType\": \"ADD\", \"reason\": null,\"actionScore\": 2, \"additionalComment\": null }";
        String statusUrl = URL + "/mannerScore";
        mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Method Argument not valid"));
    }

    @Test
    void updateProfileMannerScoreWhenReasonIsEmpty_RDTC_547() throws Exception {
        String json = "{ \"riderId\": \"RR10031\", \"actionType\": \"ADD\", \"reason\": \"\",\"actionScore\": 2, \"additionalComment\": null }";
        String statusUrl = URL + "/mannerScore";
        mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateProfileMannerScoreWhenReasonIsEmptyArray_RDTC_548() throws Exception {
        String json = "{ \"riderId\": \"RR10031\", \"actionType\": \"ADD\", \"reason\": [],\"actionScore\": 2, \"additionalComment\": null }";
        String statusUrl = URL + "/mannerScore";
        mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Reason Property is required"));
    }

    @Test
    void updateProfileMannerScoreWhenReasonIsEmptyString_RDTC_549() throws Exception {
        String json = "{ \"riderId\": \"RR10031\", \"actionType\": \"ADD\", \"reason\": \"\",\"actionScore\": 2, \"additionalComment\": null }";
        String statusUrl = URL + "/mannerScore";
        mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateProfileMannerScoreWhenWithOutActionScore_RDTC_550() throws Exception {
        String json = "{ \"riderId\": \"RR10031\", \"actionType\": \"ADD\", \"reason\": [\"แต่กายไม่เหมาะสม\", \"พูดจาไม่สุภาพ\", \"ไม่สวมแมส\"], \"additionalComment\": null }";
//        String json = "{ \"riderId\": \"RR10031\", \"actionType\": \"ADD\", \"reason\": [\"แต่กายไม่เหมาะสม\", \"พูดจาไม่สุภาพ\", \"ไม่สวมแมส\"],\"actionScore\": 2, \"additionalComment\": null }";
        String statusUrl = URL + "/mannerScore";
        mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Method Argument not valid"));
    }

    @Test
    void updateProfileMannerScoreWhenActionScoreIsNull_RDTC_551() throws Exception {
        String json = "{ \"riderId\": \"RR10031\", \"actionType\": \"ADD\", \"reason\": [\"แต่กายไม่เหมาะสม\", \"พูดจาไม่สุภาพ\", \"ไม่สวมแมส\"],\"actionScore\": null, \"additionalComment\": null }";
        String statusUrl = URL + "/mannerScore";
        mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Method Argument not valid"));
    }

    @Test
    void updateProfileMannerScoreWhenActionScoreIsEmpty_RDTC_552() throws Exception {
        String json = "{ \"riderId\": \"RR10031\", \"actionType\": \"ADD\", \"reason\": [\"แต่กายไม่เหมาะสม\", \"พูดจาไม่สุภาพ\", \"ไม่สวมแมส\"], \"actionScore\": \"\", \"additionalComment\": null }";
        String statusUrl = URL + "/mannerScore";
        mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateProfileMannerScoreWhenActionScoreIsSix_RDTC_554() throws Exception {
        String json = "{ \"riderId\": \"RR10031\", \"actionType\": \"ADD\", \"reason\": [\"แต่กายไม่เหมาะสม\", \"พูดจาไม่สุภาพ\", \"ไม่สวมแมส\"], \"actionScore\": \"SIX\", \"additionalComment\": null }";
        String statusUrl = URL + "/mannerScore";
        mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void RDTC_647_updateRiderStatusWhenEVFormNotApprovedThenReturn400() throws Exception {
        String statusUrl = URL + "/status";

        riderProfile.setStatus(RiderStatus.UNAUTHORIZED);
        riderProfile.setNationalIdStatus(MandatoryCheckStatus.APPROVED);
        riderProfile.setProfilePhotoStatus(MandatoryCheckStatus.APPROVED);
        riderProfile.setEvBikeUser(true);
        riderProfile = riderProfileRepository.save(riderProfile);

        riderDrivingLicenseDocument.setStatus(MandatoryCheckStatus.APPROVED);
        riderDrivingLicenseDocument = riderDrivingLicenseDocumentRepository.save(riderDrivingLicenseDocument);
        riderEVForm.setStatus(MandatoryCheckStatus.PENDING);
        riderEVForm = riderEVFormRepository.save(riderEVForm);

        RiderStatusDto riderStatusDto = RiderStatusDto.builder().profileId(riderProfileDto.getId()).status(RiderStatus.AUTHORIZED).build();
        String json = objectMapper.writeValueAsString(riderStatusDto);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Rider EV Form not approved for id " + riderProfileDto.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0]").value("Mandatory checks are missing"))
                .andDo(print()).andReturn();
    }


    @Test
    void RDTC_653_updateRiderStatusUnauthorizedToSuspendedThenReturn400() throws Exception {
        String statusUrl = URL + "/status";

        riderProfile.setStatus(RiderStatus.UNAUTHORIZED);
        riderProfile.setNationalIdStatus(MandatoryCheckStatus.APPROVED);
        riderProfile.setProfilePhotoStatus(MandatoryCheckStatus.APPROVED);
        riderProfile = riderProfileRepository.save(riderProfile);

        riderDrivingLicenseDocument.setStatus(MandatoryCheckStatus.APPROVED);
        riderDrivingLicenseDocument = riderDrivingLicenseDocumentRepository.save(riderDrivingLicenseDocument);
        riderVehicleRegistrationDocument.setStatus(MandatoryCheckStatus.APPROVED);
        riderVehicleRegistrationDocument = vehicleRegistrationRepository.save(riderVehicleRegistrationDocument);
        riderSelectedTrainingAppointment.setStatus(RiderTrainingStatus.COMPLETED);
        riderSelectedTrainingAppointment = riderTrainingAppointmentRepository.save(riderSelectedTrainingAppointment);

        RiderStatusDto riderStatusDto = RiderStatusDto.builder()
                .profileId(riderProfileDto.getId())
                .status(RiderStatus.SUSPENDED)
                .reason("RDTC-653")
                .build();
        String json = objectMapper.writeValueAsString(riderStatusDto);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Status transition not allowed for id " + riderProfileDto.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0]").value("Status transition not allowed"))
                .andDo(print()).andReturn();
    }

    @Test
    void RDTC_655_updateRiderStatusSuspendedToUnauthorizedThenReturn400() throws Exception {
        String statusUrl = URL + "/status";

        riderProfile.setStatus(RiderStatus.SUSPENDED);
        riderProfile.setNationalIdStatus(MandatoryCheckStatus.APPROVED);
        riderProfile.setProfilePhotoStatus(MandatoryCheckStatus.APPROVED);
        riderProfile = riderProfileRepository.save(riderProfile);

        riderDrivingLicenseDocument.setStatus(MandatoryCheckStatus.APPROVED);
        riderDrivingLicenseDocument = riderDrivingLicenseDocumentRepository.save(riderDrivingLicenseDocument);
        riderVehicleRegistrationDocument.setStatus(MandatoryCheckStatus.APPROVED);
        riderVehicleRegistrationDocument = vehicleRegistrationRepository.save(riderVehicleRegistrationDocument);
        riderSelectedTrainingAppointment.setStatus(RiderTrainingStatus.COMPLETED);
        riderSelectedTrainingAppointment = riderTrainingAppointmentRepository.save(riderSelectedTrainingAppointment);

        RiderStatusDto riderStatusDto = RiderStatusDto.builder()
                .profileId(riderProfileDto.getId())
                .status(RiderStatus.UNAUTHORIZED)
                .reason("RDTC-655")
                .build();
        String json = objectMapper.writeValueAsString(riderStatusDto);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Status transition not allowed for id " + riderProfileDto.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0]").value("Status transition not allowed"))
                .andDo(print()).andReturn();
    }

    @Test
    void RDTC_657_updateRiderStatusWhenRiderIsAuthorizedAndReqStatusIsNULLThenReturn400() throws Exception {
        String statusUrl = URL + "/status";

        riderProfile.setStatus(RiderStatus.AUTHORIZED);
        riderProfile.setNationalIdStatus(MandatoryCheckStatus.APPROVED);
        riderProfile.setProfilePhotoStatus(MandatoryCheckStatus.APPROVED);
        riderProfile = riderProfileRepository.save(riderProfile);

        riderDrivingLicenseDocument.setStatus(MandatoryCheckStatus.APPROVED);
        riderDrivingLicenseDocument = riderDrivingLicenseDocumentRepository.save(riderDrivingLicenseDocument);
        riderVehicleRegistrationDocument.setStatus(MandatoryCheckStatus.APPROVED);
        riderVehicleRegistrationDocument = vehicleRegistrationRepository.save(riderVehicleRegistrationDocument);
        riderSelectedTrainingAppointment.setStatus(RiderTrainingStatus.COMPLETED);
        riderSelectedTrainingAppointment = riderTrainingAppointmentRepository.save(riderSelectedTrainingAppointment);

        String req = "{ \"profileId\": null,\n" +
                "    \"status\": null,\n" +
                "    \"remarks\": \"RDTC-657\",\n" +
                "    \"reason\": null,\n" +
                "    \"suspensionExpiryTime\": \"2022-03-23T09:02:19.864Z\",\n" +
                "    \"suspensionDuration\": 1\n" +
                "}";

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(req))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Method Argument not valid"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0]", Matchers.in(Arrays.asList("profileId: Property should not be blank", "status: Property is required"))))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[1]", Matchers.in(Arrays.asList("profileId: Property should not be blank", "status: Property is required"))))
                .andDo(print()).andReturn();
    }

    @Test
    void RDTC_658_updateRiderStatusWhenRiderIsAuthorizedAndReqStatusIsEMPTYThenReturn400() throws Exception {
        String statusUrl = URL + "/status";

        riderProfile.setStatus(RiderStatus.AUTHORIZED);
        riderProfile.setNationalIdStatus(MandatoryCheckStatus.APPROVED);
        riderProfile.setProfilePhotoStatus(MandatoryCheckStatus.APPROVED);
        riderProfile = riderProfileRepository.save(riderProfile);

        riderDrivingLicenseDocument.setStatus(MandatoryCheckStatus.APPROVED);
        riderDrivingLicenseDocument = riderDrivingLicenseDocumentRepository.save(riderDrivingLicenseDocument);
        riderVehicleRegistrationDocument.setStatus(MandatoryCheckStatus.APPROVED);
        riderVehicleRegistrationDocument = vehicleRegistrationRepository.save(riderVehicleRegistrationDocument);
        riderSelectedTrainingAppointment.setStatus(RiderTrainingStatus.COMPLETED);
        riderSelectedTrainingAppointment = riderTrainingAppointmentRepository.save(riderSelectedTrainingAppointment);

        String req = "{ \"profileId\": \"\",\n" +
                "    \"status\": \"\",\n" +
                "    \"remarks\": \"RDTC-658\",\n" +
                "    \"reason\": \"\",\n" +
                "    \"suspensionExpiryTime\": \"2022-03-23T09:02:19.864Z\",\n" +
                "    \"suspensionDuration\": 1\n" +
                "}";

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(req))
                .andExpect(status().isBadRequest())
                .andDo(print()).andReturn();
    }

    @Test
    void RDTC_660_updateRiderStatusWhenRiderIsAuthorizedAndRequiredFieldsIsInvalidTypeThenReturn400() throws Exception {
        String statusUrl = URL + "/status";

        riderProfile.setStatus(RiderStatus.AUTHORIZED);
        riderProfile.setNationalIdStatus(MandatoryCheckStatus.APPROVED);
        riderProfile.setProfilePhotoStatus(MandatoryCheckStatus.APPROVED);
        riderProfile = riderProfileRepository.save(riderProfile);

        riderDrivingLicenseDocument.setStatus(MandatoryCheckStatus.APPROVED);
        riderDrivingLicenseDocument = riderDrivingLicenseDocumentRepository.save(riderDrivingLicenseDocument);
        riderVehicleRegistrationDocument.setStatus(MandatoryCheckStatus.APPROVED);
        riderVehicleRegistrationDocument = vehicleRegistrationRepository.save(riderVehicleRegistrationDocument);
        riderSelectedTrainingAppointment.setStatus(RiderTrainingStatus.COMPLETED);
        riderSelectedTrainingAppointment = riderTrainingAppointmentRepository.save(riderSelectedTrainingAppointment);

        String req = "{ \"profileId\": 126,\n" +
                "    \"status\": 126,\n" +
                "    \"remarks\": \"RDTC-660\",\n" +
                "    \"reason\": 126,\n" +
                "    \"suspensionExpiryTime\": \"2022-03-23T09:02:19.864Z\",\n" +
                "    \"suspensionDuration\": 1\n" +
                "}";

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(req))
                .andExpect(status().isBadRequest())
                .andDo(print()).andReturn();
    }

    @Test
    void RDTC_661_updateRiderStatusWhenRiderIsSuspendedAndRequiredFieldsIsSPACEThenReturn400() throws Exception {
        String statusUrl = URL + "/status";

        riderProfile.setStatus(RiderStatus.SUSPENDED);
        riderProfile.setNationalIdStatus(MandatoryCheckStatus.APPROVED);
        riderProfile.setProfilePhotoStatus(MandatoryCheckStatus.APPROVED);
        riderProfile = riderProfileRepository.save(riderProfile);

        riderDrivingLicenseDocument.setStatus(MandatoryCheckStatus.APPROVED);
        riderDrivingLicenseDocument = riderDrivingLicenseDocumentRepository.save(riderDrivingLicenseDocument);
        riderVehicleRegistrationDocument.setStatus(MandatoryCheckStatus.APPROVED);
        riderVehicleRegistrationDocument = vehicleRegistrationRepository.save(riderVehicleRegistrationDocument);
        riderSelectedTrainingAppointment.setStatus(RiderTrainingStatus.COMPLETED);
        riderSelectedTrainingAppointment = riderTrainingAppointmentRepository.save(riderSelectedTrainingAppointment);

        String req = "{ \"profileId\": \" \",\n" +
                "    \"status\": \" \",\n" +
                "    \"remarks\": \"RDTC-658\",\n" +
                "    \"reason\": \" \",\n" +
                "    \"suspensionExpiryTime\": \"2022-03-23T09:02:19.864Z\",\n" +
                "    \"suspensionDuration\": 1\n" +
                "}";

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(req))
                .andExpect(status().isBadRequest())
                .andDo(print()).andReturn();
    }

    @Test
    void RDTC_663_updateRiderStatusSuspendedToAuthorizedAndRiderIDNotFoundThenReturn404() throws Exception {
        String statusUrl = URL + "/status";

        riderProfile.setStatus(RiderStatus.SUSPENDED);
        riderProfile.setNationalIdStatus(MandatoryCheckStatus.APPROVED);
        riderProfile.setProfilePhotoStatus(MandatoryCheckStatus.APPROVED);
        riderProfile = riderProfileRepository.save(riderProfile);

        riderDrivingLicenseDocument.setStatus(MandatoryCheckStatus.APPROVED);
        riderDrivingLicenseDocument = riderDrivingLicenseDocumentRepository.save(riderDrivingLicenseDocument);
        riderVehicleRegistrationDocument.setStatus(MandatoryCheckStatus.APPROVED);
        riderVehicleRegistrationDocument = vehicleRegistrationRepository.save(riderVehicleRegistrationDocument);
        riderSelectedTrainingAppointment.setStatus(RiderTrainingStatus.COMPLETED);
        riderSelectedTrainingAppointment = riderTrainingAppointmentRepository.save(riderSelectedTrainingAppointment);

        RiderStatusDto riderStatusDto = RiderStatusDto.builder()
                .profileId("req_id_not_found")
                .status(RiderStatus.AUTHORIZED)
                .reason("RDTC-663")
                .build();
        String json = objectMapper.writeValueAsString(riderStatusDto);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Record not found for id req_id_not_found"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0]").value("Data not found for your request"))
                .andDo(print()).andReturn();
    }

    @Test
    void RDTC_664_updateRiderStatusWhenRiderIsAuthorizedAndRequiredFieldNotSendThenReturn400() throws Exception {
        String statusUrl = URL + "/status";

        riderProfile.setStatus(RiderStatus.AUTHORIZED);
        riderProfile.setNationalIdStatus(MandatoryCheckStatus.APPROVED);
        riderProfile.setProfilePhotoStatus(MandatoryCheckStatus.APPROVED);
        riderProfile = riderProfileRepository.save(riderProfile);

        riderDrivingLicenseDocument.setStatus(MandatoryCheckStatus.APPROVED);
        riderDrivingLicenseDocument = riderDrivingLicenseDocumentRepository.save(riderDrivingLicenseDocument);
        riderVehicleRegistrationDocument.setStatus(MandatoryCheckStatus.APPROVED);
        riderVehicleRegistrationDocument = vehicleRegistrationRepository.save(riderVehicleRegistrationDocument);
        riderSelectedTrainingAppointment.setStatus(RiderTrainingStatus.COMPLETED);
        riderSelectedTrainingAppointment = riderTrainingAppointmentRepository.save(riderSelectedTrainingAppointment);

        String req = "{ \"remarks\": \"RDTC-657\",\n" +
                "    \"suspensionExpiryTime\": \"2022-03-23T09:02:19.864Z\",\n" +
                "    \"suspensionDuration\": 1\n" +
                "}";

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(req))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Method Argument not valid"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0]", Matchers.in(Arrays.asList("profileId: Property should not be blank", "status: Property is required"))))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[1]", Matchers.in(Arrays.asList("profileId: Property should not be blank", "status: Property is required"))))
                .andDo(print()).andReturn();
    }

    @Test
    void RDTC_666_updateRiderStatusAuthorizedToSuspendedAndOptionalFieldIsNULLThenReturn200() throws Exception {
        String statusUrl = URL + "/status";

        riderProfile.setStatus(RiderStatus.AUTHORIZED);
        riderProfile.setNationalIdStatus(MandatoryCheckStatus.APPROVED);
        riderProfile.setProfilePhotoStatus(MandatoryCheckStatus.APPROVED);
        riderProfile = riderProfileRepository.save(riderProfile);

        riderDrivingLicenseDocument.setStatus(MandatoryCheckStatus.APPROVED);
        riderDrivingLicenseDocument = riderDrivingLicenseDocumentRepository.save(riderDrivingLicenseDocument);
        riderVehicleRegistrationDocument.setStatus(MandatoryCheckStatus.APPROVED);
        riderVehicleRegistrationDocument = vehicleRegistrationRepository.save(riderVehicleRegistrationDocument);
        riderSelectedTrainingAppointment.setStatus(RiderTrainingStatus.COMPLETED);
        riderSelectedTrainingAppointment = riderTrainingAppointmentRepository.save(riderSelectedTrainingAppointment);

        RiderStatusDto riderStatusDto = RiderStatusDto.builder()
                .profileId(riderProfileDto.getId())
                .status(RiderStatus.SUSPENDED)
                .reason("RDTC-666")
                .suspensionExpiryTime(null)
                .remarks(null)
                .suspensionDuration(null)
                .build();
        String json = objectMapper.writeValueAsString(riderStatusDto);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("SUSPENDED"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.reason").value("RDTC-666"))
                .andDo(print()).andReturn();
    }

    @Test
    void RDTC_669_updateRiderStatusAuthorizedToSuspendedAndOptionalFieldIsEMPTYThenReturn200() throws Exception {
        String statusUrl = URL + "/status";

        riderProfile.setStatus(RiderStatus.AUTHORIZED);
        riderProfile.setNationalIdStatus(MandatoryCheckStatus.APPROVED);
        riderProfile.setProfilePhotoStatus(MandatoryCheckStatus.APPROVED);
        riderProfile = riderProfileRepository.save(riderProfile);

        riderDrivingLicenseDocument.setStatus(MandatoryCheckStatus.APPROVED);
        riderDrivingLicenseDocument = riderDrivingLicenseDocumentRepository.save(riderDrivingLicenseDocument);
        riderVehicleRegistrationDocument.setStatus(MandatoryCheckStatus.APPROVED);
        riderVehicleRegistrationDocument = vehicleRegistrationRepository.save(riderVehicleRegistrationDocument);
        riderSelectedTrainingAppointment.setStatus(RiderTrainingStatus.COMPLETED);
        riderSelectedTrainingAppointment = riderTrainingAppointmentRepository.save(riderSelectedTrainingAppointment);

        String req = "{ \"profileId\": \"" + riderProfile.getId() + "\",\n" +
                "    \"status\": \"" + RiderStatus.SUSPENDED + "\",\n" +
                "    \"reason\": \"RDTC_669\",\n" +
                "    \"remarks\": \"\",\n" +
                "    \"suspensionDuration\": \"\",\n" +
                "    \"suspensionExpiryTime\": \"\"\n" +
                "}";

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(req))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("SUSPENDED"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.reason").value("RDTC_669"))
                .andDo(print()).andReturn();
    }

    @Test
    void RDTC_670_updateRiderStatusAuthorizedToSuspendedAndOptionalFieldsIsInvalidTypeThenReturn400() throws Exception {
        String statusUrl = URL + "/status";

        riderProfile.setStatus(RiderStatus.AUTHORIZED);
        riderProfile.setNationalIdStatus(MandatoryCheckStatus.APPROVED);
        riderProfile.setProfilePhotoStatus(MandatoryCheckStatus.APPROVED);
        riderProfile = riderProfileRepository.save(riderProfile);

        riderDrivingLicenseDocument.setStatus(MandatoryCheckStatus.APPROVED);
        riderDrivingLicenseDocument = riderDrivingLicenseDocumentRepository.save(riderDrivingLicenseDocument);
        riderVehicleRegistrationDocument.setStatus(MandatoryCheckStatus.APPROVED);
        riderVehicleRegistrationDocument = vehicleRegistrationRepository.save(riderVehicleRegistrationDocument);
        riderSelectedTrainingAppointment.setStatus(RiderTrainingStatus.COMPLETED);
        riderSelectedTrainingAppointment = riderTrainingAppointmentRepository.save(riderSelectedTrainingAppointment);

        String req = "{ \"profileId\": \"" + riderProfile.getId() + "\",\n" +
                "    \"status\": \"" + RiderStatus.SUSPENDED + "\",\n" +
                "    \"remarks\": 126,\n" +
                "    \"reason\":  \"RDTC-670\",\n" +
                "    \"suspensionExpiryTime\": 126,\n" +
                "    \"suspensionDuration\": \"a\"\n" +
                "}";

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(req))
                .andExpect(status().isBadRequest())
                .andDo(print()).andReturn();
    }

    @Test
    void RDTC_672_updateRiderStatusAuthorizedToSuspendedAndOptionalFieldsIsSPACEThenReturn200() throws Exception {
        String statusUrl = URL + "/status";

        riderProfile.setStatus(RiderStatus.AUTHORIZED);
        riderProfile.setNationalIdStatus(MandatoryCheckStatus.APPROVED);
        riderProfile.setProfilePhotoStatus(MandatoryCheckStatus.APPROVED);
        riderProfile = riderProfileRepository.save(riderProfile);

        riderDrivingLicenseDocument.setStatus(MandatoryCheckStatus.APPROVED);
        riderDrivingLicenseDocument = riderDrivingLicenseDocumentRepository.save(riderDrivingLicenseDocument);
        riderVehicleRegistrationDocument.setStatus(MandatoryCheckStatus.APPROVED);
        riderVehicleRegistrationDocument = vehicleRegistrationRepository.save(riderVehicleRegistrationDocument);
        riderSelectedTrainingAppointment.setStatus(RiderTrainingStatus.COMPLETED);
        riderSelectedTrainingAppointment = riderTrainingAppointmentRepository.save(riderSelectedTrainingAppointment);

        String req = "{ \"profileId\": \"" + riderProfile.getId() + "\",\n" +
                "    \"status\": \"" + RiderStatus.SUSPENDED + "\",\n" +
                "    \"reason\": \"RDTC-672\",\n" +
                "    \"remarks\": \" \",\n" +
                "    \"suspensionDuration\": \" \",\n" +
                "    \"suspensionExpiryTime\": \" \"\n" +
                "}";

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(req))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("SUSPENDED"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.reason").value("RDTC-672"))
                .andDo(print()).andReturn();
    }

    @Test
    void RDTC_674_updateRiderStatusAuthorizedToSuspendedAndOptionalFieldsIsNotSendThenReturn200() throws Exception {
        String statusUrl = URL + "/status";

        riderProfile.setStatus(RiderStatus.AUTHORIZED);
        riderProfile.setNationalIdStatus(MandatoryCheckStatus.APPROVED);
        riderProfile.setProfilePhotoStatus(MandatoryCheckStatus.APPROVED);
        riderProfile = riderProfileRepository.save(riderProfile);

        riderDrivingLicenseDocument.setStatus(MandatoryCheckStatus.APPROVED);
        riderDrivingLicenseDocument = riderDrivingLicenseDocumentRepository.save(riderDrivingLicenseDocument);
        riderVehicleRegistrationDocument.setStatus(MandatoryCheckStatus.APPROVED);
        riderVehicleRegistrationDocument = vehicleRegistrationRepository.save(riderVehicleRegistrationDocument);
        riderSelectedTrainingAppointment.setStatus(RiderTrainingStatus.COMPLETED);
        riderSelectedTrainingAppointment = riderTrainingAppointmentRepository.save(riderSelectedTrainingAppointment);

        RiderStatusDto riderStatusDto = RiderStatusDto.builder()
                .profileId(riderProfileDto.getId())
                .status(RiderStatus.SUSPENDED)
                .reason("RDTC-674")
                .build();
        String json = objectMapper.writeValueAsString(riderStatusDto);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("SUSPENDED"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.reason").value("RDTC-674"))
                .andDo(print()).andReturn();
    }

    @Test
    void RDTC_675_updateRiderStatusAuthorizedToSuspendedAndSuspendedExpiryTimeInvalidFormatThenReturn400() throws Exception {
        String statusUrl = URL + "/status";

        riderProfile.setStatus(RiderStatus.AUTHORIZED);
        riderProfile.setNationalIdStatus(MandatoryCheckStatus.APPROVED);
        riderProfile.setProfilePhotoStatus(MandatoryCheckStatus.APPROVED);
        riderProfile = riderProfileRepository.save(riderProfile);

        riderDrivingLicenseDocument.setStatus(MandatoryCheckStatus.APPROVED);
        riderDrivingLicenseDocument = riderDrivingLicenseDocumentRepository.save(riderDrivingLicenseDocument);
        riderVehicleRegistrationDocument.setStatus(MandatoryCheckStatus.APPROVED);
        riderVehicleRegistrationDocument = vehicleRegistrationRepository.save(riderVehicleRegistrationDocument);
        riderSelectedTrainingAppointment.setStatus(RiderTrainingStatus.COMPLETED);
        riderSelectedTrainingAppointment = riderTrainingAppointmentRepository.save(riderSelectedTrainingAppointment);

        String req = "{ \"profileId\": \"" + riderProfile.getId() + "\",\n" +
                "    \"status\": \"" + RiderStatus.SUSPENDED + "\",\n" +
                "    \"reason\":  \"RDTC-675\",\n" +
                "    \"suspensionExpiryTime\": \"2022/03/23 09:02:19\",\n" +
                "}";

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(req))
                .andExpect(status().isBadRequest())
                .andDo(print()).andReturn();
    }

    @Test
    void RDTC_676_updateRiderStatusAuthorizedToAuthorizedThenReturn400() throws Exception {
        String statusUrl = URL + "/status";

        riderProfile.setStatus(RiderStatus.AUTHORIZED);
        riderProfile.setNationalIdStatus(MandatoryCheckStatus.APPROVED);
        riderProfile.setProfilePhotoStatus(MandatoryCheckStatus.APPROVED);
        riderProfile = riderProfileRepository.save(riderProfile);

        riderDrivingLicenseDocument.setStatus(MandatoryCheckStatus.APPROVED);
        riderDrivingLicenseDocument = riderDrivingLicenseDocumentRepository.save(riderDrivingLicenseDocument);
        riderVehicleRegistrationDocument.setStatus(MandatoryCheckStatus.APPROVED);
        riderVehicleRegistrationDocument = vehicleRegistrationRepository.save(riderVehicleRegistrationDocument);
        riderSelectedTrainingAppointment.setStatus(RiderTrainingStatus.COMPLETED);
        riderSelectedTrainingAppointment = riderTrainingAppointmentRepository.save(riderSelectedTrainingAppointment);

        RiderStatusDto riderStatusDto = RiderStatusDto.builder()
                .profileId(riderProfileDto.getId())
                .status(RiderStatus.AUTHORIZED)
                .reason("RDTC-676")
                .build();
        String json = objectMapper.writeValueAsString(riderStatusDto);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Rider is already AUTHORIZED for id " + riderProfile.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0]").value("Status transition not allowed"))
                .andDo(print()).andReturn();
    }

    @Test
    void RDTC_680_updateRiderStatusSuspendedToSuspendedThenReturn400() throws Exception {
        String statusUrl = URL + "/status";

        riderProfile.setStatus(RiderStatus.SUSPENDED);
        riderProfile.setNationalIdStatus(MandatoryCheckStatus.APPROVED);
        riderProfile.setProfilePhotoStatus(MandatoryCheckStatus.APPROVED);
        riderProfile = riderProfileRepository.save(riderProfile);

        riderDrivingLicenseDocument.setStatus(MandatoryCheckStatus.APPROVED);
        riderDrivingLicenseDocument = riderDrivingLicenseDocumentRepository.save(riderDrivingLicenseDocument);
        riderVehicleRegistrationDocument.setStatus(MandatoryCheckStatus.APPROVED);
        riderVehicleRegistrationDocument = vehicleRegistrationRepository.save(riderVehicleRegistrationDocument);
        riderSelectedTrainingAppointment.setStatus(RiderTrainingStatus.COMPLETED);
        riderSelectedTrainingAppointment = riderTrainingAppointmentRepository.save(riderSelectedTrainingAppointment);

        RiderStatusDto riderStatusDto = RiderStatusDto.builder()
                .profileId(riderProfileDto.getId())
                .status(RiderStatus.SUSPENDED)
                .reason("RDTC-680")
                .build();
        String json = objectMapper.writeValueAsString(riderStatusDto);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Rider is already SUSPENDED for id " + riderProfile.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0]").value("Status transition not allowed"))
                .andDo(print()).andReturn();
    }

    @Test
    void RDTC_681_updateRiderStatusUnauthorizedToUnauthorizedThenReturn400() throws Exception {
        String statusUrl = URL + "/status";

        riderProfile.setStatus(RiderStatus.UNAUTHORIZED);
        riderProfile.setNationalIdStatus(MandatoryCheckStatus.APPROVED);
        riderProfile.setProfilePhotoStatus(MandatoryCheckStatus.APPROVED);
        riderProfile = riderProfileRepository.save(riderProfile);

        riderDrivingLicenseDocument.setStatus(MandatoryCheckStatus.APPROVED);
        riderDrivingLicenseDocument = riderDrivingLicenseDocumentRepository.save(riderDrivingLicenseDocument);
        riderVehicleRegistrationDocument.setStatus(MandatoryCheckStatus.APPROVED);
        riderVehicleRegistrationDocument = vehicleRegistrationRepository.save(riderVehicleRegistrationDocument);
        riderSelectedTrainingAppointment.setStatus(RiderTrainingStatus.COMPLETED);
        riderSelectedTrainingAppointment = riderTrainingAppointmentRepository.save(riderSelectedTrainingAppointment);

        RiderStatusDto riderStatusDto = RiderStatusDto.builder()
                .profileId(riderProfileDto.getId())
                .status(RiderStatus.UNAUTHORIZED)
                .reason("RDTC-681")
                .build();
        String json = objectMapper.writeValueAsString(riderStatusDto);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Rider is already UNAUTHORIZED for id " + riderProfile.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[0]").value("Status transition not allowed"))
                .andDo(print()).andReturn();
    }

    @Test
    void RDTC_682_updateRiderStatusAuthorizedToSuspendedLowercaseThenReturn400() throws Exception {
        String statusUrl = URL + "/status";

        riderProfile.setStatus(RiderStatus.AUTHORIZED);
        riderProfile.setNationalIdStatus(MandatoryCheckStatus.APPROVED);
        riderProfile.setProfilePhotoStatus(MandatoryCheckStatus.APPROVED);
        riderProfile = riderProfileRepository.save(riderProfile);

        riderDrivingLicenseDocument.setStatus(MandatoryCheckStatus.APPROVED);
        riderDrivingLicenseDocument = riderDrivingLicenseDocumentRepository.save(riderDrivingLicenseDocument);
        riderVehicleRegistrationDocument.setStatus(MandatoryCheckStatus.APPROVED);
        riderVehicleRegistrationDocument = vehicleRegistrationRepository.save(riderVehicleRegistrationDocument);
        riderSelectedTrainingAppointment.setStatus(RiderTrainingStatus.COMPLETED);
        riderSelectedTrainingAppointment = riderTrainingAppointmentRepository.save(riderSelectedTrainingAppointment);

        String req = "{ \"profileId\": \"" + riderProfile.getId() + "\",\n" +
                "    \"status\": \"suspended\",\n" +
                "    \"reason\":  \"RDTC-682\",\n" +
                "}";
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(req))
                .andExpect(status().isBadRequest())
                .andDo(print()).andReturn();
    }

    @Test
    void RDTC_684_updateRiderStatusSuspendedToAuthorizedLowercaseThenReturn400() throws Exception {
        String statusUrl = URL + "/status";

        riderProfile.setStatus(RiderStatus.SUSPENDED);
        riderProfile.setNationalIdStatus(MandatoryCheckStatus.APPROVED);
        riderProfile.setProfilePhotoStatus(MandatoryCheckStatus.APPROVED);
        riderProfile = riderProfileRepository.save(riderProfile);

        riderDrivingLicenseDocument.setStatus(MandatoryCheckStatus.APPROVED);
        riderDrivingLicenseDocument = riderDrivingLicenseDocumentRepository.save(riderDrivingLicenseDocument);
        riderVehicleRegistrationDocument.setStatus(MandatoryCheckStatus.APPROVED);
        riderVehicleRegistrationDocument = vehicleRegistrationRepository.save(riderVehicleRegistrationDocument);
        riderSelectedTrainingAppointment.setStatus(RiderTrainingStatus.COMPLETED);
        riderSelectedTrainingAppointment = riderTrainingAppointmentRepository.save(riderSelectedTrainingAppointment);

        String req = "{ \"profileId\": \"" + riderProfile.getId() + "\",\n" +
                "    \"status\": \"authorized\",\n" +
                "    \"reason\":  \"RDTC-684\",\n" +
                "}";
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(req))
                .andExpect(status().isBadRequest())
                .andDo(print()).andReturn();
    }

    @Test
    void RDTC_686_updateRiderStatusUnautorizedToAuthorizedCapitalLetterThenReturn400() throws Exception {
        String statusUrl = URL + "/status";

        riderProfile.setStatus(RiderStatus.UNAUTHORIZED);
        riderProfile.setNationalIdStatus(MandatoryCheckStatus.APPROVED);
        riderProfile.setProfilePhotoStatus(MandatoryCheckStatus.APPROVED);
        riderProfile = riderProfileRepository.save(riderProfile);

        riderDrivingLicenseDocument.setStatus(MandatoryCheckStatus.APPROVED);
        riderDrivingLicenseDocument = riderDrivingLicenseDocumentRepository.save(riderDrivingLicenseDocument);
        riderVehicleRegistrationDocument.setStatus(MandatoryCheckStatus.APPROVED);
        riderVehicleRegistrationDocument = vehicleRegistrationRepository.save(riderVehicleRegistrationDocument);
        riderSelectedTrainingAppointment.setStatus(RiderTrainingStatus.COMPLETED);
        riderSelectedTrainingAppointment = riderTrainingAppointmentRepository.save(riderSelectedTrainingAppointment);

        String req = "{ \"profileId\": \"" + riderProfile.getId() + "\",\n" +
                "    \"status\": \"Authorized\",\n" +
                "    \"reason\":  \"RDTC-686\",\n" +
                "}";
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(req))
                .andExpect(status().isBadRequest())
                .andDo(print()).andReturn();
    }

    @Test
    void RDTC_633_updateRiderStatusAuthorizedToSuspendedThenReturn200() throws Exception {
        String statusUrl = URL + "/status";

        riderProfile.setStatus(RiderStatus.AUTHORIZED);
        riderProfile.setNationalIdStatus(MandatoryCheckStatus.APPROVED);
        riderProfile.setProfilePhotoStatus(MandatoryCheckStatus.APPROVED);
        riderProfile = riderProfileRepository.save(riderProfile);

        riderDrivingLicenseDocument.setStatus(MandatoryCheckStatus.APPROVED);
        riderDrivingLicenseDocument = riderDrivingLicenseDocumentRepository.save(riderDrivingLicenseDocument);
        riderVehicleRegistrationDocument.setStatus(MandatoryCheckStatus.APPROVED);
        riderVehicleRegistrationDocument = vehicleRegistrationRepository.save(riderVehicleRegistrationDocument);
        riderSelectedTrainingAppointment.setStatus(RiderTrainingStatus.COMPLETED);
        riderSelectedTrainingAppointment = riderTrainingAppointmentRepository.save(riderSelectedTrainingAppointment);

        RiderStatusDto riderStatusDto = RiderStatusDto.builder()
                .profileId(riderProfileDto.getId())
                .status(RiderStatus.SUSPENDED)
                .reason("RDTC-633")
                .suspensionExpiryTime(null)
                .remarks("ทดสอบช่องคอมเม้น")
                .suspensionDuration(null)
                .riderCaseNo(null)
                .build();
        String json = objectMapper.writeValueAsString(riderStatusDto);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("SUSPENDED"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.reason").value("RDTC-633"))
                .andDo(print()).andReturn();
    }

    @Test
    void RDTC_635_updateRiderStatusSuspendedToAuthorizedThenReturn200() throws Exception {
        String statusUrl = URL + "/status";

        riderProfile.setStatus(RiderStatus.SUSPENDED);
        riderProfile.setNationalIdStatus(MandatoryCheckStatus.APPROVED);
        riderProfile.setProfilePhotoStatus(MandatoryCheckStatus.APPROVED);
        riderProfile = riderProfileRepository.save(riderProfile);

        riderDrivingLicenseDocument.setStatus(MandatoryCheckStatus.APPROVED);
        riderDrivingLicenseDocument = riderDrivingLicenseDocumentRepository.save(riderDrivingLicenseDocument);
        riderVehicleRegistrationDocument.setStatus(MandatoryCheckStatus.APPROVED);
        riderVehicleRegistrationDocument = vehicleRegistrationRepository.save(riderVehicleRegistrationDocument);
        riderSelectedTrainingAppointment.setStatus(RiderTrainingStatus.COMPLETED);
        riderSelectedTrainingAppointment = riderTrainingAppointmentRepository.save(riderSelectedTrainingAppointment);

        RiderStatusDto riderStatusDto = RiderStatusDto.builder()
                .profileId(riderProfileDto.getId())
                .status(RiderStatus.AUTHORIZED)
                .reason("RDTC-635")
                .suspensionExpiryTime(null)
                .remarks("ทดสอบช่องคอมเม้น")
                .suspensionDuration(null)
                .riderCaseNo(null)
                .build();
        String json = objectMapper.writeValueAsString(riderStatusDto);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("AUTHORIZED"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.reason").value("RDTC-635"))
                .andDo(print()).andReturn();
    }

    @Test
    void RDTC_802_updateRiderStatusAuthorizedToSuspendedWhenRiderCaseNoIsValidThenReturn200() throws Exception {
        String statusUrl = URL + "/status";

        riderProfile.setStatus(RiderStatus.AUTHORIZED);
        riderProfile.setNationalIdStatus(MandatoryCheckStatus.APPROVED);
        riderProfile.setProfilePhotoStatus(MandatoryCheckStatus.APPROVED);
        riderProfile = riderProfileRepository.save(riderProfile);

        riderDrivingLicenseDocument.setStatus(MandatoryCheckStatus.APPROVED);
        riderDrivingLicenseDocument = riderDrivingLicenseDocumentRepository.save(riderDrivingLicenseDocument);
        riderVehicleRegistrationDocument.setStatus(MandatoryCheckStatus.APPROVED);
        riderVehicleRegistrationDocument = vehicleRegistrationRepository.save(riderVehicleRegistrationDocument);
        riderSelectedTrainingAppointment.setStatus(RiderTrainingStatus.COMPLETED);
        riderSelectedTrainingAppointment = riderTrainingAppointmentRepository.save(riderSelectedTrainingAppointment);

        RiderStatusDto riderStatusDto = RiderStatusDto.builder()
                .profileId(riderProfileDto.getId())
                .status(RiderStatus.SUSPENDED)
                .reason("RDTC-802")
                .suspensionExpiryTime(null)
                .remarks("ทดสอบช่องคอมเม้น")
                .suspensionDuration(null)
                .riderCaseNo("NO1234")
                .build();
        String json = objectMapper.writeValueAsString(riderStatusDto);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("SUSPENDED"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.reason").value("RDTC-802"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.riderCaseNo").value("NO1234"))
                .andDo(print()).andReturn();
    }

    @Test
    void RDTC_803_updateRiderStatusAuthorizedToSuspendedWhenRiderCaseNoIsInvalidThenReturn200() throws Exception {
        String statusUrl = URL + "/status";

        riderProfile.setStatus(RiderStatus.AUTHORIZED);
        riderProfile.setNationalIdStatus(MandatoryCheckStatus.APPROVED);
        riderProfile.setProfilePhotoStatus(MandatoryCheckStatus.APPROVED);
        riderProfile = riderProfileRepository.save(riderProfile);

        riderDrivingLicenseDocument.setStatus(MandatoryCheckStatus.APPROVED);
        riderDrivingLicenseDocument = riderDrivingLicenseDocumentRepository.save(riderDrivingLicenseDocument);
        riderVehicleRegistrationDocument.setStatus(MandatoryCheckStatus.APPROVED);
        riderVehicleRegistrationDocument = vehicleRegistrationRepository.save(riderVehicleRegistrationDocument);
        riderSelectedTrainingAppointment.setStatus(RiderTrainingStatus.COMPLETED);
        riderSelectedTrainingAppointment = riderTrainingAppointmentRepository.save(riderSelectedTrainingAppointment);

        RiderStatusDto riderStatusDto = RiderStatusDto.builder()
                .profileId(riderProfileDto.getId())
                .status(RiderStatus.SUSPENDED)
                .reason("RDTC-803")
                .suspensionExpiryTime(null)
                .remarks("ทดสอบช่องคอมเม้น")
                .suspensionDuration(null)
                .riderCaseNo(null)
                .build();
        String json = objectMapper.writeValueAsString(riderStatusDto);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("SUSPENDED"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.reason").value("RDTC-803"))
                .andDo(print()).andReturn();
    }

    @Test
    void RDTC_804_updateRiderStatusAuthorizedToSuspendedWhenRiderCaseNoIsNullThenReturn200() throws Exception {
        String statusUrl = URL + "/status";

        riderProfile.setStatus(RiderStatus.AUTHORIZED);
        riderProfile.setNationalIdStatus(MandatoryCheckStatus.APPROVED);
        riderProfile.setProfilePhotoStatus(MandatoryCheckStatus.APPROVED);
        riderProfile = riderProfileRepository.save(riderProfile);

        riderDrivingLicenseDocument.setStatus(MandatoryCheckStatus.APPROVED);
        riderDrivingLicenseDocument = riderDrivingLicenseDocumentRepository.save(riderDrivingLicenseDocument);
        riderVehicleRegistrationDocument.setStatus(MandatoryCheckStatus.APPROVED);
        riderVehicleRegistrationDocument = vehicleRegistrationRepository.save(riderVehicleRegistrationDocument);
        riderSelectedTrainingAppointment.setStatus(RiderTrainingStatus.COMPLETED);
        riderSelectedTrainingAppointment = riderTrainingAppointmentRepository.save(riderSelectedTrainingAppointment);

        RiderStatusDto riderStatusDto = RiderStatusDto.builder()
                .profileId(riderProfileDto.getId())
                .status(RiderStatus.SUSPENDED)
                .reason("RDTC-804")
                .suspensionExpiryTime(null)
                .remarks("ทดสอบช่องคอมเม้น")
                .suspensionDuration(null)
                .riderCaseNo(null)
                .build();
        String json = objectMapper.writeValueAsString(riderStatusDto);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("SUSPENDED"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.reason").value("RDTC-804"))
                .andDo(print()).andReturn();
    }

    @Test
    void RDTC_805_updateRiderStatusAuthorizedToSuspendedWhenRiderCaseNoIsEmptyThenReturn200() throws Exception {
        String statusUrl = URL + "/status";

        riderProfile.setStatus(RiderStatus.AUTHORIZED);
        riderProfile.setNationalIdStatus(MandatoryCheckStatus.APPROVED);
        riderProfile.setProfilePhotoStatus(MandatoryCheckStatus.APPROVED);
        riderProfile = riderProfileRepository.save(riderProfile);

        riderDrivingLicenseDocument.setStatus(MandatoryCheckStatus.APPROVED);
        riderDrivingLicenseDocument = riderDrivingLicenseDocumentRepository.save(riderDrivingLicenseDocument);
        riderVehicleRegistrationDocument.setStatus(MandatoryCheckStatus.APPROVED);
        riderVehicleRegistrationDocument = vehicleRegistrationRepository.save(riderVehicleRegistrationDocument);
        riderSelectedTrainingAppointment.setStatus(RiderTrainingStatus.COMPLETED);
        riderSelectedTrainingAppointment = riderTrainingAppointmentRepository.save(riderSelectedTrainingAppointment);

        RiderStatusDto riderStatusDto = RiderStatusDto.builder()
                .profileId(riderProfileDto.getId())
                .status(RiderStatus.SUSPENDED)
                .reason("RDTC-805")
                .suspensionExpiryTime(null)
                .remarks("ทดสอบช่องคอมเม้น")
                .suspensionDuration(null)
                .riderCaseNo("")
                .build();
        String json = objectMapper.writeValueAsString(riderStatusDto);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(statusUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("SUSPENDED"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.reason").value("RDTC-805"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.riderCaseNo").value(""))
                .andDo(print()).andReturn();
    }

}
