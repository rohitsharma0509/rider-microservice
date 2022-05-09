package com.scb.rider.service.document;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.*;

import com.scb.rider.constants.Constants;
import com.scb.rider.constants.DocumentType;
import com.scb.rider.constants.SmsConstants;
import com.scb.rider.exception.DocumentAlreadyApprovedException;
import com.scb.rider.kafka.SmsPublisher;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.repository.RiderProfileRepository;
import com.scb.rider.util.PropertyUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.scb.rider.exception.DataNotFoundException;
import com.scb.rider.exception.MandatoryFieldMissingException;
import com.scb.rider.model.document.RiderBackgroundVerificationDocument;
import com.scb.rider.model.dto.RiderBackgroundVerificationDetailsRequest;
import com.scb.rider.model.dto.RiderBackgroundVerificationDetailsResponse;
import com.scb.rider.model.enumeration.MandatoryCheckStatus;
import com.scb.rider.repository.RiderBackgroundVerificationDocumentRepository;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
public class RiderBackgroundVerificationDocumentService {

	@Autowired
	private RiderProfileRepository riderProfileRepository;

	@Autowired
	private RiderBackgroundVerificationDocumentRepository riderBackgroundVerificationRepository;

	@Autowired
	private PropertyUtils propertyUtils;

	@Autowired
	private SmsPublisher smsPublisher;

	@Autowired
	private RiderUploadedDocumentService riderUploadedDocumentService;

	public RiderBackgroundVerificationDetailsResponse addBackgroundVerificationDetails(String riderId,
			RiderBackgroundVerificationDetailsRequest backgroundVerificationDetailsRequest, String requestedBy) {
		RiderProfile riderProfile = this.riderProfileRepository.findById(riderId)
				.orElseThrow(() -> new DataNotFoundException("Record not found for id " + riderId));

		Optional<RiderBackgroundVerificationDocument> backgroundVerificationDocument = riderBackgroundVerificationRepository
				.findByRiderProfileId(riderProfile.getId());

		if (backgroundVerificationDocument.isPresent()) {
			return RiderBackgroundVerificationDetailsResponse.of(updateBackgroundVerificationDetails(
					backgroundVerificationDetailsRequest, backgroundVerificationDocument.get(), requestedBy));
		}

		if (Constants.OPS.equals(requestedBy) && MandatoryCheckStatus.REJECTED.equals(backgroundVerificationDetailsRequest.getStatus()) && StringUtils.isEmpty(backgroundVerificationDetailsRequest.getReason())) {
			throw new MandatoryFieldMissingException("Reason is missing for id " + riderId);
		}

		riderUploadedDocumentService.deleteAndSaveDocuments(riderId, DocumentType.BACKGROUND_VERIFICATION_FORM, backgroundVerificationDetailsRequest.getDocumentUrls());

		List<String> docUrls = !CollectionUtils.isEmpty(backgroundVerificationDetailsRequest.getDocumentUrls()) ? backgroundVerificationDetailsRequest.getDocumentUrls()
				: (StringUtils.isNotBlank(backgroundVerificationDetailsRequest.getDocumentUrl()) ? Arrays.asList(backgroundVerificationDetailsRequest.getDocumentUrl()) : null);

		RiderBackgroundVerificationDocument mappedDocument = RiderBackgroundVerificationDocument.builder()
				.riderProfileId(riderId).status(MandatoryCheckStatus.PENDING)
				.dueDate(backgroundVerificationDetailsRequest.getDueDate())
				.reason(backgroundVerificationDetailsRequest.getReason())
				.documentUrl(backgroundVerificationDetailsRequest.getDocumentUrl())
				.documentUrls(docUrls)
				.updatedBy(backgroundVerificationDetailsRequest.getUpdatedBy()).build();
		mappedDocument = riderBackgroundVerificationRepository.save(mappedDocument);

		return RiderBackgroundVerificationDetailsResponse.of(mappedDocument);
	}


	public RiderBackgroundVerificationDocument getBackgroundVerificationDetailsByProfileId(String riderId) {
		RiderProfile riderProfile = riderProfileRepository.findById(riderId)
				.orElseThrow(() -> new DataNotFoundException("Record not found for id " + riderId));
		return riderBackgroundVerificationRepository.findByRiderProfileId(riderProfile.getId())
				.orElseThrow(() -> new DataNotFoundException("Record not found for id " + riderId));
	}

	public RiderBackgroundVerificationDocument updateBackgroundVerificationDetails(
			RiderBackgroundVerificationDetailsRequest request, RiderBackgroundVerificationDocument document, String requestedBy) {
		RiderProfile riderProfile = this.riderProfileRepository.findById(document.getRiderProfileId())
				.orElseThrow(() -> new DataNotFoundException("Record not found for id " + document.getRiderProfileId()));

		if(MandatoryCheckStatus.REJECTED.equals(request.getStatus()) && MandatoryCheckStatus.APPROVED.equals(document.getStatus())){
			throw new DocumentAlreadyApprovedException("Document is already Approved !", new Object[]{"Back Ground Verification "});
		}
		if(Constants.OPS.equals(requestedBy)){
			validateBackgroundVerificationRequest(request, document.getRiderProfileId());
		}
		riderUploadedDocumentService.deleteAndSaveDocuments(document.getRiderProfileId(), DocumentType.BACKGROUND_VERIFICATION_FORM, request.getDocumentUrls());

		sendDocumentRejectionEvent(riderProfile, request, document);
		if(MandatoryCheckStatus.REJECTED.equals(request.getStatus())){
			document.setRejectionTime(LocalDateTime.now());
		}
		document.setStatus(request.getStatus() != null ? request.getStatus() : document.getStatus());
		document.setDueDate(request.getDueDate() != null ? request.getDueDate() : document.getDueDate());
		document.setReason(request.getReason() != null ? request.getReason() : document.getReason());
		document.setComment(request.getComment() !=null ? request.getComment() : document.getComment());
		document.setDocumentUrl(StringUtils.isNotBlank(request.getDocumentUrl()) ? request.getDocumentUrl() : document.getDocumentUrl());
		List<String> docUrls = !CollectionUtils.isEmpty(request.getDocumentUrls()) ? request.getDocumentUrls() :
				(StringUtils.isNotBlank(request.getDocumentUrl()) ? Arrays.asList(request.getDocumentUrl()) : document.getDocumentUrls());
		docUrls = !CollectionUtils.isEmpty(docUrls) ? docUrls : (StringUtils.isNotBlank(document.getDocumentUrl()) ? Arrays.asList(document.getDocumentUrl()) : null);
		document.setDocumentUrls(docUrls);
		document.setUpdatedBy(request.getUpdatedBy());
		return riderBackgroundVerificationRepository.save(document);
	}
	
	private void validateBackgroundVerificationRequest(RiderBackgroundVerificationDetailsRequest request, String riderId) {
		if (MandatoryCheckStatus.PENDING.equals(request.getStatus()) && null == request.getDueDate()) {
			throw new MandatoryFieldMissingException("Due Date is missing for id " + riderId);
		}

		if (MandatoryCheckStatus.REJECTED.equals(request.getStatus()) && StringUtils.isEmpty(request.getReason())) {
			throw new MandatoryFieldMissingException("Reason is missing for id " + riderId);
		}
	}

	private void sendDocumentRejectionEvent(RiderProfile riderProfile, RiderBackgroundVerificationDetailsRequest request, RiderBackgroundVerificationDocument document) {
		if(Objects.nonNull(document.getStatus()) && !document.getStatus().equals(request.getStatus()) && MandatoryCheckStatus.REJECTED.equals(request.getStatus())) {
			log.info("BGV document rejected for riderId {}, Publishing sms event.", riderProfile.getId());
			String reason = request.getReason();
			String comment = request.getComment();
			if(Constants.OTHER.equalsIgnoreCase(reason) || Constants.OTHER_IN_THAI.equalsIgnoreCase(reason)) {
				reason = StringUtils.isNotBlank(comment) ? comment : StringUtils.EMPTY;
			} else {
				reason = StringUtils.isNotBlank(comment) ? (reason + " " + comment) : reason;
			}
			String message = MessageFormat.format(propertyUtils.getProperty(SmsConstants.BGV_REJECTED_MSG, Locale.forLanguageTag(Constants.THAI)), reason);
			smsPublisher.sendSmsNotificationEvent(riderProfile, message);
		}
	}
}
