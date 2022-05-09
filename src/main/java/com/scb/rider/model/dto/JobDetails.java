package com.scb.rider.model.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@ToString
public class JobDetails {
    private String jobId;
    private String orderId;
    private String driverId;
    private String driverPhone;
    private String jobStatusKey;
    private String creationDateTime;
    private String lastUpdatedDateTime;
    private String jobAcceptedTime;
    private String calledMerchantTime;
    private String arrivedAtMerchantTime;
    private String mealPickedUpTime;
    private String arrivedAtCustLocationTime;
    private String foodDeliveredTime;
    private Double normalPrice;
    private Double netPrice;
    private Double netPaymentPrice;
    private Double taxAmount;
    private String orderCancelledByOperationTime;
    private boolean merchantConfirm;
    private LocalDateTime merchantConfirmDateTime;

    private List<JobLocation> locationList;
    private List<OrderItems> orderItems;
    private String remark;
    private Double totalDistance;
    private Double minDistanceForJobCompletion;
    private String customerRemark;
    private String shopLandmark;
}
