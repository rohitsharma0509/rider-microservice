package com.scb.rider.model.document;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.scb.rider.model.BaseEntity;
import com.scb.rider.model.enumeration.MandatoryCheckStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@Document
public class RiderDrivingLicenseDocument extends BaseEntity {

    @Id
    private String id;
    private String drivingLicenseNumber;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfIssue;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfExpiry;

    private String typeOfLicense;

    @Indexed(unique=true)
    private String riderProfileId;

    private String documentUrl;

    private String reason;

    private String comment;

    private LocalDateTime rejectionTime;

    private MandatoryCheckStatus status;

    private String updatedBy;
}
