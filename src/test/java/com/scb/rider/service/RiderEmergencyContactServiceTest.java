package com.scb.rider.service;

import com.scb.rider.exception.DataNotFoundException;
import com.scb.rider.model.document.RiderEmergencyContact;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.dto.RiderEmergencyContactDto;
import com.scb.rider.repository.RiderEmergencyContactRepository;
import com.scb.rider.repository.RiderProfileRepository;
import com.scb.rider.service.document.RiderEmergencyContactService;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
class RiderEmergencyContactServiceTest {

    @Mock
    private RiderEmergencyContactRepository emergencyContactRepository;

    @Mock
    private RiderProfileRepository riderProfileRepository;

    @InjectMocks
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
    void shouldCreateNewContact() {
        RiderProfile riderProfile = new RiderProfile();
        riderProfile.setId("12345");
        when(emergencyContactRepository.save(Mockito.any(RiderEmergencyContact.class))).thenReturn(emergencyContact);
        when(emergencyContactRepository.findByProfileId(riderProfile.getId())).thenReturn(Optional.of(emergencyContact));
        when(riderProfileRepository.findById(emergencyContactDto.getProfileId())).thenReturn(Optional.of(riderProfile));
        RiderEmergencyContact fetchedProfile = emergencyContactService.saveEmergencyContact(emergencyContactDto);
        assertTrue(ObjectUtils.isNotEmpty(fetchedProfile));
        assertNotNull(fetchedProfile.getProfileId());
    }

    @Test
    void shouldUpdateContact() {
        RiderProfile riderProfile = new RiderProfile();
        riderProfile.setId("12345");
        when(emergencyContactRepository.save(Mockito.any(RiderEmergencyContact.class))).thenReturn(emergencyContact);
        when(emergencyContactRepository.findByProfileId(riderProfile.getId())).thenReturn(Optional.empty());
        when(riderProfileRepository.findById(emergencyContactDto.getProfileId())).thenReturn(Optional.of(riderProfile));
        RiderEmergencyContact fetchedProfile = emergencyContactService.saveEmergencyContact(emergencyContactDto);
        assertTrue(ObjectUtils.isNotEmpty(fetchedProfile));
        assertNotNull(fetchedProfile.getProfileId());
        assertNotNull(fetchedProfile.toString());
    }

    @Test
    void shouldFetchContactById() {
        when(emergencyContactRepository.findByProfileId(emergencyContactDto.getProfileId())).thenReturn(Optional.of(emergencyContact));
        RiderEmergencyContact fetchedProfile = emergencyContactService.getEmergencyContactByProfileId(emergencyContactDto.getProfileId());
        assertTrue(ObjectUtils.isNotEmpty(fetchedProfile));
        assertNotNull(fetchedProfile.getProfileId());
    }

    @Test
    void throwExceptionFetchContactById() {
        when(emergencyContactRepository.findByProfileId(emergencyContactDto.getProfileId())).thenReturn(Optional.empty());
        String id = emergencyContactDto.getProfileId();
        assertThrows(DataNotFoundException.class, () -> emergencyContactService.getEmergencyContactByProfileId(id));
    }

    @Test
    void throwExceptionSaveContact() {
        when(riderProfileRepository.findById(emergencyContactDto.getProfileId())).thenReturn(Optional.empty());
        assertThrows(DataNotFoundException.class, () ->emergencyContactService.saveEmergencyContact(emergencyContactDto));
    }

}