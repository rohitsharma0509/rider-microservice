package com.scb.rider.model.dto;

import com.scb.rider.constants.DocumentType;
import com.scb.rider.model.enumeration.MandatoryCheckStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@ToString
@Builder
public class RiderVehicleStatusRequest {

    private MandatoryCheckStatus status;
    @NotNull(message = "{api.rider.profile.null.msg}")
    private DocumentType documentType;
    private String vehicleRejectionReason;
    private String vehicleRejectionComment;
    private String foodCardRejectionReason;
    private String foodCardRejectionComment;


}
