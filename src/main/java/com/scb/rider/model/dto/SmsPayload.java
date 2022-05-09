package com.scb.rider.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SmsPayload {

    @NotBlank(message = "{api.rider.profile.blank.msg}")
    private String mobileNumber;

    @NotBlank(message = "{api.rider.profile.blank.msg}")
    private String message;

    public static SmsPayload of(String mobileNumber, String message) {
        return SmsPayload.builder()
                .mobileNumber(mobileNumber)
                .message(message)
                .build();
    }
}
