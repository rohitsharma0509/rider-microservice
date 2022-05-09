package com.scb.rider.service.document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.scb.rider.client.LocationServiceFeignClient;
import com.scb.rider.model.document.RiderActiveStatusDetails;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.dto.ZoneEntity;
import com.scb.rider.model.enumeration.AvailabilityStatus;
import com.scb.rider.repository.RiderActiveStatusDetailsRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RiderActiveTrackingZoneService {

	@Autowired
	private LocationServiceFeignClient locationServiceFeignClient;

	@Autowired
	private RiderActiveStatusDetailsRepository riderActiveStatusDetailsRepository;

	@Async("customTaskExecutor")
	public void addZoneDetails( RiderProfile riderProfile) {

		if (riderProfile.getAvailabilityStatus().equals(AvailabilityStatus.Active)) {
			log.info("RiderActiveTrackingService - adding zone footprints for rider- {} ",riderProfile.getRiderId());
			
			Optional<RiderActiveStatusDetails> getRiderActiveStatusDetails = riderActiveStatusDetailsRepository
					.findByRiderId(riderProfile.getId());
			
			if (!getRiderActiveStatusDetails.isPresent() || Objects.isNull(getRiderActiveStatusDetails.get().getActiveTime()) ||(LocalDate.now()
					.isAfter(getRiderActiveStatusDetails.get().getActiveTime().toLocalDate())
					|| StringUtils.isEmpty(getRiderActiveStatusDetails.get().getZoneName()))) {
				log.info("RiderActiveTrackingService - updating zone footprints for rider- {} ",riderProfile.getRiderId());
				RiderActiveStatusDetails riderActiveStatusDetails = !getRiderActiveStatusDetails.isPresent()
						? RiderActiveStatusDetails.builder().build()
						: getRiderActiveStatusDetails.get();
				try {
					log.info("RiderActiveTrackingService - Updating riderActiveZone for riderId-{}", riderProfile.getRiderId());
					ZoneEntity zoneEntity = locationServiceFeignClient.getRiderActiveZone(riderProfile.getId());
					riderActiveStatusDetails.setZoneName(zoneEntity.getZoneName());
					riderActiveStatusDetails.setZoneId(zoneEntity.getZoneId());

				} catch (Exception e) {
					log.error("RiderActiveTrackingService - No record of location for riderId :{}", riderProfile.getRiderId());
					riderActiveStatusDetails.setZoneName("");

				}
				riderActiveStatusDetails.setActiveTime(LocalDateTime.now());
				riderActiveStatusDetails.setRiderRrId(riderProfile.getRiderId());
				riderActiveStatusDetails.setId(new ObjectId(riderProfile.getId()));
				riderActiveStatusDetails.setRiderId(riderProfile.getId());
				riderActiveStatusDetails.setAvailabilityStatus(riderProfile.getAvailabilityStatus());
				log.info("RiderActiveTrackingService - saving zone footprints for rider- {} ",riderProfile.getRiderId());
				
				riderActiveStatusDetailsRepository.save(riderActiveStatusDetails);
			}
			
		}
		log.info("RiderActiveTrackingService - saveOrUpdateRiderInactiveStatus ID: {}", riderProfile.getRiderId());
		saveOrUpdateRiderInactiveStatus(riderProfile);

	}

	@Async("customTaskExecutor")
	public void saveOrUpdateRiderInactiveStatus(RiderProfile riderProfile) {
		try {

			RiderActiveStatusDetails riderInactiveTrackingDetails = RiderActiveStatusDetails.builder().build();
			Optional<RiderActiveStatusDetails> riderInactiveTrackingDetailsDb = riderActiveStatusDetailsRepository
					.findByRiderId(riderProfile.getId());
			LocalDateTime riderInactiveTime = LocalDateTime.now();
			if (!riderInactiveTrackingDetailsDb.isPresent()) {
			  log.info("RiderActiveTrackingService - Adding Ride Document in Activity Tracking Id: {}", riderProfile.getId());
				riderInactiveTrackingDetails = RiderActiveStatusDetails.builder().id(new ObjectId(riderProfile.getId())).riderId(riderProfile.getId())
						.riderRrId(riderProfile.getRiderId()).availabilityStatus(riderProfile.getAvailabilityStatus())
						.availabilityStatusUpdatedTime(riderInactiveTime).availabilityStatusUpdatedTimeString(dateTimeToString(riderInactiveTime)).build();
				setJobStatusChangeUpdatedTime(riderInactiveTrackingDetails);
			} else {
			    log.info("RiderActiveTrackingService - Updating Rider Document in Activity Tracking Id: {}", riderProfile.getId());
				riderInactiveTrackingDetails = riderInactiveTrackingDetailsDb.get();
				updateJobStatusChangeUpdatedTime(riderProfile, riderInactiveTrackingDetails);
				riderInactiveTrackingDetails.setAvailabilityStatus(riderProfile.getAvailabilityStatus());
				riderInactiveTrackingDetails.setAvailabilityStatusUpdatedTimeString(dateTimeToString(riderInactiveTime));
				riderInactiveTrackingDetails.setAvailabilityStatusUpdatedTime(LocalDateTime.now());
			}
			riderActiveStatusDetailsRepository.save(riderInactiveTrackingDetails);
			log.info("RiderActiveTrackingService - Rider Document Saved in Status Method Id:{}", riderProfile.getId());
		} catch (Exception ex) {
			log.error("RiderActiveTrackingService - Error Occured While Saving Rider Inactivity Tracking Details: {}", ex);
		}
	}

	private void setJobStatusChangeUpdatedTime(RiderActiveStatusDetails riderInactiveTrackingDetails) {
		if (riderInactiveTrackingDetails.getAvailabilityStatus().equals(AvailabilityStatus.JobInProgress)) {
			riderInactiveTrackingDetails.setJobStatusChangeUpdatedTime(LocalDateTime.now());
		}
	}

	private void updateJobStatusChangeUpdatedTime(RiderProfile riderProfile,
			RiderActiveStatusDetails riderInactiveTrackingDetails) {
		if (validateJobInProgressStatus(riderProfile, riderInactiveTrackingDetails)) {
		  log.info("RiderActiveTrackingService - ValidateJobInProgressStatus - True Id:{}", riderProfile.getRiderId());
			LocalDateTime jobStatusTime =  LocalDateTime.now();
		    riderInactiveTrackingDetails.setJobStatusChangeUpdatedTime(jobStatusTime);
		    riderInactiveTrackingDetails.setJobStatusChangeUpdatedTimeString(dateTimeToString(jobStatusTime));
		}
		log.info("RiderActiveTrackingService - updateJobStatusChangeUpdatedTime Id:{}", riderProfile.getRiderId());
	}

	private boolean validateJobInProgressStatus(RiderProfile riderProfile,
			RiderActiveStatusDetails riderInactiveTrackingDetails) {
		return (riderInactiveTrackingDetails.getAvailabilityStatus() // FOOD_DELIVERD OR ORDER_CANCELLED
				.equals(AvailabilityStatus.JobInProgress)
				&& !riderProfile.getAvailabilityStatus().equals(AvailabilityStatus.JobInProgress))
				|| (!riderInactiveTrackingDetails.getAvailabilityStatus() // JOB_ACCEPETED
						.equals(AvailabilityStatus.JobInProgress)
						&& riderProfile.getAvailabilityStatus().equals(AvailabilityStatus.JobInProgress));
	}
	
	public static String dateTimeToString(LocalDateTime localDateTime) {
	    String PATTERN ="yyyy-MM-dd'T'HH:mm:ss.SSS";
	    java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern(PATTERN);
	    String dateTimeString = localDateTime.format(formatter) + "Z";
	    return dateTimeString;
	  }

}