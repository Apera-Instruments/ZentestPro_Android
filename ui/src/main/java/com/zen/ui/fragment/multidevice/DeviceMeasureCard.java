package com.zen.ui.fragment.multidevice;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zen.api.Constant;
import com.zen.api.DataApi;
import com.zen.api.MyApi;
import com.zen.api.data.DataBean;
import com.zen.api.protocol.Data;
import com.zen.biz.velabt.event.DeviceDataUpdatedEvent;
import com.zen.ui.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

@SuppressLint("ViewConstructor")
public class DeviceMeasureCard extends LinearLayout {

    private String mac;
    private TextView tvMac;
    private TextView tvValuePh, tvUnitPh;
    private TextView tvValueCond, tvUnitCond;
    private TextView tvValueOrp, tvUnitOrp;
    private ImageView imgBt;

    public DeviceMeasureCard(Context ctx, String mac) {
        super(ctx);
        this.mac = mac;

        LayoutInflater.from(ctx).inflate(R.layout.device_measure_card, this, true);

        tvMac = findViewById(R.id.tv_mac);
        tvValuePh = findViewById(R.id.tv_value_ph);
        tvUnitPh = findViewById(R.id.tv_unit_ph);
        tvValueCond = findViewById(R.id.tv_value_cond);
        tvUnitCond = findViewById(R.id.tv_unit_cond);
        tvValueOrp = findViewById(R.id.tv_value_orp);
        tvUnitOrp = findViewById(R.id.tv_unit_orp);

        tvMac.setText(mac);

        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDeviceUpdate(DeviceDataUpdatedEvent e) {
        if (!e.mac.equals(mac)) return;
        updateFromDataApi();
    }

    private void updateFromDataApi() {
        DataApi dataApi = MyApi.getInstance().getDataApi(mac);
        if (dataApi == null) return;

        DataBean bean = dataApi.getData();
        if (bean == null) return;

        Data data = bean.getData();
        if (data == null) return;

        switch (data.getMode()) {
            case Constant.MODE_VELA_PH:
                tvValuePh.setText(format(data.getValue(), data.getPointDigit()));
                tvUnitPh.setText(data.getUnitString());
                break;

            case Constant.MODE_VELA_COND:
                tvValueCond.setText(format(data.getValue(), data.getPointDigit()));
                tvUnitCond.setText(data.getUnitString());
                break;

            case Constant.MODE_VELA_ORP:
                tvValueOrp.setText(format(data.getValue(), data.getPointDigit()));
                tvUnitOrp.setText(data.getUnitString());
                break;
        }
    }

    private String format(double v, int point) {
        return String.format("%." + point + "f", v);
    }
}