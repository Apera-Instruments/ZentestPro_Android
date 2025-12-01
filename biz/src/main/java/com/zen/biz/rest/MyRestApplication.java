package com.zen.biz.rest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 * Created by louis on 17-11-22.
 */
public class MyRestApplication {
    private static MyRestApplication instance = new MyRestApplication();
    private Context mContext;
    private String mDevId;
    private SharedPreferences sharedPreferences;

    public static MyRestApplication getInstance() {
        return instance;
    }

    public  void setContext(Context context) {
        MyRestApplication.instance.mContext = context;
    }

    public Context getApplicationContext() {
        return mContext.getApplicationContext();
    }

    public String getDevId() {
        return mDevId;
    }
    public void setDevId(String devId) {
        mDevId = devId;
    }

    public SharedPreferences getSharedPreferences() {
        if(sharedPreferences==null){
            sharedPreferences = getApplicationContext().getSharedPreferences("MyRestApplication",Context.MODE_PRIVATE);
        }
        return sharedPreferences;
    }



    public void sendBroadcast(String action) {
        Intent intent = new Intent(action);
        intent.setPackage(mContext.getPackageName());
        getApplicationContext().sendBroadcast(intent);
    }
}
