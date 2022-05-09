package com.scb.rider.model.dto;

import com.scb.rider.model.enumeration.MandatoryCheckStatus;
import com.scb.rider.validator.Conditional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Size;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Conditional(selected = "status", values = {"REJECTED"}, required = {"reason"})
public class RiderFoodCardRequest {
    private MandatoryCheckStatus status;
    @Size(max = 100, message = "{api.rider.profile.length.msg}")
    private String documentUrl;
    private String reason;
    private String comment;
}
