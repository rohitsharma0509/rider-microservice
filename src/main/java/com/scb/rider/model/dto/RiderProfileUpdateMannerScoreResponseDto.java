package com.scb.rider.model.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;


@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiderProfileUpdateMannerScoreResponseDto {

    private String id;
    private String riderId;
    private String actionType;
    private List<String> reason;
    private Integer actionScore;
    private String additionalComment;
}
