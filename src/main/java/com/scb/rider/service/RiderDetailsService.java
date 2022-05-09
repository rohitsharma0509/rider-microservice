package com.scb.rider.service;

import com.scb.rider.client.OperationFeignClient;
import com.scb.rider.constants.Constants;
import com.scb.rider.constants.DocumentType;
import com.scb.rider.exception.DataNotFoundException;
import com.scb.rider.model.document.*;
import com.scb.rider.model.dto.ConfigDataResponse;
import com.scb.rider.model.dto.RiderDetailsDto;
import com.scb.rider.model.enumeration.RiderProfileFilters;
import com.scb.rider.model.enumeration.RiderStatus;
import com.scb.rider.repository.*;
import com.scb.rider.util.AccountMaskUtils;
import com.scb.rider.util.LoggerUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Log4j2
public class RiderDetailsService {

	@Autowired
	private RiderProfileRepository riderProfileRepository;
	@Autowired
	private RiderEmergencyContactRepository riderEmergencyContactRepository;
	@Autowired
	private RiderDrivingLicenseDocumentRepository riderDrivingLicenseDocumentRepository;
	@Autowired
	private RiderEVFormRepository evFormRepository;
	@Autowired
	private RiderVehicleRegistrationRepository riderVehicleRegistrationRepository;
	@Autowired
	private RiderUploadedDocumentRepository uploadedDocumentRepository;
	@Autowired
	private RiderTrainingAppointmentRepository appointmentRepository;
	@Autowired
	private RiderBackgroundVerificationDocumentRepository riderBackgroundVerificationDocumentRepository;
	@Autowired
	private RiderJobDetailsRepository riderJobDetailsRepository;
	@Autowired
	private RiderDeviceDetailRepository riderDeviceDetailRepository;

	@Autowired
	private RiderFoodCardRepository riderFoodCardRepository;

	@Autowired
	private RiderCovidSelfieRepository riderCovidSelfieRepository;

	@Autowired
	private RiderLocationService riderLocationService;

	@Autowired
	private OperationFeignClient operationFeignClient;

	public RiderDetailsDto getRiderDetailsById(final String id, String[] filters) {
		log.info("Getting Rider profile for id = {} and filters {}", id, filters);
		RiderProfile riderProfile = this.riderProfileRepository.findById(id)
				.orElseThrow(() -> LoggerUtils.logError(RiderProfile.class, id, "id"));

		if(filters != null && filters.length > 0) {
			return getFilteredRiderProfile(riderProfile, filters);
		} else {
			return getRiderDetails(riderProfile);
		}
	}

	private RiderDetailsDto getRiderDetails(RiderProfile riderProfile) {
		String id = riderProfile.getId();
		log.info("Getting Emergency Contact for id = {}", id);
		Optional<RiderEmergencyContact> emergencyContact = this.riderEmergencyContactRepository.findByProfileId(id);

		log.info("Getting Driving License for id = {}", id);
		Optional<RiderDrivingLicenseDocument> drivingLicenseDocument = this.riderDrivingLicenseDocumentRepository.findByRiderProfileId(id);

		log.info("Getting Rider EV Form for id = {}", id);
		Optional<RiderEVForm> riderEVForm = this.evFormRepository.findByRiderProfileId(id);

		log.info("Getting Vehicle Details for id = {}", id);
		Optional<RiderVehicleRegistrationDocument> vehicleRegistrationDocument = this.riderVehicleRegistrationRepository.findByRiderProfileId(id);

		log.info("Getting Rider Preferred Zone Details for id = {}", id);
		Optional<RiderPreferredZones> riderPreferredZones = ObjectUtils.isEmpty(riderProfile.getRiderPreferredZones()) ? Optional.empty() : Optional.of(riderProfile.getRiderPreferredZones());

		Optional<RiderUploadedDocument> uploadedVehicleFoodCardDocument = this.uploadedDocumentRepository.findByRiderProfileIdAndDocumentType(id, DocumentType.VEHICLE_WITH_FOOD_CARD);

		if(vehicleRegistrationDocument.isPresent() && uploadedVehicleFoodCardDocument.isPresent()){
			vehicleRegistrationDocument.get().setUploadedFoodCardUrl(uploadedVehicleFoodCardDocument.get().getImageUrl());
		}

		log.info("Getting Rider Profile Photo Details for id = {}", id);
		Optional<RiderUploadedDocument> uploadedDocument = this.uploadedDocumentRepository.findByRiderProfileIdAndDocumentType(id, DocumentType.PROFILE_PHOTO);

		log.info("Getting Rider Appointment Details for id = {}", id);
		List<RiderSelectedTrainingAppointment> appointmentList = appointmentRepository.findByRiderId(id);

		log.info("Getting Rider Background details for id = {}", id);
		Optional<RiderBackgroundVerificationDocument> riderBackgroundDetails = this.riderBackgroundVerificationDocumentRepository.findByRiderProfileId(id);

		log.info("Getting Rider foodcard details for id = {}", id);
		Optional<RiderFoodCard> riderFoodCard = this.riderFoodCardRepository.findByRiderProfileId(id);

		Optional<RiderDeviceDetails> deviceDetails = this.riderDeviceDetailRepository.findByProfileId(id);
		log.info("Getting last Uploaded Selfie Time for rider id = {}", riderProfile.getRiderId());
		Optional<RiderCovidSelfie> riderCovidSelfie = this.riderCovidSelfieRepository.findFirstByRiderIdOrderByUploadedTimeDesc(riderProfile.getId());
		ConfigDataResponse configData = operationFeignClient.getConfigData(Constants.MANNER_SCORE_POSSIBLE_MAX);
		Integer mannerScorePossibleMax = Integer.valueOf(configData.getValue());
		return RiderDetailsDto.of(riderProfile, emergencyContact, drivingLicenseDocument, riderEVForm, vehicleRegistrationDocument, riderPreferredZones,
				uploadedDocument, appointmentList, riderBackgroundDetails, riderFoodCard, deviceDetails, riderCovidSelfie,mannerScorePossibleMax);
	}

	public RiderDetailsDto getRiderDetailsByPhoneNumber(String phoneNumber) {
		log.info("Getting Rider profile for phoneNumber = {}", phoneNumber);
		RiderProfile riderProfile = this.riderProfileRepository.findByPhoneNumber(phoneNumber)
				.orElseThrow(() -> LoggerUtils.logError(RiderProfile.class, phoneNumber, "phoneNumber"));
		return getRiderDetails(riderProfile);
    }

	public RiderDetailsDto getRiderDocumentDetails(String id) {
		log.info("Getting Rider profile details for id = {}", id);
		RiderProfile riderProfile = this.riderProfileRepository.findByIdInAndStatusNot(id, RiderStatus.UNAUTHORIZED)
				.orElseThrow(() -> LoggerUtils.logError(RiderProfile.class, id, "id"));

		log.info("Masking AccountNumber and NationalId for id = {}", id);
		String accountNumber = riderProfile.getAccountNumber();
		String nationalId = riderProfile.getNationalID();
		if (StringUtils.isNotBlank(accountNumber)) {
			riderProfile.setAccountNumber(AccountMaskUtils.maskAccountDetails(accountNumber, AccountMaskUtils.ACCOUNT_NO_FORMAT));
		}

		if (StringUtils.isNotBlank(nationalId)) {
			riderProfile.setNationalID(AccountMaskUtils.maskAccountDetails(nationalId, AccountMaskUtils.NATIONAL_ID_FORMAT));
		}

		return getRiderDetails(riderProfile);
	}

	public RiderProfile deleteRiderProfileByMobileNumber(String mobileNum) {

		RiderProfile riderProfile = this.riderProfileRepository.findByPhoneNumber(mobileNum)
					.orElseThrow(() -> new DataNotFoundException("Record not found for  phone Number " + mobileNum));

		String riderId = riderProfile.getId();

		this.riderProfileRepository.deleteById(riderId);
		this.riderEmergencyContactRepository.deleteByProfileId(riderId);
		riderProfile.setRiderPreferredZones(null);
		this.riderVehicleRegistrationRepository.deleteByRiderProfileId(riderId);
		this.appointmentRepository.deleteByRiderId(riderId);
		this.riderDrivingLicenseDocumentRepository.deleteByRiderProfileId(riderId);
		this.riderBackgroundVerificationDocumentRepository.deleteByRiderProfileId(riderId);
		this.riderFoodCardRepository.deleteByRiderProfileId(riderId);
		this.riderJobDetailsRepository.deleteByProfileId(riderId);
		this.uploadedDocumentRepository.deleteByRiderProfileId(riderId);
		riderLocationService.deleteRiderLocation(riderId);

		return riderProfile;
	}

	public RiderDetailsDto getFilteredRiderProfile(RiderProfile riderProfile, String[] filters) {
		log.info("Inside getFilteredRiderProfile method with filters: {}", filters);

		String id = riderProfile.getId();
		Optional<RiderDeviceDetails> riderDeviceDetails = Optional.empty();

		for (String filterName: filters) {
			if (RiderProfileFilters.RIDER_DEVICE_DETAILS.name().equalsIgnoreCase(filterName)) {
				riderDeviceDetails = this.riderDeviceDetailRepository.findByProfileId(id);
			}
		}

		return RiderDetailsDto.
				of(riderProfile, Optional.empty(), Optional.empty(), Optional.empty(),
						Optional.empty(), Optional.empty(), Optional.empty(),
						Collections.emptyList(), Optional.empty(),
						Optional.empty(), riderDeviceDetails, Optional.empty(),null);
	}

}