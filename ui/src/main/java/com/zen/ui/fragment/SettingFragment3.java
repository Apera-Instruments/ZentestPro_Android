package com.zen.ui.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.zen.ui.R;
import com.zen.ui.base.BaseFragment;


/**
 * Created by louis on 17-12-21.
 */
public class SettingFragment3 extends BaseFragment implements View.OnClickListener, TextWatcher {



    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting3, container, false);

        return view;
    }






    public void updateView() {
        super.updateView();


    }

    @Override
    public void onClick(View v) {
        Object object = v.getTag();

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


}