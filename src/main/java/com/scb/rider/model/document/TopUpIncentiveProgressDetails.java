package com.scb.rider.model.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TopUpIncentiveProgressDetails {

    private LocalTime fromTime;
    private LocalTime toTime;
    private String zoneName;
    private Integer zoneId;
    private Double incentiveAmount;
    private Integer noOfJobs;
}
