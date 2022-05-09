package com.scb.rider.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.BeanUtils;

import com.scb.rider.exception.AccessDeniedException;
import com.scb.rider.model.document.RiderDeviceDetails;
import com.scb.rider.model.document.RiderPreferredZones;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.dto.RiderPreferredZoneDto;
import com.scb.rider.model.enumeration.AvailabilityStatus;
import com.scb.rider.model.enumeration.RiderStatus;
import com.scb.rider.repository.RiderDeviceDetailRepository;
import com.scb.rider.repository.RiderProfileRepository;
import com.scb.rider.service.cache.RiderProfileUpdaterService;
import com.scb.rider.service.document.RiderDeviceService;
import com.scb.rider.service.document.RiderPreferredZoneService;


@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RiderPreferredZoneServiceTest {

    @Mock
    private RiderProfileRepository riderProfileRepository;

    @InjectMocks
    private RiderPreferredZoneService riderPreferredZoneService;

    private static RiderPreferredZones preferredZones;

    private static RiderPreferredZoneDto preferredZoneDto;

    @Mock
    private RiderDeviceDetailRepository riderDeviceDetailRepository;
    
    @Mock
    private RiderDeviceService riderDeviceService;
    
    @Mock
	private RiderProfileUpdaterService riderProfileUpdaterService;
    
    @BeforeAll
    static void setUp() {
    	preferredZoneDto = RiderPreferredZoneDto.builder().riderProfileId("123").preferredZoneId("1")
				.preferredZoneName("Bangkok").build();

		preferredZones = new RiderPreferredZones();
		BeanUtils.copyProperties(preferredZoneDto, preferredZones);
    }

    @Test
    void shouldCreateNewZone() {
        RiderProfile riderProfile = new RiderProfile();
        riderProfile.setId("1234");
        riderProfile.setStatus(RiderStatus.AUTHORIZED);
        riderProfile.setAvailabilityStatus(AvailabilityStatus.Inactive);
        RiderPreferredZones riderPreferredZones = new RiderPreferredZones();
        riderPreferredZones.setPreferredZoneId("1");
        riderPreferredZones.setPreferredZoneName("Bangkok");
        riderProfile.setRiderPreferredZones(riderPreferredZones);
        
        RiderDeviceDetails value =RiderDeviceDetails.builder().deviceToken("test").build();
        
        when(riderProfileRepository.save(any())).thenReturn(riderProfile);
        when(riderDeviceDetailRepository
				.findByProfileId(anyString())).thenReturn(Optional.of(value));
        when(riderProfileRepository.findById(preferredZoneDto.getRiderProfileId())).thenReturn(Optional.of(riderProfile));
        when(riderProfileRepository.findByRiderId(anyString())).thenReturn(Optional.of(riderProfile));
        
        RiderPreferredZones preferredZones = riderPreferredZoneService.savePreferredZone(preferredZoneDto);
        assertTrue(ObjectUtils.isNotEmpty(preferredZones));
        assertNotNull(preferredZones.getPreferredZoneId());
        assertNotNull(preferredZones.toString());
    }

    @Test
    void shouldUpdatePreferredZone() {
        RiderProfile riderProfile = new RiderProfile();
        riderProfile.setId("1234");
        riderProfile.setAvailabilityStatus(AvailabilityStatus.Inactive);
        riderProfile.setStatus(RiderStatus.AUTHORIZED);
        RiderPreferredZones riderPreferredZones = new RiderPreferredZones();
        riderPreferredZones.setPreferredZoneId("1");
        riderPreferredZones.setPreferredZoneName("Bangkok");
        riderProfile.setRiderPreferredZones(riderPreferredZones);
        when(riderProfileRepository.save(any())).thenReturn(riderProfile);
        when(riderProfileRepository.findById(preferredZoneDto.getRiderProfileId())).thenReturn(Optional.of(riderProfile));
        RiderPreferredZones preferredZones = riderPreferredZoneService.savePreferredZoneOpsMember(preferredZoneDto);
        assertTrue(ObjectUtils.isNotEmpty(preferredZones));
        assertNotNull(preferredZones.getPreferredZoneId());
        assertNotNull(preferredZones.toString());
    }

    @Test
    void shouldUpdatePreferredZoneWithException() {
        RiderProfile riderProfile = new RiderProfile();
        riderProfile.setId("1234");
        riderProfile.setAvailabilityStatus(AvailabilityStatus.Inactive);
        riderProfile.setStatus(RiderStatus.UNAUTHORIZED);
        RiderPreferredZones riderPreferredZones = new RiderPreferredZones();
        riderPreferredZones.setPreferredZoneId("1");
        riderPreferredZones.setPreferredZoneName("Bangkok");
        riderProfile.setRiderPreferredZones(riderPreferredZones);

        when(riderProfileRepository.findById(preferredZoneDto.getRiderProfileId())).thenReturn(Optional.of(riderProfile));
        assertThrows(AccessDeniedException.class,
                () -> riderPreferredZoneService.savePreferredZone(preferredZoneDto));
    }

    @Test
    void shouldSavePreferredZone() {
        RiderProfile riderProfile = new RiderProfile();
        riderProfile.setId("1234");
        riderProfile.setAvailabilityStatus(AvailabilityStatus.Inactive);
        riderProfile.setStatus(RiderStatus.UNAUTHORIZED);
        RiderPreferredZones riderPreferredZones = new RiderPreferredZones();
        riderPreferredZones.setPreferredZoneId("1");
        riderPreferredZones.setPreferredZoneName("Bangkok");
        riderProfile.setRiderPreferredZones(riderPreferredZones);
        when(riderProfileRepository.findById(preferredZoneDto.getRiderProfileId())).thenReturn(Optional.of(riderProfile));
        assertThrows(AccessDeniedException.class,
                () -> riderPreferredZoneService.savePreferredZone(preferredZoneDto));
    }
}