package com.scb.rider.util;

import com.scb.rider.exception.DataNotFoundException;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class LoggerUtils {

    private LoggerUtils() {}

    public static DataNotFoundException logError(Class<?> klass, String target, String targetName) {
        log.error("Record not found for class {} for {} {} ", klass.getSimpleName(), targetName, target);
        return new DataNotFoundException(String.format("Record not found for %s %s", targetName, target));
    }
}