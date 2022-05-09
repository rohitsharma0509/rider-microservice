package com.scb.rider.model.dto;


import com.scb.rider.constants.Constants;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.enumeration.RiderStatus;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.scb.rider.constants.TierConstants.NO_TIER;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ExcelRiderDetails {
    private String riderName;
    private String riderId;
    private String riderPhoneNumber;
    private String tierName;
    private String status;
    private String preferredZoneName;

    public static List<String> getHeaders() {
        List<String> headers = new ArrayList<>();
        headers.add(Constants.RIDER_NAME_TH);
        headers.add(Constants.RIDER_ID_TH);
        headers.add(Constants.PHONE_TH);
        headers.add(Constants.TIER_NAME_TH);
        headers.add(Constants.STATUS_TH);
        headers.add(Constants.WORK_AREA_TH);
        return headers;
    }

    public static ExcelRiderDetails createExcelRiderTierDetailEntity(RiderProfile riderProfile){
        return ExcelRiderDetails.builder()
                .riderId(riderProfile.getRiderId())
                .riderName(riderProfile.getFirstName().concat(" ").concat(riderProfile.getLastName()))
                .riderPhoneNumber(riderProfile.getPhoneNumber())
                .tierName(StringUtils.isNotBlank(riderProfile.getTierName()) ? riderProfile.getTierName(): NO_TIER)
                .status(getStatus(riderProfile.getStatus()))
                .preferredZoneName(Objects.nonNull(riderProfile.getRiderPreferredZones()) && StringUtils.isNotBlank(riderProfile.getRiderPreferredZones().getPreferredZoneName())
                        ? riderProfile.getRiderPreferredZones().getPreferredZoneName() : StringUtils.EMPTY)
                .build();
    }

    private static String getStatus(RiderStatus status) {
        if(RiderStatus.AUTHORIZED.equals(status)) {
            return Constants.AUTHORIZED_TH;
        } else if(RiderStatus.SUSPENDED.equals(status)) {
            return Constants.SUSPENDED_TH;
        } else {
            return Constants.UNAUTHORIZED_TH;
        }
    }
}
