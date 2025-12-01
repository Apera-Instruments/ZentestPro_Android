package com.zen.ui.fragment;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.zen.api.MyApi;
import com.zen.ui.R;
import com.zen.ui.base.BaseFragment;
import com.zen.ui.utils.StatusBarUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;


/**
 * Created by louis on 17-12-21.
 */
public class SettingFragmentData extends BaseFragment implements View.OnClickListener {

    private int list[] = {
            R.id.layout_item1,
           // R.id.layout_item2,
            R.id.layout_item3,
          //  R.id.layout_item4,
            R.id.layout_item5,
          //  R.id.layout_item6,
            R.id.layout_item7,
            R.id.layout_item8,
            R.id.layout_item9,
            R.id.layout_item10,
            R.id.layout_item11,
            R.id.layout_item12,
            R.id.layout_item13,
    };


    private int cbList[] = {
            R.id.cb_1,
            R.id.cb_2,
            R.id.cb_3,
            R.id.cb_4,
            R.id.cb_5,
            R.id.cb_6,
            R.id.cb_7,
            R.id.cb_8,
            R.id.cb_9,
            R.id.cb_10,
            R.id.cb_11,
            R.id.cb_12,
            R.id.cb_13,
    };
    private String cbSaveIdList[] = {
            "all_data",
            "mes_value",//need 2
            "model_no",//3
            "time_data",//need 4
            "ser_no",
            "temper",//need 6
            "mv_error",//7,
            "calibration",
            "slop",//9,
            "ele_model",//10,
            "gps",
            "wea_temp",//12,
            "air_pre",//13,
    };
    List<CheckBox > checkBoxList = new ArrayList<>();
    private Handler mHandler = new Handler();
    private View mRightView;
    private Map<String, Boolean> mSettingMap = new HashMap<>();

    //private List<CheckBox> checkBoxList = new ArrayList<>();
    private TextView tvLeft;


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.setting_data, container, false);
        mRightView  = view.findViewById(R.id.tv_right);
        if(mRightView!=null){
            mRightView.setOnClickListener(this);
        }
        tvLeft = view.findViewById(R.id.tv_left);
        tvLeft.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });
        SharedPreferences sharedPreferences = MyApi.getInstance().getDataApi().getSetting();
        int index = 0;
        for (int id : cbList) {
            CheckBox checkBox = (CheckBox) view.findViewById(id);
            if (!(index == 2 - 1 || index == 4 - 1 || index == 6 - 1)) {
                checkBox.setChecked(sharedPreferences.getBoolean("SettingFragmentData:" + cbSaveIdList[index], false));

                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        int id = buttonView.getId();
                        String saveId = "";
                        for (int i = 0; i < cbList.length; i++) {
                            if (cbList[i] == id) {
                                saveId = cbSaveIdList[i];
                                break;
                            }
                        }
                        SharedPreferences sharedPreferences = MyApi.getInstance().getDataApi().getSetting();
                        if (isChecked != sharedPreferences.getBoolean("SettingFragmentData:" + saveId, false)) {
                            mRightView.setVisibility(View.VISIBLE);
                        }
                        mSettingMap.put("SettingFragmentData:" + saveId, isChecked);
                    /*    SharedPreferences sharedPreferences = MyApi.getInstance().getDataApi().getSetting();
                        sharedPreferences.edit().
                                putBoolean("SettingFragmentData:" + saveId, isChecked).apply();*/

                        if ("all_data".equals(saveId)) {
                            //if(isChecked)
                            {
                                
                                for (CheckBox box : checkBoxList) {
                                    if (box.isClickable()) {
                                        if (box.isChecked() != isChecked) {
                                            box.setChecked(isChecked);
                                            box.requestLayout();
                                            box.refreshDrawableState();
                                        }
                                    }
                                }
                            }
                        }
                    }
                });

            } else {
                checkBox.setChecked(true);
                checkBox.setClickable(false);
            }
            checkBoxList.add(checkBox);
            index++;
        }
        for(int id:list){
           ViewGroup view1 =  view.findViewById(id);
           if(view1!=null){

               view1.setOnClickListener(this);
               for(int i=0;i<view1.getChildCount();i++){
                   if(view1.getChildAt(i) instanceof CheckBox){
                       CheckBox checkBox =  (CheckBox) (view1.getChildAt(i));
                      // checkBoxList.add(checkBox);
                      // checkBox.setChecked(true);
                       view1.setTag(checkBox);
                       break;

                   }
               }
           }
        }
        //StatusBarUtil.setStatusBarColor(getActivity(),getResources().getColor(R.color.colorPrimaryDark));


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

    @Override
    public void onClick(View v) {
        Log.v(getTAG(),"onClick "+v);
 /*       ViewGroup viewGroup = ((ViewGroup)v);
        for(int i=0;i<viewGroup.getChildCount();i++){
            if(viewGroup.getChildAt(i) instanceof CheckBox){
                CheckBox checkBox =  (CheckBox) (viewGroup.getChildAt(i));
                final boolean c= !checkBox.isChecked();
                checkBox.setChecked(c);
                Log.d(TAG,"checkBox  setChecked "+c);

            }
        }*/
        if (v.getId() == R.id.tv_right) {
               saveSetting();
               v.setVisibility(View.INVISIBLE);
        }
        else if (v.getTag() instanceof CheckBox) {
            CheckBox checkBox = (CheckBox) (v.getTag());
            checkBox.toggle();
            checkBox.requestLayout();
            checkBox.refreshDrawableState();
            Log.d(getTAG(), "checkBox  setChecked " );

        }
    }

    private void saveSetting() {
        SharedPreferences sharedPreferences = MyApi.getInstance().getDataApi().getSetting();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<Map.Entry<String, Boolean>> sets = mSettingMap.entrySet();
        for (Map.Entry<String, Boolean> entry : sets) {
            editor.putBoolean(entry.getKey(), entry.getValue());
            Log.v(getTAG(),entry.getKey()+" "+entry.getValue());
        }
        editor.apply();
        getActivity().finish();
    }


}