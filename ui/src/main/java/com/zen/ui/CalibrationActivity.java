package com.zen.ui;

import android.app.FragmentTransaction;
import android.os.Bundle;

import com.zen.api.MyApi;
import com.zen.ui.base.BaseActivity;
import com.zen.ui.fragment.CalibrationFragment;

public class CalibrationActivity extends BaseActivity {
    private int mMode=0;
    private CalibrationFragment calibrationFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);
        MyApi.getInstance().getDataApi().setCalibration(true);
        MyApi.getInstance().getDataApi().setLastData(null);
     /*   if(Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            //让应用主题内容占用系统状态栏的空间,注意:下面两个参数必须一起使用 stable 牢固的
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            //设置状态栏颜色为透明
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

    *//*    getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
*//*
        View mStatusBar = findViewById(R.id.fillStatusBarView);
        ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) mStatusBar.getLayoutParams();
        lp.width = LinearLayout.LayoutParams.MATCH_PARENT;
        lp.height = getStatusBar();
        mStatusBar.setLayoutParams(lp);*/

        mMode = getIntent().getIntExtra("MODE",0);
        calibrationFragment = new CalibrationFragment();
        calibrationFragment.setMode(mMode);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();

        fragmentTransaction.replace(R.id.framelayout, calibrationFragment);

        fragmentTransaction.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyApi.getInstance().getDataApi().setCalibration(false);
    }

    public void onBackPressed(){
        if(calibrationFragment!=null){
            calibrationFragment.onBack();
            return;
        }
        super.onBackPressed();
    }

}
