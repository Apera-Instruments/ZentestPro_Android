package com.zen.api.protocol.velaprotocal;

import android.util.Log;

import com.zen.api.protocol.Convent;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class VelaTimeSetting implements Convent<VelaTimeSetting> {

    public static final String TAG  = "VelaTimeSetting";
    public static final byte   CODE = 0x54; // 'T'

    // Data frame: [T, year(yy), month(1-12), day(1-31), hour(0-23), minute(0-59), second(0-59)]
    private final byte[] data = new byte[7];

    // Cached decoded time (for convenience)
    private Calendar calendar = Calendar.getInstance();

    // --- helpers ---
    private static int u8(byte b) { return b & 0xFF; }
    private static byte yy(int year) { return (byte) (year % 100); }
    private static byte b(int v, int lo, int hi) {
        if (v < lo || v > hi) throw new IllegalArgumentException("out of range: " + v);
        return (byte) v;
    }

    // ---- Convent ----
    @Override
    public VelaTimeSetting unpack(byte[] in) {
        if (in == null || in.length < 7) {
            Log.e(TAG, "Frame too short for 'T' time setting (need 7 bytes)");
            return null;
        }
        if (u8(in[0]) != u8(CODE)) {
            Log.e(TAG, String.format(Locale.US, "Unexpected Data0 0x%02X (expected 0x%02X)", in[0], CODE));
            return null;
        }
        System.arraycopy(in, 0, data, 0, 7);

        // Interpret year as 2000 + yy (common pattern in your protocol)
        int year   = 2000 + u8(in[1]);
        int month  = u8(in[2]); // 1..12
        int day    = u8(in[3]); // 1..31
        int hour   = u8(in[4]); // 0..23
        int minute = u8(in[5]); // 0..59
        int second = u8(in[6]); // 0..59

        calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, Math.max(0, month - 1));
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, 0);

        Log.d(TAG, "unpack: " + Arrays.toString(in) + " → " + calendar.getTime());
        return this;
    }

    @Override
    public byte[] pack() {
        // Use current calendar values
        data[0] = CODE;
        data[1] = yy(calendar.get(Calendar.YEAR));
        data[2] = (byte) (calendar.get(Calendar.MONTH) + 1);
        data[3] = (byte) calendar.get(Calendar.DAY_OF_MONTH);
        data[4] = (byte) calendar.get(Calendar.HOUR_OF_DAY);
        data[5] = (byte) calendar.get(Calendar.MINUTE);
        data[6] = (byte) calendar.get(Calendar.SECOND);

        Log.d(TAG, "pack: " + Arrays.toString(data));
        return Arrays.copyOf(data, data.length);
    }

    @Override
    public byte getCode() { return CODE; }

    @Override
    public String getData() { return okio.ByteString.of(data).hex(); }

    // ---- Fluent setters/builders ----

    /** Set all fields explicitly (year can be 4-digit; only last two digits will be sent). */
    public VelaTimeSetting setYMDHMS(int year, int month, int day, int hour, int minute, int second) {
        calendar.set(Calendar.YEAR,   year);
        calendar.set(Calendar.MONTH,  b(month, 1, 12) - 1);
        calendar.set(Calendar.DAY_OF_MONTH, b(day, 1, 31));
        calendar.set(Calendar.HOUR_OF_DAY,  b(hour, 0, 23));
        calendar.set(Calendar.MINUTE,       b(minute, 0, 59));
        calendar.set(Calendar.SECOND,       b(second, 0, 59));
        calendar.set(Calendar.MILLISECOND, 0);
        return this;
    }

    /** Set from a Calendar (copied). */
    public VelaTimeSetting setFromCalendar(Calendar cal) {
        this.calendar = (Calendar) cal.clone();
        return this;
    }

    /** Set from a Date using the default timezone Calendar. */
    public VelaTimeSetting setFromDate(Date date) {
        calendar.setTime(date);
        return this;
    }

    /** Convenience: set to current system time. */
    public VelaTimeSetting setFromSystemNow() {
        this.calendar = Calendar.getInstance();
        return this;
    }

    // ---- Getters ----
    public Calendar getCalendar() { return (Calendar) calendar.clone(); }
    public Date getDate() { return calendar.getTime(); }

    public int getYearFull() { return calendar.get(Calendar.YEAR); }
    public int getMonth1to12() { return calendar.get(Calendar.MONTH) + 1; }
    public int getDay() { return calendar.get(Calendar.DAY_OF_MONTH); }
    public int getHour() { return calendar.get(Calendar.HOUR_OF_DAY); }
    public int getMinute() { return calendar.get(Calendar.MINUTE); }
    public int getSecond() { return calendar.get(Calendar.SECOND); }

    @Override
    public String toString() {
        return "VelaTimeSetting{" +
                "yy=" + (getYearFull() % 100) +
                ", month=" + getMonth1to12() +
                ", day=" + getDay() +
                ", hour=" + getHour() +
                ", minute=" + getMinute() +
                ", second=" + getSecond() +
                ", date=" + getDate() +
                '}';
    }
}