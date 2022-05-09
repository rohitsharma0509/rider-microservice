package com.scb.rider.model.document;

import java.io.Serializable;
import java.time.LocalDateTime;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import com.scb.rider.model.BaseEntity;
import com.scb.rider.model.enumeration.AvailabilityStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Document
@Builder

public class RiderActiveStatusDetails extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    
    
    private ObjectId id;
    @Indexed(unique = true)
    private String riderId;
    private String riderRrId;
    private LocalDateTime activeTime;
    private String zoneName;
    private Integer zoneId;
    private AvailabilityStatus availabilityStatus;
    private LocalDateTime availabilityStatusUpdatedTime;
    private LocalDateTime jobStatusChangeUpdatedTime;
    private String availabilityStatusUpdatedTimeString;
    private String jobStatusChangeUpdatedTimeString;
}