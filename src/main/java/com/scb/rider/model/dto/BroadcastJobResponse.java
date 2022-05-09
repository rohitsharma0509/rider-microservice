package com.scb.rider.model.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class BroadcastJobResponse {

  private String jobId;

  private String broadcastStatus;

  private LocalDateTime lastBroadcastDateTime;

  private LocalDateTime expiryTimeForBroadcasting;


}