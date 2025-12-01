package com.zen.ui.fragment;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;


import com.zen.api.Constant;
import com.zen.api.DataApi;
import com.zen.api.MyApi;
import com.zen.api.data.DataBean;
import com.zen.api.event.BleDeviceConnectEvent;
import com.zen.api.event.UpdateEvent;
import com.zen.api.protocol.CalibrationCond;
import com.zen.api.protocol.CalibrationPh;
import com.zen.api.protocol.Convent;
import com.zen.api.protocol.Data;
import com.zen.api.protocol.Error;
import com.zen.api.protocol.Key;
import com.zen.ui.R;
import com.zen.ui.base.BaseFragment;

import com.zen.ui.view.HintsPopupWindow;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * Created by louis on 17-12-21.
 */
public class CalibrationFragment extends BaseFragment  {

    protected ImageView mViewImageAlert;
    protected ImageView mViewImageFlow3;
    protected TextView mTextViewWait;
    protected TextView mTextViewNum3;
    protected View mViewFlow;
    protected View mViewPh;
    protected View mViewORP;
    protected View mViewCOND;
    protected View mViewTDS;
    protected View mViewSAL;
    protected View mViewRES;

    protected View mViewFace;
    protected View mViewL;
    protected View mViewM;

    protected View mViewH;

    protected TextView mTextViewValue;

    protected TextView mTextViewUnit;

    protected TextView mTextViewValue2;

    protected ImageButton btEnter;
    protected ImageButton btBack;


    private int mode;
    private Handler mHandler = new Handler();


    private boolean mExiting=false;
    private HintsPopupWindow mHintsPopupWindow;
    private Error mCurrentErr;
    private String mValue2;

    public void onEnter() {
        try {
        MyApi.getInstance().getBtApi().sendCommand(new Key(Key.ENT));
        exit(3000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onBack() {
        try {
            MyApi.getInstance().getBtApi().sendCommand(new Key(Key.ESC));
            MyApi.getInstance().getDataApi().setLastData(null);
            exit(1000);
            if((!MyApi.getInstance().getBtApi().isConnected() )|| MyApi.getInstance().getBtApi().getLastDevice().isDemo()){
                getActivity().finish();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void exit(long delay){
        if(mExiting ) return;
        mExiting = true;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    DataApi dataApi = MyApi.getInstance().getDataApi();
                    DataBean bean = dataApi.getData();
                    Data data = bean.getData();
                    if (data!=null && data.isCalibration()) {
                        if(mExiting) mHandler.postDelayed(this,1000);
                        return;
                    }
                    getActivity().finish();
                    // mExiting = false;
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        },delay);
        startFlick(mTextViewValue);
        Log.i(getTAG(),"exit Calibration onBack");
    }

    private Data data = null;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(BleDeviceConnectEvent event) {
        try {
            if (!event.isConnected()) {
                getActivity().finish();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpdateEvent event) {
        try {

            Object object = event.getData();
            if (object instanceof Data) {

                data = (Data) object;

                if (data.getUnit2() != 0) {
                    if (mExiting) {
                        mTextViewValue.setText(mValue2);
                        return;
                    }
                    mExiting = true;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            getActivity().finish();

                        }
                    });
                    // startFlick(mTextViewValue);
                    Log.i(getTAG(),"exit Calibration");
                    return;
                }
                if(data.isLaughFace()){
                    mCurrentErr = null;
                }

            }else if(object instanceof  com.zen.api.protocol.Error){
                com.zen.api.protocol.Error error = (com.zen.api.protocol.Error) object;
                if(error.getErrCode()== Error.Err1){
                    mTextViewWait.setText(R.string.not_recognize);
                }else {
                    mTextViewWait.setText(error.getErrString());
                }
                mCurrentErr = error;
                //ToastUtils.showShort(error.getErrString());
                mViewImageAlert.setVisibility(View.VISIBLE);
                mExiting = false;
                mTextViewValue.clearAnimation();
                return;
            }/* else if (object instanceof CalibrationPh || object instanceof CalibrationCond) {
                getActivity().f();
                return;
            }*/
            if(!mExiting){
                updateView();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onClickReminder(){
        if(mViewImageAlert.getVisibility() ==View.VISIBLE) {
            if (mHintsPopupWindow == null) {
                mHintsPopupWindow = new HintsPopupWindow(getContext());

                mHintsPopupWindow.setHeight((int) (getHeight() * 0.8f));
                mHintsPopupWindow.setWidth((int) (getWidth() * 0.9f));
            }
            mHintsPopupWindow.setError(mCurrentErr);
            // mHintsPopupWindow.showAsDropDown(mViewBottom,0,0, Gravity.CENTER);
            if (mHintsPopupWindow.getError() != null) {
                mHintsPopupWindow.showAtLocation(getActivity().getWindow().getDecorView(), Gravity.CENTER, 0, 0);
            }
        }
    }

    private void startFlick( View view ){
        if( null == view ){
            return;
        }
        Animation alphaAnimation = new AlphaAnimation( 1, 0.0f );
        alphaAnimation.setDuration( 500 );
        alphaAnimation.setInterpolator( new AccelerateInterpolator( ) );
        alphaAnimation.setRepeatCount( Animation.INFINITE );
        alphaAnimation.setRepeatMode( Animation.REVERSE );
        view.startAnimation( alphaAnimation );
        mTextViewValue2.setVisibility(View.INVISIBLE);
        mTextViewValue.setText(mValue2);
    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calibration, container, false);
        mViewImageAlert = view.findViewById(R.id.tv_alert);
        mViewImageFlow3 = view.findViewById(R.id.iv_flow3);

        mTextViewWait = view.findViewById(R.id.tv_wait);
        mTextViewNum3 = view.findViewById(R.id.tv_num3);

        mViewFlow = view.findViewById(R.id.iv_flow);

        mViewPh = view.findViewById(R.id.iv_1);
        mViewORP = view.findViewById(R.id.iv_2);

        mViewCOND = view.findViewById(R.id.iv_3);
        mViewTDS = view.findViewById(R.id.iv_4);
        mViewSAL = view.findViewById(R.id.iv_5);

        mViewRES = view.findViewById(R.id.iv_6);
        mViewSAL = view.findViewById(R.id.iv_5);

        mViewFace = view.findViewById(R.id.iv_c_face);
        mViewL = view.findViewById(R.id.iv_c_l);

        mViewM = view.findViewById(R.id.iv_c_m);
        mViewH = view.findViewById(R.id.iv_c_h);

        mTextViewValue = view.findViewById(R.id.tv_value);
        mTextViewUnit = view.findViewById(R.id.tv_unit);

        mTextViewValue2 = view.findViewById(R.id.tv_value2);

        btEnter = view.findViewById(R.id.bt_enter);
        btBack = view.findViewById(R.id.bt_back);
        btEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onEnter();
            }
        });

        btBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBack();
            }
        });

        mTextViewWait.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickReminder();
            }
        });

        mViewImageAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickReminder();
            }
        });

        return view;
    }

    @Override
    public void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    public void updateView() {
        if(mExiting) return;
        super.updateView();
        if (data == null) {
            mViewPh.setVisibility(mode== Constant.MODE_PH ? View.VISIBLE : View.INVISIBLE);
            mViewORP.setVisibility(mode == Constant.MODE_ORP ? View.VISIBLE : View.INVISIBLE);
            mViewCOND.setVisibility(mode == Constant.MODE_COND ? View.VISIBLE : View.INVISIBLE);
            mViewTDS.setVisibility(mode == Constant.MODE_TDS ? View.VISIBLE : View.INVISIBLE);
            mViewSAL.setVisibility(mode == Constant.MODE_SAL ? View.VISIBLE : View.INVISIBLE);
            mViewRES.setVisibility(mode == Constant.MODE_RES ? View.VISIBLE : View.INVISIBLE);
            mViewFace.setVisibility(View.INVISIBLE);
            mViewL.setVisibility(View.INVISIBLE);
            mViewM.setVisibility(View.INVISIBLE);
            mViewH.setVisibility(View.INVISIBLE);
            mTextViewValue.setText(getFormatDouble((double) 0.0f,2));
            int u = 0;
            if(mode== Constant.MODE_PH){
                u=Data.UNIT_pH;
            }
            else if(mode== Constant.MODE_COND){
                u=Data.UNIT_mV;
            }
            mTextViewUnit.setText(getUnitString(u));
            mTextViewValue2.setText(getFormatDouble((double) 0.0f,2));
            mTextViewValue2.setVisibility(View.INVISIBLE);
            return;
        } else {
            mViewPh.setVisibility(data.getMode() == Data.pH ? View.VISIBLE : View.INVISIBLE);
            mViewORP.setVisibility(data.getMode() == Data.mV ? View.VISIBLE : View.INVISIBLE);
            mViewCOND.setVisibility(data.getMode() == Data.Cond ? View.VISIBLE : View.INVISIBLE);
            mViewTDS.setVisibility(data.getMode() == Data.TDS ? View.VISIBLE : View.INVISIBLE);
            mViewSAL.setVisibility(data.getMode() == Data.Salt ? View.VISIBLE : View.INVISIBLE);
            mViewRES.setVisibility(data.getMode() == Data.Res ? View.VISIBLE : View.INVISIBLE);
            mViewFace.setVisibility(data.isLaughFace() ? View.VISIBLE : View.INVISIBLE);
            mViewL.setVisibility(data.isL() ? View.VISIBLE : View.INVISIBLE);
            mViewM.setVisibility(data.isM() ? View.VISIBLE : View.INVISIBLE);
            mViewH.setVisibility(data.isH() ? View.VISIBLE : View.INVISIBLE);
            mTextViewValue.setText(getFormatDouble(data.getValue(), data.getPointDigit()));
            mTextViewUnit.setText(data.getUnitString());
            mTextViewValue2.setText(getFormatDouble(data.getValue2(), data.getPointDigit2()));
            mTextViewValue2.setVisibility(data.getValue2()==0.0f || mExiting?View.INVISIBLE:View.VISIBLE);
            if(data.getIntValue2()!=0.0){
                int flow3Id = R.mipmap.flow_3_0;
                int stringId =data.isLaughFace()?R.string.finish_cailbration: R.string.wait_readings;
                int stringNum3Id = R.string.select_buffer_solution;
                if (mode == Constant.MODE_PH) {
                    if (data.getIntValue2() == 700) {
                        mValue2 = getFormatDouble(data.getValue2(), data.getPointDigit2());
                        flow3Id = R.mipmap.flow_ph_u_3_1;
                        stringNum3Id = R.string.num3_ph7;
                    } else if (data.getIntValue2() == 400) {
                        mValue2 = getFormatDouble(data.getValue2(), data.getPointDigit2());
                        flow3Id = R.mipmap.flow_ph_u_3_21;
                        stringNum3Id = R.string.num3_ph4;
                    } else if (data.getIntValue2() == 1001) {
                        mValue2 = getFormatDouble(data.getValue2(), data.getPointDigit2());
                        flow3Id = R.mipmap.flow_ph_u_3_31;
                        stringNum3Id = R.string.num3_ph1001;
                    } else if (data.getIntValue2() == 168) {
                        mValue2 = getFormatDouble(data.getValue2(), data.getPointDigit2());
                        flow3Id = R.mipmap.flow_ph_n_3_22;
                        stringNum3Id = R.string.num3_ph168;
                    } else if (data.getIntValue2() == 1245|| data.getIntValue2() == 1246) {
                        mValue2 = getFormatDouble(data.getValue2(), data.getPointDigit2());
                        flow3Id = R.mipmap.flow_ph_n_3_32;
                        stringNum3Id = R.string.num3_ph1245;
                    } else if (data.getIntValue2() == 686) {
                        mValue2 = getFormatDouble(data.getValue2(), data.getPointDigit2());
                        flow3Id = R.mipmap.flow_ph_n_3_1;
                        stringNum3Id = R.string.num3_ph686;
                    } else if (data.getIntValue2() == 401) {
                        mValue2 = getFormatDouble(data.getValue2(), data.getPointDigit2());
                        flow3Id = R.mipmap.flow_ph_n_3_21;
                        stringNum3Id = R.string.num3_ph41;
                    }else if (data.getIntValue2() == 918) {
                        mValue2 = getFormatDouble(data.getValue2(), data.getPointDigit2());
                        flow3Id = R.mipmap.flow_ph_n_3_31;
                        stringNum3Id = R.string.num3_ph918;
                    }

                } else if (mode == Constant.MODE_COND) {
                    if (data.getIntValue2() == 1680) {
                        mValue2 = getFormatDouble(data.getValue2(), data.getPointDigit2());
                        flow3Id = R.mipmap.flow_3_4;
                        stringNum3Id = R.string.num3_168;
                    } else if (data.getIntValue2() == 1246) {
                        mValue2 = getFormatDouble(data.getValue2(), data.getPointDigit2());
                        flow3Id = R.mipmap.flow_3_5;
                        stringNum3Id = R.string.num3_1246;
                    } else if (data.getIntValue2() == 1413) {
                        mValue2 = getFormatDouble(data.getValue2(), data.getPointDigit2());
                        flow3Id = R.mipmap.flow_cond_3_1;
                        stringNum3Id = R.string.num3_1413;
                    } else if (data.getIntValue2() == 84|| data.getIntValue2() == 840) {
                        mValue2 = getFormatDouble(data.getValue2(), data.getPointDigit2());
                        flow3Id = R.mipmap.flow_cond_3_2;
                        stringNum3Id = R.string.num3_84;
                    } else if (data.getIntValue2() == 1288) {
                        mValue2 = getFormatDouble(data.getValue2(), data.getPointDigit2());
                        flow3Id = R.mipmap.flow_cond_3_3;
                        stringNum3Id = R.string.num3_1288;
                    }
                }
                mViewImageFlow3.setImageResource(flow3Id);
                if(mCurrentErr==null && data.isLaughFace()) {
                    mTextViewWait.setText(stringId);
                }
                mViewImageAlert.setVisibility(View.INVISIBLE);
                mTextViewNum3.setText(stringNum3Id);
            }else{
                mViewImageFlow3.setImageResource(R.mipmap.flow_3_0);
                mTextViewWait.setText(R.string.wait_readings);
                mTextViewNum3.setText(R.string.select_buffer_solution);
                mViewImageAlert.setVisibility(View.INVISIBLE);
            }
        }

    }





    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getMode() {
        return mode;
    }
}