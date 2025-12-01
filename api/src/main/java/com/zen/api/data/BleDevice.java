package com.zen.api.data;

import com.zen.api.R;

public class BleDevice {
    public static final String DEMO = "demo";
    private String name;
    private String mac;
    private boolean demo=false;
    private DeviceSetting deviceSetting = new DeviceSetting();
    private String SN;
    private String modle;
    private String eleModle;
    public  int measuringParameterTextId=0;
    public int suitableAppTextId=0;
    public int compEleTextId=0;

    public BleDevice(){


    }

    public static BleDevice buildDemo(){
        BleDevice bleDevice = new BleDevice();
        bleDevice.setDemo(true);
        bleDevice.setName(DEMO);
        bleDevice.setMac(DEMO);
        return bleDevice;
    }

    public BleDevice(String name,String mac){
        demo = false;
        this.name = name;
        this.mac = mac;
    }
    public boolean isDemo() {
        return demo;
    }
    public void setDemo(boolean demo) {
        this.demo=demo;
    }
    public boolean getDemo() {
        return demo;
    }

    public String getMac() {
        return mac;
    }
    public void setMac(String mac) {
        this.mac =mac;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public DeviceSetting getSetting() {
        return deviceSetting;
    }

    public void setSetting(DeviceSetting setting) {
        this.deviceSetting = setting;
    }

    public void setSN(String SN) {
        this.SN = SN;
    }

    public String getSN() {
        return SN;
    }

    public void setModle(String modle) {
        this.modle = modle;
        if (modle != null) {
            if (modle.startsWith("PC60")) {
                this.eleModle = "PC60-DE";
                this.measuringParameterTextId = 0;
                this.suitableAppTextId = 0;
                this.compEleTextId = 0;
            }
            else if (modle.startsWith("PH60S")) {
                this.eleModle = "PH60S-DE";
                this.measuringParameterTextId = 0;
                this.suitableAppTextId = 0;
                this.compEleTextId = 0;
            }
            else if (modle.startsWith("PH60F")) {
                this.eleModle = "PH60F-DE";
                this.measuringParameterTextId = 0;
                this.suitableAppTextId = 0;
                this.compEleTextId = 0;
            }
            else if (modle.startsWith("EC60")) {
                this.eleModle = "EC60-DE";
                this.measuringParameterTextId = 0;
                this.suitableAppTextId = 0;
                this.compEleTextId = 0;
            }
            else if (modle.startsWith("ORP60")) {
                this.eleModle = "ORP60-TE";
                this.measuringParameterTextId = 0;
                this.suitableAppTextId = 0;
                this.compEleTextId = 0;
            }
            else if (modle.startsWith("PH60")) {
                this.eleModle = "PH60-DE";
                this.measuringParameterTextId = 0;
                this.suitableAppTextId = 0;
                this.compEleTextId = 0;
            } else if (modle.startsWith("UWS_Waterboy")) {
                this.eleModle = "PC60-DE";
                this.measuringParameterTextId = 0;
                this.suitableAppTextId = 0;
                this.compEleTextId = 0;
            }
        }

    }

    public String getModle() {
        return modle;
    }
    public String getEleModle() {
        return eleModle;
    }
}
