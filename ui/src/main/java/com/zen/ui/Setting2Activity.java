package com.zen.ui;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.zen.api.DataApi;
import com.zen.api.MyApi;
import com.zen.api.SettingConfig;
import com.zen.api.data.BleDevice;
import com.zen.api.data.DeviceSetting;
import com.zen.api.event.SettingDeviceEvent;
import com.zen.ui.base.BaseActivity;
import com.zen.ui.fragment.SettingFragment2;
import com.zen.ui.fragment.SettingFragmentAlarm2;
import com.zen.ui.fragment.SettingFragmentParameter2;
import com.zen.ui.fragment.SettingFragmentTime;

import org.greenrobot.eventbus.EventBus;

public class Setting2Activity extends BaseActivity {

    public static final int HOLD_TIME = 1;
    public static final int BACK_LIGHT_TIME = 2;
    public static final int AUTO_POWER_TIME = 3;
    public static final int READ_STANDARD = 4;
    public static final int CALIBRATION_REMINDER = 5;
    public static final int PH_DUE_CALIBRATION = 6;
    public static final int COND_DUE_CALIBRATION = 7;
    public static final int REFE_TEMP = 8;
    public static final int TEMP_COEFF = 9;
    public static final int TDS_FATOR = 10;
    public static final int SALT_TYPE = 11;




    public static final int PARAM_PH = 31;
    public static final int PARAM_ORP = 32;
    public static final int PARAM_COND = 33;
    public static final int PARAM_SALINITY = 34;
    public static final int PARAM_TDS = 35;
    public static final int PARAM_RESISTIVITY = 36;


    public static final int ALARM_PH = 21;
    public static final int ALARM_ORP = 22;
    public static final int ALARM_COND = 23;
    public static final int ALARM_SALINITY = 24;
    public static final int ALARM_TDS = 25;
    public static final int ALARM_RESISTIVITY = 26;

    private int index = 0;
    private SettingFragment2 settingFragmentTime;
    private String[] valueArray;
    private String mTitle;
    private int mStringArrayId;
    private String mSaveId;
    private String mValue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
 /*       if(Build.VERSION.SDK_INT >= 21) {
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
        Intent intent = getIntent();
        index =intent.getIntExtra("index",0);
        if (index == 0) {
            finish();
            return;
        }


        mTitle = intent.getStringExtra("title");

        mStringArrayId = intent.getIntExtra("stringArrayId ", 0);

        mSaveId =intent.getStringExtra("saveId");

        mValue = intent.getStringExtra("value");


        BleDevice bleDevice = MyApi.getInstance().getBtApi().getLastDevice();
        if(bleDevice==null ){
            Log.w(getTAG(), "bleDevice = null");
            return;
        }
        DeviceSetting setting =  bleDevice.getSetting();
        DeviceSetting.Param param;
        int settingIndex=-1;
        String settingValue = "";
        switch (index) {
            case HOLD_TIME: {
                param = setting.getParam("HOLD_TIME");

                settingFragmentTime = new SettingFragmentTime();
                settingFragmentTime.setTitle(getString(R.string.auto_hold));

                valueArray = getResources().getStringArray(R.array.hole_time_array);
         /*          settingIndex = param.getInt("settingIndex");
             if (settingIndex > 0 && settingIndex < valueArray.length) {
                    settingValue = valueArray[settingIndex];
                } else {
                    settingValue = param.getString("settingValue");
                }*/
                mSaveId = SettingConfig.autoHold_SAVE_ID;
                SharedPreferences sharedPreferences = MyApi.getInstance().getDataApi().getSetting();
                settingValue = sharedPreferences.getString(mSaveId, mValue);
                settingFragmentTime.setSaveId(mSaveId);
                settingFragmentTime.setValue(settingValue);
                settingFragmentTime.setListValue(valueArray);
                settingFragmentTime.setSetTitle(getString(R.string.auto_hold));
                break;
            }
            case BACK_LIGHT_TIME: {
                settingFragmentTime = new SettingFragmentTime();
                settingFragmentTime.setTitle(getString(R.string.backlight_time));
                valueArray = getResources().getStringArray(R.array.back_light_time_array);
                param = setting.getParam("BACK_LIGHT_TIME");
                settingIndex = param.getInt("settingIndex");
                if (settingIndex > 0 && settingIndex < valueArray.length) {
                    settingValue = valueArray[settingIndex];
                } else {
                    settingValue = param.getString("settingValue");
                }
                mSaveId = SettingConfig.backLight_SAVE_ID;
                SharedPreferences sharedPreferences = MyApi.getInstance().getDataApi().getSetting();
                settingValue = sharedPreferences.getString(mSaveId, mValue);
                settingFragmentTime.setSaveId(mSaveId);
                settingFragmentTime.setValue(settingValue);
                settingFragmentTime.setListValue(valueArray);
                settingFragmentTime.setSetTitle(getString(R.string.backlight_time));
                break;
            }
            case AUTO_POWER_TIME: {
                settingFragmentTime = new SettingFragmentTime();
                settingFragmentTime.setTitle(getString(R.string.auto_power_off));

                valueArray = getResources().getStringArray(R.array.power_time_array);
                param = setting.getParam("AUTO_POWER_TIME");
                settingIndex = param.getInt("settingIndex");
                if (settingIndex > 0 && settingIndex < valueArray.length) {
                    settingValue = valueArray[settingIndex];
                } else {
                    settingValue = param.getString("settingValue");
                }
                mSaveId = SettingConfig.autoPowerOff_SAVE_ID;
                SharedPreferences sharedPreferences = MyApi.getInstance().getDataApi().getSetting();
                settingValue = sharedPreferences.getString(mSaveId, mValue);
                settingFragmentTime.setSaveId(mSaveId);
                settingFragmentTime.setValue(settingValue);
                settingFragmentTime.setListValue(valueArray);
                settingFragmentTime.setSetTitle(getString(R.string.auto_power_off));
            }
            break;
          /*    case READ_STANDARD:
                settingFragmentTime = new SettingFragmentTime();

                settingFragmentTime.setTitle(getString(R.string.stable_reading_standard));
                valueArray = getResources().getStringArray(R.array.hole_time_array);
                param = setting.getParam("READ_STANDARD");
                settingIndex = param.getInt("settingIndex");
                if (settingIndex > 0 && settingIndex < valueArray.length) {
                    settingValue = valueArray[settingIndex];
                } else {
                    settingValue = param.getString("settingValue");
                }
                settingFragmentTime.setValue(settingValue);
                settingFragmentTime.setListValue(valueArray);

                break;
            case CALIBRATION_REMINDER:
                settingFragmentTime = new SettingFragmentTime();

                settingFragmentTime.setTitle(getString(R.string.calibration_reminder));
                valueArray = getResources().getStringArray(R.array.hole_time_array);
                param = setting.getParam("CALIBRATION_REMINDER");
                settingIndex = param.getInt("settingIndex");
                if (settingIndex > 0 && settingIndex < valueArray.length) {
                    settingValue = valueArray[settingIndex];
                } else {
                    settingValue = param.getString("settingValue");
                }
                settingFragmentTime.setValue(settingValue);
                settingFragmentTime.setListValue(valueArray);
                break;
*/


            case REFE_TEMP:
            case TEMP_COEFF:
            case TDS_FATOR:
            case SALT_TYPE: {
                settingFragmentTime = new SettingFragmentTime();
                settingFragmentTime.setTitle(mTitle);
                valueArray = getResources().getStringArray(mStringArrayId);
                SharedPreferences sharedPreferences = MyApi.getInstance().getDataApi().getSetting();
                settingValue = sharedPreferences.getString(mSaveId, mValue);
                settingFragmentTime.setSaveId(mSaveId);
 /*               param = setting.getParam("CALIBRATION_REMINDER");
                settingIndex = param.getInt("settingIndex");
                if (settingIndex > 0 && settingIndex < valueArray.length) {
                    settingValue = valueArray[settingIndex];
                } else {
                    settingValue = param.getString("settingValue");
                }*/
                settingFragmentTime.setValue(settingValue);
                settingFragmentTime.setListValue(valueArray);
                settingFragmentTime.setSetTitle(mTitle);
            }

                break;
            case PH_DUE_CALIBRATION:
            case COND_DUE_CALIBRATION: {
                settingFragmentTime = new SettingFragmentTime();
                settingFragmentTime.setTitle(mTitle);
                valueArray = new String[200];
                for(int i=0;i<=99;i++) {
                    valueArray[i] =String.format("%d Hours",i);
                    valueArray[i+100] =String.format("%d Days",i);
                }
                SharedPreferences sharedPreferences = MyApi.getInstance().getDataApi().getSetting();
                settingValue = sharedPreferences.getString(mSaveId, mValue);
                settingFragmentTime.setSaveId(mSaveId);
                settingFragmentTime.setValue(settingValue);
                settingFragmentTime.setListValue(valueArray);
                settingFragmentTime.setSetTitle("Due calibration");
                break;
            }
            case PARAM_PH:
                settingFragmentTime = new SettingFragmentParameter2();
                settingFragmentTime.setTitle(getString(R.string.ph));
                settingFragmentTime.setSettingIndex("PARAM_PH");

                break;
            case PARAM_ORP :
                settingFragmentTime = new SettingFragmentParameter2();
                settingFragmentTime.setTitle(getString(R.string.orp));
                settingFragmentTime.setSettingIndex("PARAM_ORP");
                break;
            case PARAM_COND:
                settingFragmentTime = new SettingFragmentParameter2();
                settingFragmentTime.setTitle(getString(R.string.conductivity));
                settingFragmentTime.setSettingIndex("PARAM_COND");
                break;
            case PARAM_SALINITY:
                settingFragmentTime = new SettingFragmentParameter2();
                settingFragmentTime.setTitle(getString(R.string.salinity));
                settingFragmentTime.setSettingIndex("PARAM_SALINITY");
                break;
            case PARAM_TDS:
                settingFragmentTime = new SettingFragmentParameter2();
                settingFragmentTime.setTitle(getString(R.string.tds));
                settingFragmentTime.setSettingIndex("PARAM_TDS");
                break;
            case PARAM_RESISTIVITY:
                settingFragmentTime = new SettingFragmentParameter2();
                settingFragmentTime.setTitle(getString(R.string.resistivity));
                settingFragmentTime.setSettingIndex("PARAM_RESISTIVITY");
                break;

            case ALARM_PH:
                settingFragmentTime = new SettingFragmentAlarm2();
                settingFragmentTime.setTitle(getString(R.string.ph_range));
                settingFragmentTime.setSettingIndex("ALARM_PH");

                break;
            case ALARM_ORP:
                settingFragmentTime = new SettingFragmentAlarm2();
                settingFragmentTime.setTitle(getString(R.string.orp_range));
                settingFragmentTime.setSettingIndex("ALARM_ORP");
                break;
            case ALARM_COND:
                settingFragmentTime = new SettingFragmentAlarm2();
                settingFragmentTime.setTitle(getString(R.string.conductivity_rang));
                settingFragmentTime.setSettingIndex("ALARM_COND");
                break;
            case ALARM_SALINITY:
                settingFragmentTime = new SettingFragmentAlarm2();
                settingFragmentTime.setTitle(getString(R.string.salinity_range));
                settingFragmentTime.setSettingIndex("ALARM_SALINITY");
                break;
            case ALARM_TDS:
                settingFragmentTime = new SettingFragmentAlarm2();
                settingFragmentTime.setTitle(getString(R.string.tds_range));
                settingFragmentTime.setSettingIndex("ALARM_TDS");
                break;
            case ALARM_RESISTIVITY:
                settingFragmentTime = new SettingFragmentAlarm2();
                settingFragmentTime.setTitle(getString(R.string.resistivity_range));
                settingFragmentTime.setSettingIndex("ALARM_RESISTIVITY");
                break;
            default:
                finish();
                return;
        }
        settingFragmentTime.setIndex(index);

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();

        try {
            fragmentTransaction.replace(R.id.framelayout, settingFragmentTime);
            fragmentTransaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void onPause(){
        store();
        super.onPause();

    }


    private void store() {
        boolean settingDevice =false;
        if (settingFragmentTime != null) {
            if (settingFragmentTime.getSaveSetting() == false) {
                return;
            }
            String result = settingFragmentTime.getSelectValue();
            Log.i(getTAG(), "getSelectValue = " + result);

            BleDevice bleDevice = MyApi.getInstance().getBtApi().getLastDevice();
            if(bleDevice==null ){
                Log.w(getTAG(), "bleDevice = null");
                return;
            }
            DeviceSetting setting =  bleDevice.getSetting();
            int settingIndex = -1;

            if (valueArray != null && result != null) {
                for (int i = 0; i < valueArray.length; i++) {
                    if (result.equals(valueArray[i])) {
                        settingIndex = i;
                        break;
                    }
                }
            }
            switch (index) {
                case HOLD_TIME:
                   /* settingFragmentTime = new SettingFragmentTime();
                    settingFragmentTime.setTitle(getString(R.string.auto_hold));
                    settingFragmentTime.setValue("OFF");
                    settingFragmentTime.setListValue("1 Seconds", "2 Seconds", "3 Seconds", "4 Seconds", "5 Seconds", "6 Seconds");
                 */
                {
                    DeviceSetting.Param param = setting.getParam("HOLD_TIME");
                    param.data.put("settingIndex", settingIndex);
                    param.data.put("settingValue", result);
                    SharedPreferences sharedPreferences =   MyApi.getInstance().getDataApi().getSetting();
                    sharedPreferences.edit().putString(mSaveId,result).apply();
                    settingDevice = true;
                }
                   break;
                case BACK_LIGHT_TIME:
                {
                    DeviceSetting.Param param = setting.getParam("BACK_LIGHT_TIME");
                    param.data.put("settingIndex", settingIndex);
                    param.data.put("settingValue", result);
                    SharedPreferences sharedPreferences =   MyApi.getInstance().getDataApi().getSetting();
                    sharedPreferences.edit().putString(mSaveId,result).apply();
                    settingDevice = true;
                }
                     break;
                case AUTO_POWER_TIME:
                {
                    DeviceSetting.Param param = setting.getParam("AUTO_POWER_TIME");
                    param.data.put("settingIndex", settingIndex);
                    param.data.put("settingValue", result);
                    SharedPreferences sharedPreferences =   MyApi.getInstance().getDataApi().getSetting();
                    sharedPreferences.edit().putString(mSaveId,result).apply();
                    settingDevice = true;
                }
                     break;
                case READ_STANDARD:
                {
                    DeviceSetting.Param param = setting.getParam("READ_STANDARD");
                    param.data.put("settingIndex", settingIndex);
                    param.data.put("settingValue", result);
                    settingDevice = true;
                }

                    break;
                case CALIBRATION_REMINDER:
                {
                    DeviceSetting.Param param = setting.getParam("CALIBRATION_REMINDER");
                    param.data.put("settingIndex", settingIndex);
                    param.data.put("settingValue", result);
                    settingDevice = true;
                }

                    break;
                case PARAM_PH:
                {
                    DeviceSetting.Param param = setting.getParam("PARAM_PH");
                    param.data.put("settingIndex", settingIndex);
                    param.data.put("settingValue", result);
                }
                    break;

                case ALARM_PH:
                {
                    DeviceSetting.Param param = setting.getParam("ALARM_PH");
                    param.data.put("settingIndex", settingIndex);
                    param.data.put("settingValue", result);
                }
                    break;
                case ALARM_ORP:
                {
                    DeviceSetting.Param param = setting.getParam("ALARM_ORP");
                    param.data.put("settingIndex", settingIndex);
                    param.data.put("settingValue", result);
                }
                    break;
                case ALARM_COND:
                {
                    DeviceSetting.Param param = setting.getParam("ALARM_COND");
                    param.data.put("settingIndex", settingIndex);
                    param.data.put("settingValue", result);
                }
                    break;
                case ALARM_SALINITY:
                {
                    DeviceSetting.Param param = setting.getParam("ALARM_SALINITY");
                    param.data.put("settingIndex", settingIndex);
                    param.data.put("settingValue", result);
                }
                    break;
                case ALARM_TDS:
                {
                    DeviceSetting.Param param = setting.getParam("ALARM_TDS");
                    param.data.put("settingIndex", settingIndex);
                    param.data.put("settingValue", result);
                }
                    break;
                case ALARM_RESISTIVITY:
                {
                    DeviceSetting.Param param = setting.getParam("ALARM_RESISTIVITY");
                    param.data.put("settingIndex", settingIndex);
                    param.data.put("settingValue", result);
                }
                    break;
                case PH_DUE_CALIBRATION:
                case COND_DUE_CALIBRATION:
                {
                    SharedPreferences sharedPreferences =   MyApi.getInstance().getDataApi().getSetting();
                    sharedPreferences.edit().putString(mSaveId,result).apply();
                }
                    break;
                default:

                    return;
            }
            MyApi.getInstance().getDataApi().updateBleDevice(bleDevice, DataApi.SETTING);
           if(settingDevice) EventBus.getDefault().post(new SettingDeviceEvent());
        }
    }

    public static void showMe(Context context, int i) {
        Intent intent = new Intent(context, Setting2Activity.class);
        intent.putExtra("index", i);
        context.startActivity(intent);
    }

    public static void showMe(Context context, String saveId,String title,String value, int stringArrayId) {
        int i = getIndex(saveId);
        Intent intent = new Intent(context, Setting2Activity.class);
        intent.putExtra("index", i);
        intent.putExtra("title", title);
        intent.putExtra("stringArrayId ", stringArrayId);
        intent.putExtra("saveId",saveId);
        intent.putExtra("value",value);


        context.startActivity(intent);
    }

    private static int getIndex(String saveId) {
        switch (saveId){
            case SettingConfig.PH_DueCalibration_SAVE_ID:
                return PH_DUE_CALIBRATION;
            case SettingConfig.COND_DueCalibration_SAVE_ID:
                return COND_DUE_CALIBRATION;
            case SettingConfig.ReferenceTemperature_SAVE_ID:
                return REFE_TEMP;

            case SettingConfig.TemperatureCoefficient_SAVE_ID:
                return TEMP_COEFF;
            case SettingConfig.TDSFactor_SAVE_ID:
                return TDS_FATOR;
            case SettingConfig.SaltType_SAVE_ID:
                return SALT_TYPE;
        }
        return -1;
    }

}
