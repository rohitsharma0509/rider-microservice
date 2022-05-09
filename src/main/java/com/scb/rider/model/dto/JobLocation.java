package com.scb.rider.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class JobLocation {
	private int seq;
	private String type;
	private String addressId;
	private String addressName;
	private String address;
	private String lat;
	private String lng;
	private String contactName;
	private String contactPhone;
	private String actualArriveTime;
	private String mail;
	private String subDistrict;
}
