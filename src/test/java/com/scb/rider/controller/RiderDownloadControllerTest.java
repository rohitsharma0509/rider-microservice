package com.scb.rider.controller;

import com.scb.rider.constants.Constants;
import com.scb.rider.service.RiderDownloadService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@RunWith(MockitoJUnitRunner.class)
public class RiderDownloadControllerTest {

    @Mock
    HttpServletResponse httpServletResponse;

    @InjectMocks
    private RiderDownloadController riderDownloadController;

    @Mock
    private RiderDownloadService riderDownloadService;

    @Test
    public void getRiderTierDetailsForDownloadTest() throws Exception {
        when(riderDownloadService.getRiderDetailWorkbook(Constants.RIDER_ON_ACTIVE_JOB)).thenReturn(getMockWorkbook());
        StreamingResponseBody response = riderDownloadController.exportRiderDetailsToExcel(Constants.RIDER_ON_ACTIVE_JOB, httpServletResponse);
        assertTrue(ObjectUtils.isNotEmpty(response));
    }

    private SXSSFWorkbook getMockWorkbook() {
        SXSSFWorkbook workbook = new SXSSFWorkbook(100);
        Sheet sheet = workbook.createSheet("Test Sheet");
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        cell.setCellValue("Test Value");
        return workbook;
    }

}
