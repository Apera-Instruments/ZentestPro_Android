package com.zen.biz;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.text.TextUtils;
import android.util.Log;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleReadCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.zen.api.BtApi;
import com.zen.api.DataApi;
import com.zen.api.MyApi;
import com.zen.api.data.DeviceSetting;
import com.zen.api.event.BleDeviceConnectEvent;
import com.zen.api.event.BleDeviceFoundEvent;
import com.zen.api.event.UpdateEvent;
import com.zen.api.protocol.CalibrationCond;
import com.zen.api.protocol.CalibrationPh;
import com.zen.api.protocol.Command;
import com.zen.api.protocol.Convent;
import com.zen.api.protocol.Data;
import com.zen.api.protocol.Error;
import com.zen.api.protocol.Mode;
import com.zen.api.protocol.ParmUp;
import com.zen.api.protocol.Shutdown;
import com.zen.api.protocol.Time;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;

import com.zen.api.event.ModePatternChangedFromDevice;
import com.zen.api.protocol.velaprotocal.VelaDataInfo;
import com.zen.api.protocol.Version;
import com.zen.api.protocol.velaprotocal.VelaParamModeDeviceToApp;

import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_NOTIFY;

//https://github.com/Jasonchenlijian/FastBle
public class BtApiImpl implements BtApi {
    /*
    * 写：service uuid  FFE5  characteristic FFE9
      读：service uuid  FFE0  characteristic FFE4
    * */
    private static final String WRITE_SERVICE_UUID = "0000ffe5-0000-1000-8000-00805f9b34fb";
    private static final String WRITE_CHARAC_UUID = "0000ffe9-0000-1000-8000-00805f9b34fb";
    private static final String READ_SERVICE_UUID = "0000ffe0-0000-1000-8000-00805f9b34fb";
    private static final String READ_CHARAC_UUID = "0000ffe4-0000-1000-8000-00805f9b34fb";
    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(3);
    public static final String TAG = "BtApiImpl";
    private boolean mScanning = false;
    private volatile boolean mConnected = false;
    private com.zen.api.data.BleDevice mCurrentDevice;
    private com.zen.api.data.BleDevice mLastDevice;
    private List<BleDevice> mScanResultList;
    private Map<String, BleDevice> bleDeviceMap = new ConcurrentHashMap<>();
    // Store all connected devices
    private final Map<String, BleDevice> multiConnectedDevices = new ConcurrentHashMap<>();
    // NStore all ZenDevice wrappers
    private final Map<String, com.zen.api.data.BleDevice> multiZenDevices = new ConcurrentHashMap<>();
    private boolean mDemoConnected;
    private BleDevice mCurrentBleDevice;
    private String writeUuidService;
    private String writeUuidCharacteristic;
    private String readUuidService;
    private String readUuidCharacteristic;
    private int mDemoMode;
    private boolean mConnecting;
    private long mConnectUpdateTime;
    private CalibrationCond calibrationCond;
    private CalibrationPh calibrationPh;
    private boolean mDisConnect = false;
    private int mCurrentMode;

    public Version getDeviceVersion() {
        return mDeviceVersion;
    }

    @NotNull
    @Override
    public float getLife() {
        if (calibrationPh != null) {
            return (float) calibrationPh.getSlope1();
        }
        return 0.99f;
    }

    private Version mDeviceVersion;

    @Override
    public void startScan() {
        BleManager.getInstance().scan(new BleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {
                // 开始扫描（主线程）
                mScanning = true;
            }

            @Override
            public void onScanning(BleDevice bleDevice) {
                // 扫描到一个符合扫描规则的BLE设备（主线程）

                bleDeviceMap.put(bleDevice.getMac(), bleDevice);
                String localName = getLocalName(bleDevice.getName());
                if (!TextUtils.isEmpty(localName)) {
                    BleDeviceFoundEvent event = new BleDeviceFoundEvent(localName, bleDevice.getMac(), bleDevice.getRssi());
                    EventBus.getDefault().post(event);
                }
            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
                // 扫描结束，列出所有扫描到的符合扫描规则的BLE设备（主线程）
                mScanResultList = scanResultList;
                mScanning = false;
            }
        });
    }

    public boolean isModePH() {
        return (mCurrentMode != Data.mV || (!isDemo())) && mModePH;
    }


    public boolean isModeSAL() {
        return (mCurrentMode != Data.mV || (!isDemo())) && mModeSAL;
    }


    public boolean isModeORP() {
        return (mCurrentMode == Data.mV || (!isDemo())) && mModeORP;
    }


    public boolean isModeTDS() {
        return (mCurrentMode != Data.mV || (!isDemo())) && mModeTDS;
    }


    public boolean isModeCOND() {
        return (mCurrentMode != Data.mV || (!isDemo())) && mModeCOND;
    }


    public boolean isModeRES() {
        return (mCurrentMode != Data.mV || (!isDemo())) && mModeRES;
    }

    @Override
    public boolean open() {
        try {
            BleManager.getInstance().enableBluetooth();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean isDemo() {
        return mCurrentDevice == null || !mCurrentDevice.isDemo();
    }

    private boolean mModePH = true;
    private boolean mModeSAL = true;
    private boolean mModeORP = true;
    private boolean mModeTDS = true;
    private boolean mModeCOND = true;
    private boolean mModeRES = true;

    private String getLocalName(String name) {
        if (name == null) return null;
        if (name.startsWith("6") && name.length() >= 10) {
            mModePH = false;
            mModeSAL = false;
            mModeORP = false;
            mModeTDS = false;
            mModeCOND = false;
            mModeRES = false;
            // String sn = name.substring(0,2+4);
            String sn2 = name.substring(name.length() - 4);
            if (name.startsWith("60")) {
                mModePH = true;
                mModeORP = true;
                return "PH60" + "-" + sn2;// ph ORP
            } else if (name.startsWith("61")) {// ph ORP
                mModePH = true;
                mModeORP = true;
                return "PH60S" + "-" + sn2;
            } else if (name.startsWith("62")) {// ph ORP
                mModePH = true;
                mModeORP = true;
                return "PH60F" + "-" + sn2;
            } else if (name.startsWith("63")) {//  ORP

                mModeORP = true;
                return "ORP60" + "-" + sn2;
            } else if (name.startsWith("64")) {
                mModePH = true;
                return "PHO60" + "-" + sn2;
            } else if (name.startsWith("65")) { //电导率  TDS 营度 电阻率
                mModeCOND = true;
                mModeSAL = true;
                mModeRES = true;
                mModeTDS = true;
                return "EC60" + "-" + sn2;
            } else if (name.startsWith("66")) {
                mModeCOND = true;
                mModeSAL = true;
                mModeRES = true;
                mModeTDS = true;
                return "ECS60" + "-" + sn2;
            } else if (name.startsWith("67")) {//ph 电导率  TDS 营度 电阻率 ORP
                mModePH = true;
                mModeCOND = true;
                mModeSAL = true;
                mModeRES = true;
                mModeTDS = true;
                mModeORP = true;
                return "PC60" + "-" + sn2;
            } else if (name.startsWith("68")) {
                mModePH = true;
                mModeCOND = true;
                mModeSAL = true;
                mModeRES = true;
                mModeTDS = true;
                mModeORP = true;
                return "PCS60" + "-" + sn2;
            }
        } else {
            if (name.startsWith("24") && name.length() >= 10) {
                String sn2 = name.substring(name.length() - 4);
                mModePH = true;
                mModeCOND = true;
                mModeSAL = true;
                mModeRES = true;
                mModeTDS = true;
                mModeORP = true;

                return "UWS_Waterboy" + "-" + sn2;
            }
        }
        return null;
    }

    @Override
    public boolean isScanning() {
        return mScanning;
    }

    public Map<String, BleDevice> getConnectedBleDevices() {
        return multiConnectedDevices;
    }

    public Map<String, com.zen.api.data.BleDevice> getConnectedZenDevices() {
        return multiZenDevices;
    }

    public void disconnectDevice(String mac) {
        BleDevice d = multiConnectedDevices.remove(mac);
        if (d != null) {
            BleManager.getInstance().disconnect(d);
            multiZenDevices.remove(mac);
            Log.i(TAG, "Disconnected individual device: " + mac);
        }
    }

    @Override
    public int connect(com.zen.api.data.BleDevice device) {
        /*
         *
         * BluetoothGatt connect(BleDevice bleDevice, BleGattCallback bleGattCallback)
         *
         * */
        if (device == null) {
            return 0;
        }
        mDisConnect = false;
        if (mConnected) {
            return 0;
        }

        if (mConnecting && (System.currentTimeMillis() - mConnectUpdateTime < 10000)) {

            return 0;
        }
        if (device.isDemo()) {
            mConnected = true;
            mDemoConnected = true;
            mConnecting = false;
            initDemoValue();
            startUpdateDataSch();
            mCurrentDevice = device;
            mCurrentDevice.setSN(mCurrentDevice.getName());
            mLastDevice = device;
            mModePH = true;
            mModeSAL = true;
            mModeORP = true;
            mModeTDS = true;
            mModeCOND = true;
            mModeRES = true;
            EventBus.getDefault().post(new BleDeviceConnectEvent(mConnected));
        } else {
            mDemoConnected = false;
            if (!BleManager.getInstance().isBlueEnable()) {
                return 0;
            }

            mConnecting = true;
            mConnectUpdateTime = System.currentTimeMillis();
            BleDevice bleDevice = bleDeviceMap.get(device.getMac());
            if (bleDevice == null && BluetoothAdapter.checkBluetoothAddress(device.getMac())) {
                BluetoothDevice bluetoothDevice = BleManager.getInstance().getBluetoothAdapter().getRemoteDevice(device.getMac());
                bleDevice = new BleDevice(bluetoothDevice);
            }
            if (bleDevice != null) {
                device.setName(getLocalName(bleDevice.getName()));
                device.setModle(getModelName(bleDevice.getName()));
                device.setSN(bleDevice.getName());
                BleManager.getInstance().connect(bleDevice, new BleGattCallback() {
                    @Override
                    public void onStartConnect() {
                        // 开始连接

                        mConnecting = true;
                        mConnectUpdateTime = System.currentTimeMillis();
                    }

                    @Override
                    public void onConnectFail(BleException exception) {
                        // 连接失败

                        mConnected = false;
                        mConnecting = false;
                        mConnectUpdateTime = System.currentTimeMillis();
                        EventBus.getDefault().post(new BleDeviceConnectEvent(mConnected));
                    }

                    @Override
                    public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                        // 连接成功，BleDevice即为所连接的BLE设备


                        /*
                        *
02-27 19:37:11.602 5366-5366/com.zen.zentest I/BtApiImpl: GattService:00001800-0000-1000-8000-00805f9b34fb   Generic Access
02-27 19:37:11.603 5366-5366/com.zen.zentest I/BtApiImpl: characteristic:00002a00-0000-1000-8000-00805f9b34fb Device Name
                                                          characteristic:00002a01-0000-1000-8000-00805f9b34fb Appearance
                                                          characteristic:00002a02-0000-1000-8000-00805f9b34fb
                                                          characteristic:00002a04-0000-1000-8000-00805f9b34fb
02-27 19:37:11.604 5366-5366/com.zen.zentest I/BtApiImpl: GattService:00001801-0000-1000-8000-00805f9b34fb   Generic Attribute
                                                          characteristic:00002a05-0000-1000-8000-00805f9b34fb

                                                          GattService:0000ffe5-0000-1000-8000-00805f9b34fb      WRITE_SERVICE_UUID
                                                          characteristic:0000ffe9-0000-1000-8000-00805f9b34fb   write
02-27 19:37:11.605 5366-5366/com.zen.zentest I/BtApiImpl: GattService:0000ffe0-0000-1000-8000-00805f9b34fb      READ_SERVICE_UUID
                                                          characteristic:0000ffe4-0000-1000-8000-00805f9b34fb   READ_CHARAC_UUID notify
                                                          characteristic:0000fff0-0000-1000-8000-00805f9b34fb   read notify
02-27 19:37:11.606 5366-5366/com.zen.zentest I/BtApiImpl: GattService:0000ffc0-0000-1000-8000-00805f9b34fb
                                                          characteristic:0000ffc1-0000-1000-8000-00805f9b34fb write
                                                          characteristic:0000ffc2-0000-1000-8000-00805f9b34fb  notify
                        * */
                        mConnected = true;
                        mConnecting = false;
                        mConnectUpdateTime = System.currentTimeMillis();
                        mCurrentBleDevice = bleDevice;
                        // mLastDevice = mCurrentDevice;
                        EventBus.getDefault().post(new BleDeviceConnectEvent(mConnected));
                        List<BluetoothGattService> list = gatt.getServices();
                        if (list != null && !list.isEmpty()) {
                            for (BluetoothGattService bluetoothGattService : list) {
                                String uuid_service = bluetoothGattService.getUuid().toString();

                                if (uuid_service.equalsIgnoreCase(WRITE_SERVICE_UUID)) {
                                    writeUuidService = uuid_service;
                                }
                                if (uuid_service.equalsIgnoreCase(READ_SERVICE_UUID)) {
                                    readUuidService = uuid_service;
                                }
                                List<BluetoothGattCharacteristic> characteristics = bluetoothGattService.getCharacteristics();

                                if (characteristics != null && !characteristics.isEmpty()) {
                                    for (BluetoothGattCharacteristic characteristic : characteristics) {
                                        String uuid_characteristic = characteristic.getUuid().toString();

                                        if (uuid_service == writeUuidService) {
                                            if (uuid_characteristic.equalsIgnoreCase(WRITE_CHARAC_UUID)) {
                                                writeUuidCharacteristic = uuid_characteristic;


                                            }
                                        }
                                        if (uuid_service == readUuidService) {
                                            if (uuid_characteristic.equalsIgnoreCase(READ_CHARAC_UUID)) {
                                                readUuidCharacteristic = uuid_characteristic;
                                                if ((characteristic.getProperties() & PROPERTY_NOTIFY) == PROPERTY_NOTIFY) {

                                                    BleManager.getInstance().notify(
                                                            bleDevice,
                                                            uuid_service,
                                                            uuid_characteristic,
                                                            new BleNotifyCallback() {
                                                                @Override
                                                                public void onNotifySuccess() {
                                                                    // 打开通知操作成功

                                                                    Time time = new Time();

                                                                    sendCommand(time, new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                            //sendCommand(new Key(Key.ESC));

                                                                        }
                                                                    });

                                                                }

                                                                @Override
                                                                public void onNotifyFailure(BleException exception) {
                                                                    // 打开通知操作失败

                                                                }

                                                                @Override
                                                                public void onCharacteristicChanged(byte[] data) {
                                                                    // 打开通知后，设备发过来的数据将在这里出现
                                                                    Log.i("BtApiImpl", "Notification received: " + Arrays.toString(data));

                                                                    try {
                                                                        Object object = Command.unpack(data);

//                                                                        try {
//                                                                            Log.i("BtApiImpl", "Decoded content: " + JSON.toJSONString(object, true));
//                                                                        } catch (Exception je) {
//                                                                            Log.i("BtApiImpl", "Decoded content (toString): " + object.toString());
//                                                                        }

                                                                        if (object != null && object instanceof Convent) {
                                                                            if (object instanceof Data) {
                                                                                Log.i("BtApiImpl", "Data Instance");
                                                                                MyApi.getInstance().getDataApi().setLastData((Data) object);
                                                                                mCurrentMode = ((Data) object).getMode();
                                                                            } else if (object instanceof Shutdown) {
                                                                                if (((Shutdown) object).isOFF()) {
                                                                                    disconnect();
                                                                                }
                                                                            } else if (object instanceof ParmUp) {
                                                                                MyApi.getInstance().getDataApi().setLastParmUp((ParmUp) object);
                                                                            } else if (object instanceof CalibrationCond) {
                                                                                DeviceSetting deviceSetting = mCurrentDevice.getSetting();
                                                                                if (deviceSetting != null) {
                                                                                    deviceSetting.getCalibration().setCalibrationCond((CalibrationCond) object);
                                                                                }
                                                                                MyApi.getInstance().getDataApi().setLastCalibrationCond((CalibrationCond) object);
                                                                                MyApi.getInstance().getDataApi().updateBleDevice(mCurrentDevice, DataApi.SETTING);
                                                                            } else if (object instanceof CalibrationPh) {
                                                                                DeviceSetting deviceSetting = mCurrentDevice.getSetting();
                                                                                if (deviceSetting != null) {
                                                                                    deviceSetting.getCalibration().setCalibrationPh((CalibrationPh) object);
                                                                                }
                                                                                MyApi.getInstance().getDataApi().setLastCalibrationPh((CalibrationPh) object);
                                                                                MyApi.getInstance().getDataApi().updateBleDevice(mCurrentDevice, DataApi.SETTING);
                                                                            } else if (object instanceof Error) {
                                                                                Error error = (Error) object;
                                                                                MyApi.getInstance().getDataApi().putReminder(error);

                                                                            } else if (object instanceof Version) {
                                                                                mDeviceVersion = (Version) object;

                                                                            } else if (object instanceof VelaDataInfo) {
                                                                                VelaDataInfo vela = (VelaDataInfo) object;
                                                                                Data dataPack = vela.toMeasureDataPack();
                                                                                MyApi.getInstance().getDataApi().setLastData(dataPack);
                                                                                mCurrentMode = (dataPack).getMode();

                                                                                Log.i("BtApiImpl", "Received VelaData: " + vela.toString());
//                                                                                MyApi.getInstance().getDataApi().setLastData(convertToOldData(vela)); // optional adapter
                                                                            } else if (object instanceof VelaParamModeDeviceToApp) {
                                                                                VelaParamModeDeviceToApp modeUpload = (VelaParamModeDeviceToApp) object;

                                                                                // Notify MeasureFragmentVela
                                                                                EventBus.getDefault().post(new ModePatternChangedFromDevice());

                                                                                Log.i("BtApiImpl", "Received Mode Upload: " + modeUpload.toString());

                                                                                MyApi.getInstance().getDataApi().setLastVelaModeUpload(modeUpload);
                                                                            } else {
                                                                                Log.i("BtApiImpl", "Object Instance not found");
                                                                            }
                                                                            //ParmUp
                                                                            //CalibrationPh
                                                                            //CalibrationCond
                                                                            EventBus.getDefault().post(new UpdateEvent((Convent) object));

                                                                        }
                                                                    } catch (Exception e) {
                                                                        Log.i("BtApiImpl", "Decode Error");
                                                                    }
                                                                }
                                                            });
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }


                        /*
                        * 连接后以APP发送同步时间，仪器会上传参数列表和校正信息。
                          切换测量参数时会上传校正信息。
                        * */

                   /*     String uuid_service="";
                        String uuid_characteristic_notify="";
                        if(!TextUtils.isEmpty(uuid_service) && TextUtils.isEmpty(uuid_characteristic_notify) )
                        {

                        }*/

                    }

                    @Override
                    public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {
                        // 连接中断，isActiveDisConnected表示是否是主动调用了断开连接方法

                        mConnected = false;
                        mConnecting = false;

                        mCurrentDevice = null;

                        writeUuidService = null;
                        writeUuidCharacteristic = null;
                        readUuidService = null;
                        readUuidCharacteristic = null;
                        mConnectUpdateTime = System.currentTimeMillis();
                        MyApi.getInstance().getDataApi().setReminder(0);
                        EventBus.getDefault().post(new BleDeviceConnectEvent(mConnected));

                        if (!mDisConnect) {
                            boolean ret = gatt.connect();

                        }
                    }
                });
                mCurrentDevice = device;
                mLastDevice = device;
            }
        }

        return 1;
    }

    public int connectAdditional(com.zen.api.data.BleDevice device) {
        if (device == null) return 0;

        BleDevice bleDevice = bleDeviceMap.get(device.getMac());
        if (bleDevice == null && BluetoothAdapter.checkBluetoothAddress(device.getMac())) {
            BluetoothDevice bluetoothDevice = BleManager.getInstance().getBluetoothAdapter().getRemoteDevice(device.getMac());
            bleDevice = new BleDevice(bluetoothDevice);
        }

        if (bleDevice == null) return 0;

        BleManager.getInstance().connect(bleDevice, new BleGattCallback() {

            @Override
            public void onStartConnect() { }

            @Override
            public void onConnectFail(BleException exception) {
                Log.e(TAG, "Multi-connect FAILED for: " + device.getMac());
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                multiConnectedDevices.put(device.getMac(), bleDevice);
                multiZenDevices.put(device.getMac(), device);

                Log.i(TAG, "Multi-connect SUCCESS: " + device.getMac());

                // Do NOT touch mCurrentBleDevice or mCurrentDevice.
                // Let all existing code continue working on the "current" device.

                // If you want notifications also:
                subscribeNotificationsForAdditionalDevice(bleDevice, gatt);
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {
                multiConnectedDevices.remove(device.getMac());
                multiZenDevices.remove(device.getMac());
                Log.i(TAG, "Multi-device disconnected: " + device.getMac());
            }
        });

        return 1;
    }

    private void subscribeNotificationsForAdditionalDevice(BleDevice bleDevice, BluetoothGatt gatt) {
        for (BluetoothGattService service : gatt.getServices()) {
            for (BluetoothGattCharacteristic ch : service.getCharacteristics()) {
                if ((ch.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {

                    BleManager.getInstance().notify(
                            bleDevice,
                            service.getUuid().toString(),
                            ch.getUuid().toString(),
                            new BleNotifyCallback() {

                                @Override
                                public void onNotifySuccess() {
                                    Log.i(TAG, "Notify enabled (multi-device): " + bleDevice.getMac());
                                }

                                @Override
                                public void onNotifyFailure(BleException exception) {
                                    Log.e(TAG, "Notify FAILED (multi-device): " + bleDevice.getMac());
                                }

                                @Override
                                public void onCharacteristicChanged(byte[] data) {
                                    Log.i(TAG, "Multi-device data from " + bleDevice.getMac() + ": " + Arrays.toString(data));

                                    try {
                                        Object object = Command.unpack(data);
                                        if (object instanceof Convent) {
                                            EventBus.getDefault().post(new UpdateEvent((Convent) object));
                                        }
                                    } catch (Exception e) {
                                        Log.e(TAG, "Multi-decode error", e);
                                    }
                                }
                            }
                    );
                }
            }
        }
    }

    private String getModelName(String name) {
        if (name == null) return null;
        if (name.startsWith("6") && name.length() >= 10) {
            // String sn = name.substring(0,2+4);
            String sn2 = "Z"; //name.substring(name.length()-4);
            if (name.startsWith("60")) {
                return "PH60" + "-" + sn2;
            } else if (name.startsWith("61")) {
                return "PH60S" + "-" + sn2;
            } else if (name.startsWith("62")) {
                return "PH60F" + "-" + sn2;
            } else if (name.startsWith("63")) {
                return "ORP60" + "-" + sn2;
            } else if (name.startsWith("64")) {
                return "PHO60" + "-" + sn2;
            } else if (name.startsWith("65")) {
                return "EC60" + "-" + sn2;
            } else if (name.startsWith("66")) {
                return "ECS60" + "-" + sn2;
            } else if (name.startsWith("67")) {
                return "PC60" + "-" + sn2;
            } else if (name.startsWith("68")) {
                return "PCS60" + "-" + sn2;
            }
        } else {
            if (name.startsWith("24")) {
                return "UWS_Waterboy";
            }
        }
        return null;
    }

    /*
    * void notify(BleDevice bleDevice, String uuid_service, String uuid_notify, BleNotifyCallback callback)

  BleManager.getInstance().notify(
          bleDevice,
          uuid_service,
          uuid_characteristic_notify,
          new BleNotifyCallback() {
              @Override
              public void onNotifySuccess() {
                  // 打开通知操作成功
              }

              @Override
              public void onNotifyFailure(BleException exception) {
                  // 打开通知操作失败
              }

              @Override
              public void onCharacteristicChanged(byte[] data) {
                  // 打开通知后，设备发过来的数据将在这里出现
              }
          });


（方法说明）取消订阅通知notify，并移除数据接收的回调监听

boolean stopNotify(BleDevice bleDevice, String uuid_service, String uuid_notify)

  BleManager.getInstance().stopNotify(uuid_service, uuid_characteristic_notify);

    *
    * */



    /*
    *
    *
    *   BleManager.getInstance().write(
          bleDevice,
          uuid_service,
          uuid_characteristic_write,
          data,
          new BleWriteCallback() {
              @Override
              public void onWriteSuccess(int current, int total, byte[] justWrite) {
                  // 发送数据到设备成功
              }

              @Override
              public void onWriteFailure(BleException exception) {
                  // 发送数据到设备失败
              }
          });

    *
    *
    *
    * */

    /*
    *
    *
    * void read(BleDevice bleDevice, String uuid_service, String uuid_read, BleReadCallback callback)

  BleManager.getInstance().read(
          bleDevice,
          uuid_service,
          uuid_characteristic_read,
          new BleReadCallback() {
              @Override
              public void onReadSuccess(byte[] data) {
                  // 读特征值数据成功
              }

              @Override
              public void onReadFailure(BleException exception) {
                  // 读特征值数据失败
              }
          });

    *
    *
    * */


    @Override
    public int disconnect() {
        mConnecting = false;
        if (mCurrentDevice == null) {

            BleManager.getInstance().disconnectAllDevice();
            return 0;
        }
        if (mCurrentDevice.isDemo()) {
            mConnected = false;
            mDemoConnected = false;
            EventBus.getDefault().post(new BleDeviceConnectEvent(mConnected));
            mCurrentDevice = null;
        } else {
            BleManager.getInstance().disconnectAllDevice();
            mConnected = false;
        }

        mDisConnect = true;
        return 1;
    }

    @Override
    public boolean isConnected() {
        return mConnected;
    }

    @Override
    public void stopScan() {
        BleManager.getInstance().cancelScan();
        mScanning = false;
    }

    @Override
    public boolean isBtEnable() {
        return BleManager.getInstance().isBlueEnable();
    }

    @Override
    public boolean sendCommand(Convent convent) {
        return sendCommand(convent, null);
    }


    public boolean sendCommand(Convent convent, final Runnable callback) {
        byte data[] = convent.pack();

        String uuid_service = writeUuidService;
        String uuid_characteristic_write = writeUuidCharacteristic;

        if (mConnected && !TextUtils.isEmpty(uuid_service) && !TextUtils.isEmpty(uuid_characteristic_write)) {
            BleManager.getInstance().write(
                    mCurrentBleDevice,
                    uuid_service,
                    uuid_characteristic_write,
                    data,
                    new BleWriteCallback() {

                        @Override
                        public void onWriteSuccess() {
                            if (callback != null) {
                                callback.run();
                            }
                        }

                        @Override
                        public void onWriteFailure(BleException exception) {
                            // 发送数据到设备失败

                        }
                    });
            return true;
        } else if (mDemoConnected && convent instanceof Mode) {
            mDemoMode = ((Mode) convent).getMode();
        } else {
        }
        return false;
    }

    @Override
    public boolean readCommand() {

        if (!TextUtils.isEmpty(readUuidService) && TextUtils.isEmpty(readUuidCharacteristic)) {
            BleManager.getInstance().read(mCurrentBleDevice, readUuidService, readUuidCharacteristic, new BleReadCallback() {
                @Override
                public void onReadSuccess(byte[] data) {

                    try {
                        Object object = Command.unpack(data);
                        if (object != null && object instanceof Convent) {
                            EventBus.getDefault().post(new UpdateEvent((Convent) object));
                        }
                    } catch (Exception e) {
                    }
                }

                @Override
                public void onReadFailure(BleException exception) {

                }
            });
            return true;
        }
        return false;
    }

    @Override
    public boolean isConnecting() {
        return false;
    }

    @Override
    public int reconnect() {
        return connect(mCurrentDevice == null ? mLastDevice : mCurrentDevice);
    }

    @Override
    public com.zen.api.data.BleDevice getLastDevice() {
        return mCurrentDevice == null ? mLastDevice : mCurrentDevice;
    }

    @Nullable
    @Override
    public String getDeviceNumber() {
        if (mCurrentDevice != null)
            return mCurrentDevice.getSN();
        return "0000";
    }

    private Random random = new Random();

    private void startUpdateDataSch() {
        scheduledThreadPoolExecutor.schedule(new Runnable() {
            @Override
            public void run() {
                if (mDemoConnected) {
                    demoUpdateData();
                    startUpdateDataSch();
                }

            }
        }, 1, TimeUnit.SECONDS);
    }

    private Data mDemoData = new Data();
    private int mDemoTemp = 0;
    private int mDemoPh = 0;
    private int mDemoMv = 0;
    private int mDemoTds = 0;
    private int mDemoRes = 0;
    private int mDemoCond = 0;
    private int mDemoSalt = 0;

    private void initDemoValue() {
        mDemoData.setL(random.nextBoolean());
        mDemoData.setM(random.nextBoolean());
        mDemoData.setH(random.nextBoolean());
        mDemoData.setUnit2(random.nextBoolean() ? Data.UNIT_C : Data.UNIT_F);
        mDemoTemp = random.nextInt(350) + 100;

        mDemoPh = random.nextInt(1400 - 140);
        mDemoMv = random.nextInt(2000 - 200) - 1000;
        mDemoTds = random.nextInt(10000 - 1000);
        mDemoRes = random.nextInt(100 - 10);
        mDemoCond = random.nextInt(2000 - 200);
        mDemoSalt = random.nextInt(1000 - 100);
        calibrationCond = new CalibrationCond();
        calibrationCond.setA84(true);//random.nextBoolean());
        calibrationCond.setA1288(true);//random.nextBoolean());
        calibrationCond.setA1413(true);//random.nextBoolean());
        calibrationCond.setDate(new Date());
        calibrationPh = new CalibrationPh();
        calibrationPh.setA168(false);
        calibrationPh.setA400(true);
        calibrationPh.setA1001(true);
        calibrationPh.setA1245(false);
        calibrationPh.setA700(true);
        calibrationPh.setDate(new Date());
        calibrationPh.setSlope2(random.nextInt(50));
        calibrationPh.setSlope1(random.nextInt(60));
        calibrationPh.setOffset1(random.nextInt(50));
        calibrationPh.setOffset2(random.nextInt(100));
        int count = 0;
        if (calibrationPh.is1245()) count++;
        if (calibrationPh.is400()) count++;
        if (calibrationPh.is1001()) count++;
        if (calibrationPh.is168()) count++;
        if (calibrationPh.is700()) count++;
        calibrationPh.setPointCount(count);
        MyApi.getInstance().getDataApi().setLastCalibrationCond(calibrationCond);
        MyApi.getInstance().getDataApi().setLastCalibrationPh(calibrationPh);
        MyApi.getInstance().getDataApi().setReminder(random.nextInt(4));
    }

    private void demoUpdateData() {
        Data data = mDemoData;
        data.setMode(mDemoMode);
        if (mDemoMode == Data.pH) {
            data.setPointDigit(2);
            data.setValue(mDemoPh + random.nextInt(140));
            data.setUnit(Data.UNIT_pH);
        } else if (mDemoMode == Data.mV) {//ORP
            data.setPointDigit(0);
            data.setValue(mDemoMv + random.nextInt(200));
            data.setUnit(Data.UNIT_mV);
        } else if (mDemoMode == Data.TDS) {
            data.setPointDigit(1);
            data.setValue(mDemoTds + random.nextInt(1000));
            data.setUnit(Data.UNIT_ppm);
        } else if (mDemoMode == Data.Res) {
            data.setPointDigit(0);
            data.setValue(mDemoRes + random.nextInt(1000));
            data.setUnit(Data.UNIT_Ω);
        } else if (mDemoMode == Data.Cond) {
            data.setPointDigit(1);
            data.setValue(mDemoCond + random.nextInt(200));
            data.setUnit(Data.UNIT_uS);
        } else if (mDemoMode == Data.Salt) {
            data.setPointDigit(2);
            data.setValue(mDemoSalt + random.nextInt(100));
            data.setUnit(Data.UNIT_ppt);
        } else {
            data.setPointDigit(0);
            data.setValue(random.nextInt(4000) - 2000);
        }

        data.setPointDigit2(1);
        data.setValue2(mDemoTemp + random.nextInt(50));

        MyApi.getInstance().getDataApi().setLastData(data);
        MyApi.getInstance().getDataApi().setLastCalibrationCond(calibrationCond);
        MyApi.getInstance().getDataApi().setLastCalibrationPh(calibrationPh);
        EventBus.getDefault().post(new UpdateEvent());
    }


}
