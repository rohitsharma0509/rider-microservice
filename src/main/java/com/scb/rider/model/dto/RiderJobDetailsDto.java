package com.scb.rider.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.scb.rider.model.document.RiderJobDetails;
import com.scb.rider.model.enumeration.CancellationSource;
import com.scb.rider.model.enumeration.RiderJobStatus;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.BeanUtils;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Api(value = "RiderJobDetails")
@ApiModel(value = "RiderJobDetails")
public class RiderJobDetailsDto {

  private String id;
  private String profileId;
  private String jobId;
  private RiderJobStatus jobStatus;
  private String mealPhotoUrl;
  private String mealDeliveredPhotoUrl;
  private String parkingPhotoUrl;
  private BigDecimal parkingFee;
  private CancellationSource cancellationSource;
  private String remarks;
  private LocalDateTime jobAcceptedTime;
  private LocalDateTime calledMerchantTime;
  private LocalDateTime arrivedAtMerchantTime;
  private LocalDateTime mealPickedUpTime;
  private LocalDateTime arrivedAtCustLocationTime;
  private LocalDateTime foodDeliveredTime;
  private LocalDateTime orderCancelledByOperationTime;
  private LocalDateTime parkingReceiptPhotoTime;

  public static RiderJobDetailsDto of(RiderJobDetails riderJobDetails) {
    RiderJobDetailsDto riderJobDetailsDto = RiderJobDetailsDto.builder().build();

    BeanUtils.copyProperties(riderJobDetails, riderJobDetailsDto);

    return riderJobDetailsDto;
  }

}
