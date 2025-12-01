package com.zen.zentest

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
// import android.support.multidex.MultiDexApplication
import androidx.multidex.MultiDexApplication
import androidx.core.app.ActivityCompat
import android.util.Log

import com.alibaba.fastjson.JSON
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.CrashUtils
import com.blankj.utilcode.util.Utils
import com.foolchen.lib.tracker.Tracker
import com.foolchen.lib.tracker.data.TrackerMode
import com.foolchen.lib.tracker.lifecycle.ITrackerContext
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.CsvFormatStrategy
import com.orhanobut.logger.DiskLogAdapter
import com.orhanobut.logger.FormatStrategy
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy
//import com.pgyersdk.crash.PgyCrashManager
import com.zen.biz.Install

class MyApp : MultiDexApplication(), ITrackerContext {

    override fun onCreate() {
        super.onCreate()
        Tracker.addProperty("build_date", BuildConfig.DATE);
        Tracker.addProperty("git_revision", BuildConfig.GIT_REVISION);

        val sharedPreferences = getSharedPreferences("policy-agreement", Context.MODE_PRIVATE)
        val i = sharedPreferences.getInt("Version", 0)
        // Tracker.addProperty("附加的属性2", "附加的属性2");
        // 设定上报数据的主机和接口
        // 注意：该方法一定要在Tracker.initialize()方法前调用
        // 否则会由于上报地址未初始化，在触发启动事件时导致崩溃

        Tracker.setService("http://www.makedown.win:13101", "report");
        // 设定上报数据的项目名称
        Tracker.setProjectName(getString(R.string.app_name));
        // 设定上报数据的模式
   /*     if (BuildConfig.DEBUG) {
            Tracker.setMode(TrackerMode.DEBUG_TRACK)
        } else */
        //{
            Tracker.setMode(TrackerMode.RELEASE)
        //};
        Tracker.setChannelId("pgy");
        // 初始化AndroidTracker
        if (i==1){
            //先屏蔽试试
//            Tracker.initialize(this);
        }

        Utils.init(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            CrashUtils.init(getExternalFilesDir("crash")!!) {
                e ->
                Log.e(TAG, "uncaughtException ", e)
                Tracker.setMode(TrackerMode.RELEASE)
                var p:MutableMap<String,Any> = HashMap();
                p.put("thread_name",Thread.currentThread().name)
                p.put("thread_id",Thread.currentThread().id)
                p.put("throwable_message",e.localizedMessage)
                p.put("throwable",Log.getStackTraceString(e))
                Tracker.trackEvent("Crash",p);
            }
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                CrashUtils.init(getExternalFilesDir("crash")!!) {
                    e ->
                    Log.e(TAG, "uncaughtException ", e)
                    Tracker.setMode(TrackerMode.RELEASE)
                    var p:MutableMap<String,Any> = HashMap();
                    p.put("thread_name",Thread.currentThread().name)
                    p.put("thread_id",Thread.currentThread().id)
                    p.put("throwable_message",e.localizedMessage)
                    p.put("throwable",Log.getStackTraceString(e))
                    Tracker.trackEvent("Crash",p);
                }
            } else {
                val uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
                Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
                    Log.e(TAG, "uncaughtException ", throwable)
                    Log.e(TAG, "uncaughtException at thread " + thread.name + " " + thread.id)
                    Tracker.setMode(TrackerMode.RELEASE)
                    var p:MutableMap<String,Any> = HashMap();
                    p.put("thread_name",thread.name)
                    p.put("thread_id",thread.id)
                    p.put("throwable_message",throwable.localizedMessage)
                    p.put("throwable",Log.getStackTraceString(throwable))
                    Tracker.trackEvent("Crash",p);
                    // PgyCrashManager.reportCaughtException(MyApp.this, throwable);
                    uncaughtExceptionHandler.uncaughtException(thread, throwable)
                }
            }
        }

        if (false) {
//            Logger.addLogAdapter(AndroidLogAdapter())
        } else {
            val formatStrategy = PrettyFormatStrategy.newBuilder()
                    .showThreadInfo(false)
                    .methodOffset(0)
                    .tag(BuildConfig.APP_NAME)
                    .methodCount(0).build()
//            Logger.addLogAdapter(AndroidLogAdapter(formatStrategy))
        }
        //    FormatStrategy bleFormatStrategy = CsvFormatStrategy.newBuilder()
        //  .tag("ble")
        //           .build();
        /*   PrettyFormatStrategy uiFormatStrategy = PrettyFormatStrategy.newBuilder()
                .tag("ui")
                .build();
        PrettyFormatStrategy defaultFormatStrategy = PrettyFormatStrategy.newBuilder()
                .build();*/
//        Logger.addLogAdapter(DiskLogAdapter())
        // Logger.addLogAdapter(new DiskLogAdapter(uiFormatStrategy));
        // Logger.addLogAdapter(new DiskLogAdapter(defaultFormatStrategy));
//        Logger.json(JSON.toJSONString(AppUtils.getAppInfo()))
        //        Logger.d("is root "+ (AppUtils.isAppRoot()));
        //PgyCrashManager.register(this)

        //先不用
//        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
//            startForegroundService(Intent(this, LogService::class.java))
//        }else{
//            startService(Intent(this, LogService::class.java))
//        }

        startService(Intent(this, LogService::class.java))

        Install.getInstance().init(this)

        Tracker.trackEvent("startUp",null);

//        Log.w("init", "AppVersion: " + Install.getAppVersion() + "; git version:" + BuildConfig.GIT_REVISION + "; Build date:" + BuildConfig.DATE)
    }

    companion object {
        private val TAG = "MyApp"
    }
}
