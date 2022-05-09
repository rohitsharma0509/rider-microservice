package com.scb.rider.service.document;

import java.util.Optional;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
import com.scb.rider.util.LoggerUtils;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class RiderPreferredZoneService {

	@Autowired
	private RiderProfileRepository riderProfileRepository;

	@Autowired
	private RiderDeviceDetailRepository riderDeviceDetailRepository;
	@Autowired
	private RiderDeviceService riderDeviceService;

	@Autowired
	private RiderProfileUpdaterService riderProfileUpdaterService;
	
	public RiderPreferredZones savePreferredZone(RiderPreferredZoneDto zone) {
		log.info("saving the zone for id={}", zone.getRiderProfileId());
		RiderProfile riderProfile = riderProfileRepository.findById(zone.getRiderProfileId()).orElseThrow(
				() -> LoggerUtils.logError(RiderPreferredZones.class, zone.getRiderProfileId(), "profileId"));
		RiderPreferredZones zonesPojo = new RiderPreferredZones();
		RiderPreferredZones riderPreferredZones = riderProfile.getRiderPreferredZones();
		String prevZoneId = "";

		if (ObjectUtils.isNotEmpty(riderPreferredZones) &&  !riderPreferredZones.getPreferredZoneName().equals("")) {
			if (ObjectUtils.isEmpty(riderProfile.getStatus())
					|| !StringUtils.equals(RiderStatus.AUTHORIZED.name(), riderProfile.getStatus().name())
					|| StringUtils.equalsIgnoreCase(AvailabilityStatus.Active.name(),
							riderProfile.getAvailabilityStatus().name())) {
				throw new AccessDeniedException("Rider should be authorized and inactive before updating data");
			}
			prevZoneId = riderPreferredZones.getPreferredZoneId();
		}
		BeanUtils.copyProperties(zone, zonesPojo);
		riderProfile.setRiderPreferredZones(zonesPojo);
		riderProfileRepository.save(riderProfile);
		
		
		
		riderProfileUpdaterService.publish(riderProfile);
		updateFirebaseNotification(riderProfile, prevZoneId);
		return zonesPojo;
	}

	public RiderPreferredZones savePreferredZoneOpsMember(RiderPreferredZoneDto zone) {
		log.info("saving the zone for id={}", zone.getRiderProfileId());
		RiderProfile riderProfile = riderProfileRepository.findById(zone.getRiderProfileId()).orElseThrow(
				() -> LoggerUtils.logError(RiderPreferredZones.class, zone.getRiderProfileId(), "profileId"));
		RiderPreferredZones zonesPojo = new RiderPreferredZones();
		String prevZoneId = "";
		if (ObjectUtils.isNotEmpty(riderProfile.getRiderPreferredZones())) {
			if (ObjectUtils.isEmpty(riderProfile.getStatus()) || StringUtils
					.equalsIgnoreCase(AvailabilityStatus.Active.name(), riderProfile.getAvailabilityStatus().name())) {
				throw new AccessDeniedException("Rider should be inactive before updating data");
			}
			prevZoneId = riderProfile.getRiderPreferredZones().getPreferredZoneId();
		}
		BeanUtils.copyProperties(zone, zonesPojo);
		riderProfile.setRiderPreferredZones(zonesPojo);
		riderProfileRepository.save(riderProfile);
		riderProfileUpdaterService.publish(riderProfile);


		updateFirebaseNotification(riderProfile, prevZoneId);
		return zonesPojo;
	}

	private void updateFirebaseNotification(RiderProfile riderProfile, String prevZoneId) {
		log.info("updating firebase topic for rider-" + riderProfile.getRiderId() + "Prev zone-" + prevZoneId);
		Optional<RiderDeviceDetails> riderDeviceDetail = riderDeviceDetailRepository
				.findByProfileId(riderProfile.getId());

		if (riderDeviceDetail.isPresent()) {
			riderDeviceService.subscribeFireBaseNotificationTopic(riderProfile,
					riderDeviceDetail.get(), prevZoneId);
		}
	}
}
