package com.zen.biz

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Service
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.provider.Settings
import androidx.core.app.ActivityCompat
import android.text.TextUtils
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.alibaba.fastjson.JSON

import com.zen.api.DataApi
import com.zen.api.MyApi
import com.zen.api.SettingConfig
import com.zen.api.event.*
import com.zen.api.protocol.ParmDown
import com.zen.api.protocol.ParmUp
import com.zen.api.data.BleDevice
import com.zen.biz.utils.Utils
import org.greenrobot.eventbus.EventBus

import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class BleService : Service() {
    private val mHandler = Handler()

    override fun onBind(intent: Intent): IBinder? {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return null
    }


    override fun onCreate() {
        super.onCreate()
        EventBus.getDefault().register(this)
        val daoSession = Install.getInstance().daoSession
        val deviceDao = daoSession.deviceDao
        val list = deviceDao.loadAll()
        if (list != null && !list.isEmpty()) {
            for (d in list) {
            }
        }

        MyApi.getInstance().dataApi.records?.forEach { value ->

        }

        /*     var mPhData =  PhData();
             mPhData.category = "category";
             mPhData.value = "value";
             mPhData.listValue!!.add(PhData.Data());
             mPhData.listValue!!.add(PhData.Data());
             Logger.i(JSON.toJSONString(mPhData))*/
        /*
   mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
   locateType = mLocationManager.getBestProvider(createFineCriteria(), true)
   */
 /*       val sharedPreferences = MyApi.getInstance().dataApi.setting
        var  connectedDev= sharedPreferences.getString("connectedDev", "")
        Logger.i("get device = "+connectedDev);
        if(!TextUtils.isEmpty(connectedDev) && connectedDev.startsWith("{")){
            val bleDevice = Utils.parse(connectedDev);
            MyApi.getInstance().btApi.connect(bleDevice)
        }else{
            Logger.w("do not connect");
        }*/
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        mLocationManager?.removeUpdates(locationListener);

        super.onDestroy()
    }


    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onEvent(event: UpdateEvent) {
        if (event.data is ParmUp) {
            val parmUp = event.data as ParmUp
            val sharedPreferences = MyApi.getInstance().dataApi.setting
            val editor = sharedPreferences.edit()
            /*
            *
            * 03-25 19:52:13.777 8369-8369/com.zen.zentest I/BtApiImpl: ParmUp
                                                          pHSelect:0   ph_selection_SAVE_ID
                                                          pHResolution:0 ph_resolution_SAVE_ID
                                                          autoHold:0 autoHold_SAVE_ID
                                                          backLight:0 backLight_SAVE_ID
                                                          TDSFactor:71  TDSFactor_SAVE_ID
                                                          saltUnit:0  salt_unit_SAVE_ID
                                                          tempUnit:1 tempUnit_SAVE_ID

            * */
            editor.putString(SettingConfig.ph_selection_SAVE_ID, parmUp.phSelect)
            editor.putString(SettingConfig.ph_resolution_SAVE_ID, parmUp.phResolution)
            editor.putString(SettingConfig.autoHold_SAVE_ID, parmUp.autoHold)
            editor.putString(SettingConfig.backLight_SAVE_ID, parmUp.backLight)
            editor.putString(SettingConfig.TDSFactor_SAVE_ID, parmUp.tdsFactor)
            editor.putString(SettingConfig.salt_unit_SAVE_ID, parmUp.saltUnit)
            editor.putString(SettingConfig.tempUnit_SAVE_ID, parmUp.tempUnit)
            editor.putString(SettingConfig.ReferenceTemperature_SAVE_ID, parmUp.refTemp)
            editor.putString(SettingConfig.TemperatureCoefficient_SAVE_ID, parmUp.tempCompensate)
            editor.putString(SettingConfig.autoPowerOff_SAVE_ID, parmUp.autoPowerTime)
            editor.putString(SettingConfig.PH_DueCalibration_SAVE_ID, parmUp.phDuCalTime)
            editor.putString(SettingConfig.COND_DueCalibration_SAVE_ID, parmUp.condDuCalTime)
            //    public final static String ReferenceTemperature_SAVE_ID = "param:COND:ReferenceTemperature";
            //    public final static String TemperatureCoefficient_SAVE_ID = "param:COND:TemperatureCoefficient";

            editor.apply()
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onEvent(event: SettingDeviceEvent) {
        try {
            val sharedPreferences = MyApi.getInstance().dataApi.setting
            val parmDown = ParmDown()
            parmDown.setPHSelect(sharedPreferences.getString(SettingConfig.ph_selection_SAVE_ID, ""))
            parmDown.setPHResolution(sharedPreferences.getString(SettingConfig.ph_resolution_SAVE_ID, ""))
            parmDown.setAutoHold(sharedPreferences.getString(SettingConfig.autoHold_SAVE_ID, ""))
            parmDown.setBackLight(sharedPreferences.getString(SettingConfig.backLight_SAVE_ID, ""))
            parmDown.setTDSFactor(sharedPreferences.getString(SettingConfig.TDSFactor_SAVE_ID, ""))
            parmDown.setSaltUnit(sharedPreferences.getString(SettingConfig.salt_unit_SAVE_ID, ""))
            parmDown.setTempUnit(sharedPreferences.getString(SettingConfig.tempUnit_SAVE_ID, ""))
            parmDown.setRefTemp(sharedPreferences.getString(SettingConfig.ReferenceTemperature_SAVE_ID, ""))
            parmDown.setTempCompensate(sharedPreferences.getString(SettingConfig.TemperatureCoefficient_SAVE_ID, ""))
            parmDown.setPowerOff(sharedPreferences.getString(SettingConfig.autoPowerOff_SAVE_ID, ""))
            parmDown.setPhDueTime(sharedPreferences.getString(SettingConfig.PH_DueCalibration_SAVE_ID, ""))
            parmDown.setCondDueTime(sharedPreferences.getString(SettingConfig.COND_DueCalibration_SAVE_ID, ""))

            mHandler.post { MyApi.getInstance().btApi.sendCommand(parmDown) }

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onEvent(event: BleDeviceConnectEvent) {
        val bleDevice = MyApi.getInstance().btApi.lastDevice
        if (bleDevice != null) {
            MyApi.getInstance().dataApi.updateBleDevice(bleDevice, if (event.isConnected) DataApi.CONNECTED else DataApi.DISCONNECTED)
            if (event.isConnected) {
                val sharedPreferences = MyApi.getInstance().dataApi.setting
                var gps =true;// "ON" == sharedPreferences.getString(SettingConfig.gps_SAVE_ID, "OFF")

                if (gps) {

                    getLocation();
                } else {

                }
                sharedPreferences.edit().putString("connectedDev",JSON.toJSONString(bleDevice)).apply()
            } else {
                mLocationManager?.removeUpdates(locationListener);

            }

        }

        if (!event.isConnected) {
            mHandler.postDelayed({
                if (!MyApi.getInstance().btApi.isConnected) {
                    val sharedPreferences = MyApi.getInstance().dataApi.setting
                    var connectedDev = sharedPreferences.getString("connectedDev", "")

                    if (!TextUtils.isEmpty(connectedDev) && connectedDev!!.startsWith("{")) {
                        val bleDevice = Utils.parse(connectedDev);
                        MyApi.getInstance().btApi.connect(bleDevice)
                    } else {

                    }
                } else {
                }
            }, 10000);

        }


    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onEvent(event: BleDeviceFoundEvent) {

    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onEvent(event: StartEvent) {
        val restApi = MyApi.getInstance().restApi
        if (restApi.isLogin) {
            //restApi.checkdataList()
            //restApi.conducitivitydataList()
            //restApi.orpdataList()
            restApi.downloadList()
            // restApi.resistivitydataList()
            // restApi.salinitydataList()
            //  restApi.tdsdataList()
            EventBus.getDefault().post(SyncEventUpload())
        } else {
        }
        if (!MyApi.getInstance().btApi.isConnected) {
            val sharedPreferences = MyApi.getInstance().dataApi.setting
            var connectedDev = sharedPreferences.getString("connectedDev", "")

            if (!TextUtils.isEmpty(connectedDev) && connectedDev!!.startsWith("{")) {
                val bleDevice = Utils.parse(connectedDev);
                MyApi.getInstance().btApi.connect(bleDevice)
            } else {
            }
        } else {
        }
    }


    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    //保存更新数据使用
    fun onEvent(event: SyncEventUpload) {

        val restApi = MyApi.getInstance().restApi
        restApi.updateLoginTime()

        if (restApi.isLogin) {
            restApi.syncUpload();

        } else {
            EventBus.getDefault().post(LogoutEvent())
        }
    }
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onEvent(event: FactoryEvent) {
        val api = MyApi.getInstance().btApi
        if (api.isConnected) {
            mHandler.post({
                //var p = ParmDown.factoryReset(ParmDown.TYPE1)
                val sharedPreferences = MyApi.getInstance().dataApi.setting
                val parmDown = ParmDown.factoryReset(event.type)
                parmDown.setPHSelect(sharedPreferences.getString(SettingConfig.ph_selection_SAVE_ID, ""))
                parmDown.setPHResolution(sharedPreferences.getString(SettingConfig.ph_resolution_SAVE_ID, ""))
                parmDown.setAutoHold(sharedPreferences.getString(SettingConfig.autoHold_SAVE_ID, ""))
                parmDown.setBackLight(sharedPreferences.getString(SettingConfig.backLight_SAVE_ID, ""))
                parmDown.setTDSFactor(sharedPreferences.getString(SettingConfig.TDSFactor_SAVE_ID, ""))
                parmDown.setSaltUnit(sharedPreferences.getString(SettingConfig.salt_unit_SAVE_ID, ""))
                parmDown.setTempUnit(sharedPreferences.getString(SettingConfig.tempUnit_SAVE_ID, ""))
                parmDown.setRefTemp(sharedPreferences.getString(SettingConfig.ReferenceTemperature_SAVE_ID, ""))
                parmDown.setTempCompensate(sharedPreferences.getString(SettingConfig.TemperatureCoefficient_SAVE_ID, ""))

                api.sendCommand(parmDown)
            })
        } else {
        }
    }
    //

    @Subscribe
    fun onEvent(event: GpsEventStart) {
        if (event.switch) {
            getLocation()
        } else {
            mLocationManager?.removeUpdates(locationListener);
        }

        //
    }

    private var mLocationManager: LocationManager? = null
    private var locateType = LocationManager.GPS_PROVIDER
    /** this criteria needs high accuracy, high power and cost  */
    fun createFineCriteria(): Criteria {
        val c = Criteria()
        c.accuracy = Criteria.ACCURACY_FINE//高精度
        c.isAltitudeRequired = true//包含高度信息
        c.isBearingRequired = false//包含方位信息
        c.isSpeedRequired = false//包含速度信息
        c.isCostAllowed = true//允许付费
        c.powerRequirement = Criteria.POWER_LOW//高耗电
        return c
    }

    private val locationListener = object : LocationListener {
        /**
         * 位置信息变化时触发:当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
         * @param location
         */
        override fun onLocationChanged(location: Location) {
            //Toast.makeText(MainActivity.this, "onLocationChanged函数被触发！", Toast.LENGTH_SHORT).show();
            //updateUI(location);
//            Log.i(TAG, "时间：" + location.time)
//            Log.i(TAG, "经度：" + location.longitude)
//            Log.i(TAG, "纬度：" + location.latitude)
//            Log.i(TAG, "海拔：" + location.altitude)
            updateGPS(location)
            //  updateUI(location)
        }


        /**
         * GPS状态变化时触发:Provider被disable时触发此函数，比如GPS被关闭
         * @param provider
         * @param status
         * @param extras
         */
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            when (status) {
            //GPS状态为可见时
                LocationProvider.AVAILABLE -> {
                }
            //GPS状态为服务区外时
                LocationProvider.OUT_OF_SERVICE -> {
                }
            //GPS状态为暂停服务时
                LocationProvider.TEMPORARILY_UNAVAILABLE -> {
                }
            }//Toast.makeText(MainActivity.this, "onStatusChanged：当前GPS状态为可见状态", Toast.LENGTH_SHORT).show();
            //  Toast.makeText(MainActivity.this, "onStatusChanged:当前GPS状态为服务区外状态", Toast.LENGTH_SHORT).show();
            //   Toast.makeText(MainActivity.this, "onStatusChanged:当前GPS状态为暂停服务状态", Toast.LENGTH_SHORT).show();
        }

        /**
         * 方法描述：GPS开启时触发
         * @param provider
         */
        override fun onProviderEnabled(provider: String) {
            //Toast.makeText(MainActivity.this, "onProviderEnabled:方法被触发", Toast.LENGTH_SHORT).show();
            getLocation()
        }

        /**
         * 方法描述： GPS禁用时触发
         * @param provider
         */
        override fun onProviderDisabled(provider: String) {
            Log.d("GPS","GPSbukeyong")
        }
    }

    private fun updateGPS(location: Location) {
        MyApi.getInstance().dataApi.putLocation(location)
        //EventBus.getDefault().post(GpsEventNewLocation(location))

    }


    private fun getLocation() {
        mHandler.post {
            try {
                if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    if (mLocationManager == null) {
                        mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                        locateType = mLocationManager!!.getBestProvider(createFineCriteria(), true)!!.toString()
                    }
                    Log.i(TAG, locateType)
                    val location = mLocationManager?.getLastKnownLocation(locateType) // 通过GPS获取位置
                    if (location != null) {
                        updateGPS(location)
                    }
                    // 设置监听*器，自动更新的最小时间为间隔N秒(1秒为1*1000，这样写主要为了方便)或最小位移变化超过N米
                    mLocationManager?.requestLocationUpdates(locateType, 100, 0f,
                            locationListener)
                }else {
                }
            } catch (e: Exception) {
            }
        }
    }

    companion object {
        internal val TAG = "BleService"
    }
}

private fun Handler.postDelayed(runnable: Runnable) {

}
