package com.scb.rider.model.dto.training;

import java.time.LocalDate;
import java.time.LocalTime;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.scb.rider.model.enumeration.TrainingType;
import org.springframework.data.annotation.ReadOnlyProperty;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.scb.rider.constants.Constants;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@ToString
@Api(value = "TrainingSlot")
public class TrainingDto {

	@ApiModelProperty(notes = "It is required only in case of update.", name = "id")
	private String id;

	private TrainingType trainingType;

	@JsonFormat(pattern = Constants.DATE_FORMAT)
	@FutureOrPresent
	private LocalDate date;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
	private LocalTime startTime;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
	private LocalTime endTime;

	@NotBlank(message = "{api.rider.profile.blank.msg}")
	@Size(min = 1, max = 600, message = "{api.rider.profile.length.constraints.msg}")
	private String venue;

	@Min(1)
	private int capacity;
	
	@Min(0)
	@ReadOnlyProperty
	private int reserved;

}	