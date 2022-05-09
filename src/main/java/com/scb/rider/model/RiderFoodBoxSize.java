package com.scb.rider.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class RiderFoodBoxSize {
    private String riderId;
    private String foodBoxSize;
}
