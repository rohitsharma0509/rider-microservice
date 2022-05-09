package com.scb.rider.util.excelhelper;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.List;

import static org.apache.poi.hssf.record.cf.BorderFormatting.BORDER_THIN;
import static org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND;


@Service
public class ExcelExportUtility{

    protected static SXSSFWorkbook wb;
    protected static Sheet sh;
    protected static final String EMPTY_VALUE = " ";


    protected static CellStyle getHeaderStyle() {
        CellStyle style = wb.createCellStyle();
        style.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setFillPattern(SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.valueOf(BORDER_THIN));
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderLeft(BorderStyle.valueOf(BORDER_THIN));
        style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderRight(BorderStyle.valueOf(BORDER_THIN));
        style.setRightBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderTop(BorderStyle.valueOf(BORDER_THIN));
        style.setTopBorderColor(IndexedColors.BLACK.getIndex());

        return style;
    }


    protected static CellStyle getNormalStyle() {
        CellStyle style = wb.createCellStyle();
        style.setBorderBottom(BorderStyle.valueOf(BORDER_THIN));
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderLeft(BorderStyle.valueOf(BORDER_THIN));
        style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderRight(BorderStyle.valueOf(BORDER_THIN));
        style.setRightBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderTop(BorderStyle.valueOf(BORDER_THIN));
        style.setTopBorderColor(IndexedColors.BLACK.getIndex());

        return style;
    }

    public static SXSSFWorkbook fillHeader(List<String> columns, String sheetName) {
        wb = new SXSSFWorkbook(100); // keep 100 rows in memory, exceeding rows will be flushed to disk
        sh = wb.createSheet(sheetName);
        CellStyle headerStyle = getHeaderStyle();

        for (int rownum = 0; rownum < 1; rownum++) {
            Row row = sh.createRow(rownum);

            for (int cellnum = 0; cellnum < columns.size(); cellnum++) {
                Cell cell = row.createCell(cellnum);
                cell.setCellValue(columns.get(cellnum));
                cell.setCellStyle(headerStyle);
            }

        }
        return wb;
    }

    public static <T> SXSSFWorkbook addData(List<T> dataList, Sheet sheet) throws IllegalAccessException {

        CellStyle normalStyle = getNormalStyle();
        Row row = null;
        Cell cell = null;
        int r = sheet.getLastRowNum() + 1;
        int c = 0;
        Class beanClass = dataList.get(0).getClass();
        for (T bean : dataList) {
            c = 0;
            row = sheet.createRow(r++);
            for (Field f : beanClass.getDeclaredFields()) {
                cell = row.createCell(c++);
                cell.setCellStyle(normalStyle);
                f.setAccessible(true);
                Object value = f.get(bean);
                if (value != null) {
                    if (value instanceof String) {
                        cell.setCellValue((String) value);
                    } else if (value instanceof Double) {
                        cell.setCellValue((Double) value);
                    } else if (value instanceof Integer) {
                        cell.setCellValue((Integer) value);
                    } else if (value instanceof java.util.Date) {
                        cell.setCellValue((java.util.Date) value);
                    } else if (value instanceof Boolean) {
                        cell.setCellValue((Boolean) value);
                    }
                }
            }
        }

        return wb;
    }

}