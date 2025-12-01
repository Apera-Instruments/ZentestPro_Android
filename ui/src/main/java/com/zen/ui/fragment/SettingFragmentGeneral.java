package com.zen.ui.fragment;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.zen.api.DataApi;
import com.zen.api.MyApi;
import com.zen.api.SettingConfig;
import com.zen.api.data.BleDevice;
import com.zen.api.event.GpsEventStart;
import com.zen.api.event.SettingDeviceEvent;
import com.zen.api.event.UpdateEvent;
import com.zen.ui.R;
import com.zen.ui.Setting2Activity;
import com.zen.ui.base.BaseFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import lib.kingja.switchbutton.SwitchMultiButton;


/**
 * Created by louis on 17-12-21.
 */
public class SettingFragmentGeneral extends BaseFragment implements View.OnClickListener {

    private SwitchMultiButton mSwitchMultiButton;
    private String degreeC;

    private TextView tvLeft;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.setting_general, container, false);
        textViewValue1 = view.findViewById(R.id.tv_value1);
        textViewValue4 = view.findViewById(R.id.tv_value4);
        textViewValuePowerOff = view.findViewById(R.id.tv_value_auto_power_off);
        mSwitchGPS = view.findViewById(R.id.switch_gps);
        layout_item2= view.findViewById(R.id.layout_item2);
        layout_item2.setOnClickListener(this);
        layout_item_power_off= view.findViewById(R.id.layout_item_power_off);
        layout_item_power_off.setOnClickListener(this);
        layout_item5= view.findViewById(R.id.layout_item5);
        layout_item5.setOnClickListener(this);
        mSwitchGPS.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean check) {
                BleDevice bleDevice = MyApi.getInstance().getBtApi().getLastDevice();
                if (bleDevice != null && bleDevice.getSetting() != null) {
                    bleDevice.getSetting().getParam("GPS_SWITCH").data.put("settingValue", check ? "ON" : "OFF");
                    MyApi.getInstance().getDataApi().updateBleDevice(bleDevice, DataApi.SETTING);
                    SharedPreferences sharedPreferences =   MyApi.getInstance().getDataApi().getSetting();
                    sharedPreferences.edit().putString(SettingConfig.gps_SAVE_ID,check ? "ON" : "OFF").apply();
                    EventBus.getDefault().post(new GpsEventStart(check ));
                }
            }
        });
        degreeC =getString(R.string.temp_degree_c);

        tvLeft = view.findViewById(R.id.tv_left);
        tvLeft.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });

        //switch_temp1
        mSwitchMultiButton = (SwitchMultiButton) view.findViewById(R.id.switch_temp1);
        mSwitchMultiButton.setOnSwitchListener(new SwitchMultiButton.OnSwitchListener() {
            @Override
            public void onSwitch(int position, String tabText) {
                BleDevice bleDevice = MyApi.getInstance().getBtApi().getLastDevice();
                if (bleDevice != null && bleDevice.getSetting() != null) {
                    bleDevice.getSetting().getParam("TEMP_UNIT").data.put("settingValue", tabText);
                    MyApi.getInstance().getDataApi().updateBleDevice(bleDevice, DataApi.SETTING);
                    SharedPreferences sharedPreferences =   MyApi.getInstance().getDataApi().getSetting();
                    sharedPreferences.edit().putString(SettingConfig.tempUnit_SAVE_ID,tabText).apply();
                    EventBus.getDefault().post(new SettingDeviceEvent());
                }
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateView();
        //   API.getResourceApi().update(ResourceApi.HOT_VIEW, ResourceApi.APP_LIST);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpdateEvent event) {

    }


    public void updateView() {
        super.updateView();
        //ParmUp parmUp =  MyApi.getInstance().getDataApi().getLastParm();
        // BleDevice bleDevice = MyApi.getInstance().getBtApi().getLastDevice();
        //if(bleDevice!=null && bleDevice.getSetting()!=null)
        {
            SharedPreferences sharedPreferences =   MyApi.getInstance().getDataApi().getSetting();
            ;

            textViewValue1.setText( sharedPreferences.getString(SettingConfig.autoHold_SAVE_ID,"OFF"));
            textViewValue4.setText( sharedPreferences.getString(SettingConfig.backLight_SAVE_ID,"OFF"));
            textViewValuePowerOff.setText( sharedPreferences.getString(SettingConfig.autoPowerOff_SAVE_ID,"OFF"));
            // textViewValue5.setText(  bleDevice.getSetting().getParam("AUTO_POWER_TIME").getString("settingValue")  );
            // textViewValue2.setText(  bleDevice.getSetting().getParam("READ_STANDARD").getString("settingValue") );
            // textViewValue3.setText(  bleDevice.getSetting().getParam("CALIBRATION_REMINDER").getString("settingValue")  );
            mSwitchGPS.setChecked("ON".equals(sharedPreferences.getString(SettingConfig.gps_SAVE_ID,"OFF")));
            String tempUnit =sharedPreferences.getString(SettingConfig.tempUnit_SAVE_ID,degreeC);

            mSwitchMultiButton.setSelectedTab((tempUnit).equals(degreeC)?0:1);

        }

    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.layout_item2){Setting2Activity.showMe(getContext(),Setting2Activity.HOLD_TIME);}
        //else if(view.getId() == R.id.layout_item3){Setting2Activity.showMe(getContext(),Setting2Activity.READ_STANDARD);}
        //else if(view.getId() == R.id.layout_item4){Setting2Activity.showMe(getContext(),Setting2Activity.CALIBRATION_REMINDER);}
        else if(view.getId() == R.id.layout_item5){Setting2Activity.showMe(getContext(),Setting2Activity.BACK_LIGHT_TIME);}
        else if(view.getId() == R.id.layout_item_power_off){Setting2Activity.showMe(getContext(),Setting2Activity.AUTO_POWER_TIME);}
        //else if(view.getId() == R.id.layout_item6){Setting2Activity.showMe(getContext(),Setting2Activity.AUTO_POWER_TIME);}


    }

    TextView textViewValue1;
    TextView textViewValue4;
    TextView textViewValuePowerOff;
    Switch mSwitchGPS;
    RelativeLayout layout_item2,layout_item_power_off,layout_item5;

//    @OnCheckedChanged(R.id.switch_gps)
//    protected void onGPSCheckedChanged(CompoundButton view, boolean check) {
//        BleDevice bleDevice = MyApi.getInstance().getBtApi().getLastDevice();
//        if (bleDevice != null && bleDevice.getSetting() != null) {
//            bleDevice.getSetting().getParam("GPS_SWITCH").data.put("settingValue", check ? "ON" : "OFF");
//            MyApi.getInstance().getDataApi().updateBleDevice(bleDevice, DataApi.SETTING);
//            SharedPreferences sharedPreferences =   MyApi.getInstance().getDataApi().getSetting();
//            sharedPreferences.edit().putString(SettingConfig.gps_SAVE_ID,check ? "ON" : "OFF").apply();
//            EventBus.getDefault().post(new GpsEventStart(check ));
//        }
//    }


//    @OnClick({R.id.layout_item2,R.id.layout_item_power_off,R.id.layout_item5})
//    protected void goNext(View view){
//        if(view.getId() == R.id.layout_item2){Setting2Activity.showMe(getContext(),Setting2Activity.HOLD_TIME);}
//        //else if(view.getId() == R.id.layout_item3){Setting2Activity.showMe(getContext(),Setting2Activity.READ_STANDARD);}
//        //else if(view.getId() == R.id.layout_item4){Setting2Activity.showMe(getContext(),Setting2Activity.CALIBRATION_REMINDER);}
//        else if(view.getId() == R.id.layout_item5){Setting2Activity.showMe(getContext(),Setting2Activity.BACK_LIGHT_TIME);}
//        else if(view.getId() == R.id.layout_item_power_off){Setting2Activity.showMe(getContext(),Setting2Activity.AUTO_POWER_TIME);}
//        //else if(view.getId() == R.id.layout_item6){Setting2Activity.showMe(getContext(),Setting2Activity.AUTO_POWER_TIME);}
//
//    }

}