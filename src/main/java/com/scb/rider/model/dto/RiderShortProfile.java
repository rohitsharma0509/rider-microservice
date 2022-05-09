package com.scb.rider.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.scb.rider.constants.Constants;
import com.scb.rider.model.document.RiderProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Log4j2
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RiderShortProfile {

    private String status;
    private Integer code;
    private String errorMessage;
    private RiderInfo riderInfo;

    public static RiderShortProfile of(RiderProfile riderProfile){
        RiderShortProfile riderShortProfile = new RiderShortProfile();
        riderShortProfile.setStatus(Constants.SUCCESS);
        riderShortProfile.setCode(Constants.CODE_100);
        riderShortProfile.setRiderInfo(RiderInfo.of(riderProfile));
        log.info("Returning success response for rider " + riderShortProfile.getRiderInfo().getRiderId());
        return riderShortProfile;
    }
}
