package com.scb.rider.model.pdpa;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PdpaValidateData {

	private String dateOfBirth;
	private String customerId;
	private String channelCode;
	private String referenceType;
	private String documentId;
	private List<ValidateConsentInfo> consentInfo;
}
