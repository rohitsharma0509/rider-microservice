package com.scb.rider.service.document;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scb.rider.client.OperationFeignClient;
import com.scb.rider.client.PocketServiceFeignClient;
import com.scb.rider.constants.Constants;
import com.scb.rider.constants.DocumentType;
import com.scb.rider.constants.RiderMannerScoreActionType;
import com.scb.rider.constants.SmsConstants;
import com.scb.rider.exception.*;
import com.scb.rider.kafka.SmsPublisher;
import com.scb.rider.model.document.*;
import com.scb.rider.model.dto.*;
import com.scb.rider.model.enumeration.*;
import com.scb.rider.repository.*;
import com.scb.rider.service.AWSCognitoService;
import com.scb.rider.service.cache.RiderProfileUpdaterService;
import com.scb.rider.util.CustomBeanUtils;
import com.scb.rider.util.DateFormatterUtils;
import com.scb.rider.util.LoggerUtils;
import com.scb.rider.util.PropertyUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.scb.rider.constants.Constants.RIDER_ID;
import static com.scb.rider.model.enumeration.AvailabilityStatus.Inactive;
import static com.scb.rider.model.enumeration.RiderJobStatus.FOOD_DELIVERED;
import static com.scb.rider.model.enumeration.RiderJobStatus.ORDER_CANCELLED_BY_OPERATOR;

@Service
@Log4j2
public class RiderProfileService {

	private static final String TRAINING_CONFIGURABLE_WEEKS = "trainingConfigurableWeeks";
	private static final String PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

	private static final String ZONEID = "Asia/Bangkok";

    @Autowired
	private RiderProfileRepository riderProfileRepository;

	@Autowired
	private RiderEVFormRepository evFormRepository;

	@Autowired
	private RiderVehicleRegistrationRepository vehicleRegistrationRepository;

	@Autowired
	private RiderDrivingLicenseDocumentRepository riderDrivingLicenseDocumentRepository;

	@Autowired
	private RiderTrainingAppointmentRepository riderTrainingAppointmentRepository;
	@Autowired
	private RiderJobDetailsRepository riderJobDetailsRepository;

	@Autowired
	PocketServiceFeignClient pocketServiceFeignClient;

	@Autowired
	private PropertyUtils propertyUtils;

	@Autowired
	private SmsPublisher smsPublisher;
	
	@Autowired
    private OperationFeignClient operationFeignClient;
	
	@Autowired
	private RiderActiveTrackingZoneService riderActiveTrackingZoneService;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private RiderProfileUpdaterService riderProfileUpdaterService;

	@Autowired
	private AWSCognitoService awsCognitoService;

	@Autowired
	private RiderMannerScoreHistoryRepository riderMannerScoreHistoryRepository;

	@Autowired
	private RiderSuspendHistoryRepository riderSuspendHistoryRepository;

	public RiderProfile createRiderProfile(RiderProfileDto profileDto) {
		log.info("Creating Rider Profile ........");
		RiderProfile persistentProfile = new RiderProfile();
		BeanUtils.copyProperties(profileDto, persistentProfile);
		if (ObjectUtils.isNotEmpty(profileDto.getAddress())) {
			persistentProfile.setAddress(new Address());
			BeanUtils.copyProperties(profileDto.getAddress(), persistentProfile.getAddress());

		}
		else if (ObjectUtils.isNotEmpty(profileDto.getNationalAddress())) {
			persistentProfile.setNationalAddress(new NationalAddress());
			BeanUtils.copyProperties(profileDto.getNationalAddress(), persistentProfile.getNationalAddress());
		}

		Integer mannerScoreInitial = 0;
		try{
			ConfigDataResponse configData = operationFeignClient.getConfigData(Constants.MANNER_SCORE_INITIAL);
			mannerScoreInitial = Integer.valueOf(configData.getValue());
		}catch(Exception ex){
			log.error("Api request error; Message:{}", ""+ex);
		}

		persistentProfile.setMannerScoreInitial(mannerScoreInitial);
		persistentProfile.setMannerScoreCurrent(mannerScoreInitial);

		persistentProfile.setProfileStage(RiderProfileStage.STAGE_1);
		persistentProfile.setAvailabilityStatus(Inactive);
		persistentProfile.setStatus(RiderStatus.UNAUTHORIZED);
		persistentProfile.setAttemptBGVStatus(BackgroundVerificationAttemptStatus.ONLINE);
		persistentProfile.setNationalIdStatus(MandatoryCheckStatus.PENDING);
		persistentProfile.setProfilePhotoStatus(MandatoryCheckStatus.PENDING);
		persistentProfile.setTierName("");
		if (StringUtils.isNotEmpty(profileDto.getCountryCode())) {
			persistentProfile.setCountryCode(profileDto.getCountryCode());
		} else if (StringUtils.isNotEmpty(profileDto.getPhoneNumber())
				&& profileDto.getPhoneNumber().startsWith(Constants.ZERO)) {
			persistentProfile.setCountryCode(Constants.THAI_COUNTRY_CODE);
		} else {
			persistentProfile.setCountryCode(Constants.IN_COUNTRY_CODE);
		}
		persistentProfile.setIsReadyForAuthorization(Boolean.FALSE.toString());

		RiderPreferredZones riderPreferredZones = RiderPreferredZones.builder().preferredZoneName("").build();
		RiderDocumentUpload riderDocumentUploadMap = RiderDocumentUpload.builder().enrollmentDate("").build();
		persistentProfile.setRiderPreferredZones(riderPreferredZones);
		persistentProfile.setRiderDocumentUpload(riderDocumentUploadMap);
		RiderProfile riderProfile = riderProfileRepository.save(persistentProfile);

		riderProfileUpdaterService.publish(riderProfile);
		return riderProfile;
	}

	

	public RiderProfile getRiderProfileById(final String id) {
		log.info("Getting Rider profile for id = {}", id);
		RiderProfile riderProfile = this.riderProfileRepository.findById(id)
				.orElseThrow(() -> LoggerUtils.logError(RiderProfile.class, id, "id"));
		return riderProfile;
				
	}

	public RiderProfile updateRiderProfile(RiderProfileUpdateRequestDto updateRequestDto) {
		log.info("Getting Rider profile for id = {}", updateRequestDto.getId());
		RiderProfile riderProfile = this.riderProfileRepository.findById(updateRequestDto.getId())
				.orElseThrow(() -> LoggerUtils.logError(RiderProfile.class, updateRequestDto.getId(), "id"));
		if (allowRiderProfileUpdateOnlyForAuthorizedAndInactive(updateRequestDto)) {
			if (ObjectUtils.isEmpty(riderProfile.getStatus())
					|| !StringUtils.equals(RiderStatus.AUTHORIZED.name(), riderProfile.getStatus().name())
					|| (StringUtils.isNotBlank(updateRequestDto.getPhoneNumber()) && StringUtils.equalsIgnoreCase(
							AvailabilityStatus.Active.name(), riderProfile.getAvailabilityStatus().name()))) {
				throw new UpdatePhoneNumberException("RIDER_SHOULD_BE_INACTIVE_AUTHORIZED");
			}
		}

		CustomBeanUtils.copyNonNullProperties(updateRequestDto, riderProfile);
		return getRiderProfile(updateRequestDto, riderProfile);
	}

	public RiderProfile updateRiderNationalAddress(String riderId, NationalAddressUpdateRequestDto nationalAddressUpdateRequestDto) {
		log.info("Getting Rider profile for id = {}", riderId);

		RiderProfile riderProfile = this.riderProfileRepository.findById(riderId)
				.orElseThrow(() -> LoggerUtils.logError(RiderProfile.class, riderId, "id"));

		if (ObjectUtils.isNotEmpty(nationalAddressUpdateRequestDto.getNationalAddress())) {

			if (ObjectUtils.isEmpty(riderProfile.getNationalAddress())) {
				riderProfile.setNationalAddress(new NationalAddress());
			}

			CustomBeanUtils.copyNonNullProperties(nationalAddressUpdateRequestDto.getNationalAddress(), riderProfile.getNationalAddress());
		}
		return this.riderProfileRepository.save(riderProfile);
	}

	public RiderProfile updateRiderProfilePhoneNumber(RiderProfileUpdateRequestDto updateRequestDto) {
		log.info("Getting Rider profile for id = {}", updateRequestDto.getId());
		RiderProfile riderProfile = this.riderProfileRepository.findById(updateRequestDto.getId())
				.orElseThrow(() -> LoggerUtils.logError(RiderProfile.class, updateRequestDto.getId(), "id"));
		log.info(riderProfile.getAvailabilityStatus() + "check");
		if (!Inactive.equals(riderProfile.getAvailabilityStatus())) {
			throw new UpdatePhoneNumberException("RIDER_SHOULD_BE_INACTIVE_AUTHORIZED");
		}

		CustomBeanUtils.copyNonNullProperties(updateRequestDto, riderProfile);
		return getRiderProfile(updateRequestDto, riderProfile);
	}

	private boolean allowRiderProfileUpdateOnlyForAuthorizedAndInactive(RiderProfileUpdateRequestDto updateRequestDto) {
		if (ObjectUtils.isEmpty(updateRequestDto.getAttemptBGVStatus()) 
				&& ObjectUtils.isEmpty(updateRequestDto.getConsentAcceptFlag())
				&& ObjectUtils.isEmpty(updateRequestDto.getEvBikeUser())
				&& ObjectUtils.isEmpty(updateRequestDto.getRentingToday())) {
			return true;
		}
		return false;
	}

	private RiderProfile getRiderProfile(RiderProfileUpdateRequestDto updateRequestDto, RiderProfile riderProfile) {
		if (ObjectUtils.isNotEmpty(updateRequestDto.getAddress())) {

			if (ObjectUtils.isEmpty(riderProfile.getAddress())) {
				riderProfile.setAddress(new Address());
			}

			CustomBeanUtils.copyNonNullProperties(updateRequestDto.getAddress(), riderProfile.getAddress());

		}

		if (ObjectUtils.isNotEmpty(updateRequestDto.getNationalAddress())) {

			if (ObjectUtils.isEmpty(riderProfile.getNationalAddress())) {
				riderProfile.setNationalAddress(new NationalAddress());
			}

			CustomBeanUtils.copyNonNullProperties(updateRequestDto.getNationalAddress(), riderProfile.getNationalAddress());
		}
		return this.riderProfileRepository.save(riderProfile);
	}

	public RiderProfile updateRiderProfileOpsMember(RiderProfileUpdateRequestDto updateRequestDto) {
		log.info("Getting Rider profile for id = {}", updateRequestDto.getId());
		RiderProfile riderProfile = this.riderProfileRepository.findById(updateRequestDto.getId())
				.orElseThrow(() -> LoggerUtils.logError(RiderProfile.class, updateRequestDto.getId(), "id"));
		CustomBeanUtils.copyNonNullProperties(updateRequestDto, riderProfile);
		return getRiderProfile(updateRequestDto, riderProfile);
	}

	public List<String> getRiderProfileByZoneId(String zoneId, AvailabilityStatus status) {

		if (status == null) {
			return riderProfileRepository.findAllByPreferredZoneId(zoneId).stream().map(RiderProfile::getId)
					.collect(Collectors.toList());
		} else {
			return riderProfileRepository.findAllByPreferredZoneIdAndStatus(status, zoneId ).stream()
					.map(RiderProfile::getId).collect(Collectors.toList());
		}
	}

	public List<String> getRiderProfileByAvailabilityStatus(AvailabilityStatus status, RiderStatus riderStatus) {
		log.info("Getting Rider profile by availability status = {}", status);
		return riderProfileRepository.findByAvailabilityStatusAndStatus(status, riderStatus).stream()
				.map(RiderProfile::getId).collect(Collectors.toList());
	}


	public List<RiderScoringParamsDto> getRiderProfileWithScoringParams(AvailabilityStatus status, RiderStatus riderStatus) {
		log.info("Getting Rider profile by availability status = {}", status);
		List<RiderProfile> riderProfileList = riderProfileRepository.findByAvailabilityStatusAndStatus(status, riderStatus);
		return riderProfileList.stream()
				.map(riderProfile ->  RiderScoringParamsDto.builder().id(riderProfile.getId())
						.evBikeUser(riderProfile.getEvBikeUser()).rentingToday(riderProfile.getRentingToday())
						.preferredZone(riderProfile.getRiderPreferredZones() !=null ? riderProfile.getRiderPreferredZones().getPreferredZoneId():null).build())
				.collect(Collectors.toList());
	}



	public RiderProfile getRiderProfileByPhoneNumber(String phoneNumber) {
		log.info("Getting Rider profile for phoneNumber = {}", phoneNumber);
		return this.riderProfileRepository.findByPhoneNumber(phoneNumber)
				.orElseThrow(() -> LoggerUtils.logError(RiderProfile.class, phoneNumber, "phoneNumber"));
	}

	public RiderProfile setRiderStatus(String riderId, AvailabilityStatus status) {
		RiderProfile riderProfile = this.riderProfileRepository.findById(riderId)
				.orElseThrow(() -> LoggerUtils.logError(RiderProfile.class, riderId, "riderId"));

		// checking if the incoming status is inactive then check for existing jobs
		List<RiderJobDetails> riderJobDetail = this.riderJobDetailsRepository.findByProfileIdAndJobStatusNotIn(riderId,
				Arrays.asList(FOOD_DELIVERED.name(), ORDER_CANCELLED_BY_OPERATOR.name()));// cancelled
		if (riderJobDetail.size() > 0) {
			throw new InvalidStateTransitionException("Already on a job, cannot change status");
		}

		riderProfile.setAvailabilityStatus(status);
		riderProfileUpdaterService.publish(riderProfile);
		riderActiveTrackingZoneService.addZoneDetails(riderProfile);
		updateRiderAvailabilityStatus(riderProfile.getId(), status);
		return riderProfile;
	}

	public RiderStatusDto updateRiderStatus(RiderStatusDto riderStatusDto) {
		RiderProfile riderProfile = this.riderProfileRepository.findById(riderStatusDto.getProfileId()).orElseThrow(
				() -> new DataNotFoundException("Record not found for id " + riderStatusDto.getProfileId()));

		RiderStatus currentStatus = riderProfile.getStatus();
		if (riderProfile.getStatus().equals(riderStatusDto.getStatus())) {
			throw new StatusTransitionNotAllowedException(
					"Rider is already " + riderProfile.getStatus() + " for id " + riderProfile.getId());
		}

		if (RiderStatus.UNAUTHORIZED.equals(riderProfile.getStatus())
				&& RiderStatus.SUSPENDED.equals(riderStatusDto.getStatus())) {
			throw new StatusTransitionNotAllowedException(
					"Status transition not allowed for id " + riderProfile.getId());
		}

		if (!RiderStatus.UNAUTHORIZED.equals(riderProfile.getStatus())
				&& RiderStatus.UNAUTHORIZED.equals(riderStatusDto.getStatus())) {
			throw new StatusTransitionNotAllowedException(
					"Status transition not allowed for id " + riderProfile.getId());
		}

		if (!(Objects.isNull(riderProfile.getStatus()) || RiderStatus.UNAUTHORIZED.equals(riderProfile.getStatus()))
				&& StringUtils.isEmpty(riderStatusDto.getReason())) {
			throw new MandatoryFieldMissingException("Reason is missing for id " + riderProfile.getId());
		}

		if (RiderStatus.AUTHORIZED.equals(riderStatusDto.getStatus())) {
			if (!MandatoryCheckStatus.APPROVED.equals(riderProfile.getNationalIdStatus())) {
				throw new MandatoryChecksMissingException("NationalID not approved for id " + riderProfile.getId());
			}

			if (!MandatoryCheckStatus.APPROVED.equals(riderProfile.getProfilePhotoStatus())) {
				throw new MandatoryChecksMissingException("Selfie not approved for id " + riderProfile.getId());
			}

			Optional<RiderDrivingLicenseDocument> drivingLicense = riderDrivingLicenseDocumentRepository
					.findByRiderProfileId(riderProfile.getId());
			if (!(drivingLicense.isPresent()
					&& MandatoryCheckStatus.APPROVED.equals(drivingLicense.get().getStatus()))) {
				throw new MandatoryChecksMissingException(
						"Driving License not approved for id " + riderProfile.getId());
			}

			if (Boolean.TRUE.equals(riderProfile.getEvBikeUser())) {
				Optional<RiderEVForm> riderEvForm = evFormRepository.findByRiderProfileId(riderProfile.getId());
				if (!(riderEvForm.isPresent()
						&& MandatoryCheckStatus.APPROVED.equals(riderEvForm.get().getStatus()))) {
					throw new MandatoryChecksMissingException(
							"Rider EV Form not approved for id " + riderProfile.getId());
				}
			}
			else {
				Optional<RiderVehicleRegistrationDocument> vehicleRegistration = vehicleRegistrationRepository
						.findByRiderProfileId(riderProfile.getId());
				if (!(vehicleRegistration.isPresent()
						&& MandatoryCheckStatus.APPROVED.equals(vehicleRegistration.get().getStatus()))) {
					throw new MandatoryChecksMissingException(
							"Vehicle Registration not approved for id " + riderProfile.getId());
				}
			}

			Optional<RiderSelectedTrainingAppointment> riderSelectedTrainingAppointment = riderTrainingAppointmentRepository
					.findByRiderIdAndTrainingType(riderProfile.getId(), TrainingType.FOOD);
			if (!(riderSelectedTrainingAppointment.isPresent()
					&& RiderTrainingStatus.COMPLETED.equals(riderSelectedTrainingAppointment.get().getStatus()))) {
				throw new MandatoryChecksMissingException("Basic Training not completed for id " + riderProfile.getId());
			}

		}

		riderProfile.setStatus(riderStatusDto.getStatus());
		
		riderProfile.setReason(riderStatusDto.getReason());
		riderProfile.setLatestStatusModifiedDate(LocalDateTime.now());
		riderProfile.setUpdatedBy(riderStatusDto.getUpdatedBy());
		riderProfile.setRemarks(riderStatusDto.getRemarks());
		riderStatusDto.setModifiedDate(riderProfile.getLatestStatusModifiedDate());

		if(RiderStatus.SUSPENDED.equals(riderStatusDto.getStatus()) 
				&& Objects.nonNull(riderStatusDto.getSuspensionDuration())
				&& riderStatusDto.getSuspensionDuration() > 0) {
			riderProfile.setSuspensionExpiryTime(LocalDateTime.now().plusHours(riderStatusDto.getSuspensionDuration()));
			riderProfile.setSuspensionDuration(riderStatusDto.getSuspensionDuration());
			riderStatusDto.setSuspensionExpiryTime(riderProfile.getSuspensionExpiryTime());
		}	

		if (RiderStatus.SUSPENDED.equals(currentStatus)
				&& RiderStatus.AUTHORIZED.equals(riderStatusDto.getStatus())) {
			riderProfile.setSuspensionDuration(null);
			riderProfile.setSuspensionExpiryTime(null);
		}
		
		if (RiderStatus.UNAUTHORIZED.equals(currentStatus)
				&& RiderStatus.AUTHORIZED.equals(riderStatusDto.getStatus())) {
			ZonedDateTime zonedUTC = LocalDateTime.now().atZone(ZoneId.of("UTC"));
			ZonedDateTime zonedBangkok = zonedUTC.withZoneSameInstant(ZoneId.of(ZONEID));
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(PATTERN);
			String approvalDateTime = zonedBangkok.format(formatter);
			riderProfile.setApprovalDateTime(approvalDateTime);
			ResponseEntity<Boolean> isPocketInitialised = pocketServiceFeignClient
					.addRiderDataFromOpsPortal(riderProfile.getRiderId());
			log.info("Is Pocket initialised for Rider {} {}", riderProfile.getRiderId(), isPocketInitialised.getBody());
		}

		sendRiderStatusChangeEvent(riderProfile, currentStatus, riderStatusDto.getStatus(),riderStatusDto.getRemarks(), riderStatusDto.getSuspensionDuration());

		riderProfileRepository.save(riderProfile);
		if (isUpdateRiderStatusToSuspend(riderStatusDto)) {
			saveRiderSuspendHistory(riderProfile, riderStatusDto);
		}
		riderProfileUpdaterService.publish(riderProfile);

		return riderStatusDto;

	}

	private boolean isUpdateRiderStatusToSuspend(RiderStatusDto riderStatusDto) {
		return RiderStatus.SUSPENDED.equals(riderStatusDto.getStatus());
	}

	private void saveRiderSuspendHistory(RiderProfile riderProfile, RiderStatusDto riderStatusDto) {

		String[] riderStatus = riderStatusDto.getReason().split("\\|");
		RiderSuspendHistory riderSuspendHistory = RiderSuspendHistory.builder()
				.riderId(riderProfile.getRiderId())
				.suspensionReason(Arrays.asList(riderStatus))
				.suspensionNote(riderStatusDto.getRemarks())
				.suspensionDuration(riderProfile.getSuspensionDuration())
				.suspensionExpiryTime(riderProfile.getSuspensionExpiryTime())
				.riderCaseNo(riderStatusDto.getRiderCaseNo())
				.createdDate(riderStatusDto.getModifiedDate())
				.createdBy(riderStatusDto.getUpdatedBy())
				.build();
		this.riderSuspendHistoryRepository.save(riderSuspendHistory);
	}

	public void sendRiderStatusChangeEvent(RiderProfile riderProfile, RiderStatus currentStatus, RiderStatus newStatus,
			String reason,Integer suspensionDuration) {
		if (RiderStatus.UNAUTHORIZED.equals(currentStatus) && RiderStatus.AUTHORIZED.equals(newStatus)) {
			String message = propertyUtils.getProperty(SmsConstants.RIDER_AUTHORIZED_MSG,
					Locale.forLanguageTag(Constants.THAI));
			log.info("Rider with riderId {} has been authorized, Publishing sms event.", riderProfile.getId());
			smsPublisher.sendSmsNotificationEvent(riderProfile, message);
		} else if (RiderStatus.AUTHORIZED.equals(currentStatus) && RiderStatus.SUSPENDED.equals(newStatus)) {
			log.info("Rider with riderId {} has been suspended with a reason for {}, Publishing sms event.",
					riderProfile.getId(), reason);
				if(Objects.nonNull(suspensionDuration) && suspensionDuration > 0){
					String message = MessageFormat.format(
							propertyUtils.getProperty(SmsConstants.RIDER_TEMPORARILY_SUSPENDED_MSG, Locale.forLanguageTag(Constants.THAI)),
							reason);
					smsPublisher.sendSmsNotificationEvent(riderProfile, message);
				}else {
					String message = MessageFormat.format(
							propertyUtils.getProperty(SmsConstants.RIDER_PERMANENT_SUSPENDED_MSG, Locale.forLanguageTag(Constants.THAI)),
							reason);
					smsPublisher.sendSmsNotificationEvent(riderProfile, message);
				}
		} else if (RiderStatus.SUSPENDED.equals(currentStatus) && RiderStatus.AUTHORIZED.equals(newStatus)) {
			String message = propertyUtils.getProperty(SmsConstants.RIDER_REAUTHORIZED_MSG,
					Locale.forLanguageTag(Constants.THAI));
			log.info("Rider with riderId {} has been re-authorized, Publishing sms event.", riderProfile.getId());
			smsPublisher.sendSmsNotificationEvent(riderProfile, message);
		}
	}

	public Boolean updateNationalIdStatus(String riderId, MandatoryCheckStatus status, String reason, String comment, String updatedBy) {
		RiderProfile riderProfile = this.riderProfileRepository.findById(riderId)
				.orElseThrow(() -> new DataNotFoundException("Record not found for id " + riderId));
		if(MandatoryCheckStatus.APPROVED.equals(riderProfile.getNationalIdStatus()) && MandatoryCheckStatus.REJECTED.equals(status)){
			throw new DocumentAlreadyApprovedException("Document is already Approved !", new Object[]{"National Id"});
		}
		riderProfile.setNationalIdStatus(status);
		NationalIdDetails nationalIdDetails = NationalIdDetails.builder().build();
		if (MandatoryCheckStatus.REJECTED.equals(status)) {
			nationalIdDetails =	NationalIdDetails.builder().
					rejectionReason(reason).
					rejectionComment(comment).
					rejectionTime(LocalDateTime.now()).build();
		}
		log.info("Updating nationaId for riderId-{}",riderId);
		riderProfile.setNationalIdDetails(nationalIdDetails);
		riderProfile.setUpdatedBy(updatedBy);
		riderProfileRepository.save(riderProfile);
		return true;
	}

	public Boolean updateProfilePhotoStatus(String riderId, MandatoryCheckStatus status, String reason,
			String comment, String updatedBy) {
		RiderProfile riderProfile = this.riderProfileRepository.findById(riderId)
				.orElseThrow(() -> new DataNotFoundException("Record not found for id " + riderId));
		if(MandatoryCheckStatus.APPROVED.equals(riderProfile.getProfilePhotoStatus()) && MandatoryCheckStatus.REJECTED.equals(status)){
			throw new DocumentAlreadyApprovedException("Document is already Approved !", new Object[]{"Profile Photo"});
		}
		if (Objects.nonNull(riderProfile.getProfilePhotoStatus())
				&& !riderProfile.getProfilePhotoStatus().equals(status)
				&& MandatoryCheckStatus.REJECTED.equals(status)) {

			if(Constants.OTHER.equalsIgnoreCase(reason) || Constants.OTHER_IN_THAI.equalsIgnoreCase(reason)) {
				reason = StringUtils.isNotBlank(comment) ? comment : StringUtils.EMPTY;
			} else {
				reason = StringUtils.isNotBlank(comment) ? (reason + " " + comment) : reason;
			}
			String message = MessageFormat.format(propertyUtils.getProperty(SmsConstants.SELFIE_REJECTED_MSG, Locale.forLanguageTag(Constants.THAI)), reason);
			log.info("Profile photo rejected for riderId {}, Publishing sms event.", riderProfile.getId());
			smsPublisher.sendSmsNotificationEvent(riderProfile, message);
		}
		if (MandatoryCheckStatus.REJECTED.equals(status)) {
			riderProfile.setProfilePhotoRejectionReason(reason);
			riderProfile.setProfilePhotoRejectionComment(comment);
			riderProfile.setProfilePhotoRejectionTime(LocalDateTime.now());
		}
		riderProfile.setProfilePhotoStatus(status);
		riderProfile.setUpdatedBy(updatedBy);
		riderProfileRepository.save(riderProfile);
		return true;
	}

	public String getRiderProfileStage(String phoneNumber) {

		Optional<RiderProfile> riderProfileOpt = this.riderProfileRepository.findByPhoneNumber(phoneNumber);
		RiderProfile riderProfile = riderProfileOpt.orElseGet(() -> null);

		String ret = "";
		String riderProfileCompletionStage = "";

		if (ObjectUtils.isEmpty(riderProfile)) {
			ret = "RIDER_PROFILE_NOT_CREATED";

		} else if (ObjectUtils.isEmpty(riderProfile.getStatus())) {
			ret = "RIDER_PROFILE_STATUS_NOT_PRESENT";
		}

		else if (ObjectUtils.isEmpty(riderProfile.getProfileStage())) {
			ret = "RIDER_PROFILE_STAGE_NOT_PRESENT";
		} 
		else {
		  
          Long configuredWeeks = 2L;
          try {
			ConfigDataResponse configDataResponse = operationFeignClient.getConfigData(TRAINING_CONFIGURABLE_WEEKS);
            configuredWeeks = Long.parseLong(configDataResponse.getValue());
          } catch (Exception ex) {
            log.error("Error Occured While Fetching Configured training Weeks from OPS Service: {}",
                ex.getMessage());
          }
			String riderStatus = riderProfile.getStatus().name();
			LocalDate riderTrainingDeadlineDate = riderProfile.getCreatedDate().toLocalDate().plus(configuredWeeks,
					ChronoUnit.WEEKS);
			LocalDate today = LocalDate.now();

			riderProfileCompletionStage = riderProfile.getProfileStage().name();
			if (!riderStatus.equalsIgnoreCase("UNAUTHORIZED"))
				ret = "RIDER_AUTHORIZED";
			else if (riderStatus.equalsIgnoreCase("UNAUTHORIZED")) {

				if (!riderProfileCompletionStage.equalsIgnoreCase("STAGE_3")
						&& (today.isAfter(riderTrainingDeadlineDate)))
					ret = "RIDER_PROFILE_IN_DEACTIVE_STATUS";

				else if (riderProfileCompletionStage.equalsIgnoreCase("STAGE_1"))
					ret = "RIDER_PROFILE_CREATED";

				else if (riderProfileCompletionStage.equalsIgnoreCase("STAGE_2"))
					ret = "RIDER_MANDATORY_DOCS_UPLOADED";

				else if (riderProfileCompletionStage.equalsIgnoreCase("STAGE_3"))
					ret = "RIDER_ALL_PROCESS_COMPLETED_BUT_STILL_UNAUTHORIZED";
			}

		}

		return ret;
	}

	public List<RiderProfile> getRiferProfilesByRiderIds(List<String> riderIds) {
		return riderProfileRepository.findByRiderIdIn(riderIds);
	}

	public void processKafkaTopic(String message) throws JsonProcessingException {
		RiderProfileTierDto rider = objectMapper.readValue(message, RiderProfileTierDto.class);
		String riderId = rider.getRiderId();
		RiderProfile riderProfile = riderProfileRepository.findByRiderId(riderId).orElseThrow(
				() -> new DataNotFoundException("Record not found for id " + riderId));
		riderProfile.setTierName(rider.getTierName());
		riderProfile.setTierId(rider.getTierId());

		log.info("Saving rider profile: {}", riderId);

		riderProfileRepository.save(riderProfile);
    }

	public RiderShortProfile getRiderShortProfile(String type, String id) {

		RiderProfile riderProfile = null;

		try {
			switch (type) {
				case Constants.RIDER_ID:
					riderProfile = riderProfileRepository.findByRiderId(id).orElseThrow(() -> new ShortProfileException("Record not found for rider id " + id, Constants.ERROR_CODE_102));
					break;
				case Constants.NATIONAL_ID:
					riderProfile = riderProfileRepository.findByNationalID(id).orElseThrow(() -> new ShortProfileException("Record not found for national id " + id, Constants.ERROR_CODE_102));
					break;
				case Constants.PHONE_NUMBER:
					riderProfile = riderProfileRepository.findByPhoneNumber(id).orElseThrow(() -> new ShortProfileException("Record not found for phone number " + id, Constants.ERROR_CODE_102));
					break;
				default:
					log.error(String.format("Type - %s is not valid, Type should be either riderId or nationalId or phoneNumber", type));
					return RiderShortProfile.builder().status(Constants.FAILED).code(Constants.ERROR_CODE_101).errorMessage("Invalid Input - Type should be either riderId or nationalId or phoneNumber").build();
			}
		} catch (ShortProfileException ex) {
			log.error("Exception " + ex.getMessage());
			return RiderShortProfile.builder().status(Constants.FAILED).code(ex.getCode()).errorMessage(ex.getMessage()).build();
		} catch (Exception e) {
			log.error("Exception while getting profile details " + e.getMessage());
			return RiderShortProfile.builder().status(Constants.FAILED).code(Constants.ERROR_CODE_103).errorMessage("An error has occurred. Please try again later").build();
		}
		return RiderShortProfile.of(riderProfile);
	}

	public List<RiderProfile> evRidersList(LocalDateTime startDate, LocalDateTime endDate){
		Query query = new Query();
		String key = "createdDate";
		query.addCriteria(Criteria.where(key).exists(true).andOperator(
				Criteria.where(key).gt(startDate),
				Criteria.where(key).lte(endDate)).and("evBikeUser").is(true));
		return mongoTemplate.find(query, RiderProfile.class);
	}

	public List<RiderProfile> updateRentingTodayFlag(RentingTodayRequest updateRentingTodayRequest) {
		List<RiderProfile> riderProfiles = new ArrayList<>();
		if(updateRentingTodayRequest.getRentingToday().equals(Boolean.TRUE))
			riderProfiles = riderProfileRepository.findByRiderIdInAndStatus(updateRentingTodayRequest.getRiders(), RiderStatus.AUTHORIZED);
		else
			riderProfiles = riderProfileRepository.findByRiderIdIn(updateRentingTodayRequest.getRiders());
		riderProfiles.forEach(riderProfile -> {
			riderProfile.setRentingToday(updateRentingTodayRequest.getRentingToday());
			riderProfile.setEvBikeVendor(updateRentingTodayRequest.getEvBikeVendor());
		});
		
		 List<RiderProfile> saveAllRiders = riderProfileRepository.saveAll(riderProfiles);

		 saveAllRiders
		.forEach(rider-> riderProfileUpdaterService.publish(rider));
		 return saveAllRiders;
	}

	public void updateRentingTodayAsFalse() {
		Query query = new Query();
		//query.addCriteria(Criteria.where("evBikeUser").is(true)); // do it for all riders
		Update update = new Update();
		update.set("rentingToday", false);
		mongoTemplate.updateMulti(query, update, RiderProfile.class);
	}
	
	public PaginatedRiderDetailsList getAllRiderProfile(String zoneId, int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by(RIDER_ID));
		Page<RiderProfile> pageResponse = null;
		List<RiderStatus> riderStatus = new ArrayList<>();
		riderStatus.add(RiderStatus.AUTHORIZED);
		riderStatus.add(RiderStatus.SUSPENDED);

		if (zoneId == null)
			pageResponse = riderProfileRepository.findAllByStatusIn(riderStatus, pageable);

		else
			pageResponse = riderProfileRepository.findAllByRiderPreferredZones_PreferredZoneIdAndStatusIn(zoneId, riderStatus, pageable);

		List<RiderDetails> riderDetailsList = pageResponse.getContent().stream().map(RiderDetails::of).collect(Collectors.toList());

		return PaginatedRiderDetailsList.builder()
				.totalPages(pageResponse.getTotalPages())
				.size(pageResponse.getSize())
				.currentPageNumber(pageResponse.getNumber())
				.totalElements(pageResponse.getTotalElements())
				.riders(riderDetailsList)
				.build();

	}
	
	
  public void documentUploadedFlag(String riderId, DocumentType documentType) {
    log.info("Is All Document Uploaded Id:{}, DocType:{}", riderId, documentType.name());
    Optional<RiderProfile> riderProfile = riderProfileRepository.findById(riderId);
    riderProfile.ifPresent(obj -> updateUploadedDocumentFlag(riderProfile.get(), documentType));
  }

  private Object updateUploadedDocumentFlag(RiderProfile riderProfile, DocumentType documentType) {
    List<DocumentType> mandatoryDocumentTypesList = DocumentType.getMandatoryDocumentTypeList();
    if (mandatoryDocumentTypesList.contains(documentType)) {
      RiderDocumentUpload riderDocumentUpload = riderProfile.getRiderDocumentUpload();
      Map<DocumentType, Boolean> uploadedDocumentsMap = new HashMap<DocumentType, Boolean>();
      if (Objects.isNull(riderDocumentUpload)) {
        riderDocumentUpload = RiderDocumentUpload.builder().build();
      } else if (!Objects.isNull(riderDocumentUpload.getDocumentUploadedFlag())) {
        uploadedDocumentsMap = riderDocumentUpload.getDocumentUploadedFlag();
      }

      if (!uploadedDocumentsMap.containsKey(documentType)) {
        uploadedDocumentsMap.put(documentType, true);
        log.info("Document Uploaded For Id:{}, Dict Size:{}", riderProfile.getRiderId(),
            uploadedDocumentsMap.keySet().size());
        if (uploadedDocumentsMap.keySet().size() == mandatoryDocumentTypesList.size()) {
          String enrollmentDate = DateFormatterUtils.zonedDateTimeToString(
              ZonedDateTime.now(ZoneId.of(ZONEID)), Constants.YYYY_MM_DD);
          riderDocumentUpload.setEnrollmentDate(enrollmentDate);
          log.info("All Document Uploaded For Id:{}, Dict:{}", riderProfile.getRiderId(),
              riderDocumentUpload.toString());
        }
        Boolean evBikeUser = !ObjectUtils.isEmpty(riderProfile.getEvBikeUser()) && riderProfile.getEvBikeUser();
        if(checkDocumentStatus(evBikeUser,uploadedDocumentsMap)){
			String enrollmentDate = DateFormatterUtils.zonedDateTimeToString(
					ZonedDateTime.now(ZoneId.of(ZONEID)), Constants.YYYY_MM_DD);
			riderDocumentUpload.setEnrollmentDate(enrollmentDate);
			log.info("All mandatory documents uploaded for rider with id: {}, Dict: {}",riderProfile.getRiderId(),
					riderDocumentUpload.toString());
		}
        riderDocumentUpload.setDocumentUploadedFlag(uploadedDocumentsMap);
        riderProfile.setRiderDocumentUpload(riderDocumentUpload);
      }
    }
    log.info("Saving All Document Uploaded Id:{}, DocType:{}", riderProfile.getRiderId(),
        documentType.name());
    return riderProfileRepository.save(riderProfile);
  }

	public RiderProfile updateRiderPhoneNumber(RiderProfileUpdateRequestDto riderProfileUpdateRequestDto) {
		log.info("Getting Rider profile for id = {}", riderProfileUpdateRequestDto.getId());
		if(this.riderProfileRepository.findByPhoneNumber(riderProfileUpdateRequestDto.getPhoneNumber()).isPresent()){
			throw new UpdatePhoneNumberException("PHONE_NUMBER_ALREADY_EXISTS");
		}
		if(!this.riderProfileRepository.findById(riderProfileUpdateRequestDto.getId()).isPresent()){
			throw new UpdatePhoneNumberException("ID_DOES_NOT_EXISTS");
		}
		RiderProfile riderProfile = this.riderProfileRepository.findById(riderProfileUpdateRequestDto.getId()).get();
		String oldPhoneNumber = riderProfile.getCountryCode() + riderProfile.getPhoneNumber();
		RiderProfile updatedProfile = updateRiderProfilePhoneNumber(riderProfileUpdateRequestDto);
		awsCognitoService.deleteUserByPhoneNumber(oldPhoneNumber);
		return updatedProfile;
	}

  private static boolean checkDocumentStatus(Boolean evBikeUser,Map<DocumentType, Boolean> uploadedDocumentsMap ){
		if(evBikeUser){
			return uploadedDocumentsMap.containsKey(DocumentType.EV_FORM) &&
					uploadedDocumentsMap.containsKey(DocumentType.PROFILE_PHOTO) &&
					uploadedDocumentsMap.containsKey(DocumentType.DRIVER_LICENSE);
		}
		else
			return uploadedDocumentsMap.containsKey(DocumentType.DRIVER_LICENSE) &&
					uploadedDocumentsMap.containsKey(DocumentType.VEHICLE_REGISTRATION) &&
					uploadedDocumentsMap.containsKey(DocumentType.VEHICLE_WITH_FOOD_CARD) &&
					uploadedDocumentsMap.containsKey(DocumentType.PROFILE_PHOTO);
  }


	@Async("customTaskExecutor")
	public void publishToKafka() {

		Pageable pageable = PageRequest.of(0, 500, Sort.by("riderId"));


		Page<RiderProfile> pageResponse = riderProfileRepository.findAll(pageable);

		log.info("Taking first page data from db and saving to redis total page-{}", pageResponse.getTotalPages());

		pageResponse.getContent().stream().forEach(rider ->
				riderProfileUpdaterService.publish(rider)
		);

		int page = 1;
		while (page != pageResponse.getTotalPages() + 1) {
			pageable = PageRequest.of(page, 500, Sort.by("riderId"));
			pageResponse = riderProfileRepository.findAll(pageable);

			pageResponse.getContent().stream().forEach(rider ->
					riderProfileUpdaterService.publish(rider)
			);
			log.info("reading data from db and saving to redis current page-{}", page);

			page++;
		}
		log.info("saved to redis total page-{}", pageResponse.getTotalPages());

	}

	public void updateRiderAvailabilityStatus(String riderId, AvailabilityStatus availabilityStatus){
		Query query = new Query();
		Criteria criteria =
				Criteria.where("id").is(riderId);
		query.addCriteria(criteria);
		Update update = new Update();
		update.set("availabilityStatus", availabilityStatus);
		update.set("updatedDate", LocalDateTime.now());
		mongoTemplate.updateFirst(query, update, RiderProfile.class);

	}

	@Transactional
	public RiderProfileUpdateMannerScoreResponseDto updateRiderProfileMannerScore(final RiderProfileUpdateMannerScoreDto request, final HttpServletRequest requestHttp) {
		log.info("Getting Rider profile for id = {}", request.getRiderId());
		final String userId = !ObjectUtils.isEmpty(requestHttp.getAttribute(Constants.X_USER_ID)) ? requestHttp.getAttribute(Constants.X_USER_ID).toString() : "";
		final Integer actionScore = Math.abs(request.getActionScore());
		this.validateRequestUpdateRiderProfileMannerScore(request);
		int mannerScorePossibleMin;
		int mannerScorePossibleMax;
		int mannerScoreStartAdjust;
		int mannerScoreEndAdjust;
		final List<String> listConfigData = Arrays.asList("mannerScorePossibleMin", "mannerScorePossibleMax", "mannerScoreStartAdjust", "mannerScoreEndAdjust");
		try {
			List<ConfigDataResponse> configDataList = operationFeignClient.getListConfigData(listConfigData);
			mannerScorePossibleMin = Integer.parseInt(configDataList.stream().filter(configData ->
					configData.getKey().equalsIgnoreCase("mannerScorePossibleMin")).findFirst()
					.orElseThrow(() -> new RiderMannerScoreException("Can not get mannerScorePossibleMin")).getValue());
			mannerScorePossibleMax = Integer.parseInt(configDataList.stream().filter(configData ->
					configData.getKey().equalsIgnoreCase("mannerScorePossibleMax")).findFirst()
					.orElseThrow(() -> new RiderMannerScoreException("Can not get mannerScorePossibleMax")).getValue());
			mannerScoreStartAdjust = Integer.parseInt(configDataList.stream().filter(configData ->
					configData.getKey().equalsIgnoreCase("mannerScoreStartAdjust")).findFirst()
					.orElseThrow(() -> new RiderMannerScoreException("Can not get mannerScoreStartAdjust")).getValue());
			mannerScoreEndAdjust = Integer.parseInt(configDataList.stream().filter(configData ->
					configData.getKey().equalsIgnoreCase("mannerScoreEndAdjust")).findFirst()
					.orElseThrow(() -> new RiderMannerScoreException("Can not get mannerScoreEndAdjust")).getValue());
		} catch (Exception ex) {
			mannerScorePossibleMin = 0;
			mannerScorePossibleMax = 0;
			mannerScoreStartAdjust = 0;
			mannerScoreEndAdjust = 0;
			log.error("Api request error; Message:{}", ex);
		}
		if (actionScore.compareTo(mannerScoreStartAdjust) < 0) {
			throw new RiderMannerScoreException(String.format("actionScore must between %d and %d",mannerScoreStartAdjust, mannerScoreEndAdjust));
		}
		if (actionScore.compareTo(mannerScoreEndAdjust) > 0) {
			throw new RiderMannerScoreException(String.format("actionScore must between %d and %d",mannerScoreStartAdjust, mannerScoreEndAdjust));
		}
		Integer vFinalMannerScore = 0;
		RiderProfile riderProfile = this.riderProfileRepository.findByRiderId(request.getRiderId()).orElseThrow(
				() -> new DataNotFoundException("Record not found for id " + request.getRiderId()));
		if (RiderMannerScoreActionType.ADD.name().equalsIgnoreCase(request.getActionType())) {
			vFinalMannerScore = riderProfile.getMannerScoreCurrent() + actionScore;
		} else if (RiderMannerScoreActionType.SUBTRACT.name().equalsIgnoreCase(request.getActionType())) {
			vFinalMannerScore = riderProfile.getMannerScoreCurrent() - actionScore;
		}
		if (vFinalMannerScore.compareTo(mannerScorePossibleMin) < 0) {
			vFinalMannerScore = mannerScorePossibleMin;
		}
		if (vFinalMannerScore.compareTo(mannerScorePossibleMax) > 0) {
			vFinalMannerScore = mannerScorePossibleMax;
		}
		RiderMannerScoreHistory riderMannerScoreHistory = RiderMannerScoreHistory.builder()
				.riderId(riderProfile.getRiderId()).currentScore(riderProfile.getMannerScoreCurrent())
				.actionType(request.getActionType()).actionScore(actionScore)
				.finalScore(vFinalMannerScore).reason(request.getReason())
				.additionalComment(request.getAdditionalComment()).createdBy(userId).build();
		riderProfile.setMannerScoreCurrent(vFinalMannerScore);
		riderProfile.setUpdatedBy(userId);
		this.riderProfileRepository.save(riderProfile);
		this.riderMannerScoreHistoryRepository.save(riderMannerScoreHistory);
		sendRiderMannerScoreEvent(riderProfile, request.getActionType(), actionScore);
		return RiderProfileUpdateMannerScoreResponseDto.builder().id(riderMannerScoreHistory.getId())
				.riderId(riderMannerScoreHistory.getRiderId()).actionType(riderMannerScoreHistory.getActionType())
				.reason(riderMannerScoreHistory.getReason()).actionScore(riderMannerScoreHistory.getActionScore())
				.additionalComment(riderMannerScoreHistory.getAdditionalComment()).build();
	}

	private void validateRequestUpdateRiderProfileMannerScore(RiderProfileUpdateMannerScoreDto riderProfileUpdateMannerScoreDto) {
		if (!ObjectUtils.isEmpty(riderProfileUpdateMannerScoreDto) && riderProfileUpdateMannerScoreDto.getReason().isEmpty()){
			throw new RiderMannerScoreException("Reason Property is required");
		}
		if (!ObjectUtils.isEmpty(riderProfileUpdateMannerScoreDto) && ObjectUtils.isEmpty(riderProfileUpdateMannerScoreDto.getActionType())){
			throw new RiderMannerScoreException("actionMannerScore must be [\"ADD\", \"SUBTRACT\"]");
		}
		if (!RiderMannerScoreActionType.ADD.name().equalsIgnoreCase(riderProfileUpdateMannerScoreDto.getActionType())
			&& !RiderMannerScoreActionType.SUBTRACT.name().equalsIgnoreCase(riderProfileUpdateMannerScoreDto.getActionType())) {
			throw new RiderMannerScoreException("actionMannerScore must be [\"ADD\", \"SUBTRACT\"]");
		}
	}

	public void sendRiderMannerScoreEvent(RiderProfile riderProfile, String actionType, Integer actionScore) {
		if (actionType.equals(RiderMannerScoreActionType.ADD.name())) {
			String message = MessageFormat.format(propertyUtils.getProperty(SmsConstants.RIDER_MANNER_SCORE_ADD_MSG,
					Locale.forLanguageTag(Constants.THAI)), actionScore);
			log.info("Rider with riderId {} has been add mannerScore {} points, Publishing sms event.",
					riderProfile.getId(), actionScore);
			smsPublisher.sendSmsNotificationEvent(riderProfile, message);
		} else if (actionType.equals(RiderMannerScoreActionType.SUBTRACT.name())) {
			log.info("Rider with riderId {} has been subtract mannerScore {} points, Publishing sms event.",
					riderProfile.getId(), actionScore);
			String message = MessageFormat.format(propertyUtils.getProperty(SmsConstants.RIDER_MANNER_SCORE_SUBTRACT_MSG,
					Locale.forLanguageTag(Constants.THAI)), actionScore);
			smsPublisher.sendSmsNotificationEvent(riderProfile, message);
		}
	}
}