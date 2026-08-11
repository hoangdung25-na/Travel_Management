package com.travel.common.core.helper;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class DateTimeUtils {
    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private DateTimeUtils() {
    }

    public static String formatInstant(Instant instant) {
        if (instant == null) {
            return null;
        }
        return DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT)
                .withZone(ZoneId.systemDefault())
                .format(instant);
    }
}
