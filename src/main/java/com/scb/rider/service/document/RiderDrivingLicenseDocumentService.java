package com.scb.rider.service.document;

import com.scb.rider.constants.Constants;
import com.scb.rider.constants.ErrorConstants;
import com.scb.rider.constants.SmsConstants;
import com.scb.rider.exception.DataNotFoundException;
import com.scb.rider.exception.DocumentAlreadyApprovedException;
import com.scb.rider.exception.RiderAlreadyExistsException;
import com.scb.rider.kafka.SmsPublisher;
import com.scb.rider.model.document.RiderDrivingLicenseDocument;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.dto.RiderDrivingLicenseRequest;
import com.scb.rider.model.enumeration.MandatoryCheckStatus;
import com.scb.rider.repository.RiderDrivingLicenseDocumentRepository;
import com.scb.rider.repository.RiderProfileRepository;
import com.scb.rider.util.PropertyUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class RiderDrivingLicenseDocumentService {

    @Autowired
    private RiderDrivingLicenseDocumentRepository riderDrivingLicenseDocumentRepository;

    @Autowired
    private RiderProfileRepository riderProfileRepository;

    @Autowired
    private SmsPublisher smsPublisher;

    @Autowired
    private PropertyUtils propertyUtils;

    public RiderDrivingLicenseDocument createRiderDrivingLicense(RiderDrivingLicenseDocument licenseDocument) {

        licenseDocument.setStatus(MandatoryCheckStatus.PENDING);
        return riderDrivingLicenseDocumentRepository.save(licenseDocument);

    }

    public RiderDrivingLicenseDocument updateRiderDrivingLicense(RiderDrivingLicenseDocument licenseDocument) {

        return riderDrivingLicenseDocumentRepository.save(licenseDocument);

    }

    public Optional<RiderDrivingLicenseDocument> findRiderDrivingLicenseByProfileId(String profileId) {
        return riderDrivingLicenseDocumentRepository.findByRiderProfileId(profileId);
    }

    public RiderDrivingLicenseDocument findRiderDrivingLicenceByLicenseNo(String licenseNumber) {
        return riderDrivingLicenseDocumentRepository.findByDrivingLicenseNumber(licenseNumber);
    }

    public Optional<RiderDrivingLicenseDocument> findRiderDrivingLicenseById(String id) {
        return riderDrivingLicenseDocumentRepository.findById(id);
    }

    public RiderDrivingLicenseDocument updateRiderDrivingLicense(RiderDrivingLicenseRequest licenseRequest,
                                                                 RiderDrivingLicenseDocument licenseDocument) {
        RiderProfile riderProfile = riderProfileRepository.findById(licenseDocument.getRiderProfileId())
                .orElseThrow(() -> new DataNotFoundException("Record not found for id " + licenseDocument.getRiderProfileId()));

        if(MandatoryCheckStatus.REJECTED.equals(licenseRequest.getStatus()) && MandatoryCheckStatus.APPROVED.equals(licenseDocument.getStatus())){
            throw new DocumentAlreadyApprovedException("Document is already Approved !", new Object[]{"Driving License"});
        }
        sendDocumentRejectionEvent(riderProfile, licenseRequest, licenseDocument);

        licenseDocument.setDrivingLicenseNumber(licenseRequest.getDrivingLicenseNumber() != null ?
                licenseRequest.getDrivingLicenseNumber() : licenseDocument.getDrivingLicenseNumber());

        licenseDocument.setTypeOfLicense(licenseRequest.getTypeOfLicense() != null ?
                licenseRequest.getTypeOfLicense() : licenseDocument.getTypeOfLicense());

        licenseDocument.setDocumentUrl(licenseRequest.getDocumentUrl() != null ?
                licenseRequest.getDocumentUrl() : licenseDocument.getDocumentUrl());

        licenseDocument.setDateOfExpiry(licenseRequest.getDateOfExpiry() != null ?
                licenseRequest.getDateOfExpiry() : licenseDocument.getDateOfExpiry());

        licenseDocument.setDateOfIssue(licenseRequest.getDateOfIssue() != null ?
                licenseRequest.getDateOfIssue() : licenseDocument.getDateOfIssue());

        licenseDocument.setStatus(licenseRequest.getStatus() != null ? licenseRequest.getStatus() : licenseDocument.getStatus());

        if(MandatoryCheckStatus.REJECTED.equals(licenseRequest.getStatus())) {
            licenseDocument.setReason(licenseRequest.getReason() != null ? licenseRequest.getReason() : licenseDocument.getReason());
            licenseDocument.setComment(licenseRequest.getComment() != null ? licenseRequest.getComment() : licenseDocument.getComment());
            licenseDocument.setRejectionTime(LocalDateTime.now());
        }
        licenseDocument.setUpdatedBy(licenseRequest.getUpdateBy());
        return riderDrivingLicenseDocumentRepository.save(licenseDocument);
    }


    private void sendDocumentRejectionEvent(RiderProfile riderProfile, RiderDrivingLicenseRequest request, RiderDrivingLicenseDocument document) {
        if(Objects.nonNull(document.getStatus()) && !document.getStatus().equals(request.getStatus()) && MandatoryCheckStatus.REJECTED.equals(request.getStatus())) {
            log.info("DL rejected for riderId {}, Publishing sms event.", riderProfile.getId());
            String reason = request.getReason();
            String comment = request.getComment();
            if(Constants.OTHER.equalsIgnoreCase(reason) || Constants.OTHER_IN_THAI.equalsIgnoreCase(reason)) {
                reason = StringUtils.isNotBlank(comment) ? comment : StringUtils.EMPTY;
            } else {
                reason = StringUtils.isNotBlank(comment) ? (reason + " " + comment) : reason;
            }

            String message = MessageFormat.format(propertyUtils.getProperty(SmsConstants.DL_REJECTED_MSG, Locale.forLanguageTag(Constants.THAI)), reason);
            smsPublisher.sendSmsNotificationEvent(riderProfile, message);
        }
    }
}
