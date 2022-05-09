package com.scb.rider.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RiderFireBaseNotificationDetailsDto {
	
	private String deviceToken;
	private String newPreferredZoneId;
	private String previousPreferredZoneId;
}
