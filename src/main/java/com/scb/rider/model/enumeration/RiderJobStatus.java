package com.scb.rider.model.enumeration;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum RiderJobStatus {
    JOB_ACCEPTED,
    CALLED_MERCHANT,
    ARRIVED_AT_MERCHANT,
    MEAL_PICKED_UP,
    PARKING_RECEIPT_PHOTO,
    ARRIVED_AT_CUST_LOCATION,
    FOOD_DELIVERED,
    ORDER_CANCELLED_BY_OPERATOR;

    public static List<String> getActiveRiderJobStatuses() {
      List<String> riderActiveJobStatuses = Arrays.stream(RiderJobStatus.values())
          .filter(status -> !status.name().equals(RiderJobStatus.FOOD_DELIVERED.name()) &&
              !status.name().equals(RiderJobStatus.ORDER_CANCELLED_BY_OPERATOR.name()))
          .map(status -> status.name()).collect(Collectors.toList());
      return riderActiveJobStatuses;
    }

    public static List<String> getNotActiveRiderJobStatuses() {
        List<String> riderActiveJobStatuses = Arrays.stream(RiderJobStatus.values())
            .filter(status -> status.name().equals(RiderJobStatus.FOOD_DELIVERED.name()) &&
                status.name().equals(RiderJobStatus.ORDER_CANCELLED_BY_OPERATOR.name()))
            .map(status -> status.name()).collect(Collectors.toList());
        return riderActiveJobStatuses;
      }
    
    public static List<RiderJobStatus> getStatusForCompleteJobResponse() {
      return Arrays.asList(JOB_ACCEPTED);
    }
}
