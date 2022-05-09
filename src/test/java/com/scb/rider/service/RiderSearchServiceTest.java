package com.scb.rider.service;

import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.dto.*;
import com.scb.rider.model.enumeration.RiderProfileStage;
import com.scb.rider.model.enumeration.RiderStatus;
import com.scb.rider.repository.RiderJobDetailsRepository;
import com.scb.rider.repository.RiderSearchRepository;
import com.scb.rider.repository.RiderTrainingAppointmentRepository;
import com.scb.rider.service.document.RiderSearchService;
import org.apache.commons.lang3.ObjectUtils;
import org.bson.Document;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@ActiveProfiles(value = "test")
class RiderSearchServiceTest {

  @Mock
  private RiderSearchRepository riderSearchRepository;

  @Mock
  private RiderJobDetailsRepository riderJobDetailsRepository;

  @Mock
  private RiderTrainingAppointmentRepository riderTrainingAppointmentRepository;

  @InjectMocks
  private RiderSearchService riderSearchService;

  private final static String userId = "av3d";

  private static RiderProfile riderProfile;

  private static RiderProfileDto riderProfileDto;

  private static SearchResponseDto searchResponseDto;

  private static RiderSearchProfileDto riderSearchProfileDto;

  private static List<RiderProfile> riderProfileList;

  private static Page<RiderProfile> riderProfilePaged;

  private static Pageable pageable;

  private static List<String> riderIds = new ArrayList<>();

  private static RiderIdsOnTrainingOrActiveJobCount riderIdsOnTrainingOrActiveJobCount;

  @BeforeAll
  static void setUp() {
    riderIds.add("RR1011");
    riderProfileList = new ArrayList<>();
    AddressDto addressDto = AddressDto.builder().city("Bangkok").country("Thailand")
        .countryCode("TH").district("district").floorNumber("1234").landmark("landmark")
        .state("state").unitNumber("unitNumber").village("village").zipCode("203205").build();

    riderProfileDto = RiderProfileDto.builder().riderId(userId).id(userId).riderId(userId)
        .accountNumber("121212121212121").address(addressDto).consentAcceptFlag(true)
        .dataSharedFlag(true).firstName("John").lastName("Smith").nationalID("1234567890")
        .dob("20/12/1988").phoneNumber("9999999999").status(RiderStatus.UNAUTHORIZED)
            .profileStage(RiderProfileStage.STAGE_1).isReadyForAuthorization(Boolean.FALSE.toString()).build();
    riderProfile = new RiderProfile();
    BeanUtils.copyProperties(riderProfileDto, riderProfile);
    riderProfileList.add(riderProfile);

    riderProfileDto = RiderProfileDto.builder().riderId(userId).id(userId)
        .accountNumber("121212121212121").address(addressDto).consentAcceptFlag(true)
        .dataSharedFlag(true).firstName("Steven").lastName("Smith").nationalID("1234567890")
        .dob("20/12/1988").phoneNumber("9999999999").status(RiderStatus.AUTHORIZED)
            .profileStage(RiderProfileStage.STAGE_3).isReadyForAuthorization(Boolean.TRUE.toString()).build();
    riderProfile = new RiderProfile();
    BeanUtils.copyProperties(riderProfileDto, riderProfile);
    riderProfileList.add(riderProfile);

    riderProfileDto = RiderProfileDto.builder().riderId(userId).id(userId)
            .accountNumber("121212121212121").address(addressDto).consentAcceptFlag(true)
            .dataSharedFlag(true).firstName("Rohit").lastName("Sharma").nationalID("1234567891")
            .dob("20/12/1988").phoneNumber("9999999998").status(RiderStatus.UNAUTHORIZED)
            .profileStage(RiderProfileStage.STAGE_2).isReadyForAuthorization(Boolean.TRUE.toString()).build();
    riderProfile = new RiderProfile();
    BeanUtils.copyProperties(riderProfileDto, riderProfile);
    riderProfileList.add(riderProfile);

    riderProfilePaged = new PageImpl(riderProfileList);
    List<RiderSearchProfileDto> riderSearchProfileDtoList = new ArrayList<>();
    riderSearchProfileDtoList.add(RiderSearchProfileDto.builder().riderId(userId).id(userId)
        .firstName("John").lastName("Smith").Name("John Smith").phoneNumber("9999999999")
        .status(RiderStatus.UNAUTHORIZED).isReadyForAuthorization(Boolean.FALSE).build());

    riderSearchProfileDtoList.add(RiderSearchProfileDto.builder().riderId(userId).id(userId)
        .firstName("Steven").lastName("Smith").Name("Steven Smith").phoneNumber("9999999999")
        .status(RiderStatus.AUTHORIZED).isReadyForAuthorization(Boolean.TRUE).build());

    riderSearchProfileDtoList.add(RiderSearchProfileDto.builder().riderId(userId).id(userId)
            .firstName("Rohit").lastName("Sharma").Name("Rohit Sharma").phoneNumber("9999999998")
            .status(RiderStatus.UNAUTHORIZED).isReadyForAuthorization(Boolean.TRUE).build());

    searchResponseDto = SearchResponseDto.of(riderSearchProfileDtoList, 1, 3, 1);

    pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "firstName"));


    riderIdsOnTrainingOrActiveJobCount =
        RiderIdsOnTrainingOrActiveJobCount.builder().riderIds(riderIds).build();
  }

  @Test
  void getRiderProfileBySearchTermTest() {


    when(riderSearchRepository.findRiderByTerm(anyString(), anyString(), anyString(), anyString(),
        anyString(), any())).thenReturn(riderProfilePaged);

    SearchResponseDto searchedProfile = riderSearchService
        .getRiderProfileBySearchTermWithFilterQuery("Test", new ArrayList<>(), pageable);

    assertTrue(ObjectUtils.isNotEmpty(searchedProfile));
    assertEquals(searchedProfile, searchResponseDto);

  }

  @Test
  void getRiderProfileById() {

    RiderProfile riderProfile = Mockito.mock(RiderProfile.class);
    Optional<RiderProfile> riderProfile1 = Optional.of(riderProfile);
    when(riderSearchRepository.findByIdAndPhoneNumber(anyString(),anyString())).thenReturn(riderProfile1);

    ReflectionTestUtils.setField(riderSearchService,"prefix","RR");
    Optional<RiderProfile> searchedProfile = riderSearchService
            .findRiderProfileByRiderIdAndPhoneNumber("1212","237");

    assertTrue(ObjectUtils.isNotEmpty(searchedProfile));

  }
  @Test
  void getRiderProfileByRRId() {

    RiderProfile riderProfile = Mockito.mock(RiderProfile.class);
    Optional<RiderProfile> riderProfile1 = Optional.of(riderProfile);
    when(riderSearchRepository.findByIdAndPhoneNumber(anyString(),anyString())).thenReturn(riderProfile1);

    ReflectionTestUtils.setField(riderSearchService,"prefix","RR");
    Optional<RiderProfile> searchedProfile = riderSearchService
            .findRiderProfileByRiderIdAndPhoneNumber("RR1212","237");

    assertTrue(ObjectUtils.isNotEmpty(searchedProfile));

  }

  @Test
  void testGetReadyForAuthorizationForReady() {

    Map<String, String> filtersQuery = new HashMap<>();
    filtersQuery.put("isReadyForAuthorization","ready");

    String test = riderSearchService
            .getReadyForAuthorizedString(filtersQuery);

    assertTrue(ObjectUtils.isNotEmpty(test));
    assertEquals("true", test);

  }
  @Test
  void testGetReadyForAuthorizationForNotReady() {

    Map<String, String> filtersQuery = new HashMap<>();
    filtersQuery.put("isReadyForAuthorization","not ready yet");

    String test = riderSearchService
            .getReadyForAuthorizedString(filtersQuery);

    assertTrue(ObjectUtils.isNotEmpty(test));
    assertEquals("false", test);

  }

  @ParameterizedTest
  @ValueSource(strings = {"viewby:unAuthorized", "viewby:authorized", "viewby:allRiders", "viewby:suspended"})
  void getAllRidersProfileByNameFieldSearchTest(String filter) {

    List<String> filters = new ArrayList<>();
    filters.add(filter);

    when(riderSearchRepository.getRidersByStatusAndQueryOnFields(anyString(), anyString(),
        anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), any()))
            .thenReturn(riderProfilePaged);

    SearchResponseDto searchedProfile =
        riderSearchService.getRiderProfileBySearchTermWithFilterQuery("", filters, pageable);

    assertTrue(ObjectUtils.isNotEmpty(searchedProfile));
    assertEquals(searchedProfile, searchResponseDto);

  }


  @ParameterizedTest
  @ValueSource(strings = {"viewby:riderOnTrainingToday"})
  void getAllRidersProfileByOnTrainingOnActiveJobByNameFieldSearchTest(String filter) {

    List<String> filters = new ArrayList<>();
    filters.add(filter);

    AggregationResults<RiderIdsOnTrainingOrActiveJobCount> results =
        new AggregationResults<>(
            Collections.singletonList(riderIdsOnTrainingOrActiveJobCount), new Document("", ""));

    when(riderTrainingAppointmentRepository.getRiderIdgroupByTodaysDate(any())).thenReturn(results);

    when(riderSearchRepository.getRidersByRiderIdsAndQueryOnFields(anyString(), anyString(),
        anyString(), anyString(), anyString(), any(), anyString(), anyString(), anyString(), anyString(), anyString(), any()))
            .thenReturn(riderProfilePaged);

    SearchResponseDto searchedProfile =
        riderSearchService.getRiderProfileBySearchTermWithFilterQuery("", filters, pageable);

    assertTrue(ObjectUtils.isNotEmpty(searchedProfile));
    assertEquals(searchedProfile, searchResponseDto);

  }


  @Test
  void getRiderProfileByAsecdingSortTest() {

    pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "firstName"));
    when(riderSearchRepository.findRiderByTerm(anyString(), anyString(), anyString(), anyString(), anyString(), any()))
        .thenReturn(riderProfilePaged);

    SearchResponseDto searchedProfile = riderSearchService
        .getRiderProfileBySearchTermWithFilterQuery("Test", new ArrayList<>(), pageable);

    assertTrue(ObjectUtils.isNotEmpty(searchedProfile));
    assertEquals(searchedProfile, searchResponseDto);

  }
}
