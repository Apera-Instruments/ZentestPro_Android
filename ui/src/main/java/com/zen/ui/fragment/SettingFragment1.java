package com.zen.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.foolchen.lib.tracker.Tracker;
import com.zen.api.MyApi;
import com.zen.ui.HomeActivity;
import com.zen.ui.LoginActivity;
import com.zen.ui.R;
import com.zen.ui.SettingActivity;
import com.zen.ui.base.BaseFragment;

import cn.pedant.SweetAlert.SweetAlertDialog;


/**
 * Created by louis on 17-12-21. 设置页面
 */
public class SettingFragment1 extends BaseFragment implements View.OnClickListener, TextWatcher {
  //  private List<BaseFragment> fragmentList = new ArrayList<>();

    TextView mTextViewVersion;

    public void onMenu() {
        ((HomeActivity)getActivity()).showMenu();
    }

    void logout() {


        new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE)
                .setTitleText(getString(R.string.are_you_sure))
                .setContentText(getString(R.string.logout_the_account))
                .setConfirmText(getString(R.string.yes_logout))
                .setCancelText(getString(android.R.string.cancel))
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        //MyApi.getInstance().getDataApi().delRecordByUser( MyApi.getInstance().getRestApi().getUserId());
                        String userId = MyApi.getInstance().getRestApi().getUserId();
                        MyApi.getInstance().getRestApi().logout();
                        Tracker.INSTANCE.logout();
                        MyApi.getInstance().getBtApi().disconnect();
                        SharedPreferences sharedPreferences = MyApi.getInstance().getDataApi().getSetting();
                        String dev = sharedPreferences.getString("connectedDev","");
                        sharedPreferences.edit()
                                .putString("connectedDev","")
                                .putString("connectedDev_"+userId,dev)
                                .apply();
                        startActivity(new Intent(getContext(), LoginActivity.class));
                        getActivity().finish();
                        sweetAlertDialog.dismissWithAnimation();

                    }
                })
                .show();
    }
    private Toast mToast;

    private void showMsg(String s) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(getContext(), s, Toast.LENGTH_SHORT);
        mToast.show();

    }
    void generalSetting() {
        Log.d("item5","1");
        if(!MyApi.getInstance().getBtApi().isConnected())  {
            showMsg(getString(R.string.no_dev));
            return;
        }

        if (MyApi.getInstance().getBtApi().getDeviceNumber().equals("Demo Device")){
            showMsg(getString(R.string.no_dev));
            return;
        }
        SettingActivity.showMe(getContext(),1);
    }

    void parameterSetting() {
        Log.d("item5","2");
        if(!MyApi.getInstance().getBtApi().isConnected())  {
            showMsg(getString(R.string.no_dev));
            return;
        }
        SettingActivity.showMe(getContext(),2);
    }

    void dataStoreSetting() {
        Log.d("item5","3");
        if(!MyApi.getInstance().getBtApi().isConnected())  {
            showMsg(getString(R.string.no_dev));
            return;
        }
        SettingActivity.showMe(getContext(),3);
    }

    void alarmSetting() {
        Log.d("item5","4");
        if(!MyApi.getInstance().getBtApi().isConnected())  {
            showMsg(getString(R.string.no_dev));
            return;
        }
        SettingActivity.showMe(getContext(),4);
    }

    //需要修改的
    void gpsSetting() {
        Log.d("item5","5");
      //  MapsActivity.showMe(getContext(),"");
        if(!MyApi.getInstance().getBtApi().isConnected())  {
            showMsg(getString(R.string.no_dev));
            return;
        }

        SettingActivity.showMe(getContext(),5);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    RelativeLayout layout_item1,layout_item2,layout_item3,layout_item4,layout_item5,layout_logout;
    ImageView iv_menu;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting1, container, false);
        mTextViewVersion = view.findViewById(R.id.tv_version);
        layout_item1 = view.findViewById(R.id.layout_item1);
        layout_item2 = view.findViewById(R.id.layout_item2);
        layout_item3 = view.findViewById(R.id.layout_item3);
        layout_item4 = view.findViewById(R.id.layout_item4);
        layout_item5 = view.findViewById(R.id.layout_item5);
        layout_logout = view.findViewById(R.id.layout_logout);
        iv_menu = view.findViewById(R.id.iv_menu);
        layout_item1.setOnClickListener(this);

        layout_item1.setOnClickListener(this);
        layout_item2.setOnClickListener(this);
        layout_item3.setOnClickListener(this);
        layout_item4.setOnClickListener(this);
        layout_item5.setOnClickListener(this);
        layout_logout.setOnClickListener(this);
        iv_menu.setOnClickListener(this);


        mTextViewVersion.setText(getVersionName(getContext()));
        return view;
    }

    public void remove(){

    }

    public void updateView() {
        super.updateView();


    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.layout_item1) {
            generalSetting();
        } else if (view.getId() == R.id.layout_item2) {
            parameterSetting();
        } else if (view.getId() == R.id.layout_item3) {
            dataStoreSetting();
        } else if (view.getId() == R.id.layout_item4) {
            alarmSetting();
        } else if (view.getId() == R.id.layout_item5) {
            gpsSetting();
        } else if (view.getId() == R.id.layout_logout) {
            logout();
        } else if (view.getId() == R.id.iv_menu) {
            onMenu();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }
    //版本名
    public static String getVersionName(Context context) {
        PackageInfo pi = getPackageInfo(context);
        return pi.versionName;
    }

    //版本号
    public static int getVersionCode(Context context) {
        return getPackageInfo(context).versionCode;
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

}