package com.zen.api;

import android.content.Context;

public class MyApi {
    private Context mContext;
    private static MyApi instance = new MyApi();


    private RestApi restApi;
    private DataApi dataApi;
    private BtApi btApi;

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
        return dataApi;
    }


    public void setBtApi(BtApi btApi) {
        this.btApi = btApi;
    }

    public BtApi getBtApi() {
        return btApi;
    }

    public String getString(int id) {
        return mContext.getString(id);
    }
}
