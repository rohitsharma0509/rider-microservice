package com.scb.rider.controller;

import com.scb.rider.constants.Constants;
import com.scb.rider.model.document.RiderPreferredZones;
import com.scb.rider.model.dto.RiderPreferredZoneDto;
import com.scb.rider.service.document.RiderPreferredZoneService;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.nio.file.AccessDeniedException;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

//@ExtendWith(SpringExtension.class)

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
class RiderPreferredZoneControllerTest {

	@InjectMocks
	private RiderPreferredZoneController riderPreferredZoneController;

	@Mock
	private RiderPreferredZoneService riderPreferredZoneService;

	private static RiderPreferredZones preferredZones;

	private static RiderPreferredZoneDto preferredZoneDto;

	@BeforeAll
	static void setUp() {

		preferredZoneDto = RiderPreferredZoneDto.builder().riderProfileId("123").preferredZoneId("1")
				.preferredZoneName("Bangkok").build();

		preferredZones = new RiderPreferredZones();
		BeanUtils.copyProperties(preferredZoneDto, preferredZones);
	}

	@Test
	@Order(1)
	void shouldCreateRiderPreferredZone() throws AccessDeniedException {
		when(riderPreferredZoneService.savePreferredZone(preferredZoneDto)).thenReturn(preferredZones);
		ResponseEntity<RiderPreferredZoneDto> fetchedProfile = riderPreferredZoneController
				.savePreferredZone(preferredZoneDto);

		assertTrue(ObjectUtils.isNotEmpty(fetchedProfile.getBody()));
		assertEquals(HttpStatus.OK, fetchedProfile.getStatusCode());
		assertNotNull(preferredZoneDto.toString());
	}

	@Test
	void savePreferredZoneOpsMember() {
		when(riderPreferredZoneService.savePreferredZoneOpsMember(preferredZoneDto)).thenReturn(preferredZones);
		ResponseEntity<RiderPreferredZoneDto> fetchedProfile = riderPreferredZoneController
				.savePreferredZoneOpsMember(Constants.OPS_MEMBER, preferredZoneDto);

		assertTrue(ObjectUtils.isNotEmpty(fetchedProfile.getBody()));
		assertEquals(HttpStatus.OK, fetchedProfile.getStatusCode());
		assertNotNull(preferredZoneDto.toString());
	}

}