package com.scb.rider.model.dto;

import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiderStatusAggregateCountDocument {

  private String aggregateStatus;
  private Long riderCount;
  
}
