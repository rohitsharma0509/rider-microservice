package com.scb.rider.model.dto;

import com.scb.rider.model.enumeration.EvBikeVendors;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RentingTodayRequest {
    private List<String> riders;
    private Boolean rentingToday;
    private EvBikeVendors evBikeVendor;
}
