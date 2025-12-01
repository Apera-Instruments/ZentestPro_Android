package com.zen.api.protocol;

import android.util.Log;

import java.util.Arrays;

public class Data implements Convent<Data> {
    private byte[] data=new byte[6];



    private int mode=0;
    public static final  int pH=0x00;
    public static final  int mV=0x01;//ORP
    public static final  int Cond=0x02;//us
    public static final  int TDS=0x03;//ppm
    public static final  int Salt=0x04;//ppt
    public static final  int Res=0x05;
    private int battery;
    private int pointDigit;


    public static final  int UNIT_C=0x02;
    public static final  int UNIT_F=0x01;
    public static final  int UNIT_pH=0x01;
    public static final  int UNIT_mV=0x02;
    public static final  int UNIT_mS=0x03;
    public static final  int UNIT_uS=0x04;
    public static final  int UNIT_ppm=0x05;
    public static final  int UNIT_ppt=0x06;
    public static final  int UNIT_gl=0x07;
    public static final  int UNIT_mgl=0x08;
    public static final  int UNIT_Ω=0x09;
    public static final  int UNIT_KΩ=10;
    public static final  int UNIT_MΩ=11;
    private int unit;

    private int value=0;


      private boolean upperAlarm;
      private boolean lowerAlarm;
      private boolean hold;
      private boolean laughFace;
      private boolean H;
      private boolean M;
      private boolean L;


private int pointDigit2;
private int unit2;
private int value2;


    @Override
    public Data unpack(byte[] data) {
        if(data==null||data.length<7){
            Log.e(TAG,"data length not match ,should is 7");
            return null;
        }
        this.data=data;
        /*
 *
 * Bit7-bit5:测量参数
(0:pH/1:mV/2:Cond/3:TDS/4:Salt/5:Res)
Bit4-bit2:电池电量
Bit1,bit0:主显示小数点位数
*/
        int bH;
        int bL;
        int b= 0x00ff&(int)data[1];
        mode =(0x07 &  (b>>5));
        battery =(0x07 & (b>>2));
        pointDigit = b&0x03;

/*
Bit15-bit12:测量单位(1:pH/2:mV/3:mS/4:uS/5:ppm
/6:ppt/7:g/L/8:mg/L/9:Ω/10:KΩ/11:MΩ)
Bit11-bit0:测量数值+2000
现在只能设置ph  14.00 –> 1400
*/
        bH=0x00ff&(int) data[2];
        bL=0x00ff&(int) data[3];
        unit = (0x0f&(bH>>4));
        value =(((int)bH&0x0f)<<8)  + bL -2000;


/*
Bit6:上限标志
Bit5:下限标志
Bit4:HOLD标志
Bit3:笑脸标志
Bit2-bit0:HML标志
H M L （000）bit位数对应
1:显示 0：不显示
*/
        b =  0x00ff&(int)data[4];
        upperAlarm = ((b&(1<<6))!=0);
        lowerAlarm = ((b&(1<<5))!=0);
        hold = ((b&(1<<4))!=0);
        laughFace = ((b&(1<<3))!=0);
        H = ((b&(1<<2))!=0);
        M = ((b&(1<<1))!=0);
        L = ((b&(1<<0))!=0);
        /*
Bit14,bit13:副显示区小数点位置
Bit12: ºC
Bit11: ºF
Bit10-bit0:副显示区数值
*/
        bH=  0x00ff&(int)data[5];
        bL= 0x00ff&(int)data[6];
        pointDigit2 = 0x003&(bH>>5);
        unit2 = 0x003&(bH>>3);
        value2 = ((int)(0x007&(bH)) <<8) + bL;
        return this;
    }

    @Override
    public byte[] pack() {
        data[0] =CODE;

        Log.d(TAG, Arrays.toString(data));
        return data;
    }
    public static final byte CODE = 0x53;
    @Override
    public byte getCode() {
        return CODE;
    }


    @Override
    public String toString() {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("mode");
        stringBuilder.append(":");
        stringBuilder.append(mode);
        stringBuilder.append(" ;");
        stringBuilder.append("unit");
        stringBuilder.append(":");
        stringBuilder.append(unit);
        stringBuilder.append(" ;");
        stringBuilder.append("pointDigit");
        stringBuilder.append(":");
        stringBuilder.append(pointDigit);
        stringBuilder.append(" ;");
        stringBuilder.append("value");
        stringBuilder.append(":");
        stringBuilder.append(value);
        stringBuilder.append(" ;");
        stringBuilder.append("upperAlarm");
        stringBuilder.append(":");
        stringBuilder.append(upperAlarm);
        stringBuilder.append(" ;");
        stringBuilder.append("lowerAlarm");
        stringBuilder.append(":");
        stringBuilder.append(lowerAlarm);
        stringBuilder.append(" ;");
        stringBuilder.append("hold");
        stringBuilder.append(":");
        stringBuilder.append(hold);
        stringBuilder.append(" ;");
        stringBuilder.append("laughFace");
        stringBuilder.append(":");
        stringBuilder.append(laughFace);
        stringBuilder.append(" ;");
        stringBuilder.append("H");
        stringBuilder.append(":");
        stringBuilder.append(H);
        stringBuilder.append(" ;");
        stringBuilder.append("M");
        stringBuilder.append(":");
        stringBuilder.append(M);
        stringBuilder.append(" ;");
        stringBuilder.append("L");
        stringBuilder.append(":");
        stringBuilder.append(L);
        stringBuilder.append(" ;");
        stringBuilder.append("pointDigit2");
        stringBuilder.append(":");
        stringBuilder.append(pointDigit2);
        stringBuilder.append(" ;");
        stringBuilder.append("unit2");
        stringBuilder.append(":");
        stringBuilder.append(unit2);
        stringBuilder.append(" ;");
        stringBuilder.append("value2");
        stringBuilder.append(":");
        stringBuilder.append(value2);
        stringBuilder.append(" ;");
        return stringBuilder.toString();

    }

    public double getValue() {
        return (double)value/Math.pow(10,pointDigit);
    }
    public double getTemp() {
        return (double)value2/Math.pow(10,pointDigit2);
    }
    public double getEC() {
        return mode==Cond?getValue():0;
    }

    public double getCond() {
        return mode==Cond?getValue():0;
    }

    public double getORP() {
        return mode==mV?getValue():0;
    }

    public double getPH() {
        return mode==pH?getValue():0;
    }

    public double getResistivity() {
        return mode==Res?getValue():0;
    }


    public double getTDS() {
        return mode==TDS?getValue():0;
    }

    public double getSalinity() {
        return mode==Salt?getValue():0;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void setValue2(int value2) {
        this.value2 = value2;
    }

    public void setPointDigit(int pointDigit) {
        this.pointDigit = pointDigit;
    }

    public void setPointDigit2(int pointDigit2) {
        this.pointDigit2 = pointDigit2;
    }

    public int getPointDigit( ) {
        return this.pointDigit;
    }

    public int getPointDigit2( ) {
        return  this.pointDigit2 ;
    }

    public boolean isUpperAlarm(){return upperAlarm;}
    public boolean isLowerAlarm(){return lowerAlarm;}
    public boolean isHold(){return hold;}
    public boolean isLaughFace(){return laughFace;}
    public boolean isH(){return H;}
    public boolean isM(){return M;}
    public boolean isL(){return L;}

    public void setL(boolean l) {
        this.L = l;
    }
    public void setM(boolean l) {
        this.M = l;
    }
    public void setH(boolean l) {
        this.H = l;
    }

    public double getValue2() {
        return (double)value2/Math.pow(10,pointDigit2);
    }
    /*
    Bit15-bit12:测量单位(1:pH/2:mV/3:mS/4:uS/5:ppm
    /6:ppt/7:g/L/8:mg/L/9:Ω/10:KΩ/11:MΩ)
    Bit11-bit0:测量数值+2000
    现在只能设置ph  14.00 –> 1400
    */
    public String getUnitString() {
        String string = "";
        switch (unit) {
            case UNIT_pH:
                string = "pH";
                break;
            case UNIT_mV:
                string = "mV";
                break;
            case UNIT_mS:
                string = "mS";
                break;
            case UNIT_uS:
                string = "µS";
                break;
            case UNIT_ppm:
                string = "ppm";
                break;
            case UNIT_ppt:
                string = "ppt";
                break;
            case UNIT_gl:
                string = "g/L";
                break;
            case UNIT_mgl:
                string = "mg/L";
                break;
            case UNIT_Ω:
                string = "Ω";
                break;
            case UNIT_KΩ:
                string = "KΩ";
                break;
            case UNIT_MΩ:
                string = "MΩ";
                break;
        }


        return string;
    }

    public int getIntValue2() {
        return value2;
    }

    public int getUnit2() {
        return unit2;
    }

    public void setUnit2(int unit2) {
        this.unit2 = unit2;
    }

    public void setUnit(int unit) {
        this.unit = unit;
    }
    @Override
    public String getData() {
        return okio.ByteString.of(data).hex();
    }

    public boolean isCalibration(){
        return unit2 ==0;
    }
}
