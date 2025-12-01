package com.zen.api.protocol.velaprotocal;

import android.util.Log;

import com.zen.api.protocol.Convent;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/** Template base for Data0='C' (0x43) calibration frames. */
public abstract class VelaCalibrationBaseInfo<T extends VelaCalibrationBaseInfo<T>>
        implements Convent<T> {

    public static final String TAG  = "VelaCalibrationBase";
    public static final byte   CODE = 0x43; // 'C'

    // ---- common raw + decoded time ----
    protected byte[] raw;
    protected Date   dateTime;
    protected int    year, month, day, hour, minute;

    // ---- subclass must implement ----
    /** The expected Data1 value (1=pH, 2=Cond, 3=ORP). */
    protected abstract int expectedGroup();

    /** Parse the body from Data6 onward; return (T)this for chaining. */
    protected abstract T parseBody(byte[] data);

    // ---- Convent<T> entry point (template method) ----
    @SuppressWarnings("unchecked")
    @Override
    public final T unpack(byte[] data) {
        if (data == null || data.length < 6) {
            Log.e(TAG, "Frame too short (need >=6), len=" + (data == null ? 0 : data.length));
            return null;
        }
        if (((int)data[0] & 0xFF) != (CODE & 0xFF)) {
            Log.e(TAG, String.format(Locale.US,
                    "Unexpected Data0 0x%02X (expected 0x%02X)", data[0], CODE));
            return null;
        }
        if (((int)data[1] & 0xFF) != expectedGroup()) {
            Log.e(TAG, "Unexpected Data1=" + (data[1] & 0xFF) + ", expected=" + expectedGroup());
            return null;
        }
        this.raw = Arrays.copyOf(data, data.length);
        parseTimestamp(data);          // Data2..5
        return parseBody(data);        // subclass-specific (Data6+)
    }

    @Override
    public byte[] pack() {
        // Uplink-only today; return last raw for logging/echo
        return raw != null ? Arrays.copyOf(raw, raw.length) : new byte[]{ CODE, (byte) expectedGroup() };
    }

    @Override
    public byte getCode() { return CODE; }

    @Override
    public String getData() { return okio.ByteString.of(raw != null ? raw : new byte[]{}).hex(); }

    // ---- common helpers ----
    protected static int u8(byte b) { return b & 0xFF; }
    protected static int u16(byte hi, byte lo) { return ((hi & 0xFF) << 8) | (lo & 0xFF); }

    /** Data2..3: date; Data4..5: time (per protocol). */
    protected final void parseTimestamp(byte[] d) {
        int wDate = u16(d[2], d[3]);
        this.day   =  wDate        & 0x1F;
        this.month = (wDate >> 5)  & 0x0F;
        int yy     = (wDate >> 9)  & 0x3F;
        this.year  = 2000 + yy;

        int wTime = u16(d[4], d[5]);
        this.minute =  wTime       & 0x3F;
        this.hour   = (wTime >> 6) & 0x1F;

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, Math.max(0, month - 1));
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        this.dateTime = cal.getTime();
    }

    // ---- getters for common fields ----
    public Date getDateTime() { return dateTime; }
    public int  getYear()     { return year; }
    public int  getMonth()    { return month; }
    public int  getDay()      { return day; }
    public int  getHour()     { return hour; }
    public int  getMinute()   { return minute; }
}
