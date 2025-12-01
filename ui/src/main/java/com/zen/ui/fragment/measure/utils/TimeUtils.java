// file: com/zen/ui/fragment/measure/TimeUtils.java
package com.zen.ui.fragment.measure.utils;

import com.zen.api.Constant;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeUtils {

    private TimeUtils() { }

    public static String makeTraceId() {
        return format(new Date(), "yyyyMMddHHmmss");
    }

    public static String format(Date date, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.US);
        return sdf.format(date);
    }

    public static String formatDateTime(Date date) {
        return format(date, Constant.DateTimeFormat);
    }

    public static String formatDate(Date date) {
        return format(date, Constant.DateFormat);
    }

    public static String nowTimeLabel() {
        return format(new Date(), Constant.TimeFormat);
    }
}