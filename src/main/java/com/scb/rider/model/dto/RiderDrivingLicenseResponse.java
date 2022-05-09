package com.scb.rider.model.dto;

import com.scb.rider.model.document.RiderDrivingLicenseDocument;
import com.scb.rider.model.enumeration.MandatoryCheckStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Builder
public class RiderDrivingLicenseResponse {

    private String id;

    private String drivingLicenseNumber;

    private LocalDate dateOfIssue;

    private LocalDate dateOfExpiry;

    private String typeOfLicense;

    private String riderProfileId;

    private String documentUrl;

    private String reason;

    private String comment;

    private LocalDateTime rejectionTime;

    private MandatoryCheckStatus status;

    public static RiderDrivingLicenseResponse of(RiderDrivingLicenseDocument licenseDocument) {

        return RiderDrivingLicenseResponse.builder()
                .id(licenseDocument.getId())
                .drivingLicenseNumber(licenseDocument.getDrivingLicenseNumber())
                .dateOfExpiry(licenseDocument.getDateOfExpiry())
                .dateOfIssue(licenseDocument.getDateOfIssue())
                .typeOfLicense(licenseDocument.getTypeOfLicense())
                .documentUrl(licenseDocument.getDocumentUrl())
                .status(licenseDocument.getStatus())
                .reason(licenseDocument.getReason())
                .comment(licenseDocument.getComment())
                .rejectionTime(licenseDocument.getRejectionTime())
                .riderProfileId(licenseDocument.getRiderProfileId()).build();
    }
}
