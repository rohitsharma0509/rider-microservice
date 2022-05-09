package com.scb.rider.model.pdpa;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ValidateResponse {

	private String channelCode;
	private String customerId;
	private String referenceType;
	private String documentId;
	private String dateOfBirth;
	
}
