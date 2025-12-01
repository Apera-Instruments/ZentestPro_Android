package com.zen.api.protocol;

import android.util.Log;

import java.util.Arrays;

public class Key implements Convent<Key> {
    private byte[] data=new byte[2];
    private byte mKey=0x00;
    public static final int Calibration = 1;
    public static final int HOLD = 1<<1;
    public static final int ENT = 1<<2;
    public static final int ESC = 1<<3;
    public Key() {

    }
    public Key(int k) {
        mKey = (byte) k;
    }

    // Bit0:Calibration
  //  Bit1:”HOLD”
  //  Bit2:ENT
   // Bit3:ESC
    @Override
    public Key unpack(byte[] data) {
        this.data=data; return this;
    }

    @Override
    public byte[] pack() {
        data[0] =CODE;
        data[1] = mKey;
        Log.d(TAG, Arrays.toString(data));
        return data;
    }
    public static final byte CODE = 0x4b;
    @Override
    public byte getCode() {
        return CODE;
    }
    @Override
    public String getData() {
        return okio.ByteString.of(data).hex();
    }
}
