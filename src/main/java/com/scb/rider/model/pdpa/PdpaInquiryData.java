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
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PdpaInquiryData {
    private String customerId;
    private String referenceType;
    private String channelCode;
    private String channelName;
    private String documentId;
    private List<InquiryConsentInfo> consentInfo;
}
