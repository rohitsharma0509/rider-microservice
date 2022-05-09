package com.scb.rider.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.dto.RiderSearchProfileDto;
import com.scb.rider.model.dto.SearchResponseDto;
import com.scb.rider.model.enumeration.RiderStatus;
import com.scb.rider.service.document.RiderSearchService;


@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RiderSearchControllerTest {

  @InjectMocks
  private RiderSearchController riderProfileController;

  @Mock
  private RiderSearchService riderSearchService;

  private final static String userId = "av3d";

  private static RiderProfile riderProfile;

  private static SearchResponseDto searchResponseDto;

  private static RiderSearchProfileDto riderSearchProfileDto;

  private static List<String> riderIdList;

  private static Pageable pageable;

  @BeforeAll
  static void setUp() {

    List<RiderSearchProfileDto> riderSearchProfileDtoList = new ArrayList<RiderSearchProfileDto>();
    riderSearchProfileDtoList.add(RiderSearchProfileDto.builder().riderId(userId).id(userId)
        .firstName("John").lastName("Smith").status(RiderStatus.AUTHORIZED).tierId(1).tierName("Hero").build());

    riderSearchProfileDtoList.add(RiderSearchProfileDto.builder().riderId(userId).id(userId)
        .firstName("Steven").lastName("Smith").status(RiderStatus.AUTHORIZED).tierId(1).tierName("Hero").build());

    searchResponseDto = SearchResponseDto.of(riderSearchProfileDtoList, 1, 2, 1);

    pageable = PageRequest.of(0, 5);
  }

  @Test
  void getRiderProfileByTermTest() {

    when(riderSearchService.getRiderProfileBySearchTermWithFilterQuery("John",
        new ArrayList<String>(), pageable)).thenReturn(searchResponseDto);

    ResponseEntity<SearchResponseDto> searchResponseDtoController = riderProfileController
        .getRiderProfileBySearchTerm("John", new ArrayList<String>(), pageable);
    assertEquals(searchResponseDtoController.getBody(), searchResponseDto);
    assertTrue(ObjectUtils.isNotEmpty(searchResponseDtoController.getBody()));
    assertEquals(HttpStatus.OK, searchResponseDtoController.getStatusCode());
  }
}
