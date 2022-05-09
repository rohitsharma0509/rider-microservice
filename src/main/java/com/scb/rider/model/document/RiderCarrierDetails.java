package com.scb.rider.model.document;

import com.scb.rider.model.BaseEntity;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document
public class RiderCarrierDetails extends BaseEntity {
    @Id
    private String id;
    @Indexed(unique=true)
    private String riderId;
    @NotNull
    private String name;
    private String mobileNetworkCode;
    private String mobileNetworkOperator;
}
