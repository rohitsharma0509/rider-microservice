package com.scb.rider.model.dto;

import com.scb.rider.model.document.RiderJobDetails;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.enumeration.RiderJobStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@ToString
public class RiderSettlementDetails {

	private String jobId;
	private String orderId;
	private String riderId;
	private String riderName;
	private BigDecimal jobPrice;
	private BigDecimal netPrice;
	private String jobStatus;
	private String accountNumber;
	private String jobType;
	private BigDecimal taxAmount;
	private LocalDateTime jobStartDateTime;
	private LocalDateTime jobEndDateTime;
	private Double ewtAmount;

	public static RiderSettlementDetails of(RiderJobDetails jobDetails, RiderProfile riderProfile,
			JobSettlementDetails jobSettlementDetails) {
		BigDecimal netPaymentPrice = jobSettlementDetails != null
				? jobSettlementDetails.getNetPaymentPrice() != null ? jobSettlementDetails.getNetPaymentPrice()
						: jobSettlementDetails.getNetPrice()
				: null;

		RiderSettlementDetails riderSettlementDetail = new RiderSettlementDetails();
		riderSettlementDetail.setJobId(jobDetails.getJobId());
		riderSettlementDetail.setOrderId(jobSettlementDetails != null ? jobSettlementDetails.getOrderId() : null);
		riderSettlementDetail.setRiderId(riderProfile.getRiderId());
		riderSettlementDetail.setRiderName(riderProfile.getFirstName() + " " + riderProfile.getLastName());
		riderSettlementDetail.setJobPrice(jobSettlementDetails != null ? jobSettlementDetails.getNetPrice() : null);
		riderSettlementDetail.setNetPrice(netPaymentPrice);
		riderSettlementDetail.setTaxAmount(
				jobSettlementDetails != null ? jobSettlementDetails.getTaxAmount() : null);
		riderSettlementDetail.setAccountNumber(Objects.nonNull(riderProfile.getAccountNumber()) ? riderProfile.getAccountNumber().trim() : null);
		riderSettlementDetail.setJobStatus(
				jobDetails.getJobStatus().equals(RiderJobStatus.FOOD_DELIVERED) ? "COMPLETED" : "CANCELLED");
		riderSettlementDetail.setJobStartDateTime(jobDetails.getJobAcceptedTime());
		riderSettlementDetail.setJobEndDateTime(jobDetails.getJobStatus().equals(RiderJobStatus.FOOD_DELIVERED) ?
				jobDetails.getFoodDeliveredTime() : jobDetails.getOrderCancelledByOperationTime());
		riderSettlementDetail.setJobType(Objects.nonNull(jobSettlementDetails) ? jobSettlementDetails.getJobType() : null);
		double ewtAmount = Objects.nonNull(jobSettlementDetails) && Objects.nonNull(jobSettlementDetails.getExcessiveWaitTimeDetailsEntity())
				&& Objects.nonNull(jobSettlementDetails.getExcessiveWaitTimeDetailsEntity().getExcessiveWaitTopupAmount())
				? jobSettlementDetails.getExcessiveWaitTimeDetailsEntity().getExcessiveWaitTopupAmount() : 0.0;
		riderSettlementDetail.setEwtAmount(ewtAmount);
		return riderSettlementDetail;

	}
}
