package com.zen.api.protocol;

public class Version implements Convent<Version> {
    public static final byte CODE = 0x56;
    private byte[] data;

    @Override
    public Version unpack(byte[] data) {
        this.data = data;
        return this;
    }

    @Override
    public byte[] pack() {
        return new byte[0];
    }

    @Override
    public byte getCode() {
        return CODE;
    }

    @Override
    public String getData() {
        return data!=null?okio.ByteString.of(data).hex():null;
    }

    public String getVersion(){
       if(data!=null&&data.length>=3) return data[1]+"."+data[2];
       else return "0.0";
    }
}
