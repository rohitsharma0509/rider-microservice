package com.scb.rider.model.document;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@Document
public class RiderSuspendHistory implements Serializable {
  private static final long serialVersionUID = -4073617470445403673L;

  @Id
  private String id;

  @Indexed()
  private String riderId;

  private List<String> suspensionReason;

  private String suspensionNote;

  private Integer suspensionDuration;

  private LocalDateTime suspensionExpiryTime;

  private String riderCaseNo;

  @CreatedDate
  private LocalDateTime createdDate;

  private String createdBy;

}
