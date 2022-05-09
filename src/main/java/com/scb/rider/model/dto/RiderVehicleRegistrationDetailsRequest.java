package com.scb.rider.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.scb.rider.model.enumeration.FoodBoxSize;
import com.scb.rider.model.enumeration.MandatoryCheckStatus;
import com.scb.rider.validator.Conditional;
import lombok.*;

import javax.validation.constraints.Size;
import java.time.LocalDate;


@Getter
@Setter
@ToString
@Builder
@Conditional(selected = "status", values = {"REJECTED"}, required = {"vehicleRejectionReason"})
@Conditional(selected = "foodCardStatus", values = {"REJECTED"}, required = {"foodCardRejectionReason"})
public class RiderVehicleRegistrationDetailsRequest {
    @NonNull
    @Size(max = 40, message = "{api.rider.profile.length.msg}")
    private String registrationNo;
    @Size(max = 40, message = "{api.rider.profile.length.msg}")
    private String registrationCardId;
    private LocalDate registrationDate;
    private LocalDate expiryDate;
    @Size(max = 40, message = "{api.rider.profile.length.msg}")
    private String makerModel;
    @Size(max = 40, message = "{api.rider.profile.length.msg}")
    private String province;
    @Size(max = 100, message = "{api.rider.profile.length.msg}")
    private String uploadedVehicleDocUrl;
    private MandatoryCheckStatus status;
    private String uploadedFoodCardUrl;
    private MandatoryCheckStatus foodCardStatus;
    private String vehicleRejectionReason;
    private String vehicleRejectionComment;
    private String foodCardRejectionReason;
    private String foodCardRejectionComment;
    private FoodBoxSize foodBoxSize;
    @JsonIgnore
    private String updatedBy;
}
