package com.scb.rider.model.document;

import com.scb.rider.model.BaseEntity;
import com.scb.rider.model.enumeration.MandatoryCheckStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;


@Setter
@Getter
@Builder
@Document
@NoArgsConstructor
@AllArgsConstructor
public class RiderFoodCard extends BaseEntity {
    @Id
    private String id;
    @Indexed(unique = true)
    private String riderProfileId;
    private MandatoryCheckStatus status;
    private String documentUrl;
    private String reason;
    private String comment;
    private LocalDateTime rejectionTime;
}
