package com.scb.rider.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.scb.rider.model.enumeration.AvailabilityStatus;
import com.scb.rider.model.enumeration.BackgroundVerificationAttemptStatus;
import com.scb.rider.model.enumeration.RiderProfileStage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;


@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Api(value = "NationalAddressUpdate")
@ApiModel(value = "NationalAddressUpdate")
@NoArgsConstructor
@AllArgsConstructor
public class NationalAddressUpdateRequestDto {
    @Valid
    private NationalAddressDto nationalAddress;
}
