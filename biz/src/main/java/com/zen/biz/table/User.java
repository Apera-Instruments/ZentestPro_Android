package com.zen.biz.table;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;

/**
 * Created by louis on 17-9-6.
 */
@Entity
public class User {
    @Id(autoincrement = true)
    private Long id;
    private String userId;
    private String name;
    private String email;
    private String token;
    private String password;
    private String salt;
    private Long timestamp;
    private boolean currentUser;
    private int userStatus;
    private long updateTime;
    private long createTime;

    @Transient
    private int tempUsageCount; // not persisted

    @Generated(hash = 295450565)
    public User(Long id, String userId, String name, String email, String token,
            String password, String salt, Long timestamp, boolean currentUser,
            int userStatus, long updateTime, long createTime) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.token = token;
        this.password = password;
        this.salt = salt;
        this.timestamp = timestamp;
        this.currentUser = currentUser;
        this.userStatus = userStatus;
        this.updateTime = updateTime;
        this.createTime = createTime;
    }

    @Generated(hash = 586692638)
    public User() {
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

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return this.token;
    }

    public void seToken(String token) {
        this.token = token;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSalt() {
        return this.salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }


    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public boolean isCurrentUser() {
        return this.currentUser;
    }

    public void setCurrentUser(boolean b) {
        this.currentUser = b;
    }


    public Long getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean getCurrentUser() {
        return this.currentUser;
    }

    public void setToken(String token) {
        this.token = token;
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

    public int getUserStatus() {
        return this.userStatus;
    }

    public void setUserStatus(int userStatus) {
        this.userStatus = userStatus;
    }


}
