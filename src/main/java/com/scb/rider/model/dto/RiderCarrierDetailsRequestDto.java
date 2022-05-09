package com.scb.rider.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@Builder
public class RiderCarrierDetailsRequestDto {
    @NotNull
    private String name;
    private String mobileNetworkCode;
    private String mobileNetworkOperator;
}
