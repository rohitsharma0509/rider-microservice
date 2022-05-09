package com.scb.rider.model.pdpa;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ValidateConsentInfo {

	private String consentType;
	private Double consentVersion;
	private String consentName;
	private String startDate;
	private String endDate;
	private String header;
	private String body;
	private Content content;
	private List<IndustryInfo> industryInfo;
	private List<ObjectiveInfo> objectiveInfo;

}
