package com.scb.rider.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.scb.rider.model.enumeration.AvailabilityStatus;
import com.scb.rider.model.enumeration.BackgroundVerificationAttemptStatus;
import com.scb.rider.model.enumeration.RiderProfileStage;
import com.scb.rider.model.enumeration.RiderStatus;
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
@Api(value = "RiderProfileUpdate")
@ApiModel(value = "RiderProfileUpdate")
@NoArgsConstructor
@AllArgsConstructor
public class RiderProfileUpdateRequestDto {
    @NotBlank(message = "{api.rider.profile.blank.msg}")
    private String id;
    @Size(max = 50, message = "{api.rider.profile.length.msg}")
    private String firstName;
    @Size(max = 50, message = "{api.rider.profile.length.msg}")
    private String lastName;
    @Valid
    private AddressDto address;
    @Valid
    private NationalAddressDto nationalAddress;
    @Size(max = 10, message = "{api.rider.profile.length.msg}")
    private String dob;
    @Size(min = 8, max = 12, message = "{api.rider.profile.length.constraints.msg}")
    private String phoneNumber;
    @Size(max = 40, message = "{api.rider.profile.length.msg}")
    private String accountNumber;
    private AvailabilityStatus availabilityStatus;
    private RiderProfileStage profileStage;
    private BackgroundVerificationAttemptStatus attemptBGVStatus;
    private Boolean consentAcceptFlag;
    private boolean dataSharedFlag;

    private Boolean evBikeUser;
    private Boolean rentingToday;
    @JsonIgnore
    private String updatedBy;
}
