package com.zen.api.protocol;

import android.util.Log;

import java.util.Arrays;
import java.util.Calendar;

public class Time implements Convent<Time>{
    private byte[] data=new byte[7];
    private  Calendar calendar = Calendar.getInstance();
    @Override
    public Time unpack(byte[] data) {
        Log.d(TAG, Arrays.toString(data));
        this.data=data;
        int year = data[1];
        int month = data[2];
        int day = data[3];
        int hour = data[4];
        int minute = data[5];
        int second = data[6];
        calendar.set(Calendar.YEAR,year);

        calendar.set(Calendar.MONTH,month-1);
        calendar.set(Calendar.DAY_OF_MONTH,day);
        calendar.set(Calendar.HOUR_OF_DAY,hour);
        calendar.set(Calendar.MINUTE,minute);
        calendar.set(Calendar.SECOND,second);
        Log.d(TAG,calendar.toString());
        return this;
    }

    @Override
    public byte[] pack() {
        Log.d(TAG,calendar.toString());
        int year =  calendar.get(Calendar.YEAR);
        data[0] = CODE;
        data[1] = (byte) (year%100);
        data[2] = (byte) (calendar.get(Calendar.MONTH)+1);
        data[3] = (byte) calendar.get(Calendar.DAY_OF_MONTH);
        data[4] = (byte) calendar.get(Calendar.HOUR_OF_DAY);
        data[5] = (byte) calendar.get(Calendar.MINUTE);;
        data[6] = (byte) calendar.get(Calendar.SECOND);;
        Log.d(TAG, Arrays.toString(data));
        return data;
    }
        /*
    * 同步时间
命令字 0x44  1byte “D”
数据1 1byte 年的后两位，如2018年的18
数据2 1byte 月(18-63)
数据3 1byte 日(1-12)
数据4 1byte 小时(0-23)
数据5 1byte 分钟(0-59)
数据6 1byte 秒(0-59)
  * */
        public static final byte CODE = 0x044;
    @Override
    public byte getCode() {
        return CODE;
    }
    @Override
    public String getData() {
        return okio.ByteString.of(data).hex();
    }

}
