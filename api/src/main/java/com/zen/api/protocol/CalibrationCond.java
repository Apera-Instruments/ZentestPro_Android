package com.zen.api.protocol;

import android.util.Log;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class CalibrationCond implements Convent<CalibrationCond> {
    private byte[] data = new byte[12];
    private Date date = new Date();

    private int cailIndex;

    private double slope1;

    private double slope2;

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

    private double offset1;

    private double offset2;
    private int pointCount;
    private boolean a84;
    private boolean a1413;
    private boolean a1288;

    @Override
    public CalibrationCond unpack(byte[] data) {
        this.data =data;
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
        bH = 0x00ff & (int) data[1];
        bL = 0x00ff & (int) data[2];
        int year = 2000 + (bH >> 2);
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
        bH = 0x00ff & (int) data[3];
        bL = 0x00ff & (int) data[4];
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
        b = 0x00ff & (int) data[5];
        cailIndex = b & 0x1f;
        a84 = (cailIndex & 1) != 0;
        a1413 = (cailIndex & (1 << 1)) != 0;
        a1288 = (cailIndex & (1 << 2)) != 0;
        pointCount = b & 0x03;


/*
* 数据4

1word(2bytes)
Offset1*10+1000,单位：mV
数据5

1word(2bytes)
Slope1*10，单位%

*
* */
        bH = 0x00ff & (int) data[6];
        bL = 0x00ff & (int) data[7];
        slope1 = (bH << 8) + bL;
        slope1 /= 100;
        bH = 0x00ff & (int) data[8];
        bL = 0x00ff & (int) data[9];
        slope2 = (bH << 8) + bL;
        slope2 /= 100;

        bH = 0x00ff & (int) data[10];
        bL = 0x00ff & (int) data[11];
        offset1 = (bH << 8) + bL;
        offset1 /= 100;
        /*  bH = 0x00ff&(int)data[12];
        bL = 0x00ff&(int)data[13];
        slope2 = bH<<8 + bL;*/
        return this;


    }

    @Override
    public byte[] pack() {
        data[0] = CODE;
        Log.d(TAG, Arrays.toString(data));
        return data;
    }

    public static final byte CODE = 0x43;

    @Override
    public byte getCode() {
        return CODE;
    }

    @Override
    public String getData() {
        return okio.ByteString.of(data).hex();
    }


    @Override
    public String toString() {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.getClass().getSimpleName());
        stringBuilder.append(" ");
        stringBuilder.append("pointCount");
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

    public int getPointCount() {
        return pointCount;
    }

    public void setPointCount(int pointCount) {
        this.pointCount = pointCount;
    }


    public boolean is84() {
        return a84;
    }

    public boolean isA84() {
        return a84;
    }

    public void setA84(boolean a84) {
        this.a84 = a84;
    }

    public boolean is1413() {
        return a1413;
    }

    public boolean isA1413() {
        return a1413;
    }

    public void setA1413(boolean a1413) {
        this.a1413 = a1413;
    }

    public boolean is1288() {
        return a1288;
    }

    public boolean isA1288() {
        return a1288;
    }

    public void setA1288(boolean a1288) {
        this.a1288 = a1288;
    }

    public String toJson() {
        return  com.alibaba.fastjson.JSON.toJSONString(this);
    }

}
