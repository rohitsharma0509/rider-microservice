package com.scb.rider.service.document;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import com.scb.rider.client.LocationServiceFeignClient;
import com.scb.rider.exception.TrainingSlotEmptyException;
import com.scb.rider.model.dto.RiderSearchProfileDto;
import com.scb.rider.model.enumeration.TrainingType;
import com.scb.rider.repository.RiderSearchRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.scb.rider.client.OperationFeignClient;
import com.scb.rider.constants.DocumentType;
import com.scb.rider.exception.SeatAlreadyOccupiedException;
import com.scb.rider.exception.TrainingAlreadyCompletedException;
import com.scb.rider.exception.AppointmentIdNotFoundException;
import com.scb.rider.exception.DataNotFoundException;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.document.RiderSelectedTrainingAppointment;
import com.scb.rider.model.document.RiderUploadedDocument;
import com.scb.rider.model.dto.training.RiderTrainingAppointmentDetailsDto;
import com.scb.rider.model.dto.training.RiderTrainingAppointmentStatusResponse;
import com.scb.rider.model.dto.training.RiderTrainingStatusUpdateDto;
import com.scb.rider.model.dto.training.SeatsUpdate;
import com.scb.rider.model.dto.training.SlotData;
import com.scb.rider.model.dto.training.TrainingDto;
import com.scb.rider.model.enumeration.RiderProfileStage;
import com.scb.rider.model.enumeration.RiderTrainingStatus;
import com.scb.rider.repository.RiderProfileRepository;
import com.scb.rider.repository.RiderTrainingAppointmentRepository;
import com.scb.rider.repository.RiderUploadedDocumentRepository;
import com.scb.rider.util.CustomBeanUtils;
import com.scb.rider.util.LoggerUtils;

import lombok.extern.log4j.Log4j2;
import org.springframework.util.CollectionUtils;

@Service
@Log4j2
public class RiderTrainingAppointmentService {

	private static final String RIDER_ID_TEXT = "riderId";

	@Autowired
	private RiderTrainingAppointmentRepository appointmentRepository;

	@Autowired
	private RiderProfileRepository riderProfileRepository;
	
	@Autowired
	private OperationFeignClient operationFeignClient;

	@Autowired
	private RiderSearchRepository riderSearchRepository;

	@Autowired
	private LocationServiceFeignClient locationServiceFeignClient;

	@Autowired
	private RiderUploadedDocumentRepository riderUploadedDocumentRepository;

	public Object getAvailableSlotsByRider(String onboardDate, String trainingType) {
		Object availableSlotsForRider;
		try {
			log.info("Fetching available training slots for the rider onboarded on : {}, trainingType: {}", onboardDate, trainingType);
			availableSlotsForRider = this.operationFeignClient.getTrainingSlotsByDateForRider(onboardDate, trainingType);
		} catch (Exception e) {
			log.error("Unable to trigger the reconciliation process ", e);
			throw new DataNotFoundException("Unable find training slots for the date -: " + onboardDate); 
		}
		return availableSlotsForRider;
	}
	
	public RiderSelectedTrainingAppointment getAppointmentByProfileId(String riderId, TrainingType trainingType) {
		log.info("Getting Rider's Training Details by profile id = {}, trainingType = {}", riderId, trainingType);
		return appointmentRepository.findByRiderIdAndTrainingType(riderId, trainingType)
				.orElseThrow( ()-> LoggerUtils.logError(RiderSelectedTrainingAppointment.class, riderId, RIDER_ID_TEXT));
	}

	public List<RiderSelectedTrainingAppointment> getBookedTrainingAppointmentsByRiderId(String riderId) {
		log.info("Getting Rider's booked appointments by profile id = {}", riderId);
		return appointmentRepository.findByRiderId(riderId);
	}

	public List<RiderSelectedTrainingAppointment> getAllTrainingAppointmentsByRiderId(String riderId) {
		log.info("Getting Rider's appointments by profile id = {}", riderId);
		List<String> requiredTrainings = operationFeignClient.getAllRequiredTrainings();
		List<RiderSelectedTrainingAppointment> orderedAppointments = new LinkedList<>();
		if(!CollectionUtils.isEmpty(requiredTrainings)) {
			List<RiderSelectedTrainingAppointment> bookedAppointments = appointmentRepository.findByRiderIdAndTrainingTypeIn(riderId, requiredTrainings);
			for (String requiredTraining : requiredTrainings) {
				TrainingType trainingType = TrainingType.valueOf(requiredTraining);
				Optional<RiderSelectedTrainingAppointment> bookedAppointment = bookedAppointments.stream().filter(app -> trainingType.equals(app.getTrainingType())).findFirst();
				if (bookedAppointment.isPresent()) {
					orderedAppointments.add(bookedAppointment.get());
				} else {
					orderedAppointments.add(RiderSelectedTrainingAppointment.builder().riderId(riderId).trainingType(trainingType).build());
				}
			}
		}
		return orderedAppointments;
	}
	
	public RiderTrainingAppointmentStatusResponse getAppointmentStatusByProfileId(final String riderId, TrainingType trainingType) {
		log.info("Getting Rider's Training Status by profile id = {}, trainingType = {}", riderId, trainingType);
		RiderSelectedTrainingAppointment trainingAppointment = appointmentRepository.findByRiderIdAndTrainingType(riderId, trainingType)
				.orElseThrow(() -> LoggerUtils.logError(RiderSelectedTrainingAppointment.class, riderId, RIDER_ID_TEXT));
		log.info("Found Rider's Status as {}", trainingAppointment.getStatus());
		return RiderTrainingAppointmentStatusResponse.builder()
				.riderId(trainingAppointment.getRiderId())
				.trainingType(trainingAppointment.getTrainingType())
				.status(trainingAppointment.getStatus())
				.build();
	}
	 
	public RiderTrainingStatusUpdateDto updateAppointmentStatusByProfileId(RiderTrainingStatusUpdateDto trainingUpdateDto, String updatedBy) {
		log.info("Getting Rider's Training Details by profile id = {}, trainingType = {}", trainingUpdateDto.getRiderId(), trainingUpdateDto.getTrainingType());

		RiderProfile riderProfile = riderProfileRepository.findById(trainingUpdateDto.getRiderId())
				.orElseThrow(() -> LoggerUtils.logError(RiderProfile.class, trainingUpdateDto.getRiderId(), RIDER_ID_TEXT));

		RiderSelectedTrainingAppointment trainingAppointment = appointmentRepository.findByRiderIdAndTrainingType(riderProfile.getId(), trainingUpdateDto.getTrainingType())
				.orElseThrow(() -> LoggerUtils.logError(RiderSelectedTrainingAppointment.class, trainingUpdateDto.getRiderId(), RIDER_ID_TEXT));

		trainingAppointment.setStatus(trainingUpdateDto.getStatus());

		if(RiderTrainingStatus.COMPLETED.equals(trainingUpdateDto.getStatus())) {
			trainingAppointment.setCompletionDate(Objects.nonNull(trainingUpdateDto.getCompletionDate()) ? trainingUpdateDto.getCompletionDate() : LocalDate.now());
			if(TrainingType.FOOD.equals(trainingUpdateDto.getTrainingType())) {
				Optional<RiderUploadedDocument> document = riderUploadedDocumentRepository.findByRiderProfileIdAndDocumentType(riderProfile.getId(), DocumentType.BACKGROUND_VERIFICATION_FORM);
				if (document.isPresent()) {
					riderProfile.setProfileStage(RiderProfileStage.STAGE_3);
					riderProfileRepository.save(riderProfile);
				}
			} else if(TrainingType.MART.equals(trainingUpdateDto.getTrainingType())) {
				locationServiceFeignClient.updateRiderToMartRider(riderProfile.getId(), Boolean.TRUE);
			} else if(TrainingType.EXPRESS.equals(trainingUpdateDto.getTrainingType())) {
				locationServiceFeignClient.updateRiderToExpressRider(riderProfile.getId(), Boolean.TRUE);
			} else if(TrainingType.POINTX.equals(trainingUpdateDto.getTrainingType())) {
				locationServiceFeignClient.updateRiderToPointXRider(riderProfile.getId(), Boolean.TRUE);
			}
		}
		trainingAppointment.setUpdatedBy(updatedBy);
		appointmentRepository.save(trainingAppointment);
		
		return RiderTrainingStatusUpdateDto.builder()
				.riderId(trainingAppointment.getRiderId())
				.trainingType(trainingAppointment.getTrainingType())
				.completionDate(trainingAppointment.getCompletionDate())
				.status(trainingAppointment.getStatus())
				.build();
	}
	
	public RiderSelectedTrainingAppointment saveSelectedAppointment(RiderTrainingAppointmentDetailsDto appointmentDto) {
		log.info("Checking Rider's existence by profile id = {}", appointmentDto.getRiderId());
		
		RiderProfile riderProfile = riderProfileRepository.findById(appointmentDto.getRiderId())
			      .orElseThrow(() -> LoggerUtils.logError(RiderProfile.class, appointmentDto.getRiderId(), RIDER_ID_TEXT));

		
		Optional<RiderSelectedTrainingAppointment> riderAppointment = appointmentRepository.findByRiderIdAndTrainingType(riderProfile.getId(), appointmentDto.getTrainingType());

		if (riderAppointment.isPresent() && RiderTrainingStatus.COMPLETED.equals(riderAppointment.get().getStatus())) {
			throw new TrainingAlreadyCompletedException("The training has already been completed for the rider : " + riderAppointment.get().getRiderId());
		}

		RiderTrainingAppointmentDetailsDto trainingSlotDetails = getTrainingSlotDetails(appointmentDto.getAppointmentId(), riderProfile.getId());
		
		boolean slotUpdateCheck;

		SlotData newSlot = SlotData.builder()
				.slotId(appointmentDto.getAppointmentId())
				.occupied(true)
				.build();
		
		if (riderAppointment.isPresent()) {
			SlotData previousSlot = SlotData.builder()
					.slotId(riderAppointment.get().getAppointmentId())
					.occupied(false)
					.build(); 

			List<SlotData> slotData = Arrays.asList(newSlot,previousSlot);
			
			SeatsUpdate seatsUpdate = SeatsUpdate.builder()
										.slotData(slotData)
										.build();

			log.info("Updating Rider's Selected Training Details for slots-{}",slotData);
			
			RiderSelectedTrainingAppointment newRiderAppointment;
			newRiderAppointment = riderAppointment.get();
			CustomBeanUtils.copyNonNullProperties(trainingSlotDetails, newRiderAppointment);
		
			slotUpdateCheck = operationFeignClient.updateOccupiedSlotSeats(seatsUpdate);
			if (!slotUpdateCheck) {
				throw new SeatAlreadyOccupiedException("No more available seats for the the slot id " + appointmentDto.getAppointmentId());
			}
			newRiderAppointment.setUpdatedBy(appointmentDto.getUpdatedBy());
			return this.appointmentRepository.save(newRiderAppointment);
		}
		
		log.info("Saving Rider's Selected Training Details ");
		RiderSelectedTrainingAppointment selectedTrainingAppointment = new RiderSelectedTrainingAppointment();
		BeanUtils.copyProperties(trainingSlotDetails, selectedTrainingAppointment);

		
		List<SlotData> slotData = Arrays.asList(newSlot);
		SeatsUpdate seatsUpdate = SeatsUpdate.builder()
									.slotData(slotData)
									.build();
		log.info("Updating Rider's Selected Training Details for first time for slot-{}",slotData);
		slotUpdateCheck = operationFeignClient.updateOccupiedSlotSeats(seatsUpdate);
		
		if (!slotUpdateCheck) throw new SeatAlreadyOccupiedException("No more available seats for the the slot id " + appointmentDto.getAppointmentId());

		selectedTrainingAppointment.setUpdatedBy(appointmentDto.getUpdatedBy());
		return this.appointmentRepository.save(selectedTrainingAppointment);
	}
	
	private RiderTrainingAppointmentDetailsDto getTrainingSlotDetails(String slotId, String riderId) {

		TrainingDto trainingSlotDetails;
		try {
			trainingSlotDetails = operationFeignClient.getTrainingSlotDetails(slotId);
		} catch (Exception e) {
			throw new AppointmentIdNotFoundException("The training slot not found with the id : " + slotId);
		}
		RiderTrainingAppointmentDetailsDto riderTrainingAppointmentDetailsDto = new RiderTrainingAppointmentDetailsDto();

		riderTrainingAppointmentDetailsDto.setRiderId(riderId);
		
		riderTrainingAppointmentDetailsDto.setAppointmentId(trainingSlotDetails.getId());
		riderTrainingAppointmentDetailsDto.setDate(trainingSlotDetails.getDate());
		riderTrainingAppointmentDetailsDto.setStartTime(trainingSlotDetails.getStartTime());
		riderTrainingAppointmentDetailsDto.setEndTime(trainingSlotDetails.getEndTime());
		riderTrainingAppointmentDetailsDto.setVenue(trainingSlotDetails.getVenue());
		riderTrainingAppointmentDetailsDto.setTrainingType(trainingSlotDetails.getTrainingType());
		riderTrainingAppointmentDetailsDto.setStatus(RiderTrainingStatus.PENDING);
		return riderTrainingAppointmentDetailsDto;
		
	}

	public List<RiderSearchProfileDto> getRidersListBySlotId(String appointmentId) {
		TrainingDto trainingSlotDetails;
		try {
			trainingSlotDetails = operationFeignClient.getTrainingSlotDetails(appointmentId);
		} catch (Exception e) {
			throw new AppointmentIdNotFoundException("Training Slot Not Found with Slot ID : " + appointmentId);
		}
		if (trainingSlotDetails.getReserved() >= 1) {
   			List<RiderSelectedTrainingAppointment> riderSelectedTrainingAppointments = appointmentRepository.findByAppointmentId(appointmentId);
   			List riderIds = riderSelectedTrainingAppointments.stream().map(appointment-> appointment.getRiderId()).collect(Collectors.toList());
   			List<RiderProfile> riderProfiles = riderProfileRepository.findByIdIn(riderIds);
   			 return RiderSearchProfileDto.of(riderProfiles);

		} else {
			throw new TrainingSlotEmptyException("No appointment booked for Slot ID : " + appointmentId);
		}
	}
	
}
