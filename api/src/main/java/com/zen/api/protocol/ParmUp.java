package com.zen.api.protocol;

import android.util.Log;

import java.util.Arrays;

public class ParmUp implements Convent<ParmUp> {
    private byte[] data=new byte[12];
    public static final int USA=0;
    public static final int NIST=1;

    public static final int TEMP_UNIT_C=1;
    public static final int TEMP_UNIT_F=0;
    public static final int SALT_UNIT_PPT=0;
    public static final int SALT_UNIT_GL=1;
    public static final int PH_RESOLUTION_01=1;
    public static final int PH_RESOLUTION_001=2;


    /*
    * “G”
pH buffer select :0/USA  1/NIST
Auto Hold:0/OFF,3-30second
Backlight auto close: 0/ON,1-8minutes
Temperature compensate factor:0~9.99%
TDS factor:0.40~1.00
放在home  SampleView中
Salt unit:0/ppt,1/g/L
Salt type:0:F=0.5,1/NaCl,2/Seawater
Temperature unit:0/,1/
pH calibration Due to:0/OFF,1~99/Hours
101~199/Days
Cond calibration Due to:
0/OFF,1~99/Hours,101~199/Days

    *
    *
    * */

    private int pHSelect;
    private int autoHold;//3-30second
    private int backLight;//0/ON,1-8minutes
    //int tempCompFactor;//0~9.99%
    private int TDSFactor;//0.40~1.00
    private int saltUnit;//0/ppt,1/g/L
    //int SaltType;//0:F=0.5,1/NaCl,2/Seawater
    private int tempUnit;//0/,1/
    //int phCalibration;//0/OFF,1~99/Hours 101~199/Days
    //int condCalibration;//0/OFF,1~99/Hours,101~199/Days
    private int refTemp;
    private  int tempCompensate;
    private int pHResolution;
    private int phTime=0;

    public int getPhTime() {
        return phTime;
    }

    public void setPhTime(int phTime) {
        this.phTime = phTime;
    }

    public int getCondTime() {
        return condTime;
    }

    public void setCondTime(int condTime) {
        this.condTime = condTime;
    }

    public int getPowerCloseTimer() {
        return powerCloseTimer;
    }

    public void setPowerCloseTimer(int powerCloseTimer) {
        this.powerCloseTimer = powerCloseTimer;
    }

    private int condTime=0;
    private int powerCloseTimer=0;


    @Override
    public ParmUp unpack(byte[] data) {
        if(data.length<15){
            Log.w(TAG,"ParmUp unpack err ,len <12 ,"+data.length);
            return null;
        }
        this.data=data;
        //0x4701000020001900c800470050482a10000003e8
        tempUnit =  0x00ff&(int)data[1];
        autoHold =0x00ff&(int) data[2]  ;
        backLight = 0x00ff&(int)data[3]  ;
        int r = 0x00ff&(int)data[4];
        pHSelect =r&0x01    ;
        pHResolution=(r&0x10 >>4)    ;
        //TDSFactor = 0x00ff&(int)data[5]  ;
        refTemp =0x00ff&(int) data[6]  ;
        int r1 =0x00ff&(int) data[7]  ; //8
        int r2 =0x00ff&(int) data[8]  ; //8
        tempCompensate = r2 + (r1<<8);
        //phCalibration =0x00ff&(int) data[9]  ;
        TDSFactor =0x00ff&(int) data[10]  ;
        saltUnit =0x00ff&(int) data[11]  ;
        powerCloseTimer =0x00ff&(int) data[12]  ;
        phTime =0x00ff&(int) data[13]  ;
        condTime =0x00ff&(int) data[14]  ;
        return this;
    }

    @Override
    public byte[] pack() {
        data[0] =CODE;

        Log.d(TAG, Arrays.toString(data));
        return data;
    }
    public static final byte CODE = 0x47;
    @Override
    public byte getCode() {
        return CODE;
    }


    /*
    *
    * 02-28 22:29:42.246 20752-20752/com.zen.zentest W/BtApiImpl: onCharacteristicChanged 0x4700000000c8470000010000504b8915ab45ffa6
02-28 22:29:42.253 20752-20752/com.zen.zentest I/BtApiImpl: pHSelect:0
                                                            autoHold:0
                                                            backLight:0
                                                            tempCompFactor:0
                                                            TDSFactor:-56
                                                            SaltUnit:71
                                                            SaltType:0
                                                            tempUnit:0
                                                            phCalibration:1
                                                            condCalibration:0
    *
    *
    * */
    @Override
    public String toString() {


        return this.getClass().getSimpleName() +
                "\n" +
                "pHSelect" +
                ":" +
                pHSelect + " " + getPHSelect() +
                "\n" +
                "pHResolution" +
                ":" +
                pHResolution + " " + getPHResolution() +
                "\n" +
                "autoHold" +
                ":" +
                autoHold + " " + getAutoHold() +
                "\n" +
                "backLight" +
                ":" +
                backLight + " " + getBackLight() +
                "\n" +
                "TDSFactor" +
                ":" +
                TDSFactor + " " + getTDSFactor() +
                "\n" +
                "saltUnit" +
                ":" +
                saltUnit + " " + getSaltUnit() +
                "\n" +
                "tempUnit" +
                ":" +
                tempUnit + " " + getTempUnit()
                + "\n" +
                "refTemp" +
                ":" +
                refTemp + " " + getRefTemp()
                + "\n" +
                "tempCompensate" +
                ":" +
                tempCompensate + " " + getTempCompensate()
                ;

    }

    public String getHoldTime() {
        //Auto Hold:0/OFF,3-30second
        if(autoHold==0) return "OFF";
        return ""+autoHold+" Sec.";
    }

    public String getReadingTime() {
        return "";
    }

    public String getCailbrationRmd() {
        return "";
    }

    public String getBacklightTime() {
        ////0/ON,1-8minutes
        if(backLight==0) return "ON";
        return ""+backLight+" min";
    }

    public String getAutoPowerTime() {
        //
        if(powerCloseTimer==0) return "OFF";
        return String.format("%d Minutes", powerCloseTimer);
    }


    public String getTempUnit() {
        return tempUnit == TEMP_UNIT_C ? "℃" : "℉";
    }

    public String getSaltUnit() {
        return saltUnit == SALT_UNIT_PPT ? "ppt" : "g/l";
    }

    public String getTDSFactor() {
        return String.format("%.2f", ((float) TDSFactor) / 100);
    }

    public String getBackLight() {
        if (backLight == 0) return "OFF";
        return String.format("%d Minutes", backLight);
    }

    public String getAutoHold() {
        if (autoHold == 0) return "OFF";
        return String.format("%d Seconds", autoHold);
    }

    public String getPHResolution() {
        return pHResolution == PH_RESOLUTION_01 ? "0.1" : "0.01";
    }

    public String getPHSelect() {
        return pHSelect == USA ? "USA" : "NIST";

    }

    public String getRefTemp() {
        return String.format("%d",refTemp);
    }

    public String getPhDuCalTime() {
        if (phTime == 0) return "OFF";
        if (phTime <= 100)
            return String.format("%d Hours", phTime);
        else
            return String.format("%d Days", (phTime - 100));

    }

    public String getCondDuCalTime() {
        if (condTime == 0) return "OFF";
        if (condTime <= 100)
            return String.format("%d Hours", condTime);
        else
            return String.format("%d Days", (condTime - 100));

    }
    //



    public String getTempCompensate() {
        return String.format("%.2f%%",((float)tempCompensate)/100) ;
    }
    @Override
    public String getData() {
        return okio.ByteString.of(data).hex();
    }
}
