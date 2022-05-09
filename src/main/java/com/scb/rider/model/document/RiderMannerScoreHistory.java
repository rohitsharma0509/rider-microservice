package com.scb.rider.model.document;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@Setter
@Document
public class RiderMannerScoreHistory implements Serializable {
  private static final long serialVersionUID = 1L;
  @Id
  private String id;
  private String riderId;
  private Integer currentScore;
  private String actionType;
  private Integer actionScore;
  private Integer finalScore;
  private List<String> reason;
  private String additionalComment;
  private String createdBy;
  @CreatedDate
  private LocalDateTime createdDate;
}
