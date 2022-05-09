package com.scb.rider.model;

import java.math.BigDecimal;
import com.scb.rider.model.document.RiderPreferredZones;
import com.scb.rider.model.enumeration.AvailabilityStatus;
import com.scb.rider.model.enumeration.EvBikeVendors;
import com.scb.rider.model.enumeration.RiderJobStatus;

import com.scb.rider.model.enumeration.RiderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@ToString
public class RiderAvailabilityEventModel {
    private String riderId;
    private String dateTime;
    private String event;
    private String serviceName;
    private RiderPreferredZones riderPreferredZones;

    
}
