package com.scb.rider.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.scb.rider.model.document.RiderVehicleRegistrationDocument;
import com.scb.rider.model.enumeration.FoodBoxSize;
import com.scb.rider.model.enumeration.MandatoryCheckStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Builder
public class RiderVehicleRegistrationDetailsResponse {
    private  String id;
    private String registrationNo;
    private String registrationCardId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate registrationDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;
    private String makerModel;
    private String province;
    private String uploadedVehicleDocUrl;
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

    public static RiderVehicleRegistrationDetailsResponse of(RiderVehicleRegistrationDocument document) {

        return RiderVehicleRegistrationDetailsResponse.builder()
                .id(document.getId())
                .registrationNo(document.getRegistrationNo())
                .registrationCardId(document.getRegistrationCardId())
                .expiryDate(document.getExpiryDate())
                .registrationDate(document.getRegistrationDate())
                .makerModel(document.getMakerModel())
                .province(document.getProvince())
                .riderProfileId(document.getRiderProfileId())
                .uploadedVehicleDocUrl(document.getUploadedVehicleDocUrl())
                .status(document.getStatus())
                .uploadedFoodCardUrl(document.getUploadedFoodCardUrl())
                .foodCardStatus(document.getFoodCardStatus())
                .vehicleRejectionReason(document.getVehicleRejectionReason())
                .vehicleRejectionComment(document.getVehicleRejectionComment())
                .vehicleRejectionTime(document.getVehicleRejectionTime())
                .foodCardRejectionReason(document.getFoodCardRejectionReason())
                .foodCardRejectionComment(document.getFoodCardRejectionComment())
                .foodCardRejectionTime(document.getFoodCardRejectionTime())
                .foodBoxSize(document.getFoodBoxSize())
                .build();

    }
}
