package com.zen.biz.rest;


import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.zen.api.RestApi;
import com.zen.api.event.LogoutEvent;

import org.greenrobot.eventbus.EventBus;
import org.w3c.dom.Text;

import retrofit2.*;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;


/**
 * Created by xinhua.lin on 2016/11/1.
 */
public abstract class RestUtil extends RestBase implements RestApi {
    protected final static String secretKey ="ZenTest@QD201802201802201802" ;
    protected final Service service;
    private boolean mLogining=false;
    ;
    protected String userId="0";
    protected String token;

    protected String userName;
    private boolean mDemoLogin=false;


    private void updatePhoneInfo(){

        try {
            Log.i(TAG,"updatePhoneInfo ");
            mDataMap.put("systemInfo", "Android ;" + ReadSysVer()+" ;" + (mDataMap.containsKey("IsRoot")?mDataMap.get("IsRoot"):""));


        }catch (Throwable e){
            Log.e(TAG,"getDeviceInfo Exception",e);

        }
    }

    public void setDefault() {
        super.setDefault();
        updatePhoneInfo();


    }

    public String getUserName() {
        return userName;
    }

    public RestUtil(Context context,String url) {
        MyRestApplication.getInstance().setContext(context);
        mCache = getCache();
        initHttpClient();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)//Constants.HOST)
                .addConverterFactory(FastJsonConverterFactory.create())
                .client(client)
                .build();
        service = retrofit.create(Service.class);
        setDefault();
        load();
        Log.i(TAG,"RestUtil new instance");

    }

    protected abstract void load();


    public void setLogout() {
        Log.w(TAG,"setLogout");
        userId="0";
        token = null;
        mDemoLogin = false;
        setDefault();
        EventBus.getDefault().post(new LogoutEvent());

    }
    public void logout(){
        setLogout();
    }


    public boolean isLogin(){
        return !TextUtils.isEmpty(userId) && !TextUtils.isEmpty(token);
    }
    public boolean isDemoLogin(){
        return mDemoLogin;
    }

    public void setDemoLogin(boolean b){
         mDemoLogin=b;
    }

    public String getUserId(){
        return userId==null?"0":userId;
    }
}
