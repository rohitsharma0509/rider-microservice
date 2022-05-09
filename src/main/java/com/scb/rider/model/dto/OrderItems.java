package com.scb.rider.model.dto;

import com.fasterxml.jackson.annotation.JsonView;
import com.scb.rider.view.View;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OrderItems {

  @JsonView(value = {View.RiderJobDetailsView.class})
  private String name;
  @JsonView(value = {View.RiderJobDetailsView.class})
  private int quantity;

}