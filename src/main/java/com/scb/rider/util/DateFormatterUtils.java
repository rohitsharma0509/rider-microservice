package com.scb.rider.util;

import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class DateFormatterUtils {
    private DateFormatterUtils(){}
    public static  final  String PATTERN ="yyyy-MM-dd'T'HH:mm:ssXXX";
    public static String zonedDateTimeToString(ZonedDateTime zonedDateTime) {
      return zonedDateTimeToString(zonedDateTime, PATTERN);
    }

    public static String zonedDateTimeToString(ZonedDateTime zonedDateTime, String dateFormat) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
        return zonedDateTime.format(formatter);
    }

    public static String getFormattedDateFromDateString(String dateTimeStr, String fromFormat, String toFormat) {
        if (StringUtils.isEmpty(dateTimeStr)) {
            return StringUtils.EMPTY;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(fromFormat);
        LocalDate localDate = LocalDate.parse(dateTimeStr, formatter);
        return getFormattedDate(localDate, toFormat);
    }

    public static String getFormattedDateTimeFromDateTimeString(String dateTimeStr, String fromFormat, String toFormat) {
        if(StringUtils.isEmpty(dateTimeStr)) {
            return StringUtils.EMPTY;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(fromFormat);
        LocalDateTime localDateTime = LocalDateTime.parse(dateTimeStr, formatter);
        return getFormattedDateTime(localDateTime, toFormat);
    }

    public static String getFormattedDateTime(LocalDateTime localDateTime, String format) {
        if(Objects.isNull(localDateTime)) {
            return StringUtils.EMPTY;
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(format);
        return dtf.format(localDateTime);
    }

    public static String getFormattedDate(LocalDate localDate, String format) {
        if(Objects.isNull(localDate)) {
            return StringUtils.EMPTY;
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(format);
        return dtf.format(localDate);
    }

    public static String getFormattedDateTimeWithZoneId(LocalDateTime localDateTime) {
        if(Objects.isNull(localDateTime)) {
            return StringUtils.EMPTY;
        }
        ZonedDateTime zonedBangkok = localDateTime.atZone(ZoneId.of("Asia/Bangkok"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

        return zonedBangkok.format(formatter);
    }
}
