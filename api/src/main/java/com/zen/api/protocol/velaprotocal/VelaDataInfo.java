package com.zen.api.protocol.velaprotocal;

import android.util.Log;

import com.zen.api.protocol.Convent;

import java.util.Arrays;

/**
 * New realtime data frame (code 0x44, 'D')
 *
 * Layout (bytes are Data0..Data8):
 *  Data0 = 'D' (0x44)
 *  (Data1<<8 | Data2):
 *      bits15..10: (year_last2 - 20)  -> year = 2000 + 20 + value
 *      bits9 ..5 : day (1..31)
 *      bits4 ..0 : hour (0..23)
 *  (Data3<<8 | Data4):
 *      bits15..12: month (1..12)
 *      bits11..6 : minute (0..59)
 *      bits5 ..0 : second (0..59)
 *  (Data5<<8 | Data6):
 *      bits15..12: param (0 pH / 1 mV / 2 Cond / 3 TDS / 4 Salt / 5 Res / 6 ORP / 7 Cl / 8 DO)
 *      bits11..0 : display_digits_minus_1000  (so stored = shown*10^decimals + 1000)
 *  (Data7<<8 | Data8):
 *      bits15..14: decimal places (0..2)   <-- main value’s decimal digits
 *      bits13..10: unit code:
 *          0 mol, 1 pX, 2 pH, 3 mV, 4 mS, 5 uS, 6 g/L, 7 mg/L, 8 ug/L, 9 ppt, 10 ppm, 11 ppb,
 *          12 Ω, 13 KΩ, 14 MΩ, 15 %
 *      bits9 ..0 : temperature in 0.1 °C (Celsius only)
 */
public class VelaDataInfo implements Convent<VelaDataInfo> {

    public static final String TAG = "DataV2";

    // Protocol identifier for this frame
    public static final byte CODE = 0x44; // 'D'

    // Keep old constants for downstream compatibility
    public static final int pH   = 0x00;
    public static final int mV   = 0x01;
    public static final int Cond = 0x02; // µS/mS block
    public static final int TDS  = 0x03;
    public static final int Salt = 0x04;
    public static final int Res  = 0x05;
    public static final int ORP  = 0x06;

    // Old unit constants (used by getUnitString for compatibility)
    public static final int UNIT_F   = 0x01;
    public static final int UNIT_C   = 0x02;
    public static final int UNIT_pH  = 0x01;
    public static final int UNIT_mV  = 0x02;
    public static final int UNIT_mS  = 0x03;
    public static final int UNIT_uS  = 0x04;
    public static final int UNIT_ppm = 0x05;
    public static final int UNIT_ppt = 0x06;
    public static final int UNIT_gl  = 0x07;
    public static final int UNIT_mgl = 0x08;
    public static final int UNIT_Ω   = 0x09;
    public static final int UNIT_KΩ  = 10;
    public static final int UNIT_MΩ  = 11;

    // Raw buffer (we expect at least 9 bytes: 0..8)
    private byte[] raw = new byte[9];

    // Parsed fields (main value)
    private int decimals;          // main value decimal places (0..2)
    private int unitV2;            // 0..15 (see table above)
    private int unitCompat;        // mapped to old UNIT_* constants when possible
    private int valueInt;          // digits (display*10^decimals), after +1000 correction
    private int mode;              // mapped to old mode constants where possible

    // Temperature (Celsius only, resolution 0.1 °C)
    private int tempTenths;        // integer, 0.1°C units
    private int pointDigit2 = 1;   // fixed 1 decimal place for °C
    private int unit2 = UNIT_C;

    // Battery/HML/flags are not in the new spec; keep placeholders for API compatibility
    private boolean upperAlarm;
    private boolean lowerAlarm;
    private boolean hold;
    private boolean laughFace;
    private boolean H, M, L;

    // Timestamp
    private int year, month, day, hour, minute, second;

    // ----- Convent interface -----

    @Override
    public VelaDataInfo unpack(byte[] data) {
        if (data == null || data.length < 9) {
            Log.e(TAG, "data length not match, should be >= 9");
            return null;
        }
        if ((data[0] & 0xFF) != (CODE & 0xFF)) {
            Log.e(TAG, String.format("unexpected code 0x%02X (expected 0x%02X)", data[0], CODE));
            return null;
        }
        this.raw = Arrays.copyOf(data, 9);

        int w12 = ((data[1] & 0xFF) << 8) | (data[2] & 0xFF);
        int w34 = ((data[3] & 0xFF) << 8) | (data[4] & 0xFF);
        int w56 = ((data[5] & 0xFF) << 8) | (data[6] & 0xFF);
        int w78 = ((data[7] & 0xFF) << 8) | (data[8] & 0xFF);

        // Time
        int yearLast2Minus20 = (w12 >> 10) & 0x3F; // 0..63
        this.year  = 2000 + 20 + yearLast2Minus20; // 2020..2083
        this.day   = (w12 >> 5) & 0x1F;            // 1..31
        this.hour  =  w12       & 0x1F;            // 0..23

        this.month  = (w34 >> 12) & 0x0F;          // 1..12
        this.minute = (w34 >>  6) & 0x3F;          // 0..59
        this.second =  w34        & 0x3F;          // 0..59

        // Value + parameter
        int param = (w56 >> 12) & 0x0F;
        int digitsMinus1000 = w56 & 0x0FFF;
        this.valueInt = digitsMinus1000;

        // Decimals + unit + temp
        this.decimals   = (w78 >> 14) & 0x03;      // 0..2 (2 bits)
        this.unitV2     = (w78 >> 10) & 0x0F;      // 0..15
        this.tempTenths =  w78        & 0x03FF;    // 0..1023  (0.1 °C)

        // Map new "param" to old modes (keep old API)
        // 0-pH /1-mV /2-Cond /3-TDS /4-Salt /5-RES /6-ORP /7-余氯 /8-溶解氧
        switch (param) {
            case 0:  this.mode = pH;   break;
            case 1:  // mV
            case 6:  // ORP (treat as mV for compatibility)
                this.mode = ORP;        break;
            case 2:  this.mode = Cond; break;
            case 3:  this.mode = TDS;  break;
            case 4:  this.mode = Salt; break;
            case 5:  this.mode = Res;  break;
            case 7:  // free chlorine (no old constant; choose closest)
                this.mode = TDS;       break; // or keep a new field if you add UI for it
            case 8:  // dissolved oxygen (no old constant)
                this.mode = TDS;       break;
            default:
                this.mode = pH;        break;
        }

        // Map unitV2 to old unit constants when possible (for getUnitString)
        // 0 mol, 1 pX, 2 pH, 3 mV, 4 mS, 5 uS, 6 g/L, 7 mg/L, 8 ug/L, 9 ppt, 10 ppm, 11 ppb, 12 Ω, 13 KΩ, 14 MΩ, 15 %
        switch (unitV2) {
            case 2:  unitCompat = UNIT_pH;  break;
            case 3:  unitCompat = UNIT_mV;  break;
            case 4:  unitCompat = UNIT_mS;  break;
            case 5:  unitCompat = UNIT_uS;  break;
            case 6:  unitCompat = UNIT_gl;  break;
            case 7:  unitCompat = UNIT_mgl; break;
            case 9:  unitCompat = UNIT_ppt; break;
            case 10: unitCompat = UNIT_ppm; break;
            case 12: unitCompat = UNIT_Ω;   break;
            case 13: unitCompat = UNIT_KΩ;  break;
            case 14: unitCompat = UNIT_MΩ;  break;
            default:
                unitCompat = 0;             break; // will be string-mapped
        }

        // Flags not defined in new spec → keep false (API compatibility)
        upperAlarm = lowerAlarm = hold = laughFace = false;
        H = M = L = false;

        return this;
    }

    @Override
    public byte[] pack() {
        // Real devices generate this; app usually doesn’t pack DataV2 for sending.
        byte[] out = Arrays.copyOf(raw, raw.length);
        out[0] = CODE;
        Log.d(TAG, Arrays.toString(out));
        return out;
    }

    @Override
    public byte getCode() {
        return CODE;
    }

    @Override
    public String getData() {
        return okio.ByteString.of(raw).hex();
    }

    // ----- Value/Temp (compatible with old Data API) -----

    public double getValue() {
//        return valueInt / Math.pow(10, decimals);
        int v = this.valueInt;      // 0..4095 (mantissa)
        int n = this.decimals;      // 0..3   (decimal places)
        return (v - 1000) / Math.pow(10.0, n);
    }

    public int getPointDigit() {
        return decimals;
    }

    public int getPointDigit2() {
        return pointDigit2; // 1 decimal for °C
    }

    public double getTemp() {
        return tempTenths / 10.0;
    }

    public double getEC()   { return mode == Cond ? getValue() : 0; }
    public double getORP()  { return mode == ORP   ? getValue() : 0; }
    public double getPH()   { return mode == pH   ? getValue() : 0; }
    public double getResistivity() { return mode == Res ? getValue() : 0; }
    public double getTDS()  { return mode == TDS  ? getValue() : 0; }
    public double getSalinity() { return mode == Salt ? getValue() : 0; }

    public int getMode() { return mode; }
    public void setMode(int mode) { this.mode = mode; }

    public int getUnit2() { return unit2; }
    public void setUnit2(int unit2) { this.unit2 = unit2; }

    public int getIntValue2() { return tempTenths; }
    public void setValue2(int v2Tenths) { this.tempTenths = v2Tenths; }

    public void setPointDigit2(int pd2) { /* fixed 1 in this protocol */ }

    public void setValue(int vTimes10PowDecimals) { this.valueInt = vTimes10PowDecimals; }
    public void setPointDigit(int pd) { this.decimals = pd; }
    public void setUnit(int unit) { this.unitCompat = unit; }

    public boolean isUpperAlarm(){ return upperAlarm; }
    public boolean isLowerAlarm(){ return lowerAlarm; }
    public boolean isHold(){ return hold; }
    public boolean isLaughFace(){ return laughFace; }
    public boolean isH(){ return H; }
    public boolean isM(){ return M; }
    public boolean isL(){ return L; }

    // New protocol: temperature is always Celsius; this flag from old API no longer applies
    public boolean isCalibration(){ return false; }

    // ----- Timestamp getters -----

    public int getYear()   { return year; }
    public int getMonth()  { return month; }
    public int getDay()    { return day; }
    public int getHour()   { return hour; }
    public int getMinute() { return minute; }
    public int getSecond() { return second; }

    // ----- Units -----

    public String getUnitString() {
        // Prefer accurate new mapping; fallback to old constants
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
                // Fallback to old mapping if ever needed
                switch (unitCompat) {
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
    public String toString() {
        return "DataV2{"
                + "value=" + getValue()
                + ", decimals=" + decimals
                + ", unit='" + getUnitString() + '\''
                + ", temp=" + getTemp() + "°C"
                + ", mode=" + mode
                + ", ts=" + String.format("%04d-%02d-%02d %02d:%02d:%02d",
                year, month, day, hour, minute, second)
                + '}';
    }

    // returns a measure data pack
    public VelaMeasureDataPack toMeasureDataPack() {
        return new VelaMeasureDataPack(
                /* value, temp already scaled: */
                this.getValue(),
                this.getTemp(),
                /* mode & legacy unit: */
                this.getMode(),
                /* unitCompat is available inside this class; unit2 is Celsius in V2 */
                this.unitCompat,
                UNIT_C,
                /* display digits (metadata only) */
                this.getPointDigit(),
                this.getPointDigit2(),
                /* V2-first unit and compat fallback */
                this.unitV2,
                this.unitCompat,
                /* flags (placeholders in V2) */
                this.upperAlarm,
                this.lowerAlarm,
                this.hold,
                this.laughFace,
                this.H, this.M, this.L,
                /* timestamp */
                this.getYear(), this.getMonth(), this.getDay(),
                this.getHour(), this.getMinute(), this.getSecond(),
                /* raw hex parity */
                this.raw
        );
    }
}

