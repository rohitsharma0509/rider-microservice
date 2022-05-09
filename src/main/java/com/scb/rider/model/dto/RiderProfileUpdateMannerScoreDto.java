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
public class RiderProfileUpdateMannerScoreDto {


    @NotBlank(message = "{api.rider.profile.blank.msg}")
    private String riderId;
    @NotBlank(message = "{api.rider.profile.blank.msg}")
    private String actionType;
    @NotNull(message = "{api.rider.profile.null.msg}")
    private List<String> reason;
    @NotNull(message = "{api.rider.profile.null.msg}")
    private Integer actionScore;
    private String additionalComment;
}
