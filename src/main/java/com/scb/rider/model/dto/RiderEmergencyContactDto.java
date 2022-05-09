package com.scb.rider.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.scb.rider.model.document.RiderEmergencyContact;
import com.scb.rider.model.enumeration.Relationship;
import com.scb.rider.validator.EnumValidator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;


@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Api(value = "EmergencyContact")
@ApiModel(value = "EmergencyContact")
public class RiderEmergencyContactDto {

    @ApiModelProperty(notes = "It is required only in case of update.", name="id")
    //@Size(min = 50, message = "{api.rider.profile.length.msg}")
    @NotBlank(message = "{api.rider.profile.blank.msg}")
    private String profileId;
    @Size(max = 50, message = "{api.rider.profile.length.msg}")
    @NotBlank(message = "{api.rider.profile.blank.msg}")
    private String name;
    @Size(min = 8, max = 12, message = "{api.rider.profile.length.constraints.msg}")
    @NotBlank(message = "{api.rider.profile.blank.msg}")
    private String mobilePhoneNumber;
    private String homePhoneNumber;
    @EnumValidator(targetClassType = Relationship.class, message = "{api.rider.profile.enumNotFound.msg}")
    private String relationship;
    @NotBlank(message = "{api.rider.profile.blank.msg}")
    @Size(max = 300, message = "{api.rider.profile.length.msg}")
    private String address1;
   // @NotBlank(message = "{api.rider.profile.blank.msg}")
    private String address2;
    @Size(max = 20, message = "{api.rider.profile.length.msg}")
    private String district;
    @Size(max = 20, message = "{api.rider.profile.length.msg}")
    private String subDistrict;
    @Size(max = 30, message = "{api.rider.profile.length.msg}")
    private String province;
    @Size(max = 10, message = "{api.rider.profile.length.msg}")
    private String zipCode;

    public static RiderEmergencyContactDto of(RiderEmergencyContact emergencyContact) {
        RiderEmergencyContactDto emergencyContactDto = RiderEmergencyContactDto.builder().build();
        BeanUtils.copyProperties(emergencyContact, emergencyContactDto);
        return emergencyContactDto;
    }

}
