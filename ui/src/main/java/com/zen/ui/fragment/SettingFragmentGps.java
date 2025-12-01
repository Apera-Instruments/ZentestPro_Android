package com.zen.ui.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.logger.Logger;
import com.zen.ui.AssectActivity;
import com.zen.ui.AssectChangeActivity;
import com.zen.ui.R;
import com.zen.ui.SettingActivity;
import com.zen.ui.base.BaseFragment;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.zen.ui.adapter.AssectListAdapter;
import com.zen.ui.utils.AssectListview;
import com.zen.ui.utils.AssectSaveData;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by louis on 17-12-21.
 */
public class SettingFragmentGps extends BaseFragment implements View.OnClickListener {
    private List<AssectSaveData> assectList = new ArrayList<>();
    private View mNewAssectRightView;
    private View viewAll;
//修改过，以前是GPS定位
//    private LocationManager mLocationManager;
//    private String locateType=LocationManager.GPS_PROVIDER;
//    private TextView mTextViewGPS;
    private TextView tvLeft;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_assect);

        viewAll = inflater.inflate(R.layout.activity_assect, container, false);
//        mLocationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
//        mTextViewGPS = view.findViewById(R.id.tv_gps_value);
//      //  MyLocationListener mLocationListener =new MyLocationListener();
//       // mLocationManager.requestLocationUpdates(pProvider, MIN_TIME_UPDATE,MIN_DISTANCE_UPDATE, mLocationListener);
//        locateType = mLocationManager.getBestProvider(createFineCriteria(),true);
//        getLocation();
        mNewAssectRightView  = viewAll.findViewById(R.id.tv_newright);
        New_Right();
        tvLeft = viewAll.findViewById(R.id.tv_left);
        tvLeft.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });

//        initAssect();
//
//        RecyclerView recyclerView = (RecyclerView) viewAll.findViewById(R.id.Recycler_View);
//        LinearLayoutManager layoutManager = new LinearLayoutManager(viewAll.getContext());
//        layoutManager.setOrientation(OrientationHelper.VERTICAL);
//        recyclerView.setLayoutManager(layoutManager);
//
//        AssectListAdapter adapter = new AssectListAdapter(assectList,new AssectListAdapter.OnReCyclerItemClickListener(){
//
//            @Override
//            public void onItemClick(View V,int position){
//                AssectSaveData assectSaveData = assectList.get(position);
//
//                Intent intent = new Intent(getActivity(), AssectChangeActivity.class);
//                intent.putExtra("Textname",assectSaveData.getName());
//                intent.putExtra("Imagename",assectSaveData.getImageID());
//                startActivity(intent);
//            }
//        });
//
//        recyclerView.setAdapter(adapter);
        return viewAll;
    }

    private void New_Right() {
        mNewAssectRightView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickNewRight(view);
            }
        });
    }

    public void onClickNewRight(View v){

        Intent intent = new Intent((SettingActivity) getActivity(),AssectActivity.class);
        startActivity(intent);
    }

    private void initAssect(){
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("AssectList",MODE_PRIVATE);
        assectList.clear();
        int size = sharedPreferences.getInt("data_size",0);

        for (int i=0;i<size;i++){
            AssectSaveData beer = new AssectSaveData(sharedPreferences.getString("name_"+i,null), sharedPreferences.getString("Image"+i,null),sharedPreferences.getString("nameid"+i,null),sharedPreferences.getString("dataid"+i,null));
            assectList.add(beer);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        initAssect();
        if (assectList.size()==0){
            return;
        }

        RecyclerView recyclerView = (RecyclerView) viewAll.findViewById(R.id.Recycler_View);
        LinearLayoutManager layoutManager = new LinearLayoutManager(viewAll.getContext());
        layoutManager.setOrientation(OrientationHelper.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        AssectListAdapter adapter = new AssectListAdapter(assectList,new AssectListAdapter.OnReCyclerItemClickListener(){

            @Override
            public void onItemClick(View V,int position){
                AssectSaveData assectSaveData = assectList.get(position);

                Intent intent = new Intent(getActivity(), AssectChangeActivity.class);
                intent.putExtra("Textname",assectSaveData.getName());
                intent.putExtra("Imagename",assectSaveData.getImageID());
                intent.putExtra("nameid",assectSaveData.getNameid());
                startActivity(intent);
            }
        });

        recyclerView.setAdapter(adapter);
//   API.getResourceApi().update(ResourceApi.HOT_VIEW, ResourceApi.APP_LIST);
    }


//    @Override
//    public void onDestroy() {
//        mLocationManager.removeUpdates(locationListener);
//        super.onDestroy();
//    }
//
//    public void updateView() {
//        super.updateView();
//
//
//    }

    @Override
    public void onClick(View v) {
        Object object = v.getTag();

    }

//    /** this criteria needs high accuracy, high power and cost */
//    public static Criteria createFineCriteria() {
//
//        Criteria c = new Criteria();
//        c.setAccuracy(Criteria.ACCURACY_FINE);//高精度
//        c.setAltitudeRequired(true);//包含高度信息
//        c.setBearingRequired(false);//包含方位信息
//        c.setSpeedRequired(false);//包含速度信息
//        c.setCostAllowed(true);//允许付费
//        c.setPowerRequirement(Criteria.POWER_LOW);//高耗电
//        return c;
//    }
//
//    private LocationListener locationListener = new LocationListener() {
//        /**
//         * 位置信息变化时触发:当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
//         * @param location
//         */
//        @Override
//        public void onLocationChanged(Location location) {
//            //Toast.makeText(MainActivity.this, "onLocationChanged函数被触发！", Toast.LENGTH_SHORT).show();
//            //updateUI(location);
//            Log.i(getTAG(), "时间：" + location.getTime());
//            Log.i(getTAG(), "经度：" + location.getLongitude());
//            Log.i(getTAG(), "纬度：" + location.getLatitude());
//            Log.i(getTAG(), "海拔：" + location.getAltitude());
//            updateUI(location);
//        }
//
//
//
//        /**
//         * GPS状态变化时触发:Provider被disable时触发此函数，比如GPS被关闭
//         * @param provider
//         * @param status
//         * @param extras
//         */
//        @Override
//        public void onStatusChanged(String provider, int status, Bundle extras) {
//            switch (status) {
//                //GPS状态为可见时
//                case LocationProvider.AVAILABLE:
//                    //Toast.makeText(MainActivity.this, "onStatusChanged：当前GPS状态为可见状态", Toast.LENGTH_SHORT).show();
//                    break;
//                //GPS状态为服务区外时
//                case LocationProvider.OUT_OF_SERVICE:
//                  //  Toast.makeText(MainActivity.this, "onStatusChanged:当前GPS状态为服务区外状态", Toast.LENGTH_SHORT).show();
//                    break;
//                //GPS状态为暂停服务时
//                case LocationProvider.TEMPORARILY_UNAVAILABLE:
//                 //   Toast.makeText(MainActivity.this, "onStatusChanged:当前GPS状态为暂停服务状态", Toast.LENGTH_SHORT).show();
//                    break;
//            }
//        }
//
//        /**
//         * 方法描述：GPS开启时触发
//         * @param provider
//         */
//        @Override
//        public void onProviderEnabled(String provider) {
//            //Toast.makeText(MainActivity.this, "onProviderEnabled:方法被触发", Toast.LENGTH_SHORT).show();
//            getLocation();
//        }
//
//        /**
//         * 方法描述： GPS禁用时触发
//         * @param provider
//         */
//        @Override
//        public void onProviderDisabled(String provider) {
//
//        }
//    };
//
//    private void getLocation() {
//        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
//                (getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//            //mPermissionHelper.request(MULTI_PERMISSIONS);
//            return;
//        }
//        Log.i(getTAG(),locateType);
//        Location location = mLocationManager.getLastKnownLocation(locateType); // 通过GPS获取位置
//        if (location != null) {
//            updateUI(location);
//        }
//        // 设置监听*器，自动更新的最小时间为间隔N秒(1秒为1*1000，这样写主要为了方便)或最小位移变化超过N米
//        mLocationManager.requestLocationUpdates(locateType, 100,0,
//                locationListener);
//    }
//
//    private void updateUI(Location location) {
//       StringBuilder stringBuilder = new StringBuilder();
//        stringBuilder.append("Time：").append(new Date(location.getTime()).toString());
//        stringBuilder.append("\n");
//        stringBuilder.append("Longitude：").append(location.getLongitude());
//        stringBuilder.append("\n");
//        stringBuilder.append("Latitude：").append(location.getLatitude());
//        stringBuilder.append("\n");
//        stringBuilder.append("Altitude：").append(location.getAltitude());
//        stringBuilder.append("\n");
//        mTextViewGPS.setText(stringBuilder.toString());
//    }
}