package com.zen.ui.fragment;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.zen.api.MyApi;
import com.zen.api.SettingConfig;
import com.zen.ui.R;
import com.zen.ui.Setting2Activity;
import com.zen.ui.adapter.ParameterSimpleAdapter;
import com.zen.ui.view.Pickers;

import java.util.ArrayList;
import java.util.List;
/**
 * Created by louis on 17-12-21.
 */
public class SettingFragmentParameter2 extends SettingFragment2 {

    private List<Pickers> list=new ArrayList<>();
    private List<String> listValue=new ArrayList<>();
    private String title="";


    TextView mTextViewTitle;
    ListView mListView;

    private String mTextSelect;
    private String value;

    private TextView tvLeft;


    private boolean mSwitchValue=false;
    private ParameterSimpleAdapter mParameterSimpleAdapter;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting_parameter2, container, false);
        mTextViewTitle = view.findViewById(R.id.tv_title);
        mListView = view.findViewById(R.id.list_view);
        mParameterSimpleAdapter  = new ParameterSimpleAdapter(getContext());
        mListView.setAdapter(mParameterSimpleAdapter);
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
    public void onResume() {
        super.onResume();
        updateView();
     //   API.getResourceApi().update(ResourceApi.HOT_VIEW, ResourceApi.APP_LIST);
    }

    public void updateView() {
        super.updateView();

        SettingConfig settingConfig=null;
        switch (getIndex()){
            case Setting2Activity.PARAM_COND:
                settingConfig =  SettingConfig.COND;
                break;
            case Setting2Activity.PARAM_SALINITY:
                settingConfig =  SettingConfig.Salinity;
                break;
            case Setting2Activity.PARAM_PH:
                settingConfig =  SettingConfig.pH;
                break;
            case Setting2Activity.PARAM_TDS:
                settingConfig =  SettingConfig.TDS;
                break;
            default:
                break;

        }
        List<SettingConfig.Config> configs =settingConfig==null?null:   settingConfig.getList();
        SharedPreferences sharedPreferences =  MyApi.getInstance().getDataApi().getSetting();
        mParameterSimpleAdapter.clear();
        if(configs!=null && configs.size()>0){
            for(SettingConfig.Config config :configs){
                ParameterSimpleAdapter.Bean bean = new ParameterSimpleAdapter.Bean();
                bean.type = config.type;
                bean.title = config.title;
                bean.mSwitchTabsResId = config.mSwitchTabsResId;
                bean.layoutId = ParameterSimpleAdapter.getLayoutId( bean.type);
                bean.saveId = config.saveId;
                bean.value=sharedPreferences.getString(bean.saveId ,config.defaultValue);
                Log.i(getTAG(), "getString "+bean.saveId +" "+bean.value);
                mParameterSimpleAdapter.addData(bean);
            }
        }
        mParameterSimpleAdapter.notifyDataSetChanged();
    }

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