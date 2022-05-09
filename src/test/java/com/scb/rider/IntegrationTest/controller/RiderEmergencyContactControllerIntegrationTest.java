package com.scb.rider.IntegrationTest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.rider.model.document.RiderEmergencyContact;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.dto.RiderEmergencyContactDto;
import com.scb.rider.repository.RiderProfileRepository;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@RunWith(SpringRunner.class)
//@ExtendWith(SpringExtension.class)
//@DataJpaTest
@EnableConfigurationProperties
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RiderEmergencyContactControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private RiderProfileRepository riderProfileRepository;

    private String URL = "/profile/emergency-contact";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    private static RiderEmergencyContact emergencyContact;

    private static RiderEmergencyContactDto emergencyContactDto;

    @BeforeAll
    static void setUp() {
        emergencyContactDto = RiderEmergencyContactDto.builder()
                .address1("address1")
                .name("Micheal")
                .relationship("Father")
                .zipCode("12345")
                .district("Bulandshahr")
                .homePhoneNumber("9999999999")
                .mobilePhoneNumber("9999999999")
                .build();
        emergencyContact = new RiderEmergencyContact();
        BeanUtils.copyProperties(emergencyContactDto, emergencyContact);
    }

    @Test
    void whenValidInput_thenReturns200() throws Exception {
        Assumptions.assumeTrue(StringUtils.isNotBlank(emergencyContactDto.getProfileId()));
        MvcResult result = mockMvc
                .perform(
                        get(URL+"/"+ emergencyContactDto.getProfileId())
                                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andReturn();
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }

    @Test
    @Order(1)
    void save_thenReturns200() throws Exception {
        RiderProfile riderProfile = riderProfileRepository.findFirstByOrderByPhoneNumberAsc();
        if(ObjectUtils.isNotEmpty(riderProfile)){
            emergencyContactDto.setProfileId(riderProfile.getId());
        }
        Assumptions.assumeTrue(StringUtils.isNotBlank(emergencyContactDto.getProfileId()));
        String json = objectMapper.writeValueAsString(emergencyContactDto);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print()).andReturn();
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        RiderEmergencyContactDto profileResult = objectMapper.readValue(result.getResponse().getContentAsString(),
                RiderEmergencyContactDto.class);
        assertNotNull(profileResult.getProfileId());
        emergencyContactDto.setProfileId(profileResult.getProfileId());
    }

    @Test
    void update_thenReturns200() throws Exception {
        Assumptions.assumeTrue(StringUtils.isNotBlank(emergencyContactDto.getProfileId()));
        emergencyContactDto.setHomePhoneNumber("7777777777");
        String json = objectMapper.writeValueAsString(emergencyContactDto);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print()).andReturn();
            assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        RiderEmergencyContactDto profileResult = objectMapper.readValue(result.getResponse().getContentAsString(),
                RiderEmergencyContactDto.class);
        assertNotNull(profileResult.getProfileId());
        assertNotNull(emergencyContactDto.toString());
        assertNotNull(emergencyContact.toString());
    }
    
}
