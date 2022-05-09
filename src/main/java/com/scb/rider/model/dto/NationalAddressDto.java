package com.scb.rider.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.Api;
import lombok.*;

import javax.validation.constraints.Size;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Api(value = "NationalAddress")

public class NationalAddressDto {
    private String buildingName;
    private String roomNumber;
    private String floor;
    private String number;
    private String alley;
    private String neighbourhood;
    private String road;
    private String subdistrict;
    private String district;
    private String province;
    private String postalCode;
}