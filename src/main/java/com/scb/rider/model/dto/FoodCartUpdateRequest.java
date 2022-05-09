package com.scb.rider.model.dto;

import com.scb.rider.model.enumeration.FoodBoxSize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@AllArgsConstructor
@Builder
@Getter
@NoArgsConstructor
@Setter
public class FoodCartUpdateRequest {
    @NotNull(message = "{api.rider.profile.null.msg}")
    private FoodBoxSize foodBoxSize;
}
