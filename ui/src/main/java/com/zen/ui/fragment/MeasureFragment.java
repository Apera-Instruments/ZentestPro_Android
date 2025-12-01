package com.zen.ui.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.Process;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bigkoo.pickerview.OptionsPickerView;
import com.zen.api.Constant;
import com.zen.api.DataApi;
import com.zen.api.MyApi;
import com.zen.api.RestApi;
import com.zen.api.SettingConfig;
import com.zen.api.data.Calibration;
import com.zen.api.data.DataBean;
import com.zen.api.data.DeviceSetting;
import com.zen.api.data.Record;
import com.zen.api.event.BleDeviceConnectEvent;
import com.zen.api.event.SyncEventUpload;
import com.zen.api.protocol.CalibrationCond;
import com.zen.api.protocol.CalibrationPh;
import com.zen.api.protocol.Data;
import com.zen.api.protocol.Key;
import com.zen.api.protocol.Measure;
import com.zen.api.protocol.Mode;
import com.zen.api.protocol.ParmUp;
import com.zen.ui.CalibrationActivity;
import com.zen.ui.CategoryActivity;
import com.zen.ui.HomeActivity;
import com.zen.ui.NoteActivity;
import com.zen.ui.R;
import com.zen.ui.Setting2Activity;
import com.zen.ui.adapter.MeasureDataSimpleAdapter;
import com.zen.ui.base.BaseFragment;
import com.zen.ui.fragment.bean.TimeBean;
import com.zen.ui.view.HintsPopupWindow;
import com.zen.ui.view.ModePopupWindow;
import com.zen.ui.view.MyDashBoardView2;
import com.zen.ui.view.OnDoubleClickListener;
import com.zen.ui.view.SaveImageDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import im.dacer.androidcharts.LineView;


/**
 * Created by louis on 17-12-21.
 */
public class MeasureFragment extends BaseFragment implements View.OnClickListener, TextWatcher, Runnable {
    private static final String[] COL_HEADER = {"SN", "Date", "Time", "Value", "Temp.", ""};
    private LayoutInflater mInflater;
    private ViewPager viewPager;

    private ModePopupWindow mModePopupWindow;

    private int mModeType = Constant.MODE_PH;

    private int mGraphModeType = Constant.MODE_VELA_PH;

    private boolean mAutoSave = false;
    private boolean mAutoSaveRun = false;

    TextView mTextViewHoldBt;
    TextView mTextViewSetTimer;
    ImageButton buttonSave;
    TextView textViewSave;
    View mLayoutTimeSet;
    RadioGroup mRadioGroupSelect;
    View mViewFace;
    View mViewHold;
    View mViewAlarmDown;
    View mViewAlarmUp;
    ImageView mImageMeasure;
    View mLayoutMeasure;
    TextView mDanweiTextView;
    TextView mValueTextView;
    TextView mTempUnitTextView;
    TextView mTempTextView;
    TextView mRemindersTextView;
    View mLayoutHint;
    View mViewBottom;
//    View mModeButton;
    ImageView mTabImage1;
    ImageView mTabImage2;
    ImageView mTabImage3;
    ImageView mTabImage4;
    private double mCurrentValue;
    private LineView mLineView;
    private List<Float> mDataList1 = new LinkedList<>();
    private List<Float> mDataList2 = new LinkedList<>();
    private List<Float> mOrgDataList1 = new LinkedList<>();
    private List<Float> mOrgDataList2 = new LinkedList<>();
    private List<String> mBottomTextList = new LinkedList<>();
    private List<String> mOrgBottomTextList = new LinkedList<>();
    private double mCurrentTemp;
    private ImageView mImageViewPoint1;
    private ImageView mImageViewPoint2;
    private ImageView mImageViewPoint3;
    private boolean mNewMode = true;
    private TextView mTextViewOffset;
    private TextView mTextViewSlop;
    private TextView mTextViewCalDate;
    private float mZeroAngle;
    private int mTempUnit = 0;
    private ViewGroup mViewGroupSlop;
    private ViewGroup mViewGroupZeroPoint;
    private ViewGroup mViewGroupPoints;
    private TextView lineLeftTextView;
    private TextView lineRightTextView;

    private TextView mTextViewTempRef;
    private ViewGroup mViewGroupTempRef;

    private TextView mTextViewTempCoeffc;
    private ViewGroup mViewGroupTempCoeffc;

    private TextView mTextViewSalinity;
    private ViewGroup mViewGroupSalinity;

    private TextView mTextViewTDS;
    private ViewGroup mViewGroupTDS;

    private Animation mBitmapAnimation;
    private TextView mTextViewDialUnit;
    private ListView mDataListView;
    private MeasureDataSimpleAdapter mMeasureDataSimpleAdapter;
    private long mTimer = 1000 * 10;
    private OptionsPickerView pvOptions;
    private SharedPreferences mSharedPreferences;
    private boolean mAlarmDirty = false;
    private boolean mAlarmL = false;
    private boolean mAlarmH = false;
    private String mTraceNo = null;
    private int mSn = 0;

    private int PERMISSION_REQUEST_CODE = 100;

    private HintsPopupWindow mHintsPopupWindow;
    private PowerManager.WakeLock wakeLock;
    private Random random = new Random();
    private AtomicInteger atomicInteger = new AtomicInteger(0);
    private String mCurrentUnit;
    private double lowValue;
    private int pHSelect = ParmUp.USA;
    private ViewGroup mViewGroupLastDate;
    private boolean mUpdateViewGraph3 = false;
    private String mTempUnitStr;

    ImageView imageViewbg;
    private int Reid = 0;
    private int NotiTime = 60;
    private int alarmid = 0;
    private int alarmbtid = 0;
    ImageButton imageButtonAlarm;

    RelativeLayout relativeLayoutdia;
    //mHintsPopupWindow = new HintsPopupWindow(this);

    SaveImageDialog saveDialogFragment;

    public void onEnter() {
        MyApi.getInstance().getBtApi().sendCommand(new Key(Key.HOLD));
    }

    public void onClickReminder() {
        if (mHintsPopupWindow == null) {
            mHintsPopupWindow = new HintsPopupWindow(getContext());

            mHintsPopupWindow.setHeight((int) (getHeight() * 0.8f));
            mHintsPopupWindow.setWidth((int) (getWidth() * 0.9f));
        }
        mHintsPopupWindow.setError(MyApi.getInstance().getDataApi().getReminderError());
        // mHintsPopupWindow.showAsDropDown(mViewBottom,0,0, Gravity.CENTER);
        if (mHintsPopupWindow.getError() != null) {
            mHintsPopupWindow.showAtLocation(getActivity().getWindow().getDecorView(), Gravity.CENTER, 0, 0);
        }
    }

    public void onMode() {
        if (mModePopupWindow == null) {
            mModePopupWindow = new ModePopupWindow(this.getContext());
            mModePopupWindow.updateButtonView();

            mModePopupWindow.setOnClickListener(new ModePopupWindow.onButtonClickListener() {
                @Override
                public void onClick(int index) {
                    if (mModeType != index) {
                        clearHistory();
                    }
                    mModeType = index;
                    mNewMode = true;
                    Mode mode = new Mode();
                    mode.setMode(ConventMode(mModeType));
                    MyApi.getInstance().getBtApi().sendCommand(mode);

                    updateView();
                }
            });
        }
//        mModePopupWindow.setButtonEnable(mModeType);
//        mModePopupWindow.showAsDropDown(mModeButton);
    }

    private void clearHistory() {
        //    mCellList2.clear();
        mLineView.setHorizontalGridNum(mLineDateSize);
     /*   mCellList = new ArrayList<>();
        for (int i = 0; i < ROW_SIZE; i++) {
            mCellList.add(new ArrayList<Cell>());
        }*/
        mOrgDataList1.clear();
        mOrgDataList2.clear();
        mDataList1.clear();
        mDataList2.clear();
        mBottomTextList.clear();
        mOrgBottomTextList.clear();
        mMeasureDataSimpleAdapter.getData().clear();
        mAlarmH = false;
        mAlarmL = false;
        mAlarmDirty = true;
    }

    private int ConventMode(int type) {
        switch (type) {
            case Constant.MODE_COND:
                return Mode.Cond;
            case Constant.MODE_PH:
                return Mode.pH;
            case Constant.MODE_SAL:
                return Mode.Salt;
            case Constant.MODE_ORP:
                return Mode.mV;
            case Constant.MODE_TDS:
                return Mode.TDS;
            case Constant.MODE_RES:
                return Mode.Res;
        }
        return 0;
    }

    public void closeWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
            wakeLock = null;
        }
    }

    public void openWakeLock() {
        try {
            if (wakeLock != null && wakeLock.isHeld()) return;
            PowerManager powerManager = (PowerManager) getContext().getSystemService(Service.POWER_SERVICE);

            if (powerManager != null) {
                wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Lock");
                //是否需计算锁的数量
                wakeLock.setReferenceCounted(false);
                //请求常亮，onResume()
                wakeLock.acquire();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onMenu() {
        ((HomeActivity) getActivity()).showMenu();
    }

    public void onSave() {
        if (!mAutoSave) {
            if (true) { // checkPer(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                // 已授权读取外部存储权限，可以读取相册中的图片
                mTraceNo = getTraceString(new Date()) + Integer.toHexString((this.hashCode() * 100 + random.nextInt(100)) * 100 + atomicInteger.addAndGet(1));
                //Integer.toHexString(hashCode()) + java.lang.Long.toHexString(System.currentTimeMillis()) + Integer.toHexString(random.nextInt(1000)) + Integer.toHexString(atomicInteger.addAndGet(1)
                saveRecordData();
                saveView();
                showDialog();
                EventBus.getDefault().post(new SyncEventUpload());

                mTraceNo = null;
            } else {
//                // 未授权读取外部存储权限，需要请求授权
//                HomeActivity h = (HomeActivity) getActivity();
//                h.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                return;
            }
        } else {
            if (mAutoSaveRun) {
                MyApi.getInstance().getBtApi().sendCommand(new Measure(Measure.OFF));
                closeWakeLock();
                saveView();

                showDialog();
                EventBus.getDefault().post(new SyncEventUpload());
                mAutoSaveRun = false;
                buttonSave.setImageDrawable(getResources().getDrawable(R.drawable.btn_save));
                mTraceNo = null;
            } else {
                if (true) {
                    //checkPer(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    // 已授权读取外部存储权限，可以读取相册中的图片
                    mAutoSaveRun = true;
                    mTraceNo = getTraceString(new Date()) + Integer.toHexString(this.hashCode());
                    mSn = 0;
                    clearHistory();
                    MyApi.getInstance().getBtApi().sendCommand(new Measure(Measure.ContinuousMeasureON));
                    openWakeLock();
                    buttonSave.setImageDrawable(getResources().getDrawable(R.drawable.btn_auto_save));
                    //run();
                    mHandler.postDelayed(this, 1 * 1000);

                    EventBus.getDefault().post(new SyncEventUpload());
                } else {
//                    // 未授权读取外部存储权限，需要请求授权
//                    HomeActivity h = (HomeActivity) getActivity();
//                    h.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
//                    return;
                }
//                if (ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//                    // 已授权读取外部存储权限，可以读取相册中的图片
//                    mAutoSaveRun = true;
//                    mTraceNo = getTraceString(new Date())+Integer.toHexString(this.hashCode());
//                    mSn = 0;
//                    clearHistory();
//                    MyApi.getInstance().getBtApi().sendCommand(new Measure(Measure.ContinuousMeasureON));
//                    openWakeLock();
//                    buttonSave.setImageDrawable(getResources().getDrawable(R.drawable.btn_auto_save));
//                    //run();
//                    mHandler.postDelayed(this,1*1000);
//
//                    EventBus.getDefault().post(new SyncEventUpload());
//                } else {
//                    // 未授权读取外部存储权限，需要请求授权
//                    ActivityCompat.requestPermissions(getActivity(), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), GALLERY_PERMISSION_REQUEST_CODE);
//                }
            }
        }
    }

    private boolean checkPer(String permission) {
        return getContext().checkPermission(permission, Process.myPid(), Process.myUid()) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean isAutoSave() {
        return mAutoSaveRun && mAutoSave;
    }

    private Handler mHandler = new Handler();

    private void startTimer() {
        if (mAutoSaveRun) mHandler.postDelayed(this, mTimer);
    }

    //需要改的地方
    private void showDialog() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(SaveImageDialog.DIALOG_BACK, true);
        bundle.putBoolean(SaveImageDialog.DIALOG_CANCELABLE_TOUCH_OUT_SIDE, true);

//        bundle.putString("DataId",DataId);
        saveDialogFragment = SaveImageDialog.newInstance(SaveImageDialog.class, bundle);
        saveDialogFragment.show(getFragmentManager(), SaveImageDialog.class.getName());
        saveDialogFragment.setOnConfirmClcik(new SaveImageDialog.IonConfirmClick() {
            @Override
            public void onClcik(int type) {

                Record record = MyApi.getInstance().getDataApi().getRecords().get(0);
                long ID = record.getId();

                if (type == 1) {

                }

                if (type == 2) {
//                    saveDialogFragment.dismiss();
                }

                if (type == 3) {
                    CategoryActivity.showMe(getContext(), ID);
                }

                if (type == 4) {
                    String id = MyApi.getInstance().getDataApi().setDelRecord(ID);
                    int ret = MyApi.getInstance().getRestApi().dataDelete(id, record.getType());
                    if (ret == RestApi.SUCCESS) {
                        MyApi.getInstance().getDataApi().delRecord(ID);
                    }
                    saveDialogFragment.dismiss();
                }

                if (type == 5) {
                    NoteActivity.Companion.showMe(getContext(), ID);
                }

                if (type == 6) {
                    saveDialogFragment.dismiss();
                }
            }

            @Override
            public void onClickStr(String string) {
            }
        });
    }

    private void saveView() {
        mLayoutMeasure.setDrawingCacheEnabled(true);
        Bitmap bitmap = saveBitmap(mLayoutMeasure.getDrawingCache());

        mLayoutMeasure.setDrawingCacheEnabled(false);
        mImageMeasure.setImageBitmap(bitmap);
        mImageMeasure.setVisibility(View.VISIBLE);
        mImageMeasure.startAnimation(mBitmapAnimation);
    }

    public void onSetTimer() {
     /*   pvOptions = new  OptionsPickerView.Builder(getContext(), new OptionsPickerView.OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int option2, int options3 ,View v) {
             *//*   //返回的分别是三个级别的选中位置
                String tx = options1Items.get(options1).getPickerViewText()
                        + options2Items.get(options1).get(option2)
                        + options3Items.get(options1).get(option2).get(options3).getPickerViewText();
                tvOptions.setText(tx);*//*
            }
        })
                .setSubmitText(getString(R.string.save))//确定按钮文字
                //.setCancelText("取消")//取消按钮文字
                .setTitleText(getString(R.string.timer_set))//标题
                .setSubCalSize(18)//确定和取消文字大小
                .setTitleSize(20)//标题文字大小
                .setTitleColor(Color.BLACK)//标题文字颜色
                .setSubmitColor(Color.BLUE)//确定按钮文字颜色
               // .setCancelColor(Color.BLUE)//取消按钮文字颜色
                .setTitleBgColor(0xFF333333)//标题背景颜色 Night mode
                .setBgColor(0xFF000000)//滚轮背景颜色 Night mode
                .setContentTextSize(18)//滚轮文字大小
                .setLinkage(false)//设置是否联动，默认true
                //.setLabels("省", "市", "区")//设置选择的三级单位
                .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
                .setCyclic(false, false, false)//循环与否
                .setSelectOptions(1, 1, 1)  //设置默认选中项
                .setOutSideCancelable(false)//点击外部dismiss default true
                .isDialog(true)//是否显示为对话框样式
                .build();

        List<String> options1Items=new ArrayList();
        options1Items.add("11");
        List<String> options2Items1=new ArrayList();
        List<String> options2Items2=new ArrayList();
        options2Items1.add("1");
        options2Items2.add("2");
        List<List<String>> options2Items=new ArrayList<>();
        options2Items.add(options2Items1);
        options2Items.add(options2Items1);
        pvOptions.setPicker(options1Items, options2Items);//添加数据源*/
        if (pvOptions == null) {
            initOptionData();
            initOptionPicker();
        }
        pvOptions.show();
    }

    private ArrayList<TimeBean> options1Items = new ArrayList<>();

    private ArrayList<ArrayList<String>> options2Items = new ArrayList<>();

    private void initOptionData() {

        //选项2
        ArrayList<String> options2Items_01 = new ArrayList<>();
        options2Items_01.add(getString(R.string.seconds));
        options2Items_01.add(getString(R.string.minutes));
        //选项1
        for (int i = 0; i < 60; i++) {
            options1Items.add(new TimeBean(i, "" + (i + 1), (i + 1)));
            options2Items.add(options2Items_01);
        }

     /*   options2Items.add(options2Items_02);
        options2Items.add(options2Items_03);
*/
        /*--------数据源添加完毕---------*/
    }


    private void initOptionPicker() {//条件选择器初始化

        /**
         * 注意 ：如果是三级联动的数据(省市区等)，请参照 JsonDataActivity 类里面的写法。
         */
        int opt2 = mTimer > 60 * 1000 ? 1 : 0;
        int opt1 = (int) (mTimer / (1000 * (opt2 == 1 ? 60 : 1))) - 1;
        if (opt1 >= options1Items.size()) opt1 = options1Items.size() - 1;
        pvOptions = new OptionsPickerView.Builder(getContext(), new OptionsPickerView.OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int options2, int options3, View v) {
                //返回的分别是三个级别的选中位置
                String tx = options1Items.get(options1).getPickerViewText()
                        + options2Items.get(options1).get(options2)
                        /* + options3Items.get(options1).get(options2).get(options3).getPickerViewText()*/;
                //btn_Options.setText(tx);

                mTimer = 1000 * options1Items.get(options1).getValue() * (options2 == 1 ? 60 : 1);
                mSharedPreferences.edit().putLong("mTimer", mTimer).apply();

                updateSetTimer();
                Log.i(getTAG(), tx + " mTimer=" + mTimer);

            }
        })

                .setTitleText(getString(R.string.timer_set))
                .setContentTextSize(20)//设置滚轮文字大小
                .setDividerColor(Color.LTGRAY)//设置分割线的颜色
                .setSelectOptions(opt1, opt2)//默认选中项
                .setBgColor(Color.WHITE)
                .setTitleBgColor(Color.WHITE)
                .setTitleColor(Color.BLACK)
                .setCancelColor(Color.WHITE)
                .setSubmitColor(getResources().getColor(R.color.colorPrimary))
                .setTextColorCenter(Color.LTGRAY)
                .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
                .setLabels("", "", "")
                .setBackgroundId(0x66000000) //设置外部遮罩颜色
                .build();

        //pvOptions.setSelectOptions(1,1);
        /*pvOptions.setPicker(options1Items);//一级选择器*/
        pvOptions.setPicker(options1Items, options2Items);//二级选择器
        /*pvOptions.setPicker(options1Items, options2Items,options3Items);//三级选择器*/
    }

    TextView mTitleView;
    ImageView mBtView;

    public void btConnect() {
        Activity activity = getActivity();
        if (activity != null && activity instanceof HomeActivity) {
            ((HomeActivity) activity).btConnect();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(BleDeviceConnectEvent event) {
        if (event.isConnected()) {
            mBtView.setImageResource(R.mipmap.bt_linked);
            loadAlarm();
        } else {
            mBtView.setImageResource(R.mipmap.bt_normal);
            mModePopupWindow = null;
        }
    }

    public void onResume() {
        super.onResume();
        mNewMode = true;
        loadAlarm();
    }

    private Map<String, Double> alarmLMap = new HashMap<>();
    private Map<String, Double> alarmHMap = new HashMap<>();
    private Map<String, Boolean> alarmLSwitchMap = new HashMap<>();
    private Map<String, Boolean> alarmHSwitchMap = new HashMap<>();
    private String mhighValueUnit;
    private String mlowValueUnit;

    private void loadAlarm() {
      /*  BleDevice bleDevice = MyApi.getInstance().getBtApi().getLastDevice();
        if(bleDevice==null) return;*/
        SharedPreferences sharedPreferences = MyApi.getInstance().getDataApi().getSetting();
        String ph_selection = sharedPreferences.getString(SettingConfig.ph_selection_SAVE_ID, "USA");
        if ("USA".equals(ph_selection)) {
            pHSelect = ParmUp.USA;
        } else {
            pHSelect = ParmUp.NIST;
        }

        int i = 0;

        for (String settingIndex : Constant.ALARMs) {
            String str = sharedPreferences.getString(settingIndex, Constant.DEFAULT_ALARMs[i]);
            DeviceSetting.Param param = JSON.parseObject(str, DeviceSetting.Param.class);//bleDevice.getSetting().getParam(settingIndex);

            String lowValue = (param.getString(Constant.lowValue));

            boolean lowSwitchValue = (Constant.ON.equals(param.getString(Constant.lowSwitchValue)));
            String highValue = (param.getString(Constant.highValue));

            boolean highSwitchValue = (Constant.ON.equals(param.getString(Constant.highSwitchValue)));
            try {
                alarmLSwitchMap.put(settingIndex, lowSwitchValue);
                alarmLMap.put(settingIndex, lowSwitchValue ? Double.parseDouble(lowValue) : 0);
            } catch (NumberFormatException e) {
                alarmLMap.put(settingIndex, 0.0);
            }
            try {
                alarmHSwitchMap.put(settingIndex, highSwitchValue);
                alarmHMap.put(settingIndex, highSwitchValue ? Double.parseDouble(highValue) : 0);

            } catch (NumberFormatException e) {
                alarmHMap.put(settingIndex, 0.0);
            }
            i++;

        }
    }

    private Bitmap saveBitmap(Bitmap bitmap) {
        File fileDir = getContext().getExternalFilesDir("save");
        Log.i(getTAG(), "saveBitmap success " + fileDir.getPath());
        File f = new File(fileDir, mTraceNo + Long.toHexString(System.currentTimeMillis()) + ".png");
        if (f.exists()) {
            f.delete();
        }
        Bitmap outBitmap = Bitmap.createBitmap(bitmap);
        try {
            FileOutputStream out = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
            Log.i(getTAG(), "saveBitmap success " + f.getPath());
            Record record = new Record();
            record.setTraceNo(mTraceNo);

            record.setTempValue(mTempTextView.getText().toString());
            record.setPotential(MyApi.getInstance().getBtApi().getLastDevice().getModle());
            record.setOperator("Admin");
            record.setCategory("Default");

            record.setSlop(mTextViewSlop.getText().toString());
            record.setPic(f.getPath());
            record.setType(mModeType);
            record.setTabType(viewPager.getCurrentItem());
            record.setValue(mCurrentValue);
            String unit = mDanweiTextView.getText().toString();
            record.setValueUnit(unit);

            record.setUserId(MyApi.getInstance().getRestApi().getUserId());
            record.setCreateTime(System.currentTimeMillis());
            try {
                record.setDeviceNumber(MyApi.getInstance().getBtApi().getDeviceNumber());
                record.setLocation(MyApi.getInstance().getDataApi().getLocation());
                record.setTempUnit(mTempUnitStr);//mTempUnitTextView.getText().toString());
                if (MyApi.getInstance().getBtApi().getLastDevice() != null && MyApi.getInstance().getBtApi().getLastDevice().getSetting() != null) {
                    if (mModeType == Constant.MODE_PH) {
                        if (MyApi.getInstance().getBtApi().getLastDevice().getSetting().getCalibrationPh() != null)
                            record.setCalibration(MyApi.getInstance().getBtApi().getLastDevice().getSetting().getCalibrationPh().toString());
                    } else if (mModeType == Constant.MODE_COND) {
                        if (MyApi.getInstance().getBtApi().getLastDevice().getSetting().getCalibrationCond() != null)
                            record.setCalibration(MyApi.getInstance().getBtApi().getLastDevice().getSetting().getCalibrationCond().toString());
                    }
                }

            } catch (Exception e) {

            }
            MyApi.getInstance().getDataApi().saveRecord(record);
            log("no log " + JSON.toJSONString(MyApi.getInstance().getDataApi().getRecords().get(0)));
            log("no log " + MyApi.getInstance().getDataApi().getRecords().get(0));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return outBitmap;
    }

    private void log(String msg) {

    }


    private void updateTableView() {
//        mTabImage1.setEnabled(0 != viewPager.getCurrentItem());
//        mTabImage2.setEnabled(1 != viewPager.getCurrentItem());
//        mTabImage3.setEnabled(2 != viewPager.getCurrentItem());
//        mTabImage4.setEnabled(3 != viewPager.getCurrentItem());

        mRadioGroupSelect.check(getRadioButtonId(viewPager.getCurrentItem()));

        if (viewPager.getCurrentItem() <= 1) {
            mLayoutTimeSet.setVisibility(View.INVISIBLE);
            buttonSave.setImageDrawable(getResources().getDrawable(R.drawable.btn_save));
            textViewSave.setText(R.string.save);
            mAutoSave = false;
        } else {
            mAutoSave = true;
            mLayoutTimeSet.setVisibility(View.VISIBLE);
            textViewSave.setText(R.string.auto_save);
            buttonSave.setImageDrawable(getResources().getDrawable(mAutoSaveRun ? R.drawable.btn_auto_save : R.drawable.btn_save));
        }
    }

    private int getRadioButtonId(int currentItem) {
        switch (currentItem) {
            case 0:
                return R.id.rb_1;
            case 1:
                return R.id.rb_2;
            case 2:
                return R.id.rb_3;
//            case 3:
//                return R.id.rb_4;

        }
        return 0;
    }

    @Override
    public void onDestroy() {
        closeWakeLock();
        super.onDestroy();

    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedPreferences = getContext().getSharedPreferences(
                this.getClass().getSimpleName(), Context.MODE_PRIVATE);
        mTimer = mSharedPreferences.getLong("mTimer", mTimer);
        //新增
//        manager = (NotificationManager)getContext().getSystemService(getContext().NOTIFICATION_SERVICE);

        mMyDashBoardView2 = new MyDashBoardView2(getContext());
        if (isAutoSave()) {
            openWakeLock();
        }
    }

    private void updateSetTimer() {
        if (mTimer >= 60 * 1000) {
            mTextViewSetTimer.setText(String.format("%dM", mTimer / (60 * 1000)));
        } else {
            mTextViewSetTimer.setText(String.format("%dS", mTimer / (1000)));
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.ib_hold) {
            onEnter();
        } else if (id == R.id.layout_hint) {
            onClickReminder();
//        } else if (id == R.id.bt_mode) {
//            onMode();
//        } else if (id == R.id.tab_1) {
//            viewPager.setCurrentItem(0);
//        } else if (id == R.id.tab_2) {
//            viewPager.setCurrentItem(1);
//        } else if (id == R.id.tab_3) {
//            viewPager.setCurrentItem(2);
//        } else if (id == R.id.tab_4) {
//            viewPager.setCurrentItem(3);
        } else if (id == R.id.bt_blue_connect) {
            btConnect();
        } else if (id == R.id.iv_menu) {
            onMenu();
        } else if (id == R.id.bt_save) {
            onSave();
        } else if (id == R.id.bt_set_timer) {
            onSetTimer();
        }


    }

    private ImageButton ib_hold, bt_set_timer;
    private RelativeLayout tab_1, tab_2, tab_3, tab_4, iv_menu;

    private TextView tv_value_ph, tv_danwei1_ph;
    private TextView tv_value_cond, tv_danwei1_cond;
    private TextView tv_value_orp, tv_danwei1_orp;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_measure, container, false);

        mTextViewHoldBt = view.findViewById(R.id.tv_hold_bt);
        mTextViewSetTimer = view.findViewById(R.id.tv_set_timer);
        buttonSave = view.findViewById(R.id.bt_save);
        textViewSave = view.findViewById(R.id.tv_save);
        mLayoutTimeSet = view.findViewById(R.id.layout_time_set);
        mRadioGroupSelect = view.findViewById(R.id.rg_select);
//        mViewFace = view.findViewById(R.id.iv_face);
//        mViewHold = view.findViewById(R.id.tv_hold);
//        mViewAlarmDown = view.findViewById(R.id.iv_updown_down);
//        mViewAlarmUp = view.findViewById(R.id.iv_updown_up);
        mImageMeasure = view.findViewById(R.id.iv_layout_measure);
        mLayoutMeasure = view.findViewById(R.id.layout_measure);
//        mDanweiTextView = view.findViewById(R.id.tv_danwei1);
//        mValueTextView = view.findViewById(R.id.tv_value);

        tv_value_ph = view.findViewById(R.id.tv_value_ph);
        tv_danwei1_ph = view.findViewById(R.id.tv_danwei1_ph);

        tv_value_cond = view.findViewById(R.id.tv_value_cond);
        tv_danwei1_cond = view.findViewById(R.id.tv_danwei1_cond);

        tv_value_orp = view.findViewById(R.id.tv_value_orp);
        tv_danwei1_orp = view.findViewById(R.id.tv_danwei1_orp);

        tv_value_ph.setOnClickListener(v -> {
            mGraphModeType = Constant.MODE_VELA_PH;
            clearHistory();
            updateViewGraph();
            updateViewGraph3Clear();
        });

        tv_value_cond.setOnClickListener(v -> {
            mGraphModeType = Constant.MODE_VELA_COND;
            clearHistory();
            updateViewGraph();
            updateViewGraph3Clear();
        });

        tv_value_orp.setOnClickListener(v -> {
            mGraphModeType = Constant.MODE_VELA_ORP;
            clearHistory();
            updateViewGraph();
            updateViewGraph3Clear();
        });

        mTempUnitTextView = view.findViewById(R.id.tv_danwei2);
        mTempTextView = view.findViewById(R.id.tv_temp);
        mRemindersTextView = view.findViewById(R.id.tv_reminders);
        mLayoutHint = view.findViewById(R.id.layout_hint);
//        mViewBottom = view.findViewById(R.id.layout_bottom);
//        mModeButton = view.findViewById(R.id.bt_mode);
//        mTabImage1 = view.findViewById(R.id.iv_tab_1);
//        mTabImage2 = view.findViewById(R.id.iv_tab_2);
//        mTabImage3 = view.findViewById(R.id.iv_tab_3);
//        mTabImage4 = view.findViewById(R.id.iv_tab_4);
        mTitleView = view.findViewById(R.id.tv_title);
        mBtView = view.findViewById(R.id.bt_blue_connect);

//        tab_1 = view.findViewById(R.id.tab_1);
//        tab_1.setOnClickListener(this);
//        tab_2 = view.findViewById(R.id.tab_2);
//        tab_2.setOnClickListener(this);
//        tab_3 = view.findViewById(R.id.tab_3);
//        tab_3.setOnClickListener(this);
//        tab_4 = view.findViewById(R.id.tab_4);
//        tab_4.setOnClickListener(this);

        mBtView.setOnClickListener(this);

        iv_menu = view.findViewById(R.id.iv_menu);
        iv_menu.setOnClickListener(this);
        buttonSave.setOnClickListener(this);

        bt_set_timer = view.findViewById(R.id.bt_set_timer);
        bt_set_timer.setOnClickListener(this);

        ib_hold = view.findViewById(R.id.ib_hold);
        ib_hold.setOnClickListener(this);
        mLayoutHint.setOnClickListener(this);
//        mModeButton.setOnClickListener(this);

//        relativeLayoutdia = view.findViewById(R.id.alarm_dia);
//        relativeLayoutdia.setVisibility(View.INVISIBLE);

        mInflater = inflater;
        initViewPager(view);
        imageViewbg = view.findViewById(R.id.home_bg_imag);
//        imageButtonAlarm = view.findViewById(R.id.bt_alarm);
//        imageButtonAlarm.setOnClickListener(new View.OnClickListener() {
//                                                @Override
//                                                public void onClick(View view) {
//                                                    relativeLayoutdia.setVisibility(View.VISIBLE);
//                                                    alarmbtid = 1;
//                                                }
//                                            }
//        );

        int w = getWidth();
        ViewGroup.LayoutParams params = mImageViewGraph.getLayoutParams();
        params.width = w;
        params.height = w;
        mImageViewGraph.setLayoutParams(params);
        mModePopupWindow = null;
        updateTableView();
        mBitmapAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.bitmap_save);
        mBitmapAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mImageMeasure.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        //
        updateSetTimer();
        return view;
    }

    private LayoutInflater getMyLayoutInflater() {
        if (Build.VERSION.SDK_INT >= 26) {
            return super.getLayoutInflater();
        } else {
            return mInflater;
        }
    }


    private void initViewPager(View rootView) {
        viewPager = (ViewPager) rootView.findViewById(R.id.viewpager);
        // Set ViewPager height to 50% of screen height
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenHeight = displayMetrics.heightPixels;

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) viewPager.getLayoutParams();
        params.height = screenHeight / 2;
        viewPager.setLayoutParams(params);
        viewPager.requestLayout();

        LayoutInflater inflater = getMyLayoutInflater();
        View view1 = inflater.inflate(R.layout.viewpager_measure_1, null);
        View view2 = inflater.inflate(R.layout.viewpager_measure_2, null);
        View view3 = inflater.inflate(R.layout.viewpager_measure_3, null);
        ViewGroup view4 = (ViewGroup) inflater.inflate(R.layout.viewpager_measure_4, null);
        initView1(view1);
        initView2(view2);
        initView3(view3);
        initView4(view4);

        final List<View> viewList = new ArrayList<View>();// 将要分页显示的View装入数组中
        viewList.add(view1);
//        viewList.add(view2);
        viewList.add(view3);
        viewList.add(view4);

        PagerAdapter pagerAdapter = new PagerAdapter() {

            @Override
            public boolean isViewFromObject(View arg0, Object arg1) {

                return arg0 == arg1;
            }

            @Override
            public int getCount() {

                return viewList.size();
            }

            @Override
            public void destroyItem(ViewGroup container, int position,
                                    Object object) {

                container.removeView(viewList.get(position));
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                container.addView(viewList.get(position));
                return viewList.get(position);
            }
        };

        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                updateTableView();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    private void initView1(View view) {
        mImageViewPoint1 = (ImageView) view.findViewById(R.id.iv_point1);
        mImageViewPoint2 = (ImageView) view.findViewById(R.id.iv_point2);
        mImageViewPoint3 = (ImageView) view.findViewById(R.id.iv_point3);
        mTextViewOffset = (TextView) view.findViewById(R.id.tv_offset1);
        mTextViewSlop = (TextView) view.findViewById(R.id.tv_slop1);
        mTextViewCalDate = (TextView) view.findViewById(R.id.tv_cal_date);
        mViewGroupLastDate = (ViewGroup) view.findViewById(R.id.layout_last_cailb);
        mViewGroupSlop = (ViewGroup) view.findViewById(R.id.layout_slope);
        mViewGroupZeroPoint = (ViewGroup) view.findViewById(R.id.layout_zero_point);
        mViewGroupPoints = (ViewGroup) view.findViewById(R.id.layout_points);

        mTextViewTempRef = (TextView) view.findViewById(R.id.tv_temp_ref);
        mViewGroupTempRef = (ViewGroup) view.findViewById(R.id.layout_temp_ref);

        mTextViewTempCoeffc = (TextView) view.findViewById(R.id.tv_temp_coeffc);
        mViewGroupTempCoeffc = (ViewGroup) view.findViewById(R.id.layout_temp_coeff);

        mTextViewSalinity = (TextView) view.findViewById(R.id.tv_salinity);
        mViewGroupSalinity = (ViewGroup) view.findViewById(R.id.layout_salinity);

        mTextViewTDS = (TextView) view.findViewById(R.id.tv_tds);
        mViewGroupTDS = (ViewGroup) view.findViewById(R.id.layout_tds);
    }

    private MyDashBoardView2 mMyDashBoardView2;
    private ImageView mImageViewGraph;
    private ImageView mImageViewDial;
    private double fullValue = 0.00f;
    private int mSweepAngle = 160;//270;

    private void setDialValue(double value) {
        if (fullValue > 0) {
            if (value < lowValue) {
                mImageViewDial.setRotation((float) (mSweepAngle * lowValue / fullValue - (mSweepAngle / 2) + mZeroAngle));
            } else if (value > fullValue) {
                mImageViewDial.setRotation((float) (mSweepAngle * fullValue / fullValue - (mSweepAngle / 2) + mZeroAngle));
            } else {
                mImageViewDial.setRotation((float) (mSweepAngle * value / fullValue - (mSweepAngle / 2) + mZeroAngle));
            }
        }
    }

    private double getAllAlarmValue(Map<String, Double> map, String key) {
        try {
            double v = map.get(key);
            return v;
            // fullValue = 100;
            //  mSweepAngle = 200;
//            if (v > fullValue) {
//                return (int) (mSweepAngle * fullValue / fullValue);
//            } else if (v < lowValue) {
//                return (int) (mSweepAngle * lowValue / fullValue);
//            } else return (int) (mSweepAngle * v / fullValue);
        } catch (Exception e) {
            return 0;
        }
    }

    private double getAlarmValue(Map<String, Double> map, String key) {
        try {
            Double v = map.get(key);

            Log.i("getAlarmValue", "getAlarmValue = " + mhighValueUnit);

            if (mhighValueUnit.equals("ppm") || mlowValueUnit.equals("ppm")) {
                if ("ppm".equals(getUnit())) {
                    return v / 5;
                } else {
                    return v / 50;
                }
            }

            if (mhighValueUnit.equals("ppt") || mlowValueUnit.equals("ppt")) {
                if ("ppm".equals(getUnit())) {
                    return v * 200;
                } else {
                    return v * 20;
                }
            }

            if (mhighValueUnit.equals("µS/cm") || mlowValueUnit.equals("µS/cm")) {
                if ("mS".equals(getUnit())) {
                    Log.v(getTAG(), "getAlarmValue " + getUnit());
                    return v / 100;
                } else if ("µS".equals(getUnit())) {
                    Log.v(getTAG(), "getAlarmValue " + getUnit());
                    return v;
                }
            }
            if (mhighValueUnit.equals("mS/cm") || mlowValueUnit.equals("mS/cm")) {
                if ("mS".equals(getUnit())) {
                    Log.v(getTAG(), "getAlarmValue " + getUnit());
                    return v * 10;
                } else if ("µS".equals(getUnit())) {
                    Log.v(getTAG(), "getAlarmValue " + getUnit());
                    return v * 1000;
                }
            }

            if (mhighValueUnit.equals("Ω·cm") || mlowValueUnit.equals("Ω·cm")) {
                if ("Ω".equals(getUnit())) {
                    return v / 5;
                } else if ("KΩ".equals(getUnit())) {
                    return v / 500;
                } else {
                    return v / 100000;
                }
            }

            if (mhighValueUnit.equals("KΩ·cm") || mlowValueUnit.equals("KΩ·cm")) {
                if ("Ω".equals(getUnit())) {
                    return v * 200;
                } else if ("KΩ".equals(getUnit())) {
                    return v * 2;
                } else {
                    return v / 100;
                }
            }

            if (mhighValueUnit.equals("MΩ·cm") || mlowValueUnit.equals("MΩ·cm")) {
                if ("Ω".equals(getUnit())) {
                    return v * 500000;
                } else if ("KΩ".equals(getUnit())) {
                    return v * 2000;
                } else {
                    return v * 10;
                }
            }

            if (v > fullValue) {
                return (mSweepAngle * fullValue / fullValue);
            } else if (v < lowValue) {
                return (mSweepAngle * lowValue / fullValue);
            } else return (mSweepAngle * v / fullValue);
        } catch (Exception e) {
            return 0;
        }
    }

    private String getUnit() {
        return mCurrentUnit;
    }

    private double getValue() {
        return mCurrentValue;
    }

    // private final String ALARMs[] ={"ALARM_PH","ALARM_ORP","ALARM_COND","ALARM_SALINITY","ALARM_TDS","ALARM_RESISTIVITY"};
    private void updateViewGraph() {
        SharedPreferences sharedPreferences = MyApi.getInstance().getDataApi().getSetting();
        String ph_selection = sharedPreferences.getString(SettingConfig.ph_selection_SAVE_ID, "USA");
        String str = "";

        double alarmL = 0;
        double alarmH = 0;
        boolean alarmHSwitch = false;
        boolean alarmLSwitch = false;

        String key = "";
        int sweep = 0;
        boolean flagAlarmL = false;
        boolean flagAlarmH = false;
        switch (mGraphModeType) {
            case Constant.MODE_PH:
                mMyDashBoardView2.setMode(MyDashBoardView2.MODE_OVER);
                mMyDashBoardView2.setImageResource(R.mipmap.graph_ph);
                fullValue = 18;//14.0f;
                lowValue = -2;
                mLineView.setRangeY(16);
                mSweepAngle = 180;// 150;
                mZeroAngle = 20;
                key = Constant.ALARMs[0];
                str = sharedPreferences.getString(key, Constant.DEFAULT_ALARMs[0]);

                alarmLSwitch = getAlarmSwitch(alarmLSwitchMap, key);
                alarmL = alarmLSwitch ? getAlarmValue(alarmLMap, key) : (int) lowValue;
                mMyDashBoardView2.setPathL((int) (270 - mSweepAngle / 2), alarmLSwitch ? (int) (alarmL + mZeroAngle) : 0);
                alarmHSwitch = getAlarmSwitch(alarmHSwitchMap, key);
                alarmH = getAlarmValue(alarmHMap, key);
                sweep = alarmHSwitch ? (int) (mSweepAngle - mZeroAngle - alarmH) : 0;

                mMyDashBoardView2.setPathH((int) (270 + mSweepAngle / 2 - sweep), sweep);
                break;
            case Constant.MODE_RES:
                mMyDashBoardView2.setMode(MyDashBoardView2.MODE_ATOP);
                if ("Ω".equals(getUnit())) {
                    mMyDashBoardView2.setImageResource(R.mipmap.graph_ris_2);//1000 Ω
                    fullValue = 1000;
                } else if ("KΩ".equals(getUnit())) {
                    if (getValue() < 100) {
                        mMyDashBoardView2.setImageResource(R.mipmap.graph_ris_1);//100 KΩ
                        fullValue = 100;
                    } else {
                        mMyDashBoardView2.setImageResource(R.mipmap.graph_ris_2);//1000 KΩ
                        fullValue = 1000;
                    }
                } else if ("MΩ".equals(getUnit())) {
                    mMyDashBoardView2.setImageResource(R.mipmap.graph_ris_3);//20MΩ
                    fullValue = 20;
                } else {
                    mMyDashBoardView2.setImageResource(R.mipmap.graph_ris_1);//100 K
                    //mMyDashBoardView2.setImageResource(R.mipmap.graph_ris_2);//1000   0,K
                    //mMyDashBoardView2.setImageResource(R.mipmap.graph_ris_3);//20M
                    fullValue = 100;
                }
                lowValue = 0;

                mLineView.setRangeY(fullValue);
                mSweepAngle = 200;
                mZeroAngle = 0;
                key = Constant.ALARMs[5];
                str = sharedPreferences.getString(key, Constant.DEFAULT_ALARMs[5]);

                alarmLSwitch = getAlarmSwitch(alarmLSwitchMap, key);
                alarmL = alarmLSwitch ? getAlarmValue(alarmLMap, key) : (int) lowValue;
                mMyDashBoardView2.setPathL(270 - mSweepAngle / 2, alarmL);
                alarmHSwitch = getAlarmSwitch(alarmHSwitchMap, key);
                alarmH = getAlarmValue(alarmHMap, key);
                sweep = alarmHSwitch ? (int) (mSweepAngle - mZeroAngle - alarmH) : 0;

                mMyDashBoardView2.setPathH((int) (mZeroAngle + 270 + mSweepAngle / 2 - sweep), sweep);

                break;
            case Constant.MODE_COND:
                mMyDashBoardView2.setMode(MyDashBoardView2.MODE_ATOP);
                if ("mS".equals(getUnit())) {
                    mMyDashBoardView2.setImageResource(R.mipmap.graph_cond_3);//20 ms
                    fullValue = 20;
                } else if ("µS".equals(getUnit())) {
                    if (getValue() < 200.0f) {
                        mMyDashBoardView2.setImageResource(R.mipmap.graph_cond_1);//200 us
                        fullValue = 200;
                    } else {
                        mMyDashBoardView2.setImageResource(R.mipmap.graph_cond_2);//2000 us
                        fullValue = 2000;
                    }
                } else {
                    mMyDashBoardView2.setImageResource(R.mipmap.graph_cond_1);//200 us
                    fullValue = 200;
                }

                //
                lowValue = 0;
                mLineView.setRangeY(fullValue);
                mSweepAngle = 200;
                mZeroAngle = 0;
                key = Constant.ALARMs[2];

                str = sharedPreferences.getString(key, Constant.DEFAULT_ALARMs[2]);

                alarmLSwitch = getAlarmSwitch(alarmLSwitchMap, key);
                alarmL = alarmLSwitch ? getAlarmValue(alarmLMap, key) : (int) lowValue;
                mMyDashBoardView2.setPathL(270 - mSweepAngle / 2, alarmL);
                alarmHSwitch = getAlarmSwitch(alarmHSwitchMap, key);
                alarmH = getAlarmValue(alarmHMap, key);
                sweep = alarmHSwitch ? (int) (mSweepAngle - mZeroAngle - alarmH) : 0;

                mMyDashBoardView2.setPathH((int) (mZeroAngle + 270 + mSweepAngle / 2 - sweep), sweep);

                break;
            case Constant.MODE_SAL:
                mMyDashBoardView2.setMode(MyDashBoardView2.MODE_ATOP);
            /*    mMyDashBoardView2.setImageResource(R.mipmap.graph_sal);//10
                //mMyDashBoardView2.setImageResource(R.mipmap.graph_sal_nacl);//10
                //mMyDashBoardView2.setImageResource(R.mipmap.graph_sal_sea);//50
                fullValue = 10;
*/
                if (getValue() < 10.0f) {
                    mMyDashBoardView2.setImageResource(R.mipmap.graph_sal);//10
                    fullValue = 10;
                } else {
                    mMyDashBoardView2.setImageResource(R.mipmap.graph_sal_sea);//50
                    fullValue = 1000;
                }
                lowValue = 0;
                mLineView.setRangeY(fullValue);
                mSweepAngle = 200;
                mZeroAngle = 0;
                key = Constant.ALARMs[3];
                str = sharedPreferences.getString(key, Constant.DEFAULT_ALARMs[3]);
                alarmLSwitch = getAlarmSwitch(alarmLSwitchMap, key);
                alarmL = alarmLSwitch ? getAlarmValue(alarmLMap, key) : (int) lowValue;
                mMyDashBoardView2.setPathL(270 - mSweepAngle / 2, alarmL);
                alarmHSwitch = getAlarmSwitch(alarmHSwitchMap, key);
                alarmH = getAlarmValue(alarmHMap, key);
                sweep = alarmHSwitch ? (int) (mSweepAngle - mZeroAngle - alarmH) : 0;

                mMyDashBoardView2.setPathH((int) (mZeroAngle + 270 + mSweepAngle / 2 - sweep), sweep);

                break;
            case Constant.MODE_TDS:
                mMyDashBoardView2.setMode(MyDashBoardView2.MODE_ATOP);
            /*    mMyDashBoardView2.setImageResource(R.mipmap.graph_tds_1);//1000
                mMyDashBoardView2.setImageResource(R.mipmap.graph_tds_2);//10
*/
                if (getValue() < 10.0f) {
                    mMyDashBoardView2.setImageResource(R.mipmap.graph_tds_2);//10
                    fullValue = 10;
                } else {
                    mMyDashBoardView2.setImageResource(R.mipmap.graph_tds_1);//1000
                    fullValue = 1000;
                }
                lowValue = 0;
                //fullValue = 1000f;
                mLineView.setRangeY(fullValue);
                mSweepAngle = 200;
                mZeroAngle = 0;
                key = Constant.ALARMs[4];
                str = sharedPreferences.getString(key, Constant.DEFAULT_ALARMs[4]);
                alarmLSwitch = getAlarmSwitch(alarmLSwitchMap, key);
                alarmL = alarmLSwitch ? getAlarmValue(alarmLMap, key) : (int) lowValue;
                mMyDashBoardView2.setPathL(270 - mSweepAngle / 2, alarmL);
                alarmHSwitch = getAlarmSwitch(alarmHSwitchMap, key);
                alarmH = getAlarmValue(alarmHMap, key);
                sweep = alarmHSwitch ? (int) (mSweepAngle - mZeroAngle - alarmH) : 0;

                mMyDashBoardView2.setPathH((int) (mZeroAngle + 270 + mSweepAngle / 2 - sweep), sweep);

                break;
            case Constant.MODE_ORP:
                mMyDashBoardView2.setMode(MyDashBoardView2.MODE_ATOP);
                mMyDashBoardView2.setImageResource(R.mipmap.graph_orp);
                fullValue = 2000f;
                lowValue = -1000f;
                mLineView.setRangeY(fullValue);
                mSweepAngle = 180 + 24;
                mZeroAngle = mSweepAngle / 2;
                key = Constant.ALARMs[1];
                str = sharedPreferences.getString(key, Constant.DEFAULT_ALARMs[1]);
                alarmLSwitch = getAlarmSwitch(alarmLSwitchMap, key);
                alarmL = alarmLSwitch ? getAlarmValue(alarmLMap, key) : (int) lowValue;
                mMyDashBoardView2.setPathL(270 - mSweepAngle / 2, (int) (alarmL + mZeroAngle));
                alarmHSwitch = getAlarmSwitch(alarmHSwitchMap, key);
                alarmH = getAlarmValue(alarmHMap, key);
                sweep = alarmHSwitch ? (int) (mSweepAngle - mZeroAngle - alarmH) : 0;

                mMyDashBoardView2.setPathH((int) (270 + mSweepAngle / 2 - sweep), sweep);

                break;
        }

        DeviceSetting.Param param = JSON.parseObject(str, DeviceSetting.Param.class);//bleDevice.getSetting().getParam(settingIndex);

        if (param == null) return;

        mlowValueUnit = param.getString(Constant.lowValueUnit);
        mhighValueUnit = param.getString(Constant.highValueUnit);

        mImageViewGraph.setImageBitmap(mMyDashBoardView2.getBitmap());
        double current = mSweepAngle * mCurrentValue / fullValue;
        flagAlarmL = alarmLSwitch ? (current < alarmL) : false;
        flagAlarmH = alarmHSwitch ? (current > alarmH) : false;
        mAlarmDirty = mAlarmDirty || flagAlarmL != mAlarmL || flagAlarmH != mAlarmH;

        Log.v(getTAG(), "flagAlarmL=" + flagAlarmL + " flagAlarmH=" + flagAlarmH + " current=" + current + " alarmL=" + alarmL + " alarmH=" + alarmH);
        // mImageViewGraph.setBitmap(BitmapFactory.decodeResource(getResources(),mImageViewGraph.getImageResource()));
        if (mAlarmDirty) {
            byte s = isAutoSave() ? Measure.ContinuousMeasureON : 0;
            if (mAlarmL) s |= Measure.LOW_ALARM_ON;
            if (mAlarmH) s |= Measure.UPPER_ALARM_ON;
            mAlarmH = flagAlarmH;
            mAlarmL = flagAlarmL;
            mAlarmDirty = false;
            MyApi.getInstance().getBtApi().sendCommand(new Measure(s));
        }
    }

    private boolean getAlarmSwitch(Map<String, Boolean> map, String key) {
        Boolean ret = map.get(key);
        if (ret == null) return false;
        return ret;
    }

    private void initView2(View view) {
        //iv_graph
        //iv_dial
        mImageViewGraph = view.findViewById(R.id.iv_graph);
        mImageViewDial = view.findViewById(R.id.iv_dial);
        mTextViewDialUnit = view.findViewById(R.id.gp_tv_unit);
 /*       MyDashBoardView  dashView = (MyDashBoardView) view.findViewById(R.id.dashview);
        dashView.setmMax(14.0f);
        dashView.setCurrentValue((float) (Math.random() * 10));*/
    }

    private void initView3(View view) {
        lineLeftTextView = (TextView) view.findViewById(R.id.tv_left);
        lineRightTextView = (TextView) view.findViewById(R.id.tv_right);
        final LineView lineView = (LineView) view.findViewById(R.id.line_view);
        lineView.setOnTouchListener(new OnDoubleClickListener(new OnDoubleClickListener.DoubleClickCallback() {
            @Override
            public void onDoubleClick() {
                //mLineView.zoom();
                if (lineZoom == 1) {
                    lineZoom = 16;
                } else {
                    lineZoom = 1;
                }
                Log.i("Zoom", "onDoubleClick  lineZoom " + lineZoom);
                mLineView.zoom(1.0f / lineZoom);
                //updateViewGraph3();
                updateViewGraph3Clear();

            }
        }));
        lineView.setGridCount(5);
        lineView.setWidth(getWidth());
        lineView.setSideLineLength(getWidth() * 100 / 750);
        lineView.setDrawDotLine(false); //optional
        //lineView.setShowPopup(LineView.SHOW_POPUPS_MAXMIN_ONLY); //optional
        //int randomint = 16;
        ArrayList<String> strList = new ArrayList<>();
        SimpleDateFormat formatter1 = new SimpleDateFormat(Constant.TimeFormat, Locale.US);
        long t = System.currentTimeMillis();
        strList.add(formatter1.format(new Date(t)));
        for (int i = 0; i < mLineDateSize; i++) {
            strList.add("");
        }
        lineView.setBottomTextList(strList);
        lineView.setColorArray(new int[]{Color.parseColor("#1ab9f0"), Color.parseColor("#a9e65d"), Color.GRAY, Color.CYAN});
        List<List<Integer>> dataLists = new ArrayList<>();
        List<Integer> dataList = new ArrayList<>();

        // float random = (float) (Math.random() * 9 + 1);
        for (int i = 0; i < mLineDateSize; i++) {
            dataList.add(0);
        }

        ArrayList<Integer> dataList2 = new ArrayList<>();
        // random = (int) (Math.random() * 9 + 1);
        for (int i = 0; i < mLineDateSize; i++) {
            dataList2.add(0);
        }

    /*    ArrayList<Integer> dataList3 = new ArrayList<>();
        random = (int) (Math.random() * 9 + 1);
        for (int i = 0; i < randomint; i++) {
            dataList3.add((int) (Math.random() * random));
        }*/
        dataLists.add(dataList);
        dataLists.add(dataList2);
        // dataLists.add(dataList3);
        lineView.setDataList(dataLists); //or lineView.setFloatDataList(floatDataLists)
        mLineView = lineView;
    }
    //private List<RowHeader> mRowHeaderList;
/*
    private List<ColumnHeader> mColumnHeaderList;
    private List<List<Cell>> mCellList = new ArrayList<>();
    private List<List<Cell>> mCellList2 = new ArrayList<>();
*/
/*
    private AbstractTableAdapter mTableViewAdapter;
    private TableView mTableView;*/

    private void initView4(ViewGroup view) {
      /*  initData();
        mTableView = createTableView();
        ViewGroup viewGroup = view.findViewById(R.id.layout_view);
        viewGroup.addView(mTableView);

        loadData();*/
        //SimpleAdapter
        ViewGroup viewGroup = view.findViewById(R.id.layout_view);
        mDataListView = viewGroup.findViewById(R.id.list_view);
        mMeasureDataSimpleAdapter = new MeasureDataSimpleAdapter(getContext());
        mMeasureDataSimpleAdapter.setData(new LinkedList<MeasureDataSimpleAdapter.Bean>());
        mDataListView.setAdapter(mMeasureDataSimpleAdapter);
    }

/*    private TableView createTableView() {
        TableView tableView = new TableView(getContext());

        // Set adapter
        mTableViewAdapter = new TableViewAdapter(getContext());
        tableView.setAdapter(mTableViewAdapter);

        // Set layout params
        FrameLayout.LayoutParams tlp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams
                .MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        tableView.setLayoutParams(tlp);

        // Set TableView listener
        //tableView.setTableViewListener(new TableViewListener(tableView));
        tableView.setIgnoreSelectionColors(true);
        return tableView;
    }*/


/*    private void initData() {
        //mRowHeaderList = new ArrayList<>();
        mColumnHeaderList = new ArrayList<>();
        mCellList = new ArrayList<>();
        for (int i = 0; i < ROW_SIZE; i++) {
            mCellList.add(new ArrayList<Cell>());
        }
    }*/

/*    private void loadData() {
        // List<RowHeader> rowHeaders = getRowHeaderList();
        // List<List<Cell>> cellList = getCellListForSorting(); // getCellList();
        // getRandomCellList(); //
        List<ColumnHeader> columnHeaders = getColumnHeaderList(); //getRandomColumnHeaderList(); //

        // m_jRowHeaderList.addAll(rowHeaders);
     *//*   for (int i = 0; i < cellList.size(); i++) {
            mCellList.get(i).addAll(cellList.get(i));
        }*//*

        // Load all data
        mColumnHeaderList.addAll(columnHeaders);
        mTableViewAdapter.setAllItems(mColumnHeaderList, new ArrayList(), mCellList);

    }*/

    private void updateViewGraph4(String value, String temp) {
        //List<Cell> cellList = new ArrayList<>();
        SimpleDateFormat formatter1 = new SimpleDateFormat(Constant.DateFormat, Locale.US);
        SimpleDateFormat formatter2 = new SimpleDateFormat(Constant.Time2Format, Locale.US);
        Date date = new Date();
        String date1Str = formatter1.format(date);
        String date2Str = formatter2.format(date);
        //ArrayList<MeasureDataSimpleAdapter.Bean> data =mMeasureDataSimpleAdapter.getData();
        MeasureDataSimpleAdapter.Bean bean1 = mMeasureDataSimpleAdapter.getItem(0);//mMeasureDataSimpleAdapter.getCount()-1);
        MeasureDataSimpleAdapter.Bean bean = new MeasureDataSimpleAdapter.Bean();
        bean.sn = bean1 == null ? 1 : (bean1.sn + 1);
        bean.date = date1Str;
        bean.time = date2Str;
        bean.temp = temp;
        bean.value = value;
        mMeasureDataSimpleAdapter.addData(bean);
        mMeasureDataSimpleAdapter.notifyDataSetChanged();
      /*  for (int j = 0; j < COLUMN_SIZE; j++) {
            String strText = "";

            if (j == 0) {
                strText = "" + (mCellList2.size() + 1);
            } else if (j == 1) {

                strText = date1Str;//new Date().toLocaleString();
            } else if (j == 2) {
                strText = date2Str;
            } else if (j == 3) {
                strText = String.valueOf(value);
            } else if (j == 4) {
                strText = String.valueOf(temp);
            }

            String strID = (mCellList2.size() + 1) + "-" + j;

            Cell cell = new Cell(strID, strText);
            cellList.add(cell);
        }
        mCellList2.add(cellList);
        for (int i = 0;i<ROW_SIZE && i < mCellList2.size() && i < mCellList.size(); i++) {
            if (mCellList.get(i).size() == 0) {
                mCellList.get(i).addAll(mCellList2.get(i));
            }
        }
     *//*   if (mCellList2.size() < mCellList.size()) {
            for (int i = mCellList2.size(); i < mCellList.size(); i++) {
                mCellList.get(i).addAll(new ArrayList<Cell>());
            }
        }*//*
        mTableViewAdapter.setAllItems(mColumnHeaderList, new ArrayList(), mCellList);*/
    }

/*    private List<RowHeader> getRowHeaderList() {
        List<RowHeader> list = new ArrayList<>();
        for (int i = 0; i < ROW_SIZE; i++) {
            RowHeader header = new RowHeader(String.valueOf(i), "row " + i);
            list.add(header);
        }

        return list;
    }*/

   /* private List<ColumnHeader> getColumnHeaderList() {
        List<ColumnHeader> list = new ArrayList<>();
        int w = getWidth() / COLUMN_SIZE;
        for (int i = 0; i < COLUMN_SIZE; i++) {
            String strTitle = COL_HEADER[i];

            ColumnHeader header = new ColumnHeader(String.valueOf(i), strTitle);
            header.setTextColor(Color.WHITE);
            header.setWidth(w);
            if (i % 2 == 0) {
                header.setBackgroundColor(getColor(R.color.colHeader1));
            } else {
                header.setBackgroundColor(getColor(R.color.colHeader2));
            }
            list.add(header);
        }

        return list;
    }*/

/*    private List<ColumnHeader> getRandomColumnHeaderList() {
        List<ColumnHeader> list = new ArrayList<>();

        for (int i = 0; i < COLUMN_SIZE; i++) {
            String strTitle = "column " + i;
            int nRandom = new Random().nextInt();
            if (nRandom % 4 == 0 || nRandom % 3 == 0 || nRandom == i) {
                strTitle = "large column " + i;
            }

            ColumnHeader header = new ColumnHeader(String.valueOf(i), strTitle);
            list.add(header);
        }

        return list;
    }*/

/*    private List<List<Cell>> getCellList() {
        List<List<Cell>> list = new ArrayList<>();
        for (int i = 0; i < ROW_SIZE; i++) {
            List<Cell> cellList = new ArrayList<>();
            for (int j = 0; j < COLUMN_SIZE; j++) {
                String strText = "cell " + j + " " + i;
                if (j % 4 == 0 && i % 5 == 0) {
                    strText = "large cell " + j + " " + i + ".";
                }
                String strID = j + "-" + i;

                Cell cell = new Cell(strID, strText);
                cellList.add(cell);
            }
            list.add(cellList);
        }

        return list;
    }*/
/*
    private List<List<Cell>> getCellListForSorting() {
        List<List<Cell>> list = new ArrayList<>();
        for (int i = 0; i < ROW_SIZE; i++) {
            List<Cell> cellList = new ArrayList<>();
            for (int j = 0; j < COLUMN_SIZE; j++) {
                Object strText = "cell " + j + " " + i;

                if (j == 0) {
                    strText = i;
                } else if (j == 1) {
                    int nRandom = new Random().nextInt();
                    strText = nRandom;
                }

                String strID = j + "-" + i;

                Cell cell = new Cell(strID, strText);
                cellList.add(cell);
            }
            list.add(cellList);
        }

        return list;
    }*/

    public static final int COLUMN_SIZE = 5;
    public static final int ROW_SIZE = 50;
/*

    private List<List<Cell>> getRandomCellList() {
        List<List<Cell>> list = new ArrayList<>();
        for (int i = 0; i < ROW_SIZE; i++) {
            List<Cell> cellList = new ArrayList<>();
            list.add(cellList);
            for (int j = 0; j < COLUMN_SIZE; j++) {
                String strText = "cell " + j + " " + i;
                int nRandom = new Random().nextInt();
                if (nRandom % 2 == 0 || nRandom % 5 == 0 || nRandom == j) {
                    strText = "large cell  " + j + " " + i + getRandomString() + ".";
                }

                String strID = j + "-" + i;

                Cell cell = new Cell(strID, strText);
                cellList.add(cell);
            }
        }

        return list;
    }
*/

/*
    private String getRandomString() {
        Random r = new Random();
        String str = " a ";
        for (int i = 0; i < r.nextInt(); i++) {
            str = str + " a ";
        }

        return str;
    }*/
/*
    private void setFullScreenMode() {
        // Set full screen mode
        this.getActivity().getWindow().getDecorView().setSystemUiVisibility(View
                .SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View
                .SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide
                // nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }*/

    public void setTitle(String title) {
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void updateView() {
        super.updateView();

        Log.v(getTAG(), "mModeType=" + mModeType + " viewPager currentItem=" + viewPager.getCurrentItem());
        DataApi dataApi = MyApi.getInstance().getDataApi();
        DataBean bean = dataApi.getData();
        Log.v(getTAG(), "bean = " + bean.toString());
        if (mModeType != bean.getMode() && bean.getMode() != 0) {
            mNewMode = true;
            mModeType = bean.getMode();
//            clearHistory();
            Log.d(getTAG(), "update mode to " + mModeType);
        }

        if (mGraphModeType == Constant.MODE_VELA_PH) {
            mViewGroupTempRef.setVisibility(View.GONE);
            mViewGroupTempCoeffc.setVisibility(View.GONE);
            mViewGroupSalinity.setVisibility(View.GONE);
            mViewGroupTDS.setVisibility(View.GONE);
            mViewGroupZeroPoint.setVisibility(View.VISIBLE);
            mViewGroupPoints.setVisibility(View.VISIBLE);
            mViewGroupLastDate.setVisibility(View.VISIBLE);
            CalibrationPh calibrationPh = bean.getCalibrationPh();
            if (calibrationPh != null) {
                Log.i("testph", "111111111111111");
                mTextViewOffset.setText(getFormatDouble(calibrationPh.getOffset1(), 1) + "mV");
                mTextViewSlop.setText(getFormatDouble(calibrationPh.getSlope1(), 1) + "%");
                mTextViewCalDate.setText(getDataString(calibrationPh.getDate()));
                int c = 0;
                ImageView imageView[] = {mImageViewPoint1, mImageViewPoint2, mImageViewPoint3};
                if (calibrationPh.is168()) {
                    if (c < imageView.length) {
                        imageView[c].setImageResource(pHSelect == ParmUp.USA ? R.mipmap.ph_usa_1 : R.mipmap.ph_nist_1);
                        imageView[c].setVisibility(View.VISIBLE);
                        c++;
                    }
                }
                if (calibrationPh.is400()) {
                    if (c < imageView.length) {

                        imageView[c].setImageResource(pHSelect == ParmUp.USA ? R.mipmap.ph_usa_2 : R.mipmap.ph_nist_2);
                        imageView[c].setVisibility(View.VISIBLE);
                        c++;
                    }
                }
                if (calibrationPh.is700()) {
                    if (c < imageView.length) {
                        imageView[c].setImageResource(pHSelect == ParmUp.USA ? R.mipmap.ph_usa_3 : R.mipmap.ph_nist_3);
                        imageView[c].setVisibility(View.VISIBLE);
                        c++;
                    }
                }
                if (calibrationPh.is1001()) {
                    if (c < imageView.length) {
                        imageView[c].setImageResource(pHSelect == ParmUp.USA ? R.mipmap.ph_usa_4 : R.mipmap.ph_nist_4);
                        imageView[c].setVisibility(View.VISIBLE);
                        c++;
                    }
                }
                if (calibrationPh.is1245()) {
                    if (c < imageView.length) {
                        imageView[c].setImageResource(pHSelect == ParmUp.USA ? R.mipmap.ph_usa_5 : R.mipmap.ph_nist_5);
                        imageView[c].setVisibility(View.VISIBLE);
                        c++;
                    }
                }
                if (c <= 1) {
                    mViewGroupSlop.setVisibility(View.GONE);

                } else {
                    mViewGroupSlop.setVisibility(View.VISIBLE);

                }
                if (c == 0) {
                    mTextViewCalDate.setText("");
                    mTextViewOffset.setText("");
                }


                if (c < imageView.length) {
                    for (; c < imageView.length; c++) {
                        imageView[c].setVisibility(View.INVISIBLE);
                    }
                }

            } else {
                mViewGroupSlop.setVisibility(View.GONE);
                mImageViewPoint1.setImageDrawable(null);
                mImageViewPoint2.setImageDrawable(null);
                mImageViewPoint3.setImageDrawable(null);
                mTextViewOffset.setText("");
                mTextViewSlop.setText("");

                mTextViewCalDate.setText("");
                Log.i("testph", "22222222222");
            }
        } else if (mGraphModeType == Constant.MODE_VELA_COND) {
            mViewGroupTempRef.setVisibility(View.VISIBLE);
            mViewGroupTempCoeffc.setVisibility(View.VISIBLE);
            if (dataApi.getLastParm() != null) {
                mTextViewTempRef.setText(dataApi.getLastParm().getRefTemp());
                mTextViewTempCoeffc.setText(dataApi.getLastParm().getTempCompensate());
            }
            mViewGroupSalinity.setVisibility(View.GONE);
            mViewGroupTDS.setVisibility(View.GONE);
            mViewGroupSlop.setVisibility(View.GONE);
            mViewGroupZeroPoint.setVisibility(View.GONE);
            mViewGroupPoints.setVisibility(View.VISIBLE);
            mViewGroupLastDate.setVisibility(View.VISIBLE);
            CalibrationCond calibrationCond = bean.getCalibrationCond();
            if (calibrationCond != null) {
                // mTextViewOffset.setText(getFormatDouble(calibrationCond.getOffset1(), 2));
                // mTextViewSlop.setText(getFormatDouble(calibrationCond.getSlope1(), 2) + "");
                Log.i("testcond", "33333333");
                mTextViewCalDate.setText(getDataString(calibrationCond.getDate()));
                mImageViewPoint1.setImageResource(R.mipmap.cond_1);
                mImageViewPoint2.setImageResource(R.mipmap.cond_2);
                mImageViewPoint3.setImageResource(R.mipmap.cond_3);
                mImageViewPoint1.setVisibility(calibrationCond.is84() ? View.VISIBLE : View.INVISIBLE);
                mImageViewPoint3.setVisibility(calibrationCond.is1288() ? View.VISIBLE : View.INVISIBLE);
                mImageViewPoint2.setVisibility(calibrationCond.is1413() ? View.VISIBLE : View.INVISIBLE);
            } else {
                mImageViewPoint1.setImageDrawable(null);
                mImageViewPoint2.setImageDrawable(null);
                mImageViewPoint3.setImageDrawable(null);
                mTextViewOffset.setText("");
                mTextViewSlop.setText("");

                mTextViewCalDate.setText("");
            }
        } else if (mGraphModeType == Constant.MODE_VELA_ORP) {
            mViewGroupSlop.setVisibility(View.GONE);
            mViewGroupZeroPoint.setVisibility(View.GONE);
            mViewGroupPoints.setVisibility(View.GONE);
            mViewGroupLastDate.setVisibility(View.GONE);

            mImageViewPoint1.setImageDrawable(null);
            mImageViewPoint2.setImageDrawable(null);
            mImageViewPoint3.setImageDrawable(null);

            mTextViewOffset.setText("");
            mTextViewSlop.setText("");
            mTextViewCalDate.setText("");

            // ORP NEVER uses salinity or TDS — always hide
            mViewGroupTempRef.setVisibility(View.GONE);
            mViewGroupTempCoeffc.setVisibility(View.GONE);
            mViewGroupSalinity.setVisibility(View.GONE);
            mViewGroupTDS.setVisibility(View.GONE);
        } else {
            mViewGroupSlop.setVisibility(View.GONE);
            mViewGroupZeroPoint.setVisibility(View.GONE);
            mViewGroupPoints.setVisibility(View.GONE);
            mViewGroupLastDate.setVisibility(View.GONE);
            mImageViewPoint1.setImageDrawable(null);
            mImageViewPoint2.setImageDrawable(null);
            mImageViewPoint3.setImageDrawable(null);
            mTextViewOffset.setText("");
            mTextViewSlop.setText("");
            mTextViewCalDate.setText("");
            Log.i("testcond", "4444444444");
            mViewGroupTempRef.setVisibility(View.GONE);
            mViewGroupTempCoeffc.setVisibility(View.GONE);
            mViewGroupSalinity.setVisibility(mModeType == Constant.MODE_SAL ? View.VISIBLE : View.GONE);
            if (mGraphModeType == Constant.MODE_SAL) {
                mTextViewSalinity.setText(R.string.salinity);
            }
            mViewGroupTDS.setVisibility(mModeType == Constant.MODE_TDS ? View.VISIBLE : View.GONE);

            if (mGraphModeType == Constant.MODE_TDS) {
                if (dataApi.getLastParm() != null) {
                    mTextViewTDS.setText(dataApi.getLastParm().getTDSFactor());
                }
            }
        }
        mNewMode = false;


//        mViewFace.setVisibility(bean.isLaughFace()?View.VISIBLE:View.INVISIBLE);
//        mViewHold.setVisibility(bean.isHold()?View.VISIBLE:View.INVISIBLE);
        if (bean.isHold()) {
            mTextViewHoldBt.setText(R.string.unhold);
        } else {
            mTextViewHoldBt.setText(R.string.hold);
        }
//        mViewAlarmDown.setVisibility(bean.isLowerAlarm()?View.VISIBLE:View.INVISIBLE);
//        mViewAlarmUp.setVisibility(bean.isUpperAlarm()?View.VISIBLE:View.INVISIBLE);

//        mAlarmDirty = mAlarmDirty || bean.isLowerAlarm()!=mAlarmL ||  bean.isUpperAlarm()!=mAlarmH;

        mCurrentTemp = bean.getTemp();
        mModeType = bean.getMode();
        mTempTextView.setText(getFormatDouble(mCurrentTemp, bean.getTempPointDigit()));

        Data data = bean.getData();

        if (data != null) {
            int digit = data.getPointDigit();

            mModeType = data.getMode();
            String unitStr = data.getUnitString();
            String valueStr = getFormatDouble(data.getValue(), digit);


            switch (mModeType) {
                case Constant.MODE_VELA_PH:
//                mTitleView.setText(R.string.ph);
                    mCurrentValue = bean.getPh();

                    tv_value_ph.setText(valueStr);
                    tv_danwei1_ph.setText(unitStr);

                    alarmid = getAlarmId(mCurrentValue, bean.getTemp(), 0);

                    break;
                case Constant.MODE_VELA_COND:
                    //mDanweiTextView.setText(R.string.uscm);//"uS/cm");
//                mTitleView.setText(R.string.conductivity);
                    mCurrentValue = bean.getEc();

                    tv_value_cond.setText(valueStr);
                    tv_danwei1_cond.setText(unitStr);

                    alarmid = getAlarmId(mCurrentValue, bean.getTemp(), 2);
                    break;
                case Constant.MODE_VELA_ORP:
                    // mDanweiTextView.setText(R.string.mv);
//                mTitleView.setText(R.string.orp);
                    mCurrentValue = bean.getOrp();

                    tv_value_orp.setText(valueStr);
                    tv_danwei1_orp.setText(unitStr);

                    alarmid = getAlarmId(mCurrentValue, bean.getTemp(), 1);
                    break;
                case Constant.MODE_VELA_RES:
                    break;
                case Constant.MODE_VELA_SALT:
                    break;
                case Constant.MODE_VELA_TDS:
                    break;
            }

            if (Reid == 3) {
                Reid = 0;
                HomeActivity activity = (HomeActivity) getActivity();
                ImageView imageView = (ImageView) activity.findViewById(R.id.home_bg_tipimg);
                imageView.setImageResource(R.mipmap.bg);
                imageViewbg.setImageResource(R.mipmap.bg);
            } else {
//            Log.v(getTAG(),"bean = "+alarmid);
                if (alarmid == 2) {
                    imageViewbg.setImageResource(R.mipmap.alarmbg);
                    HomeActivity activity = (HomeActivity) getActivity();
                    ImageView imageView = (ImageView) activity.findViewById(R.id.home_bg_tipimg);
                    imageView.setImageResource(R.mipmap.alarmbg);
                    if (alarmbtid == 1) {
                        relativeLayoutdia.setVisibility(View.INVISIBLE);
                    } else {
                        relativeLayoutdia.setVisibility(View.VISIBLE);
                    }

                    if (!isAppOnForeground()) {
                        if (NotiTime == 60) {
                            NotiTime = 0;
                            NotificationChannel channel = new NotificationChannel("localService", "告警", NotificationManager.IMPORTANCE_NONE);
                            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

                            NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);

                            notificationManager.createNotificationChannel(channel);
                            NotificationCompat.Builder bBuilder = new NotificationCompat.Builder(getContext(), "localService");
                            Notification notification = bBuilder.setOngoing(true)
                                    .setContentTitle(getString(R.string.alarm))
                                    .setContentText(getString(R.string.Alerm_text))
                                    .setWhen(System.currentTimeMillis())
                                    .setSmallIcon(R.drawable.ic_launchertwo)
                                    .setStyle(new NotificationCompat.BigTextStyle())
                                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launchersmall))
                                    .build();
                            notificationManager.notify(30001, notification);
                        } else {
                            NotiTime++;
                        }
                    }
                }

                if (alarmid == 1) {
                    imageViewbg.setImageResource(R.mipmap.bg);
//                relativeLayoutdia.setVisibility(View.INVISIBLE);
                    HomeActivity activity = (HomeActivity) getActivity();
                    ImageView imageView = (ImageView) activity.findViewById(R.id.home_bg_tipimg);
                    imageView.setImageResource(R.mipmap.bg);
                }
                Reid++;
            }

//        mTextViewDialUnit.setText(mDanweiTextView.getText());
//        if (!mDanweiTextView.getText().toString().equals(lineLeftTextView.getText().toString())) {
//            lineLeftTextView.setText(mDanweiTextView.getText());
//        }
//        mCurrentUnit =mTextViewDialUnit.getText().toString();

            // --- choose which parameter this graph set is showing ---
            double phValue = bean.getPh();
            double condValue = bean.getEc();
            double orpValue = bean.getOrp();

            // Default unit strings for each param
            String phUnit = tv_danwei1_ph.getText().toString();
            String condUnit = tv_danwei1_cond.getText().toString();
            String orpUnit = tv_danwei1_orp.getText().toString();

            String phValueString = tv_value_ph.getText().toString();
            String condValueString = tv_value_cond.getText().toString();
            String orpValueString = tv_value_orp.getText().toString();

            String mCurrentValueStr = "";

            switch (mModeType) {
                case Constant.MODE_VELA_PH:
                    mCurrentValue = phValue;
                    mCurrentValueStr = phValueString;
                    mCurrentUnit = phUnit;
                    alarmid = getAlarmId(mCurrentValue, mCurrentTemp, 0); // PH uses ALARMs[0]
                    break;
                case Constant.MODE_VELA_COND:
                    mCurrentValue = condValue;
                    mCurrentValueStr = condValueString;
                    mCurrentUnit = condUnit;
                    alarmid = getAlarmId(mCurrentValue, mCurrentTemp, 2); // COND uses ALARMs[2]
                    break;
                case Constant.MODE_VELA_ORP:
                    mCurrentValue = orpValue;
                    mCurrentValueStr = orpValueString;
                    mCurrentUnit = orpUnit;
                    alarmid = getAlarmId(mCurrentValue, mCurrentTemp, 1); // ORP uses ALARMs[1]
                    break;
                default:
                    // fallback – keep existing behavior
                    break;
            }

            // also sync the left label of the line chart with current graph unit
            lineLeftTextView.setText(mCurrentUnit);

            updateViewGraph();
            setDialValue(mCurrentValue);

            if (!mAutoSaveRun) {
                Log.d(getTAG(), "current mode: " + data.getMode() + " selected: " + mGraphModeType);
                if (data.getMode() == mGraphModeType) {
                    updateViewGraph3(data.getValue(), data.getTemp());
                    updateViewGraph4(valueStr + " " + unitStr, mTempTextView.getText().toString() + " " + mTempUnitTextView.getText().toString());
                }
            }

            if (data != null && data.getUnit2() == 0) {
                boolean isCalibration = MyApi.getInstance().getDataApi().isCalibration();
                if (!isCalibration) {
                    int mode = MyApi.getInstance().getDataApi().conventMode(data.getMode());

                    if (mode == Constant.MODE_PH || mode == Constant.MODE_COND) {
                        Intent intent = new Intent(getContext(), CalibrationActivity.class);
                        intent.putExtra("MODE", mode);
                        startActivity(intent);
                    } else {
                        Log.d(getTAG(), "mode = " + mode);
                    }
                }
            } else {
                if (data != null && (mTempUnit != data.getUnit2())) {
                    switch (data.getUnit2()) {
                        case Data.UNIT_C:
                            mTempUnitStr = "C";
                            mTempUnitTextView.setText(R.string.temp_degree_c);
                            mTempUnit = data.getUnit2();
                            lineRightTextView.setText(mTempUnitTextView.getText());
                            break;
                        case Data.UNIT_F:
                            mTempUnitStr = "F";
                            mTempUnitTextView.setText(R.string.zen_temp_degree_f);
                            mTempUnit = data.getUnit2();
                            lineRightTextView.setText(mTempUnitTextView.getText());
                            break;
                        default:
                            break;
                    }
                }
                Log.v(getTAG(), "data : " + data);
            }

            if (bean.hasReminder()) {
                mLayoutHint.setVisibility(View.INVISIBLE);
                int n = MyApi.getInstance().getDataApi().getReminder();
                mRemindersTextView.setText(n > 1 ? String.format(getString(R.string.n_hits), n) : getText(R.string.one_hits));
            } else {
                mLayoutHint.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private int getAlarmId(Double d, Double t, int n) {
        try {
            String key = Constant.ALARMs[n];

            if (getAllAlarmValue(alarmHMap, key) == 0 && getAllAlarmValue(alarmLMap, key) == 0) {
                return 1;
            }

            if (mAlarmH || mAlarmL) {
                Log.d(getTAG(), "modess = " + getAllAlarmValue(alarmHMap, key));
                if (t == 0) {
                    return 1;
                } else {
                    return 2;
                }
            } else {
                return 1;
            }

        } catch (Exception e) {
            return 0;
        }
    }

    private String getTraceString(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyymmddHHmmss", Locale.US);
        return simpleDateFormat.format(date);

    }

    private String getDataString(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Constant.DateTimeFormat, Locale.US);
        return simpleDateFormat.format(date);
    }

    final int DATA_SIZE1 = 16;
    private int lineZoom = 1;
    private int mLineDateSize = DATA_SIZE1;

    private void updateViewGraph3(double value, double temp) {
        mUpdateViewGraph3 = false;
        ArrayList<List<Float>> dataLists = new ArrayList<>();
        mOrgDataList1.add((float) value);
        mOrgDataList2.add((float) temp);
        mDataList1.add((float) value);
        mDataList2.add((float) temp);
        SimpleDateFormat formatter1 = new SimpleDateFormat(Constant.TimeFormat, Locale.US);
        long t = System.currentTimeMillis();

        int dataIndex = mDataList2.size() - 1;
        if (dataIndex >= mBottomTextList.size()) {
            int c = dataIndex - mBottomTextList.size() + 1;
            for (int i = 0; i < c; i++) {
                mBottomTextList.add("");
            }
        }
        mBottomTextList.add(dataIndex, formatter1.format(new Date(t)));
        mOrgBottomTextList.add(formatter1.format(new Date(t)));
        boolean remove = false;
        if (false && isAutoSave()) {
            //mLineView.setHorizontalGridNum(DATA_SIZE);
            if (mBottomTextList.size() < mLineDateSize * 10 && mBottomTextList.size() == mLineView.getCurrentHorizontalGridNum()) {
                mLineView.setHorizontalGridNumTwice();
                // mLineView.setBottomTextList(mBottomTextList);
            }

        } else {
            if (mDataList1.size() > mLineDateSize) {
                mDataList1.remove(0);
                remove = true;
            }
            if (mDataList2.size() > mLineDateSize) {
                mDataList2.remove(0);
                remove = true;
            }
            if (mBottomTextList.size() > mLineDateSize) {
                mBottomTextList.remove(0);
            }
        }

        if (lineZoom == 1 /*||  mOrgDataList1.size()<=DATA_SIZE1*/) {
            mLineView.setBottomTextList2(mBottomTextList);
            dataLists.add(mDataList1);
            dataLists.add(mDataList2);
        } else {
            List<Float> dataList1 = new LinkedList<>();
            List<Float> dataList2 = new LinkedList<>();
            List<String> bottomTextList = new LinkedList<>();
            int len = mOrgDataList1.size();
            int dataSize = DATA_SIZE1 * lineZoom;
            float m = ((float) len) / (dataSize);
            //if(m<=0.1f) m=0.1f;
       /*     if(m>=1.0f && lineZoom<=16) {
                lineZoom*=2;
                mLineView.zoom(1.0f/lineZoom);
            }*/

            for (float i = 0; i < len; i = (i + m)) {
                if (dataList1.size() >= dataSize) {
                    break;
                }
                bottomTextList.add(mOrgBottomTextList.get((int) i));
                dataList1.add(mOrgDataList1.get((int) i));
                dataList2.add(mOrgDataList2.get((int) i));
            }

            mLineView.setBottomTextList2(bottomTextList);
            dataLists.add(dataList1);
            dataLists.add(dataList2);
            mLineView.updateGridNum();
        }
        // dataLists.add(dataList3);
        mLineView.setFloatDataList(dataLists); //or lineView.setFloatDataList(floatDataLists)
    }

    private void updateViewGraph3Clear() {
        ArrayList<List<Float>> dataLists = new ArrayList<>();
        dataLists.add(new LinkedList<Float>());
        dataLists.add(new LinkedList<Float>());
        mLineView.setFloatDataList(dataLists);
        mUpdateViewGraph3 = true;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mUpdateViewGraph3) {
                    mUpdateViewGraph3 = false;
                    try {
                        updateViewGraph3();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 100);
    }

    private void updateViewGraph3() {
        ArrayList<List<Float>> dataLists = new ArrayList<>();
        dataLists.add(new LinkedList<Float>());
        dataLists.add(new LinkedList<Float>());
        mLineView.setFloatDataList(dataLists);


        if (lineZoom == 1 /*||  mOrgDataList1.size()<=DATA_SIZE1*/) {
            mLineView.setBottomTextList2(mBottomTextList);
            dataLists.add(mDataList1);
            dataLists.add(mDataList2);
        } else {
            List<Float> dataList1 = new LinkedList<>();
            List<Float> dataList2 = new LinkedList<>();
            List<String> bottomTextList = new LinkedList<>();
            int len = mOrgDataList1.size();
            int dataSize = DATA_SIZE1 * lineZoom;
            float m = ((float) len) / (dataSize);
            //if(m<=0.1f) m=0.1f;
       /*     if(m>=1.0f && lineZoom<=16) {
                lineZoom*=2;
                mLineView.zoom(1.0f/lineZoom);
            }*/

            for (float i = 0; i < len; i = (i + m)) {
                if (dataList1.size() >= dataSize) {
                    break;
                }
                bottomTextList.add(mOrgBottomTextList.get((int) i));
                dataList1.add(mOrgDataList1.get((int) i));
                dataList2.add(mOrgDataList2.get((int) i));


            }

            mLineView.setBottomTextList2(bottomTextList);
            dataLists.add(dataList1);
            dataLists.add(dataList2);
            mLineView.updateGridNum();
        }
        // dataLists.add(dataList3);
        mLineView.setFloatDataList(dataLists); //or lineView.setFloatDataList(floatDataLists)
    }

    public void setGraphModeType(int mode) {
        if (mGraphModeType != mode) {
            mGraphModeType = mode;
            clearHistory();            // clear graph history when switching
            updateViewGraph();         // redraw dial
            updateViewGraph3Clear();   // reset and rebuild line chart
        }
    }

    public boolean isAppOnForeground() {
        ActivityManager activityManager = (ActivityManager) getActivity().getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = getActivity().getApplicationContext().getPackageName();
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }

        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(packageName) && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }
        return false;
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

    private void saveRecordData() {
        try {
            String value = mValueTextView.getText().toString() + " " + mDanweiTextView.getText().toString();
            String temp1 = mTempTextView.getText().toString() + " " + mTempUnitTextView.getText().toString();
            String temp2 = mTempTextView.getText().toString() + " " + mTempUnitStr;// mTempUnitTextView.getText().toString();

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("value", value);
            jsonObject.put("temp", temp2);
            try {
                jsonObject.put("deviceNumber", MyApi.getInstance().getBtApi().getDeviceNumber());
                jsonObject.put("location", MyApi.getInstance().getDataApi().getLocation());
                jsonObject.put("tempUnit", mTempUnitStr);//mTempUnitTextView.getText().toString());
                if (MyApi.getInstance().getBtApi().getLastDevice() != null && MyApi.getInstance().getBtApi().getLastDevice().getSetting() != null) {
                    if (mModeType == Constant.MODE_PH) {
                        if (MyApi.getInstance().getBtApi().getLastDevice().getSetting().getCalibrationPh() != null) {
                            try {
                                Calibration calibration = MyApi.getInstance().getBtApi().getLastDevice().getSetting().getCalibration();
                                calibration.setMode("PH");
                                calibration.setRefTemp(MyApi.getInstance().getDataApi().getLastParm().getRefTemp());
                                calibration.setTempCompensate(MyApi.getInstance().getDataApi().getLastParm().getTempCompensate());
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                            jsonObject.put("calibration", MyApi.getInstance().getBtApi().getLastDevice().getSetting().getCalibrationJson());
                        }
                    } else if (mModeType == Constant.MODE_COND) {
                        if (MyApi.getInstance().getBtApi().getLastDevice().getSetting().getCalibrationCond() != null) {
                            try {
                                Calibration calibration = MyApi.getInstance().getBtApi().getLastDevice().getSetting().getCalibration();
                                calibration.setMode("COND");
                                calibration.setRefTemp(MyApi.getInstance().getDataApi().getLastParm().getRefTemp());
                                calibration.setTempCompensate(MyApi.getInstance().getDataApi().getLastParm().getTempCompensate());
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                            jsonObject.put("calibration", MyApi.getInstance().getBtApi().getLastDevice().getSetting().getCalibrationJson());
                        }
                    }
                }
            } catch (Exception e) {

            }

            MyApi.getInstance().getDataApi().saveData(mSn++, mTraceNo, jsonObject.toJSONString());
            log("json " + jsonObject.toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        if (mAutoSaveRun) {
            startTimer();
            try {
                updateViewGraph3(mCurrentValue, mCurrentTemp);
                String value = mValueTextView.getText().toString() + " " + mDanweiTextView.getText().toString();
                String temp1 = mTempTextView.getText().toString() + " " + mTempUnitTextView.getText().toString();
                String temp2 = mTempTextView.getText().toString() + " " + mTempUnitStr;// mTempUnitTextView.getText().toString();
                updateViewGraph4(value, temp1);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("value", value);
                jsonObject.put("temp", temp2);
                try {
                    jsonObject.put("deviceNumber", MyApi.getInstance().getBtApi().getDeviceNumber());
                    jsonObject.put("location", MyApi.getInstance().getDataApi().getLocation());
                    jsonObject.put("tempUnit", mTempUnitStr);//mTempUnitTextView.getText().toString());
                    if (MyApi.getInstance().getBtApi().getLastDevice() != null && MyApi.getInstance().getBtApi().getLastDevice().getSetting() != null) {
                        if (mModeType == Constant.MODE_PH) {
                            if (MyApi.getInstance().getBtApi().getLastDevice().getSetting().getCalibrationPh() != null) {
                                try {
                                    Calibration calibration = MyApi.getInstance().getBtApi().getLastDevice().getSetting().getCalibration();
                                    calibration.setMode("PH");
                                    calibration.setRefTemp(MyApi.getInstance().getDataApi().getLastParm().getRefTemp());
                                    calibration.setTempCompensate(MyApi.getInstance().getDataApi().getLastParm().getTempCompensate());
                                } catch (NullPointerException e) {
                                    e.printStackTrace();
                                }
                                jsonObject.put("calibration", MyApi.getInstance().getBtApi().getLastDevice().getSetting().getCalibrationJson());
                            }
                        } else if (mModeType == Constant.MODE_COND) {
                            if (MyApi.getInstance().getBtApi().getLastDevice().getSetting().getCalibrationCond() != null) {
                                try {
                                    Calibration calibration = MyApi.getInstance().getBtApi().getLastDevice().getSetting().getCalibration();
                                    calibration.setMode("COND");
                                    calibration.setRefTemp(MyApi.getInstance().getDataApi().getLastParm().getRefTemp());
                                    calibration.setTempCompensate(MyApi.getInstance().getDataApi().getLastParm().getTempCompensate());
                                } catch (NullPointerException e) {
                                    e.printStackTrace();
                                }
                                jsonObject.put("calibration", MyApi.getInstance().getBtApi().getLastDevice().getSetting().getCalibrationJson());
                            }
                        }
                    }
                } catch (Exception e) {

                }
                MyApi.getInstance().getDataApi().saveData(mSn++, mTraceNo, jsonObject.toJSONString());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}