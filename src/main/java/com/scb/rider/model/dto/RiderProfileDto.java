package com.scb.rider.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.scb.rider.model.document.NationalIdDetails;
import com.scb.rider.model.document.RiderPreferredZones;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.enumeration.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Data
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Api(value = "RiderProfile")
@ApiModel(value = "RiderProfile")
public class RiderProfileDto {

    @ApiModelProperty(notes = "It is required only in case of update.",name="id")
    private String id;
    @Size(max = 50, message = "{api.rider.profile.length.msg}")
    @NotBlank(message = "{api.rider.profile.blank.msg}")
    private String firstName;
    @Size(max = 50, message = "{api.rider.profile.length.msg}")
    @NotBlank(message = "{api.rider.profile.blank.msg}")
    private String lastName;
    @Valid
    private AddressDto address;
    @Valid
    private NationalAddressDto nationalAddress;
    @NotBlank(message = "{api.rider.profile.blank.msg}")
    @Size(max = 10, message = "{api.rider.profile.length.msg}")
    private String dob;
    @Size(max = 6, message = "{api.rider.profile.length.msg}")
    private String gender;
    @NotBlank(message = "{api.rider.profile.blank.msg}")
    @Size(max = 40, message = "{api.rider.profile.length.msg}")
    private String nationalID;
    @NotBlank(message = "{api.rider.profile.blank.msg}")
    @Size(max = 40, message = "{api.rider.profile.length.msg}")
    private String accountNumber;
    private String countryCode;
    @Size(min = 8, max = 12, message = "{api.rider.profile.length.constraints.msg}")
    @NotBlank(message = "{api.rider.profile.blank.msg}")
    private String phoneNumber;
    private RiderStatus status;
    private MandatoryCheckStatus nationalIdStatus;
    private AvailabilityStatus availabilityStatus;
    private MandatoryCheckStatus profilePhotoStatus;
    private RiderProfileStage profileStage;
    private LocalDateTime latestStatusModifiedDate;
    private BackgroundVerificationAttemptStatus attemptBGVStatus;
    private String remarks; 
    @Size(max = 40, message = "{api.rider.profile.length.msg}")
    private String reason;
    private ZonedDateTime suspensionExpiryTime;
    private Integer suspensionDuration;
    @Size(max = 100, message = "{api.rider.profile.length.msg}")
    private String profilePhotoUrl;
    private boolean consentAcceptFlag;
    private boolean dataSharedFlag;
    @Size(max = 40, message = "{api.rider.profile.length.msg}")
    private String riderId;
    private LocalDateTime createdDate;
    @Size(max = 100, message = "{api.rider.profile.length.msg}")
    private String profilePhotoExternalUrl;
    private String profilePhotoRejectionReason;
    private String profilePhotoRejectionComment;
    private LocalDateTime profilePhotoRejectionTime;
    private NationalIdDetails nationalIdDetails;
    private String isReadyForAuthorization;
    private String tierName;
    private int tierId;
    private Boolean evBikeUser;
    private Boolean rentingToday;
    private RiderPreferredZones riderPreferredZones;
    private String approvalDateTime;
    private String  enrollmentDate;


    public static RiderProfileDto of(RiderProfile riderProfile) {
        RiderProfileDto riderProfileDto = RiderProfileDto.builder().build();
        AddressDto addressDto = AddressDto.builder().build();
        NationalAddressDto nationalAddressDto = NationalAddressDto.builder().build();
        riderProfileDto.setNationalAddress(nationalAddressDto);
        riderProfileDto.setAddress(addressDto);
        BeanUtils.copyProperties(riderProfile, riderProfileDto);
        if(ObjectUtils.isNotEmpty(riderProfile.getAddress())) {
            BeanUtils.copyProperties(riderProfile.getAddress(), riderProfileDto.getAddress());
        }

        if(ObjectUtils.isNotEmpty(riderProfile.getNationalAddress())) {
            BeanUtils.copyProperties(riderProfile.getNationalAddress(), riderProfileDto.getNationalAddress());
        }

        riderProfileDto.setSuspensionExpiryTime(Objects.nonNull(riderProfile.getSuspensionExpiryTime()) ? ZonedDateTime.of(riderProfile.getSuspensionExpiryTime(), ZoneOffset.UTC) : null);
        riderProfileDto.setEnrollmentDate(Objects.nonNull(riderProfile.getRiderDocumentUpload()) ? riderProfile.getRiderDocumentUpload().getEnrollmentDate() : "");
        return riderProfileDto;
    }

    public static List<RiderProfileDto> of(List<RiderProfile> riderProfiles) {
        List<RiderProfileDto> profiles = new ArrayList<>();
        for(RiderProfile riderProfile : riderProfiles){
            profiles.add(of(riderProfile));
        }
        return profiles;
    }

	
}
