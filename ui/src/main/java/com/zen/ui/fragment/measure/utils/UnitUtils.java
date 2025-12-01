package com.zen.ui.fragment.measure.utils;

import com.zen.api.protocol.Data;

public class UnitUtils {

    private UnitUtils() { }

    public static String tempUnitString(int unit2) {
        switch (unit2) {
            case Data.UNIT_C:
                return "C";
            case Data.UNIT_F:
                return "F";
            default:
                return "";
        }
    }
}