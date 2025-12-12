package com.zen.ui.zentestpro.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.foolchen.lib.tracker.Tracker;
import com.zen.api.Constant;
import com.zen.api.MyApi;
import com.zen.api.data.BleDevice;
import com.zen.api.event.BleDeviceConnectEvent;
import com.zen.api.event.BleDeviceFoundEvent;
import com.zen.api.event.StartEvent;
import com.zen.api.event.SyncEventUpload;
import com.zen.api.protocol.Key;
import com.zen.ui.CalibrationActivity;
import com.zen.ui.R;
import com.zen.ui.base.BaseActivity;
import com.zen.ui.base.BaseFragment;
import com.zen.ui.fragment.CalibrationFragment;
import com.zen.ui.fragment.DataFragment;
import com.zen.ui.fragment.InfoFragment;
import com.zen.ui.fragment.MeasureFragment;
import com.zen.ui.fragment.SettingFragment1;
import com.zen.ui.utils.AssectSaveData;
import com.zen.ui.utils.ToastUtilsX;
import com.zen.ui.view.BleDevicePopupWindow;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import cn.pedant.SweetAlert.SweetAlertDialog;

//import android.support.design.widget.NavigationView;
public class VelaHomeActivity extends BaseActivity implements View.OnClickListener {
    public final static String TAG=  "HOME_ACTIVITY";
    private static final boolean BOTTOM = false;
    private String DEMO_DEVICE = "Demo Device";
    private final int[] image1Id = {R.drawable.ico_measure_normal, R.drawable.ico_calibrate_normal, R.drawable.ico_data_normal, R.drawable.ico_settings_normal, R.drawable.ico_info_normal};
    private final int[] image2Id = {R.drawable.ico_measure_sel, R.drawable.ico_calibrate_sel, R.drawable.ico_data_sel, R.drawable.ico_setting_sel, R.drawable.ico_info_sel};
    private final int items[] = {R.id.layout_item1, R.id.layout_item2, R.id.layout_item3, R.id.layout_item4, R.id.layout_item5};
    private final int ITEM_LEN = items.length;
    private SparseArray<Fragment> fragmentArray = new SparseArray<>(ITEM_LEN);
    private SparseArray<ImageView> imageViewArray = new SparseArray<>(ITEM_LEN);
    private SparseArray<TextView> textViewSArray = new SparseArray<>(ITEM_LEN);
    private SparseArray<Bitmap> bitmap1Array = new SparseArray<>(ITEM_LEN);
    private SparseArray<Bitmap> bitmap2Array = new SparseArray<>(ITEM_LEN);
    private int mCurrentIndex = 0;
    private int mPreIndex = 0;
    private final int color1 = Color.argb(0xFF, 0xFF, 0xFF, 0xFF);
    private final int color2 = Color.argb(0xff, 0x27, 0xbb, 0xf2);
    private final int[] TITLEs = {R.string.measure, R.string.calibration, R.string.data, R.string.setting, R.string.info};//

    private int PERMISSION_BUTTCh_CODE = 120;

    TextView mTitleView;
    ViewGroup mFrameLayout;
    ViewGroup noDeviceView;

    private View mStatusBar;
    private int mBackTime=0;
    private long mBackUpdateTimestamp=0;
    private ImageView HometopImage;

    public void onMenu() {
        showMenu();
    }

    private BleDevicePopupWindow mBleDevicePopupWindow;

    private Fragment mCurrentFragment;

    ImageView mBtView;
    private ViewGroup navView1;
    private ViewGroup navView2;
    private ViewGroup navView3;
    private ViewGroup navView4;
    private ViewGroup navView5;
    private ViewGroup[] viewGroups = new ViewGroup[5];
    private View navViewClose;

    private void setViewGroupSelect(ViewGroup viewGroup, boolean selected) {
        for (int i = 0; i < viewGroup.getChildCount(); i++)
        {
            viewGroup.getChildAt(i).setSelected(selected);
        }
    }

    private boolean isAndroid12() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S;
    }

    public void btConnect() {
        EventBus.getDefault().post(new SyncEventUpload());
        if (!MyApi.getInstance().getBtApi().isBtEnable()) {
            SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(this,SweetAlertDialog.WARNING_TYPE);
            sweetAlertDialog.setTitleText(getString(R.string.bt))
                    .setContentText(getString(R.string.bt_turn_on))
                    .setConfirmText(getString(R.string.bt_open))
                    .setCancelText(getString(android.R.string.cancel))
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener(){
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            MyApi.getInstance().getBtApi().open();
                            sweetAlertDialog.dismissWithAnimation();
                        }
                    }).show();
            return;
        }else {
//            // fixme https://blog.csdn.net/minusn/article/details/128660803 蓝牙没做兼容
            if (isAndroid12()) {
                if (checkPermissionGranted(Manifest.permission.BLUETOOTH_SCAN)||checkPermissionGranted(Manifest.permission.BLUETOOTH_ADVERTISE)||checkPermissionGranted(Manifest.permission.BLUETOOTH_CONNECT)){

                }else {

                    checkPermission(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_ADVERTISE,Manifest.permission.BLUETOOTH_CONNECT);
                    return;
                }
            }
        }

        if (!VelaHomeActivity.isLocServiceEnable(this)){
            Log.d("GPSYESORNO","weilianjie");
            SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(this,SweetAlertDialog.WARNING_TYPE);
            sweetAlertDialog.setTitleText(getString(R.string.GPSOpen))
                    .setContentText(getString(R.string.GPSToast))
                    .setConfirmText(getString(R.string.bt_open))
                    .setCancelText(getString(android.R.string.cancel))
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener(){
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            Log.d("GPSYESORNO","weilianjie");
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                            sweetAlertDialog.dismissWithAnimation();
                        }
                    }).show();
            return;
        }else {
            if (checkPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)||checkPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)){

            }else {
                checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION);
                return;
            }
        }

        if (mBleDevicePopupWindow == null) {
            mBleDevicePopupWindow = new BleDevicePopupWindow(this);
            mBleDevicePopupWindow.setOnClickListener(new BleDevicePopupWindow.onButtonClickListener() {
                @Override
                public void onClick(Object tag) {
                    if (MyApi.getInstance().getBtApi().isScanning()) {
                        MyApi.getInstance().getBtApi().stopScan();
                    }
                    if (MyApi.getInstance().getBtApi().isConnected()) {
                        BleDevice bleDevice = MyApi.getInstance().getBtApi().getLastDevice();
                        if (bleDevice != null) {
                            if (!bleDevice.getMac().equals(tag.toString())) {
                                MyApi.getInstance().getBtApi().disconnect();
                            } else {
                                Log.i(TAG,"same device return");
                                return;
                            }
                        }
                    }

                    if (DEMO_DEVICE.equals(tag) || BleDevice.DEMO.equals(tag)) {
                        BleDevice bleDevice =  BleDevice.buildDemo();
                        bleDevice.setName(DEMO_DEVICE);
                        MyApi.getInstance().getBtApi().connect(bleDevice);
                        Tracker.INSTANCE.trackEvent("ble_connect",null);
                    } else if (tag != null) {
                        MyApi.getInstance().getBtApi().connect(new BleDevice("", tag.toString()));
                        Tracker.INSTANCE.trackEvent("ble_connect",null);
                    }
                }
            });
        }
//        mBleDevicePopupWindow.clearDevices();
        if (!MyApi.getInstance().getBtApi().isConnected()) {

            BleDevice device = BleDevice.buildDemo();
            device.setName(DEMO_DEVICE);
            mBleDevicePopupWindow.addDevice(device.getName(), device.getMac());

        }
        //mBleDevicePopupWindow.showAsDropDown(mBtView, 0,0, Gravity.TOP | Gravity.END);
        mBleDevicePopupWindow.showAsDropDown(mBtView, -mBleDevicePopupWindow.getWidth(), 0);
        MyApi.getInstance().getBtApi().startScan();
    }

    private boolean checkPermissionGranted(String permission){
        return  this.checkPermission(permission,Process.myPid(), Process.myUid())==PackageManager.PERMISSION_GRANTED;
    }

    //判断定位服务是否开启
    public static boolean isLocServiceEnable(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gps || network) {
            return true;
        }
        return false;
    }

    //BleDeviceConnectEvent
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(BleDeviceConnectEvent event) {
        updateBtView(event.isConnected());
        EventBus.getDefault().post(new SyncEventUpload());
    }

    private void updateBtView(boolean connected){
        if (connected) {
            mBtView.setImageResource(R.mipmap.bt_linked);
            if(mCurrentIndex ==  0 || mCurrentIndex ==  4) {
                if (mFrameLayout.getVisibility() != View.VISIBLE) {
                    mFrameLayout.setVisibility(View.VISIBLE);
                    noDeviceView.setVisibility(View.GONE);
                }
            }
        } else {
            mBtView.setImageResource(R.mipmap.bt_normal);
            if (mCurrentIndex == 0 || mCurrentIndex == 4) {
                noDeviceView.setVisibility(View.VISIBLE);
                mFrameLayout.setVisibility(View.INVISIBLE);
            }
            if(mBleDevicePopupWindow!=null){
                BleDevice device = BleDevice.buildDemo();
                device.setName(DEMO_DEVICE);
                mBleDevicePopupWindow.addDevice(device.getName(), device.getMac());
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(BleDeviceFoundEvent event) {
        if (MyApi.getInstance().getBtApi().isScanning()) {
           // showMsg("" + event.getName() + " " + event.getMac());
            if (mBleDevicePopupWindow != null) {
                if(!TextUtils.isEmpty(event.getName()))
                {
                    mBleDevicePopupWindow.addDevice(event.getName(), event.getMac());
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().post(new SyncEventUpload());
        if (mToast != null) {
            mToast.cancel();
        }

        if(MyApi.getInstance().getBtApi().isConnected()){
            MyApi.getInstance().getBtApi().disconnect();
        }
        if (MyApi.getInstance().getBtApi().isScanning()) {
            MyApi.getInstance().getBtApi().stopScan();
        }
 /*       FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        for (int i = 0; i < fragmentArray.size(); i++) {
            Fragment fragment = fragmentArray.get(i);
            if (fragment.isAdded()) {
                fragmentTransaction.remove(fragment);
            }
        }
        fragmentTransaction.commit();*/
        super.onDestroy();
    }

    private List<AssectSaveData> assectList = new ArrayList<>();
    private String mTraceNo=null;
    private Random random=new Random();
    private AtomicInteger atomicInteger=new AtomicInteger(0);
    private String getTraceString(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyymmddHHmmss", Locale.US);
        return simpleDateFormat.format(date);
    }

    private String getImageid(int id){
        //测试保存图片
        Resources res = this.getResources();
        BitmapDrawable d = (BitmapDrawable) res.getDrawable(id);
        Bitmap imageMap = d.getBitmap();

        mTraceNo = getTraceString(new Date())+Integer.toHexString((this.hashCode()*100+random.nextInt(100))*100+atomicInteger.addAndGet(1)) ;
        File fileDir = this.getExternalFilesDir("save");
        Log.i(getTAG(), "saveBitmap success "+fileDir.getPath());
        File f = new File(fileDir, mTraceNo+Long.toHexString(System.currentTimeMillis()) + ".png");
        if (f.exists()) {
            f.delete();
        }
        Bitmap outBitmap =  Bitmap.createBitmap(imageMap);
        try {
            FileOutputStream out = new FileOutputStream(f);
            imageMap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
            Log.i(getTAG(), "saveBitmap success " + f.getPath());
        }catch (IOException e){
            Log.i("1111",e.toString());
        };
        return f.getPath();
    }

    public boolean SaveArray(){

        SharedPreferences.Editor editor = getSharedPreferences("AssectList",MODE_PRIVATE).edit();
        editor.putInt("data_size",assectList.size());

        for (int i=0;i<assectList.size();i++){
            editor.putString("name_"+i, assectList.get(i).getName());
            editor.putString("Image"+i, assectList.get(i).getImageID());
            editor.putString("nameid"+i, assectList.get(i).getNameid());
            editor.putString("dataid"+i, assectList.get(i).getDataid());
        }
        return editor.commit();
    }

    RelativeLayout iv_menu;

    @SuppressLint("WrongThread")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mTitleView = findViewById(R.id.tv_title);
        mFrameLayout = findViewById(R.id.framelayout);
        noDeviceView = findViewById(R.id.layout_nodevice);
        mBtView = findViewById(R.id.bt_blue_connect);

        iv_menu = findViewById(R.id.iv_menu);
        iv_menu.setOnClickListener(this);

        mBtView.setOnClickListener(this);

        DEMO_DEVICE = getString(R.string.demo_device);
        if(Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            //让应用主题内容占用系统状态栏的空间,注意:下面两个参数必须一起使用 stable 牢固的
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            //设置状态栏颜色为透明
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

//        List<String> mpermissionList = new ArrayList<>();
//        if (Build.VERSION.SDK_INT>= 31){
//            mpermissionList.add(Manifest.permission.BLUETOOTH_SCAN);
//            mpermissionList.add(Manifest.permission.BLUETOOTH_ADVERTISE);
//            mpermissionList.add(Manifest.permission.BLUETOOTH_CONNECT);
//            ActivityCompat.requestPermissions(this,mpermissionList.toArray(new String[0]),PERMISSION_BUTTCh_CODE);
//        }

        HometopImage = findViewById(R.id.home_bg_tipimg);
        mStatusBar = findViewById(R.id.fillStatusBarView);
        ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) mStatusBar.getLayoutParams();
        lp.width = LinearLayout.LayoutParams.MATCH_PARENT;
        lp.height = getStatusBar();
        mStatusBar.setLayoutParams(lp);

        getDisplayMetrics();
        Log.i(TAG,getString(R.string.test));

        SharedPreferences sharedPreferences = getSharedPreferences("AssectList",MODE_PRIVATE);
        assectList.clear();
        int size = sharedPreferences.getInt("data_size",0);

        for (int i=0;i<size;i++){
            AssectSaveData beer = new AssectSaveData(sharedPreferences.getString("name_"+i,null), sharedPreferences.getString("Image"+i,null),sharedPreferences.getString("nameid"+i,null),sharedPreferences.getString("dataid"+i,null));
            assectList.add(beer);
        }

        if (assectList.size()>0){
            for (int i=0;i<assectList.size();i++){
            }
//            Logger.d("data_size shuzi0 "+sharedPreferences.getAll());
        }else {
            for (int i=1;i<2;i++) {
                AssectSaveData beer = new AssectSaveData("tomato (asset example)", getImageid(R.drawable.assect_tomato), "0", "0");
                assectList.add(beer);
                AssectSaveData assect_choclate = new AssectSaveData("strawberry (asset example)", getImageid(R.drawable.assect_strawberry), "0", "1");
                assectList.add(assect_choclate);
                AssectSaveData goat_cheese = new AssectSaveData("beer (asset example)", getImageid(R.drawable.assect_beer), "0", "2");
                assectList.add(goat_cheese);
                AssectSaveData pool = new AssectSaveData("choclate (asset example)", getImageid(R.drawable.assect_choclate), "0", "3");
                assectList.add(pool);
                AssectSaveData salami = new AssectSaveData("goat cheese (asset example)", getImageid(R.drawable.assect_goat_cheese), "0", "4");
                assectList.add(salami);
                AssectSaveData strawberry = new AssectSaveData("pool (asset example)", getImageid(R.drawable.assect_pool), "0", "5");
                assectList.add(strawberry);
                AssectSaveData tomato = new AssectSaveData("salami (asset example)", getImageid(R.drawable.assect_salami), "0", "6");
                assectList.add(tomato);
                AssectSaveData water = new AssectSaveData("water (asset example)", getImageid(R.drawable.assect_water), "0", "7");
                assectList.add(water);
                AssectSaveData wine = new AssectSaveData("wine (asset example)", getImageid(R.drawable.assect_wine), "0", "8");
                assectList.add(wine);

                SaveArray();
            }

            for (int m=0;m<assectList.size();m++){
            }

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
 /*       ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();*/

  /*      NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);*/
        navView1 = drawer.findViewById(R.id.ll_measure);
        navView2 = drawer.findViewById(R.id.ll_calibration);
        navView3 = drawer.findViewById(R.id.ll_data);
        navView4 = drawer.findViewById(R.id.ll_setting);
        navView5 = drawer.findViewById(R.id.ll_info);
        navViewClose = drawer.findViewById(R.id.iv_close);
        viewGroups[0] = navView1;
        viewGroups[1] = navView2;
        viewGroups[2] = navView3;
        viewGroups[3] = navView4;
        viewGroups[4] = navView5;

        navView1.setOnClickListener(this);
        navView2.setOnClickListener(this);
        navView3.setOnClickListener(this);
        navView4.setOnClickListener(this);
        navView5.setOnClickListener(this);
        navViewClose.setOnClickListener(this);

        if (BOTTOM) {
            int index = 0;

            for (int id : items) {
                View view1 = findViewById(id);
                if (view1 != null) {
                    view1.setTag(index);
                    view1.setOnClickListener(this);
                }
                index++;
            }

            ImageView mIconView1 = (ImageView) findViewById(R.id.iv_icon1);
            ImageView mIconView2 = (ImageView) findViewById(R.id.iv_icon2);
            ImageView mIconView3 = (ImageView) findViewById(R.id.iv_icon3);
            ImageView mIconView4 = (ImageView) findViewById(R.id.iv_icon4);
            ImageView mIconView5 = (ImageView) findViewById(R.id.iv_icon5);
            imageViewArray.append(0, mIconView1);
            imageViewArray.append(1, mIconView2);
            imageViewArray.append(2, mIconView3);
            imageViewArray.append(3, mIconView4);
            imageViewArray.append(4, mIconView5);
            TextView mNameTextView1 = (TextView) findViewById(R.id.tv_name1);
            TextView mNameTextView2 = (TextView) findViewById(R.id.tv_name2);
            TextView mNameTextView3 = (TextView) findViewById(R.id.tv_name3);
            TextView mNameTextView4 = (TextView) findViewById(R.id.tv_name4);
            TextView mNameTextView5 = (TextView) findViewById(R.id.tv_name5);
            textViewSArray.append(0, mNameTextView1);
            textViewSArray.append(1, mNameTextView2);
            textViewSArray.append(2, mNameTextView3);
            textViewSArray.append(3, mNameTextView4);
            textViewSArray.append(4, mNameTextView5);
            index = 0;
            for (int id : image1Id) {
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), id);
                bitmap1Array.append(index, bitmap);
                index++;
            }
            index = 0;
            for (int id : image2Id) {
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), id);
                bitmap2Array.append(index, bitmap);
                index++;
            }
        }
        fragmentArray.append(0, new MeasureFragment());
        fragmentArray.append(1, new CalibrationFragment());
        fragmentArray.append(2, new DataFragment());
        fragmentArray.append(3, new SettingFragment1());
        fragmentArray.append(4, new InfoFragment());
        mCurrentIndex = 0;
        mPreIndex = 0;
        setViewGroupSelect(viewGroups[mCurrentIndex],true);
        updateView();
//        checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN);
        EventBus.getDefault().post(new StartEvent());
    }

    public void onResume() {
        super.onResume();
        updateBtView (MyApi.getInstance().getBtApi().isConnected()) ;
    }
/*
    @Override
    public void onClick(View v) {
        int index = (int) v.TAG;
        switch (index) {
            case 0:

            case 2:
            case 3:
            case 4:
                mPreIndex = mCurrentIndex;
                mCurrentIndex = index;
                break;
            case 1:
                startActivity(new Intent(this, CalibrationActivity.class));
                return;

            default:
                showMsg("");

                return;
        }
        updateView();
    }*/

    @Override
    public void onClick(View v) {
        // Handle navigation view item clicks here.
        int id = v.getId();
        if (id == R.id.ll_measure) {
            if(mCurrentIndex==0) return;
            mPreIndex = mCurrentIndex;
            setViewGroupSelect(viewGroups[mCurrentIndex],false);
            mCurrentIndex = 0;
            setViewGroupSelect(viewGroups[mCurrentIndex],true);
            updateView();
            if(MyApi.getInstance().getBtApi().isConnected()) {
                mFrameLayout.setVisibility(View.VISIBLE);
                noDeviceView.setVisibility(View.GONE);
            }else{
                mFrameLayout.setVisibility(View.INVISIBLE);
                noDeviceView.setVisibility(View.VISIBLE);
            }
        } else if (id == R.id.ll_calibration) {
          if(MyApi.getInstance().getBtApi().isConnected()){
              MyApi.getInstance().getBtApi().sendCommand(new Key(Key.Calibration));

              Intent intent =new Intent(this, CalibrationActivity.class);
              int mode = MyApi.getInstance().getDataApi().getData().getMode();
              if (mode == Constant.MODE_PH || mode == Constant.MODE_COND) {
                  if (MyApi.getInstance().getBtApi().getLastDevice()!=null && MyApi.getInstance().getBtApi().getLastDevice().isDemo() || mCurrentIndex!=0) {
                      intent.putExtra("MODE", mode);
                      startActivity(intent);
                  }
              }else{
                  String modeText ="";
                  switch (mode){
                      case Constant.MODE_ORP:
                          modeText =getString(R.string.not_need_calibrated_orp);// String.format(getString(R.string.not_calibrated),Constant.STR_MODE_ORP);
                          break;
                      case Constant.MODE_SAL:
                          modeText=getString(R.string.not_need_caslib);//Constant.STR_MODE_SAL;
                          break;
                      case Constant.MODE_TDS:
                          modeText=getString(R.string.not_need_caslib);
                          break;
                      case Constant.MODE_RES:
                          modeText=getString(R.string.not_need_caslib);
                          break;
                  }

                  new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE)
                          .setTitleText("").setContentText(modeText)
                          .show();
              }
          }else{
              showMsg(getString(R.string.no_dev));
          }
        } else if (id == R.id.ll_data) {
            if(mCurrentIndex==2) return;
            mPreIndex = mCurrentIndex;
            setViewGroupSelect(viewGroups[mCurrentIndex],false);
            mCurrentIndex = 2;
            setViewGroupSelect(viewGroups[mCurrentIndex],true);
            updateView();
            mFrameLayout.setVisibility(View.VISIBLE);
            noDeviceView.setVisibility(View.GONE);
        } else if (id == R.id.ll_setting) {
            if(mCurrentIndex==3) return;
            mPreIndex = mCurrentIndex;
            setViewGroupSelect(viewGroups[mCurrentIndex],false);
            mCurrentIndex = 3;
            setViewGroupSelect(viewGroups[mCurrentIndex],true);
            updateView();
            mFrameLayout.setVisibility(View.VISIBLE);
            noDeviceView.setVisibility(View.GONE);
        } else if (id == R.id.ll_info) {
            if(mCurrentIndex==4) return;
            mPreIndex = mCurrentIndex;
            setViewGroupSelect(viewGroups[mCurrentIndex],false);
            mCurrentIndex = 4;
            setViewGroupSelect(viewGroups[mCurrentIndex],true);
            updateView();
            if(MyApi.getInstance().getBtApi().isConnected()) {
                mFrameLayout.setVisibility(View.VISIBLE);
                noDeviceView.setVisibility(View.GONE);
            }else{
                mFrameLayout.setVisibility(View.INVISIBLE);
                noDeviceView.setVisibility(View.VISIBLE);
            }
           ;
        } else if (id == R.id.iv_close) {

        } else if (id == R.id.iv_menu) {
            onMenu();
        } else if (id == R.id.bt_blue_connect) {
            btConnect();
        }else {
            return;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    private Toast mToast;

    private void showMsg(String s) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(this, s, Toast.LENGTH_SHORT);
        mToast.show();
    }

    public void updateView() {
        if (mCurrentFragment == fragmentArray.get(mCurrentIndex)) {
            Log.v(TAG, "same Fragment not run updateView");
            return;
        }
        updateBtView(MyApi.getInstance().getBtApi().isConnected());

        Fragment fragment = fragmentArray.get(mPreIndex);

        if (mPreIndex != mCurrentIndex) {

            if (fragment instanceof BaseFragment) {
                ((BaseFragment) fragment).remove();
            }
        }

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        if (mPreIndex != mCurrentIndex) {
            if (fragment.isAdded()) {
                fragmentTransaction.remove(fragment);
            }
        }
        fragmentTransaction.replace(R.id.framelayout, fragmentArray.get(mCurrentIndex));
        mCurrentFragment = fragmentArray.get(mCurrentIndex);
        fragmentTransaction.commit();

        if(mCurrentIndex==0){
            mStatusBar.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        }else{
            mStatusBar.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        }

      if(BOTTOM) {
          for (int i = 0; i < ITEM_LEN; i++) {
              imageViewArray.get(i).setImageBitmap(mCurrentIndex == i ? bitmap2Array.get(i) : bitmap1Array.get(i));
              textViewSArray.get(i).setTextColor(
                      mCurrentIndex == i ?
                              color2
                              : color1
              );
          }
      }
        EventBus.getDefault().post(new SyncEventUpload());
       // mTitleView.setText(TITLEs[mCurrentIndex]);
    }

    public void showMenu() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.openDrawer(GravityCompat.START);
    }

    public void onBackPressed(){
        if(mCurrentIndex!=0){
            mPreIndex = mCurrentIndex;
            setViewGroupSelect(viewGroups[mCurrentIndex],false);
            mCurrentIndex = 0;
            setViewGroupSelect(viewGroups[mCurrentIndex],true);
            updateView();
            if(MyApi.getInstance().getBtApi().isConnected()) {
                mFrameLayout.setVisibility(View.VISIBLE);
                noDeviceView.setVisibility(View.GONE);
            }else{
                mFrameLayout.setVisibility(View.INVISIBLE);
                noDeviceView.setVisibility(View.VISIBLE);
            }
            return;
        }

        if (System.currentTimeMillis() - mBackUpdateTimestamp > 3000) {
            mBackTime = 0;
        }
        if (mBackTime >= 1) {
            super.onBackPressed();
            return;
        }
        mBackTime++;
//        ToastUtils.showShort(R.string.press_back_again);
        ToastUtilsX.showActi(this,R.string.press_back_again);
        mBackUpdateTimestamp = System.currentTimeMillis();
    }
}
