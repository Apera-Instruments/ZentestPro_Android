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
    private boolean useState = true;
    private String calibration;
    private String valueUnit;
    private Double phValue;
    private Double condValue;
    private Double orpValue;

    private String pHUnit;
    private String condUnit;
    private String orpUnit;

    @Generated(hash = 1269324560)
    public Record(Long id, String noteId, String locationId, String devId, int type,
            String traceNo, String attach, long updateTime, long createTime, String localPic,
            String pic, double value, int tableType, String userId, boolean sync,
            String category, String location, String notes, String noteName,
            String deviceNumber, String operator, String potential, String tempValue,
            String tempUnit, String syncId, boolean useState, String calibration,
            String valueUnit, Double phValue, Double condValue, Double orpValue,
            String pHUnit, String condUnit, String orpUnit) {
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
        this.phValue = phValue;
        this.condValue = condValue;
        this.orpValue = orpValue;
        this.pHUnit = pHUnit;
        this.condUnit = condUnit;
        this.orpUnit = orpUnit;
    }

    @Generated(hash = 477726293)
    public Record() {
    }

    

    // ALL GETTERS AND SETTERS -----------------

    public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }

    public String getNoteId() { return this.noteId; }
    public void setNoteId(String noteId) { this.noteId = noteId; }

    public String getLocationId() { return this.locationId; }
    public void setLocationId(String locationId) { this.locationId = locationId; }

    public String getDevId() { return this.devId; }
    public void setDevId(String devId) { this.devId = devId; }

    public int getType() { return this.type; }
    public void setType(int type) { this.type = type; }

    public String getTraceNo() { return this.traceNo; }
    public void setTraceNo(String traceNo) { this.traceNo = traceNo; }

    public String getAttach() { return this.attach; }
    public void setAttach(String attach) { this.attach = attach; }

    public long getUpdateTime() { return this.updateTime; }
    public void setUpdateTime(long updateTime) { this.updateTime = updateTime; }

    public long getCreateTime() { return this.createTime; }
    public void setCreateTime(long createTime) { this.createTime = createTime; }

    public String getLocalPic() { return this.localPic; }
    public void setLocalPic(String localPic) { this.localPic = localPic; }

    public String getPic() { return this.pic; }
    public void setPic(String pic) { this.pic = pic; }

    public double getValue() { return this.value; }
    public void setValue(double value) { this.value = value; }

    public int getTableType() { return this.tableType; }
    public void setTableType(int tableType) { this.tableType = tableType; }

    public String getUserId() { return this.userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public boolean getSync() { return this.sync; }
    public void setSync(boolean sync) { this.sync = sync; }

    public String getCategory() { return this.category; }
    public void setCategory(String category) { this.category = category; }

    public String getLocation() { return this.location; }
    public void setLocation(String location) { this.location = location; }

    public String getNotes() { return this.notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getNoteName() { return this.noteName; }
    public void setNoteName(String noteName) { this.noteName = noteName; }

    public String getDeviceNumber() { return this.deviceNumber; }
    public void setDeviceNumber(String deviceNumber) { this.deviceNumber = deviceNumber; }

    public String getOperator() { return this.operator; }
    public void setOperator(String operator) { this.operator = operator; }

    public String getPotential() { return this.potential; }
    public void setPotential(String potential) { this.potential = potential; }

    public String getTempValue() { return this.tempValue; }
    public void setTempValue(String tempValue) { this.tempValue = tempValue; }

    public String getTempUnit() { return this.tempUnit; }
    public void setTempUnit(String tempUnit) { this.tempUnit = tempUnit; }

    public String getSyncId() { return this.syncId; }
    public void setSyncId(String syncId) { this.syncId = syncId; }

    public boolean getUseState() { return this.useState; }
    public void setUseState(boolean useState) { this.useState = useState; }

    public String getCalibration() { return this.calibration; }
    public void setCalibration(String calibration) { this.calibration = calibration; }

    public String getValueUnit() { return this.valueUnit; }
    public void setValueUnit(String valueUnit) { this.valueUnit = valueUnit; }

    public Double getPhValue() { return phValue; }
    public void setPhValue(Double phValue) { this.phValue = phValue; }

    public Double getCondValue() { return condValue; }
    public void setCondValue(Double condValue) { this.condValue = condValue; }

    public Double getOrpValue() { return orpValue; }
    public void setOrpValue(Double orpValue) { this.orpValue = orpValue; }

    public String getpHUnit() { return pHUnit; }
    public void setpHUnit(String pHUnit) { this.pHUnit = pHUnit; }

    public String getCondUnit() { return condUnit; }
    public void setCondUnit(String condUnit) { this.condUnit = condUnit; }

    public String getOrpUnit() { return orpUnit; }
    public void setOrpUnit(String orpUnit) { this.orpUnit = orpUnit; }

    public String getPHUnit() {
        return this.pHUnit;
    }

    public void setPHUnit(String pHUnit) {
        this.pHUnit = pHUnit;
    }
}
