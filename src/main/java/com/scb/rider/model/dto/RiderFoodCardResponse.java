package com.scb.rider.model.dto;

import com.scb.rider.model.document.RiderFoodCard;
import com.scb.rider.model.enumeration.MandatoryCheckStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiderFoodCardResponse {

    private String id;
    private String riderProfileId;
    private MandatoryCheckStatus status;
    private String documentUrl;
    private String reason;
    private String comment;
    private LocalDateTime rejectionTime;

    public static RiderFoodCardResponse of(RiderFoodCard document) {
        return RiderFoodCardResponse.builder().id(document.getId())
                .riderProfileId(document.getRiderProfileId())
                .status(document.getStatus())
                .documentUrl(document.getDocumentUrl())
                .reason(document.getReason())
                .comment(document.getComment())
                .build();
    }
}
