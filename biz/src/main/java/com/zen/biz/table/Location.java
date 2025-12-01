package com.zen.biz.table;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class Location {
    @Id(autoincrement = true)
    private Long id;
    private String userId;
    private String locationId;//currentUserId+ name+timestamp
    private String name;
    private String categoryName;
    private String address;
    private String longitude;//经度
    private String latitude; //纬度
    private long updateTime;
    private long createTime;
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
    public String getAddress() {
        return this.address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public String getCategoryName() {
        return this.categoryName;
    }
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getLocationId() {
        return this.locationId;
    }
    public void setLocationId(String locationId) {
        this.locationId = locationId;
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
    @Generated(hash = 1373230213)
    public Location(Long id, String userId, String locationId, String name,
            String categoryName, String address, String longitude, String latitude,
            long updateTime, long createTime) {
        this.id = id;
        this.userId = userId;
        this.locationId = locationId;
        this.name = name;
        this.categoryName = categoryName;
        this.address = address;
        this.longitude = longitude;
        this.latitude = latitude;
        this.updateTime = updateTime;
        this.createTime = createTime;
    }
    @Generated(hash = 375979639)
    public Location() {
    }

}
