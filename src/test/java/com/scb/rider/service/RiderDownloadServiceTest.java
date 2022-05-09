package com.scb.rider.service;

import com.scb.rider.constants.Constants;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.dto.RiderIdsOnTrainingOrActiveJobCount;
import com.scb.rider.model.enumeration.RiderStatus;
import com.scb.rider.repository.RiderJobDetailsRepository;
import com.scb.rider.repository.RiderProfileRepository;
import com.scb.rider.repository.RiderTrainingAppointmentRepository;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RiderDownloadServiceTest {

    private static final String RIDER_ID = "RR0001";
    private static final String TEST = "test";

    @Mock
    private RiderProfileRepository riderProfileRepository;

    @Mock
    private RiderTrainingAppointmentRepository riderTrainingAppointmentRepository;

    @Mock
    private RiderJobDetailsRepository riderJobDetailsRepository;

    @InjectMocks
    private RiderDownloadService riderDownloadService;

    @Test
    void getRiderTierDetailsForRiderWhoWillAttendTrainingTodayTest() throws Exception {
        RiderIdsOnTrainingOrActiveJobCount riderIdsOnTraining = RiderIdsOnTrainingOrActiveJobCount.builder().riderIds(Arrays.asList(RIDER_ID)).build();
        List<RiderIdsOnTrainingOrActiveJobCount> mappedResults = Arrays.asList(riderIdsOnTraining);
        AggregationResults<RiderIdsOnTrainingOrActiveJobCount> aggResults = new AggregationResults<>(mappedResults, new Document());
        when(riderTrainingAppointmentRepository.getRiderIdgroupByTodaysDate(any(LocalDate.class))).thenReturn(aggResults);
        when(riderProfileRepository.findByIdIn(anyList(), any(Pageable.class))).thenReturn(getRiderDetails());
        SXSSFWorkbook response = riderDownloadService.getRiderDetailWorkbook(Constants.RIDER_ON_TRAINING_TODAY);
        assertNotNull(response);
    }

    @Test
    void getRiderTierDetailsForWorkingRiderTest() throws Exception {
        RiderIdsOnTrainingOrActiveJobCount riderIdsActiveJob = RiderIdsOnTrainingOrActiveJobCount.builder().riderIds(Arrays.asList(RIDER_ID)).build();
        List<RiderIdsOnTrainingOrActiveJobCount> mappedResults = Arrays.asList(riderIdsActiveJob);
        AggregationResults<RiderIdsOnTrainingOrActiveJobCount> aggResults = new AggregationResults<>(mappedResults, new Document());
        when(riderJobDetailsRepository.getRiderIdgroupByActiveJob(anyList())).thenReturn(aggResults);
        when(riderProfileRepository.findByIdIn(anyList(), any(Pageable.class))).thenReturn(getRiderDetails());
        SXSSFWorkbook response = riderDownloadService.getRiderDetailWorkbook(Constants.RIDER_ON_ACTIVE_JOB);
        assertNotNull(response);
    }

    @Test
    void getRiderTierDetailsForAuthorizedRidersTest() throws Exception {
        when(riderProfileRepository.findByStatus(any(RiderStatus.class), any(Pageable.class))).thenReturn(getRiderDetails());
        SXSSFWorkbook response = riderDownloadService.getRiderDetailWorkbook(RiderStatus.AUTHORIZED.name());
        assertNotNull(response);
    }

    @Test
    void getRiderTierDetailsForUnAuthorizedRidersTest() throws Exception {
        when(riderProfileRepository.findByStatus(any(RiderStatus.class), any(Pageable.class))).thenReturn(getRiderDetails());
        SXSSFWorkbook response = riderDownloadService.getRiderDetailWorkbook(RiderStatus.UNAUTHORIZED.name());
        assertNotNull(response);
    }

    @Test
    void getRiderTierDetailsForAllRidersTest() throws Exception {
        when(riderProfileRepository.findAll(any(Pageable.class))).thenReturn(getRiderDetails());
        SXSSFWorkbook response = riderDownloadService.getRiderDetailWorkbook(Constants.ALL_RIDERS);
        assertNotNull(response);
    }

    private Page<RiderProfile> getRiderDetails() {
        List<RiderProfile> list = new ArrayList<>();
        RiderProfile rider = new RiderProfile();
        rider.setRiderId(RIDER_ID);
        rider.setFirstName(TEST);
        rider.setLastName(TEST);
        list.add(rider);
        return new PageImpl<>(list);
    }
}
