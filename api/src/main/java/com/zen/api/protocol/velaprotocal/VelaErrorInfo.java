package com.zen.api.protocol.velaprotocal;

import android.util.Log;

import com.zen.api.protocol.Convent;

import java.util.Arrays;
import java.util.Locale;

/**
 * VelaErrorInfo
 * Data0='E'(0x45): 错误信息(上传)
 *
 * Per spec:
 *   Data1=1: 溶液错误
 *   Data1=2: 未稳定
 *   Data1=3: 长时间不稳定
 *
 * (Optionally handled if device sends more):
 *   4: 零点超标
 *   5: 斜率超标
 *   6: 超过校正时限提醒
 */
public class VelaErrorInfo implements Convent<VelaErrorInfo> {

    public static final String TAG  = "VelaErrorInfo";
    public static final byte   CODE = 0x45;  // 'E'

    // --- helpers ---
    private static int u8(byte b) { return b & 0xFF; }

    // --- raw ---
    private byte[] raw = new byte[2];

    // --- decoded ---
    private int errCode;          // Data1
    private String errString;     // human-readable error
    private String fixString;     // suggested fix

    // Canonical codes (aligns with your existing constants, but simple ints are fine)
    public static final int ERR_SOLUTION            = 1; // 溶液错误
    public static final int ERR_UNSTABLE            = 2; // 未稳定
    public static final int ERR_LONG_UNSTABLE       = 3; // 长时间不稳定
    public static final int ERR_ZERO_OUT_OF_RANGE   = 4; // 零点超标 (optional)
    public static final int ERR_SLOPE_OUT_OF_RANGE  = 5; // 斜率超标 (optional)
    public static final int ERR_CAL_DUE_EXCEEDED    = 6; // 超过校正时限提醒 (optional)

    @Override
    public VelaErrorInfo unpack(byte[] data) {
        if (data == null || data.length < 2) {
            Log.e(TAG, "Frame too short for error info (need >=2), len=" + (data == null ? 0 : data.length));
            return null;
        }
        if ((data[0] & 0xFF) != (CODE & 0xFF)) {
            Log.e(TAG, String.format(Locale.US, "Unexpected Data0 0x%02X (expected 0x%02X)", data[0], CODE));
            return null;
        }
        this.raw = Arrays.copyOf(data, 2);
        this.errCode = u8(data[1]);

        mapError(errCode);
        return this;
    }

    @Override
    public byte[] pack() {
        // Uplink-only; keep raw for logging/echo
        if (raw == null || raw.length < 2) raw = new byte[2];
        raw[0] = CODE;
        raw[1] = (byte) (errCode & 0xFF);
        Log.d(TAG, "pack: " + Arrays.toString(raw));
        return Arrays.copyOf(raw, raw.length);
    }

    @Override
    public byte getCode() { return CODE; }

    @Override
    public String getData() {
        return okio.ByteString.of(raw != null ? raw : new byte[]{}).hex();
    }

    // --- mapping ---
    private void mapError(int code) {
        switch (code) {
            case ERR_SOLUTION:
                errString = "校正溶液错误";
                fixString = "检查所用缓冲/标准溶液是否与选择的标准一致；确保溶液未过期且温度稳定。";
                break;
            case ERR_UNSTABLE:
                errString = "测量未稳定";
                fixString = "确认电极连接良好，轻微搅拌后静置；检查电极是否污染或损坏并进行清洗/活化。";
                break;
            case ERR_LONG_UNSTABLE:
                errString = "校正超过3分钟未稳定";
                fixString = "更换新鲜溶液并清洗电极；如仍不稳定请重新开始校正流程或更换电极。";
                break;
            // Optional extended codes your old app hinted at:
            case ERR_ZERO_OUT_OF_RANGE:
                errString = "零点超标";
                fixString = "执行零点校正；检查电极是否老化或污染，必要时更换电极。";
                break;
            case ERR_SLOPE_OUT_OF_RANGE:
                errString = "斜率超标";
                fixString = "重复多点校正；确认缓冲液配方/浓度正确，温度一致，电极状态良好。";
                break;
            case ERR_CAL_DUE_EXCEEDED:
                errString = "超过校正时限提醒";
                fixString = "请尽快进行重新校准，以保证测量准确性。";
                break;
            default:
                errString = "未知错误(" + code + ")";
                fixString = "重试操作；若多次出现请检查电极与仪表或联系技术支持。";
                break;
        }
    }

    // --- getters / setters ---
    public int getErrCode() { return errCode; }
    public String getErrString() { return errString; }
    public String getFixString() { return fixString; }

    /** For testing or UI override. Also updates mapped strings. */
    public void setErr(int err) { this.errCode = err; mapError(err); }

    public void setErrString(String custom) { this.errString = custom; }
    public void setFixString(String custom) { this.fixString = custom; }

    @Override
    public String toString() {
        return "VelaErrorInfo{" +
                "errCode=" + errCode +
                ", errString='" + errString + '\'' +
                ", fixString='" + fixString + '\'' +
                '}';
    }
}