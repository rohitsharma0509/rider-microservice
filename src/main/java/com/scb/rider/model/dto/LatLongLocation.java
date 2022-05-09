package com.scb.rider.model.dto;

import com.fasterxml.jackson.annotation.JsonView;
import com.scb.rider.view.View;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LatLongLocation {
  @JsonView(value = {View.RiderJobDetailsView.class})
  private String latitude;
  @JsonView(value = {View.RiderJobDetailsView.class})
  private String longitude;
}