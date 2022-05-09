package com.scb.rider.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.scb.rider.model.enumeration.RiderStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RiderStatusDto {
	@Size(max = 40, message = "{api.rider.profile.length.msg}")
	@NotBlank(message = "{api.rider.profile.blank.msg}")
	private String profileId;
	@NotNull(message = "{api.rider.profile.null.msg}")
	private RiderStatus status;
	private LocalDateTime modifiedDate;
	private String remarks; 
	@Size(max = 1000, message = "{api.rider.profile.length.msg}")
	private String reason;
	private LocalDateTime suspensionExpiryTime;
	private Integer suspensionDuration;
	private String riderCaseNo;
	@JsonIgnore
	private String updatedBy;
}