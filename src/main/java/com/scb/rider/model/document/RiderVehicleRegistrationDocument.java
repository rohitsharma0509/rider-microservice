package com.scb.rider.model.document;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.scb.rider.model.BaseEntity;
import com.scb.rider.model.enumeration.FoodBoxSize;
import com.scb.rider.model.enumeration.MandatoryCheckStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Setter
@Getter
@Builder
@Document
@CompoundIndex(name="registrationNo_province", def = "{'registrationNo': 1, 'province': 1}", unique = true)
public class RiderVehicleRegistrationDocument extends BaseEntity {
    @Id
    private String id;
    private String registrationNo;
    private String registrationCardId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate registrationDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;
    private String makerModel;
    private String province;
    private String uploadedVehicleDocUrl;
    @Indexed(unique = true)
    private String riderProfileId;
    private MandatoryCheckStatus status;
    private String uploadedFoodCardUrl;
    private MandatoryCheckStatus foodCardStatus;
    private String vehicleRejectionReason;
    private String vehicleRejectionComment;
    private LocalDateTime vehicleRejectionTime;
    private String foodCardRejectionReason;
    private String foodCardRejectionComment;
    private LocalDateTime foodCardRejectionTime;
    private FoodBoxSize foodBoxSize;
    private String updatedBy;
}
