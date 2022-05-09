package com.scb.rider.model.document;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IncentiveProgressDetails {

    private String id;

    private String riderId;

    private Integer zone;

    private String zoneName;

    private List<ProgressDetails> progressDetails;

    private LocalDateTime createdDate;

    private LocalDateTime lastModifiedDate;

}
