package com.zen.ui.fragment;

import android.os.Build;
import android.os.Bundle;
import androidx.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zen.ui.R;

import com.zen.ui.Setting2Activity;
import com.zen.ui.base.BaseFragment;


/**
 * Created by louis on 17-12-21. 告警页面
 */
public class SettingFragmentAlarm extends BaseFragment implements View.OnClickListener {

    private TextView tvLeft;
    private RelativeLayout layout_item1;
    private RelativeLayout layout_item2;
    private RelativeLayout layout_item3;
    private RelativeLayout layout_item4;

    private RelativeLayout layout_item5;
    private RelativeLayout layout_item6;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.setting_alarm, container, false);
        tvLeft = view.findViewById(R.id.tv_left);
        tvLeft.setOnClickListener(this);

        layout_item1 = view.findViewById(R.id.layout_item1);
        layout_item1.setOnClickListener(this);
        layout_item2 = view.findViewById(R.id.layout_item2);
        layout_item2.setOnClickListener(this);

        layout_item3 = view.findViewById(R.id.layout_item3);
        layout_item3.setOnClickListener(this);

        layout_item4 = view.findViewById(R.id.layout_item4);
        layout_item4.setOnClickListener(this);

        layout_item5 = view.findViewById(R.id.layout_item5);
        layout_item5.setOnClickListener(this);

        layout_item6 = view.findViewById(R.id.layout_item6);
        layout_item6.setOnClickListener(this);



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
    public void onClick(View view) {
        if (view.getId() == R.id.layout_item1) {
            Setting2Activity.showMe(getContext(), Setting2Activity.ALARM_PH);
        } else if (view.getId() == R.id.layout_item2) {
            Setting2Activity.showMe(getContext(), Setting2Activity.ALARM_ORP);
        } else if (view.getId() == R.id.layout_item3) {
            Setting2Activity.showMe(getContext(), Setting2Activity.ALARM_COND);
        } else if (view.getId() == R.id.layout_item4) {
            Setting2Activity.showMe(getContext(), Setting2Activity.ALARM_SALINITY);
        } else if (view.getId() == R.id.layout_item5) {
            Setting2Activity.showMe(getContext(), Setting2Activity.ALARM_TDS);
        } else if (view.getId() == R.id.layout_item6) {
            Setting2Activity.showMe(getContext(), Setting2Activity.ALARM_RESISTIVITY);
        } else if (view.getId() == R.id.tv_left) {
            getActivity().finish();
        }

    }


}