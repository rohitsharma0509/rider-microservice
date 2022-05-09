package com.scb.rider.controller;

import com.scb.rider.constants.Constants;
import com.scb.rider.model.document.RiderSelectedTrainingAppointment;
import com.scb.rider.model.dto.RiderSearchProfileDto;
import com.scb.rider.model.dto.training.RiderTrainingAppointmentDetailsDto;
import com.scb.rider.model.dto.training.RiderTrainingAppointmentStatusResponse;
import com.scb.rider.model.enumeration.RiderStatus;
import com.scb.rider.model.enumeration.RiderTrainingStatus;
import com.scb.rider.model.enumeration.TrainingType;
import com.scb.rider.service.document.RiderTrainingAppointmentService;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RiderTrainingAppointmentControllerTest {

	private static final String RIDER_ID = "609a5be7a769cc6adc3cbc98";

	@InjectMocks
	private RiderTrainingAppointmentController appointmentController;
	
	@Mock
	private RiderTrainingAppointmentService appointmentService;

	private static RiderSelectedTrainingAppointment trainingAppointment;
	private static RiderTrainingAppointmentDetailsDto trainingAppointmentDto;
	private static RiderTrainingAppointmentStatusResponse trainingStatus;
	
	@BeforeAll
	static void setup() {
		trainingAppointmentDto = RiderTrainingAppointmentDetailsDto.builder()
				.riderId("R123456789qwertyuio")
				.appointmentId("A123456789qwertyuio")
				.venue("City Square")
				.date(LocalDate.now())
				.startTime(LocalTime.now())
				.endTime(LocalTime.now().plusHours(2))
				.status(RiderTrainingStatus.PENDING)
				.build();
		trainingStatus = new RiderTrainingAppointmentStatusResponse();
		trainingAppointment = new RiderSelectedTrainingAppointment();
		
		BeanUtils.copyProperties(trainingAppointmentDto, trainingAppointment);
		
		trainingStatus.setRiderId(trainingAppointment.getRiderId());
		trainingStatus.setStatus(trainingAppointment.getStatus());
		
	}
	
	@Test
    @Order(1)
	void test_toCreateNewAppointment() {
		Mockito.when(appointmentService.saveSelectedAppointment(trainingAppointmentDto))
				.thenReturn(trainingAppointment);
		ResponseEntity<RiderTrainingAppointmentDetailsDto> responseTrainingAppointment = appointmentController.updateRiderTrainingAppointmentByRiderId(Constants.OPS_MEMBER, trainingAppointmentDto);
		
		assertTrue(ObjectUtils.isNotEmpty(responseTrainingAppointment.getBody()));
        assertEquals(HttpStatus.OK, responseTrainingAppointment.getStatusCode());
        
        RiderTrainingAppointmentDetailsDto dto = responseTrainingAppointment.getBody();
        assertEquals("R123456789qwertyuio", dto.getRiderId());
        assertEquals("A123456789qwertyuio", dto.getAppointmentId());
        assertEquals("City Square", dto.getVenue());
        
	}

	@Test
    @Order(2)
	void test_toUpdateAppointment() {
		RiderTrainingAppointmentDetailsDto newTrainingAppointmentDto = RiderTrainingAppointmentDetailsDto.builder().appointmentId("A0123456789qwertyui").build();
		trainingAppointment.setAppointmentId("A0123456789qwertyui");
		Mockito.when(appointmentService.saveSelectedAppointment(newTrainingAppointmentDto))
				.thenReturn(trainingAppointment);
		
		ResponseEntity<RiderTrainingAppointmentDetailsDto> responseNewTrainingAppointment = appointmentController.updateRiderTrainingAppointmentByRiderId(Constants.OPS_MEMBER, newTrainingAppointmentDto);

		assertTrue(ObjectUtils.isNotEmpty(responseNewTrainingAppointment.getBody()));
		assertEquals(HttpStatus.OK, responseNewTrainingAppointment.getStatusCode());

		RiderTrainingAppointmentDetailsDto dto = responseNewTrainingAppointment.getBody();
		assertEquals("R123456789qwertyuio", dto.getRiderId());
		assertEquals("A0123456789qwertyui", dto.getAppointmentId());
	}
	
	@Test
    @Order(3)
	void test_toFetchAppointmentDetailsByRiderId() {
		Mockito.when(appointmentService.getAppointmentByProfileId(trainingAppointmentDto.getRiderId(), TrainingType.FOOD))
				.thenReturn(trainingAppointment);
		ResponseEntity<RiderTrainingAppointmentDetailsDto> responseTrainingAppointment = appointmentController
				.getRiderTrainingAppointmentDetailsByRiderId(trainingAppointmentDto.getRiderId(), TrainingType.FOOD);

		assertTrue(ObjectUtils.isNotEmpty(responseTrainingAppointment.getBody()));
		assertEquals(HttpStatus.OK, responseTrainingAppointment.getStatusCode());

		RiderTrainingAppointmentDetailsDto dto = responseTrainingAppointment.getBody();
		assertEquals("R123456789qwertyuio", dto.getRiderId());
		assertEquals("A0123456789qwertyui", dto.getAppointmentId());
		assertEquals("City Square", dto.getVenue());

	}
	
	@Test
    @Order(4)
	void test_toFetchAppointmentStatusByRiderId() {
		Mockito.when(appointmentService.getAppointmentStatusByProfileId(trainingAppointmentDto.getRiderId(), TrainingType.FOOD))
		.thenReturn(trainingStatus);
		ResponseEntity<RiderTrainingAppointmentStatusResponse> responseTrainingStatus = appointmentController
				.getRiderTrainingStatusByRiderId(trainingAppointmentDto.getRiderId(), TrainingType.FOOD);

		assertTrue(ObjectUtils.isNotEmpty(responseTrainingStatus.getBody()));
		assertEquals(HttpStatus.OK, responseTrainingStatus.getStatusCode());

		RiderTrainingAppointmentStatusResponse dto = responseTrainingStatus.getBody();
		assertEquals("R123456789qwertyuio", dto.getRiderId());
		assertEquals(RiderTrainingStatus.PENDING, dto.getStatus());

	}
	
	@Test
    @Order(5)
	void test_toUpdateAppointmentStatusByRiderId() {
		trainingStatus.setStatus(RiderTrainingStatus.COMPLETED);
		Mockito.when(appointmentService.getAppointmentStatusByProfileId(trainingAppointmentDto.getRiderId(), TrainingType.FOOD))
		.thenReturn(trainingStatus);
		ResponseEntity<RiderTrainingAppointmentStatusResponse> responseTrainingStatus = appointmentController
				.getRiderTrainingStatusByRiderId(trainingAppointmentDto.getRiderId(), TrainingType.FOOD);

		assertTrue(ObjectUtils.isNotEmpty(responseTrainingStatus.getBody()));
		assertEquals(HttpStatus.OK, responseTrainingStatus.getStatusCode());

		RiderTrainingAppointmentStatusResponse dto = responseTrainingStatus.getBody();
		assertEquals("R123456789qwertyuio", dto.getRiderId());
		assertEquals(RiderTrainingStatus.COMPLETED, dto.getStatus());

	}

	@Test
	@Order(6)
	void test_toGetRidersByTrainingAppointmentId() {
		RiderSearchProfileDto riderSearchProfileDto = RiderSearchProfileDto.builder().id("600aa386ace87a69976c3dfc").riderId("600aa384ffea9a03b2cae891")
				.firstName("Amit").lastName("Kumar").phoneNumber("454958515").status(RiderStatus.AUTHORIZED).build();
		List<RiderSearchProfileDto> riderSearchProfileDtoList = new ArrayList<>();
		riderSearchProfileDtoList.add(riderSearchProfileDto);
		Mockito.lenient().when(appointmentService.getRidersListBySlotId(riderSearchProfileDto.getId())).thenReturn(riderSearchProfileDtoList);

		assertEquals("600aa384ffea9a03b2cae891", riderSearchProfileDtoList.get(0).getRiderId());
		assertEquals("600aa386ace87a69976c3dfc", riderSearchProfileDtoList.get(0).getId());
		assertEquals("Amit", riderSearchProfileDtoList.get(0).getFirstName());
		assertEquals("Kumar", riderSearchProfileDtoList.get(0).getLastName());
		assertEquals("454958515", riderSearchProfileDtoList.get(0).getPhoneNumber());
		assertEquals(RiderStatus.AUTHORIZED, riderSearchProfileDtoList.get(0).getStatus());
	}

	@Test
	void getAllAppointmentsByRiderIdTest() {
		RiderSelectedTrainingAppointment appointment = RiderSelectedTrainingAppointment.builder()
				.riderId(RIDER_ID).trainingType(TrainingType.FOOD).build();
		Mockito.when(appointmentService.getAllTrainingAppointmentsByRiderId(Mockito.eq(RIDER_ID))).thenReturn(Arrays.asList(appointment));
		ResponseEntity<List<RiderTrainingAppointmentDetailsDto>> result = appointmentController.getAllAppointmentsByRiderId(RIDER_ID);
		assertEquals(RIDER_ID, result.getBody().get(0).getRiderId());
		assertEquals(TrainingType.FOOD, result.getBody().get(0).getTrainingType());
	}

}