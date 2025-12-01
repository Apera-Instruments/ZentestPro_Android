package com.zen.ui;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.zen.api.event.UpdateEvent;
import com.zen.ui.base.BaseActivity;
import com.zen.ui.fragment.CalibrationFragment;
import com.zen.ui.fragment.SettingFragmentAlarm;
import com.zen.ui.fragment.SettingFragmentData;
import com.zen.ui.fragment.SettingFragmentGeneral;
import com.zen.ui.fragment.SettingFragmentGps;
import com.zen.ui.fragment.SettingFragmentParameter;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class SettingActivity extends BaseActivity {

    private Class fragmentCsList[]={
            SettingFragmentGeneral.class,
            SettingFragmentParameter.class,
            SettingFragmentData.class,
            SettingFragmentAlarm.class,
            SettingFragmentGps.class,
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
/*        if(Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            //让应用主题内容占用系统状态栏的空间,注意:下面两个参数必须一起使用 stable 牢固的
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            //设置状态栏颜色为透明
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
 *//*       getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
      *//*
        View mStatusBar = findViewById(R.id.fillStatusBarView);
        ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) mStatusBar.getLayoutParams();
        lp.width = LinearLayout.LayoutParams.MATCH_PARENT;
        lp.height = getStatusBar();
        mStatusBar.setLayoutParams(lp);*/

        int index =getIntent().getIntExtra("index",0);
        if(index==0 || index>fragmentCsList.length) {
            finish();
            return;
        }

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();

        try {
            fragmentTransaction.replace(R.id.framelayout, (Fragment) fragmentCsList[index-1].newInstance());
            fragmentTransaction.commit();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void showMe(Context context, int i) {
        Intent intent = new Intent(context,SettingActivity.class);
        intent.putExtra("index",i);
        context.startActivity(intent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpdateEvent event) {

    }

}
