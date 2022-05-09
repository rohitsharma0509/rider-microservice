package com.scb.rider.model.kafka;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class RiderJobCancellationPayload {

    private String jobId;
    private String type;
    private LocalDateTime dateTime;

    private String title;
    private String body;
    private String sound;
    private String click_action;

}
