package com.scb.rider.model.pdpa;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ConsentUpdateRequest {

	private String channelCode ;
	private String customerId;
	private String referenceType ;
	private String documentId ;
	private String dateOfBirth;
	private List<ConsentCollectionInfo> consentCollectionInfo;
	private String callBackURL; 
	
}
