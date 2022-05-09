package com.scb.rider.model.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
public class JobSettlementDetails {

    private String jobId;
    private String orderId;
    private BigDecimal normalPrice;
    private BigDecimal netPrice;
    private BigDecimal netPaymentPrice;
    private BigDecimal taxAmount;
    private String jobType;
    private ExcessiveWaitingTimeDetailsEntity excessiveWaitTimeDetailsEntity;
}
