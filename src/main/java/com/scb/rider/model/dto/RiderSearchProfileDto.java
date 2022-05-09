package com.scb.rider.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.enumeration.RiderStatus;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Api(value = "RiderProfile")
@ApiModel(value = "RiderProfile")
@SuppressWarnings("squid:S3776")
public class RiderSearchProfileDto {

  @ApiModelProperty(notes = "It is used for searching rider.", name = "riderId")
  private String riderId;
  
  private String id;

  @JsonIgnore
  private String firstName;

  @JsonIgnore
  private String lastName;

  @JsonProperty("name")
  private String Name;

  private String phoneNumber;

  @JsonProperty("status")
  private RiderStatus status;

  private Boolean isReadyForAuthorization;

  private String tierName;
  private int tierId;
  private String approvalDateTime;

  private String preferredZoneName;
  private String  enrollmentDate;

  public static List<RiderSearchProfileDto> of(List<RiderProfile> riderProfiles) {
    return riderProfiles.stream().map(riderProfile -> {
      RiderSearchProfileDto riderProfileDto = RiderSearchProfileDto.builder().build();
      BeanUtils.copyProperties(riderProfile, riderProfileDto);
      String fullName = !StringUtils.isEmpty(riderProfile.getLastName())
          ? String.format("%s %s", riderProfile.getFirstName(), riderProfile.getLastName())
          : riderProfile.getFirstName();
      if(ObjectUtils.isEmpty(riderProfile.getStatus())) {
        riderProfileDto.setStatus(RiderStatus.UNAUTHORIZED);
      }
      riderProfileDto.setName(fullName);
      riderProfileDto.setId(riderProfileDto.getId());
      if(StringUtils.isNotBlank(riderProfile.getIsReadyForAuthorization())) {
        riderProfileDto.setIsReadyForAuthorization(Boolean.parseBoolean(riderProfile.getIsReadyForAuthorization()));
      } else {
        riderProfileDto.setIsReadyForAuthorization(Boolean.FALSE);
      }
      riderProfileDto.setTierName(riderProfile.getTierName());
      riderProfileDto.setTierId(riderProfile.getTierId());

      if(ObjectUtils.isNotEmpty(riderProfile.getRiderPreferredZones())){
        if(StringUtils.isNotBlank(riderProfile.getRiderPreferredZones().getPreferredZoneName()))
          riderProfileDto.setPreferredZoneName(riderProfile.getRiderPreferredZones().getPreferredZoneName());
      }
      
      if(!Objects.isNull(riderProfile.getRiderDocumentUpload())) {
        String enrollmentDate = !Objects.isNull(riderProfile.getRiderDocumentUpload().getEnrollmentDate()) ? 
            riderProfile.getRiderDocumentUpload().getEnrollmentDate() : "";
         riderProfileDto.setEnrollmentDate(enrollmentDate);
      }
      return riderProfileDto;
    }).collect(Collectors.toList());
  }
}
