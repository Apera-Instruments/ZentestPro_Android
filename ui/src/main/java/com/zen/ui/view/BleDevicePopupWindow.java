package com.zen.ui.view;

import android.bluetooth.BluetoothClass;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TextView;

import com.foolchen.lib.tracker.Tracker;
import com.zen.api.Constant;
import com.zen.api.MyApi;
import com.zen.api.data.BleDevice;
import com.zen.ui.R;

import java.util.ArrayList;
import java.util.List;


public class BleDevicePopupWindow extends PopupWindow implements View.OnClickListener {
    private static final String TAG = "BleDevicePopupWindow";
    private final ViewGroup mViewGroup;
    private final ImageView mImageViewSearch;
    private final Animation animation;
    private final Switch mLinkedSwitch;
    private final View mLinkView;
    private final TextView mLinkNameView;
    private final View mLinkDeviceView;
    private String linkMac="";
    private List<View> viewList = new ArrayList<>();
    private onButtonClickListener onClickListener;
    private String linkName;

    public BleDevicePopupWindow(Context context) {
        super(context);
        View contentView = LayoutInflater.from(context).inflate(R.layout.ble_device_popup_layout, null);
        setContentView(contentView);
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setFocusable(true);
        setOutsideTouchable(true);
        setBackgroundDrawable(new ColorDrawable(0x00000000));


        mLinkDeviceView = contentView.findViewById(R.id.lv_associated_devices);
        mLinkView = contentView.findViewById(R.id.layout_associated_devices);
        mLinkNameView = contentView.findViewById(R.id.tv_associated_name);
        linkMac= context.getString(R.string.demo_device);
        mLinkView.setTag(linkMac);
        mLinkView.setOnClickListener(this);
        mViewGroup = contentView.findViewById(R.id.layout_devices);
        mLinkedSwitch = contentView.findViewById(R.id.iv_associated_link);
        mLinkedSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i(TAG,"onCheckedChanged");
                if (isChecked) {
                    int ret = MyApi.getInstance().getBtApi().reconnect();
                    if (ret == 0) {
                        if (onClickListener != null && MyApi.getInstance().getBtApi().getLastDevice() == null) {
                            onClickListener.onClick(linkMac);
                        }
                        buttonView.post(new Runnable() {
                            @Override
                            public void run() {
                                boolean connected = MyApi.getInstance().getBtApi().isConnected();
                                if (connected != mLinkedSwitch.isChecked())
                                    mLinkedSwitch.setChecked(connected);
                            }
                        });
                    }
                } else {
                    MyApi.getInstance().getBtApi().disconnect();
                    Tracker.INSTANCE.trackEvent("ble_disconnect",null);
                    SharedPreferences sharedPreferences = MyApi.getInstance().getDataApi().getSetting();
                    sharedPreferences.edit().putString("connectedDev","").apply();
                }
            }
        });

        mImageViewSearch = contentView.findViewById(R.id.iv_searching);
        animation = AnimationUtils.loadAnimation(context,R.anim.rotate);
        animation.setInterpolator(new LinearInterpolator());
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                Log.d(TAG,"onAnimationStart");
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Log.d(TAG,"onAnimationEnd");
                if(isShowing())
                {

                    boolean connected =MyApi.getInstance().getBtApi().isConnected();
                    if(connected!=mLinkedSwitch.isChecked()) mLinkedSwitch.setChecked(connected);
                    if(connected) return;
                    boolean connecting =MyApi.getInstance().getBtApi().isConnecting();
                    if(connecting) return;
                    boolean scanning =MyApi.getInstance().getBtApi().isScanning();
                    if(!scanning){
                        MyApi.getInstance().getBtApi().startScan();
                    }
                    mImageViewSearch.startAnimation(animation);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
               Log.d(TAG,"onAnimationRepeat");
            }
        });
        //mImageViewSearch.startAnimation(animation);

    }

    public void clearDevices() {
        mViewGroup.removeAllViews();
        bleDevices.clear();
    }

    public void showAsDropDown(View anchor, int xoff, int yoff) {
        super.showAsDropDown(anchor, xoff, yoff);
        mImageViewSearch.clearAnimation();
        mImageViewSearch.startAnimation(animation);
        mLinkedSwitch.setChecked(MyApi.getInstance().getBtApi().isConnected());
        BleDevice bleDevice = MyApi.getInstance().getBtApi().getLastDevice();
        if (bleDevice != null && MyApi.getInstance().getBtApi().isConnected()) {
            setLinkDevice(bleDevice.getName(), bleDevice.getMac());

        } else {
            mLinkDeviceView.setVisibility(View.INVISIBLE);
        }
    }
    public void clearLinkDevice() {
        linkMac= null;
        linkName = null;
        if(mLinkNameView!=null && mLinkView!=null && mLinkDeviceView!=null) {
            mLinkNameView.setText("");
            mLinkView.setTag(null);
            mLinkDeviceView.setVisibility(View.INVISIBLE);
        }
    }

    public void setLinkDevice(String name,String mac){
        linkMac= mac;
        linkName = name;
        mLinkNameView.setText(name);
        mLinkView.setTag(linkMac);
        mLinkDeviceView.setVisibility(View.VISIBLE);
    }

    private List<String > bleDevices = new ArrayList<>();
    public void addDevice(String name,String mac) {
        if(TextUtils.isEmpty(mac) || bleDevices.contains(mac)){
            Log.w(TAG,"not add ble view");
            return;
        }
        bleDevices.add(0,mac);
        View contentView = LayoutInflater.from(getContentView().getContext()).inflate(R.layout.ble_device_item, null);
       //tv_name
        TextView textView = contentView.findViewById(R.id.tv_name);
        if (TextUtils.isEmpty(name)) {
            textView.setText(mac);
        } else {
            textView.setText(name);
        }
        contentView.setTag(mac);
        contentView.setOnClickListener(this);
        mViewGroup.addView(contentView,0);

    }


    public interface onButtonClickListener {
        void onClick(Object index);
    }

    public void setOnClickListener(onButtonClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    @Override
    public void onClick(View v) {
      //  int id = v.getId();
       // int ret = -1;

        if (onClickListener != null) {
            onClickListener.onClick(v.getTag());
        }
        dismiss();
    }
    public void dismiss() {
        super.dismiss();
        MyApi.getInstance().getBtApi().stopScan();
        mImageViewSearch.clearAnimation();
    }

}
