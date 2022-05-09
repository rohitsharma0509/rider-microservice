package com.scb.rider.controller;

import com.scb.rider.constants.Constants;
import com.scb.rider.exception.ExcelProcessingException;
import com.scb.rider.service.RiderDownloadService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Log4j2
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
@Api(value = "Rider Tier Endpoints")
public class RiderDownloadController {

    @Autowired
    private RiderDownloadService riderDownloadService;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_hh_mm_ss");
    @ApiOperation(nickname = "get-rider-tier-details",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE,
            value = "Gets Rider Tier Details")

    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @GetMapping(value = "/rider/download")
    public StreamingResponseBody exportRiderDetailsToExcel(
            @RequestParam(required = false) String status,
            final HttpServletResponse response) {

        return outStream -> {
            ZonedDateTime currentETime = ZonedDateTime.now(ZoneId.of(Constants.BKK_ZONE_ID));
            String excelFileName = "Rider_Details" + formatter.format(currentETime) + ".xlsx";
            try {
                SXSSFWorkbook wb = riderDownloadService.getRiderDetailWorkbook(status);
                ByteArrayOutputStream outByteStream = new ByteArrayOutputStream();
                wb.write(outByteStream);
                byte[] outArray = outByteStream.toByteArray();

                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                response.setContentLength(outArray.length);
                response.setHeader("Expires:", "0"); // eliminates browser caching
                response.setHeader("Content-Disposition", "attachment; filename=" + excelFileName);
                OutputStream newOutputStream = response.getOutputStream();
                newOutputStream.write(outArray);
                newOutputStream.flush();
                wb.dispose();
                wb.close();
            } catch (Exception e) {
                log.error(e.getMessage());
                throw new ExcelProcessingException(e.getMessage());
            }
        };
    }
}
