package com.zen.biz.table;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;


@Entity
public class Record {
    @Id(autoincrement = true)
    private Long id;
    private String noteId;
    private String locationId;
    private String devId;
    private int type;//mode
    private String traceNo;
    private String attach;
    private long updateTime;
    private long createTime;
    private String localPic;//[]
    private String pic; //[]
    private double value;
    private int tableType;
    private String userId;
    private boolean sync;
    private String category;
    private String location;
    private String notes;
    private String noteName;
    private String deviceNumber;
    private String operator;
    private String potential;
    private String tempValue;
    private String tempUnit;
    private String syncId;
    private boolean useState=true;
    private String calibration;
    private String valueUnit;


    @Generated(hash = 163116591)
    public Record(Long id, String noteId, String locationId, String devId,
            int type, String traceNo, String attach, long updateTime,
            long createTime, String localPic, String pic, double value,
            int tableType, String userId, boolean sync, String category,
            String location, String notes, String noteName, String deviceNumber,
            String operator, String potential, String tempValue, String tempUnit,
            String syncId, boolean useState, String calibration, String valueUnit) {
        this.id = id;
        this.noteId = noteId;
        this.locationId = locationId;
        this.devId = devId;
        this.type = type;
        this.traceNo = traceNo;
        this.attach = attach;
        this.updateTime = updateTime;
        this.createTime = createTime;
        this.localPic = localPic;
        this.pic = pic;
        this.value = value;
        this.tableType = tableType;
        this.userId = userId;
        this.sync = sync;
        this.category = category;
        this.location = location;
        this.notes = notes;
        this.noteName = noteName;
        this.deviceNumber = deviceNumber;
        this.operator = operator;
        this.potential = potential;
        this.tempValue = tempValue;
        this.tempUnit = tempUnit;
        this.syncId = syncId;
        this.useState = useState;
        this.calibration = calibration;
        this.valueUnit = valueUnit;
    }
    @Generated(hash = 477726293)
    public Record() {
    }


    public String getPic() {
        return this.pic;
    }
    public void setPic(String pic) {
        this.pic = pic;
    }
    public String getLocalPic() {
        return this.localPic;
    }
    public void setLocalPic(String localPic) {
        this.localPic = localPic;
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
    public String getAttach() {
        return this.attach;
    }
    public void setAttach(String attach) {
        this.attach = attach;
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
    public int getType() {
        return this.type;
    }
    public void setType(int type) {
        this.type = type;
    }
    public String getDevId() {
        return this.devId;
    }
    public void setDevId(String devId) {
        this.devId = devId;
    }
    public String getLocationId() {
        return this.locationId;
    }
    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }
    public String getNoteId() {
        return this.noteId;
    }
    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public void setTableType(int tableType) {
        this.tableType = tableType;
    }

    public int getTableType() {
        return tableType;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public boolean getSync() {
        return sync;
    }

    public boolean isSync() {
        return sync;
    }

    public void setSync(boolean sync) {
        this.sync = sync;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getNoteName() {
        return noteName;
    }

    public void setNoteName(String noteName) {
        this.noteName = noteName;
    }

    public String getDeviceNumber() {
        return deviceNumber;
    }

    public void setDeviceNumber(String deviceNumber) {
        this.deviceNumber = deviceNumber;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getPotential() {
        return potential;
    }

    public void setPotential(String potential) {
        this.potential = potential;
    }

    public String getTempValue() {
        return tempValue;
    }

    public void setTempValue(String tempValue) {
        this.tempValue = tempValue;
    }

    public String getTempUnit() {
        return tempUnit;
    }

    public void setTempUnit(String tempUnit) {
        this.tempUnit = tempUnit;
    }

    public String getSyncId() {
        return syncId;
    }

    public void setSyncId(String syncId) {
        this.syncId = syncId;
    }

    public void setUseState(boolean useState) {
        this.useState = useState;
    }

    public boolean isUseState() {
        return useState;
    }
    public boolean getUseState() {
        return this.useState;
    }

    public void setCalibration(String calibration) {
        this.calibration = calibration;
    }

    public String getCalibration() {
        return calibration;
    }

    public void setValueUnit(String valueUnit) {
        this.valueUnit = valueUnit;
    }

    public String getValueUnit() {
        return valueUnit;
    }
}
