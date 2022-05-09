package com.scb.rider.model.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import com.scb.rider.model.enumeration.CancellationSource;
import com.scb.rider.model.enumeration.RiderJobStatus;
import com.scb.rider.view.View;
import lombok.*;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RiderJobAcceptedDetails {
    @JsonView(value = {View.RiderJobDetailsView.class})
    private String id;
    @JsonView(value = {View.RiderJobDetailsView.class})
    private String profileId;
    @JsonView(value = {View.RiderJobDetailsView.class})
    private String jobId;
    @JsonView(value = {View.RiderJobDetailsView.class})
    private RiderJobStatus jobStatus;
    @JsonView(value = {View.RiderJobDetailsView.class})
    private String mealPhotoUrl;
    @JsonView(value = {View.RiderJobDetailsView.class})
    private String mealDeliveredPhotoUrl;
    @JsonView(value = {View.RiderJobDetailsView.class})
    private String parkingPhotoUrl;
    @JsonView(value = {View.RiderJobDetailsView.class})
    private BigDecimal parkingFee;
    @JsonView(value = {View.RiderJobDetailsView.class})
    private CancellationSource cancellationSource;
    @JsonView(value = {View.RiderJobDetailsView.class})
    private String remarks;
    @JsonView(value = {View.RiderJobDetailsView.class})
    private LocalDateTime jobAcceptedTime;
    @JsonView(value = {View.RiderJobDetailsView.class})
    private LocalDateTime calledMerchantTime;
    @JsonView(value = {View.RiderJobDetailsView.class})
    private LocalDateTime arrivedAtMerchantTime;
    @JsonView(value = {View.RiderJobDetailsView.class})
    private LocalDateTime mealPickedUpTime;
    @JsonView(value = {View.RiderJobDetailsView.class})
    private LocalDateTime arrivedAtCustLocationTime;
    @JsonView(value = {View.RiderJobDetailsView.class})
    private LocalDateTime foodDeliveredTime;
    @JsonView(value = {View.RiderJobDetailsView.class})
    private LocalDateTime orderCancelledByOperationTime;
    @JsonView(value = {View.RiderJobDetailsView.class})
    private LocalDateTime parkingReceiptPhotoTime;

    @JsonView(value = {View.RiderJobDetailsView.class})
    private String merchantName;
    @JsonView(value = {View.RiderJobDetailsView.class})
    private String merchantAddress;
    @JsonView(value = {View.RiderJobDetailsView.class})
    private String merchantPhone;
    @JsonView(value = {View.RiderJobDetailsView.class})
    private LatLongLocation merchantLocation;
    @JsonView(value = {View.RiderJobDetailsView.class})
    private String customerName;
    @JsonView(value = {View.RiderJobDetailsView.class})
    private String customerAddress;
    @JsonView(value = {View.RiderJobDetailsView.class})
    private String customerPhone;
    @JsonView(value = {View.RiderJobDetailsView.class})
    private LatLongLocation customerLocation;
    @JsonView(value = {View.RiderJobDetailsView.class})
    private String expiry;
    @JsonView(value = {View.RiderJobDetailsView.class})
    private String orderId;
    @JsonView(value = {View.RiderJobDetailsView.class})
    private List<OrderItems> orderItems;
    @JsonView(value = {View.RiderJobDetailsView.class})
    private String remark;
    @JsonView(value = {View.RiderJobDetailsView.class})
    private Double price;
    @JsonView(value = {View.RiderJobDetailsView.class})
    private Double distance;
    @JsonView(value = {View.RiderJobDetailsView.class})
    private Double minDistanceForJobCompletion;
    @JsonView(value = {View.RiderJobDetailsView.class})
    private String customerRemark;
    @JsonView(value = {View.RiderJobDetailsView.class})
    private String shopLandmark;

    public static RiderJobAcceptedDetails of(JobDetails jobDetails) {
        JobLocation merchantDetails = getLocation(jobDetails, 1);
        JobLocation customerDetails = getLocation(jobDetails, 2);

        LatLongLocation merchantlatLng = LatLongLocation.builder().latitude(merchantDetails.getLat())
                .longitude(merchantDetails.getLng()).build();
        LatLongLocation customerlatLng = LatLongLocation.builder().latitude(customerDetails.getLat())
                .longitude(customerDetails.getLng()).build();

        RiderJobAcceptedDetails completeDetails = RiderJobAcceptedDetails.builder()
                .merchantName(merchantDetails.getContactName())
                .merchantAddress(merchantDetails.getAddress())
                .merchantPhone(merchantDetails.getContactPhone())
                .merchantLocation(merchantlatLng)
                .customerName(customerDetails.getContactName())
                .customerAddress(customerDetails.getAddress())
                .customerPhone(customerDetails.getContactPhone())
                .customerLocation(customerlatLng)
                .orderId(jobDetails.getOrderId())
                .remark(jobDetails.getRemark())
                .customerRemark(jobDetails.getCustomerRemark())
                .price(jobDetails.getNetPrice() == null ? 0.0 : jobDetails.getNetPrice())
                .minDistanceForJobCompletion(jobDetails.getMinDistanceForJobCompletion())
                .shopLandmark(jobDetails.getShopLandmark())
                .build();
        if (ObjectUtils.isEmpty(jobDetails.getOrderItems()))
            completeDetails.setOrderItems(Collections.emptyList());
        else
            completeDetails.setOrderItems(jobDetails.getOrderItems());
        return completeDetails;
    }

    private static JobLocation getLocation(JobDetails jobEntity, int seq) {
        return jobEntity.getLocationList().stream().filter(loc -> loc.getSeq() == seq).findFirst()
                .orElseThrow(() -> new RuntimeException("Error fetching location data from job data"));
    }
}
