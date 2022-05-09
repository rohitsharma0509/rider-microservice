package com.scb.rider.model.pdpa;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PartnerInfo {

	private String partnerCode;
	private String partnerName;
	private String partnerDisplay;
	private String partnerType;
	private String partnerEffectiveDate;
}
