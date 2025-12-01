package com.zen.api.protocol.velaprotocal;

import com.zen.api.protocol.Data;
import okio.ByteString;

/**
 * VelaMeasureDataPack
 * A manual data holder that inherits from Data.
 * - Stores scaled values as doubles: value, temp
 * - No setters; initialize via constructors
 * - Getters return stored values directly (no computation)
 * - Matches VelaDataInfo behavior for ORP and unit string preference
 */
public class VelaMeasureDataPack extends Data {

    // --- for parity with VelaDataInfo.getORP() (Data compares to mV) ---
    private static final int ORP = 0x06;

    // --- core already-scaled values ---
    private final double value; // main measurement (already scaled)
    private final double temp;  // temperature in °C (already scaled)

    // --- metadata & mode/units ---
    private final int mode;        // pH/mV/Cond/TDS/Salt/Res/ORP
    private final int unit;        // legacy Data unit (UNIT_*)
    private final int unit2;       // temp unit (keep UNIT_C for V2)
    private final int pointDigit;  // display-only decimals (no effect on value)
    private final int pointDigit2; // display-only decimals for temp

    // prefer V2 unit table first (0..15); fallback to legacy unit when  invalid
    private final int unitV2;      // 0..15 or <0 if unknown
    private final int unitCompat;  // legacy mapping (UNIT_*), used as fallback

    // --- flags (placeholders; V2 doesn’t provide them, but we keep for API parity) ---
    private final boolean upperAlarm;
    private final boolean lowerAlarm;
    private final boolean hold;
    private final boolean laughFace;
    private final boolean H, M, L;

    // --- timestamp (copied from VelaDataInfo) ---
    private final int year, month, day, hour, minute, second;

    // --- optional raw bytes for getData() parity ---
    private final byte[] raw;

    // ---------- Constructors (no setters) ----------

    /**
     * Full constructor (call this from VelaDataInfo.toMeasureDataPack()).
     * No validation here—callers are expected to pass correct values.
     */
    public VelaMeasureDataPack(
            double value,
            double temp,
            int mode,
            int unit,
            int unit2,
            int pointDigit,
            int pointDigit2,
            int unitV2,
            int unitCompat,
            boolean upperAlarm,
            boolean lowerAlarm,
            boolean hold,
            boolean laughFace,
            boolean H,
            boolean M,
            boolean L,
            int year, int month, int day,
            int hour, int minute, int second,
            byte[] raw
    ) {
        this.value = value;
        this.temp = temp;
        this.mode = mode;
        this.unit = unit;
        this.unit2 = unit2;
        this.pointDigit = pointDigit;
        this.pointDigit2 = pointDigit2;
        this.unitV2 = unitV2;
        this.unitCompat = unitCompat;
        this.upperAlarm = upperAlarm;
        this.lowerAlarm = lowerAlarm;
        this.hold = hold;
        this.laughFace = laughFace;
        this.H = H;
        this.M = M;
        this.L = L;
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
        this.raw = (raw == null ? new byte[0] : raw.clone());
    }

    // ---------- Overrides from Data (no computation in getValue/getTemp) ----------

    @Override
    public double getValue() { return value; }

    @Override
    public double getTemp() { return temp; }

    @Override
    public double getCond() { return mode == Cond ? value : 0.0; }

    @Override
    public double getPH() { return mode == pH ? value : 0.0; }

    // Align with VelaDataInfo: ORP is its own mode (0x06), not mV
    @Override
    public double getORP() { return mode == ORP ? value : 0.0; }

    @Override
    public double getTDS() { return mode == TDS ? value : 0.0; }

    @Override
    public double getSalinity() { return mode == Salt ? value : 0.0; }

    @Override
    public double getResistivity() { return mode == Res ? value : 0.0; }

    @Override
    public int getMode() { return mode; }

    @Override
    public int getPointDigit() { return pointDigit; }

    @Override
    public int getPointDigit2() { return pointDigit2; }

    // keep API parity: value2 is the same as temp; int form uses display digits
    @Override
    public double getValue2() { return temp; }

    @Override
    public int getIntValue2() {
        return (int) Math.round(temp * Math.pow(10, Math.max(0, pointDigit2)));
    }

    @Override
    public int getUnit2() { return unit2; }

    @Override
    public boolean isUpperAlarm() { return upperAlarm; }

    @Override
    public boolean isLowerAlarm() { return lowerAlarm; }

    @Override
    public boolean isHold() { return hold; }

    @Override
    public boolean isLaughFace() { return laughFace; }

    @Override
    public boolean isH() { return H; }

    @Override
    public boolean isM() { return M; }

    @Override
    public boolean isL() { return L; }

    // Prefer V2 mapping first (like VelaDataInfo), fallback to legacy Data units
    @Override
    public String getUnitString() {
        switch (unitV2) {
            case 0:  return "mol";
            case 1:  return "pX";
            case 2:  return "pH";
            case 3:  return "mV";
            case 4:  return "mS";
            case 5:  return "µS";
            case 6:  return "g/L";
            case 7:  return "mg/L";
            case 8:  return "µg/L";
            case 9:  return "ppt";
            case 10: return "ppm";
            case 11: return "ppb";
            case 12: return "Ω";
            case 13: return "KΩ";
            case 14: return "MΩ";
            case 15: return "%";
            default:
                switch (unitCompat != 0 ? unitCompat : unit) {
                    case UNIT_pH:  return "pH";
                    case UNIT_mV:  return "mV";
                    case UNIT_mS:  return "mS";
                    case UNIT_uS:  return "µS";
                    case UNIT_ppm: return "ppm";
                    case UNIT_ppt: return "ppt";
                    case UNIT_gl:  return "g/L";
                    case UNIT_mgl: return "mg/L";
                    case UNIT_Ω:   return "Ω";
                    case UNIT_KΩ:  return "KΩ";
                    case UNIT_MΩ:  return "MΩ";
                    default:       return "";
                }
        }
    }

    @Override
    public String getData() {
        return (raw == null || raw.length == 0) ? "" : ByteString.of(raw).hex();
    }

    // ---------- Timestamp getters ----------
    public int getYear() { return year; }
    public int getMonth() { return month; }
    public int getDay() { return day; }
    public int getHour() { return hour; }
    public int getMinute() { return minute; }
    public int getSecond() { return second; }

    @Override
    public String toString() {
        return "VelaMeasureDataPack{" +
                "value=" + value +
                ", unit='" + getUnitString() + '\'' +
                ", temp=" + temp + "°C" +
                ", mode=" + mode +
                ", ts=" + String.format("%04d-%02d-%02d %02d:%02d:%02d",
                year, month, day, hour, minute, second) +
                '}';
    }
}