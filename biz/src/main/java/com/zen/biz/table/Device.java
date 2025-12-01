package com.zen.biz.table;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class Device {
    @Id(autoincrement = true)
    private Long id;
    private String userId;
    private String devId;//userId + mac+timestamp
    private String traceNo;
    private int sn;

    private String mac;
    private String name;
    private long connectTime;
    private long disconnectTime;
    private long updateTime;
    private long createTime;
    private long lastDataTime;
    private long lastSettingTime;
    private long lastCalibrationTime;
    private String setting;


    private String firmwareVersion;
    private String instrumentModel;
    private String electrodeModel;
    private String serialNumber;
    private String factoryCalibrationTime;
    private String info;

    private int devStatus;
    private String remarks;
    private String xInfo1;
    private String xInfo2;
    private int xInfo3;
    private int xInfo4;
    private String attach;

    @Generated(hash = 1721940335)
    public Device(Long id, String userId, String devId, String traceNo, int sn,
            String mac, String name, long connectTime, long disconnectTime,
            long updateTime, long createTime, long lastDataTime,
            long lastSettingTime, long lastCalibrationTime, String setting,
            String firmwareVersion, String instrumentModel, String electrodeModel,
            String serialNumber, String factoryCalibrationTime, String info,
            int devStatus, String remarks, String xInfo1, String xInfo2,
            int xInfo3, int xInfo4, String attach) {
        this.id = id;
        this.userId = userId;
        this.devId = devId;
        this.traceNo = traceNo;
        this.sn = sn;
        this.mac = mac;
        this.name = name;
        this.connectTime = connectTime;
        this.disconnectTime = disconnectTime;
        this.updateTime = updateTime;
        this.createTime = createTime;
        this.lastDataTime = lastDataTime;
        this.lastSettingTime = lastSettingTime;
        this.lastCalibrationTime = lastCalibrationTime;
        this.setting = setting;
        this.firmwareVersion = firmwareVersion;
        this.instrumentModel = instrumentModel;
        this.electrodeModel = electrodeModel;
        this.serialNumber = serialNumber;
        this.factoryCalibrationTime = factoryCalibrationTime;
        this.info = info;
        this.devStatus = devStatus;
        this.remarks = remarks;
        this.xInfo1 = xInfo1;
        this.xInfo2 = xInfo2;
        this.xInfo3 = xInfo3;
        this.xInfo4 = xInfo4;
        this.attach = attach;
    }
    @Generated(hash = 1469582394)
    public Device() {
    }

    public String getAttach() {
        return this.attach;
    }
    public void setAttach(String attach) {
        this.attach = attach;
    }
    public int getXInfo4() {
        return this.xInfo4;
    }
    public void setXInfo4(int xInfo4) {
        this.xInfo4 = xInfo4;
    }
    public int getXInfo3() {
        return this.xInfo3;
    }
    public void setXInfo3(int xInfo3) {
        this.xInfo3 = xInfo3;
    }
    public String getXInfo2() {
        return this.xInfo2;
    }
    public void setXInfo2(String xInfo2) {
        this.xInfo2 = xInfo2;
    }
    public String getXInfo1() {
        return this.xInfo1;
    }
    public void setXInfo1(String xInfo1) {
        this.xInfo1 = xInfo1;
    }
    public String getRemarks() {
        return this.remarks;
    }
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
    public String getInfo() {
        return this.info;
    }
    public void setInfo(String info) {
        this.info = info;
    }
    public String getFactoryCalibrationTime() {
        return this.factoryCalibrationTime;
    }
    public void setFactoryCalibrationTime(String factoryCalibrationTime) {
        this.factoryCalibrationTime = factoryCalibrationTime;
    }
    public String getSerialNumber() {
        return this.serialNumber;
    }
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }
    public String getElectrodeModel() {
        return this.electrodeModel;
    }
    public void setElectrodeModel(String electrodeModel) {
        this.electrodeModel = electrodeModel;
    }
    public String getInstrumentModel() {
        return this.instrumentModel;
    }
    public void setInstrumentModel(String instrumentModel) {
        this.instrumentModel = instrumentModel;
    }
    public String getFirmwareVersion() {
        return this.firmwareVersion;
    }
    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }
    public String getSetting() {
        return this.setting;
    }
    public void setSetting(String setting) {
        this.setting = setting;
    }
    public long getLastCalibrationTime() {
        return this.lastCalibrationTime;
    }
    public void setLastCalibrationTime(long lastCalibrationTime) {
        this.lastCalibrationTime = lastCalibrationTime;
    }
    public long getLastSettingTime() {
        return this.lastSettingTime;
    }
    public void setLastSettingTime(long lastSettingTime) {
        this.lastSettingTime = lastSettingTime;
    }
    public long getLastDataTime() {
        return this.lastDataTime;
    }
    public void setLastDataTime(long lastDataTime) {
        this.lastDataTime = lastDataTime;
    }
    public long getCreateTime() {
        return this.createTime;
    }
    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
    public long getUpdateTime() {
        return this.updateTime;
    }
    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }
    public long getDisconnectTime() {
        return this.disconnectTime;
    }
    public void setDisconnectTime(long disconnectTime) {
        this.disconnectTime = disconnectTime;
    }
    public long getConnectTime() {
        return this.connectTime;
    }
    public void setConnectTime(long connectTime) {
        this.connectTime = connectTime;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getMac() {
        return this.mac;
    }
    public void setMac(String mac) {
        this.mac = mac;
    }
    public int getSn() {
        return this.sn;
    }
    public void setSn(int sn) {
        this.sn = sn;
    }
    public String getTraceNo() {
        return this.traceNo;
    }
    public void setTraceNo(String traceNo) {
        this.traceNo = traceNo;
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public int getDevStatus() {
        return this.devStatus;
    }
    public void setDevStatus(int devStatus) {
        this.devStatus = devStatus;
    }
    public String getDevId() {
        return this.devId;
    }
    public void setDevId(String devId) {
        this.devId = devId;
    }
    public String getUserId() {
        return this.userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }


}
