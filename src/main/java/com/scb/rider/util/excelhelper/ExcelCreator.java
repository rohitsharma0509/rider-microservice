package com.scb.rider.util.excelhelper;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.util.List;

public class ExcelCreator {
    private ExcelCreator() {}

    public static SXSSFWorkbook excelCreator(List<String> tierConfig, String sheetName) throws Exception {
        return ExcelExportUtility.fillHeader(tierConfig, sheetName);
    }

    public static SXSSFWorkbook addData(List<?> data, SXSSFWorkbook workbook) throws IllegalAccessException {
        Sheet sheet = workbook.getSheetAt(workbook.getActiveSheetIndex());
        return ExcelExportUtility.addData(data, sheet);
    }
}
