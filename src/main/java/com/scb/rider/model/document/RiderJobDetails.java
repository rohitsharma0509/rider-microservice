package com.scb.rider.model.document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonView;
import com.scb.rider.model.BaseEntity;
import com.scb.rider.model.enumeration.CancellationSource;
import com.scb.rider.model.enumeration.RiderJobStatus;
import com.scb.rider.view.View;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document
public class RiderJobDetails extends BaseEntity {
    @Id
    @JsonView(value = {View.RiderJobDetailsView.class})
    private String id;
    @Indexed(name="riderJobProfileId")
    @JsonView(value = {View.RiderJobDetailsView.class})
    private String profileId;
    @Indexed(unique = true)
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
    private String customerRemark;
    @JsonIgnore
    private String updatedBy;
}
