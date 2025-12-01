package com.zen.ui.fragment.measure.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class FormatUtils {

    private FormatUtils() { }

    public static String formatDouble(double value, int digits) {
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(digits, RoundingMode.HALF_UP);
        return bd.toPlainString();
    }
}