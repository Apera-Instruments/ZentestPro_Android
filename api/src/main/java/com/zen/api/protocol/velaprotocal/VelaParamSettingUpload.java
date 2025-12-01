package com.zen.api.protocol.velaprotocal;

import android.util.Log;

import com.zen.api.protocol.Convent;

import java.util.Arrays;
import java.util.Locale;

/**
 * VelaParamSettingInfo
 * Decodes Data0='P'(0x50) "参数列表(上传/下传)" frames.
 *
 * Layout (from spec):
 *  Data0 = 0x50 ('P')
 *  Data1 = group selector:
 *     0: 仪器设置 (instrument)
 *     1: pH设置
 *     2: 电导设置 (cond/TDS/salt/res)
 *     3: ORP设置
 *
 * For each group, the meaning of subsequent DataN fields differs (see below).
 */
public class VelaParamSettingUpload implements Convent<VelaParamSettingUpload> {

    public static final String TAG = "VelaParamSettingInfo";
    public static final byte CODE = 0x50; // 'P'

    // --- Common helpers ------------------------------------------------------

    private static int u8(byte b)      { return b & 0xFF; }
    private static int u16(byte hi, byte lo) { return ((hi & 0xFF) << 8) | (lo & 0xFF); }

    // --- Public enums & types ------------------------------------------------

    public enum Group {
        INSTRUMENT(0), PH(1), COND(2), ORP(3), UNKNOWN(255);
        public final int code;
        Group(int c) { this.code = c; }
        public static Group of(int c) {
            for (Group g : values()) if (g.code == c) return g;
            return UNKNOWN;
        }
    }

    public enum TempUnit { C, F }
    public enum SaltUnit { PPT, GL }         // ppt / g/L
    public enum PHBuffer { CH, EN, NIST }    // 0/CH 1/EN 2/NIST (spec says “pH buff 0:CH 1:En 2:NIST”)
    public enum CondStd { CN, STD }          // 0/中国 1/标准
    public enum Language { CN, EN, OTHERS }  // simplistic placeholder

    // --- Decoded group payloads ---------------------------------------------

    /** Data1=0 仪器设置 */
    public static final class InstrumentSettings {
        public boolean autoHoldOn;          // Data2: 1-On 0-Off (NOTE: this differs from old "3-30s" design)
        public int timedSaveRaw;            // Data3..4: 0 manual, 1-99 sec, 101-199 min, 201-299 hour
        public TempUnit tempUnit;           // Data5: 1-℃ 0-℉
        public int autoPowerMinutes;        // Data6: 0 manual; 10/20/30 min
        public Language language;           // Data7: language code (mapping below)
        public boolean restoreFactory;      // Data8: only valid on downlink

        // Helpers
        public String getTimedSaveDisplay() {
            int v = timedSaveRaw;
            if (v == 0) return "Manual";
            if (v >= 1 && v <= 99)     return v + " sec";
            if (v >= 101 && v <= 199)  return (v - 100) + " min";
            if (v >= 201 && v <= 299)  return (v - 200) + " hour";
            return String.valueOf(v);
        }

        public String getTempUnitDisplay() {
            return tempUnit == TempUnit.C ? "℃" : "℉";
        }

        public String getLanguageDisplay() {
            switch (language) {
                case CN: return "中文";
                case EN: return "English";
                default: return "Unknown";
            }
        }
    }

    /** Data1=1 pH设置 */
    public static final class PHSettings {
        public PHBuffer buffer;         // Data2: 0/CH 1/EN 2/NIST
        public int resolution;          // Data3: 1 -> 0.1 ; 2 -> 0.01
        public int calibrationDueRaw;   // Data4: 0/OFF 1-99 Hours 101-199 Days
        public int upperLimit;          // Data5..6: pH upper (encoded integer)
        public int lowerLimit;          // Data7..8: pH lower (encoded integer)
        public boolean restoreFactory;  // Data9: downlink only

        public String getResolutionDisplay() { return (resolution == 1) ? "0.1" : "0.01"; }

        public String getCalibrationDueDisplay() {
            int v = calibrationDueRaw;
            if (v == 0) return "OFF";
            if (v <= 100) return v + " Hours";
            return (v - 100) + " Days";
        }
    }

    /** Data1=2 电导设置（含 TDS/盐度/电阻） */
    public static final class CondSettings {
        public CondStd standard;       // Data2: 0/CN 1/STD
        public int calibrationDueRaw;  // Data3: 0/OFF 1-99 Hours 101-199 Days
        public int refTempC;           // Data4: 参比温度 (°C)
        public int tempComp01pct;      // Data5..6: 温度补偿系数, 单位0.01%
        public int tdsFactor01;        // Data7: TDS系数, 单位0.01 (e.g., 40..100 → 0.40..1.00)
        public int saltType;           // Data8: 盐度类型 (0:F=0.5 1:NaCl 2:Seawater) – per your comment block
        public SaltUnit saltUnit;      // Data9: 盐度单位
        public int condUpper;          // Data10..11: 电导上限
        public int condLower;          // Data12..13: 电导下限
        public int tdsUpper;           // Data14..15: TDS上限
        public int tdsLower;           // Data16..17: TDS下限
        public int saltUpper;          // Data18..19: 盐度上限
        public int saltLower;          // Data20..21: 盐度下限
        public boolean restoreFactory; // Data22: downlink only

        public String getCalibrationDueDisplay() {
            int v = calibrationDueRaw;
            if (v == 0) return "OFF";
            if (v <= 100) return v + " Hours";
            return (v - 100) + " Days";
        }

        public String getTempCompPercent() {
            // tempComp01pct is in 0.01%, so 123 -> 1.23%
            return String.format(Locale.US, "%.2f%%", tempComp01pct / 100.0);
        }

        public String getTdsFactor() {
            // tdsFactor01 is in 0.01, so 40 -> 0.40
            return String.format(Locale.US, "%.2f", tdsFactor01 / 100.0);
        }

        public String getSaltUnitDisplay() {
            return (saltUnit == SaltUnit.PPT) ? "ppt" : "g/L";
        }
    }

    /** Data1=3 ORP设置 */
    public static final class ORPSettings {
        public int calibrationDueRaw;  // Data2
        public int upperLimit;         // Data3..4
        public int lowerLimit;         // Data5..6
        public boolean restoreFactory; // Data7 (downlink only)

        public String getCalibrationDueDisplay() {
            int v = calibrationDueRaw;
            if (v == 0) return "OFF";
            if (v <= 100) return v + " Hours";
            return (v - 100) + " Days";
        }
    }

    // --- Instance fields -----------------------------------------------------

    private byte[] raw;       // entire frame buffer (kept for debugging/pack)
    private Group group;      // Data1
    private InstrumentSettings instrument;
    private PHSettings ph;
    private CondSettings cond;
    private ORPSettings orp;

    // --- Convent interface ---------------------------------------------------

    @Override
    public VelaParamSettingUpload unpack(byte[] data) {
        if (data == null || data.length < 2) {
            Log.e(TAG, "Frame too short");
            return null;
        }
        if ((data[0] & 0xFF) != (CODE & 0xFF)) {
            Log.e(TAG, String.format(Locale.US,
                    "Unexpected code 0x%02X (expected 0x%02X)", data[0], CODE));
            return null;
        }

        this.raw = Arrays.copyOf(data, data.length);
        int g = u8(data[1]);
        this.group = Group.of(g);

        switch (this.group) {
            case INSTRUMENT: parseInstrument(data); break;
            case PH:         parsePH(data);         break;
            case COND:       parseCond(data);       break;
            case ORP:        parseORP(data);        break;
            default:
                Log.w(TAG, "Unknown group: " + g);
                break;
        }
        return this;
    }

    @Override
    public byte[] pack() {
        // If you need to *downlink* settings, construct raw accordingly.
        // This is a placeholder that simply returns the last raw.
        return raw != null ? Arrays.copyOf(raw, raw.length) : new byte[]{ CODE, (byte) group.code };
    }

    @Override
    public byte getCode() { return CODE; }

    @Override
    public String getData() {
        return okio.ByteString.of(raw != null ? raw : new byte[]{ }).hex();
    }

    // --- Parsers -------------------------------------------------------------

    private void parseInstrument(byte[] d) {
        // Expect at least Data8 => len >= 9
        if (d.length < 9) {
            Log.w(TAG, "Instrument frame too short: " + d.length);
        }
        InstrumentSettings s = new InstrumentSettings();
        s.autoHoldOn      = u8(dSafe(d,2)) == 1;
        s.timedSaveRaw    = u16(dSafe(d,3), dSafe(d,4));
        s.tempUnit        = (u8(dSafe(d,5)) == 1) ? TempUnit.C : TempUnit.F;
        int ap = u8(dSafe(d,6));  // 0,10,20,30
        s.autoPowerMinutes = (ap == 0) ? 0 : ap; // store minutes as-is
        int lang = u8(dSafe(d,7));
        s.language        = (lang == 0) ? Language.CN : (lang == 1 ? Language.EN : Language.OTHERS);
        s.restoreFactory  = false; // Data8 is only valid downlink; ignore on uploads

        this.instrument = s;
    }

    private void parsePH(byte[] d) {
        // Need up to Data9 => len >= 10
        if (d.length < 10) {
            Log.w(TAG, "PH frame too short: " + d.length);
        }
        PHSettings s = new PHSettings();
        int buff = u8(dSafe(d,2));
        s.buffer = (buff == 0) ? PHBuffer.CH : (buff == 1 ? PHBuffer.EN : PHBuffer.NIST);
        s.resolution         = u8(dSafe(d,3)); // 1 or 2
        s.calibrationDueRaw  = u8(dSafe(d,4));
        s.upperLimit         = u16(dSafe(d,5), dSafe(d,6));
        s.lowerLimit         = u16(dSafe(d,7), dSafe(d,8));
        s.restoreFactory     = false; // Data9 downlink only

        this.ph = s;
    }

    private void parseCond(byte[] d) {
        // Need up to Data22 => len >= 23
        if (d.length < 23) {
            Log.w(TAG, "COND frame too short: " + d.length);
        }
        CondSettings s = new CondSettings();
        s.standard          = (u8(dSafe(d,2)) == 0) ? CondStd.CN : CondStd.STD;
        s.calibrationDueRaw = u8(dSafe(d,3));
        s.refTempC          = u8(dSafe(d,4));
        s.tempComp01pct     = u16(dSafe(d,5), dSafe(d,6));
        s.tdsFactor01       = u8(dSafe(d,7));               // 0.01 units
        int saltTypeRaw     = u8(dSafe(d,8));
        s.saltType          = saltTypeRaw;                   // 0:F=0.5 1:NaCl 2:Seawater
        s.saltUnit          = (u8(dSafe(d,9)) == 0) ? SaltUnit.PPT : SaltUnit.GL;

        s.condUpper         = u16(dSafe(d,10), dSafe(d,11));
        s.condLower         = u16(dSafe(d,12), dSafe(d,13));
        s.tdsUpper          = u16(dSafe(d,14), dSafe(d,15));
        s.tdsLower          = u16(dSafe(d,16), dSafe(d,17));
        s.saltUpper         = u16(dSafe(d,18), dSafe(d,19));
        s.saltLower         = u16(dSafe(d,20), dSafe(d,21));
        s.restoreFactory    = false; // Data22 downlink only

        this.cond = s;
    }

    private void parseORP(byte[] d) {
        // Need up to Data7 => len >= 8
        if (d.length < 8) {
            Log.w(TAG, "ORP frame too short: " + d.length);
        }
        ORPSettings s = new ORPSettings();
        s.calibrationDueRaw = u8(dSafe(d,2));
        s.upperLimit        = u16(dSafe(d,3), dSafe(d,4));
        s.lowerLimit        = u16(dSafe(d,5), dSafe(d,6));
        s.restoreFactory    = false; // Data7 downlink only
        this.orp = s;
    }

    private static byte dSafe(byte[] d, int idx) {
        return (idx >= 0 && idx < d.length) ? d[idx] : 0;
    }

    // --- Accessors -----------------------------------------------------------

    public Group getGroup() { return group; }
    public InstrumentSettings getInstrument() { return instrument; }
    public PHSettings getPh() { return ph; }
    public CondSettings getCond() { return cond; }
    public ORPSettings getOrp() { return orp; }

    // Handy formatted summary for logs/diagnostics.
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("VelaParamSettingInfo{ group=" + group + " }\n");
        switch (group) {
            case INSTRUMENT:
                if (instrument != null) {
                    sb.append(" autoHold: ").append(instrument.autoHoldOn ? "ON" : "OFF").append('\n')
                            .append(" timedSave: ").append(instrument.getTimedSaveDisplay()).append('\n')
                            .append(" tempUnit: ").append(instrument.getTempUnitDisplay()).append('\n')
                            .append(" autoPower: ").append(
                                    instrument.autoPowerMinutes == 0 ? "Manual" :
                                            instrument.autoPowerMinutes + " min").append('\n')
                            .append(" language: ").append(instrument.getLanguageDisplay()).append('\n');
                }
                break;
            case PH:
                if (ph != null) {
                    sb.append(" buffer: ").append(ph.buffer).append('\n')
                            .append(" resolution: ").append(ph.getResolutionDisplay()).append('\n')
                            .append(" calDue: ").append(ph.getCalibrationDueDisplay()).append('\n')
                            .append(" upper: ").append(ph.upperLimit).append('\n')
                            .append(" lower: ").append(ph.lowerLimit).append('\n');
                }
                break;
            case COND:
                if (cond != null) {
                    sb.append(" std: ").append(cond.standard).append('\n')
                            .append(" calDue: ").append(cond.getCalibrationDueDisplay()).append('\n')
                            .append(" refTemp: ").append(cond.refTempC).append("℃").append('\n')
                            .append(" tempComp: ").append(cond.getTempCompPercent()).append('\n')
                            .append(" tdsFactor: ").append(cond.getTdsFactor()).append('\n')
                            .append(" saltType: ").append(cond.saltType).append('\n')
                            .append(" saltUnit: ").append(cond.getSaltUnitDisplay()).append('\n')
                            .append(" condUpper: ").append(cond.condUpper).append('\n')
                            .append(" condLower: ").append(cond.condLower).append('\n')
                            .append(" tdsUpper: ").append(cond.tdsUpper).append('\n')
                            .append(" tdsLower: ").append(cond.tdsLower).append('\n')
                            .append(" saltUpper: ").append(cond.saltUpper).append('\n')
                            .append(" saltLower: ").append(cond.saltLower).append('\n');
                }
                break;
            case ORP:
                if (orp != null) {
                    sb.append(" calDue: ").append(orp.getCalibrationDueDisplay()).append('\n')
                            .append(" upper: ").append(orp.upperLimit).append('\n')
                            .append(" lower: ").append(orp.lowerLimit).append('\n');
                }
                break;
            default:
                break;
        }
        return sb.toString();
    }
}