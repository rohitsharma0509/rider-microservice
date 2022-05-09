package com.scb.rider.model.document;

import com.scb.rider.model.BaseEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
@Document
@Builder
@CompoundIndex(name="riderId_uploadedTimeDesc", def = "{'riderId': 1, 'uploadedTime': -1}")
public class RiderCovidSelfie extends BaseEntity {
    @Id
    private String id;
    private String riderId;
    @NotNull
    private String fileName;
    private LocalDateTime uploadedTime;
    private String mimeType;
}
