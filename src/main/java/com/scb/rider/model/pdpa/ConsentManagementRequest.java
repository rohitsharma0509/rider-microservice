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
public class ConsentManagementRequest {
	private String channelCode ;
	private String customerId;
	private List<ConsentCollectionInfo> consentCollectionInfo;

}
