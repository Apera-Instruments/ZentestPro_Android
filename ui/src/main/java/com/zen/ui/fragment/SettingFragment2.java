package com.zen.ui.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zen.api.event.UpdateEvent;
import com.zen.ui.R;
import com.zen.ui.base.BaseFragment;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


/**
 * Created by louis on 17-12-21.
 */
public abstract class SettingFragment2 extends BaseFragment {


    protected String settingIndex;
    protected int index;
    protected String setTitle;
    protected String title;
    private boolean saveSetting=false;
    private String saveId;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting2, container, false);

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


    }


    public abstract void setTitle(String string);

    public abstract void setValue(String off);

    public abstract void setListValue(String ... s);

    public abstract String getSelectValue();

    public void setSettingIndex(String settingIndex) {
        this.settingIndex = settingIndex;
    }

    public String getSettingIndex() {
        return settingIndex;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public void setSetTitle(String setTitle) {
        this.setTitle = setTitle;
    }

    public boolean getSaveSetting() {
        return saveSetting;
    }

    public boolean isSaveSetting() {
        return saveSetting;
    }

    public void setSaveSetting(boolean saveSetting) {
        this.saveSetting = saveSetting;
    }

    public void setSaveId(String saveId) {
        this.saveId = saveId;
    }

    public String getSaveId() {
        return saveId;
    }
}