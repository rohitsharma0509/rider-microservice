package com.scb.rider.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ExcessiveWaitingTimeDetailsEntity {
    private double excessiveWaitTopupAmount;
    private LocalDateTime excessiveWaitTopupDateTime;
}