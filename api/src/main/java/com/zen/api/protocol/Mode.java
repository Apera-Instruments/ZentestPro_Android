package com.zen.api.protocol;

import android.util.Log;

import java.util.Arrays;

public class Mode implements Convent<Mode> {
    private byte[] data=new byte[2];
    /*
    * 00：pH/01：mV/02:Cond/
03:TDS/04:Salt/05:Res
    *
    * */
    private int mode=0;
    public static final  int pH=0x00;
    public static final  int mV=0x01;
    public static final  int Cond=0x02;
    public static final  int TDS=0x03;
    public static final  int Salt=0x04;
    public static final  int Res=0x05;

    @Override
    public Mode unpack(byte[] data) {
        this.data=data;return this;
    }

    @Override
    public byte[] pack() {
        data[0] =CODE;
        data[1] = (byte) mode;

        Log.d(TAG, Arrays.toString(data));
        return data;
    }
    public static final byte CODE = 0x4d;
    @Override
    public byte getCode() {
        return CODE;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getMode() {
        return mode;
    }
    @Override
    public String getData() {
        return okio.ByteString.of(data).hex();
    }
}
