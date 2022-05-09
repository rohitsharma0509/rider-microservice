package com.scb.rider.model.document;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.scb.rider.model.enumeration.MandatoryCheckStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Document
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RiderEVForm {

	@Id
	private String id;

	@Indexed(unique = true)
	private String riderProfileId;

	private String evRentalAgreementNumber;

	private MandatoryCheckStatus status;

	private String province;

	private String documentUrl;

	private String reason;

	private String comment;
	
	private LocalDateTime rejectionTime;

}
