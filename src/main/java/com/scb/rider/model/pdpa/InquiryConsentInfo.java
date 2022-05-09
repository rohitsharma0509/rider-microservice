package com.scb.rider.model.pdpa;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InquiryConsentInfo {

    private String consentType;
    private Double consentVersion;
    private String consentName;
    private String dateOfBirth;
    private Boolean consentStatus;
    private String acceptLanguage;
    private String revisitDate;
    private String callbackURL;
    private String consentHeader;
    private String consentBody;
    private Boolean consentActive;
    private Boolean toggle;
    private String startDate;
    private String endDate;
    private String createdDate;
    private String updatedDate;
	private Content content; 
	private List<IndustryInfo> industryInfo;
	private List<ObjectiveInfo> objectiveInfo;
}
