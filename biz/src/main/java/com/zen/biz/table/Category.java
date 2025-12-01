package com.zen.biz.table;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class Category {
    @Id(autoincrement = true)
    private Long id;
    private String name;
    private String userId;
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
    public String getUserId() {
        return this.userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    @Generated(hash = 1401358230)
    public Category(Long id, String name, String userId, long updateTime,
            long createTime) {
        this.id = id;
        this.name = name;
        this.userId = userId;
        this.updateTime = updateTime;
        this.createTime = createTime;
    }
    @Generated(hash = 1150634039)
    public Category() {
    }
}
