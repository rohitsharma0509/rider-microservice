package com.scb.rider.model.dto;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.scb.rider.model.enumeration.TrainingType;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.scb.rider.model.document.RiderBackgroundVerificationDocument;
import com.scb.rider.model.document.RiderCovidSelfie;
import com.scb.rider.model.document.RiderDeviceDetails;
import com.scb.rider.model.document.RiderDrivingLicenseDocument;
import com.scb.rider.model.document.RiderEVForm;
import com.scb.rider.model.document.RiderEmergencyContact;
import com.scb.rider.model.document.RiderFoodCard;
import com.scb.rider.model.document.RiderPreferredZones;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.document.RiderSelectedTrainingAppointment;
import com.scb.rider.model.document.RiderUploadedDocument;
import com.scb.rider.model.document.RiderVehicleRegistrationDocument;
import com.scb.rider.model.dto.training.RiderTrainingAppointmentDetailsDto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.util.CollectionUtils;


@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class RiderDetailsDto {
    private RiderProfileDto riderProfileDto;
    private RiderEmergencyContactDto riderEmergencyContactDto;
    private RiderDrivingLicenseResponse riderDrivingLicenseResponse;
    private RiderVehicleRegistrationDetailsResponse riderVehicleRegistrationDetailsResponse;
    private RiderPreferredZoneDto riderPreferredZoneDto;
    private RiderTrainingAppointmentDetailsDto appointmentDetailsDto;
    private List<RiderTrainingAppointmentDetailsDto> appointmentList;
    private RiderEVFormDto riderEvFormDto;
    private RiderBackgroundVerificationDetailsResponse riderBackgroundVerificationDetailsResponse;
    private RiderFoodCardResponse riderFoodCardResponse;
    private RiderDeviceDetails riderDeviceDetails;
    private LocalDateTime lastUploadedCovidSelfieTime;
    private Integer mannerScoreCurrent;
    private Integer mannerScorePossibleMax;

    public static RiderDetailsDto of(RiderProfile riderProfile, Optional<RiderEmergencyContact> emergencyContact
            , Optional<RiderDrivingLicenseDocument> drivingLicenseDocument
            , Optional<RiderEVForm> riderEvForm
            , Optional<RiderVehicleRegistrationDocument> vehicleRegistrationDocument
            , Optional<RiderPreferredZones> riderPreferredZones, Optional<RiderUploadedDocument> uploadedDocument
            , List<RiderSelectedTrainingAppointment> appointments
            , Optional<RiderBackgroundVerificationDocument> riderBackgroundVerificationDocument
            ,Optional<RiderFoodCard> riderFoodCard, Optional<RiderDeviceDetails> riderDeviceDetails, Optional<RiderCovidSelfie> riderCovidSelfie
            ,Integer mannerScorePossibleMax) {
        RiderProfileDto riderProfileDto = RiderProfileDto.builder().build();
        riderProfileDto.setAddress(AddressDto.builder().build());
        riderProfileDto.setNationalAddress(NationalAddressDto.builder().build());
        RiderDetailsDto riderDetailsDto = RiderDetailsDto.builder().build();
        riderDetailsDto.setRiderProfileDto(riderProfileDto);
        riderDetailsDto.setRiderEmergencyContactDto(RiderEmergencyContactDto.builder().build());
        riderDetailsDto.setRiderDrivingLicenseResponse(RiderDrivingLicenseResponse.builder().build());
        riderDetailsDto.setRiderEvFormDto(RiderEVFormDto.builder().build());
        riderDetailsDto.setRiderVehicleRegistrationDetailsResponse(RiderVehicleRegistrationDetailsResponse.builder().build());
        riderDetailsDto.setRiderPreferredZoneDto(RiderPreferredZoneDto.builder().build());
        riderDetailsDto.setAppointmentDetailsDto(RiderTrainingAppointmentDetailsDto.builder().build());
        riderDetailsDto.setRiderBackgroundVerificationDetailsResponse(RiderBackgroundVerificationDetailsResponse.builder().build());
        riderDetailsDto.setRiderFoodCardResponse(RiderFoodCardResponse.builder().build());
        BeanUtils.copyProperties(riderProfile, riderProfileDto);
        riderProfileDto.setSuspensionExpiryTime(Objects.nonNull(riderProfile.getSuspensionExpiryTime()) ? ZonedDateTime.of(riderProfile.getSuspensionExpiryTime(), ZoneOffset.UTC) : null);
        riderProfileDto.setEnrollmentDate(Objects.nonNull(riderProfile.getRiderDocumentUpload()) ? riderProfile.getRiderDocumentUpload().getEnrollmentDate() : "");
        if(ObjectUtils.isNotEmpty(riderProfile.getAddress())) {
            BeanUtils.copyProperties(riderProfile.getAddress(), riderProfileDto.getAddress());
        }
        if(ObjectUtils.isNotEmpty(riderProfile.getNationalAddress())) {
            BeanUtils.copyProperties(riderProfile.getNationalAddress(), riderProfileDto.getNationalAddress());
        }

        List<RiderTrainingAppointmentDetailsDto> appointmentList = new ArrayList<>();
        if(!CollectionUtils.isEmpty(appointments)) {
            appointments.stream().forEach(appointment -> {
                RiderTrainingAppointmentDetailsDto appointmentDto = new RiderTrainingAppointmentDetailsDto();
                BeanUtils.copyProperties(appointment, appointmentDto);
                appointmentList.add(appointmentDto);
                if(TrainingType.FOOD.equals(appointment.getTrainingType())) {
                    riderDetailsDto.setAppointmentDetailsDto(appointmentDto);
                }
            });
        }

        riderDetailsDto.setAppointmentList(appointmentList);
        emergencyContact.ifPresent(riderEmergencyContact -> BeanUtils.copyProperties(riderEmergencyContact, riderDetailsDto.getRiderEmergencyContactDto()));
        drivingLicenseDocument.ifPresent(riderDrivingLicenseDocument -> BeanUtils.copyProperties(riderDrivingLicenseDocument, riderDetailsDto.getRiderDrivingLicenseResponse()));
        riderEvForm.ifPresent(evForm -> BeanUtils.copyProperties(evForm, riderDetailsDto.getRiderEvFormDto()));
        vehicleRegistrationDocument.ifPresent(riderVehicleRegistrationDocument -> BeanUtils.copyProperties(riderVehicleRegistrationDocument, riderDetailsDto.getRiderVehicleRegistrationDetailsResponse()));
        riderPreferredZones.ifPresent(preferredZones -> BeanUtils.copyProperties(preferredZones, riderDetailsDto.getRiderPreferredZoneDto()));
        uploadedDocument.ifPresent(riderUploadedDocument -> riderProfileDto.setProfilePhotoUrl(riderUploadedDocument.getImageUrl()));
        uploadedDocument.ifPresent(riderUploadedDocument -> riderProfileDto.setProfilePhotoExternalUrl(riderUploadedDocument.getImageExternalUrl()));
        riderBackgroundVerificationDocument.ifPresent(riderBackgroundDocumentDetails -> BeanUtils.copyProperties(riderBackgroundDocumentDetails, riderDetailsDto.getRiderBackgroundVerificationDetailsResponse()));
        riderFoodCard.ifPresent(riderFoodCardDocument -> BeanUtils.copyProperties(riderFoodCardDocument, riderDetailsDto.getRiderFoodCardResponse()));
        riderDeviceDetails.ifPresent(riderDetailsDto::setRiderDeviceDetails);
        riderDetailsDto.setLastUploadedCovidSelfieTime( riderCovidSelfie.isPresent() ? riderCovidSelfie.get().getUploadedTime(): null);
        riderDetailsDto.setMannerScoreCurrent(riderProfile.getMannerScoreCurrent());
        riderDetailsDto.setMannerScorePossibleMax(mannerScorePossibleMax);
        return riderDetailsDto;
    }

}
