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
public class ExcelUnauthorizedRiderDetails {
    private String riderName;
    private String riderId;
    private String riderPhoneNumber;
    private String tierName;
    private String status;
    private String isReadyForAuthorization;
    private String preferredZoneName;
    private String enrollmentDate;

    public static List<String> getHeaders() {
        List<String> headers = new ArrayList<>();
        headers.add(Constants.RIDER_NAME_TH);
        headers.add(Constants.RIDER_ID_TH);
        headers.add(Constants.PHONE_TH);
        headers.add(Constants.TIER_NAME_TH);
        headers.add(Constants.STATUS_TH);
        headers.add(Constants.IS_READY_TH);
        headers.add(Constants.WORK_AREA_TH);
        headers.add(Constants.REGISTRATION_DATE_TH);
        return headers;
    }

    public static ExcelUnauthorizedRiderDetails of(RiderProfile riderProfile){
        return ExcelUnauthorizedRiderDetails.builder()
                .riderName(riderProfile.getFirstName().concat(" ").concat(riderProfile.getLastName()))
                .riderId(riderProfile.getRiderId())
                .riderPhoneNumber(riderProfile.getPhoneNumber())
                .tierName(StringUtils.isNotBlank(riderProfile.getTierName()) ? riderProfile.getTierName(): NO_TIER)
                .status(Constants.UNAUTHORIZED_TH)
                .isReadyForAuthorization(getReadyToAuthorized(riderProfile.getIsReadyForAuthorization()))
                .preferredZoneName(Objects.nonNull(riderProfile.getRiderPreferredZones()) && StringUtils.isNotBlank(riderProfile.getRiderPreferredZones().getPreferredZoneName())
                        ? riderProfile.getRiderPreferredZones().getPreferredZoneName() : StringUtils.EMPTY)
                .enrollmentDate(Objects.nonNull(riderProfile.getRiderDocumentUpload()) && StringUtils.isNotBlank(riderProfile.getRiderDocumentUpload().getEnrollmentDate())
                        ? DateFormatterUtils.getFormattedDateFromDateString(riderProfile.getRiderDocumentUpload().getEnrollmentDate(), Constants.YYYY_MM_DD, Constants.DATE_WITH_SHORT_MONTH_NAME) : StringUtils.EMPTY)
                .build();
    }

    private static String getReadyToAuthorized(String isReadyForAuthorization) {
        if (StringUtils.isNotBlank(isReadyForAuthorization) && Boolean.parseBoolean(isReadyForAuthorization)) {
            return Constants.READY_TH;
        } else {
            return Constants.NOT_READY_TH;
        }
    }
}