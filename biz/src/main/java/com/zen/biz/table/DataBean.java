package com.zen.biz.table;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class DataBean {
    @Id(autoincrement = true)
    private Long id;


    private String userId;
    private String traceNo;
    private String recordId;

    private int sn;
    private int type;//mode
    //主显示	指针显示
    //测量范围	单位
    private double pH;  //ph
    //	pH/温度	0~7~14	 pH
    private double ORP;  //mV
    //ORP/温度	-1000~0~1000	mV
    private double EC;//电导率  μS/cm
    //电导率/温度	0~200	μS/cm
    //0~2000	μS/cm
    //0~20	mS/cm
    private double TDS;//ppm
    //TDS/温度	0~1000	ppm
    //0~10	ppt
    private double salinity;//盐度 ppt
    //盐度/温度	盐度：0~10	ppt
    // 海水盐度：0~50	ppt
    //NaCl盐度：0~100	ppt
    private double resistivity;//电阻率 Ω·cm
    //电阻率/温度	0~100	Ω·cm
    //1~1000	KΩ·cm
    //1~20	MΩ·cm

    private double temp; //温度 .C

    private long timestamp;

    private String longitude;//经度

    private String latitude; //纬度



    private String remarks;

    private int uploadStatus;
    private int dataStatus;

    private String xInfo1;
    private String xInfo2;
    private int xInfo3;
    private int xInfo4;




    private String attach;

    private long updateTime;
    private long createTime;
    private String unitString;
    private int mode;
    private int unit2;
    private int pointDigit2;
    private int pointDigit;
    private double value;
    private double value2;

    @Generated(hash = 2013283316)
    public DataBean(Long id, String userId, String traceNo, String recordId,
            int sn, int type, double pH, double ORP, double EC, double TDS,
            double salinity, double resistivity, double temp, long timestamp,
            String longitude, String latitude, String remarks, int uploadStatus,
            int dataStatus, String xInfo1, String xInfo2, int xInfo3, int xInfo4,
            String attach, long updateTime, long createTime, String unitString,
            int mode, int unit2, int pointDigit2, int pointDigit, double value,
            double value2) {
        this.id = id;
        this.userId = userId;
        this.traceNo = traceNo;
        this.recordId = recordId;
        this.sn = sn;
        this.type = type;
        this.pH = pH;
        this.ORP = ORP;
        this.EC = EC;
        this.TDS = TDS;
        this.salinity = salinity;
        this.resistivity = resistivity;
        this.temp = temp;
        this.timestamp = timestamp;
        this.longitude = longitude;
        this.latitude = latitude;
        this.remarks = remarks;
        this.uploadStatus = uploadStatus;
        this.dataStatus = dataStatus;
        this.xInfo1 = xInfo1;
        this.xInfo2 = xInfo2;
        this.xInfo3 = xInfo3;
        this.xInfo4 = xInfo4;
        this.attach = attach;
        this.updateTime = updateTime;
        this.createTime = createTime;
        this.unitString = unitString;
        this.mode = mode;
        this.unit2 = unit2;
        this.pointDigit2 = pointDigit2;
        this.pointDigit = pointDigit;
        this.value = value;
        this.value2 = value2;
    }
    @Generated(hash = 908697775)
    public DataBean() {
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
    public int getDataStatus() {
        return this.dataStatus;
    }
    public void setDataStatus(int dataStatus) {
        this.dataStatus = dataStatus;
    }
    public int getUploadStatus() {
        return this.uploadStatus;
    }
    public void setUploadStatus(int uploadStatus) {
        this.uploadStatus = uploadStatus;
    }
    public String getRemarks() {
        return this.remarks;
    }
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
    public String getLatitude() {
        return this.latitude;
    }
    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }
    public String getLongitude() {
        return this.longitude;
    }
    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
    public long getTimestamp() {
        return this.timestamp;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    public double getTemp() {
        return this.temp;
    }
    public void setTemp(double temp) {
        this.temp = temp;
    }
    public double getResistivity() {
        return this.resistivity;
    }
    public void setResistivity(double resistivity) {
        this.resistivity = resistivity;
    }
    public double getSalinity() {
        return this.salinity;
    }
    public void setSalinity(double salinity) {
        this.salinity = salinity;
    }
    public double getTDS() {
        return this.TDS;
    }
    public void setTDS(double TDS) {
        this.TDS = TDS;
    }
    public double getEC() {
        return this.EC;
    }
    public void setEC(double EC) {
        this.EC = EC;
    }
    public double getORP() {
        return this.ORP;
    }
    public void setORP(double ORP) {
        this.ORP = ORP;
    }
    public double getPH() {
        return this.pH;
    }
    public void setPH(double pH) {
        this.pH = pH;
    }
    public int getType() {
        return this.type;
    }
    public void setType(int type) {
        this.type = type;
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

    public String getUserId() {
        return this.userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }


    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getRecordId() {
        return this.recordId;
    }
    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }


    public void setUnitString(String unitString) {
        this.unitString = unitString;
    }

    public String getUnitString() {
        return unitString;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getMode() {
        return mode;
    }

    public void setUnit2(int unit2) {
        this.unit2 = unit2;
    }

    public int getUnit2() {
        return unit2;
    }

    public void setPointDigit2(int pointDigit2) {
        this.pointDigit2 = pointDigit2;
    }

    public int getPointDigit2() {
        return pointDigit2;
    }

    public void setPointDigit(int pointDigit) {
        this.pointDigit = pointDigit;
    }

    public int getPointDigit() {
        return pointDigit;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public void setValue2(double value2) {
        this.value2 = value2;
    }

    public double getValue2() {
        return value2;
    }
}
