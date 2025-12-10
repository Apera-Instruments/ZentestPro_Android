package com.zen.ui.fragment.measure;

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
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
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
import com.zen.api.event.ModePatternChangedFromDevice;
import com.zen.api.event.SyncEventUpload;
import com.zen.api.protocol.CalibrationCond;
import com.zen.api.protocol.CalibrationPh;
import com.zen.api.protocol.Data;
import com.zen.api.protocol.Key;
import com.zen.api.protocol.Measure;
import com.zen.api.protocol.Mode;
import com.zen.api.protocol.ParmUp;
import com.zen.api.protocol.velaprotocal.VelaParamModeAppToDevice;
import com.zen.ui.CalibrationActivity;
import com.zen.ui.CategoryActivity;
import com.zen.ui.HomeActivity;
import com.zen.ui.NoteActivity;
import com.zen.ui.R;
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
 * MeasureFragment – modularized with internal managers/controllers,
 */
public class MeasureFragmentVela extends BaseFragment
        implements View.OnClickListener, TextWatcher, Runnable {

    // ---------------------------------------------------------------------
    // Constants
    // ---------------------------------------------------------------------
    private static final String[] COL_HEADER = {"SN", "Date", "Time", "Value", "Temp.", ""};
    public static final int COLUMN_SIZE = 5;
    public static final int ROW_SIZE = 50;
    final int DATA_SIZE1 = 16;

    // ---------------------------------------------------------------------
    // Core Android / UI
    // ---------------------------------------------------------------------
    private LayoutInflater mInflater;
    private ViewPager viewPager;
    private SharedPreferences mSharedPreferences;
    private Handler mHandler = new Handler();

    // ---------------------------------------------------------------------
    // Mode / Graph mode
    // ---------------------------------------------------------------------
    private ModePopupWindow mModePopupWindow;
    private int mModeType = Constant.MODE_PH;
    private int mGraphModeType = Constant.MODE_VELA_PH;
    private boolean mNewMode = true;

    // ---------------------------------------------------------------------
    // Auto-save / timer
    // ---------------------------------------------------------------------
    private boolean mAutoSave = false;
    private boolean mAutoSaveRun = false;
    private long mTimer = 1000 * 10;
    private OptionsPickerView pvOptions;
    private String mTraceNo = null;
    private int mSn = 0;

    // ---------------------------------------------------------------------
    // UI widgets
    // ---------------------------------------------------------------------
    TextView mTextViewHoldBt;
    TextView mTextViewSetTimer;
    ImageButton buttonSave;
    TextView textViewSave;
    View mLayoutTimeSet;
    RadioGroup mRadioGroupSelect;
    View mLayoutHint;
    TextView mRemindersTextView;
    ImageView mImageMeasure;
    View mLayoutMeasure;
    TextView mDanweiTextView;
    TextView mValueTextView;
    TextView mTempUnitTextView;
    TextView mTempTextView;
    View mViewBottom;
    TextView mTitleView;
    ImageView mBtView;

    private ImageButton ib_hold, bt_set_timer;
    private RelativeLayout iv_menu;

    private ConstraintLayout mainValueContainer;
    private View mainRowPh;
    private View mainRowCond;
    private View mainRowOrp;

    private TextView tv_value_ph, tv_danwei1_ph;
    private TextView tv_value_cond, tv_danwei1_cond;
    private TextView tv_value_orp, tv_danwei1_orp;

    // radio group

    private RelativeLayout tab1, tab3, tab4;
    private ImageView ivTab1, ivTab3, ivTab4;

    // Graph / Dial
    private LineView mLineView;
    private List<Float> mDataList1 = new LinkedList<>();
    private List<Float> mDataList2 = new LinkedList<>();
    private List<Float> mOrgDataList1 = new LinkedList<>();
    private List<Float> mOrgDataList2 = new LinkedList<>();
    private List<String> mBottomTextList = new LinkedList<>();
    private List<String> mOrgBottomTextList = new LinkedList<>();

    private ImageView mImageViewPoint1;
    private ImageView mImageViewPoint2;
    private ImageView mImageViewPoint3;
    private TextView mTextViewOffset;
    private TextView mTextViewSlop;
    private TextView mTextViewCalDate;
    private ViewGroup mViewGroupSlop;
    private ViewGroup mViewGroupZeroPoint;
    private ViewGroup mViewGroupPoints;
    private TextView lineLeftTextView;
    private TextView lineRightTextView;

    // Extra calibration views
    private TextView mTextViewTempRef;
    private ViewGroup mViewGroupTempRef;
    private TextView mTextViewTempCoeffc;
    private ViewGroup mViewGroupTempCoeffc;
    private TextView mTextViewSalinity;
    private ViewGroup mViewGroupSalinity;
    private TextView mTextViewTDS;
    private ViewGroup mViewGroupTDS;
    private ViewGroup mViewGroupLastDate;

    private Animation mBitmapAnimation;
    private TextView mTextViewDialUnit;
    private ListView mDataListView;
    private MeasureDataSimpleAdapter mMeasureDataSimpleAdapter;

    // Data
    private double mCurrentValue;
    private double mCurrentTemp;
    private String mCurrentUnit;
    private double lowValue;
    private double fullValue = 0.00f;
    private int mSweepAngle = 160;
    private float mZeroAngle;
    private int mTempUnit = 0;
    private String mTempUnitStr;

    // Dial / graph draw
    private MyDashBoardView2 mMyDashBoardView2;
    private ImageView mImageViewGraph;
    private ImageView mImageViewDial;

    // Alarm
    private boolean mAlarmDirty = false;
    private boolean mAlarmL = false;
    private boolean mAlarmH = false;
    private Map<String, Double> alarmLMap = new HashMap<>();
    private Map<String, Double> alarmHMap = new HashMap<>();
    private Map<String, Boolean> alarmLSwitchMap = new HashMap<>();
    private Map<String, Boolean> alarmHSwitchMap = new HashMap<>();
    private String mhighValueUnit;
    private String mlowValueUnit;

    // Line graph zoom / size
    private int lineZoom = 1;
    private int mLineDateSize = DATA_SIZE1;
    private boolean mUpdateViewGraph3 = false;

    // Misc
    private int PERMISSION_REQUEST_CODE = 100;
    private HintsPopupWindow mHintsPopupWindow;
    private PowerManager.WakeLock wakeLock;
    private Random random = new Random();
    private AtomicInteger atomicInteger = new AtomicInteger(0);
    private int pHSelect = ParmUp.USA;

    ImageView imageViewbg;
    private int Reid = 0;
    private int NotiTime = 60;
    private int alarmid = 0;
    private int alarmbtid = 0;
    ImageButton imageButtonAlarm;
    RelativeLayout relativeLayoutdia;
    SaveImageDialog saveDialogFragment;

    private ArrayList<TimeBean> options1Items = new ArrayList<>();
    private ArrayList<ArrayList<String>> options2Items = new ArrayList<>();

    // ---------------------------------------------------------------------
    // Managers / Controllers (modularization)
    // ---------------------------------------------------------------------
    private ModePatternManager modePatternManager;
    private ModeManager modeManager;
    private GraphDialController graphDialController;
    private GraphLineController graphLineController;
    private TableController tableController;
    private AutoSaveManager autoSaveManager;
    private SaveRecordManager saveRecordManager;
    private AlarmManager alarmManager;
    private CalibrationManager calibrationManager;

    // ---------------------------------------------------------------------
    // Lifecycle / Entry
    // ---------------------------------------------------------------------

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
        if (mHintsPopupWindow.getError() != null) {
            mHintsPopupWindow.showAtLocation(
                    getActivity().getWindow().getDecorView(),
                    Gravity.CENTER,
                    0,
                    0
            );
        }
    }

    public void onMenu() {
        Activity a = getActivity();
        if (a instanceof HomeActivity) {
            ((HomeActivity) a).showMenu();
        }
    }

    public void onMenuClicked() {

        if (mModePopupWindow == null) {
            mModePopupWindow = new ModePopupWindow(getContext());

            mModePopupWindow.setOnClickListener(new ModePopupWindow.onButtonClickListener() {
                @Override
                public void onClick(int index) {

                    // --------------------------------------------------------
                    // Multi-select toggle logic (up to 3, at least 1 selected)
                    // --------------------------------------------------------
                    boolean phSel   = modePatternManager.isPhSelected();
                    boolean condSel = modePatternManager.isCondSelected();
                    boolean orpSel  = modePatternManager.isOrpSelected();

                    switch (index) {
                        case Constant.MODE_PH: // PH toggle
                            phSel = !phSel;
                            break;

                        case Constant.MODE_COND: // COND toggle
                            condSel = !condSel;
                            break;

                        case Constant.MODE_ORP: // ORP toggle
                            orpSel = !orpSel;
                            break;

                        default:
                            break;
                    }

                    // --------------------------------------------------------
                    // Ensure at least 1 selected
                    // --------------------------------------------------------
                    if (!phSel && !condSel && !orpSel) {
                        // revert change: keep the last selected item ON
                        switch (index) {
                            case Constant.MODE_PH: phSel = true; break;
                            case Constant.MODE_COND: condSel = true; break;
                            case Constant.MODE_ORP: orpSel = true; break;
                        }
                    }

                    // --------------------------------------------------------
                    // Update ModePatternManager
                    // --------------------------------------------------------
                    modePatternManager.setSelectedModes(phSel, condSel, orpSel);

                    clearHistory();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        updateView();
                    }

                    // update button visual states
                    mModePopupWindow.setMultiSelectState(phSel, condSel, orpSel);
                }
            });
        }

        // preload state from manager when opening the menu
        mModePopupWindow.setMultiSelectState(
                modePatternManager.isPhSelected(),
                modePatternManager.isCondSelected(),
                modePatternManager.isOrpSelected()
        );

        mModePopupWindow.showAsDropDown(iv_menu, 0, 0);
    }

    private void updateMainValueRowVisibility() {
        boolean phSel   = modePatternManager.isPhSelected();
        boolean condSel = modePatternManager.isCondSelected();
        boolean orpSel  = modePatternManager.isOrpSelected();

        // PH visibility
        if (mainRowPh != null)
            mainRowPh.setVisibility(phSel ? View.VISIBLE : View.GONE);

        // COND visibility
        if (mainRowCond != null)
            mainRowCond.setVisibility(condSel ? View.VISIBLE : View.GONE);

        // ORP visibility
        if (mainRowOrp != null)
            mainRowOrp.setVisibility(orpSel ? View.VISIBLE : View.GONE);
    }

    // ---------------------------------------------------------------------
    // Wake lock
    // ---------------------------------------------------------------------

    public void closeWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
            wakeLock = null;
        }
    }

    public void openWakeLock() {
        try {
            if (wakeLock != null && wakeLock.isHeld()) return;
            PowerManager powerManager =
                    (PowerManager) getContext().getSystemService(Service.POWER_SERVICE);

            if (powerManager != null) {
                wakeLock = powerManager.newWakeLock(
                        PowerManager.SCREEN_DIM_WAKE_LOCK,
                        "My Lock"
                );
                wakeLock.setReferenceCounted(false);
                wakeLock.acquire();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------------------------------------------------------------------
    // Save / Auto-save entry points (delegate to AutoSaveManager)
    // ---------------------------------------------------------------------

    public void onSave() {
        saveRecordManager.saveRecord();
    }

    public boolean isAutoSave() {
        return autoSaveManager.isAutoSave();
    }

    private void startTimer() {
        autoSaveManager.startTimer();
    }

    public void onSetTimer() {
        autoSaveManager.onSetTimer();
    }

    private void updateSetTimer() {
        autoSaveManager.updateSetTimer();
    }

    private void saveRecordData() {
        saveRecordManager.saveRecord();
    }

    // ---------------------------------------------------------------------
    // Small helpers
    // ---------------------------------------------------------------------

    private boolean checkPer(String permission) {
        return getContext().checkPermission(permission, Process.myPid(), Process.myUid())
                == PackageManager.PERMISSION_GRANTED;
    }

    // ---------------------------------------------------------------------
    // EventBus
    // ---------------------------------------------------------------------

    public void btConnect() {
        Activity activity = getActivity();
        if (activity instanceof HomeActivity) {
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
            modeManager.resetModePopup();
        }
    }

    private void loadAlarm() {
        alarmManager.loadAlarm();
    }

    @Override
    public void onResume() {
        super.onResume();
        mNewMode = true;
        loadAlarm();
    }

    // ---------------------------------------------------------------------
    // View setup
    // ---------------------------------------------------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedPreferences = getContext().getSharedPreferences(
                this.getClass().getSimpleName(),
                Context.MODE_PRIVATE
        );
        mTimer = mSharedPreferences.getLong("mTimer", mTimer);

        // managers
        modePatternManager = new ModePatternManager();
        modeManager = new ModeManager();
        graphDialController = new GraphDialController();
        graphLineController = new GraphLineController();
        tableController = new TableController();
        autoSaveManager = new AutoSaveManager();

        alarmManager = new AlarmManager();
        calibrationManager = new CalibrationManager();

        mMyDashBoardView2 = new MyDashBoardView2(getContext());
        if (isAutoSave()) {
            openWakeLock();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_measure, container, false);

        mInflater = inflater;

        // Top controls
        mTextViewHoldBt = view.findViewById(R.id.tv_hold_bt);
        mTextViewSetTimer = view.findViewById(R.id.tv_set_timer);
        buttonSave = view.findViewById(R.id.bt_save);
        textViewSave = view.findViewById(R.id.tv_save);
        mLayoutTimeSet = view.findViewById(R.id.layout_time_set);
        mRadioGroupSelect = view.findViewById(R.id.rg_select);
        mImageMeasure = view.findViewById(R.id.iv_layout_measure);
        mLayoutMeasure = view.findViewById(R.id.layout_measure);
        mModePopupWindow = null;

        // PH / COND / ORP values
        mainValueContainer = view.findViewById(R.id.layout_top_rows);
        mainRowPh = view.findViewById(R.id.main_value_row_ph);
        mainRowCond = view.findViewById(R.id.main_value_row_cond);
        mainRowOrp = view.findViewById(R.id.main_value_row_orp);

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

        mTitleView = view.findViewById(R.id.tv_title);
        mBtView = view.findViewById(R.id.bt_blue_connect);
        mBtView.setOnClickListener(this);

        iv_menu = view.findViewById(R.id.iv_menu);
        iv_menu.setOnClickListener(this);

        tab1 = view.findViewById(R.id.tab_1);
        tab3 = view.findViewById(R.id.tab_3);
        tab4 = view.findViewById(R.id.tab_4);

        ivTab1 = view.findViewById(R.id.iv_tab_1);
        ivTab3 = view.findViewById(R.id.iv_tab_3);
        ivTab4 = view.findViewById(R.id.iv_tab_4);

        // Tab click → update ViewPager & RadioGroup
        tab1.setOnClickListener(v -> selectTab(0));
        tab3.setOnClickListener(v -> selectTab(1));
        tab4.setOnClickListener(v -> selectTab(2));

        // RadioGroup click → update ViewPager & Tabs
        mRadioGroupSelect.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_1) selectTab(0);
            else if (checkedId == R.id.rb_2) selectTab(1);
            else if (checkedId == R.id.rb_3) selectTab(2);
        });

        buttonSave.setOnClickListener(this);

        bt_set_timer = view.findViewById(R.id.bt_set_timer);
        bt_set_timer.setOnClickListener(this);

        ib_hold = view.findViewById(R.id.ib_hold);
        ib_hold.setOnClickListener(this);
        mLayoutHint.setOnClickListener(this);

        imageViewbg = view.findViewById(R.id.home_bg_imag);

        initViewPager(view);

        int w = getWidth();
        ViewGroup.LayoutParams params = mImageViewGraph.getLayoutParams();
        params.width = w;
        params.height = w;
        mImageViewGraph.setLayoutParams(params);

        modeManager.resetModePopup();
        updateTableView();

        mBitmapAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.bitmap_save);
        mBitmapAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) { }

            @Override
            public void onAnimationEnd(Animation animation) {
                mImageMeasure.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) { }
        });

        updateSetTimer();

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        saveRecordManager = new SaveRecordManager(
                getContext(),
                modePatternManager,
                tv_value_ph, tv_danwei1_ph,
                tv_value_cond, tv_danwei1_cond,
                tv_value_orp, tv_danwei1_orp,
                mTempTextView, mTempUnitTextView
        );

        selectTab(0);
    }


    private LayoutInflater getMyLayoutInflater() {
        if (Build.VERSION.SDK_INT >= 26) {
            return super.getLayoutInflater();
        } else {
            return mInflater;
        }
    }

    private void initViewPager(View rootView) {
        viewPager = rootView.findViewById(R.id.viewpager);

        // Dynamic height: 50% of screen height
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

        final List<View> viewList = new ArrayList<>();
        viewList.add(view1);
        // viewList.add(view2);   // originally commented
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
            public void destroyItem(ViewGroup container, int position, Object object) {
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
            public void onPageScrolled(
                    int position,
                    float positionOffset,
                    int positionOffsetPixels
            ) { }

            @Override
            public void onPageSelected(int position) {
                selectTab(position);   // full sync here
                updateTableView();
            }

            @Override
            public void onPageScrollStateChanged(int state) { }
        });
    }

    private void initView1(View view) {
        mImageViewPoint1 = view.findViewById(R.id.iv_point1);
        mImageViewPoint2 = view.findViewById(R.id.iv_point2);
        mImageViewPoint3 = view.findViewById(R.id.iv_point3);
        mTextViewOffset = view.findViewById(R.id.tv_offset1);
        mTextViewSlop = view.findViewById(R.id.tv_slop1);
        mTextViewCalDate = view.findViewById(R.id.tv_cal_date);
        mViewGroupLastDate = view.findViewById(R.id.layout_last_cailb);
        mViewGroupSlop = view.findViewById(R.id.layout_slope);
        mViewGroupZeroPoint = view.findViewById(R.id.layout_zero_point);
        mViewGroupPoints = view.findViewById(R.id.layout_points);

        mTextViewTempRef = view.findViewById(R.id.tv_temp_ref);
        mViewGroupTempRef = view.findViewById(R.id.layout_temp_ref);

        mTextViewTempCoeffc = view.findViewById(R.id.tv_temp_coeffc);
        mViewGroupTempCoeffc = view.findViewById(R.id.layout_temp_coeff);

        mTextViewSalinity = view.findViewById(R.id.tv_salinity);
        mViewGroupSalinity = view.findViewById(R.id.layout_salinity);

        mTextViewTDS = view.findViewById(R.id.tv_tds);
        mViewGroupTDS = view.findViewById(R.id.layout_tds);
    }

    private void initView2(View view) {
        mImageViewGraph = view.findViewById(R.id.iv_graph);
        mImageViewDial = view.findViewById(R.id.iv_dial);
        mTextViewDialUnit = view.findViewById(R.id.gp_tv_unit);
    }

    private void initView3(View view) {
        graphLineController.initView3(view);
    }

    private void initView4(ViewGroup view) {
        tableController.initView4(view);
    }

    // ---------------------------------------------------------------------
    // Table / pager / auto-save UI
    // ---------------------------------------------------------------------

    private void updateTableView() {
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
            buttonSave.setImageDrawable(
                    getResources().getDrawable(
                            mAutoSaveRun ? R.drawable.btn_auto_save : R.drawable.btn_save
                    )
            );
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
        }
        return 0;
    }

    @Override
    public void onDestroy() {
        closeWakeLock();
        super.onDestroy();
    }

    // ---------------------------------------------------------------------
    // Click handling
    // ---------------------------------------------------------------------

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.ib_hold) {
            onEnter();
        } else if (id == R.id.layout_hint) {
            onClickReminder();
        } else if (id == R.id.bt_blue_connect) {
            btConnect();
        } else if (id == R.id.iv_menu) {
            onMenuClicked();
        } else if (id == R.id.bt_save) {
            onSave();
        } else if (id == R.id.bt_set_timer) {
            onSetTimer();
        }
    }

    // ---------------------------------------------------------------------
    // Mode -> protocol ConventMode wrapper (delegates to ModeManager)
    // ---------------------------------------------------------------------

    private int ConventMode(int type) {
        return modeManager.conventMode(type);
    }

    private void clearHistory() {
        graphLineController.clearHistory();
    }

    // ---------------------------------------------------------------------
    // Graph / Dial / Line / Table update wrappers
    // ---------------------------------------------------------------------

    private void updateViewGraph() {
        graphDialController.updateViewGraph();
    }

    private void setDialValue(double value) {
        graphDialController.setDialValue(value);
    }

    private void updateViewGraph3(double value, double temp) {
        graphLineController.updateViewGraph3(value, temp);
    }

    private void updateViewGraph3Clear() {
        graphLineController.updateViewGraph3Clear();
    }

    private void updateViewGraph3() {
        graphLineController.updateViewGraph3();
    }

    private void updateViewGraph4(String value, String temp) {
        tableController.updateViewGraph4(value, temp);
    }

    private int getAlarmId(Double d, Double t, int n) {
        return alarmManager.getAlarmId(d, t, n);
    }

    // ---------------------------------------------------------------------
    // Title set
    // ---------------------------------------------------------------------

    public void setTitle(String title) {
        // no-op for now
    }

    // ---------------------------------------------------------------------
    // Main updateView – uses managers but still in this class
    // ---------------------------------------------------------------------

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void updateView() {
        super.updateView();
        // Do nothing if no active device
        if (MyApi.getInstance().getBtApi() == null) {
            return;
        }

        if (!MyApi.getInstance().getBtApi().isConnected()) {
            return;
        }

        DataBean bean = MyApi.getInstance().getDataApi().getData();
        if (bean == null) {
            return; // still avoid crash
        }
        new UpdatePipeline().run(bean);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDevicePatternChanged(ModePatternChangedFromDevice e) {
        DataBean bean = MyApi.getInstance().getDataApi().getData();

        new UpdatePipeline().runSync(bean);
    }


    @Override
    public void onStop() {
        super.onStop();
    }

    // ---------------------------------------------------------------------
    // Runnable – delegate body to AutoSaveManager
    // ---------------------------------------------------------------------

    @Override
    public void run() {
        autoSaveManager.onTimerTick();
    }

    // ---------------------------------------------------------------------
    // TextWatcher
    // ---------------------------------------------------------------------

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) { }
    @Override
    public void afterTextChanged(Editable s) { }

    // ---------------------------------------------------------------------
    // App foreground check – used by AlarmManager
    // ---------------------------------------------------------------------

    public boolean isAppOnForeground() {
        ActivityManager activityManager =
                (ActivityManager) getActivity().getApplicationContext()
                        .getSystemService(Context.ACTIVITY_SERVICE);

        String packageName = getActivity().getApplicationContext().getPackageName();
        List<ActivityManager.RunningAppProcessInfo> appProcesses =
                activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }

        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(packageName)
                    && appProcess.importance
                    == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sync selected tab
     * @param index
     */
    private void selectTab(int index) {
        viewPager.setCurrentItem(index, true);

        // Sync RadioGroup
        switch (index) {
            case 0:
                mRadioGroupSelect.check(R.id.rb_1);
                break;
            case 1:
                mRadioGroupSelect.check(R.id.rb_2);
                break;
            case 2:
                mRadioGroupSelect.check(R.id.rb_3);
                break;
        }

        // Sync Tab Icons (highlight selected)
        ivTab1.setAlpha(index == 0 ? 1f : 0.4f);
        ivTab3.setAlpha(index == 1 ? 1f : 0.4f);
        ivTab4.setAlpha(index == 2 ? 1f : 0.4f);
    }


    // ---------------------------------------------------------------------
    // Formatting helpers
    // ---------------------------------------------------------------------

    private String getTraceString(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyymmddHHmmss", Locale.US);
        return simpleDateFormat.format(date);
    }

    private String getDataString(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Constant.DateTimeFormat, Locale.US);
        return simpleDateFormat.format(date);
    }

    // ---------------------------------------------------------------------
    // ================= INTERNAL MANAGER CLASSES ==========================
    // ---------------------------------------------------------------------

    /**
     * Handles mode / graphMode conversions and mode popup.
     */
    private class ModeManager {
        private ModePopupWindow mModePopupWindow;

        int conventMode(int type) {
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

        void resetModePopup() {
            mModePopupWindow = null;
        }

        void syncModeFromDataBean(DataBean bean) {
            if (mModeType != bean.getMode() && bean.getMode() != 0) {
                mNewMode = true;
                mModeType = bean.getMode();
                Log.d(getTAG(), "update mode to " + mModeType);
            }
        }
    }

    // ---------------------------------------------------------------------

    /**
     * Handles mode / graphMode conversions and mode popup.
     */
    private class ModePatternManager {

        private boolean isPhSelected = true;
        private boolean isCondSelected = true;
        private boolean isOrpSelected = true;

        private DataBean.PhMode phMode  = DataBean.PhMode.PH;
        private DataBean.CondMode condMode = DataBean.CondMode.COND;
        private DataBean.OrpMode orpMode   = DataBean.OrpMode.ORP;

        private boolean isUserChange = false;

        // ---------------------------
        // Public getters
        // ---------------------------
        boolean isPhSelected() { return isPhSelected; }
        boolean isCondSelected() { return isCondSelected; }
        boolean isOrpSelected() { return isOrpSelected; }

        DataBean.PhMode getPhMode() { return phMode; }
        DataBean.CondMode getCondMode() { return condMode; }
        DataBean.OrpMode getOrpMode() { return orpMode; }


        // ==========================================================
        // USER triggered changes
        // ==========================================================
        void setSelectedModes(boolean ph, boolean cond, boolean orp) {
            isUserChange = true;

            isPhSelected = ph;
            isCondSelected = cond;
            isOrpSelected = orp;

            saveIntoDataBean(MyApi.getInstance().getDataApi().getData());
            onPatternChanged();
        }

        void setPhSubMode(DataBean.PhMode mode) {
            isUserChange = true;
            phMode = mode;
            saveIntoDataBean(MyApi.getInstance().getDataApi().getData());
            onPatternChanged();
        }

        void setCondSubMode(DataBean.CondMode mode) {
            isUserChange = true;
            condMode = mode;
            saveIntoDataBean(MyApi.getInstance().getDataApi().getData());
            onPatternChanged();
        }

        void setOrpSubMode(DataBean.OrpMode mode) {
            isUserChange = true;
            orpMode = mode;
            saveIntoDataBean(MyApi.getInstance().getDataApi().getData());
            onPatternChanged();
        }


        // ==========================================================
        // DEVICE → APP SYNC
        // (should NOT send commands back!)
        // ==========================================================
        void syncPatternFromDataBean(DataBean bean) {
            if (bean == null) return;

            isUserChange = false; // IMPORTANT

            isPhSelected  = bean.isPhSelected();
            isCondSelected = bean.isCondSelected();
            isOrpSelected  = bean.isOrpSelected();

            phMode  = bean.getPhMode();
            condMode = bean.getCondMode();
            orpMode  = bean.getOrpMode();
        }


        // ==========================================================
        // APPLY changes
        // ==========================================================
        private void onPatternChanged() {

            // Update UI rows
            updateMainValueRowVisibility();

            clearHistory();
            updateViewGraph();
            updateViewGraph3Clear();

            // ONLY send command if USER changed pattern
            if (isUserChange) {
                sendPatternToDevice();
            }
        }


        // ==========================================================
        // SEND VelaParamModeAppToDevice to device
        // ==========================================================
        private void sendPatternToDevice() {

            VelaParamModeAppToDevice msg = new VelaParamModeAppToDevice();

            // PH / MV
            msg.setPhmv(
                    isPhSelected,
                    (phMode == DataBean.PhMode.MV
                            ? VelaParamModeAppToDevice.PhMvMode.MV
                            : VelaParamModeAppToDevice.PhMvMode.PH)
            );

            // COND / TDS / SAL / RES
            VelaParamModeAppToDevice.CondMode cMode;
            switch (condMode) {
                case TDS:  cMode = VelaParamModeAppToDevice.CondMode.TDS; break;
                case SAL:  cMode = VelaParamModeAppToDevice.CondMode.SALT; break;
                case RES:  cMode = VelaParamModeAppToDevice.CondMode.RESISTIVITY; break;
                default:   cMode = VelaParamModeAppToDevice.CondMode.COND;
            }

            msg.setCond(isCondSelected, cMode);

            // ORP
            msg.setOrp(isOrpSelected);

            // SEND COMMAND
            MyApi.getInstance().getBtApi().sendCommand(msg);

            Log.d("MODE_PATTERN", "Sent VelaParamModeAppToDevice: " + msg.getData());

            isUserChange = false; // reset
        }


        // ==========================================================
        // Save into DataBean
        // ==========================================================
        private void saveIntoDataBean(DataBean bean) {
            if (bean == null) return;

            bean.setPhSelected(isPhSelected);
            bean.setCondSelected(isCondSelected);
            bean.setOrpSelected(isOrpSelected);

            bean.setPhMode(phMode);
            bean.setCondMode(condMode);
            bean.setOrpMode(orpMode);
        }
    }

    /**
     * SaveRecordManager for Vela multi-mode system.
     * Saves: PH + COND/TDS/SAL/RES + ORP + TEMP
     * -> Into ONE DataBean row in DB
     * -> Using DataApi.saveData(sn, traceNo, attachJson)
     */
    public class SaveRecordManager {

        private final Context context;
        private final ModePatternManager modePattern;

        // --- Vela UI fields ---
        private final TextView tvValuePh, tvUnitPh;
        private final TextView tvValueCond, tvUnitCond;
        private final TextView tvValueOrp, tvUnitOrp;
        private final TextView tvTemp, tvTempUnit;

        private int sn = 0;             // Sequence counter
        private final String traceNo;   // One traceNo per session/save group

        public SaveRecordManager(
                Context ctx,
                ModePatternManager pattern,
                TextView tvValuePh, TextView tvUnitPh,
                TextView tvValueCond, TextView tvUnitCond,
                TextView tvValueOrp, TextView tvUnitOrp,
                TextView tvTemp, TextView tvTempUnit
        ) {
            this.context = ctx;
            this.modePattern = pattern;

            this.tvValuePh = tvValuePh;
            this.tvUnitPh = tvUnitPh;

            this.tvValueCond = tvValueCond;
            this.tvUnitCond = tvUnitCond;

            this.tvValueOrp = tvValueOrp;
            this.tvUnitOrp = tvUnitOrp;

            this.tvTemp = tvTemp;
            this.tvTempUnit = tvTempUnit;

            // Same behavior as legacy → one traceNo per session
            this.traceNo = "T" + System.currentTimeMillis();
        }

        // ------------------------------------------------------------------
        // MAIN ENTRY: SAVE RECORD
        // ------------------------------------------------------------------
        public void saveRecord() {

            JSONObject json = new JSONObject();

            try {
                // -----------------------------------------------------
                // PH block
                // -----------------------------------------------------
                if (modePattern.isPhSelected()) {
                    json.put("ph_value", tvValuePh.getText().toString());
                    json.put("ph_unit", tvUnitPh.getText().toString());
                    json.put("ph_mode", modePattern.getPhMode().name()); // PH / MV
                }

                // -----------------------------------------------------
                // Conductivity/TDS/Salt/Resistivity block
                // -----------------------------------------------------
                if (modePattern.isCondSelected()) {
                    json.put("cond_value", tvValueCond.getText().toString());
                    json.put("cond_unit", tvUnitCond.getText().toString());
                    json.put("cond_mode", modePattern.getCondMode().name()); // COND / TDS / SAL / RES
                }

                // -----------------------------------------------------
                // ORP block
                // -----------------------------------------------------
                if (modePattern.isOrpSelected()) {
                    json.put("orp_value", tvValueOrp.getText().toString());
                    json.put("orp_unit", tvUnitOrp.getText().toString());
                    json.put("orp_mode", "ORP");
                }

                // -----------------------------------------------------
                // Temperature
                // -----------------------------------------------------
                json.put("temp_value", tvTemp.getText().toString());
                json.put("temp_unit", tvTempUnit.getText().toString());

                // -----------------------------------------------------
                // Metadata (matching original system)
                // -----------------------------------------------------
                json.put("timestamp", System.currentTimeMillis());
                json.put("traceNo", traceNo);

            } catch (Exception e) {
                e.printStackTrace();
            }

            // ------------------------------------------------------------------
            // WRITE TO DATABASE (legacy-compatible)
            // ------------------------------------------------------------------
            MyApi.getInstance()
                    .getDataApi()
                    .saveData(sn++, traceNo, json.toJSONString());

            Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show();
        }
    }

    // ---------------------------------------------------------------------

    /**
     * Manages auto-save toggle, timer, and run() behaviour.
     */
    private class AutoSaveManager {
        void onSave() {
            if (!mAutoSave) {
                if (true) {  // permission check stub
                    mTraceNo = getTraceString(new Date())
                            + Integer.toHexString(
                            (MeasureFragmentVela.this.hashCode() * 100
                                    + random.nextInt(100)) * 100
                                    + atomicInteger.addAndGet(1)
                    );
                    saveRecordManager.saveRecord();
                    EventBus.getDefault().post(new SyncEventUpload());
                    mTraceNo = null;
                } else {
                    return;
                }
            } else {
                if (mAutoSaveRun) {
                    MyApi.getInstance().getBtApi().sendCommand(new Measure(Measure.OFF));
                    closeWakeLock();
                    EventBus.getDefault().post(new SyncEventUpload());
                    mAutoSaveRun = false;
                    buttonSave.setImageDrawable(
                            getResources().getDrawable(R.drawable.btn_save)
                    );
                    mTraceNo = null;
                } else {
                    if (true) { // permission stub
                        mAutoSaveRun = true;
                        mTraceNo = getTraceString(new Date())
                                + Integer.toHexString(MeasureFragmentVela.this.hashCode());
                        mSn = 0;
                        clearHistory();
                        MyApi.getInstance().getBtApi().sendCommand(
                                new Measure(Measure.ContinuousMeasureON)
                        );
                        openWakeLock();
                        buttonSave.setImageDrawable(
                                getResources().getDrawable(R.drawable.btn_auto_save)
                        );
                        mHandler.postDelayed(MeasureFragmentVela.this, 1 * 1000);
                        EventBus.getDefault().post(new SyncEventUpload());
                    } else {
                        // request permission if needed
                    }
                }
            }
        }

        boolean isAutoSave() {
            return mAutoSaveRun && mAutoSave;
        }

        void startTimer() {
            if (mAutoSaveRun) {
                mHandler.postDelayed(MeasureFragmentVela.this, mTimer);
            }
        }

        void onSetTimer() {
            if (pvOptions == null) {
                initOptionData();
                initOptionPicker();
            }
            pvOptions.show();
        }

        void updateSetTimer() {
            if (mTextViewSetTimer == null) return;
            if (mTimer >= 60 * 1000) {
                mTextViewSetTimer.setText(String.format("%dM", mTimer / (60 * 1000)));
            } else {
                mTextViewSetTimer.setText(String.format("%dS", mTimer / 1000));
            }
        }

        void onTimerTick() {
            if (mAutoSaveRun) {
                startTimer();
                try {
                    updateViewGraph3(mCurrentValue, mCurrentTemp);

                    String value = mValueTextView.getText().toString() + " " +
                            mDanweiTextView.getText().toString();
                    String temp1 = mTempTextView.getText().toString() + " " +
                            mTempUnitTextView.getText().toString();
                    String temp2 = mTempTextView.getText().toString() + " " + mTempUnitStr;

                    updateViewGraph4(value, temp1);

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("value", value);
                    jsonObject.put("temp", temp2);
                    try {
                        jsonObject.put("deviceNumber",
                                MyApi.getInstance().getBtApi().getDeviceNumber());
                        jsonObject.put("location",
                                MyApi.getInstance().getDataApi().getLocation());
                        jsonObject.put("tempUnit", mTempUnitStr);

                        if (MyApi.getInstance().getBtApi().getLastDevice() != null
                                && MyApi.getInstance().getBtApi().getLastDevice().getSetting() != null) {

                            if (mModeType == Constant.MODE_PH) {
                                if (MyApi.getInstance().getBtApi().getLastDevice().getSetting()
                                        .getCalibrationPh() != null) {
                                    try {
                                        Calibration calibration =
                                                MyApi.getInstance().getBtApi().getLastDevice()
                                                        .getSetting().getCalibration();
                                        calibration.setMode("PH");
                                        calibration.setRefTemp(
                                                MyApi.getInstance().getDataApi().getLastParm().getRefTemp()
                                        );
                                        calibration.setTempCompensate(
                                                MyApi.getInstance().getDataApi().getLastParm().getTempCompensate()
                                        );
                                    } catch (NullPointerException e) {
                                        e.printStackTrace();
                                    }
                                    jsonObject.put("calibration",
                                            MyApi.getInstance().getBtApi().getLastDevice()
                                                    .getSetting().getCalibrationJson());
                                }
                            } else if (mModeType == Constant.MODE_COND) {
                                if (MyApi.getInstance().getBtApi().getLastDevice().getSetting()
                                        .getCalibrationCond() != null) {
                                    try {
                                        Calibration calibration =
                                                MyApi.getInstance().getBtApi().getLastDevice()
                                                        .getSetting().getCalibration();
                                        calibration.setMode("COND");
                                        calibration.setRefTemp(
                                                MyApi.getInstance().getDataApi().getLastParm().getRefTemp()
                                        );
                                        calibration.setTempCompensate(
                                                MyApi.getInstance().getDataApi().getLastParm().getTempCompensate()
                                        );
                                    } catch (NullPointerException e) {
                                        e.printStackTrace();
                                    }
                                    jsonObject.put("calibration",
                                            MyApi.getInstance().getBtApi().getLastDevice()
                                                    .getSetting().getCalibrationJson());
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    MyApi.getInstance().getDataApi().saveData(
                            mSn++,
                            mTraceNo,
                            jsonObject.toJSONString()
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        private void initOptionData() {
            ArrayList<String> options2Items_01 = new ArrayList<>();
            options2Items_01.add(getString(R.string.seconds));
            options2Items_01.add(getString(R.string.minutes));

            for (int i = 0; i < 60; i++) {
                options1Items.add(
                        new TimeBean(i, "" + (i + 1), (i + 1))
                );
                options2Items.add(options2Items_01);
            }
        }

        private void initOptionPicker() {
            int opt2 = mTimer > 60 * 1000 ? 1 : 0;
            int opt1 = (int) (mTimer / (1000 * (opt2 == 1 ? 60 : 1))) - 1;
            if (opt1 >= options1Items.size()) opt1 = options1Items.size() - 1;

            pvOptions = new OptionsPickerView.Builder(
                    getContext(),
                    (options1, options2, options3, v) -> {
                        String tx = options1Items.get(options1).getPickerViewText()
                                + options2Items.get(options1).get(options2);

                        mTimer = 1000L
                                * options1Items.get(options1).getValue()
                                * (options2 == 1 ? 60 : 1);

                        mSharedPreferences.edit().putLong("mTimer", mTimer).apply();
                        updateSetTimer();
                        Log.i(getTAG(), tx + " mTimer=" + mTimer);
                    }
            )
                    .setTitleText(getString(R.string.timer_set))
                    .setContentTextSize(20)
                    .setDividerColor(Color.LTGRAY)
                    .setSelectOptions(opt1, opt2)
                    .setBgColor(Color.WHITE)
                    .setTitleBgColor(Color.WHITE)
                    .setTitleColor(Color.BLACK)
                    .setCancelColor(Color.WHITE)
                    .setSubmitColor(getResources().getColor(R.color.colorPrimary))
                    .setTextColorCenter(Color.LTGRAY)
                    .isCenterLabel(false)
                    .setLabels("", "", "")
                    .setBackgroundId(0x66000000)
                    .build();

            pvOptions.setPicker(options1Items, options2Items);
        }
    }

    // ---------------------------------------------------------------------

    /**
     * Manages screenshot bitmap saving & SaveImageDialog.
     */
    private class SaveViewManager {
        void saveView() {
            mLayoutMeasure.setDrawingCacheEnabled(true);
            Bitmap bitmap = saveBitmap(mLayoutMeasure.getDrawingCache());
            mLayoutMeasure.setDrawingCacheEnabled(false);

            mImageMeasure.setImageBitmap(bitmap);
            mImageMeasure.setVisibility(View.VISIBLE);
            mImageMeasure.startAnimation(mBitmapAnimation);
        }

        Bitmap saveBitmap(Bitmap bitmap) {
            File fileDir = getContext().getExternalFilesDir("save");
            Log.i(getTAG(), "saveBitmap dir " + fileDir.getPath());
            File f = new File(fileDir,
                    mTraceNo + Long.toHexString(System.currentTimeMillis()) + ".png");
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
                    record.setTempUnit(mTempUnitStr);
                    if (MyApi.getInstance().getBtApi().getLastDevice() != null
                            && MyApi.getInstance().getBtApi().getLastDevice().getSetting() != null) {
                        if (mModeType == Constant.MODE_PH) {
                            if (MyApi.getInstance().getBtApi().getLastDevice().getSetting()
                                    .getCalibrationPh() != null) {
                                record.setCalibration(
                                        MyApi.getInstance().getBtApi().getLastDevice().getSetting()
                                                .getCalibrationPh().toString()
                                );
                            }
                        } else if (mModeType == Constant.MODE_COND) {
                            if (MyApi.getInstance().getBtApi().getLastDevice().getSetting()
                                    .getCalibrationCond() != null) {
                                record.setCalibration(
                                        MyApi.getInstance().getBtApi().getLastDevice().getSetting()
                                                .getCalibrationCond().toString()
                                );
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                MyApi.getInstance().getDataApi().saveRecord(record);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return outBitmap;
        }

        void showDialog() {
            Bundle bundle = new Bundle();
            bundle.putBoolean(SaveImageDialog.DIALOG_BACK, true);
            bundle.putBoolean(SaveImageDialog.DIALOG_CANCELABLE_TOUCH_OUT_SIDE, true);

            saveDialogFragment = SaveImageDialog.newInstance(SaveImageDialog.class, bundle);
            saveDialogFragment.show(
                    getFragmentManager(),
                    SaveImageDialog.class.getName()
            );
            saveDialogFragment.setOnConfirmClcik(new SaveImageDialog.IonConfirmClick() {
                @Override
                public void onClcik(int type) {
                    Record record = MyApi.getInstance().getDataApi().getRecords().get(0);
                    long ID = record.getId();

                    if (type == 3) {
                        CategoryActivity.showMe(getContext(), ID);
                    }

                    if (type == 4) {
                        String id = MyApi.getInstance().getDataApi().setDelRecord(ID);
                        int ret = MyApi.getInstance().getRestApi()
                                .dataDelete(id, record.getType());
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
                public void onClickStr(String string) { }
            });
        }
    }

    // ---------------------------------------------------------------------

    /**
     * Handles alarm thresholds, unit conversion, and “alarm background” UI.
     */
    private class AlarmManager {

        void loadAlarm() {
            if (MyApi.getInstance().getCurrentDeviceMac() == null) return;

            SharedPreferences sharedPreferences =
                    MyApi.getInstance().getDataApi().getSetting();

            String ph_selection = sharedPreferences.getString(
                    SettingConfig.ph_selection_SAVE_ID,
                    "USA"
            );
            if ("USA".equals(ph_selection)) {
                pHSelect = ParmUp.USA;
            } else {
                pHSelect = ParmUp.NIST;
            }

            int i = 0;
            for (String settingIndex : Constant.ALARMs) {
                String str = sharedPreferences.getString(
                        settingIndex,
                        Constant.DEFAULT_ALARMs[i]
                );
                DeviceSetting.Param param =
                        JSON.parseObject(str, DeviceSetting.Param.class);

                String lowValue = param.getString(Constant.lowValue);
                boolean lowSwitchValue =
                        (Constant.ON.equals(param.getString(Constant.lowSwitchValue)));

                String highValue = param.getString(Constant.highValue);
                boolean highSwitchValue =
                        (Constant.ON.equals(param.getString(Constant.highSwitchValue)));

                try {
                    alarmLSwitchMap.put(settingIndex, lowSwitchValue);
                    alarmLMap.put(
                            settingIndex,
                            lowSwitchValue ? Double.parseDouble(lowValue) : 0
                    );
                } catch (NumberFormatException e) {
                    alarmLMap.put(settingIndex, 0.0);
                }
                try {
                    alarmHSwitchMap.put(settingIndex, highSwitchValue);
                    alarmHMap.put(
                            settingIndex,
                            highSwitchValue ? Double.parseDouble(highValue) : 0
                    );
                } catch (NumberFormatException e) {
                    alarmHMap.put(settingIndex, 0.0);
                }
                i++;
            }
        }

        private double getAllAlarmValue(Map<String, Double> map, String key) {
            try {
                return map.get(key);
            } catch (Exception e) {
                return 0;
            }
        }

        private boolean getAlarmSwitch(Map<String, Boolean> map, String key) {
            Boolean ret = map.get(key);
            return ret != null && ret;
        }

        private double getAlarmValue(Map<String, Double> map, String key) {
            try {
                Double v = map.get(key);
                if (v == null) return 0;

                if ("ppm".equals(mhighValueUnit) || "ppm".equals(mlowValueUnit)) {
                    if ("ppm".equals(mCurrentUnit)) {
                        return v / 5;
                    } else {
                        return v / 50;
                    }
                }

                if ("ppt".equals(mhighValueUnit) || "ppt".equals(mlowValueUnit)) {
                    if ("ppm".equals(mCurrentUnit)) {
                        return v * 200;
                    } else {
                        return v * 20;
                    }
                }

                if ("µS/cm".equals(mhighValueUnit) || "µS/cm".equals(mlowValueUnit)) {
                    if ("mS".equals(mCurrentUnit)) {
                        return v / 100;
                    } else if ("µS".equals(mCurrentUnit)) {
                        return v;
                    }
                }
                if ("mS/cm".equals(mhighValueUnit) || "mS/cm".equals(mlowValueUnit)) {
                    if ("mS".equals(mCurrentUnit)) {
                        return v * 10;
                    } else if ("µS".equals(mCurrentUnit)) {
                        return v * 1000;
                    }
                }

                if ("Ω·cm".equals(mhighValueUnit) || "Ω·cm".equals(mlowValueUnit)) {
                    if ("Ω".equals(mCurrentUnit)) {
                        return v / 5;
                    } else if ("KΩ".equals(mCurrentUnit)) {
                        return v / 500;
                    } else {
                        return v / 100000;
                    }
                }

                if ("KΩ·cm".equals(mhighValueUnit) || "KΩ·cm".equals(mlowValueUnit)) {
                    if ("Ω".equals(mCurrentUnit)) {
                        return v * 200;
                    } else if ("KΩ".equals(mCurrentUnit)) {
                        return v * 2;
                    } else {
                        return v / 100;
                    }
                }

                if ("MΩ·cm".equals(mhighValueUnit) || "MΩ·cm".equals(mlowValueUnit)) {
                    if ("Ω".equals(mCurrentUnit)) {
                        return v * 500000;
                    } else if ("KΩ".equals(mCurrentUnit)) {
                        return v * 2000;
                    } else {
                        return v * 10;
                    }
                }

                if (v > fullValue) {
                    return (mSweepAngle * fullValue / fullValue);
                } else if (v < lowValue) {
                    return (mSweepAngle * lowValue / fullValue);
                } else {
                    return (mSweepAngle * v / fullValue);
                }
            } catch (Exception e) {
                return 0;
            }
        }

        int getAlarmId(Double d, Double t, int n) {
            try {
                String key = Constant.ALARMs[n];

                if (getAllAlarmValue(alarmHMap, key) == 0
                        && getAllAlarmValue(alarmLMap, key) == 0) {
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

        void updateAlarmBackground(int alarmId) {
            if (Reid == 3) {
                Reid = 0;
                HomeActivity activity = (HomeActivity) getActivity();
                ImageView imageView = activity.findViewById(R.id.home_bg_tipimg);
                imageView.setImageResource(R.mipmap.vela_bg);
                imageViewbg.setImageResource(R.mipmap.vela_bg);
                return;
            }

            if (alarmId == 2) {
                imageViewbg.setImageResource(R.mipmap.alarmbg);
                HomeActivity activity = (HomeActivity) getActivity();
                ImageView imageView = activity.findViewById(R.id.home_bg_tipimg);
                imageView.setImageResource(R.mipmap.alarmbg);
                if (alarmbtid == 1) {
                    relativeLayoutdia.setVisibility(View.INVISIBLE);
                } else {
                    relativeLayoutdia.setVisibility(View.VISIBLE);
                }

                if (!isAppOnForeground()) {
                    if (NotiTime == 60) {
                        NotiTime = 0;
                        NotificationChannel channel = new NotificationChannel(
                                "localService",
                                "告警",
                                NotificationManager.IMPORTANCE_NONE
                        );
                        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
                        NotificationManager notificationManager =
                                (NotificationManager) getContext()
                                        .getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.createNotificationChannel(channel);
                        NotificationCompat.Builder bBuilder =
                                new NotificationCompat.Builder(
                                        getContext(), "localService");
                        Notification notification = bBuilder.setOngoing(true)
                                .setContentTitle(getString(R.string.alarm))
                                .setContentText(getString(R.string.Alerm_text))
                                .setWhen(System.currentTimeMillis())
                                .setSmallIcon(R.drawable.ic_launchertwo)
                                .setStyle(new NotificationCompat.BigTextStyle())
                                .setLargeIcon(BitmapFactory.decodeResource(
                                        getResources(),
                                        R.drawable.ic_launchersmall))
                                .build();
                        notificationManager.notify(30001, notification);
                    } else {
                        NotiTime++;
                    }
                }
            }

            if (alarmId == 1) {
                imageViewbg.setImageResource(R.mipmap.vela_bg);
                HomeActivity activity = (HomeActivity) getActivity();
                ImageView imageView = activity.findViewById(R.id.home_bg_tipimg);
                imageView.setImageResource(R.mipmap.vela_bg);
            }
            Reid++;
        }
    }

    // ---------------------------------------------------------------------

    /**
     * Manages dial / gauge drawing and alarm overlay paths.
     */
    private class GraphDialController {

        void setDialValue(double value) {
            if (fullValue > 0) {
                float rotation;
                if (value < lowValue) {
                    rotation = (float) (mSweepAngle * lowValue / fullValue
                            - (mSweepAngle / 2) + mZeroAngle);
                } else if (value > fullValue) {
                    rotation = (float) (mSweepAngle * fullValue / fullValue
                            - (mSweepAngle / 2) + mZeroAngle);
                } else {
                    rotation = (float) (mSweepAngle * value / fullValue
                            - (mSweepAngle / 2) + mZeroAngle);
                }
                mImageViewDial.setRotation(rotation);
            }
        }

        void updateViewGraph() {
            SharedPreferences sharedPreferences =
                    MyApi.getInstance().getDataApi().getSetting();
            String str = "";
            double alarmL = 0;
            double alarmH = 0;
            boolean alarmHSwitch = false;
            boolean alarmLSwitch = false;
            String key = "";
            int sweep = 0;

            switch (mGraphModeType) {
                case Constant.MODE_PH:
                    mMyDashBoardView2.setMode(MyDashBoardView2.MODE_OVER);
                    mMyDashBoardView2.setImageResource(R.mipmap.graph_ph);
                    fullValue = 18;
                    lowValue = -2;
                    mLineView.setRangeY(16);
                    mSweepAngle = 180;
                    mZeroAngle = 20;
                    key = Constant.ALARMs[0];
                    str = sharedPreferences.getString(
                            key,
                            Constant.DEFAULT_ALARMs[0]
                    );

                    alarmLSwitch = alarmManager.getAlarmSwitch(alarmLSwitchMap, key);
                    alarmL = alarmLSwitch
                            ? alarmManager.getAlarmValue(alarmLMap, key)
                            : lowValue;
                    mMyDashBoardView2.setPathL(
                            (int) (270 - mSweepAngle / 2),
                            alarmLSwitch ? (int) (alarmL + mZeroAngle) : 0
                    );

                    alarmHSwitch = alarmManager.getAlarmSwitch(alarmHSwitchMap, key);
                    alarmH = alarmManager.getAlarmValue(alarmHMap, key);
                    sweep = alarmHSwitch
                            ? (int) (mSweepAngle - mZeroAngle - alarmH)
                            : 0;
                    mMyDashBoardView2.setPathH(
                            (int) (270 + mSweepAngle / 2 - sweep),
                            sweep
                    );
                    break;

                case Constant.MODE_RES:
                    mMyDashBoardView2.setMode(MyDashBoardView2.MODE_ATOP);
                    if ("Ω".equals(mCurrentUnit)) {
                        mMyDashBoardView2.setImageResource(R.mipmap.graph_ris_2);
                        fullValue = 1000;
                    } else if ("KΩ".equals(mCurrentUnit)) {
                        if (mCurrentValue < 100) {
                            mMyDashBoardView2.setImageResource(R.mipmap.graph_ris_1);
                            fullValue = 100;
                        } else {
                            mMyDashBoardView2.setImageResource(R.mipmap.graph_ris_2);
                            fullValue = 1000;
                        }
                    } else if ("MΩ".equals(mCurrentUnit)) {
                        mMyDashBoardView2.setImageResource(R.mipmap.graph_ris_3);
                        fullValue = 20;
                    } else {
                        mMyDashBoardView2.setImageResource(R.mipmap.graph_ris_1);
                        fullValue = 100;
                    }
                    lowValue = 0;
                    mLineView.setRangeY(fullValue);
                    mSweepAngle = 200;
                    mZeroAngle = 0;
                    key = Constant.ALARMs[5];
                    str = sharedPreferences.getString(
                            key,
                            Constant.DEFAULT_ALARMs[5]
                    );

                    alarmLSwitch = alarmManager.getAlarmSwitch(alarmLSwitchMap, key);
                    alarmL = alarmLSwitch
                            ? alarmManager.getAlarmValue(alarmLMap, key)
                            : lowValue;
                    mMyDashBoardView2.setPathL(
                            270 - mSweepAngle / 2,
                            (int) alarmL
                    );
                    alarmHSwitch = alarmManager.getAlarmSwitch(alarmHSwitchMap, key);
                    alarmH = alarmManager.getAlarmValue(alarmHMap, key);
                    sweep = alarmHSwitch
                            ? (int) (mSweepAngle - mZeroAngle - alarmH)
                            : 0;
                    mMyDashBoardView2.setPathH(
                            (int) (mZeroAngle + 270 + mSweepAngle / 2 - sweep),
                            sweep
                    );
                    break;

                case Constant.MODE_COND:
                    mMyDashBoardView2.setMode(MyDashBoardView2.MODE_ATOP);
                    if ("mS".equals(mCurrentUnit)) {
                        mMyDashBoardView2.setImageResource(R.mipmap.graph_cond_3);
                        fullValue = 20;
                    } else if ("µS".equals(mCurrentUnit)) {
                        if (mCurrentValue < 200.0f) {
                            mMyDashBoardView2.setImageResource(R.mipmap.graph_cond_1);
                            fullValue = 200;
                        } else {
                            mMyDashBoardView2.setImageResource(R.mipmap.graph_cond_2);
                            fullValue = 2000;
                        }
                    } else {
                        mMyDashBoardView2.setImageResource(R.mipmap.graph_cond_1);
                        fullValue = 200;
                    }
                    lowValue = 0;
                    mLineView.setRangeY(fullValue);
                    mSweepAngle = 200;
                    mZeroAngle = 0;
                    key = Constant.ALARMs[2];
                    str = sharedPreferences.getString(
                            key,
                            Constant.DEFAULT_ALARMs[2]
                    );

                    alarmLSwitch = alarmManager.getAlarmSwitch(alarmLSwitchMap, key);
                    alarmL = alarmLSwitch
                            ? alarmManager.getAlarmValue(alarmLMap, key)
                            : lowValue;
                    mMyDashBoardView2.setPathL(
                            270 - mSweepAngle / 2,
                            (int) alarmL
                    );
                    alarmHSwitch = alarmManager.getAlarmSwitch(alarmHSwitchMap, key);
                    alarmH = alarmManager.getAlarmValue(alarmHMap, key);
                    sweep = alarmHSwitch
                            ? (int) (mSweepAngle - mZeroAngle - alarmH)
                            : 0;
                    mMyDashBoardView2.setPathH(
                            (int) (mZeroAngle + 270 + mSweepAngle / 2 - sweep),
                            sweep
                    );
                    break;

                case Constant.MODE_SAL:
                    mMyDashBoardView2.setMode(MyDashBoardView2.MODE_ATOP);
                    if (mCurrentValue < 10.0f) {
                        mMyDashBoardView2.setImageResource(R.mipmap.graph_sal);
                        fullValue = 10;
                    } else {
                        mMyDashBoardView2.setImageResource(R.mipmap.graph_sal_sea);
                        fullValue = 1000;
                    }
                    lowValue = 0;
                    mLineView.setRangeY(fullValue);
                    mSweepAngle = 200;
                    mZeroAngle = 0;
                    key = Constant.ALARMs[3];
                    str = sharedPreferences.getString(
                            key,
                            Constant.DEFAULT_ALARMs[3]
                    );

                    alarmLSwitch = alarmManager.getAlarmSwitch(alarmLSwitchMap, key);
                    alarmL = alarmLSwitch
                            ? alarmManager.getAlarmValue(alarmLMap, key)
                            : lowValue;
                    mMyDashBoardView2.setPathL(
                            270 - mSweepAngle / 2,
                            (int) alarmL
                    );
                    alarmHSwitch = alarmManager.getAlarmSwitch(alarmHSwitchMap, key);
                    alarmH = alarmManager.getAlarmValue(alarmHMap, key);
                    sweep = alarmHSwitch
                            ? (int) (mSweepAngle - mZeroAngle - alarmH)
                            : 0;
                    mMyDashBoardView2.setPathH(
                            (int) (mZeroAngle + 270 + mSweepAngle / 2 - sweep),
                            sweep
                    );
                    break;

                case Constant.MODE_TDS:
                    mMyDashBoardView2.setMode(MyDashBoardView2.MODE_ATOP);
                    if (mCurrentValue < 10.0f) {
                        mMyDashBoardView2.setImageResource(R.mipmap.graph_tds_2);
                        fullValue = 10;
                    } else {
                        mMyDashBoardView2.setImageResource(R.mipmap.graph_tds_1);
                        fullValue = 1000;
                    }
                    lowValue = 0;
                    mLineView.setRangeY(fullValue);
                    mSweepAngle = 200;
                    mZeroAngle = 0;
                    key = Constant.ALARMs[4];
                    str = sharedPreferences.getString(
                            key,
                            Constant.DEFAULT_ALARMs[4]
                    );
                    alarmLSwitch = alarmManager.getAlarmSwitch(alarmLSwitchMap, key);
                    alarmL = alarmLSwitch
                            ? alarmManager.getAlarmValue(alarmLMap, key)
                            : lowValue;
                    mMyDashBoardView2.setPathL(
                            270 - mSweepAngle / 2,
                            (int) alarmL
                    );
                    alarmHSwitch = alarmManager.getAlarmSwitch(alarmHSwitchMap, key);
                    alarmH = alarmManager.getAlarmValue(alarmHMap, key);
                    sweep = alarmHSwitch
                            ? (int) (mSweepAngle - mZeroAngle - alarmH)
                            : 0;
                    mMyDashBoardView2.setPathH(
                            (int) (mZeroAngle + 270 + mSweepAngle / 2 - sweep),
                            sweep
                    );
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
                    str = sharedPreferences.getString(
                            key,
                            Constant.DEFAULT_ALARMs[1]
                    );
                    alarmLSwitch = alarmManager.getAlarmSwitch(alarmLSwitchMap, key);
                    alarmL = alarmLSwitch
                            ? alarmManager.getAlarmValue(alarmLMap, key)
                            : lowValue;
                    mMyDashBoardView2.setPathL(
                            270 - mSweepAngle / 2,
                            (int) (alarmL + mZeroAngle)
                    );
                    alarmHSwitch = alarmManager.getAlarmSwitch(alarmHSwitchMap, key);
                    alarmH = alarmManager.getAlarmValue(alarmHMap, key);
                    sweep = alarmHSwitch
                            ? (int) (mSweepAngle - mZeroAngle - alarmH)
                            : 0;
                    mMyDashBoardView2.setPathH(
                            (int) (270 + mSweepAngle / 2 - sweep),
                            sweep
                    );
                    break;
            }

            DeviceSetting.Param param =
                    JSON.parseObject(str, DeviceSetting.Param.class);
            if (param == null) return;

            mlowValueUnit = param.getString(Constant.lowValueUnit);
            mhighValueUnit = param.getString(Constant.highValueUnit);

            mImageViewGraph.setImageBitmap(mMyDashBoardView2.getBitmap());
            double current = mSweepAngle * mCurrentValue / fullValue;

            boolean flagAlarmL = alarmLSwitch ? (current < alarmL) : false;
            boolean flagAlarmH = alarmHSwitch ? (current > alarmH) : false;
            mAlarmDirty = mAlarmDirty
                    || flagAlarmL != mAlarmL
                    || flagAlarmH != mAlarmH;

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
    }

    // ---------------------------------------------------------------------

    /**
     * Manages line chart and its data buffers.
     */
    private class GraphLineController {

        void initView3(View view) {
            lineLeftTextView = view.findViewById(R.id.tv_left);
            lineRightTextView = view.findViewById(R.id.tv_right);
            final LineView lineView = view.findViewById(R.id.line_view);

            lineView.setOnTouchListener(
                    new OnDoubleClickListener(() -> {
                        if (lineZoom == 1) {
                            lineZoom = 16;
                        } else {
                            lineZoom = 1;
                        }
                        Log.i("Zoom", "onDoubleClick  lineZoom " + lineZoom);
                        lineView.zoom(1.0f / lineZoom);
                        updateViewGraph3Clear();
                    })
            );

            lineView.setGridCount(5);
            lineView.setWidth(getWidth());
            lineView.setSideLineLength(getWidth() * 100 / 750);
            lineView.setDrawDotLine(false);

            ArrayList<String> strList = new ArrayList<>();
            SimpleDateFormat formatter1 =
                    new SimpleDateFormat(Constant.TimeFormat, Locale.US);
            long t = System.currentTimeMillis();
            strList.add(formatter1.format(new Date(t)));
            for (int i = 0; i < mLineDateSize; i++) {
                strList.add("");
            }

            lineView.setBottomTextList(strList);
            lineView.setColorArray(new int[]{
                    Color.parseColor("#1ab9f0"),
                    Color.parseColor("#a9e65d"),
                    Color.GRAY,
                    Color.CYAN
            });

            List<List<Integer>> dataLists = new ArrayList<>();
            List<Integer> dataList = new ArrayList<>();
            for (int i = 0; i < mLineDateSize; i++) {
                dataList.add(0);
            }
            ArrayList<Integer> dataList2 = new ArrayList<>();
            for (int i = 0; i < mLineDateSize; i++) {
                dataList2.add(0);
            }
            dataLists.add(dataList);
            dataLists.add(dataList2);

            lineView.setDataList(dataLists);
            mLineView = lineView;
        }

        void clearHistory() {
            mLineView.setHorizontalGridNum(mLineDateSize);
            mOrgDataList1.clear();
            mOrgDataList2.clear();
            mDataList1.clear();
            mDataList2.clear();
            mBottomTextList.clear();
            mOrgBottomTextList.clear();
            if (mMeasureDataSimpleAdapter != null) {
                mMeasureDataSimpleAdapter.getData().clear();
                mMeasureDataSimpleAdapter.notifyDataSetChanged();
            }
            mAlarmH = false;
            mAlarmL = false;
            mAlarmDirty = true;
        }

        void updateViewGraph3(double value, double temp) {
            mUpdateViewGraph3 = false;
            ArrayList<List<Float>> dataLists = new ArrayList<>();
            mOrgDataList1.add((float) value);
            mOrgDataList2.add((float) temp);
            mDataList1.add((float) value);
            mDataList2.add((float) temp);

            SimpleDateFormat formatter1 =
                    new SimpleDateFormat(Constant.TimeFormat, Locale.US);
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

            if (!isAutoSave()) {
                if (mDataList1.size() > mLineDateSize) {
                    mDataList1.remove(0);
                }
                if (mDataList2.size() > mLineDateSize) {
                    mDataList2.remove(0);
                }
                if (mBottomTextList.size() > mLineDateSize) {
                    mBottomTextList.remove(0);
                }
            }

            if (lineZoom == 1) {
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

                for (float i = 0; i < len; i = (i + m)) {
                    if (dataList1.size() >= dataSize) break;
                    bottomTextList.add(mOrgBottomTextList.get((int) i));
                    dataList1.add(mOrgDataList1.get((int) i));
                    dataList2.add(mOrgDataList2.get((int) i));
                }

                mLineView.setBottomTextList2(bottomTextList);
                dataLists.add(dataList1);
                dataLists.add(dataList2);
                mLineView.updateGridNum();
            }
            mLineView.setFloatDataList(dataLists);
        }

        void updateViewGraph3Clear() {
            ArrayList<List<Float>> dataLists = new ArrayList<>();
            dataLists.add(new LinkedList<>());
            dataLists.add(new LinkedList<>());
            mLineView.setFloatDataList(dataLists);
            mUpdateViewGraph3 = true;
            mHandler.postDelayed(() -> {
                if (mUpdateViewGraph3) {
                    mUpdateViewGraph3 = false;
                    try {
                        updateViewGraph3();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 100);
        }

        void updateViewGraph3() {
            ArrayList<List<Float>> dataLists = new ArrayList<>();
            dataLists.add(new LinkedList<>());
            dataLists.add(new LinkedList<>());
            mLineView.setFloatDataList(dataLists);

            if (lineZoom == 1) {
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

                for (float i = 0; i < len; i = (i + m)) {
                    if (dataList1.size() >= dataSize) break;
                    bottomTextList.add(mOrgBottomTextList.get((int) i));
                    dataList1.add(mOrgDataList1.get((int) i));
                    dataList2.add(mOrgDataList2.get((int) i));
                }

                mLineView.setBottomTextList2(bottomTextList);
                dataLists.add(dataList1);
                dataLists.add(dataList2);
                mLineView.updateGridNum();
            }
            mLineView.setFloatDataList(dataLists);
        }
    }

    // ---------------------------------------------------------------------

    /**
     * Manages the table/list for saved points.
     */
    private class TableController {

        void initView4(ViewGroup view) {
            ViewGroup viewGroup = view.findViewById(R.id.layout_view);
            mDataListView = viewGroup.findViewById(R.id.list_view);
            mMeasureDataSimpleAdapter = new MeasureDataSimpleAdapter(getContext());
            mMeasureDataSimpleAdapter.setData(new LinkedList<>());
            mDataListView.setAdapter(mMeasureDataSimpleAdapter);
        }

        void updateViewGraph4(String value, String temp) {
            SimpleDateFormat formatter1 =
                    new SimpleDateFormat(Constant.DateFormat, Locale.US);
            SimpleDateFormat formatter2 =
                    new SimpleDateFormat(Constant.Time2Format, Locale.US);
            Date date = new Date();
            String date1Str = formatter1.format(date);
            String date2Str = formatter2.format(date);

            MeasureDataSimpleAdapter.Bean bean1 =
                    mMeasureDataSimpleAdapter.getItem(0);
            MeasureDataSimpleAdapter.Bean bean = new MeasureDataSimpleAdapter.Bean();
            bean.sn = bean1 == null ? 1 : (bean1.sn + 1);
            bean.date = date1Str;
            bean.time = date2Str;
            bean.temp = temp;
            bean.value = value;
            mMeasureDataSimpleAdapter.addData(bean);
            mMeasureDataSimpleAdapter.notifyDataSetChanged();
        }
    }

    // ---------------------------------------------------------------------

    /**
     * Handles calibration UI logic extracted from updateView().
     */
    private class CalibrationManager {

        void updateCalibrationUI(DataBean bean) {
            DataApi dataApi = MyApi.getInstance().getDataApi();

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
                    mTextViewOffset.setText(
                            getFormatDouble(calibrationPh.getOffset1(), 1) + "mV"
                    );
                    mTextViewSlop.setText(
                            getFormatDouble(calibrationPh.getSlope1(), 1) + "%"
                    );
                    mTextViewCalDate.setText(getDataString(calibrationPh.getDate()));
                    int c = 0;
                    ImageView[] imageView = {
                            mImageViewPoint1,
                            mImageViewPoint2,
                            mImageViewPoint3
                    };
                    if (calibrationPh.is168() && c < imageView.length) {
                        imageView[c].setImageResource(
                                pHSelect == ParmUp.USA ? R.mipmap.ph_usa_1 : R.mipmap.ph_nist_1
                        );
                        imageView[c].setVisibility(View.VISIBLE);
                        c++;
                    }
                    if (calibrationPh.is400() && c < imageView.length) {
                        imageView[c].setImageResource(
                                pHSelect == ParmUp.USA ? R.mipmap.ph_usa_2 : R.mipmap.ph_nist_2
                        );
                        imageView[c].setVisibility(View.VISIBLE);
                        c++;
                    }
                    if (calibrationPh.is700() && c < imageView.length) {
                        imageView[c].setImageResource(
                                pHSelect == ParmUp.USA ? R.mipmap.ph_usa_3 : R.mipmap.ph_nist_3
                        );
                        imageView[c].setVisibility(View.VISIBLE);
                        c++;
                    }
                    if (calibrationPh.is1001() && c < imageView.length) {
                        imageView[c].setImageResource(
                                pHSelect == ParmUp.USA ? R.mipmap.ph_usa_4 : R.mipmap.ph_nist_4
                        );
                        imageView[c].setVisibility(View.VISIBLE);
                        c++;
                    }
                    if (calibrationPh.is1245() && c < imageView.length) {
                        imageView[c].setImageResource(
                                pHSelect == ParmUp.USA ? R.mipmap.ph_usa_5 : R.mipmap.ph_nist_5
                        );
                        imageView[c].setVisibility(View.VISIBLE);
                        c++;
                    }
                    mViewGroupSlop.setVisibility(c <= 1 ? View.GONE : View.VISIBLE);
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

                mViewGroupTempRef.setVisibility(View.GONE);
                mViewGroupTempCoeffc.setVisibility(View.GONE);
                mViewGroupSalinity.setVisibility(
                        mModeType == Constant.MODE_SAL ? View.VISIBLE : View.GONE
                );
                if (mGraphModeType == Constant.MODE_SAL) {
                    mTextViewSalinity.setText(R.string.salinity);
                }

                mViewGroupTDS.setVisibility(
                        mModeType == Constant.MODE_TDS ? View.VISIBLE : View.GONE
                );
                if (mGraphModeType == Constant.MODE_TDS && dataApi.getLastParm() != null) {
                    mTextViewTDS.setText(dataApi.getLastParm().getTDSFactor());
                }
            }

            mNewMode = false;
        }
    }

    /**
     * UpdatePipeline – Orchestrates updateView() into small clean modules
     */
    private class UpdatePipeline {

        void run(DataBean bean) {
            if (bean == null) return;

            updateHoldUI(bean);
            updatePrimaryValues(bean);
            updateTemperature(bean);
            updateLeftAxisUnit();
            updateCalibrationUI(bean);
            updateGraphDial(bean);
            updateGraphLine(bean);
            updateTable(bean);
            updateReminder(bean);
            updateAlarmBackground();
        }

        void runSync(DataBean bean) {
            if (bean == null) return;

            deviceSync(bean);
        }

        // -------------------------------------------------------------
        // 1) Device sync (mode + pattern)
        // -------------------------------------------------------------
        private void deviceSync(DataBean bean) {
            modeManager.syncModeFromDataBean(bean);
            modePatternManager.syncPatternFromDataBean(bean);
        }

        // -------------------------------------------------------------
        // 2) Hold button
        // -------------------------------------------------------------
        private void updateHoldUI(DataBean bean) {
            mTextViewHoldBt.setText(bean.isHold() ? R.string.unhold : R.string.hold);
        }

        // -------------------------------------------------------------
        // 3) PH/COND/ORP primary readings
        // -------------------------------------------------------------
        private void updatePrimaryValues(DataBean bean) {
            Data data = bean.getData();
            if (data == null) return;

            mModeType = data.getMode();

            String unitStr = data.getUnitString();
            String valueStr = getFormatDouble(data.getValue(), data.getPointDigit());

            switch (mModeType) {
                case Constant.MODE_VELA_PH:
                    mCurrentValue = bean.getPh();
                    tv_value_ph.setText(valueStr);
                    tv_danwei1_ph.setText(unitStr);
                    break;

                case Constant.MODE_VELA_COND:
                    mCurrentValue = bean.getEc();
                    tv_value_cond.setText(valueStr);
                    tv_danwei1_cond.setText(unitStr);
                    break;

                case Constant.MODE_VELA_ORP:
                    mCurrentValue = bean.getOrp();
                    tv_value_orp.setText(valueStr);
                    tv_danwei1_orp.setText(unitStr);
                    break;
            }
        }

        // -------------------------------------------------------------
        // 4) Temperature section
        // -------------------------------------------------------------
        private void updateTemperature(DataBean bean) {
            mCurrentTemp = bean.getTemp();
            mTempTextView.setText(getFormatDouble(mCurrentTemp, bean.getTempPointDigit()));

            int unit2 = bean.getData() != null ? bean.getData().getUnit2() : mTempUnit;

            if (unit2 != mTempUnit) {
                if (unit2 == Data.UNIT_C) {
                    mTempUnitTextView.setText(R.string.temp_degree_c);
                } else if (unit2 == Data.UNIT_F) {
                    mTempUnitTextView.setText(R.string.zen_temp_degree_f);
                }
                mTempUnit = unit2;
                if (lineRightTextView != null) {
                    lineRightTextView.setText(mTempUnitTextView.getText());
                }
            }
        }

        // -------------------------------------------------------------
        // 5) Left axis unit
        // -------------------------------------------------------------
        private void updateLeftAxisUnit() {
            if (lineLeftTextView != null) {
                switch (mModeType) {
                    case Constant.MODE_VELA_PH:   lineLeftTextView.setText(tv_danwei1_ph.getText()); break;
                    case Constant.MODE_VELA_COND: lineLeftTextView.setText(tv_danwei1_cond.getText()); break;
                    case Constant.MODE_VELA_ORP:  lineLeftTextView.setText(tv_danwei1_orp.getText()); break;
                }
            }
        }

        // -------------------------------------------------------------
        // 6) Calibration UI
        // -------------------------------------------------------------
        private void updateCalibrationUI(DataBean bean) {
            calibrationManager.updateCalibrationUI(bean);
        }

        // -------------------------------------------------------------
        // 7) Dial update
        // -------------------------------------------------------------
        private void updateGraphDial(DataBean bean) {
            updateViewGraph();     // background dial
            setDialValue(mCurrentValue);
        }

        // -------------------------------------------------------------
        // 8) Line graph update
        // -------------------------------------------------------------
        private void updateGraphLine(DataBean bean) {
            Data data = bean.getData();
            if (data == null) return;

            if (!mAutoSaveRun && data.getMode() == mGraphModeType) {
                updateViewGraph3(data.getValue(), data.getTemp());
            }
        }

        // -------------------------------------------------------------
        // 9) Table update
        // -------------------------------------------------------------
        private void updateTable(DataBean bean) {
            Data data = bean.getData();
            if (data == null) return;

            if (!mAutoSaveRun && data.getMode() == mGraphModeType) {
                String v = getFormatDouble(data.getValue(), data.getPointDigit()) + " " + data.getUnitString();
                String t = mTempTextView.getText() + " " + mTempUnitTextView.getText();
                updateViewGraph4(v, t);
            }
        }

        // -------------------------------------------------------------
        // 10) Reminder UI
        // -------------------------------------------------------------
        private void updateReminder(DataBean bean) {
            if (bean.hasReminder()) {
                mLayoutHint.setVisibility(View.INVISIBLE);
                int n = MyApi.getInstance().getDataApi().getReminder();
                mRemindersTextView.setText(
                        n > 1 ? getString(R.string.n_hits, n)
                                : getString(R.string.one_hits));
            } else {
                mLayoutHint.setVisibility(View.INVISIBLE);
            }
        }

        // -------------------------------------------------------------
        // 11) Alarm background
        // -------------------------------------------------------------
        private void updateAlarmBackground() {
            alarmManager.updateAlarmBackground(alarmid);
        }
    }
}