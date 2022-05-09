package com.scb.rider.model.pdpa;

import java.util.List;

import javax.validation.Valid;
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
@ToString
@Builder
public class ConsentUpdateDto {

	@NotBlank
	private String riderId;

	@NotBlank
	private String dateOfBirth;
	
	private String documentId;
	
	@Valid
	private List<ConsentCollectionInfo> consentCollectionInfo;
	
}
