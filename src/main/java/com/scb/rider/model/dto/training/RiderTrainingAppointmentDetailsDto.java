package com.scb.rider.model.dto.training;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.scb.rider.model.enumeration.TrainingType;
import org.springframework.beans.BeanUtils;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.scb.rider.model.document.RiderSelectedTrainingAppointment;
import com.scb.rider.model.enumeration.RiderTrainingStatus;
import com.scb.rider.constants.Constants;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.util.CollectionUtils;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Api(value = "TrainingAppointment")
@ApiModel(value = "TrainingAppointment")
public class RiderTrainingAppointmentDetailsDto {
	
	@NotBlank(message = "{api.rider.profile.blank.msg}")
	private String riderId;
	private TrainingType trainingType;
	private String appointmentId;
	private String venue;
	@JsonFormat(pattern= Constants.DATE_FORMAT, timezone = JsonFormat.DEFAULT_LOCALE)
	private LocalDate date;
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="HH:mm",timezone = JsonFormat.DEFAULT_LOCALE)
	private LocalTime startTime;
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="HH:mm",timezone = JsonFormat.DEFAULT_LOCALE)
	private LocalTime endTime;
	private RiderTrainingStatus status;
	@JsonFormat(pattern= Constants.DATE_FORMAT, timezone = JsonFormat.DEFAULT_LOCALE)
	private LocalDate completionDate;
	@JsonIgnore
	private String updatedBy;

	public static List<RiderTrainingAppointmentDetailsDto> of(List<RiderSelectedTrainingAppointment> appointments) {
		List<RiderTrainingAppointmentDetailsDto> appointmentDtos = new ArrayList<>();
		if(!CollectionUtils.isEmpty(appointments)) {
			for (RiderSelectedTrainingAppointment appointment : appointments) {
				appointmentDtos.add(of(appointment));
			}
		}
		return appointmentDtos;
	}

	public static RiderTrainingAppointmentDetailsDto of(RiderSelectedTrainingAppointment appointment) {
		RiderTrainingAppointmentDetailsDto appointmentDetailsDto = RiderTrainingAppointmentDetailsDto.builder().build();
		BeanUtils.copyProperties(appointment, appointmentDetailsDto);
		return appointmentDetailsDto;
	}
}
