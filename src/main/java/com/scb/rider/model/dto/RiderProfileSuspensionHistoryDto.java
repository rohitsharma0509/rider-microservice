package com.scb.rider.model.dto;

import com.scb.rider.model.document.RiderSuspendHistory;
import com.scb.rider.util.DateFormatterUtils;
import lombok.*;
import org.joda.time.DateTimeUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiderProfileSuspensionHistoryDto {

    private String riderId;
    private List<String> suspensionReason;
    private String suspensionNote;
    private Integer suspensionDuration;
    private String suspensionExpiryTime;
    private String createdDate;
    private String createdBy;

    public static RiderProfileSuspensionHistoryDto of(RiderSuspendHistory riderSuspendHistory) {
        String suspensionExpiryTime = riderSuspendHistory.getSuspensionExpiryTime() == null ? null: riderSuspendHistory.getSuspensionExpiryTime().toString();
        String createdDate = riderSuspendHistory.getCreatedDate().toString();

        return RiderProfileSuspensionHistoryDto.builder()
                .riderId(riderSuspendHistory.getRiderId())
                .suspensionReason(riderSuspendHistory.getSuspensionReason())
                .suspensionNote(riderSuspendHistory.getSuspensionNote())
                .suspensionDuration(riderSuspendHistory.getSuspensionDuration())
                .suspensionExpiryTime(suspensionExpiryTime)
                .createdBy(riderSuspendHistory.getCreatedBy())
                .createdDate(createdDate)
                .build();
    }

}
