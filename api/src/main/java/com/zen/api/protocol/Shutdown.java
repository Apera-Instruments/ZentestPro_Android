package com.zen.api.protocol;

import android.util.Log;

import java.util.Arrays;

public class Shutdown implements Convent<Shutdown> {
    private byte[] data=new byte[2];
    private final int CLOSE =0 ;
    private final int OPEN =1 ;
    private int state =CLOSE ;
    @Override
    public Shutdown unpack(byte[] data) {
        this.data=data;
        state= data[1];
        return this;
    }

    @Override
    public byte[] pack() {
        data[0] =CODE;
        data[1] = (byte) state;
        Log.d(TAG, Arrays.toString(data));
        return data;
    }
    public static final byte CODE = 0x57;
    @Override
    public byte getCode() {
        return CODE;
    }

    public boolean isOFF() {
        return state == CLOSE;
    }
    @Override
    public String getData() {
        return okio.ByteString.of(data).hex();
    }
}
