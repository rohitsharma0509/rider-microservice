package com.scb.rider.model.dto.training;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.scb.rider.constants.Constants;
import com.scb.rider.model.enumeration.RiderTrainingStatus;

import com.scb.rider.model.enumeration.TrainingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Size;


@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiderTrainingStatusUpdateDto {

	@Size(max = 40, message = "{api.rider.profile.length.msg}")
	private String riderId; 
	private RiderTrainingStatus status;
	private TrainingType trainingType;
	@JsonFormat(pattern= Constants.DATE_FORMAT, timezone = JsonFormat.DEFAULT_LOCALE)
	private LocalDate completionDate;
	
}
