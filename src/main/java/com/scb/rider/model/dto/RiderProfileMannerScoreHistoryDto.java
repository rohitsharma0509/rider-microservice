package com.scb.rider.model.dto;

import com.scb.rider.model.document.RiderMannerScoreHistory;
import com.scb.rider.util.DateFormatterUtils;
import lombok.*;

import java.util.List;


@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiderProfileMannerScoreHistoryDto {

    private String riderId;
    private String currentScore;
    private String actionType;
    private Integer actionScore;
    private Integer finalScore;
    private List<String> reason;
    private String additionalComment;
    private String createdDate;
    private String createdBy;

    public static RiderProfileMannerScoreHistoryDto of(RiderMannerScoreHistory riderMannerScoreHistory){
        return RiderProfileMannerScoreHistoryDto.builder()
                .riderId(riderMannerScoreHistory.getRiderId())
                .currentScore(riderMannerScoreHistory.getCurrentScore().toString())
                .actionType(riderMannerScoreHistory.getActionType())
                .actionScore(riderMannerScoreHistory.getActionScore())
                .finalScore(riderMannerScoreHistory.getFinalScore())
                .reason(riderMannerScoreHistory.getReason())
                .additionalComment(riderMannerScoreHistory.getAdditionalComment())
                .createdDate(riderMannerScoreHistory.getCreatedDate().toString())
                .createdBy(riderMannerScoreHistory.getCreatedBy())
                .build();
    }

}
