package com.scb.rider.model.dto;

import java.time.LocalDateTime;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.springframework.data.annotation.Id;

import com.scb.rider.model.enumeration.MandatoryCheckStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RiderEVFormDto {

	@Id
	private String id;

	@NotBlank(message = "{api.rider.profile.blank.msg}")
	private String riderProfileId;

	@NotBlank(message = "{api.rider.profile.blank.msg}")
	private String evRentalAgreementNumber;

	private MandatoryCheckStatus status;

	private String province;

	@Size(max = 100, message = "{api.rider.profile.length.msg}")
	private String documentUrl;

	private String reason;

	private String comment;
	
	private LocalDateTime rejectionTime;

}
