package com.scb.rider.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.scb.rider.model.dto.RiderManagementDashBoardResponseDto;
import com.scb.rider.service.document.RiderManagementDashBoardService;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RiderManagementDashBoardControllerTest {

  @InjectMocks
  private RiderManagementDashBoardController riderManagementDashBoardController;

  @Mock
  private RiderManagementDashBoardService riderManagementDashBoardService;

  private static RiderManagementDashBoardResponseDto riderManagementDashBoardResponseDto;

  @BeforeAll
  static void setUp() {

    riderManagementDashBoardResponseDto =
        RiderManagementDashBoardResponseDto.builder().allRiderCount((long) 20)
            .authorizedCount((long) 5).authorizedCount((long) 14).suspendedCount((long) 1)
            .riderOnActiveJobCount((long) 10).riderTrainingTodayCount((long) 2).build();
  }

  @Test
  void getRiderProfileByTermTest() {

    when(riderManagementDashBoardService.getRiderManagementDashBoardSummary(anyString()))
        .thenReturn(riderManagementDashBoardResponseDto);

    ResponseEntity<RiderManagementDashBoardResponseDto> riderStatusSummaryDtoController =
        riderManagementDashBoardController.getRiderManagementDashBoardSummary();
    assertEquals(riderStatusSummaryDtoController.getBody(), riderManagementDashBoardResponseDto);
    assertTrue(ObjectUtils.isNotEmpty(riderStatusSummaryDtoController.getBody()));
    assertEquals(HttpStatus.OK, riderStatusSummaryDtoController.getStatusCode());
  }
}
