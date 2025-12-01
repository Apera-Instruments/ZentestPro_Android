package com.zen.api.protocol;

import android.util.Log;

import java.util.Arrays;

public class Error implements Convent<Error> {
    private byte[] data=new byte[2];

    private int errCode=0;
    public static final  int Err1=0x01;
    public static final  int Err2=0x02;
    public static final  int Err3=0x03;
    public static final  int Err4=0x04;
    public static final  int Err5=0x05;
    public static final  int Err6=0x06;
    private String fixString;
    private String errString;


    /*
    * Err1: 校正溶液错误
Err2: 测量未稳定
Err3: 校正时超过3分钟未稳定
Err4: 零点超标
Err5: 斜率超标
Err6：超过校正时限提醒
    *
    * */
    @Override
    public Error unpack(byte[] data) {
        this.data=data;
        int b= 0x00ff&(int)data[1];
        errCode = b;
        getErrString();
        return this;
    }

    @Override
    public byte[] pack() {
        data[0] =CODE;


        Log.d(TAG, Arrays.toString(data));
        return data;
    }
    public static final byte CODE = 0x45;
    @Override
    public byte getCode() {
        return CODE;
    }

    public int getErrCode() {
        return errCode;
    }
    @Override
    public String getData() {
        return okio.ByteString.of(data).hex();
    }

    public void setErr(int err) {
        this.errCode = err;
        getErrString();

    }

    public String getErrString() {
/*
        switch (errCode){
            case Err1:
                errString =null;//"校准溶液错误";
                setFixString(null);
                break;
            case Err2:
                errString =null;//"测量未稳定";
                setFixString(null);//"1,检查仪器与电极连接是否良好\n2,检查电极是否损坏");
                break;
            case Err3:
                errString ="校准超过3分钟未稳定";
                setFixString("");
                break;
            case Err4:
                errString ="零点超标";
                setFixString("");
                break;
            case Err5:
                errString ="斜率超标";
                setFixString("");
                break;
            case Err6:
                errString ="超过校准时限提醒";
                setFixString("");
                break;
        }*/
        //setFixString(null);
        return errString;
    }

    public String getFixString() {
        return fixString;
    }

    public void setFixString(String fixString) {
        this.fixString = fixString;
    }

    public void setErrString(String string) {
        this.errString = string;
    }
}
