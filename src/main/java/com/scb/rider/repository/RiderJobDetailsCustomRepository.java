package com.scb.rider.repository;

import com.scb.rider.model.document.RiderJobDetails;

import java.time.LocalDateTime;
import java.util.List;

public interface RiderJobDetailsCustomRepository {

    public List<RiderJobDetails> findRiderJobReconciliationDetails(LocalDateTime startDate, LocalDateTime endDate);

    public  RiderJobDetails findRunningJobIdForRider(String riderId);
}
