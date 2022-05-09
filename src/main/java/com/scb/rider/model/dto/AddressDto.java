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
@Api(value = "Address")
public class AddressDto {
    @Size(max = 50, message = "{api.rider.profile.length.msg}")
    private String landmark;
    //@NotBlank(message = "{api.rider.profile.blank.msg}")
    @Size(max = 50, message = "{api.rider.profile.length.msg}")
    private String city;
    //@NotBlank(message = "{api.rider.profile.blank.msg}")
    @Size(max = 20, message = "{api.rider.profile.length.msg}")
    private String country;
    @Size(max = 50, message = "{api.rider.profile.length.msg}")
    private String village;
    //@NotBlank(message = "{api.rider.profile.blank.msg}")
    @Size(max = 50, message = "{api.rider.profile.length.msg}")
    private String district;
    //@NotBlank(message = "{api.rider.profile.blank.msg}")
    @Size(max = 30, message = "{api.rider.profile.length.msg}")
    private String state;
    @Size(max = 2, message = "{api.rider.profile.length.msg}")
    private String countryCode;
    //@NotBlank(message = "{api.rider.profile.blank.msg}")
    @Size(max = 10, message = "{api.rider.profile.length.msg}")
    private String zipCode;
    @Size(max = 50, message = "{api.rider.profile.length.msg}")
    private String floorNumber;
    //@NotBlank(message = "{api.rider.profile.blank.msg}")
    @Size(max = 50, message = "{api.rider.profile.length.msg}")
    private String unitNumber;
}