package com.scb.rider.util;

import com.scb.rider.constants.Constants;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class DateFormatterUtilsTest {

    @Test
    void testZonedDateTimeToString() {
        ZonedDateTime zonedDateTime = ZonedDateTime.parse("2011-12-03T10:15:30+01:00");
        String dateTime = DateFormatterUtils.zonedDateTimeToString(zonedDateTime);
        assertNotNull(dateTime);
        assertEquals("2011-12-03T10:15:30+01:00",dateTime);
    }

    @Test
    void testGetFormattedDateFromDateStringForNull() {
        String result = DateFormatterUtils.getFormattedDateFromDateString(null, Constants.YYYY_MM_DD, Constants.DATE_WITH_SHORT_MONTH_NAME);
        assertEquals(StringUtils.EMPTY, result);
    }

    @Test
    void testGetFormattedDateFromDateString() {
        String result = DateFormatterUtils.getFormattedDateFromDateString("2021-05-28", Constants.YYYY_MM_DD, Constants.DATE_WITH_SHORT_MONTH_NAME);
        assertNotNull(result);
    }

    @Test
    void testGetFormattedDateTimeFromDateTimeStringForNull() {
        String result = DateFormatterUtils.getFormattedDateTimeFromDateTimeString(null, Constants.DATETIME_FULL, Constants.DATE_WITH_SHORT_MONTH_NAME);
        assertEquals(StringUtils.EMPTY, result);
    }

    @Test
    void testGetFormattedDateTimeFromDateTimeString() {
        String result = DateFormatterUtils.getFormattedDateTimeFromDateTimeString("2021-05-28T13:15:42.827+0700", Constants.DATETIME_FULL, Constants.DATE_WITH_SHORT_MONTH_NAME);
        assertNotNull(result);
    }

    @Test
    void testGetFormattedDateTimeForNull() {
        String result = DateFormatterUtils.getFormattedDateTime(null, Constants.DATE_WITH_SHORT_MONTH_NAME);
        assertEquals(StringUtils.EMPTY, result);
    }

    @Test
    void testGetFormattedDateTime() {
        String result = DateFormatterUtils.getFormattedDateTime(LocalDateTime.now(), Constants.DATE_WITH_SHORT_MONTH_NAME);
        assertNotNull(result);
    }

    @Test
    void testGetFormattedDateForNull() {
        String result = DateFormatterUtils.getFormattedDate(null, Constants.DATE_WITH_SHORT_MONTH_NAME);
        assertEquals(StringUtils.EMPTY, result);
    }

    @Test
    void testGetFormattedDate() {
        String result = DateFormatterUtils.getFormattedDate(LocalDate.now(), Constants.DATE_WITH_SHORT_MONTH_NAME);
        assertNotNull(result);
    }
}
