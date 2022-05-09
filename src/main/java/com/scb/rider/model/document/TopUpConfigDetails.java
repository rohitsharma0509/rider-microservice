package com.scb.rider.model.document;

import com.scb.rider.model.enumeration.IncentiveStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TopUpConfigDetails {

    private String id;
    private Integer zone;
    private String zoneName;
    private LocalDate date;
    private LocalTime fromTime;
    private LocalTime toTime;
    private String jobType;
    private Double incentiveAmount;
    private IncentiveStatus status;
}
