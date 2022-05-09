package com.scb.rider.model.dto.training;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.scb.rider.model.enumeration.RiderTrainingStatus;

import com.scb.rider.model.enumeration.TrainingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class RiderTrainingAppointmentStatusResponse {

	private String riderId; 
	private TrainingType trainingType;
	private RiderTrainingStatus status;

	public static RiderTrainingAppointmentStatusResponse of(RiderTrainingAppointmentStatusResponse appointmentStatus) {
		return RiderTrainingAppointmentStatusResponse.builder()
				.riderId(appointmentStatus.getRiderId())
				.trainingType(appointmentStatus.getTrainingType())
				.status(appointmentStatus.getStatus()).build();
	}
}
