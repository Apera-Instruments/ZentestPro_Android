package com.zen.zentest;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

//import com.pgyersdk.javabean.AppBean;
//import com.pgyersdk.update.PgyUpdateManager;
//import com.pgyersdk.update.UpdateManagerListener;
import com.zen.api.MyApi;
import com.zen.ui.HomeActivity;
import com.zen.ui.LoginActivity;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    private Handler handler = new Handler();

    private TextView versionTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        versionTextView = findViewById(R.id.tv_version);
        versionTextView.setText("" + getVersionName(this));
        //PgyCrashManager.register(this);
        /*
        PgyUpdateManager.register(MainActivity.this,
                new UpdateManagerListener() {

                    @Override
                    public void onUpdateAvailable(final String result) {
                        Log.i(TAG,"onUpdateAvailable "+result);
                        // Encapsulates new version information into AppBean
                        final AppBean appBean = getAppBeanFromString(result);
                        handler.removeCallbacksAndMessages(null);
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Update Available")
                                .setMessage("")
                                .setNegativeButton(
                                        "OK",
                                        new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int which) {
                                                startDownloadTask(
                                                        MainActivity.this,
                                                        appBean.getDownloadURL());
                                            }
                                        })
                                .show();
                    }

                    @Override
                    public void onNoUpdateAvailable() {
                        Log.i(TAG,"onNoUpdateAvailable");
                        if(handler!=null) {
                            handler.removeCallbacksAndMessages(null);
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    goNext();
                                }
                            }, 3000);
                            //goNext();
                        }
                    }
                });
*/

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                goNext();
            }
        },1000);
        new Task().execute();

    }

    public static class Task extends AsyncTask<Void,Integer,Integer>{
        @Override
        protected Integer doInBackground(Void... voids) {
            MyApi.getInstance().getRestApi().updateLoginTime();
            return 0;
        }
    };

    //版本名
    public static String getVersionName(Context context) {
        PackageInfo pi = getPackageInfo(context);
        return pi.versionName;
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

    public void onDestroy(){
      //  PgyUpdateManager.unregister();
        super.onDestroy();
    }

    private void goNext(){
        handler = null;
        if (MyApi.getInstance().getRestApi().isLogin()||MyApi.getInstance().getRestApi().isDemoLogin()) {
            startActivity(new Intent(this, HomeActivity.class));
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }
        finish();
    }
}
