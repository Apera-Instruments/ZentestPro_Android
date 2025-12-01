package com.zen.biz.json;

public class ServiceRespond {
    String msg;
    String code;
    com.alibaba.fastjson.JSONObject data;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public com.alibaba.fastjson.JSONObject getData() {
        return data;
    }

    public void setData(com.alibaba.fastjson.JSONObject data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String data) {
        this.msg = data;
    }
}
