package com.scb.rider.model.pdpa;

import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class PdpaValidateDto {
	@NotBlank
	private String riderId;
	@NotBlank 
	private String dateOfBirth;
	
	private String documentId;
}
