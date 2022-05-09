package com.scb.rider.controller;

import com.scb.rider.model.document.RiderEmergencyContact;
import com.scb.rider.model.dto.RiderEmergencyContactDto;
import com.scb.rider.service.document.RiderEmergencyContactService;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RiderEmergencyContactControllerTest {

    @InjectMocks
    private RiderEmergencyContactController emergencyContactController;

    @Mock
    private RiderEmergencyContactService emergencyContactService;

    private static RiderEmergencyContact emergencyContact;

    private static RiderEmergencyContactDto emergencyContactDto;

    @BeforeAll
    static void setUp() {
        emergencyContactDto = RiderEmergencyContactDto.builder()
                .address1("address1")
                .name("Micheal")
                .profileId("123456789qwertyuio")
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
    @Order(1)
    void shouldCreateNewContact() {
        when(emergencyContactService.saveEmergencyContact(emergencyContactDto)).thenReturn(emergencyContact);
        ResponseEntity<RiderEmergencyContactDto> fetchedProfile = emergencyContactController.saveEmergencyContact(emergencyContactDto);
        assertTrue(ObjectUtils.isNotEmpty(fetchedProfile.getBody()));
        assertEquals(HttpStatus.OK, fetchedProfile.getStatusCode());
    }

    @Test
    @Order(2)
    void shouldUpdateContact() {
        when(emergencyContactService.saveEmergencyContact(emergencyContactDto)).thenReturn(emergencyContact);
        ResponseEntity<RiderEmergencyContactDto> fetchedProfile = emergencyContactController.saveEmergencyContact(emergencyContactDto);
        assertTrue(ObjectUtils.isNotEmpty(fetchedProfile.getBody()));
        assertEquals(HttpStatus.OK, fetchedProfile.getStatusCode());
    }

    @Test
    @Order(3)
    void shouldFetchContactById() {
        when(emergencyContactService.getEmergencyContactByProfileId(emergencyContactDto.getProfileId())).thenReturn(emergencyContact);
        ResponseEntity<RiderEmergencyContactDto> fetchedProfile = emergencyContactController.getRiderEmergencyContactByProfileId(emergencyContactDto.getProfileId());
        assertTrue(ObjectUtils.isNotEmpty(fetchedProfile.getBody()));
        assertEquals(HttpStatus.OK, fetchedProfile.getStatusCode());
    }
}