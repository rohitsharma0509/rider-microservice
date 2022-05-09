package com.scb.rider.service;

import com.scb.rider.constants.Constants;
import com.scb.rider.model.document.RiderProfile;
import com.scb.rider.model.dto.ExcelAuthorizedRiderDetails;
import com.scb.rider.model.dto.ExcelRiderDetails;
import com.scb.rider.model.dto.ExcelTodayTrainingAttendeeDetails;
import com.scb.rider.model.dto.ExcelUnauthorizedRiderDetails;
import com.scb.rider.model.dto.RiderIdsOnTrainingOrActiveJobCount;
import com.scb.rider.model.enumeration.RiderJobStatus;
import com.scb.rider.model.enumeration.RiderStatus;
import com.scb.rider.repository.RiderJobDetailsRepository;
import com.scb.rider.repository.RiderProfileRepository;
import com.scb.rider.repository.RiderTrainingAppointmentRepository;
import com.scb.rider.util.excelhelper.ExcelCreator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RiderDownloadService {
    @Autowired
    private RiderProfileRepository riderProfileRepository;

    @Autowired
    private RiderTrainingAppointmentRepository riderTrainingAppointmentRepository;

    @Autowired
    private RiderJobDetailsRepository riderJobDetailsRepository;

    public SXSSFWorkbook getRiderDetailWorkbook(String status) throws Exception {
        log.info("Request received to generate generate excel for status {}", status);
        SXSSFWorkbook workbook = getWorkbook(status);
        List<String> riderIds = getRiderIdsForWorkingRidersOrWhoWillTrainedToday(status);

        Page<RiderProfile> page;
        Pageable pageable = PageRequest.of(0, Constants.PAGE_SIZE_10K, Sort.by(Constants.RIDER_ID));
        do {
            page = getDataForWorkbook(status, pageable, riderIds);
            if (!CollectionUtils.isEmpty(page.getContent())) {
                log.info("Exporting to excel for batch : {}", page.getNumber());
                workbook = addDataToWorkbook(workbook, page.getContent(), status);
                pageable = page.nextPageable();
            }
        } while (page.hasNext());
        log.info("Excel has been generated for status {}", status);
        return workbook;
    }

    private SXSSFWorkbook getWorkbook(String status) throws Exception {
        SXSSFWorkbook workbook;
        String sheetName = "sheet";
		if(StringUtils.isNotBlank(status) && RiderStatus.AUTHORIZED.name().equals(status.toUpperCase())) {
            workbook = ExcelCreator.excelCreator(ExcelAuthorizedRiderDetails.getHeaders(), sheetName);
        } else if(StringUtils.isNotBlank(status) && RiderStatus.UNAUTHORIZED.name().equals(status.toUpperCase())) {
            workbook = ExcelCreator.excelCreator(ExcelUnauthorizedRiderDetails.getHeaders(), sheetName);
        } else if (Constants.RIDER_ON_TRAINING_TODAY.equals(status)) {
            workbook = ExcelCreator.excelCreator(ExcelTodayTrainingAttendeeDetails.getHeaders(), sheetName);
        } else {
            workbook = ExcelCreator.excelCreator(ExcelRiderDetails.getHeaders(), sheetName);
        }
        return workbook;
    }

    private List<String> getRiderIdsForWorkingRidersOrWhoWillTrainedToday(String status) {
        List<String> riderIds = new ArrayList<>();
        if (Constants.RIDER_ON_TRAINING_TODAY.equals(status)) {
            LocalDate localDate = LocalDate.now(ZoneId.of(Constants.BKK_ZONE_ID));
            AggregationResults<RiderIdsOnTrainingOrActiveJobCount> aggResult = riderTrainingAppointmentRepository.getRiderIdgroupByTodaysDate(localDate);
            aggResult.getMappedResults().stream().forEach(riderObj -> riderIds.addAll(riderObj.getRiderIds()));
        } else if (Constants.RIDER_ON_ACTIVE_JOB.equals(status)) {
            AggregationResults<RiderIdsOnTrainingOrActiveJobCount> aggResult = riderJobDetailsRepository.getRiderIdgroupByActiveJob(RiderJobStatus.getActiveRiderJobStatuses());
            aggResult.getMappedResults().stream().forEach(riderObj -> riderIds.addAll(riderObj.getRiderIds()));
        }
        return riderIds;
    }

    private Page<RiderProfile> getDataForWorkbook(String status, Pageable pageable, List<String> riderIds) {
        Page<RiderProfile> page;
        if (StringUtils.isNotBlank(status) && Arrays.stream(RiderStatus.values()).anyMatch(riderStatus -> riderStatus.name().equals(status.toUpperCase()))) {
            RiderStatus riderStatus = RiderStatus.valueOf(status.toUpperCase());
            page = riderProfileRepository.findByStatus(riderStatus, pageable);
        } else if (StringUtils.isNotBlank(status) && (Constants.RIDER_ON_TRAINING_TODAY.equals(status) || Constants.RIDER_ON_ACTIVE_JOB.equals(status))) {
            page = riderProfileRepository.findByIdIn(riderIds, pageable);
        } else {
            page = riderProfileRepository.findAll(pageable);
        }
        return page;
    }

    private SXSSFWorkbook addDataToWorkbook(SXSSFWorkbook workbook, List<RiderProfile> riderProfiles, String status) throws IllegalAccessException {
        if(StringUtils.isNotBlank(status) && RiderStatus.AUTHORIZED.name().equals(status.toUpperCase())) {
            List<ExcelAuthorizedRiderDetails> riderList = riderProfiles.stream().map(ExcelAuthorizedRiderDetails::of).collect(Collectors.toList());
            workbook = ExcelCreator.addData(riderList, workbook);
        } else if(StringUtils.isNotBlank(status) && RiderStatus.UNAUTHORIZED.name().equals(status.toUpperCase())) {
            List<ExcelUnauthorizedRiderDetails> riderList = riderProfiles.stream().map(ExcelUnauthorizedRiderDetails::of).collect(Collectors.toList());
            workbook = ExcelCreator.addData(riderList, workbook);
        }  else if (Constants.RIDER_ON_TRAINING_TODAY.equals(status)) {
            List<ExcelTodayTrainingAttendeeDetails> riderList = riderProfiles.stream().map(ExcelTodayTrainingAttendeeDetails::of).collect(Collectors.toList());
            workbook = ExcelCreator.addData(riderList, workbook);
        } else {
            List<ExcelRiderDetails> excelRiderDetailsList = riderProfiles.stream().map(ExcelRiderDetails::createExcelRiderTierDetailEntity).collect(Collectors.toList());
            workbook = ExcelCreator.addData(excelRiderDetailsList, workbook);
        }
        return workbook;
    }
}
