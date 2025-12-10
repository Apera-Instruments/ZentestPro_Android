package com.zen.api;

import android.content.Context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MyApi {
    private Context mContext;
    private static MyApi instance = new MyApi();
    private RestApi restApi;
    private String currentDeviceMac = null;
    private DataApi dataApi;
    private BtApi btApi;
    private final Map<String, BtApi> btApiMap = new ConcurrentHashMap<>();
    private final Map<String, DataApi> dataApiMap = new ConcurrentHashMap<>();

    public static MyApi getInstance() {
        return instance;
    }

    public void setContext(Context mContext) {
        this.mContext = mContext;
    }

    public void setRestApi(RestApi restApi) {
        this.restApi = restApi;
    }

    public RestApi getRestApi() {
        return restApi;
    }

    public void setDataApi(DataApi dataApi) {
        this.dataApi = dataApi;
    }

    public DataApi getDataApi() {
        return dataApiMap.get(currentDeviceMac);
    }
    public void setBtApi(BtApi btApi) {
        this.btApi = btApi;
    }

    public BtApi getBtApi() {
        if (currentDeviceMac == null) return null;
        return btApiMap.get(currentDeviceMac);
    }

    public BtApi getBtApi(String mac) {
        if (currentDeviceMac == null) return null;
        return btApiMap.get(mac);
    }

    public DataApi getDataApi(String mac) {
        return dataApiMap.get(mac);
    }

    public void registerDevice(String mac, BtApi btApi, DataApi dataApi) {
        btApiMap.put(mac, btApi);
        dataApiMap.put(mac, dataApi);

        if (currentDeviceMac == null) {
            currentDeviceMac = mac; // first device becomes “current”
        }
    }

    public String getCurrentDeviceMac() {
        return currentDeviceMac;
    }

    public void setCurrentDevice(String mac) {
        if (btApiMap.containsKey(mac)) {
            currentDeviceMac = mac;
        }
    }

    public String getString(int id) {
        return mContext.getString(id);
    }
}
