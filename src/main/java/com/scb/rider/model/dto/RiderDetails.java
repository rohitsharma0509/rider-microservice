package com.scb.rider.model.dto;

import com.scb.rider.model.document.RiderProfile;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;

@Getter
@Setter
@Builder
public class RiderDetails {
    private String id;
    private String riderId;
    private String preferredZoneId;
    private String preferredZoneName;


    public static RiderDetails of(RiderProfile riderProfile) {

        RiderDetails rider =  RiderDetails.builder()
                .id(riderProfile.getId())
                .riderId(riderProfile.getRiderId())
                .build();

        if(!ObjectUtils.isEmpty(riderProfile.getRiderPreferredZones())){
            rider.setPreferredZoneId(riderProfile.getRiderPreferredZones().getPreferredZoneId());
            rider.setPreferredZoneName(riderProfile.getRiderPreferredZones().getPreferredZoneName());
        }
        return rider;

    }
}
