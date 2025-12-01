package com.zen.ui.fragment.measure.manager;

import android.content.SharedPreferences;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.zen.api.Constant;
import com.zen.api.MyApi;
import com.zen.api.data.DeviceSetting;
import com.zen.api.protocol.Measure;

import java.util.HashMap;
import java.util.Map;

public class AlarmManager {

    public static class AlarmState {
        public boolean lowAlarm;
        public boolean highAlarm;
        public int alarmId;  // 0 = error, 1 = normal, 2 = alarm
    }

    private static final String TAG = "AlarmManager";

    private final Map<String, Double> alarmLMap = new HashMap<>();
    private final Map<String, Double> alarmHMap = new HashMap<>();
    private final Map<String, Boolean> alarmLSwitchMap = new HashMap<>();
    private final Map<String, Boolean> alarmHSwitchMap = new HashMap<>();

    private boolean alarmL = false;
    private boolean alarmH = false;
    private boolean alarmDirty = false;

    private String highValueUnit = "";
    private String lowValueUnit = "";

    private double fullValue = 1.0;
    private double lowValue = 0.0;
    private int sweepAngle = 160;

    public void load() {
        SharedPreferences sp = MyApi.getInstance().getDataApi().getSetting();
        if (sp == null) return;

        int i = 0;
        for (String key : Constant.ALARMs) {
            String def = Constant.DEFAULT_ALARMs[i];
            String str = sp.getString(key, def);
            DeviceSetting.Param param = JSON.parseObject(str, DeviceSetting.Param.class);

            if (param == null) {
                alarmLMap.put(key, 0.0);
                alarmHMap.put(key, 0.0);
                alarmLSwitchMap.put(key, false);
                alarmHSwitchMap.put(key, false);
            } else {
                try {
                    String low = param.getString(Constant.lowValue);
                    String high = param.getString(Constant.highValue);
                    boolean lowSwitch = Constant.ON.equals(param.getString(Constant.lowSwitchValue));
                    boolean highSwitch = Constant.ON.equals(param.getString(Constant.highSwitchValue));

                    alarmLSwitchMap.put(key, lowSwitch);
                    alarmHSwitchMap.put(key, highSwitch);
                    alarmLMap.put(key, lowSwitch ? Double.parseDouble(low) : 0.0);
                    alarmHMap.put(key, highSwitch ? Double.parseDouble(high) : 0.0);
                } catch (Exception e) {
                    alarmLMap.put(key, 0.0);
                    alarmHMap.put(key, 0.0);
                    alarmLSwitchMap.put(key, false);
                    alarmHSwitchMap.put(key, false);
                }
            }
            i++;
        }
    }

    public void configDial(double fullValue, double lowValue, int sweepAngle,
                           String highUnit, String lowUnit) {
        this.fullValue = fullValue;
        this.lowValue = lowValue;
        this.sweepAngle = sweepAngle;
        this.highValueUnit = highUnit;
        this.lowValueUnit = lowUnit;
    }

    public AlarmState eval(int alarmIndex, double currentValue, double temp,
                           boolean autoSaveRunning) {
        AlarmState state = new AlarmState();
        try {
            String key = Constant.ALARMs[alarmIndex];

            double highVal = getAlarmValue(alarmHMap, key, currentValueUnit());
            double lowVal = getAlarmValue(alarmLMap, key, currentValueUnit());
            boolean highSwitch = getAlarmSwitch(alarmHSwitchMap, key);
            boolean lowSwitch = getAlarmSwitch(alarmLSwitchMap, key);

            double scaledCurrent = sweepAngle * currentValue / fullValue;

            boolean flagLow = lowSwitch && (scaledCurrent < lowVal);
            boolean flagHigh = highSwitch && (scaledCurrent > highVal);

            alarmDirty = alarmDirty || flagLow != alarmL || flagHigh != alarmH;

            if (alarmDirty) {
                byte s = 0;
                if (autoSaveRunning) s |= Measure.ContinuousMeasureON;
                if (flagLow) s |= Measure.LOW_ALARM_ON;
                if (flagHigh) s |= Measure.UPPER_ALARM_ON;

                MyApi.getInstance().getBtApi().sendCommand(new Measure(s));

                alarmL = flagLow;
                alarmH = flagHigh;
                alarmDirty = false;
            }

            state.lowAlarm = flagLow;
            state.highAlarm = flagHigh;
            state.alarmId = calcAlarmId(key, temp);
            return state;

        } catch (Exception e) {
            Log.e(TAG, "eval error", e);
            state.alarmId = 0;
            return state;
        }
    }

    private boolean getAlarmSwitch(Map<String, Boolean> map, String key) {
        Boolean v = map.get(key);
        return v != null && v;
    }

    private double getAlarmValue(Map<String, Double> map, String key, String currentUnit) {
        Double v = map.get(key);
        if (v == null) return 0;

        double value = v;
        // (Simplified version of your original unit-conversion logic)
        try {
            if ("ppm".equals(highValueUnit) || "ppm".equals(lowValueUnit)) {
                if ("ppm".equals(currentUnit)) {
                    return value;
                } else {
                    return value / 10.0;
                }
            }
            if ("ppt".equals(highValueUnit) || "ppt".equals(lowValueUnit)) {
                if ("ppm".equals(currentUnit)) {
                    return value * 1000.0;
                }
                return value * 10.0;
            }
            // fallback linear mapping with dial
            if (value > fullValue) {
                return sweepAngle * fullValue / fullValue;
            } else if (value < lowValue) {
                return sweepAngle * lowValue / fullValue;
            } else {
                return sweepAngle * value / fullValue;
            }
        } catch (Exception e) {
            return 0;
        }
    }

    private int calcAlarmId(String key, Double temp) {
        try {
            if (getRawAlarmValue(alarmHMap, key) == 0 && getRawAlarmValue(alarmLMap, key) == 0) {
                return 1;
            }
            if (alarmH || alarmL) {
                if (temp == null || temp == 0) {
                    return 1;
                } else {
                    return 2;
                }
            } else {
                return 1;
            }
        } catch (Exception e) {
            return 0;
        }
    }

    private double getRawAlarmValue(Map<String, Double> map, String key) {
        Double v = map.get(key);
        return v == null ? 0.0 : v;
    }

    private String currentValueUnit() {
        // This is a hook – you can inject currentUnit from outside if needed
        return "";
    }
}

