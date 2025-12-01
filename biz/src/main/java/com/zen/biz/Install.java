package com.zen.biz;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.clj.fastble.BleManager;
import com.zen.api.MyApi;
import com.zen.biz.greendao.gen.DaoMaster;
import com.zen.biz.greendao.gen.DaoSession;
import com.zen.biz.rest.MyRestApplication;
import org.greenrobot.greendao.database.Database;

public class Install {
    private static final  String SERVER_URL="https://www.zentest.tech:6060/";
    private static Install instance=new Install();
    private MyOpenHelper mHelper;
    private SQLiteDatabase db;
    private DaoMaster mDaoMaster;
    private DaoSession mDaoSession;
    private Application application;

    public static Install getInstance() {
        return instance;
    }

    public Context getContext() {
        return application;
    }

    public static class MyOpenHelper extends DaoMaster.OpenHelper {
        public MyOpenHelper(Context context, String name) {
            super(context, name);
        }

        public MyOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
            super(context, name, factory);
        }

        @Override
        public void onUpgrade(Database db, int oldVersion, int newVersion) {
            Log.i("greenDAO", "Upgrading schema from version " + oldVersion + " to " + newVersion + " by dropping all tables");
            DaoMaster.dropAllTables(db,false);
            onCreate(db);
        }
    }

    private void setDatabase(Context context) {
        mHelper = new MyOpenHelper(context, "zentest-data-db", null);
//        Database database = mHelper.getEncryptedWritableDb("zentest-data-db");
        db = mHelper.getWritableDatabase();
        mDaoMaster = new DaoMaster(db);
        mDaoSession = mDaoMaster.newSession();

    }

    public DaoSession getDaoSession() {
        return mDaoSession;
    }

    public SQLiteDatabase getDb() {
        return db;
    }

    public void init(Application application) {
        this.application = application;
        setDatabase(application);

        //https://github.com/Jasonchenlijian/FastBle
        BleManager.getInstance().init(application);
        /*
        *   BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
          .setServiceUuids(serviceUuids)      // 只扫描指定的服务的设备，可选
          .setDeviceName(true, names)         // 只扫描指定广播名的设备，可选
          .setDeviceMac(mac)                  // 只扫描指定mac的设备，可选
          .setAutoConnect(isAutoConnect)      // 连接时的autoConnect参数，可选，默认false
          .setScanTimeOut(10000)              // 扫描超时时间，可选，默认10秒；小于等于0表示不限制扫描时间
          .build();
  BleManager.getInstance().initScanRule(scanRuleConfig);
        *
        * */
        String url =SERVER_URL;
        MyApi.getInstance().setContext(application);
        MyApi.getInstance().setRestApi(new RestApiImpl(application,url));
        MyApi.getInstance().setDataApi(new DataApiImpl());
        MyApi.getInstance().setBtApi(new BtApiImpl());
        MyApi.getInstance().getRestApi().setLocalType(application.getString(R.string.local_type));
        application.startService(new Intent(application,BleService.class));
    }

    private static PackageInfo getPackageInfo(Context context) {
        PackageInfo pi = null;

        try {
            PackageManager pm = context.getPackageManager();
            pi = pm.getPackageInfo(context.getPackageName(),
                    PackageManager.GET_CONFIGURATIONS | PackageManager.GET_SIGNATURES);

            return pi;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return pi;
    }

    public static String getAppVersion() {
        try {
            Context context = getInstance().application;
            context.getPackageName();
            PackageInfo packageInfo = getPackageInfo(context);
            StringBuilder builder = new StringBuilder();
            try {
                Signature[] signatures = packageInfo.signatures;
                /******* 循环遍历签名数组拼接应用签名 *******/
                for (Signature signature : signatures) {
                    builder.append(signature.toCharsString());

                }
                /************** 得到应用签名 **************/
            } catch (Exception e) {

            }
            String signature = okio.ByteString.decodeBase64(builder.toString()).sha1().hex();

            // packageInfo.signatures[0];
            return packageInfo.packageName + "_" + packageInfo.versionName + "_" + packageInfo.versionCode + "_" + signature;
        } catch (Exception e) {
            return "";
        }
    }
}
