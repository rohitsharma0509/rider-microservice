package com.scb.rider.service;

import com.scb.rider.client.LocationServiceFeignClient;
import com.scb.rider.client.OperationFeignClient;
import com.scb.rider.constants.Constants;
import com.scb.rider.constants.DocumentType;
import com.scb.rider.exception.DataNotFoundException;
import com.scb.rider.exception.TrainingAlreadyCompletedException;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.document.RiderSelectedTrainingAppointment;
import com.scb.rider.model.document.RiderUploadedDocument;
import com.scb.rider.model.dto.RiderSearchProfileDto;
import com.scb.rider.model.dto.training.*;
import com.scb.rider.model.enumeration.RiderTrainingStatus;
import com.scb.rider.model.enumeration.TrainingType;
import com.scb.rider.repository.RiderProfileRepository;
import com.scb.rider.repository.RiderTrainingAppointmentRepository;
import com.scb.rider.repository.RiderUploadedDocumentRepository;
import com.scb.rider.service.document.RiderTrainingAppointmentService;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RiderTrainingAppointmentServiceTest {

	private static final String RIDER_ID = "609a5be7a769cc6adc3cbc98";

	@Mock
    private RiderProfileRepository riderProfileRepository;

	@Mock
	private RiderTrainingAppointmentRepository appointmentRepository;

	@Mock
	private OperationFeignClient operationFeignClient;
	
	@InjectMocks
	private RiderTrainingAppointmentService appointmentService;

	@Mock
	private RiderUploadedDocumentRepository riderUploadedDocumentRepository;

	@Mock
	private LocationServiceFeignClient locationServiceFeignClient;

	private static RiderTrainingAppointmentDetailsDto appointmentDetailsDto;
	private static RiderSelectedTrainingAppointment selectedTrainingAppointment;
	private static RiderTrainingAppointmentStatusResponse statusResponse;
	private static RiderProfile riderProfile;
	private static TrainingDto trainingDto;
	
	@BeforeAll
	static void setup() {

		appointmentDetailsDto = RiderTrainingAppointmentDetailsDto.builder()
				.riderId("R123456789qwertyuio")
				.appointmentId("A123456789qwertyuio")
				.venue("City Square")
				.date(LocalDate.now())
				.startTime(LocalTime.now())
				.endTime(LocalTime.now().plusHours(2))
				.status(RiderTrainingStatus.PENDING)
				.trainingType(TrainingType.FOOD)
				.build();
		statusResponse = new RiderTrainingAppointmentStatusResponse();
		selectedTrainingAppointment = new RiderSelectedTrainingAppointment();
		riderProfile = new RiderProfile();
		
		BeanUtils.copyProperties(appointmentDetailsDto, selectedTrainingAppointment);
		
		statusResponse.setRiderId(selectedTrainingAppointment.getRiderId());
		statusResponse.setStatus(selectedTrainingAppointment.getStatus());

		riderProfile.setRiderId(appointmentDetailsDto.getRiderId());
		riderProfile.setId(appointmentDetailsDto.getRiderId());
		riderProfile.setCreatedDate(LocalDateTime.now().minusDays(10));
		
		trainingDto = TrainingDto.builder()
				.id("A123456789qwertyuio")
				.venue("City Square")
				.startTime(LocalTime.now())
				.endTime(LocalTime.now().plusHours(2))
				.date(LocalDate.now())
				.build();
		
	}

	@Test
	void getAvailableSlotsByRiderTestForException() {
		Mockito.when(operationFeignClient.getTrainingSlotsByDateForRider("2021-10-20", TrainingType.FOOD.name()))
				.thenThrow(new NullPointerException());
		assertThrows(DataNotFoundException.class, () -> appointmentService.getAvailableSlotsByRider("2021-10-20", TrainingType.FOOD.name()));
	}

	@Test
	void getAvailableSlotsByRiderTests() {
		Mockito.when(operationFeignClient.getTrainingSlotsByDateForRider("2021-10-20", TrainingType.FOOD.name()))
				.thenReturn(new Object());
		Object result = appointmentService.getAvailableSlotsByRider("2021-10-20", TrainingType.FOOD.name());
		assertNotNull(result);
	}
	
	@Test
	void test_toGetAppointmentByProfileId() {
		Mockito.when(appointmentRepository.findByRiderIdAndTrainingType(appointmentDetailsDto.getRiderId(), TrainingType.FOOD)).thenReturn(Optional.of(selectedTrainingAppointment));
		RiderSelectedTrainingAppointment appointmentDetails = appointmentService.getAppointmentByProfileId(appointmentDetailsDto.getRiderId(), TrainingType.FOOD);
		assertTrue(ObjectUtils.isNotEmpty(appointmentDetails));
		assertNotNull(appointmentDetails);
		assertEquals("R123456789qwertyuio", appointmentDetails.getRiderId());
		assertEquals("A123456789qwertyuio", appointmentDetails.getAppointmentId());
		assertEquals("City Square", appointmentDetails.getVenue());
	}

	@Test
	void throwException_getAppointmentDetailsByProfileId() {
		Mockito.when(appointmentRepository.findByRiderIdAndTrainingType(appointmentDetailsDto.getRiderId(), TrainingType.FOOD)).thenReturn(Optional.empty());
		assertThrows(DataNotFoundException.class, () -> appointmentService.getAppointmentByProfileId(appointmentDetailsDto.getRiderId(), TrainingType.FOOD));
	}

	@Test
	void testGetBookedTrainingAppointmentsByRiderId() {
		RiderSelectedTrainingAppointment appointment = RiderSelectedTrainingAppointment.builder()
			.riderId(RIDER_ID).trainingType(TrainingType.FOOD).build();
		Mockito.when(appointmentRepository.findByRiderId(Mockito.eq(RIDER_ID))).thenReturn(Arrays.asList(appointment));
		List<RiderSelectedTrainingAppointment> result = appointmentService.getBookedTrainingAppointmentsByRiderId(RIDER_ID);
		assertEquals(RIDER_ID, result.get(0).getRiderId());
		assertEquals(TrainingType.FOOD, result.get(0).getTrainingType());
	}

	@Test
	void getAllTrainingAppointmentsByRiderIdTestWhenNoRequiredTrainingAvailable() {
		Mockito.when(operationFeignClient.getAllRequiredTrainings()).thenReturn(Collections.emptyList());
		List<RiderSelectedTrainingAppointment> result = appointmentService.getAllTrainingAppointmentsByRiderId(RIDER_ID);
		assertEquals(0, result.size());
		Mockito.verifyNoMoreInteractions(appointmentRepository);
	}

	@Test
	void getAllTrainingAppointmentsByRiderIdTestWhenRequiredTrainingAvailableAndRiderNotScheduledRequiredTraining() {
		Mockito.when(operationFeignClient.getAllRequiredTrainings()).thenReturn(Arrays.asList(TrainingType.FOOD.name()));
		Mockito.when(appointmentRepository.findByRiderIdAndTrainingTypeIn(Mockito.eq(RIDER_ID), Mockito.anyList())).thenReturn(new ArrayList<>());
		List<RiderSelectedTrainingAppointment> result = appointmentService.getAllTrainingAppointmentsByRiderId(RIDER_ID);
		assertEquals(1, result.size());
	}

	@Test
	void getAllTrainingAppointmentsByRiderIdTestWhenRequiredTrainingAvailableAndRiderScheduledRequiredTraining() {
		Mockito.when(operationFeignClient.getAllRequiredTrainings()).thenReturn(Arrays.asList(TrainingType.FOOD.name()));
		RiderSelectedTrainingAppointment appointment = RiderSelectedTrainingAppointment.builder().riderId(RIDER_ID).trainingType(TrainingType.FOOD).build();
		List<RiderSelectedTrainingAppointment> appointments = new ArrayList<>();
		appointments.add(appointment);
		Mockito.when(appointmentRepository.findByRiderIdAndTrainingTypeIn(Mockito.eq(RIDER_ID), Mockito.anyList())).thenReturn(appointments);
		List<RiderSelectedTrainingAppointment> result = appointmentService.getAllTrainingAppointmentsByRiderId(RIDER_ID);
		assertEquals(1, result.size());
	}
	
	@Test
	void test_toGetAppointmentStatusByProfileId() {
		Mockito.when(appointmentRepository.findByRiderIdAndTrainingType(appointmentDetailsDto.getRiderId(), TrainingType.FOOD)).thenReturn(Optional.of(selectedTrainingAppointment));
		RiderTrainingAppointmentStatusResponse appointmentStatus = appointmentService.getAppointmentStatusByProfileId(appointmentDetailsDto.getRiderId(), TrainingType.FOOD);
		assertTrue(ObjectUtils.isNotEmpty(appointmentStatus));
		assertNotNull(appointmentStatus);
		assertEquals("R123456789qwertyuio", appointmentStatus.getRiderId());
		assertEquals(RiderTrainingStatus.PENDING, appointmentStatus.getStatus());
	}
	
	@Test
	void throwException_getAppointmentStatusByProfileId() {
		Mockito.when(appointmentRepository.findByRiderIdAndTrainingType(appointmentDetailsDto.getRiderId(), TrainingType.FOOD)).thenReturn(Optional.empty());
		assertThrows(DataNotFoundException.class, () -> appointmentService.getAppointmentStatusByProfileId(appointmentDetailsDto.getRiderId(), TrainingType.FOOD));
	}
	
	@Test
	void test_toUpdateAppointmentStatusByProfileId() {
		RiderProfile riderProfile = new RiderProfile();
		riderProfile.setId("1234");
		RiderSelectedTrainingAppointment tempTrainingAppointment = new RiderSelectedTrainingAppointment();
		BeanUtils.copyProperties(selectedTrainingAppointment, tempTrainingAppointment);
		Mockito.when(riderProfileRepository.findById(appointmentDetailsDto.getRiderId())).thenReturn(Optional.of(riderProfile));
		Mockito.when(appointmentRepository.findByRiderIdAndTrainingType(riderProfile.getId(), TrainingType.FOOD)).thenReturn(Optional.of(tempTrainingAppointment));
		Mockito.when(riderUploadedDocumentRepository.findByRiderProfileIdAndDocumentType(riderProfile.getId(), DocumentType.BACKGROUND_VERIFICATION_FORM)).thenReturn(Optional.empty());

		RiderTrainingStatusUpdateDto updateDto = RiderTrainingStatusUpdateDto.builder()
				.riderId(appointmentDetailsDto.getRiderId())
				.status(RiderTrainingStatus.COMPLETED)
				.trainingType(TrainingType.FOOD)
				.completionDate(appointmentDetailsDto.getDate()).build();
		RiderTrainingStatusUpdateDto appointmentStatus = appointmentService.updateAppointmentStatusByProfileId(updateDto, Constants.OPS_MEMBER);
		assertTrue(ObjectUtils.isNotEmpty(appointmentStatus));
		assertNotNull(appointmentStatus);
		assertEquals("R123456789qwertyuio", appointmentStatus.getRiderId());
		assertEquals(RiderTrainingStatus.COMPLETED, appointmentStatus.getStatus());
	}

	@Test
	void updateAppointmentStatusByProfileIdShouldMarkRiderStageToStage3() {
		RiderProfile riderProfile = new RiderProfile();
		riderProfile.setId("1234");
		RiderSelectedTrainingAppointment tempTrainingAppointment = new RiderSelectedTrainingAppointment();
		BeanUtils.copyProperties(selectedTrainingAppointment, tempTrainingAppointment);
		Mockito.when(riderProfileRepository.findById(appointmentDetailsDto.getRiderId())).thenReturn(Optional.of(riderProfile));
		Mockito.when(appointmentRepository.findByRiderIdAndTrainingType(riderProfile.getId(), TrainingType.FOOD)).thenReturn(Optional.of(tempTrainingAppointment));
		RiderUploadedDocument document = RiderUploadedDocument.builder().build();
		Mockito.when(riderUploadedDocumentRepository.findByRiderProfileIdAndDocumentType(riderProfile.getId(), DocumentType.BACKGROUND_VERIFICATION_FORM))
				.thenReturn(Optional.of(document));

		RiderTrainingStatusUpdateDto updateDto = RiderTrainingStatusUpdateDto.builder()
				.riderId(appointmentDetailsDto.getRiderId())
				.status(RiderTrainingStatus.COMPLETED)
				.trainingType(TrainingType.FOOD)
				.completionDate(appointmentDetailsDto.getDate()).build();
		RiderTrainingStatusUpdateDto appointmentStatus = appointmentService.updateAppointmentStatusByProfileId(updateDto, Constants.OPS_MEMBER);
		Mockito.verify(riderProfileRepository, Mockito.times(1)).save(Mockito.any(RiderProfile.class));
		assertEquals(RiderTrainingStatus.COMPLETED, appointmentStatus.getStatus());
	}

	@Test
	void updateAppointmentStatusByProfileIdShouldMarkRiderEligibleForMartTraining() {
		RiderProfile riderProfile = new RiderProfile();
		riderProfile.setId("1234");
		RiderSelectedTrainingAppointment tempTrainingAppointment = new RiderSelectedTrainingAppointment();
		BeanUtils.copyProperties(selectedTrainingAppointment, tempTrainingAppointment);
		Mockito.when(riderProfileRepository.findById(appointmentDetailsDto.getRiderId())).thenReturn(Optional.of(riderProfile));
		Mockito.when(appointmentRepository.findByRiderIdAndTrainingType(riderProfile.getId(), TrainingType.MART)).thenReturn(Optional.of(tempTrainingAppointment));
		RiderTrainingStatusUpdateDto updateDto = RiderTrainingStatusUpdateDto.builder()
				.riderId(appointmentDetailsDto.getRiderId())
				.status(RiderTrainingStatus.COMPLETED)
				.trainingType(TrainingType.MART)
				.completionDate(appointmentDetailsDto.getDate()).build();
		RiderTrainingStatusUpdateDto appointmentStatus = appointmentService.updateAppointmentStatusByProfileId(updateDto, Constants.OPS_MEMBER);
		Mockito.verify(locationServiceFeignClient, Mockito.times(1)).updateRiderToMartRider(riderProfile.getId(), Boolean.TRUE);
		assertEquals(RiderTrainingStatus.COMPLETED, appointmentStatus.getStatus());
	}

	@Test
	void updateAppointmentStatusByProfileIdShouldMarkRiderEligibleForExpressTraining() {
		RiderProfile riderProfile = new RiderProfile();
		riderProfile.setId("1234");
		RiderSelectedTrainingAppointment tempTrainingAppointment = new RiderSelectedTrainingAppointment();
		BeanUtils.copyProperties(selectedTrainingAppointment, tempTrainingAppointment);
		Mockito.when(riderProfileRepository.findById(appointmentDetailsDto.getRiderId())).thenReturn(Optional.of(riderProfile));
		Mockito.when(appointmentRepository.findByRiderIdAndTrainingType(riderProfile.getId(), TrainingType.EXPRESS)).thenReturn(Optional.of(tempTrainingAppointment));
		RiderTrainingStatusUpdateDto updateDto = RiderTrainingStatusUpdateDto.builder()
				.riderId(appointmentDetailsDto.getRiderId())
				.status(RiderTrainingStatus.COMPLETED)
				.trainingType(TrainingType.EXPRESS)
				.completionDate(appointmentDetailsDto.getDate()).build();
		RiderTrainingStatusUpdateDto appointmentStatus = appointmentService.updateAppointmentStatusByProfileId(updateDto, Constants.OPS_MEMBER);
		Mockito.verify(locationServiceFeignClient, Mockito.times(1)).updateRiderToExpressRider(riderProfile.getId(), Boolean.TRUE);
		assertEquals(RiderTrainingStatus.COMPLETED, appointmentStatus.getStatus());
	}

	@Test
	void updateAppointmentStatusByProfileIdShouldMarkRiderEligibleForPointXTraining() {
		RiderProfile riderProfile = new RiderProfile();
		riderProfile.setId("1234");
		RiderSelectedTrainingAppointment tempTrainingAppointment = new RiderSelectedTrainingAppointment();
		BeanUtils.copyProperties(selectedTrainingAppointment, tempTrainingAppointment);
		Mockito.when(riderProfileRepository.findById(appointmentDetailsDto.getRiderId())).thenReturn(Optional.of(riderProfile));
		Mockito.when(appointmentRepository.findByRiderIdAndTrainingType(riderProfile.getId(), TrainingType.POINTX)).thenReturn(Optional.of(tempTrainingAppointment));
		RiderTrainingStatusUpdateDto updateDto = RiderTrainingStatusUpdateDto.builder()
				.riderId(appointmentDetailsDto.getRiderId())
				.status(RiderTrainingStatus.COMPLETED)
				.trainingType(TrainingType.POINTX)
				.completionDate(appointmentDetailsDto.getDate()).build();
		RiderTrainingStatusUpdateDto appointmentStatus = appointmentService.updateAppointmentStatusByProfileId(updateDto, Constants.OPS_MEMBER);
		Mockito.verify(locationServiceFeignClient, Mockito.times(1)).updateRiderToPointXRider(riderProfile.getId(), Boolean.TRUE);
		assertEquals(RiderTrainingStatus.COMPLETED, appointmentStatus.getStatus());
	}

	@Test
	void throwException_updateAppointmentStatusByProfileId() {
		Mockito.when(riderProfileRepository.findById(appointmentDetailsDto.getRiderId())).thenReturn(Optional.empty());
		RiderTrainingStatusUpdateDto updateDto = RiderTrainingStatusUpdateDto.builder()
				.riderId(appointmentDetailsDto.getRiderId())
				.status(RiderTrainingStatus.COMPLETED)
				.trainingType(TrainingType.FOOD)
				.completionDate(appointmentDetailsDto.getCompletionDate()).build();
		assertThrows(DataNotFoundException.class, () -> appointmentService.updateAppointmentStatusByProfileId(updateDto, Constants.OPS_MEMBER));
	}
	
	@Test
	void test_toSaveSelectedAppointment() {
		Mockito.when(appointmentRepository.save(selectedTrainingAppointment)).thenReturn(selectedTrainingAppointment);
		Mockito.when(appointmentRepository.findByRiderIdAndTrainingType(appointmentDetailsDto.getRiderId(), TrainingType.FOOD)).thenReturn(Optional.empty());
		Mockito.when(riderProfileRepository.findById(appointmentDetailsDto.getRiderId())).thenReturn(Optional.of(riderProfile));
		SeatsUpdate seatsUpdate = new SeatsUpdate();
		Boolean respBool = true;
		Mockito.when(operationFeignClient.updateOccupiedSlotSeats(Mockito.any())).thenReturn(respBool);
		Mockito.when(operationFeignClient.getTrainingSlotDetails(Mockito.any())).thenReturn(trainingDto);
		
		RiderSelectedTrainingAppointment trainingAppointment = appointmentService.saveSelectedAppointment(appointmentDetailsDto);
		assertTrue(ObjectUtils.isNotEmpty(trainingAppointment));
		assertNotNull(trainingAppointment);
		assertEquals("R123456789qwertyuio", trainingAppointment.getRiderId());
		assertEquals("A123456789qwertyuio", trainingAppointment.getAppointmentId());
		assertEquals("City Square", trainingAppointment.getVenue());
	}

	@Test
	void throwException_whenTrainingAlreadyCompleted_saveAppointmentDetailsByProfileId() {
		Mockito.when(riderProfileRepository.findById(appointmentDetailsDto.getRiderId())).thenReturn(Optional.of(riderProfile));
		RiderSelectedTrainingAppointment training = RiderSelectedTrainingAppointment.builder().status(RiderTrainingStatus.COMPLETED).build();
		Mockito.when(appointmentRepository.findByRiderIdAndTrainingType(appointmentDetailsDto.getRiderId(), TrainingType.FOOD)).thenReturn(Optional.of(training));
		assertThrows(TrainingAlreadyCompletedException.class, () -> appointmentService.saveSelectedAppointment(appointmentDetailsDto));
	}
	
	@Test
	void throwException_saveAppointmentDetailsByProfileId() {
		Mockito.when(riderProfileRepository.findById(appointmentDetailsDto.getRiderId())).thenReturn(Optional.empty());
		assertThrows(DataNotFoundException.class, () -> appointmentService.saveSelectedAppointment(appointmentDetailsDto));
	}
	
	@Test
	void test_toUpdateSelectedAppointment() {
		Mockito.when(appointmentRepository.save(selectedTrainingAppointment)).thenReturn(selectedTrainingAppointment);
		Mockito.when(appointmentRepository.findByRiderIdAndTrainingType(appointmentDetailsDto.getRiderId(), TrainingType.FOOD)).thenReturn(Optional.of(selectedTrainingAppointment));
		Mockito.when(riderProfileRepository.findById(appointmentDetailsDto.getRiderId())).thenReturn(Optional.of(riderProfile));
		SeatsUpdate seatsUpdate = new SeatsUpdate();
		Boolean respBool = true;
		Mockito.when(operationFeignClient.updateOccupiedSlotSeats(Mockito.any())).thenReturn(respBool);
		Mockito.when(operationFeignClient.getTrainingSlotDetails(Mockito.any())).thenReturn(trainingDto);
		
		RiderSelectedTrainingAppointment trainingAppointment = appointmentService.saveSelectedAppointment(appointmentDetailsDto);
		assertTrue(ObjectUtils.isNotEmpty(trainingAppointment));
		assertNotNull(trainingAppointment);
		assertEquals("R123456789qwertyuio", trainingAppointment.getRiderId());
		assertEquals("A123456789qwertyuio", trainingAppointment.getAppointmentId());
		assertEquals("City Square", trainingAppointment.getVenue());

	}

	@Test
	void test_toGetRidersListBySlotId() {
		Mockito.when(operationFeignClient.getTrainingSlotDetails(Mockito.any())).thenReturn(trainingDto);
		trainingDto.setReserved(3);
		List<String> riderId = new ArrayList<>();
		riderId.add(selectedTrainingAppointment.getRiderId());
		RiderProfile riderProfile = new RiderProfile();
		riderProfile.setRiderId("R123456789qwertyuio");
		riderProfile.setFirstName("Amit");
		riderProfile.setLastName("Kumar");
		riderProfile.setPhoneNumber("454958515");
		List<RiderProfile> riderProfiles = new ArrayList<>();
		riderProfiles.add(riderProfile);
		Mockito.lenient().when(appointmentService.getRidersListBySlotId("A123456789qwertyuio")).thenReturn(RiderSearchProfileDto.of(riderProfiles));

		assertNotNull(trainingDto.getId());
		assertEquals("A123456789qwertyuio", trainingDto.getId());
		assertEquals("R123456789qwertyuio", riderProfiles.get(0).getRiderId());
		assertEquals("Amit", riderProfiles.get(0).getFirstName());
		assertEquals("Kumar", riderProfiles.get(0).getLastName());
		assertEquals("454958515", riderProfiles.get(0).getPhoneNumber());
	}
	
}
