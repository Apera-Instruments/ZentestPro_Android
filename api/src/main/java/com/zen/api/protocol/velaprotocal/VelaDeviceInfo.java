package com.zen.api.protocol.velaprotocal;

import android.util.Log;

import com.zen.api.protocol.Convent;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;

/**
 * VelaDeviceInfo
 * Data0='N'(0x4E): 电极序列号(上传)
 * Data1..Data10: ASCII serial number (10 bytes, may be NUL-padded).
 */
public class VelaDeviceInfo implements Convent<VelaDeviceInfo> {

    public static final String TAG  = "VelaDeviceInfo";
    public static final byte   CODE = 0x4E; // 'N'

    private byte[] raw = new byte[11];  // Data0..Data10
    private String serial;              // parsed ASCII (trimmed)

    // --- helpers ---
    private static int u8(byte b) { return b & 0xFF; }

    @Override
    public VelaDeviceInfo unpack(byte[] data) {
        if (data == null || data.length < 11) {
            Log.e(TAG, "Frame too short for DeviceInfo (need >=11), len=" + (data == null ? 0 : data.length));
            return null;
        }
        if (u8(data[0]) != u8(CODE)) {
            Log.e(TAG, String.format(Locale.US, "Unexpected Data0 0x%02X (expected 0x%02X)", data[0], CODE));
            return null;
        }

        this.raw = Arrays.copyOf(data, 11);

        // Extract Data1..Data10
        byte[] snBytes = Arrays.copyOfRange(data, 1, 11);

        // Convert to ASCII string; trim trailing NULs and spaces
        String ascii = new String(snBytes, StandardCharsets.US_ASCII)
                .replace('\u0000', ' ')     // normalize NULs to spaces first
                .trim();

        // If the result ends up empty but bytes are non-zero, fall back to hex
        if (ascii.isEmpty()) {
            boolean anyNonZero = false;
            for (byte b : snBytes) if (b != 0x00) { anyNonZero = true; break; }
            if (anyNonZero) {
                // hex fallback like "4E313233..."
                StringBuilder sb = new StringBuilder();
                for (byte b : snBytes) sb.append(String.format("%02X", b));
                ascii = sb.toString();
            }
        }

        this.serial = ascii;
        return this;
    }

    @Override
    public byte[] pack() {
        // Uplink-only by spec. If we already have raw, echo it; otherwise synthesize from serial (if provided).
        if (raw == null || raw.length < 11) raw = new byte[11];
        raw[0] = CODE;

        if (serial != null) {
            byte[] buf = serial.getBytes(StandardCharsets.US_ASCII);
            int len = Math.min(buf.length, 10);
            Arrays.fill(raw, 1, 11, (byte) 0x00);
            System.arraycopy(buf, 0, raw, 1, len);
        }
        Log.d(TAG, "pack: " + Arrays.toString(raw));
        return Arrays.copyOf(raw, raw.length);
    }

    @Override
    public byte getCode() { return CODE; }

    @Override
    public String getData() {
        return okio.ByteString.of(raw != null ? raw : new byte[]{}).hex();
    }

    // --- getters / setters ---
    public String getSerial() { return serial; }

    /** Optional: set serial (ASCII). When packing, will be NUL-padded to 10 bytes. */
    public void setSerial(String serial) { this.serial = serial == null ? "" : serial; }

    @Override
    public String toString() {
        return "VelaDeviceInfo{serial='" + serial + "'}";
    }
}