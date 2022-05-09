package com.scb.rider.model.pdpa;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ConsentCollectionInfo {

	@NotBlank
	private String consentType;
	
	@NotNull
	private Double consentVersion;

	@NotNull
	private Boolean consentStatus;
}
