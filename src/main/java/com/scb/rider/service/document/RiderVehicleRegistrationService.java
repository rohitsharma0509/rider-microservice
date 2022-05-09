package com.scb.rider.service.document;

import com.scb.rider.client.LocationServiceFeignClient;
import com.scb.rider.constants.Constants;
import com.scb.rider.constants.DocumentType;
import com.scb.rider.constants.ErrorConstants;
import com.scb.rider.constants.SmsConstants;
import com.scb.rider.exception.DataNotFoundException;
import com.scb.rider.exception.DocumentAlreadyApprovedException;
import com.scb.rider.exception.RiderAlreadyExistsException;
import com.scb.rider.kafka.SmsPublisher;
import com.scb.rider.model.RiderFoodBoxSize;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.document.RiderUploadedDocument;
import com.scb.rider.model.document.RiderVehicleRegistrationDocument;
import com.scb.rider.model.dto.FoodCartUpdateRequest;
import com.scb.rider.model.dto.RiderVehicleStatusRequest;
import com.scb.rider.model.dto.RiderVehicleRegistrationDetailsRequest;
import com.scb.rider.model.dto.RiderVehicleRegistrationDetailsResponse;
import com.scb.rider.model.enumeration.MandatoryCheckStatus;
import com.scb.rider.repository.RiderProfileRepository;
import com.scb.rider.repository.RiderUploadedDocumentRepository;
import com.scb.rider.repository.RiderVehicleRegistrationRepository;
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
public class RiderVehicleRegistrationService {

    @Autowired
    private RiderVehicleRegistrationRepository vehicleRegistrationRepository;

    @Autowired
    private RiderProfileRepository riderProfileRepository;

    @Autowired
    private LocationServiceFeignClient locationServiceFeignClient;

    @Autowired
    private PropertyUtils propertyUtils;

    @Autowired
    private RiderUploadedDocumentRepository uploadedDocumentRepository;

    @Autowired
    private SmsPublisher smsPublisher;

    public RiderVehicleRegistrationDetailsResponse createRiderVehicleRegistrationDetails(String id,
                                                                                         RiderVehicleRegistrationDetailsRequest request) {

        Optional<RiderVehicleRegistrationDocument> drivingLicenseDocument = vehicleRegistrationRepository.findByRiderProfileId(id);

        if (drivingLicenseDocument.isPresent()) {
            return RiderVehicleRegistrationDetailsResponse.of(updateRiderVehicleRegistrationDetails(
                    request, drivingLicenseDocument.get()));
        }
        if (isRiderAlreadyExist(request.getRegistrationNo(),request.getProvince())) {
            throw new RiderAlreadyExistsException(propertyUtils
                    .getProperty(ErrorConstants.RIDER_VEHICLE_REG_NUMBER_ALREADY_EXIST));
        }
        Optional<RiderUploadedDocument> riderUploadedDocument = uploadedDocumentRepository.findByRiderProfileIdAndDocumentType(id, DocumentType.VEHICLE_WITH_FOOD_CARD);

        final RiderVehicleRegistrationDocument mappedDocument = RiderVehicleRegistrationDocument.builder()
                .registrationNo(request.getRegistrationNo())
                .registrationCardId(request.getRegistrationCardId())
                .expiryDate(request.getExpiryDate())
                .registrationDate(request.getRegistrationDate())
                .makerModel(request.getMakerModel())
                .province(request.getProvince())
                .riderProfileId(id)
                .uploadedVehicleDocUrl(request.getUploadedVehicleDocUrl())
                .foodBoxSize(request.getFoodBoxSize())
                .updatedBy(request.getUpdatedBy())
                .build();

        riderUploadedDocument.ifPresent(uploadedDocument -> mappedDocument.setUploadedFoodCardUrl(uploadedDocument.getImageUrl()));
        mappedDocument.setStatus(MandatoryCheckStatus.PENDING);
        mappedDocument.setFoodCardStatus(MandatoryCheckStatus.PENDING);
        RiderVehicleRegistrationDocument vehicleRegistrationDocument = vehicleRegistrationRepository.save(mappedDocument);
        return RiderVehicleRegistrationDetailsResponse.of(vehicleRegistrationDocument);
    }

    public Optional<RiderVehicleRegistrationDocument> findRiderVehicleRegistrationDetailsByProfileId(String profileId) {

        return vehicleRegistrationRepository.findByRiderProfileId(profileId);

    }

    public RiderVehicleRegistrationDocument updateRiderVehicleRegistrationDetails(RiderVehicleRegistrationDetailsRequest request,
                                                                                  RiderVehicleRegistrationDocument document) {

        RiderProfile riderProfile = riderProfileRepository.findById(document.getRiderProfileId())
                .orElseThrow(() -> new DataNotFoundException("Record not found for id " + document.getRiderProfileId()));

       if(!document.getRegistrationNo().equals(request.getRegistrationNo())
                && isRiderAlreadyExist(request.getRegistrationNo(), request.getProvince())) {
                throw new RiderAlreadyExistsException(propertyUtils
                        .getProperty(ErrorConstants.RIDER_VEHICLE_REG_NUMBER_ALREADY_EXIST));
        }
        if(MandatoryCheckStatus.REJECTED.equals(request.getStatus()) && MandatoryCheckStatus.APPROVED.equals(document.getStatus())){
            throw new DocumentAlreadyApprovedException("Document is already Approved !", new Object[]{"Vehicle Registration"});
        }
        if(MandatoryCheckStatus.REJECTED.equals(request.getFoodCardStatus()) && MandatoryCheckStatus.APPROVED.equals(document.getFoodCardStatus())){
            throw new DocumentAlreadyApprovedException("Document is already Approved !", new Object[]{"Food Card "});
        }
        sendDocumentRejectionEvent(riderProfile, document.getStatus(), request.getStatus(), SmsConstants.VEHICLE_REGISTRATION_REJECTED_MSG,
                request.getVehicleRejectionReason(), request.getVehicleRejectionComment());
        sendDocumentRejectionEvent(riderProfile, document.getFoodCardStatus(), request.getFoodCardStatus(), SmsConstants.FOOD_CART_REJECTED_MSG,
                request.getFoodCardRejectionReason(), request.getFoodCardRejectionComment());

        document.setExpiryDate(request.getExpiryDate() != null ?
                request.getExpiryDate() : document.getExpiryDate());

        document.setRegistrationNo(request.getRegistrationNo() != null ?
                request.getRegistrationNo() : document.getRegistrationNo());

        document.setRegistrationCardId(request.getRegistrationCardId() != null ?
                request.getRegistrationCardId() : document.getRegistrationCardId());

        document.setRegistrationDate(request.getRegistrationDate() != null ?
                request.getRegistrationDate() : document.getRegistrationDate());

        document.setMakerModel(request.getMakerModel() != null ?
                request.getMakerModel() : document.getMakerModel());

        document.setProvince(request.getProvince() != null ?
                request.getProvince() : document.getProvince());

        document.setUploadedVehicleDocUrl(request.getUploadedVehicleDocUrl() != null ?
                request.getUploadedVehicleDocUrl() : document.getUploadedVehicleDocUrl());

        document.setUploadedFoodCardUrl(request.getUploadedFoodCardUrl() != null ?
                request.getUploadedFoodCardUrl() : document.getUploadedFoodCardUrl());

        document.setStatus(request.getStatus() != null ? request.getStatus() : document.getStatus());

        document.setFoodCardStatus(request.getFoodCardStatus() != null ? request.getFoodCardStatus() : document.getFoodCardStatus());

        document.setFoodBoxSize(Objects.nonNull(request.getFoodBoxSize()) ? request.getFoodBoxSize() : document.getFoodBoxSize());

        if(MandatoryCheckStatus.REJECTED.equals(request.getStatus())) {
            document.setVehicleRejectionReason(request.getVehicleRejectionReason());
            document.setVehicleRejectionComment(request.getVehicleRejectionComment());
            document.setVehicleRejectionTime(LocalDateTime.now());
        }
        if(MandatoryCheckStatus.REJECTED.equals(request.getFoodCardStatus())) {
            document.setFoodCardRejectionReason(request.getFoodCardRejectionReason());
            document.setFoodCardRejectionComment(request.getFoodCardRejectionComment());
            document.setFoodCardRejectionTime(LocalDateTime.now());
        }
        if(MandatoryCheckStatus.APPROVED.equals(request.getFoodCardStatus())){
            log.info("Updating food box size for rider {} {}",document.getRiderProfileId(),request.getFoodBoxSize());
            locationServiceFeignClient.updateRiderFoodBoxSize(
                    RiderFoodBoxSize.builder()
                            .riderId(document.getRiderProfileId())
                            .foodBoxSize(request.getFoodBoxSize().toString())
                            .build()
            );
            log.info("Updated food box size for rider {} {}",document.getRiderProfileId(),request.getFoodBoxSize());
        }
        document.setUpdatedBy(request.getUpdatedBy());
        return vehicleRegistrationRepository.save(document);
    }

    private boolean isRiderAlreadyExist(String registrationNo, String province) {
        return vehicleRegistrationRepository.findByRegistrationNoAndProvince(registrationNo,province) != null;
    }

    public RiderVehicleRegistrationDocument updateVehicleRegistrationStatus(final RiderVehicleStatusRequest riderVehicleStatusRequest, final RiderVehicleRegistrationDocument riderVehicleRegistrationDocument){
        RiderProfile riderProfile = riderProfileRepository.findById(riderVehicleRegistrationDocument.getRiderProfileId())
                .orElseThrow(() -> new DataNotFoundException("Record not found for id " + riderVehicleRegistrationDocument.getRiderProfileId()));

        if(DocumentType.VEHICLE_REGISTRATION == riderVehicleStatusRequest.getDocumentType()) {
            sendDocumentRejectionEvent(riderProfile, riderVehicleRegistrationDocument.getStatus(), riderVehicleStatusRequest.getStatus(),
                    SmsConstants.VEHICLE_REGISTRATION_REJECTED_MSG, riderVehicleStatusRequest.getVehicleRejectionReason(), riderVehicleStatusRequest.getVehicleRejectionComment());
            riderVehicleRegistrationDocument.setStatus(riderVehicleStatusRequest.getStatus());
        } else if(DocumentType.VEHICLE_WITH_FOOD_CARD == riderVehicleStatusRequest.getDocumentType()){
            sendDocumentRejectionEvent(riderProfile, riderVehicleRegistrationDocument.getFoodCardStatus(), riderVehicleStatusRequest.getStatus(),
                    SmsConstants.FOOD_CART_REJECTED_MSG, riderVehicleStatusRequest.getFoodCardRejectionReason(), riderVehicleStatusRequest.getFoodCardRejectionComment());
            riderVehicleRegistrationDocument.setFoodCardStatus(riderVehicleStatusRequest.getStatus());
        }

        return vehicleRegistrationRepository.save(riderVehicleRegistrationDocument);
    }

    public RiderVehicleRegistrationDocument updateFoodBoxSize(String profileId, String userId, FoodCartUpdateRequest foodCartUpdateRequest) {
        riderProfileRepository.findById(profileId).orElseThrow(() -> new DataNotFoundException("Record not found for id " + profileId));
        RiderVehicleRegistrationDocument foodCardDocument = vehicleRegistrationRepository.findByRiderProfileId(profileId)
                .orElseThrow(() -> new DataNotFoundException("Record not found for id " + profileId));
        foodCardDocument.setFoodBoxSize(foodCartUpdateRequest.getFoodBoxSize());
        foodCardDocument.setUpdatedBy(userId);
        log.info("Updating food box size for rider {} {}",profileId,foodCartUpdateRequest.getFoodBoxSize());
        locationServiceFeignClient.updateRiderFoodBoxSize(
                RiderFoodBoxSize.builder()
                        .riderId(profileId)
                        .foodBoxSize(foodCartUpdateRequest.getFoodBoxSize().toString())
                        .build()
        );
        log.info("Updated food box size for rider {} {}",profileId,foodCartUpdateRequest.getFoodBoxSize());
        return vehicleRegistrationRepository.save(foodCardDocument);
    }

    private void sendDocumentRejectionEvent(RiderProfile riderProfile, MandatoryCheckStatus existingStatus, MandatoryCheckStatus newStatus, String messageKey ,String reason, String comment) {
        if(Constants.OTHER.equalsIgnoreCase(reason) || Constants.OTHER_IN_THAI.equalsIgnoreCase(reason)) {
            reason = StringUtils.isNotBlank(comment) ? comment : StringUtils.EMPTY;
        } else {
            reason = StringUtils.isNotBlank(comment) ? (reason + " " + comment) : reason;
        }
        if(Objects.nonNull(existingStatus) && !existingStatus.equals(newStatus) && MandatoryCheckStatus.REJECTED.equals(newStatus)) {
            String message = MessageFormat.format(propertyUtils.getProperty(messageKey, Locale.forLanguageTag(Constants.THAI)), reason);
            log.info("Document rejected for riderId {}, Publishing sms event.", riderProfile.getId());
            smsPublisher.sendSmsNotificationEvent(riderProfile, message);
        }
    }
}
