package com.scb.rider.model.document;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.scb.rider.model.enumeration.TrainingType;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.scb.rider.model.BaseEntity;
import com.scb.rider.model.enumeration.RiderTrainingStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nonapi.io.github.classgraph.json.Id;

@Getter
@Setter
@Builder
@Document(collection = "riderTrainingAppointment")
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndex(name = "riderId_trainingType", def = "{ 'riderId': 1, 'trainingType': 1 }", unique = true)
public class RiderSelectedTrainingAppointment extends BaseEntity implements Serializable{

	@Id
	private String id;

	private String riderId;

	private TrainingType trainingType;
	
	@NotBlank
	private String appointmentId;
	
	private String venue;
	@JsonFormat(pattern="yyyy-MM-dd", timezone = JsonFormat.DEFAULT_LOCALE)
	private LocalDate date;		
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="HH:mm",timezone = JsonFormat.DEFAULT_LOCALE)
	private LocalTime startTime;
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="HH:mm",timezone = JsonFormat.DEFAULT_LOCALE)
	private LocalTime endTime;
	private RiderTrainingStatus status;
	@JsonFormat(pattern="yyyy-MM-dd", timezone = JsonFormat.DEFAULT_LOCALE)
	private LocalDate completionDate;		
	@JsonIgnore
	private String updatedBy;
}
