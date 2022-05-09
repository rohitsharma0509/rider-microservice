package com.scb.rider.model.document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.scb.rider.model.BaseEntity;
import com.scb.rider.model.enumeration.MandatoryCheckStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
@Document
@NoArgsConstructor
@AllArgsConstructor
public class RiderBackgroundVerificationDocument extends BaseEntity {
	@Id
	private String id;
	@Indexed(unique = true)
	private String riderProfileId;
	private MandatoryCheckStatus status;
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate dueDate;
	private String reason;
	private String documentUrl;
	private List<String> documentUrls;
	private String comment;
    private LocalDateTime rejectionTime;
    private String updatedBy;
}
