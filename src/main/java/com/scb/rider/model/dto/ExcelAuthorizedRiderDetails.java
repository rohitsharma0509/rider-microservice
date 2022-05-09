package com.scb.rider.model.dto;

import com.scb.rider.constants.Constants;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.util.DateFormatterUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.scb.rider.constants.TierConstants.NO_TIER;

@AllArgsConstructor
@Builder
@Getter
@NoArgsConstructor
@Setter
public class ExcelAuthorizedRiderDetails {
    private String riderName;
    private String riderId;
    private String riderPhoneNumber;
    private String tierName;
    private String status;
    private String approvalDate;
    private String preferredZoneName;

    public static List<String> getHeaders() {
        List<String> headers = new ArrayList<>();
        headers.add(Constants.RIDER_NAME_TH);
        headers.add(Constants.RIDER_ID_TH);
        headers.add(Constants.PHONE_TH);
        headers.add(Constants.TIER_NAME_TH);
        headers.add(Constants.STATUS_TH);
        headers.add(Constants.APPROVAL_DATE_TH);
        headers.add(Constants.WORK_AREA_TH);
        return headers;
    }

    public static ExcelAuthorizedRiderDetails of(RiderProfile riderProfile){
        return ExcelAuthorizedRiderDetails.builder()
                .riderName(riderProfile.getFirstName().concat(" ").concat(riderProfile.getLastName()))
                .riderId(riderProfile.getRiderId())
                .riderPhoneNumber(riderProfile.getPhoneNumber())
                .tierName(StringUtils.isNotBlank(riderProfile.getTierName()) ? riderProfile.getTierName(): NO_TIER)
                .status(Constants.AUTHORIZED_TH)
                .approvalDate(DateFormatterUtils.getFormattedDateTimeFromDateTimeString(riderProfile.getApprovalDateTime(), Constants.DATETIME_FULL, Constants.DATE_WITH_SHORT_MONTH_NAME))
                .preferredZoneName(Objects.nonNull(riderProfile.getRiderPreferredZones()) && StringUtils.isNotBlank(riderProfile.getRiderPreferredZones().getPreferredZoneName())
                        ? riderProfile.getRiderPreferredZones().getPreferredZoneName() : StringUtils.EMPTY)
                .build();
    }
}