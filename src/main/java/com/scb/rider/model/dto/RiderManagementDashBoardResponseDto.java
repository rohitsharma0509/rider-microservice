package com.scb.rider.model.dto;

import java.util.Map;
import com.scb.rider.model.enumeration.RiderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RiderManagementDashBoardResponseDto {

  private static final String RIDER_TRAINING_TODAY_COUNT = "ridertrainingtodaycount";

  private static final String RIDER_ON_ACTIVE_JOB_COUNT = "rideronactivejobcount";

  private static final String ALL_RIDERS_COUNT = "allriderscount";

  private Long authorizedCount;

  private Long unAuthorizedCount;

  private Long suspendedCount;

  private Long allRiderCount;

  private Long riderTrainingTodayCount;

  private Long riderOnActiveJobCount;

  public static RiderManagementDashBoardResponseDto of(
      Map<String, Long> riderStatusAggregatedCount) {

    final Long defaultCount = (long) 0;
    RiderManagementDashBoardResponseDto riderDashBoardResponseDto =

        RiderManagementDashBoardResponseDto.builder()
            .authorizedCount(riderStatusAggregatedCount.getOrDefault(RiderStatus.AUTHORIZED.name(),
                defaultCount))
            .unAuthorizedCount(riderStatusAggregatedCount
                .getOrDefault(RiderStatus.UNAUTHORIZED.name(), defaultCount))
            .suspendedCount(
                riderStatusAggregatedCount.getOrDefault(RiderStatus.SUSPENDED.name(), defaultCount))
            .allRiderCount(riderStatusAggregatedCount.getOrDefault(ALL_RIDERS_COUNT, defaultCount))
            .riderTrainingTodayCount(
                riderStatusAggregatedCount.getOrDefault(RIDER_TRAINING_TODAY_COUNT, defaultCount))
            .riderOnActiveJobCount(
                riderStatusAggregatedCount.getOrDefault(RIDER_ON_ACTIVE_JOB_COUNT, defaultCount))
            .build();
    return riderDashBoardResponseDto;
  }
}
