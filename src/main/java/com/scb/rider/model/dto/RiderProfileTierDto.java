package com.scb.rider.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.scb.rider.model.document.RiderProfile;
import lombok.*;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiderProfileTierDto {

    private String riderId;
    private String tierName;
    private int tierId;


    public static RiderProfileTierDto of(RiderProfile riderProfile){
        return RiderProfileTierDto.builder()
                .riderId(riderProfile.getRiderId())
                .tierName(riderProfile.getTierName())
                .tierId(riderProfile.getTierId())
                .build();
    }
}
