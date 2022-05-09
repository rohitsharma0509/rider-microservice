package com.scb.rider.service.document;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.scb.rider.model.document.RiderEVForm;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.dto.RiderEVFormDto;
import com.scb.rider.model.enumeration.MandatoryCheckStatus;
import com.scb.rider.repository.RiderEVFormRepository;
import com.scb.rider.repository.RiderProfileRepository;
import com.scb.rider.service.cache.RiderProfileUpdaterService;
import com.scb.rider.util.LoggerUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RiderEVFormService {

	@Autowired
	private RiderProfileRepository riderProfileRepository;

	@Autowired
	private RiderEVFormRepository evFormRepository;

	@Autowired
	private RiderProfileUpdaterService riderProfileUpdaterService;
	
	
	public RiderEVForm getRiderEVForm(String riderProfileId) {
		log.info("Fetching EV Form for Rider: {}", riderProfileId);
		return evFormRepository.findByRiderProfileId(riderProfileId)
				.orElseThrow(() -> LoggerUtils.logError(RiderEVForm.class, riderProfileId, "riderId"));

	}

	public RiderEVForm saveRiderEVForm(RiderEVFormDto evFormDto) {
		RiderProfile riderProfile = riderProfileRepository.findById(evFormDto.getRiderProfileId())
				.orElseThrow(() -> LoggerUtils.logError(RiderProfile.class, evFormDto.getRiderProfileId(), "riderId"));

		Optional<RiderEVForm> riderEVForm = evFormRepository.findByRiderProfileId(evFormDto.getRiderProfileId());

		if (riderEVForm.isPresent()) {
			log.info("Saving the existing EV Form as:{}", evFormDto);
			RiderEVForm evFormUpdated = evFormEntityBuilder(evFormDto, riderEVForm.get());
			
			riderProfileUpdaterService.publish(riderProfile);
			
			return evFormRepository.save(evFormUpdated);
		}
		RiderEVForm newEvForm = new RiderEVForm();
		BeanUtils.copyProperties(evFormDto, newEvForm);	
		log.info("Saving the new rider EV Form: {}", newEvForm);
		return evFormRepository.save(newEvForm);
	}
	
	private RiderEVForm evFormEntityBuilder(RiderEVFormDto evFormDto, RiderEVForm evFormEntity) {

		evFormEntity.setRiderProfileId(evFormDto.getRiderProfileId() != null ?
				evFormDto.getRiderProfileId() : evFormEntity.getRiderProfileId());
		
		evFormEntity.setDocumentUrl(evFormDto.getDocumentUrl() != null ?
				evFormDto.getDocumentUrl() : evFormEntity.getDocumentUrl());

		evFormEntity.setEvRentalAgreementNumber(evFormDto.getEvRentalAgreementNumber() != null ?
				evFormDto.getEvRentalAgreementNumber() : evFormEntity.getEvRentalAgreementNumber());

		evFormEntity.setProvince(evFormDto.getProvince() != null ?
				evFormDto.getProvince() : evFormEntity.getProvince());

		evFormEntity.setStatus(evFormDto.getStatus() != null ?
				evFormDto.getStatus() : evFormEntity.getStatus());

		if(MandatoryCheckStatus.REJECTED.equals(evFormDto.getStatus())) {
			evFormEntity.setReason(evFormDto.getReason() != null ? evFormDto.getReason() : evFormEntity.getReason());
			evFormEntity.setComment(evFormDto.getComment() != null ? evFormDto.getComment() : evFormEntity.getComment());
			evFormEntity.setRejectionTime(LocalDateTime.now());
        } else {
        	evFormEntity.setReason(null);
        	evFormEntity.setComment(null);
        	evFormEntity.setRejectionTime(null);
        }
		return evFormEntity;
	}
	
}
