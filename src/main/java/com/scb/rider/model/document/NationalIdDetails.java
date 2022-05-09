package com.scb.rider.model.document;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.validation.constraints.Size;

@Getter
@Setter
@Document
@Builder
public class NationalIdDetails implements Serializable {
    private static final long serialVersionUID = 1L;
    @Size(max=500)
    private String rejectionReason;
    @Size(max=500)
    private String rejectionComment;
    private LocalDateTime rejectionTime;
}
