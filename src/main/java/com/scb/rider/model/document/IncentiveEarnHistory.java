package com.scb.rider.model.document;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IncentiveEarnHistory {

    private String id;

    private String jobId;

    private String riderId;

    private String driverId;

    private Double topUpEarnAmount;

    private Double overTimeEarnAmount;

    private List<OverTimeConfigDetails> overTimeConfig;

    private TopUpConfigDetails topUpConfig;

}
