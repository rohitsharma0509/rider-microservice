package com.scb.rider.model.enumeration;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum SmsServiceStatus {
    ENABLED, DISABLED, RESTRICTED;

    public static List<String> list() {
        return Arrays.stream(SmsServiceStatus.values()).map(SmsServiceStatus::name)
                .collect(Collectors.toList());
    }
}
