package com.scb.rider.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@ToString
public class RiderCovidSelfieDataList {
    private String filName;
    private LocalDateTime uploadedTime;
    private String mimeType;
    private byte[] data;
}
