package com.zen.api.protocol;

import android.text.TextUtils;
import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParmDown implements Convent<ParmDown> {
    private byte[] data=new byte[15];

    public byte getPhTime() {
        return phTime;
    }

    public void setPhTime(byte phTime) {
        this.phTime = phTime;
    }

    public byte getCondTime() {
        return condTime;
    }

    public void setCondTime(byte condTime) {
        this.condTime = condTime;
    }

    public byte getPowerCloseTimer() {
        return powerCloseTimer;
    }

    public void setPowerCloseTimer(byte powerCloseTimer) {
        this.powerCloseTimer = powerCloseTimer;
    }

    private byte phTime=0;
    private byte condTime=0;
    private byte powerCloseTimer=0;
    /*
    *
    * “Q”
pH buffer select :0/USA  1/NIST
Auto Hold:0/OxFF,3-30second
Backlight auto close: 0x00/ON,1-8minutes
Temperature compensate factor:0~9.99%
TDS factor:0.40~1.00
Datum Tempature 温度补偿系数 25度
Reference Temp
Salt unit:0/ppt,1/g/L
Salt type:0:F=0.5,1/NaCl,2/Seawater
Temperature unit:0/,1/
pH calibration Due to:0/OFF,1~99/Hours
101~199/Days
Con calibration Due to:0/OFF,1~99/Hours
101~199/Days

只有 pH 和 CON
ORP 不校准  sampleView 空白

    *
    * */

    public int getTempUnit() {
        return tempUnit;
    }

    public void setTempUnit(int tempUnit) {
        this.tempUnit = tempUnit;
    }

    public int getAutoHold() {
        return autoHold;
    }

    public void setAutoHold(int autoHold) {
        this.autoHold = autoHold;
    }

    public int getBackLight() {
        return backLight;
    }

    public void setBackLight(int backLight) {
        this.backLight = backLight;
    }

    public int getpHSelect() {
        return pHSelect;
    }

    public void setpHSelect(int pHSelect) {
        this.pHSelect = pHSelect;
    }

    public int getpHResolution() {
        return pHResolution;
    }

    public void setpHResolution(int pHResolution) {
        this.pHResolution = pHResolution;
    }

    public int getRefTemp() {
        return refTemp;
    }

    public void setRefTemp(int refTemp) {
        this.refTemp = refTemp;
    }

    public int getTempCompFactor() {
        return tempCompFactor;
    }

    public void setTempCompFactor(int tempCompFactor) {
        this.tempCompFactor = tempCompFactor;
    }

    public int getTDSFactor() {
        return TDSFactor;
    }

    public void setTDSFactor(int TDSFactor) {
        this.TDSFactor = TDSFactor;
    }

    public int getSaltUnit() {
        return saltUnit;
    }

    public void setSaltUnit(int saltUnit) {
        this.saltUnit = saltUnit;
    }

    public int getTempCompensate() {
        return tempCompensate;
    }

    public void setTempCompensate(int tempCompensate) {
        this.tempCompensate = tempCompensate;
    }

    public int getResetType() {
        return resetType;
    }

    public void setResetType(int resetType) {
        this.resetType = resetType;
    }

    private int tempUnit;
    private int autoHold;
    private int backLight;
    private int pHSelect;
    private int pHResolution;
    private int refTemp;
    private int tempCompFactor;
    private int TDSFactor;
    private int saltUnit;


/*
    int SaltUnit;
    int SaltType;

    int phCalibration;
    int condCalibration;
*/



    private int tempCompensate;

    public static final int RESET_CODE=0x53;
    public static final int TYPE1=1;
    public static final int TYPE2=3;

    private int resetType=0;

    @Override
    public ParmDown unpack(byte[] data) {
        this.data=data;return this;
    }

    @Override
    public byte[] pack() {
        data[0] =CODE;
        data[1] = (byte) tempUnit;
        data[2] = (byte) autoHold;
        data[3] = (byte) backLight;
        data[4] = (byte) (pHSelect | (pHResolution<<4));
        //data[5] = (byte) ;//0x53
        if(resetType ==TYPE1){
            data[5] = (byte)RESET_CODE ;//0x53
        }
        data[6] = (byte)refTemp ;
        data[7] = (byte)((tempCompensate>>8)&0x00ff) ;
        data[8] = (byte)(tempCompensate&0x00ff) ;
        //data[9] = (byte) ;//0x53
        if(resetType ==TYPE2){
            data[9] = (byte)RESET_CODE ;//0x53
        }
        data[10] = (byte) TDSFactor;
        data[11] = (byte) saltUnit;
        data[12] = (byte) powerCloseTimer;
        data[13] = (byte) phTime;
        data[14] = (byte) condTime;

        /*
        *     private int tempUnit;
    private int autoHold;
    private int backLight;
    private int pHSelect;
    private int pHResolution;
    private int refTemp;
    private int tempCompFactor;
    private int TDSFactor;
    private int saltUnit;
        * */
        Log.d(TAG, Arrays.toString(data)
                +"tempUnit="+tempUnit +" autoHold=" + autoHold
                +" backLight="+backLight+" pHSelect="+pHSelect +" pHResolution = "
                +pHResolution +" refTemp="
                +refTemp+" tempCompensate="+tempCompensate +" TDSFactor="
                +TDSFactor+" saltUnit="+saltUnit +" resetType = "+resetType );
        return data;
    }
    public static final byte CODE = 0x51;
    @Override
    public byte getCode() {
        return CODE;
    }

    public static ParmDown factoryReset(int type) {
        ParmDown parmDown =  new ParmDown();
        parmDown.resetType = type;
        return parmDown;
    }




    public void setPHSelect(String PHSelect) {
       if("USA".equals(PHSelect)) pHSelect = ParmUp.USA;
       else pHSelect = ParmUp.NIST;
    }


    public void setPHResolution(String PHResolution) {
        if("0.1".equals(PHResolution)) pHResolution = ParmUp.PH_RESOLUTION_01;
        else pHResolution = ParmUp.PH_RESOLUTION_001;
    }


    public void setAutoHold(String autoHold) {
        if ("OFF".equals(autoHold)) {
            this.autoHold = 0;
            return;
        }
        String str = autoHold;
        Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher(str);
        if (m.find()) {
            this.autoHold = Integer.parseInt(m.group());
        }
    }




    public void setBackLight(String backLight) {
        if ("OFF".equals(backLight)) {
            this.backLight = 0;
            return;
        }
        String str = backLight;
        Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher(str);
        if (m.find()) {
            this.backLight = Integer.parseInt(m.group());
        }

    }



    public void setTDSFactor(String TDSFactor) {
        if(!TextUtils.isEmpty(TDSFactor))
        {
            this.TDSFactor = (int) (Float.parseFloat(TDSFactor)*100);
        }
    }



    public void setSaltUnit(String saltUnit) {
        if(TextUtils.isEmpty(saltUnit)) return;
        if("ppt".equals(saltUnit)) this.saltUnit = ParmUp.SALT_UNIT_PPT;
        else this.saltUnit = ParmUp.SALT_UNIT_GL;

    }


    public void setTempUnit(String tempUnit) {
        if(TextUtils.isEmpty(tempUnit)) return;
        //return tempUnit == TEMP_UNIT_C ? "℃" : "℉";
        if("℃".equals(tempUnit)) this.tempUnit = ParmUp.TEMP_UNIT_C;
        else this.tempUnit = ParmUp.TEMP_UNIT_F;
    }


    public void setRefTemp(String refTemp) {
        if(TextUtils.isEmpty(refTemp)) return;
        this.refTemp = Integer.parseInt(refTemp.replace("℃",""));
    }

    public void setTempCompensate(String tempCompensate) {
        if(TextUtils.isEmpty(tempCompensate)) return;
        this.tempCompensate = (int) (100*Float.parseFloat(tempCompensate.replace("%","")));
    }
    @Override
    public String getData() {
        return okio.ByteString.of(data).hex();
    }

    public void setPowerOff(@Nullable String string) {
        if ("OFF".equals(string)) {
            this.powerCloseTimer = 0;
            return;
        }
        String str = string;
        Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher(str);
        if (m.find()) {
            this.powerCloseTimer = (byte) Integer.parseInt(m.group());
        }
    }

    public void setPhDueTime(@Nullable String string) {
        if ("OFF".equals(string)) {
            this.phTime = 0;
            return;
        }
        String str = string;
        Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher(str);
        if (m.find()) {
            this.phTime = (byte) Integer.parseInt(m.group());
            if(string.endsWith("Days")){
                this.phTime+=100;
            }
        }
    }

    public void setCondDueTime(@Nullable String string) {
        if ("OFF".equals(string)) {
            this.condTime = 0;
            return;
        }
        String str = string;
        Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher(str);
        if (m.find()) {
            this.condTime = (byte) Integer.parseInt(m.group());
            if(string.endsWith("Days")){
                this.condTime+=100;
            }
        }
    }
}
