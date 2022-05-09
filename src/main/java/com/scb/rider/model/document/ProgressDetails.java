package com.scb.rider.model.document;

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
public class ProgressDetails {
    private IncentiveType incentiveType;
    private TimeRangeType timeRange;
    private Long noOfJobs;
    private Integer configuredJobs;
    private Double noOfKms;
    private Double configuredKms;
    private Double earning;
    private Double configuredEarnings;
}
