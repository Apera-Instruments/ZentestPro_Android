package com.zen.api.protocol;

import android.util.Log;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class CalibrationPh implements Convent<CalibrationPh> {
    private byte[] data=new byte[14];
    private Date date=new Date();

    private int cailIndex;

    private double slope1;

    private double slope2;

    private double offset1;

    private double offset2;
    private boolean a168;
    private boolean a400;
    private boolean a700;
    private boolean a1001;
    private boolean a1245;

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getCailIndex() {
        return cailIndex;
    }

    public void setCailIndex(int cailIndex) {
        this.cailIndex = cailIndex;
    }

    public void setSlope1(double slope1) {
        this.slope1 = slope1;
    }

    public void setSlope2(double slope2) {
        this.slope2 = slope2;
    }

    private int pointCount;


    @Override
    public CalibrationPh unpack(byte[] data) {
        this.data=data;
        int bH;
        int bL;
        int b;
/*
* 数据1

1word（2bytes）
Bit15-bit10：年的后两位
Bit9-bit5:日
Bit4-bit0:时

*
* */
        bH = 0x00ff&(int)data[1];
        bL = 0x00ff&(int)data[2];
        int year = 2000+ (bH >> 2);
        int day = ((bH & 0x03) << 8) + bL >> 5;
        int hour = bL & 0x1f;



/*
* 数据2

1word（2bytes）
Bit15-bit12：月
Bit11-bit6:分
Bit5-bit0:秒

*
* */
        bH = 0x00ff&(int)data[3];
        bL = 0x00ff&(int)data[4];
        int month = bH >> 4;
        int min = ((bH & 0x07) << 8) + bL >> 6;
        int sec = bL & 0x1f;
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.MONTH, month-1);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, min);
        calendar.set(Calendar.SECOND, sec);
        date.setTime(calendar.getTimeInMillis());
/*
*
* 数据3

1byte
Bit0:1.68/
bit1:4.00/
bit2:7.00/
bit3:10.01/
bit4:12.45/
bit5,bit6表示几点校正(0,1,2,3)

*
* */
        b=0x00ff&(int)data[5];
        cailIndex = b&0x1f;
        a168=(cailIndex&1)!=0;
        a400=(cailIndex&(1<<1))!=0;
        a700=(cailIndex&(1<<2))!=0;
        a1001=(cailIndex&(1<<3))!=0;
        a1245=(cailIndex&(1<<4))!=0;

        pointCount  = b&0x03;


/*
* 数据4

1word(2bytes)
Offset1*10+1000,单位：mV
数据5

1word(2bytes)
Slope1*10，单位%

 0x50 494b 5396 24 0433 03e8 03e8 03e8
*
* */
        bH =0x000f&(int) data[6];
        bL =0x00ff&(int) data[7];
        offset1 = (bH<<8) + bL;
        offset1 -=1000;
        offset1/=10;
        bH =0x000f&(int) data[8];
        bL =0x00ff&(int) data[9];
        slope1 = ((double)((bH<<8) + bL)) /10;
/*
*
* 数据6

1word(2bytes)
Offset2*10+1000,单位：mV
数据7

1word(2bytes)
Slope2*10，单位%

*
* */
        bH =0x000f&(int) data[10];
        bL =0x00ff&(int) data[11];
        offset2 = (bH<<8) + bL;
        offset2 -=1000;
        offset2/=10;
        bH =0x000f&(int) data[12];
        bL =0x00ff&(int) data[13];
        slope2 = ((double)((bH<<8) + bL)) /10;
        return this;
    }

    @Override
    public byte[] pack() {
        data[0] =CODE;
        Log.d(TAG, Arrays.toString(data));
        return data;
    }
    public static final byte CODE = 0x50;
    @Override
    public byte getCode() {
        return CODE;
    }

    @Override
    public String toString() {
/*
* 03-04 20:09:23.058 31366-31366/com.zen.zentest I/BtApiImpl: CalibrationPh
                                                            pointNum:2
                                                            cailIndex:6
                                                            offset1:196608.0
                                                            slope1:192
                                                            offset2:0.0
                                                            slope2:196608
*
* */
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.getClass().getSimpleName());
        stringBuilder.append(" ");
        stringBuilder.append("pointNum");
        stringBuilder.append(":");
        stringBuilder.append(pointCount);
        stringBuilder.append(" ");
        stringBuilder.append("cailIndex");
        stringBuilder.append(":");
        stringBuilder.append(cailIndex);
        stringBuilder.append(" ");
        stringBuilder.append("offset1");
        stringBuilder.append(":");
        stringBuilder.append(offset1);
        stringBuilder.append(" ");
        stringBuilder.append("slope1");
        stringBuilder.append(":");
        stringBuilder.append(slope1);
        stringBuilder.append(" ");
        stringBuilder.append("offset2");
        stringBuilder.append(":");
        stringBuilder.append(offset2);
        stringBuilder.append(" ");
        stringBuilder.append("slope2");
        stringBuilder.append(":");
        stringBuilder.append(slope2);
        stringBuilder.append(" ");


        return stringBuilder.toString();

    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public double getSlope1() {
        return slope1;
    }



    public double getOffset1() {
        return offset1;
    }

    public void setOffset1(double offset1) {
        this.offset1 = offset1;
    }

    public double getSlope2() {
        return slope2;
    }



    public double getOffset2() {
        return offset2;
    }

    public void setOffset2(double offset2) {
        this.offset2 = offset2;
    }

    public boolean is168() {
        return a168;
    }

    public boolean isA168() {
        return a168;
    }

    public void setA168(boolean a168) {
        this.a168 = a168;
    }

    public boolean is400() {
        return a400;
    }

    public boolean isA400() {
        return a400;
    }

    public void setA400(boolean a400) {
        this.a400 = a400;
    }

    public boolean is700() {
        return a700;
    }

    public boolean isA700() {
        return a700;
    }

    public void setA700(boolean a700) {
        this.a700 = a700;
    }

    public boolean is1001() {
        return a1001;
    }

    public boolean isA1001() {
        return a1001;
    }

    public void setA1001(boolean a1001) {
        this.a1001 = a1001;
    }

    public boolean is1245() {
        return a1245;
    }

    public boolean isA1245() {
        return a1245;
    }

    public void setA1245(boolean a1245) {
        this.a1245 = a1245;
    }

    public int getPointCount() {
        return pointCount;
    }

    public void setPointCount(int pointCount) {
        this.pointCount = pointCount;
    }

    public void setSlope1(int slope1) {
        this.slope1 = slope1;
    }

    public void setSlope2(int slope2) {
        this.slope2 = slope2;
    }
    @Override
    public String getData() {
        return okio.ByteString.of(data).hex();
    }

    public String toJson() {
        return  com.alibaba.fastjson.JSON.toJSONString(this);
    }
}
