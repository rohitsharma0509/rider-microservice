package com.scb.rider.service.job;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import com.scb.rider.constants.DocumentType;
import com.scb.rider.model.document.RiderUploadedDocument;
import com.scb.rider.repository.RiderUploadedDocumentRepository;
import com.scb.rider.service.document.RiderProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.scb.rider.client.BroadCastServiceFeignClient;
import com.scb.rider.constants.Constants;
import com.scb.rider.exception.DataNotFoundException;
import com.scb.rider.exception.JobAlreadyAcceptedException;
import com.scb.rider.exception.JobNotAcceptedException;
import com.scb.rider.exception.JobTimeOutException;
import com.scb.rider.kafka.KafkaPublisher;
import com.scb.rider.model.RiderJobStatusEventModel;
import com.scb.rider.model.document.RiderJobDetails;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.dto.BroadcastJobResponse;
import com.scb.rider.model.enumeration.AvailabilityStatus;
import com.scb.rider.model.enumeration.CancellationSource;
import com.scb.rider.model.enumeration.RiderJobStatus;
import com.scb.rider.repository.RiderJobDetailsRepository;
import com.scb.rider.repository.RiderProfileRepository;
import com.scb.rider.service.cache.RiderProfileUpdaterService;
import com.scb.rider.service.document.RiderActiveTrackingZoneService;
import com.scb.rider.util.DateFormatterUtils;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class RiderJobAcceptedService implements RiderJobService {

	@Autowired
	private RiderJobDetailsRepository riderJobDetailsRepository;

	@Autowired
	private RiderProfileRepository riderProfileRepository;

	@Autowired
	private KafkaPublisher kafkaPublisher;

	@Autowired
	private RiderActiveTrackingZoneService riderInactiveTrackingService;

	@Autowired
	private BroadCastServiceFeignClient broadCastServiceFeignClient;

	@Autowired
	private RiderUploadedDocumentRepository uploadedDocumentRepository;

	 @Autowired
	private RiderProfileUpdaterService riderProfileUpdaterService;

	@Autowired
	private RiderProfileService riderProfileService;

	@Override
	public RiderJobStatus getStatusType() {
		return RiderJobStatus.JOB_ACCEPTED;
	}

	@Override
	public RiderJobDetails performActionRiderJobStatus(MultipartFile file, String profileId, String jobId,
			BigDecimal fee, BigDecimal jobPrice, Boolean isJobPriceModified, String remark, CancellationSource source,
			LocalDateTime timeStamp, String updatedBy) {

		RiderProfile riderProfile = riderProfileRepository.findById(profileId)
				.orElseThrow(() -> new DataNotFoundException("Record not found for id " + profileId));
		Optional<RiderJobDetails> riderJobDetails = riderJobDetailsRepository.findByJobId(jobId);
		validateJob(jobId, riderProfile, riderJobDetails);
		RiderJobDetails jobDetail = new RiderJobDetails();
		jobDetail.setJobStatus(RiderJobStatus.JOB_ACCEPTED);
		jobDetail.setProfileId(profileId);
		jobDetail.setJobId(jobId);
		LocalDateTime jobAcceptedTime = Objects.nonNull(timeStamp) ? timeStamp : LocalDateTime.now();
		jobDetail.setJobAcceptedTime(jobAcceptedTime);

		if (remark != null)
			jobDetail.setRemarks(remark);
		try {
			jobDetail = riderJobDetailsRepository.save(jobDetail);
			// Update Rider Profile to Job in progress Stage to do not Accept Order in this
			// period
			log.info("JOB_ACCEPTED for job Id {} by rider {} , updating status to Inactive", jobId,
					riderProfile.getRiderId());
			riderProfile.setAvailabilityStatus(AvailabilityStatus.JobInProgress);
			riderProfileService.updateRiderAvailabilityStatus(riderProfile.getId(), AvailabilityStatus.JobInProgress);
			riderProfileUpdaterService.publish(riderProfile);
			log.info("RiderActiveTrackingService - Saving Rider Job Accepted Time Stamp Id:{}", riderProfile.getId());
			riderInactiveTrackingService.saveOrUpdateRiderInactiveStatus(riderProfile);
			log.info("Rider status changes successfully after accepting the job to {} ",
					riderProfile.getAvailabilityStatus());
		} catch (DuplicateKeyException exception) {
			log.error("Exception occurs while accepting job {}", exception.getMessage());
			throw new JobAlreadyAcceptedException(
					String.format("Job already exist with RiderID-%s", riderJobDetails.get().getProfileId()));
		}

		try {
			log.info("Getting Rider Profile Photo Details for id = {}", profileId);
			Optional<RiderUploadedDocument> uploadedDocument = this.uploadedDocumentRepository
					.findByRiderProfileIdAndDocumentType(profileId, DocumentType.PROFILE_PHOTO);

			log.info("Sending Kafka message to JOB-SERVICE for jobId {} and status {}", jobId, getStatusType());
			kafkaPublisher.publish(RiderJobStatusEventModel.builder().riderId(profileId).jobId(jobId)
					.dateTime(DateFormatterUtils.zonedDateTimeToString(jobAcceptedTime.atZone(ZoneOffset.UTC))).status(RiderJobStatus.JOB_ACCEPTED)
					.riderRRid(riderProfile.getRiderId()).evBikeUser(riderProfile.getEvBikeUser()).evBikeVendor(riderProfile.getEvBikeVendor())
					.rentingToday(riderProfile.getRentingToday())
					.driverName(riderProfile.getFirstName()+ " " + riderProfile.getLastName())
					.driverPhone(riderProfile.getPhoneNumber())
					.driverImageUrl(uploadedDocument.map(RiderUploadedDocument::getImageExternalUrl).orElse(null))
					.build()

			);
			log.info("Kafka message sent successfully to JOB-SERVICE for jobId {} and status {}", jobId,
					getStatusType());
		} catch (ExecutionException | InterruptedException e) {
			log.error("Exception while sending message to queue {}", e.getMessage());
			Thread.currentThread().interrupt();
		}
		return jobDetail;
	}

	private void validateJob(String jobId, RiderProfile riderProfile, Optional<RiderJobDetails> riderJobDetails) {
		if (riderProfile.getAvailabilityStatus().equals(AvailabilityStatus.JobInProgress))
			throw new JobAlreadyAcceptedException(
					String.format("Rider already having Job RiderID-%s", riderProfile.getRiderId()));

		if (riderJobDetails.isPresent()) {
		//Rider attempting to accept job changing availability status time
	     riderInactiveTrackingService.saveOrUpdateRiderInactiveStatus(riderProfile);
		  throw new JobAlreadyAcceptedException(
              String.format("Job already exist with RiderID-%s", riderJobDetails.get().getProfileId()));
		}
			
		BroadcastJobResponse broadcastJobResponse;

		try {
			broadcastJobResponse = broadCastServiceFeignClient.getBroadcastData(jobId);

		} catch (Exception e1) {
			log.error("Unable to accept job with job id-{} for riderId-{}", jobId, riderProfile.getRiderId());
			throw new JobNotAcceptedException(
					"Unable to accept job with job id-" + jobId + " for riderId-" + riderProfile.getRiderId());
		}
		if (!broadcastJobResponse.getBroadcastStatus().equalsIgnoreCase(Constants.BROADCASTING)) {
			log.info("Job timed out for JobId - {}", jobId);
			throw new JobTimeOutException("Job Timed out for job Id- " + jobId);

		}
	}
}

