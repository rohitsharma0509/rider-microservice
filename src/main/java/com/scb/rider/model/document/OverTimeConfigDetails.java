package com.scb.rider.model.document;

import com.scb.rider.model.enumeration.IncentiveStatus;
import com.scb.rider.model.enumeration.IncentiveType;
import com.scb.rider.model.enumeration.TimeRangeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OverTimeConfigDetails {

    private String id;
    private Integer zone;
    private String zoneName;
    private TimeRangeType timeRange;
    private Integer noOfCompletedJob;
    private Double kmCovered;
    private Double earnings;
    private Double incentiveAmount;
    private IncentiveStatus status;
    private IncentiveType incentiveType;
}
