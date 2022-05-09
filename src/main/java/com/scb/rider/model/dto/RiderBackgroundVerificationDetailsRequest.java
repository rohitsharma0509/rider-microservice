package com.scb.rider.model.dto;

import java.time.LocalDate;
import java.util.List;


import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Conditional(selected = "status", values = {"REJECTED"}, required = {"reason", "dueDate"})
public class RiderBackgroundVerificationDetailsRequest {
	private MandatoryCheckStatus status;
	private LocalDate dueDate;
	@Size(max = 80, message = "{api.rider.profile.length.msg}")
	private String reason;
	@Size(max = 100, message = "{api.rider.profile.length.msg}")
	private String documentUrl;
	private List<String> documentUrls;
	@Size(max = 500, message = "{api.rider.profile.length.msg}")
	private String comment;
	@JsonIgnore
	private String updatedBy;
}
