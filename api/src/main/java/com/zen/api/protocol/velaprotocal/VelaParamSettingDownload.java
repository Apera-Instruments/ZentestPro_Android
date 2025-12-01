package com.zen.api.protocol.velaprotocal;

import android.util.Log;

import com.zen.api.protocol.Convent;

import java.util.Arrays;
import java.util.Locale;

/**
 * VelaParamSettingDownload
 * Downlink (write) builder for Data0='P' (0x50) 参数列表.
 *
 * Data1 selects the group:
 *   0: Instrument
 *   1: pH
 *   2: Conductivity (Cond/TDS/Salt)
 *   3: ORP
 *
 * Use the fluent Builder to create frames. Example:
 *   byte[] dl = VelaParamSettingDownload.builder(Group.INSTRUMENT)
 *       .autoHold(true)
 *       .timedSaveSeconds(30)
 *       .tempUnitC(true)
 *       .autoPowerMinutes(20)
 *       .language(1) // 0=CN, 1=EN, etc.
 *       .restoreFactory(false)
 *       .build();
 */
public class VelaParamSettingDownload implements Convent<VelaParamSettingDownload> {

    public static final String TAG  = "VelaParamSettingDl";
    public static final byte   CODE = 0x50;

    // ---- helpers ------------------------------------------------------------
    private static int clamp(int v, int lo, int hi) { return Math.max(lo, Math.min(hi, v)); }
    private static void putU16(byte[] buf, int idx, int value) {
        buf[idx]   = (byte)((value >> 8) & 0xFF);
        buf[idx+1] = (byte)( value       & 0xFF);
    }
    // timed-save buckets: 0 manual; 1..99 sec; 101..199 min; 201..299 hour
    public static int timedSaveSeconds(int sec) { return (sec <= 0) ? 0 : clamp(sec, 1, 99); }
    public static int timedSaveMinutes(int min) { return (min <= 0) ? 0 : 100 + clamp(min, 1, 99); }
    public static int timedSaveHours(int hr)    { return (hr  <= 0) ? 0 : 200 + clamp(hr,  1, 99); }
    // calibration due: 0 off; 1..99 hours; 101..199 days
    public static int calDueHours(int h) { return (h <= 0) ? 0 : clamp(h, 1, 99); }
    public static int calDueDays(int d)  { return (d <= 0) ? 0 : 100 + clamp(d, 1, 99); }

    // ---- enums --------------------------------------------------------------
    public enum Group { INSTRUMENT(0), PH(1), COND(2), ORP(3);
        public final int code; Group(int c){code=c;} }
    public enum TempUnit { F(0), C(1); public final int code; TempUnit(int c){code=c;} }
    public enum PHBuffer { CH(0), EN(1), NIST(2); public final int code; PHBuffer(int c){code=c;} }
    public enum CondStd { CN(0), STD(1); public final int code; CondStd(int c){code=c;} }
    public enum SaltUnit { PPT(0), GL(1); public final int code; SaltUnit(int c){code=c;} }

    // ---- raw ---------------------------------------------------------------
    private byte[] raw;   // built frame

    // ---- Convent -----------------------------------------------------------
    @Override
    public VelaParamSettingDownload unpack(byte[] data) {
        // This class is primarily for downlink building; allow echo-storage.
        this.raw = (data == null) ? null : Arrays.copyOf(data, data.length);
        return this;
    }

    @Override
    public byte[] pack() {
        return raw != null ? Arrays.copyOf(raw, raw.length) : new byte[]{ CODE, 0 };
    }

    @Override
    public byte getCode() { return CODE; }

    @Override
    public String getData() { return okio.ByteString.of(raw != null ? raw : new byte[]{}).hex(); }

    // ---- Builder -----------------------------------------------------------
    public static Builder builder(Group g) { return new Builder(g); }

    public static final class Builder {
        private final Group g;

        // Instrument (Data1=0)
        private Boolean autoHold;         // Data2: 1/0
        private Integer timedSaveRaw;     // Data3..4 (bucketed value)
        private TempUnit tempUnit;        // Data5: 1℃/0℉
        private Integer autoPowerMin;     // Data6: 0/10/20/30
        private Integer language;         // Data7: passthrough int
        private Boolean restoreFactory0;  // Data8: downlink only

        // pH (Data1=1)
        private PHBuffer phBuffer;        // Data2: 0/CH 1/EN 2/NIST
        private Integer phResolution;     // Data3: 1 or 2
        private Integer phCalDueRaw;      // Data4: 0 / 1..99 hours / 101..199 days
        private Integer phUpper;          // Data5..6
        private Integer phLower;          // Data7..8
        private Boolean restoreFactory1;  // Data9

        // COND (Data1=2)
        private CondStd condStd;          // Data2: 0/CN 1/STD
        private Integer condCalDueRaw;    // Data3
        private Integer refTempC;         // Data4
        private Integer tempComp01pct;    // Data5..6 (0.01%)
        private Integer tdsFactor01;      // Data7 (0.01)
        private Integer saltType;         // Data8
        private SaltUnit saltUnit;        // Data9
        private Integer condUpper;        // Data10..11
        private Integer condLower;        // Data12..13
        private Integer tdsUpper;         // Data14..15
        private Integer tdsLower;         // Data16..17
        private Integer saltUpper;        // Data18..19
        private Integer saltLower;        // Data20..21
        private Boolean restoreFactory2;  // Data22

        // ORP (Data1=3)
        private Integer orpCalDueRaw;     // Data2
        private Integer orpUpper;         // Data3..4
        private Integer orpLower;         // Data5..6
        private Boolean restoreFactory3;  // Data7

        private Builder(Group g){ this.g=g; }

        // -------- INSTRUMENT --------
        public Builder autoHold(boolean on){ this.autoHold = on; return this; }
        public Builder timedSaveRaw(int raw){ this.timedSaveRaw = clamp(raw,0,299); return this; }
        public Builder timedSaveSecondsB(int sec){ this.timedSaveRaw = timedSaveSeconds(sec); return this; }
        public Builder timedSaveMinutesB(int min){ this.timedSaveRaw = timedSaveMinutes(min); return this; }
        public Builder timedSaveHoursB(int hr){ this.timedSaveRaw = timedSaveHours(hr); return this; }
        public Builder tempUnitC(boolean c){ this.tempUnit = c?TempUnit.C:TempUnit.F; return this; }
        /** Only 0/10/20/30 are valid */
        public Builder autoPowerMinutes(int mins){ this.autoPowerMin = (mins==10||mins==20||mins==30)? mins : 0; return this; }
        public Builder language(int code){ this.language = code; return this; }
        public Builder restoreFactory(boolean yes){
            switch (g){
                case INSTRUMENT: this.restoreFactory0 = yes; break;
                case PH:         this.restoreFactory1 = yes; break;
                case COND:       this.restoreFactory2 = yes; break;
                case ORP:        this.restoreFactory3 = yes; break;
            }
            return this;
        }

        // -------- pH --------
        public Builder phBuffer(PHBuffer b){ this.phBuffer = b; return this; }
        public Builder phResolution(int r){ this.phResolution = clamp(r,1,2); return this; }
        public Builder phCalDueHours(int h){ this.phCalDueRaw = calDueHours(h); return this; }
        public Builder phCalDueDays(int d){ this.phCalDueRaw = calDueDays(d); return this; }
        public Builder phCalDueOff(){ this.phCalDueRaw = 0; return this; }
        public Builder phUpper(int v){ this.phUpper = v; return this; }
        public Builder phLower(int v){ this.phLower = v; return this; }

        // -------- COND --------
        public Builder condStd(CondStd s){ this.condStd = s; return this; }
        public Builder condCalDueHours(int h){ this.condCalDueRaw = calDueHours(h); return this; }
        public Builder condCalDueDays(int d){ this.condCalDueRaw = calDueDays(d); return this; }
        public Builder condCalDueOff(){ this.condCalDueRaw = 0; return this; }
        public Builder refTempC(int c){ this.refTempC = clamp(c,0,99); return this; }
        /** 0..999 (=> 0..9.99%) */
        public Builder tempComp01pct(int v){ this.tempComp01pct = clamp(v,0,999); return this; }
        /** 40..100 (=> 0.40..1.00) typical range */
        public Builder tdsFactor01(int v){ this.tdsFactor01 = clamp(v,0,255); return this; }
        /** 0:F=0.5 1:NaCl 2:Seawater */
        public Builder saltType(int v){ this.saltType = clamp(v,0,255); return this; }
        public Builder saltUnit(SaltUnit u){ this.saltUnit = u; return this; }
        public Builder condUpper(int v){ this.condUpper = clamp(v,0,0xFFFF); return this; }
        public Builder condLower(int v){ this.condLower = clamp(v,0,0xFFFF); return this; }
        public Builder tdsUpper(int v){ this.tdsUpper = clamp(v,0,0xFFFF); return this; }
        public Builder tdsLower(int v){ this.tdsLower = clamp(v,0,0xFFFF); return this; }
        public Builder saltUpper(int v){ this.saltUpper = clamp(v,0,0xFFFF); return this; }
        public Builder saltLower(int v){ this.saltLower = clamp(v,0,0xFFFF); return this; }

        // -------- ORP --------
        public Builder orpCalDueHours(int h){ this.orpCalDueRaw = calDueHours(h); return this; }
        public Builder orpCalDueDays(int d){ this.orpCalDueRaw = calDueDays(d); return this; }
        public Builder orpCalDueOff(){ this.orpCalDueRaw = 0; return this; }
        public Builder orpUpper(int v){ this.orpUpper = clamp(v,0,0xFFFF); return this; }
        public Builder orpLower(int v){ this.orpLower = clamp(v,0,0xFFFF); return this; }

        public byte[] build(){
            switch (g){
                case INSTRUMENT: return buildInstrument();
                case PH:         return buildPH();
                case COND:       return buildCond();
                case ORP:        return buildORP();
                default:         return new byte[]{ CODE, (byte)0xFF };
            }
        }

        private byte[] buildInstrument(){
            byte[] out = new byte[9]; // Data0..Data8
            out[0] = CODE;
            out[1] = (byte) Group.INSTRUMENT.code;
            out[2] = (byte) ((autoHold != null && autoHold) ? 1 : 0);
            putU16(out, 3, timedSaveRaw == null ? 0 : clamp(timedSaveRaw,0,299));
            out[5] = (byte) ((tempUnit == null ? TempUnit.C : tempUnit).code);
            int ap = (autoPowerMin == null) ? 0 : (autoPowerMin==10||autoPowerMin==20||autoPowerMin==30? autoPowerMin:0);
            out[6] = (byte) ap;
            out[7] = (byte) (language == null ? 0 : language);
            out[8] = (byte) ((restoreFactory0 != null && restoreFactory0) ? 1 : 0);
            Log.d(TAG, "Instrument DL: " + Arrays.toString(out));
            return out;
        }

        private byte[] buildPH(){
            byte[] out = new byte[10]; // Data0..Data9
            out[0] = CODE;
            out[1] = (byte) Group.PH.code;
            int buff = phBuffer == null ? PHBuffer.CH.code : phBuffer.code;
            out[2] = (byte) buff;
            out[3] = (byte) (phResolution == null ? 1 : clamp(phResolution,1,2));
            out[4] = (byte) (phCalDueRaw == null ? 0 : clamp(phCalDueRaw,0,199));
            putU16(out, 5, phUpper == null ? 0 : clamp(phUpper,0,0xFFFF));
            putU16(out, 7, phLower == null ? 0 : clamp(phLower,0,0xFFFF));
            out[9] = (byte) ((restoreFactory1 != null && restoreFactory1) ? 1 : 0);
            Log.d(TAG, "PH DL: " + Arrays.toString(out));
            return out;
        }

        private byte[] buildCond(){
            byte[] out = new byte[23]; // Data0..Data22
            out[0] = CODE;
            out[1] = (byte) Group.COND.code;
            out[2] = (byte) ((condStd == null ? CondStd.CN : condStd).code);
            out[3] = (byte) (condCalDueRaw == null ? 0 : clamp(condCalDueRaw,0,199));
            out[4] = (byte) (refTempC == null ? 25 : clamp(refTempC,0,99));
            putU16(out, 5, tempComp01pct == null ? 0 : clamp(tempComp01pct,0,999));
            out[7] = (byte) (tdsFactor01 == null ? 74 : clamp(tdsFactor01,0,255));
            out[8] = (byte) (saltType == null ? 0 : clamp(saltType,0,255));
            out[9] = (byte) ((saltUnit == null ? SaltUnit.PPT : saltUnit).code);
            putU16(out,10, condUpper == null ? 0 : condUpper);
            putU16(out,12, condLower == null ? 0 : condLower);
            putU16(out,14, tdsUpper  == null ? 0 : tdsUpper);
            putU16(out,16, tdsLower  == null ? 0 : tdsLower);
            putU16(out,18, saltUpper == null ? 0 : saltUpper);
            putU16(out,20, saltLower == null ? 0 : saltLower);
            out[22] = (byte) ((restoreFactory2 != null && restoreFactory2) ? 1 : 0);
            Log.d(TAG, "COND DL: " + Arrays.toString(out));
            return out;
        }

        private byte[] buildORP(){
            byte[] out = new byte[8]; // Data0..Data7
            out[0] = CODE;
            out[1] = (byte) Group.ORP.code;
            out[2] = (byte) (orpCalDueRaw == null ? 0 : clamp(orpCalDueRaw,0,199));
            putU16(out,3, orpUpper == null ? 0 : orpUpper);
            putU16(out,5, orpLower == null ? 0 : orpLower);
            out[7] = (byte) ((restoreFactory3 != null && restoreFactory3) ? 1 : 0);
            Log.d(TAG, "ORP DL: " + Arrays.toString(out));
            return out;
        }
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "VelaParamSettingDownload{len=%d}", raw == null ? 0 : raw.length);
    }
}