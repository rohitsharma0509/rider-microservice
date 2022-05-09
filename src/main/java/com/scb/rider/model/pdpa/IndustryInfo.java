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
public class IndustryInfo {

	private String industryCode;
	private String industryName;
	private String industryDisplay;
	
	private List<PartnerInfo> partnerInfo;
}
