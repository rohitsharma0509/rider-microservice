package com.scb.rider.model;

import java.math.BigDecimal;

import com.scb.rider.model.enumeration.EvBikeVendors;
import com.scb.rider.model.enumeration.RiderJobStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@ToString
public class RiderJobStatusEventModel {
    private String riderId;
    private String jobId;
    private String dateTime ;
    private RiderJobStatus status;
    private BigDecimal jobPrice;
    private Boolean isJobPriceModified;
    private String riderRRid;
    private String imageUrl;
	private Boolean evBikeUser;
	private Boolean rentingToday;
    private String updatedBy;
	private EvBikeVendors evBikeVendor;

	private String driverName;
	private String driverPhone;
	private String driverImageUrl;


	public RiderJobStatusEventModel(String riderId, String jobId, String dateTime, RiderJobStatus status,
                                    String riderRRid) {
		super();
		this.riderId = riderId;
		this.jobId = jobId;
		this.dateTime = dateTime;
		this.status = status;
		this.riderRRid = riderRRid;
	}

	public RiderJobStatusEventModel(String riderId, String jobId, String dateTime, RiderJobStatus status,
									String riderRRid, String imageUrl) {
		super();
		this.riderId = riderId;
		this.jobId = jobId;
		this.dateTime = dateTime;
		this.status = status;
		this.riderRRid = riderRRid;
		this.imageUrl = imageUrl;
	}

	public RiderJobStatusEventModel(String riderId, String jobId, String dateTime, RiderJobStatus status,
									BigDecimal jobPrice, Boolean isJobPriceModified, String riderRRid) {
		super();
		this.riderId = riderId;
		this.jobId = jobId;
		this.dateTime = dateTime;
		this.status = status;
		this.riderRRid = riderRRid;
		this.jobPrice = jobPrice;
		this.isJobPriceModified = isJobPriceModified;
	}

	public RiderJobStatusEventModel(String riderId, String jobId, String dateTime, RiderJobStatus status,
									BigDecimal jobPrice, Boolean isJobPriceModified, String riderRRid, String updatedBy) {
		super();
		this.riderId = riderId;
		this.jobId = jobId;
		this.dateTime = dateTime;
		this.status = status;
		this.riderRRid = riderRRid;
		this.jobPrice = jobPrice;
		this.isJobPriceModified = isJobPriceModified;
		this.updatedBy = updatedBy;
	}
    
}
