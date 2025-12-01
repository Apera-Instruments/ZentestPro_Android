package com.zen.ui.fragment;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.alibaba.fastjson.JSON;
import com.zen.api.Constant;
import com.zen.api.DataApi;
import com.zen.api.MyApi;
import com.zen.api.data.BleDevice;
import com.zen.api.data.DeviceSetting;
import com.zen.ui.R;
import com.zen.ui.view.Pickers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



/**
 * Created by louis on 17-12-21.
 */
public class SettingFragmentAlarm2 extends SettingFragment2 {
    private final String TAG="SettingFragmentAlarm2";

    private List<Pickers> list = new ArrayList<>();
    private List<String> listValue = new ArrayList<>();
    private String title = "";

    Spinner mSpinnerUnit;
    TextView mTextViewAlarmTips;
    TextView mTextViewAlarmRang;
    TextView mTextViewTitle;
    TextView mEditViewHigh;
    TextView mEditViewLow;
    Switch mSwViewHigh;
    Switch mSwViewLow;

    private String defaultAlarm = "{}";
    private String typestr = "";
    private DeviceSetting.Param mParam;

    private boolean isHightCheck=false;
    private boolean isLowCheck=false;
    //上限的开关
//    @OnCheckedChanged(R.id.switch_high)
//    void highOnCheckedChanged(Switch sw, boolean check) {
//        isHightCheck = check;
//        if(update) return;
//        BleDevice bleDevice = MyApi.getInstance().getBtApi().getLastDevice();
//        if (bleDevice != null && bleDevice.getSetting() != null) {
//            Map<String, Object> data = bleDevice.getSetting().getParam(settingIndex).data;
//            data.put(Constant.highSwitchValue, check ? Constant.ON : Constant.OFF);
//            mParam.data.put(Constant.highSwitchValue, check ? Constant.ON : Constant.OFF);
//            //MyApi.getInstance().getDataApi().updateBleDevice(bleDevice, DataApi.SETTING);
//            mSaveView.setVisibility(View.VISIBLE);
//        }
//    }

//    //下限的开关
//    @OnCheckedChanged(R.id.switch_low)
//    void lowOnCheckedChanged(Switch sw, boolean check) {
//        isLowCheck = check;
//        if(update) return;
//        BleDevice bleDevice = MyApi.getInstance().getBtApi().getLastDevice();
//        if (bleDevice != null && bleDevice.getSetting() != null) {
//            Map<String, Object> data = bleDevice.getSetting().getParam(settingIndex).data;
//            data.put(Constant.lowSwitchValue, check ? Constant.ON : Constant.OFF);
//            mParam.data.put(Constant.lowSwitchValue, check ? Constant.ON : Constant.OFF);
//            //MyApi.getInstance().getDataApi().updateBleDevice(bleDevice, DataApi.SETTING);
//            mSaveView.setVisibility(View.VISIBLE);
//        }
//    }

    void lowOnTextChanged() {
        if(update) return;
        mSaveView.setVisibility(View.VISIBLE);

    }

    void highOnTextChanged() {
        if(update) return;
        mSaveView.setVisibility(View.VISIBLE);
    }
    View mSaveView;

    //保存量程
    void save(View view) {
        String hig = mEditViewHigh.getText().toString();
        String low = mEditViewLow.getText().toString();
        if (isNumber(hig)) {
            if (!isNumber(low)){
                Toast.makeText(getContext(),getString(R.string.min_number_format),Toast.LENGTH_LONG).show();
                return;
            }
        }else {
            Toast.makeText(getContext(),getString(R.string.max_number_format),Toast.LENGTH_LONG).show();
            return;
        }

        if(TextUtils.isEmpty(low)&&TextUtils.isEmpty(hig)){
            Toast.makeText(getContext(),getString(R.string.empty_str),Toast.LENGTH_LONG).show();
            return;
        }else {
            if(TextUtils.isEmpty(low)){
                String strhig = alarmText(hig);
                if (strhig.length()>0){
                    Toast.makeText(getContext(),getString(R.string.max_must_benotover),Toast.LENGTH_LONG).show();
                    return;
                }
            }else {
                if (TextUtils.isEmpty(hig)){
                    String strlow = alarmText(low);
                    if (strlow.length()>0){
                        Toast.makeText(getContext(),getString(R.string.min_must_beoverthe),Toast.LENGTH_LONG).show();
                        return;
                    }
                }else {
                    String strhig = alarmText(hig);
                    String strlow = alarmText(low);
                    if (Double.parseDouble(hig)<=Double.parseDouble(low)){
                        Toast.makeText(getContext(),getString(R.string.max_must_be),Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (strhig.length()>0){
                        Toast.makeText(getContext(),getString(R.string.max_must_benotover),Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (strlow.length()>0){
                        Toast.makeText(getContext(),getString(R.string.min_must_beoverthe),Toast.LENGTH_LONG).show();
                        return;
                    }
                }
            }
        }

        if (saveSetting()) {
            view.setVisibility(View.INVISIBLE);
            getActivity().finish();
        }
    }

    public static boolean isNumber(String str){
        if (str.length()>1){
            String s = str.substring(0,1);

            if (s.equals("-")){
                str = str.substring(1);
            }
        }

        try {
            Double.parseDouble(str);
            Log.d("number", "number2" + str);
            return true;
        }catch (NumberFormatException e){
            Log.d("number", "number1" + str);
            if (str.equals("")){
                return true;
            }
            return false;
        }
    }

    //切换单位

    void OnSpItemSelected(View view) {
        TypeChange();
        if(update) return;
        if (mSpinnerUnit.getVisibility() == View.VISIBLE
                && mSpinnerUnit.getVisibility() == View.VISIBLE && (
                !TextUtils.equals(mSpinnerUnit.getSelectedItem().toString(),mParam.getString(Constant.lowValueUnit))
                || !TextUtils.equals(mSpinnerUnit.getSelectedItem().toString(),mParam.getString(Constant.highValueUnit))
                )
                ) {

            mSaveView.setVisibility(View.VISIBLE);
        }
    }

//判断保存量程的范围等是否合规
    private boolean saveSetting() {
        BleDevice bleDevice = MyApi.getInstance().getBtApi().getLastDevice();

        if (bleDevice != null && bleDevice.getSetting() != null) {
            {
                // Map<String, Object> data = bleDevice.getSetting().getParam(settingIndex).data;
                String unit = mSpinnerUnit.getVisibility() == View.VISIBLE ? mSpinnerUnit.getSelectedItem().toString() : "";//getValueUnit(mEditViewLow.getText().toString());
                boolean ret = checkValue(unit);
                if (!ret) {
                    Log.w(TAG,"unit="+unit +" value="+value);
                    messageToast(getString(R.string.unit_err));
                    return false;
                }
                String v= mEditViewLow.getText().toString();
                double value = getBaseValue(getValueDigitsOnly(v), unit);
                if(v.startsWith("-")) {
                    value = (-value);
                }
//                if (Constant.ON.equals(mParam.getString(Constant.highSwitchValue))) {
//                    int h = parseInt(mParam.getString(Constant.highValue));
//                    if ((h != 0) && (value > h)) {
//                        Log.w(TAG,"h="+h +" value="+value);
//                        messageToast(getString(R.string.value_err) + String.format("%d > %d",value,h));
//                        return false;
//                    }
//                }

                mParam.data.put(Constant.lowValueUnit, unit);
                mParam.data.put(Constant.lowValue, Double.toString(value));
               // MyApi.getInstance().getDataApi().updateBleDevice(bleDevice, DataApi.SETTING);
            }

            {
                String unit = mSpinnerUnit.getVisibility() == View.VISIBLE ? mSpinnerUnit.getSelectedItem().toString() : ""; //getValueUnit(mEditViewHigh.getText().toString());
                boolean ret = checkValue(unit);
                if (!ret) {
                    Log.w(TAG,"unit="+unit +" value="+value);
                    messageToast(getString(R.string.unit_err));
                    return false;
                }
                String v= mEditViewHigh.getText().toString();
                double value = getBaseValue(getValueDigitsOnly(v), unit);
                if(v.startsWith("-")) {
                    value = (-value);
                }
//                if (Constant.ON.equals(mParam.getString(Constant.lowSwitchValue))) {
//                    int l = parseInt(mParam.getString(Constant.lowValue));
//
//                    if ((l != 0) && (value < l)) {
//                        Log.w(TAG,"l="+l +" value="+value);
//                        messageToast(getString(R.string.value_err) + String.format("%d < %d",value,l));
//                        return false;
//                    }
//                }

                mParam.data.put(Constant.highValueUnit, unit);
                mParam.data.put(Constant.highValue, Double.toString(value));
               // MyApi.getInstance().getDataApi().updateBleDevice(bleDevice, DataApi.SETTING);
            }
            MyApi.getInstance().getDataApi().updateBleDevice(bleDevice, DataApi.SETTING);
            return true;
        }
        return true;
    }

    private String alarmText(String higlow){
        String text = "";
        if (typestr.equals("pH")){
            if (Double.parseDouble(higlow)>16||Double.parseDouble(higlow)<-2){
                text = "1111111";
            }
        }
        if (typestr.equals("mV")){
            if (Double.parseDouble(higlow)>1000||Double.parseDouble(higlow)<-1000){
                text = "1111111";
            }
        }

        if (typestr.equals("mS/cm")){
            if (mSpinnerUnit.getSelectedItemId()==1){
                if (Float.parseFloat(higlow)<0||Float.parseFloat(higlow)>1999){
                    text = "1111111";
                }
            }else {
                if (Float.parseFloat(higlow)<0||Float.parseFloat(higlow)>20){
                    text = "1111111";
                }
            }
        }
        if (typestr.equals("ppt")){
            if (mSpinnerUnit.getSelectedItemId()==1){
                if (Float.parseFloat(higlow)<0||Float.parseFloat(higlow)>999){
                    text = "1111111";
                }
            }else {
                if (Float.parseFloat(higlow)<0||Float.parseFloat(higlow)>10){
                    text = "1111111";
                }
            }
        }
        Log.w(TAG, "value--="+typestr);
        if (typestr.equals("MΩ·cm")){
            if (mSpinnerUnit.getSelectedItemId()==0||mSpinnerUnit.getSelectedItemId()==1){
                if (Float.parseFloat(higlow)<0||Float.parseFloat(higlow)>999){
                    text = "1111111";
                }
            }else {
                if (Float.parseFloat(higlow)<0||Float.parseFloat(higlow)>20){
                    text = "1111111";
                }
            }
        }

        if (typestr.equals("Ω·cm")){
            if (mSpinnerUnit.getSelectedItemId()==0||mSpinnerUnit.getSelectedItemId()==1){
                if (Float.parseFloat(higlow)<0||Float.parseFloat(higlow)>999){
                    text = "1111111";
                }
            }else {
                if (Float.parseFloat(higlow)<0||Float.parseFloat(higlow)>20){
                    text = "1111111";
                }
            }
        }

        if (typestr.equals("KΩ·cm")){
            if (mSpinnerUnit.getSelectedItemId()==0||mSpinnerUnit.getSelectedItemId()==1){
                if (Float.parseFloat(higlow)<0||Float.parseFloat(higlow)>999){
                    text = "1111111";
                }
            }else {
                if (Float.parseFloat(higlow)<0||Float.parseFloat(higlow)>20){
                    text = "1111111";
                }
            }
        }

        return text;
    }

    private void messageToast(String msg) {
//        ToastUtils.showShort(msg);
//        ToastUtilsX.showTextActi(getActivity(),msg);
        Toast.makeText(getActivity(),msg,Toast.LENGTH_LONG).show();
    }

    private void TypeChange(){
        if (typestr.equals("pH")){
            if(mParam.lowHit!=null)mEditViewLow.setHint(mParam.lowHit);
            if(mParam.highHit!=null)mEditViewHigh.setHint(mParam.highHit);
        }
        if (typestr.equals("mS/cm")){
            if (mSpinnerUnit.getSelectedItemId()==1){
                if(mParam.lowHit!=null)mEditViewLow.setHint("0-1999 μS/cm");
                if(mParam.highHit!=null)mEditViewHigh.setHint("0-1999 μS/cm");
            }else {
                if(mParam.lowHit!=null)mEditViewLow.setHint(mParam.lowHit);
                if(mParam.highHit!=null)mEditViewHigh.setHint(mParam.highHit);
            }            }
        if (typestr.equals("ppt")){
            if (mSpinnerUnit.getSelectedItemId()==1){
                if(mParam.lowHit!=null)mEditViewLow.setHint("0-999 ppm");
                if(mParam.highHit!=null)mEditViewHigh.setHint("0-999 ppm");
            }else {
                if(mParam.lowHit!=null)mEditViewLow.setHint("0-10 ppt");
                if(mParam.highHit!=null)mEditViewHigh.setHint("0-10 ppt");
            }
        }
        if (typestr.equals("MΩ")){
            if (mSpinnerUnit.getSelectedItemId()==0){
                if(mParam.lowHit!=null)mEditViewLow.setHint("0-999 Ω·cm");
                if(mParam.highHit!=null)mEditViewHigh.setHint("0-999 Ω·cm");
            }else if (mSpinnerUnit.getSelectedItemId()==1){
                if(mParam.lowHit!=null)mEditViewLow.setHint("0-999 KΩ·cm");
                if(mParam.highHit!=null)mEditViewHigh.setHint("0-999 KΩ·cm");
            }else {
                mEditViewHigh.setHint("0MΩ-20MΩ");
                mEditViewLow.setHint("0MΩ-20MΩ");
            }
        }
    }

    public static double parseInt(String s) {
        try{
            return  Double.parseDouble(s);
        }catch (Exception e){
            return  0.0;
        }
    }

    private boolean checkValue(String unit) {

        if(TextUtils.isEmpty(unit)
                || "KΩ".equals(unit) || "KΩ·cm".equals(unit)
                || "K".equals(unit)
                || "MΩ".equals(unit) || "MΩ·cm".equals(unit)
                || "M".equals(unit)
                || "Ω".equals(unit) || "Ω·cm".equals(unit)
                || "pH".equals(unit)
                || "p".equals(unit)
                || "mV".equals(unit)
                || "m".equals(unit)
                || "µS".equals(unit)  || "µS/cm".equals(unit)
                || "µS".equals(unit)  || "µS/cm".equals(unit)
                || "µ".equals(unit) || "µ".equals(unit)
                || "mS".equals(unit) || "mS/cm".equals(unit)
                || "m".equals(unit)
                || "ppm".equals(unit)
                || "p".equals(unit)
                || "pp".equals(unit)
                || "ppt".equals(unit)
                || "g/l".equals(unit)
                || "g".equals(unit)
                || "g/".equals(unit)
                || "mg/l".equals(unit)
                || "mg".equals(unit)
                || "mg/".equals(unit)
                )
        return true;

        return false;
    }

    private String mTextSelect;
    private String value;


    private boolean mSwitchValue = false;
    private TextView tvLeft;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting_alarm2, container, false);

        mTextViewAlarmTips = view.findViewById(R.id.tv_alarm_tips);
        mTextViewAlarmRang = view.findViewById(R.id.tv_alarm_rang);
        mTextViewTitle = view.findViewById(R.id.tv_title);
        mEditViewHigh = view.findViewById(R.id.et_high);
        mEditViewLow = view.findViewById(R.id.et_low);
        mSwViewHigh = view.findViewById(R.id.switch_high);
        mSpinnerUnit = view.findViewById(R.id.sp_unit);
        mSpinnerUnit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
                OnSpItemSelected(view);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mEditViewLow.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                lowOnTextChanged();
            }
        });

        mEditViewHigh.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                highOnTextChanged();
            }
        });

        mSwViewHigh.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean check) {
                isHightCheck = check;
                if(update) return;
                BleDevice bleDevice = MyApi.getInstance().getBtApi().getLastDevice();
                if (bleDevice != null && bleDevice.getSetting() != null) {
                    Map<String, Object> data = bleDevice.getSetting().getParam(settingIndex).data;
                    data.put(Constant.highSwitchValue, check ? Constant.ON : Constant.OFF);
                    mParam.data.put(Constant.highSwitchValue, check ? Constant.ON : Constant.OFF);
                    //MyApi.getInstance().getDataApi().updateBleDevice(bleDevice, DataApi.SETTING);
                    mSaveView.setVisibility(View.VISIBLE);
                }
            }
        });



        mSwViewLow = view.findViewById(R.id.switch_low);
        mSwViewLow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean check) {
                isLowCheck = check;
                if(update) return;
                BleDevice bleDevice = MyApi.getInstance().getBtApi().getLastDevice();
                if (bleDevice != null && bleDevice.getSetting() != null) {
                    Map<String, Object> data = bleDevice.getSetting().getParam(settingIndex).data;
                    data.put(Constant.lowSwitchValue, check ? Constant.ON : Constant.OFF);
                    mParam.data.put(Constant.lowSwitchValue, check ? Constant.ON : Constant.OFF);
                    //MyApi.getInstance().getDataApi().updateBleDevice(bleDevice, DataApi.SETTING);
                    mSaveView.setVisibility(View.VISIBLE);
                }
            }
        });
        mSaveView = view.findViewById(R.id.tv_right);
        mSaveView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save(view);
            }
        });


        mEditViewLow.setHint("0");
        mEditViewHigh.setHint("0");
        mTextViewTitle.setText(title);
        tvLeft = view.findViewById(R.id.tv_left);

        tvLeft.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });
        return view;
    }

    @Override
    public void onDestroy() {
        SharedPreferences sharedPreferences = MyApi.getInstance().getDataApi().getSetting();
        String out = JSON.toJSONString(mParam);
        sharedPreferences.edit().putString(settingIndex, out).apply();
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateView1();
        //   API.getResourceApi().update(ResourceApi.HOT_VIEW, ResourceApi.APP_LIST);
    }

    private boolean update=false;
    public void updateView1() {
        super.updateView();
        update = true;
        // BleDevice bleDevice = MyApi.getInstance().getBtApi().getLastDevice();

        //;if (bleDevice != null && bleDevice.getSetting() != null)
        for (int i = 0; i < Constant.ALARMs.length; i++) {
            if (Constant.ALARMs[i].equals(settingIndex)) {
                defaultAlarm = Constant.DEFAULT_ALARMs[i];
            }
        }
        SharedPreferences sharedPreferences = MyApi.getInstance().getDataApi().getSetting();
        String str = sharedPreferences.getString(settingIndex, defaultAlarm);

        // DeviceSetting.Param param = bleDevice.getSetting().getParam(settingIndex);
        mParam = JSON.parseObject(str, DeviceSetting.Param.class);//bleDevice.getSetting().getParam(settingIndex);
        if (mParam.name == null) mParam.name = settingIndex;

//        mEditViewLow.setText(getValueString(mParam.getString(Constant.lowValue),mParam.getString(Constant.lowValueUnit)));
//        mEditViewHigh.setText(getValueString(mParam.getString(Constant.highValue),mParam.getString(Constant.highValueUnit)));

        mSwViewLow.setChecked(Constant.ON.equals(mParam.getString(Constant.lowSwitchValue)));
        mSwViewHigh.setChecked(Constant.ON.equals(mParam.getString(Constant.highSwitchValue)));

        if (isLowCheck){
            mEditViewLow.setText(getValueString(mParam.getString(Constant.lowValue),mParam.getString(Constant.lowValueUnit)));
        }else {
            if(mParam.lowHit!=null)mEditViewLow.setHint(mParam.lowHit);
        }
        if (isHightCheck){
            Log.w(TAG,"testtype"+mParam.getString(Constant.highValue));
            mEditViewHigh.setText(getValueString(mParam.getString(Constant.highValue),mParam.getString(Constant.highValueUnit)));
        }else {
            if(mParam.highHit!=null)mEditViewHigh.setHint(mParam.highHit);
        }
        if (mParam.getString(Constant.rangTips) != null) {
            mTextViewAlarmRang.setText(mParam.getString(Constant.rangTips));
        } else if (mParam.getString(Constant.rangTipsId) != null) {
            int stringID = getResources().getIdentifier(mParam.getString(Constant.rangTipsId),// string.xml内配置的名字
                    "string", getContext().getPackageName());
            if(stringID>0) {
                String string = getResources().getString(stringID);
                mTextViewAlarmRang.setText(string);
            }
        }
        if (mParam.getString(Constant.alarmTipsId) != null) {
            int stringID = getResources().getIdentifier(mParam.getString(Constant.alarmTipsId),// string.xml内配置的名字
                    "string", getContext().getPackageName());
            if(stringID>0) {
                String string = getResources().getString(stringID);
                mTextViewAlarmTips.setText(string);
            }
        }
        //ALARM_RESISTIVITY
        if ("ALARM_RESISTIVITY".equals(mParam.name)) {
            mSpinnerUnit.setVisibility(View.VISIBLE);

            String lu = mParam.getString(Constant.lowValueUnit);
            Log.w(TAG,"testtype"+lu);
            if (lu == null) {
                mSpinnerUnit.setSelection(0);
            } else {
                typestr = lu;
                SpinnerAdapter adapter = mSpinnerUnit.getAdapter();
                for (int i = 0; i < adapter.getCount(); i++) {
                    if (lu.equals(adapter.getItem(i))) {
                        mSpinnerUnit.setSelection(i);
                        break;
                    }
                }
            }
    /*        mSpinnerHigh.setVisibility(View.VISIBLE);
            String hu = mParam.getString(Constant.highValueUnit);
            if (hu == null) {
                mSpinnerHigh.setSelection(0);
            } else {
                SpinnerAdapter adapter = mSpinnerHigh.getAdapter();
                for (int i = 0; i < adapter.getCount(); i++) {
                    if (hu.equals(adapter.getItem(i))) {
                        mSpinnerHigh.setSelection(i);
                        break;
                    }
                }
            }*/

        } else {
          //  mSpinnerLow.setVisibility(View.INVISIBLE);
          //  mSpinnerHigh.setVisibility(View.INVISIBLE);
                    ArrayAdapter<String> adapterHigh = new ArrayAdapter<String>(
                               this.getContext(), android.R.layout.simple_spinner_item,
                              getData(mParam.name,1));
            typestr = adapterHigh.getItem(0);
            mSpinnerUnit.setAdapter(adapterHigh);
        ArrayAdapter<String> adapterLow = new ArrayAdapter<String>(
                this.getContext(), android.R.layout.simple_spinner_item,
                getData(mParam.name,0));
            mSpinnerUnit.setAdapter(adapterLow);
            if(adapterHigh.getCount()<=1){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mSpinnerUnit.setBackground(null);
                }
            }

            String lu = mParam.getString(Constant.lowValueUnit);
            if (lu == null) {
                mSpinnerUnit.setSelection(0);
            } else {
                if (lu.equals("µS/cm")||lu.equals("ppm")){
                    mSpinnerUnit.setSelection(1);
                }else {
                    mSpinnerUnit.setSelection(0);
                }
            }
        }

        TypeChange();
        update = false;
    }

    private String[] getData(String name, int i) {
        if("ALARM_RESISTIVITY".equals(name)){
            return getResources().getStringArray(R.array.res_unit);

        }else if("ALARM_TDS".equals(name)){
            return getResources().getStringArray(R.array.tds_unit);
        }
        else if("ALARM_SALINITY".equals(name)){
            return getResources().getStringArray(R.array.sail_unit);
        }
        else if("ALARM_COND".equals(name)){
            return getResources().getStringArray(R.array.cond_unit);
        }
        else if("ALARM_ORP".equals(name)){
            return getResources().getStringArray(R.array.orp_unit);
        }
        else if("ALARM_PH".equals(name)){
            return getResources().getStringArray(R.array.ph_unit);
        }
        return new String[0];
    }

    private String getValueDigitsOnly(String string) {
        if(TextUtils.isDigitsOnly(string)) return string;
        //String a="love23next234csdn3423javaeye";
        String regEx="[^.0-9]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(string);
        return  m.replaceAll("").trim();
    }

    private String getValueUnit(String string) {
        if(TextUtils.isDigitsOnly(string)) return "";
        String value =  getValueDigitsOnly(string);
        return  string.substring(value.length());
    }

    private double getBaseValue(String value, String unit) {
        double v = parseInt(value);

        if (unit == null) {

        }
        return v;
    }

    private String getValueString(String value, String unit) {
        if (unit == null) {
            unit = "";
        }
        return value;// + unit;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setListValue(String... lists) {
        if (lists != null && lists.length > 0) {
            for (String s : lists) {
                listValue.add(s);
            }
        }
    }

    public String getSelectValue() {
        if (mSwitchValue) {
            return mTextSelect;
        } else {
            return Constant.OFF;
        }
    }

    public void setValue(String value) {
        mSwitchValue = !Constant.OFF.equalsIgnoreCase(value);
        this.value = value;
    }
}