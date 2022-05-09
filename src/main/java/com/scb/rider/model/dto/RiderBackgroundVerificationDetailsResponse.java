package com.scb.rider.model.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.scb.rider.model.document.RiderBackgroundVerificationDocument;
import com.scb.rider.model.enumeration.MandatoryCheckStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiderBackgroundVerificationDetailsResponse {
	private String id;
	private String riderProfileId;
	private MandatoryCheckStatus status;
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate dueDate;
	private String reason;
	private String documentUrl;
	private List<String> documentUrls;
    private String comment;
	private LocalDateTime rejectionTime;

	public static RiderBackgroundVerificationDetailsResponse of(RiderBackgroundVerificationDocument document) {
		List<String> docUrls = !CollectionUtils.isEmpty(document.getDocumentUrls()) ? document.getDocumentUrls()
				: (StringUtils.isNotBlank(document.getDocumentUrl()) ? Arrays.asList(document.getDocumentUrl()) : null);
		return RiderBackgroundVerificationDetailsResponse.builder().id(document.getId())
				.riderProfileId(document.getRiderProfileId()).status(document.getStatus())
				.dueDate(document.getDueDate()).reason(document.getReason())
				.documentUrl(document.getDocumentUrl()).documentUrls(docUrls)
				.comment(document.getComment())
				.rejectionTime(document.getRejectionTime())
				.build();
	}
}
