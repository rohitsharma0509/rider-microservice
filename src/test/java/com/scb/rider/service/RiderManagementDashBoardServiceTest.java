package com.scb.rider.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.apache.commons.lang3.ObjectUtils;
import org.bson.Document;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import com.scb.rider.model.dto.RiderStatusAggregateCountDocument;
import com.scb.rider.model.dto.RiderManagementDashBoardResponseDto;
import com.scb.rider.model.enumeration.RiderStatus;
import com.scb.rider.repository.RiderProfileRepository;
import com.scb.rider.repository.RiderTrainingAppointmentRepository;
import com.scb.rider.service.document.RiderManagementDashBoardService;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
class RiderManagementDashBoardServiceTest {

  @Mock
  private RiderProfileRepository riderProfileRepository;

  @Mock
  private RiderTrainingAppointmentRepository riderTrainingAppointmentRepository;

  @InjectMocks
  private RiderManagementDashBoardService riderManagementDashBoardService;

  private static RiderStatusAggregateCountDocument riderStatusAggregateCountDocument;

  private static RiderStatusAggregateCountDocument riderStatusAggregateCountDocument2;


  @BeforeAll
  static void setUp() {

    riderStatusAggregateCountDocument = RiderStatusAggregateCountDocument.builder()
        .aggregateStatus(RiderStatus.UNAUTHORIZED.name()).riderCount((long) 20).build();

    riderStatusAggregateCountDocument2 = RiderStatusAggregateCountDocument.builder()
        .aggregateStatus(RiderStatus.AUTHORIZED.name()).riderCount((long) 20).build();
  }

  @Test
  void getRiderManagementDashBoardSummaryUnauthorizedCountTest() {

    AggregationResults<RiderStatusAggregateCountDocument> results =
        new AggregationResults<>(Collections.singletonList(riderStatusAggregateCountDocument), new Document("", ""));


    when(riderProfileRepository.groupByStatus()).thenReturn(results);

    when(riderProfileRepository.groupByRiderId()).thenReturn(results);


    when(riderProfileRepository
        .groupByRiderAvailabilityStatus(anyString())).thenReturn(results);

    when(riderTrainingAppointmentRepository.groupByTodaysDate(any())).thenReturn(results);

    RiderManagementDashBoardResponseDto responseDto =
        riderManagementDashBoardService.getRiderManagementDashBoardSummary("UUID");
    assertTrue(ObjectUtils.isNotEmpty(responseDto));
    assertNotNull(responseDto.getUnAuthorizedCount());
    assertEquals(responseDto.getUnAuthorizedCount(), 20);
  }


  @Test
  void getRiderManagementDashBoardSummaryAuthorizedCountTest() {

    AggregationResults<RiderStatusAggregateCountDocument> results =
        new AggregationResults<>(Collections.singletonList(riderStatusAggregateCountDocument2), new Document("", ""));


    when(riderProfileRepository.groupByStatus()).thenReturn(results);

    when(riderProfileRepository.groupByRiderId()).thenReturn(results);


    when(riderProfileRepository
        .groupByRiderAvailabilityStatus(anyString())).thenReturn(results);

    when(riderTrainingAppointmentRepository.groupByTodaysDate(any())).thenReturn(results);

    RiderManagementDashBoardResponseDto responseDto =
        riderManagementDashBoardService.getRiderManagementDashBoardSummary("UUID");
    assertTrue(ObjectUtils.isNotEmpty(responseDto));
    assertNotNull(responseDto.getAuthorizedCount());
    assertEquals(responseDto.getAuthorizedCount(), 20);
  }


}
