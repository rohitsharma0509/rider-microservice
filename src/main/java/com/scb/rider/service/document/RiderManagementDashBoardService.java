package com.scb.rider.service.document;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.stereotype.Service;
import com.scb.rider.model.dto.RiderManagementDashBoardResponseDto;
import com.scb.rider.model.dto.RiderStatusAggregateCountDocument;
import com.scb.rider.model.enumeration.AvailabilityStatus;
import com.scb.rider.model.enumeration.RiderJobStatus;
import com.scb.rider.repository.RiderJobDetailsRepository;
import com.scb.rider.repository.RiderProfileRepository;
import com.scb.rider.repository.RiderTrainingAppointmentRepository;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class RiderManagementDashBoardService {

  @Autowired
  private RiderProfileRepository riderProfileRepository;

  @Autowired
  private RiderTrainingAppointmentRepository riderTrainingAppointmentRepository;


  public RiderManagementDashBoardResponseDto getRiderManagementDashBoardSummary(String requestId) {

    Map<String, Long> riderStatusAggregatedCount = new HashMap<String, Long>();

    AggregationResults<RiderStatusAggregateCountDocument> riderProfileStatusAgg =
        riderProfileRepository.groupByStatus();
    
    log.info(String.format("Rider Management DashBoard riderProfileStatusAgg Request Id - %s", requestId));
    
    AggregationResults<RiderStatusAggregateCountDocument> countAllRiderAgg =
        riderProfileRepository.groupByRiderId();

    log.info(String.format("Rider Management DashBoard countAllRiderAgg Request Id - %s", requestId));
    
    AggregationResults<RiderStatusAggregateCountDocument> countActiveJobRiderAgg =
        riderProfileRepository.groupByRiderAvailabilityStatus(AvailabilityStatus.JobInProgress.name());
        
    log.info(String.format("Rider Management DashBoard countActiveJobRiderAgg Request Id - %s", requestId));
    LocalDate bangkokToday = LocalDate.now(ZoneId.of("Asia/Bangkok"));
    AggregationResults<RiderStatusAggregateCountDocument> countRiderOnTrainingAgg =
        riderTrainingAppointmentRepository.groupByTodaysDate(bangkokToday);

    log.info(String.format("Rider Management DashBoard countRiderOnTrainingAgg Request Id - %s", requestId));
    
    riderProfileStatusAgg.getMappedResults().stream()
        .forEach(aggResult -> riderStatusAggregatedCount.put(aggResult.getAggregateStatus(),
            aggResult.getRiderCount()));

    countAllRiderAgg.getMappedResults().stream().forEach(aggResult -> riderStatusAggregatedCount
        .put(aggResult.getAggregateStatus(), aggResult.getRiderCount()));

    countActiveJobRiderAgg.getMappedResults().stream()
    .forEach(aggResult -> riderStatusAggregatedCount.put(aggResult.getAggregateStatus(),
        aggResult.getRiderCount()));


    countRiderOnTrainingAgg.getMappedResults().stream()
        .forEach(aggResult -> riderStatusAggregatedCount.put(aggResult.getAggregateStatus(),
            aggResult.getRiderCount()));
    
    log.info(String.format("Rider Management DashBoard Response return Request Id - %s", requestId));
    
    return RiderManagementDashBoardResponseDto.of(riderStatusAggregatedCount);
  }
}
