package com.scb.rider.model.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class DistanceResponseEntity {
    private Double distance;
    private Double duration;
}
