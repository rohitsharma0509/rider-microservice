package com.scb.rider.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.scb.rider.model.document.RiderProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RiderInfo {

    private String riderId;
    private String firstName;
    private String lastName;
    private String dob;
    private String gender;
    private String nationalID;
    private String phoneNumber;

    public static RiderInfo of(RiderProfile riderProfile) {
        RiderInfo riderInfo = new RiderInfo();
        riderInfo.setFirstName(riderProfile.getFirstName());
        riderInfo.setLastName(riderProfile.getLastName());
        riderInfo.setRiderId(riderProfile.getRiderId());
        riderInfo.setDob(riderProfile.getDob());
        riderInfo.setNationalID(riderProfile.getNationalID());
        riderInfo.setPhoneNumber(riderProfile.getPhoneNumber());
        riderInfo.setGender(riderProfile.getGender());
        return riderInfo;
    }
}
