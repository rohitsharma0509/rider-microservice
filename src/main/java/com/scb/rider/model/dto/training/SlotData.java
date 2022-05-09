package com.scb.rider.model.dto.training;

import com.scb.rider.model.enumeration.RiderTrainingStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SlotData {
	private String slotId;
	private boolean occupied;
}