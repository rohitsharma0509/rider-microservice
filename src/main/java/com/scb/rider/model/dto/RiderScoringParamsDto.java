package com.scb.rider.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RiderScoringParamsDto {
    private String id;
    private Boolean evBikeUser;
    private Boolean rentingToday;
    private String preferredZone;
}
