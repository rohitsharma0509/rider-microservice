package com.scb.rider.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.scb.rider.model.enumeration.MandatoryCheckStatus;
import com.scb.rider.validator.Conditional;
import lombok.*;

import javax.validation.constraints.Size;
import java.time.LocalDate;

@Getter
@Setter
@ToString
@Builder
@Conditional(selected = "status", values = {"REJECTED"}, required = {"reason"})
public class RiderDrivingLicenseRequest {
    @NonNull
    private String drivingLicenseNumber;

    private LocalDate dateOfIssue;
    @NonNull
    private LocalDate dateOfExpiry;
    @Size(max = 40, message = "{api.rider.profile.length.msg}")
    private String typeOfLicense;
    @NonNull
    @Size(max = 100, message = "{api.rider.profile.length.msg}")
    private String documentUrl;

    private String reason;

    private String comment;

    private MandatoryCheckStatus status;

    @JsonIgnore
    private String updateBy;

}
