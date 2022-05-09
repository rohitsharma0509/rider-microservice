package com.scb.rider.service.document;

import com.scb.rider.model.document.RiderEmergencyContact;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.dto.RiderEmergencyContactDto;
import com.scb.rider.model.enumeration.RiderProfileStage;
import com.scb.rider.repository.RiderEmergencyContactRepository;
import com.scb.rider.repository.RiderProfileRepository;
import com.scb.rider.util.CustomBeanUtils;
import com.scb.rider.util.LoggerUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Log4j2
public class RiderEmergencyContactService {

	@Autowired
	private RiderEmergencyContactRepository emergencyContactRepository;

	@Autowired
	private RiderProfileRepository riderProfileRepository;

	public RiderEmergencyContact saveEmergencyContact(RiderEmergencyContactDto emergencyContactDto) {
		log.info("Getting Rider profile emergency contact for id = {}", emergencyContactDto.getProfileId());
		RiderProfile riderProfile = riderProfileRepository.findById(emergencyContactDto.getProfileId())
				.orElseThrow(() -> LoggerUtils.logError(RiderEmergencyContact.class, emergencyContactDto.getProfileId(), "id"));
		Optional<RiderEmergencyContact> emergencyContact = this.emergencyContactRepository.findByProfileId(riderProfile.getId());
		if(emergencyContact.isPresent()) {
			log.info("Updating Rider Profile Emergency Contact ........");
			CustomBeanUtils.copyNonNullProperties(emergencyContactDto, emergencyContact.get());
			return this.emergencyContactRepository.save(emergencyContact.get());
		}

		riderProfile.setProfileStage(RiderProfileStage.STAGE_2);
		riderProfile.setIsReadyForAuthorization(Boolean.TRUE.toString());
		riderProfileRepository.save(riderProfile);
		log.info("Creating Rider Profile Emergency Contact ........");
		RiderEmergencyContact riderEmergencyContact = new RiderEmergencyContact();
		BeanUtils.copyProperties(emergencyContactDto, riderEmergencyContact);
		return this.emergencyContactRepository.save(riderEmergencyContact);
	}

	public RiderEmergencyContact getEmergencyContactByProfileId(final String id) {
		log.info("Getting Rider profile for profile id = {}", id);
		return this.emergencyContactRepository.findByProfileId(id)
				.orElseThrow(() -> LoggerUtils.logError(RiderEmergencyContact.class, id, "id"));
	}
}