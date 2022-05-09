package com.scb.rider.model.kafka;

import com.scb.rider.model.document.RiderPreferredZones;
import com.scb.rider.model.enumeration.AvailabilityStatus;
import com.scb.rider.model.enumeration.RiderStatus;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@ToString
public class RiderStatusUpdateModel {
    private String riderId;
    private RiderPreferredZones riderPreferredZones;
    private String id;
    private RiderStatus status;
    private AvailabilityStatus availabilityStatus;
    private Boolean evBikeUser;
    private Boolean rentingToday;
}
