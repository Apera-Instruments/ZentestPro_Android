package com.zen.api.protocol;

import android.util.Log;

import java.util.Arrays;

public class Measure implements Convent<Measure> {
    private byte[] data=new byte[2];
    private byte status=0;
    public final static byte ContinuousMeasureON = 1<<3;
    public final static byte LOW_ALARM_ON = 1;
    public final static byte UPPER_ALARM_ON = 1<<1;
    public final static byte DUE_CALI_ON = 1<<2;
    public final static byte OFF = 0;

    public Measure() {

    }
    public Measure(byte on) {
        status = (byte) on;
    }

    @Override
    public Measure unpack(byte[] data) {
        this.data=data;
        return this;
    }

    @Override
    public byte[] pack() {
        data[0] =CODE;
        data[1] = status;
        Log.d(TAG, Arrays.toString(data));
        return data;
    }
    public static final byte CODE = 0x53;
    @Override
    public byte getCode() {
        return CODE;
    }
    @Override
    public String getData() {
        return okio.ByteString.of(data).hex();
    }
}
