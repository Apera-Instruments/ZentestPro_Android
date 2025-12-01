package com.zen.ui.fragment;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.zen.api.DataApi;
import com.zen.api.MyApi;
import com.zen.api.data.BleDevice;
import com.zen.api.data.DeviceSetting;
import com.zen.api.event.SettingDeviceEvent;
import com.zen.ui.R;
import com.zen.ui.view.PickerScrollView;
import com.zen.ui.view.Pickers;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by louis on 17-12-21.
 */
public class SettingFragmentTime extends SettingFragment2 implements View.OnClickListener {

    private PickerScrollView pickerscrlllview;
    private List<Pickers> list=new ArrayList<>();
    private List<String> listValue=new ArrayList<>();

    TextView mTextViewSetTitle;
    TextView mTextViewSelect;
    TextView mTextViewSave;
    TextView mTextViewTitle;
    Switch switch1;
    private String mTextSelect;
    private String value;

    ViewGroup mLayoutSetting;
    boolean settingDevice =false;
    @Override
    public void onClick(View v) {
        // Handle navigation view item clicks here.
        int id = v.getId();
        if (id == R.id.tv_right) {
            onSave();
        }
    }

    void onSave(){
        if(settingDevice){
        BleDevice bleDevice = MyApi.getInstance().getBtApi().getLastDevice();
          if(bleDevice==null ){
            Log.w(getTAG(), "bleDevice = null");
            return;
          }
            String result = getSelectValue();
            int settingIndex = -1;

            if (listValue != null && result != null) {
                for (int i = 0; i < listValue.size(); i++) {
                    if (result.equals(listValue.get(i))) {
                        settingIndex = i;
                        break;
                    }
                }
            }
            DeviceSetting setting = bleDevice.getSetting();
            SharedPreferences sharedPreferences = MyApi.getInstance().getDataApi().getSetting();
            sharedPreferences.edit().putString(getSaveId(), result).apply();
            MyApi.getInstance().getDataApi().updateBleDevice(bleDevice, DataApi.SETTING);
            EventBus.getDefault().post(new SettingDeviceEvent());
            mTextViewSave.setVisibility(View.INVISIBLE);
            settingDevice=false;
            getActivity().finish();
        }
    }

//    @OnCheckedChanged(R.id.switch1)
//    void switchOnCheckedChanged(Switch switch1){
//        mSwitchValue = switch1.isChecked();
//        if(mSwitchValue){
//            mLayoutSetting.setVisibility(View.VISIBLE);
//        }else{
//            mLayoutSetting.setVisibility(View.INVISIBLE);
//        }
//        settingDevice =true;
//        mTextViewSave.setVisibility(View.VISIBLE);
//    }

    private boolean mSwitchValue=false;
    private TextView tvLeft;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting_time, container, false);
        mTextViewSetTitle = view.findViewById(R.id.tv_set_title);
        mTextViewSelect = view.findViewById(R.id.tv_select);
        mTextViewSave = view.findViewById(R.id.tv_right);
        mTextViewTitle = view.findViewById(R.id.tv_title);
        switch1 = view.findViewById(R.id.switch1);
        mLayoutSetting = view.findViewById(R.id.layout_setting);
        mTextViewSave.setOnClickListener(this);
        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mSwitchValue = isChecked;
                if(mSwitchValue){
                    mLayoutSetting.setVisibility(View.VISIBLE);
                }else{
                    mLayoutSetting.setVisibility(View.INVISIBLE);
                }
                settingDevice =true;
                mTextViewSave.setVisibility(View.VISIBLE);
            }
        });


        initView(view);
        initData();
        initLinstener();
        tvLeft = view.findViewById(R.id.tv_left);
        tvLeft.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });

        switch1.setChecked(mSwitchValue);
        mTextViewTitle.setText(title);
        mTextViewSetTitle.setText(setTitle);
        if(mSwitchValue){
            mLayoutSetting.setVisibility(View.VISIBLE);
        }else{
            mLayoutSetting.setVisibility(View.INVISIBLE);
        }
        settingDevice =false;
        mTextViewSave.setVisibility(View.INVISIBLE);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateView();
     //   API.getResourceApi().update(ResourceApi.HOT_VIEW, ResourceApi.APP_LIST);
    }

    public void updateView() {
        super.updateView();
    }
    /**
     * 初始化
     */
    private void initView(View view) {
      //  bt_scrollchoose = (Button) findViewById(R.id.bt_scrollchoose);
      //  picker_rel = (RelativeLayout) findViewById(R.id.picker_rel);
        pickerscrlllview = (PickerScrollView) view.findViewById(R.id.pickerscrlllview);
       // bt_yes = (Button) findViewById(R.id.picker_yes);
    }

    /**
     * 设置监听事件
     */
    private void initLinstener() {
        //bt_scrollchoose.setOnClickListener(onClickListener);
        pickerscrlllview.setOnSelectListener(pickerListener);
      //  bt_yes.setOnClickListener(onClickListener);
    }

    /**
     * 初始化数据
     */
    private void initData() {
    /*    list = new ArrayList<Pickers>();
        String[] id = new String[]{"1", "2", "3", "4", "5", "6"};
        String[] name = new String[]{"1 Seconds", "2 Seconds", "3 Seconds", "4 Seconds", "5 Seconds", "6 Seconds"};
        for (int i = 0; i < name.length; i++) {
            list.add(new Pickers(name[i], id[i]));
        }*/
       int selectIndex = 0;
        for (int i = 0; i < listValue.size(); i++) {
            list.add(new Pickers(listValue.get(i), ""+i));
            if(listValue.get(i).equals(value)) selectIndex = i;
        }
        // 设置数据，默认选择第一条
        pickerscrlllview.setData(list);
        mTextViewSelect.setText(mTextSelect = list.get(selectIndex).getShowConetnt());
        pickerscrlllview.setSelected(selectIndex);
    }

    // 滚动选择器选中事件
    PickerScrollView.onSelectListener pickerListener = new PickerScrollView.onSelectListener() {

        @Override
        public void onSelect(Pickers pickers) {
            System.out.println("选择：" + pickers.getShowId() + "--："
                    + pickers.getShowConetnt());
            mTextViewSelect.setText(mTextSelect = pickers.getShowConetnt());
            settingDevice =true;
            mTextViewSave.setVisibility(View.VISIBLE);
        }
    };

    // 点击监听事件
    View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
          /*  if (v == bt_scrollchoose) {
                picker_rel.setVisibility(View.VISIBLE);
            } else if (v == bt_yes) {
                picker_rel.setVisibility(View.GONE);
            }*/
        }
    };

    public void setTitle(String title) {
        this.title = title;

    }

    public void setListValue(String  ... lists) {
        if(lists!=null && lists.length>0){
            for(String s:lists){
                listValue.add(s);
            }

        }

    }

    public String getSelectValue() {
        if (mSwitchValue) {
            return mTextSelect;
        } else {
            return "OFF";
        }
    }

    public void setValue(String value) {
        mSwitchValue = !"OFF".equalsIgnoreCase(value);
        this.value = value;
    }



}