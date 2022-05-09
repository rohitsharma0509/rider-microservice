package com.scb.rider.model.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class RiderExcessiveWaitDetailsDto {
	private String riderId;
	private String jobId;
	private double topUpAmount;
	private LocalDateTime topupDateTime;

	

}
