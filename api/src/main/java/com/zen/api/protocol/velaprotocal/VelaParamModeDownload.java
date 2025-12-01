package com.zen.api.protocol.velaprotocal;

import android.util.Log;

import com.zen.api.protocol.Convent;

import java.util.Arrays;

public class VelaParamModeDownload implements Convent<VelaParamModeDownload> {

    public static final String TAG = "VelaParamModeDownload";
    public static final byte CODE = 0x4D; // 'M'

    private byte[] raw = new byte[]{ CODE, 0x00 };

    public enum PhMvMode { PH, MV }
    public enum CondMode { COND, TDS, SALT, RESISTIVITY }

    private boolean phmvSelected = false;
    private PhMvMode phmvMode = PhMvMode.PH;

    private boolean condSelected = false;
    private CondMode condMode = CondMode.COND;

    private boolean orpSelected = false;

    @Override
    public VelaParamModeDownload unpack(byte[] data) {
        // Optional: accept an echo frame
        if (data != null && data.length >= 2 && (data[0] & 0xFF) == (CODE & 0xFF)) {
            raw = Arrays.copyOf(data, 2);
        }
        return this;
    }

    @Override
    public byte[] pack() {
        int b = 0;

        // pH/mV
        if (phmvSelected) b |= (1<<1);
        if (phmvMode == PhMvMode.MV) b |= (1<<0);

        // Cond/TDS/Salt/Resistivity
        if (condSelected) b |= (1<<4);
        int condVal = 0;
        switch (condMode) {
            case COND:        condVal = 0; break;
            case TDS:         condVal = 1; break;
            case SALT:        condVal = 2; break;
            case RESISTIVITY: condVal = 3; break;
        }
        b |= (condVal & 0x03) << 2;

        // ORP
        if (orpSelected) b |= (1<<5);

        raw = new byte[]{ CODE, (byte) (b & 0xFF) };
        Log.d(TAG, "M-DL: " + Arrays.toString(raw));
        return Arrays.copyOf(raw, raw.length);
    }

    @Override public byte getCode() { return CODE; }
    @Override public String getData() { return okio.ByteString.of(raw).hex(); }

    // Fluent setters
    public VelaParamModeDownload setPhmv(boolean selected, PhMvMode mode) {
        this.phmvSelected = selected;
        this.phmvMode = (mode == null ? PhMvMode.PH : mode);
        return this;
    }
    public VelaParamModeDownload setCond(boolean selected, CondMode mode) {
        this.condSelected = selected;
        this.condMode = (mode == null ? CondMode.COND : mode);
        return this;
    }
    public VelaParamModeDownload setOrp(boolean selected) {
        this.orpSelected = selected;
        return this;
    }
}