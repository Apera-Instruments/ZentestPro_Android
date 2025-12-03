package com.zen.api.protocol.velaprotocal;

import android.util.Log;

import com.zen.api.protocol.Convent;

import java.util.Arrays;
import java.util.Locale;

public class VelaParamModeDeviceToApp implements Convent<VelaParamModeDeviceToApp> {

    public static final String TAG = "VelaParamModeUpload";
    public static final byte CODE = 0x4D; // 'M'

    private static int u8(byte b){ return b & 0xFF; }

    public enum PhMvMode { PH, MV }
    public enum CondMode { COND, TDS, SALT, RESISTIVITY }

    private byte[] raw = new byte[2];
    private boolean phmvSelected;
    private PhMvMode phmvMode = PhMvMode.PH;

    private boolean condSelected;
    private CondMode condMode = CondMode.COND;

    private boolean orpSelected;

    @Override
    public VelaParamModeDeviceToApp unpack(byte[] data) {
        if (data == null || data.length < 2) {
            Log.e(TAG, "len < 2 for M frame");
            return null;
        }
        if (u8(data[0]) != u8(CODE)) {
            Log.e(TAG, String.format(Locale.US, "Data0 0x%02X != 'M'", u8(data[0])));
            return null;
        }
        this.raw = Arrays.copyOf(data, 2);
        int b = u8(data[1]);

        // bit1: selected, bit0: submode 0=PH 1=MV
        phmvSelected = (b & (1<<1)) != 0;
        phmvMode = ((b & 0x01) != 0) ? PhMvMode.MV : PhMvMode.PH;

        // bit4: selected, bits3..2: 0,1,2,3
        condSelected = (b & (1<<4)) != 0;
        int condVal = (b >> 2) & 0x03;
        switch (condVal) {
            case 0: condMode = CondMode.COND; break;
            case 1: condMode = CondMode.TDS; break;
            case 2: condMode = CondMode.SALT; break;
            default: condMode = CondMode.RESISTIVITY; break;
        }

        // bit5: ORP selected
        orpSelected = (b & (1<<5)) != 0;

        return this;
    }

    @Override
    public byte[] pack() {
        // Uplink-only parser; echo last raw
        return Arrays.copyOf(raw, raw.length);
    }

    @Override public byte getCode() { return CODE; }
    @Override public String getData() { return okio.ByteString.of(raw).hex(); }

    // Getters
    public boolean isPhmvSelected() { return phmvSelected; }
    public PhMvMode getPhmvMode() { return phmvMode; }
    public boolean isCondSelected() { return condSelected; }
    public CondMode getCondMode() { return condMode; }
    public boolean isOrpSelected() { return orpSelected; }

    @Override
    public String toString() {
        return "VelaParamModeUpload{" +
                "phmvSelected=" + phmvSelected +
                ", phmvMode=" + phmvMode +
                ", condSelected=" + condSelected +
                ", condMode=" + condMode +
                ", orpSelected=" + orpSelected +
                '}';
    }
}