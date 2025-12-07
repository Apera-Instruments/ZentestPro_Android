package com.zen.biz.velabt.velaApi;

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
import com.zen.api.event.ModePatternChangedFromDevice;
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
import com.zen.api.protocol.Version;
import com.zen.api.protocol.velaprotocal.VelaDataInfo;
import com.zen.api.protocol.velaprotocal.VelaParamModeDeviceToApp;
import com.zen.biz.velabt.session.DeviceSession;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_NOTIFY;

// https://github.com/Jasonchenlijian/FastBle
public class BtApiVela implements BtApi {

    /*
     * 写：service uuid  FFE5  characteristic FFE9
     * 读：service uuid  FFE0  characteristic FFE4
     */
    private static final String WRITE_SERVICE_UUID = "0000ffe5-0000-1000-8000-00805f9b34fb";
    private static final String WRITE_CHARAC_UUID  = "0000ffe9-0000-1000-8000-00805f9b34fb";
    private static final String READ_SERVICE_UUID  = "0000ffe0-0000-1000-8000-00805f9b34fb";
    private static final String READ_CHARAC_UUID   = "0000ffe4-0000-1000-8000-00805f9b34fb";

    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(3);
    public static final String TAG = "BtApiVela";

    private boolean mScanning = false;
    private volatile boolean mConnected = false;
    private boolean mDemoConnected = false;
    private boolean mConnecting = false;
    private long mConnectUpdateTime;
    private boolean mDisConnect = false;
    private int mDemoMode;
    private int mCurrentMode;

    // Demo-only calibration (real devices also update these for getLife() compatibility)
    private CalibrationCond calibrationCond;
    private CalibrationPh calibrationPh;

    // Sessions
    private final Map<String, DeviceSession> sessionMap = new ConcurrentHashMap<>();
    private DeviceSession currentSession;
    private DeviceSession lastSession;

    // Scan results
    private List<BleDevice> mScanResultList;
    private Map<String, BleDevice> bleDeviceMap = new ConcurrentHashMap<>();

    // Multi-device maps (kept for API compatibility)
    // Store all connected additional devices
    private final Map<String, BleDevice> multiConnectedDevices = new ConcurrentHashMap<>();
    // Store all ZenDevice wrappers for additional devices
    private final Map<String, com.zen.api.data.BleDevice> multiZenDevices = new ConcurrentHashMap<>();

    // Mode flags
    private boolean mModePH = true;
    private boolean mModeSAL = true;
    private boolean mModeORP = true;
    private boolean mModeTDS = true;
    private boolean mModeCOND = true;
    private boolean mModeRES = true;

    private Random random = new Random();

    // Demo data fields
    private Data mDemoData = new Data();
    private int mDemoTemp = 0;
    private int mDemoPh = 0;
    private int mDemoMv = 0;
    private int mDemoTds = 0;
    private int mDemoRes = 0;
    private int mDemoCond = 0;
    private int mDemoSalt = 0;

    public Version getDeviceVersion() {
        if (currentSession != null) {
            return currentSession.version;
        }
        return null;
    }

    @NotNull
    @Override
    public float getLife() {
        if (calibrationPh != null) {
            return (float) calibrationPh.getSlope1();
        }
        return 0.99f;
    }

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
                    BleDeviceFoundEvent event = new BleDeviceFoundEvent(
                            localName,
                            bleDevice.getMac(),
                            bleDevice.getRssi()
                    );
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

    /**
     * Note: original semantics:
     *   return mCurrentDevice == null || !mCurrentDevice.isDemo();
     * i.e. "true" when NOT demo (or unknown). Keep same to avoid breaking logic.
     */
    private boolean isDemo() {
        if (currentSession == null || currentSession.zenDevice == null) {
            return true;
        }
        return !currentSession.zenDevice.isDemo();
    }

    private String getLocalName(String name) {
        if (name == null) return null;
        if (name.startsWith("6") && name.length() >= 10) {
            mModePH = false;
            mModeSAL = false;
            mModeORP = false;
            mModeTDS = false;
            mModeCOND = false;
            mModeRES = false;

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
            } else if (name.startsWith("65")) { // 电导率  TDS 营度 电阻率
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

    private String getModelName(String name) {
        if (name == null) return null;
        if (name.startsWith("6") && name.length() >= 10) {
            String sn2 = "Z"; // original used Z placeholder
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

    @Override
    public boolean isScanning() {
        return mScanning;
    }

    /**
     * Multi-device API compatibility: returns the map of additional devices.
     * For primary device, use currentSession.
     */
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
            sessionMap.remove(mac);
            Log.i(TAG, "Disconnected individual device: " + mac);
        }
    }

    @Override
    public int connect(com.zen.api.data.BleDevice device) {
        /*
         *
         * BluetoothGatt connect(BleDevice bleDevice, BleGattCallback bleGattCallback)
         *
         */
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
            // Demo session
            DeviceSession session = new DeviceSession();
            session.mac = "DEMO";
            session.zenDevice = device;
            session.isDemoDevice = true;
            session.connected = true;
            session.connecting = false;
            currentSession = session;
            lastSession = session;
            sessionMap.put(session.mac, session);

            mConnected = true;
            mDemoConnected = true;
            mConnecting = false;

            initDemoValue();
            startUpdateDataSch();

            // name / SN
            currentSession.zenDevice.setSN(currentSession.zenDevice.getName());

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
                BluetoothDevice bluetoothDevice = BleManager.getInstance()
                        .getBluetoothAdapter()
                        .getRemoteDevice(device.getMac());
                bleDevice = new BleDevice(bluetoothDevice);
            }
            if (bleDevice != null) {

                DeviceSession session = new DeviceSession();
                session.mac = device.getMac();
                session.zenDevice = device;
                session.bleDevice = bleDevice;

                // Set human-friendly name/model/SN
                device.setName(getLocalName(bleDevice.getName()));
                device.setModle(getModelName(bleDevice.getName()));
                device.setSN(bleDevice.getName());

                currentSession = session;
                lastSession = session;
                sessionMap.put(session.mac, session);

                BleManager.getInstance().connect(bleDevice, new BleGattCallback() {
                    @Override
                    public void onStartConnect() {
                        // 开始连接
                        mConnecting = true;
                        mConnectUpdateTime = System.currentTimeMillis();
                        session.connecting = true;
                    }

                    @Override
                    public void onConnectFail(BleException exception) {
                        // 连接失败
                        mConnected = false;
                        mConnecting = false;
                        mConnectUpdateTime = System.currentTimeMillis();
                        session.connected = false;
                        session.connecting = false;
                        EventBus.getDefault().post(new BleDeviceConnectEvent(mConnected));
                    }

                    @Override
                    public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                        // 连接成功，BleDevice即为所连接的BLE设备

                        mConnected = true;
                        mConnecting = false;
                        mConnectUpdateTime = System.currentTimeMillis();
                        session.connected = true;
                        session.connecting = false;
                        session.bleDevice = bleDevice;

                        EventBus.getDefault().post(new BleDeviceConnectEvent(mConnected));

                        List<BluetoothGattService> list = gatt.getServices();
                        if (list != null && !list.isEmpty()) {
                            for (BluetoothGattService bluetoothGattService : list) {
                                String uuid_service = bluetoothGattService.getUuid().toString();

                                if (uuid_service.equalsIgnoreCase(WRITE_SERVICE_UUID)) {
                                    session.writeService = uuid_service;
                                }
                                if (uuid_service.equalsIgnoreCase(READ_SERVICE_UUID)) {
                                    session.readService = uuid_service;
                                }
                                List<BluetoothGattCharacteristic> characteristics = bluetoothGattService.getCharacteristics();

                                if (characteristics != null && !characteristics.isEmpty()) {
                                    for (BluetoothGattCharacteristic characteristic : characteristics) {
                                        String uuid_characteristic = characteristic.getUuid().toString();

                                        if (uuid_service == session.writeService) {
                                            if (uuid_characteristic.equalsIgnoreCase(WRITE_CHARAC_UUID)) {
                                                session.writeCharacteristic = uuid_characteristic;
                                            }
                                        }
                                        if (uuid_service == session.readService) {
                                            if (uuid_characteristic.equalsIgnoreCase(READ_CHARAC_UUID)) {
                                                session.readCharacteristic = uuid_characteristic;
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
                                                                            // sendCommand(new Key(Key.ESC));
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
                                                                    Log.i(TAG, "Notification received: " + Arrays.toString(data));

                                                                    try {
                                                                        Object object = Command.unpack(data);

                                                                        if (object != null && object instanceof Convent) {
                                                                            Convent convent = (Convent) object;

                                                                            if (convent instanceof Data) {
                                                                                Log.i(TAG, "Data Instance");
                                                                                Data d = (Data) convent;
                                                                                mCurrentMode = d.getMode();
                                                                                session.currentMode = d.getMode();
                                                                                session.updateLastData(d);
                                                                                MyApi.getInstance().getDataApi().setLastData(d);

                                                                            } else if (convent instanceof Shutdown) {
                                                                                if (((Shutdown) convent).isOFF()) {
                                                                                    disconnect();
                                                                                }
                                                                            } else if (convent instanceof ParmUp) {
                                                                                MyApi.getInstance().getDataApi().setLastParmUp((ParmUp) convent);
                                                                            } else if (convent instanceof CalibrationCond) {
                                                                                CalibrationCond cond = (CalibrationCond) convent;
                                                                                calibrationCond = cond;
                                                                                DeviceSetting deviceSetting = session.getDeviceSetting();
                                                                                if (deviceSetting != null) {
                                                                                    deviceSetting.getCalibration().setCalibrationCond(cond);
                                                                                }
                                                                                session.updateCalibrationCond(cond);
                                                                                MyApi.getInstance().getDataApi().setLastCalibrationCond(cond);
                                                                                MyApi.getInstance().getDataApi().updateBleDevice(session.zenDevice, DataApi.SETTING);
                                                                            } else if (convent instanceof CalibrationPh) {
                                                                                CalibrationPh ph = (CalibrationPh) convent;
                                                                                calibrationPh = ph;
                                                                                DeviceSetting deviceSetting = session.getDeviceSetting();
                                                                                if (deviceSetting != null) {
                                                                                    deviceSetting.getCalibration().setCalibrationPh(ph);
                                                                                }
                                                                                session.updateCalibrationPh(ph);
                                                                                MyApi.getInstance().getDataApi().setLastCalibrationPh(ph);
                                                                                MyApi.getInstance().getDataApi().updateBleDevice(session.zenDevice, DataApi.SETTING);
                                                                            } else if (convent instanceof Error) {
                                                                                Error error = (Error) convent;
                                                                                MyApi.getInstance().getDataApi().putReminder(error);

                                                                            } else if (convent instanceof Version) {
                                                                                Version version = (Version) convent;
                                                                                session.updateVersion(version);
                                                                            } else if (convent instanceof VelaDataInfo) {
                                                                                VelaDataInfo vela = (VelaDataInfo) convent;
                                                                                Data dataPack = vela.toMeasureDataPack();
                                                                                mCurrentMode = dataPack.getMode();
                                                                                session.currentMode = dataPack.getMode();
                                                                                session.updateLastData(dataPack);
                                                                                MyApi.getInstance().getDataApi().setLastData(dataPack);

                                                                                Log.i(TAG, "Received VelaData: " + vela.toString());
                                                                            } else if (convent instanceof VelaParamModeDeviceToApp) {
                                                                                VelaParamModeDeviceToApp modeUpload = (VelaParamModeDeviceToApp) convent;

                                                                                // Notify MeasureFragmentVela
                                                                                EventBus.getDefault().post(new ModePatternChangedFromDevice());

                                                                                Log.i(TAG, "Received Mode Upload: " + modeUpload.toString());

                                                                                session.updateModeUpload(modeUpload);
                                                                                MyApi.getInstance().getDataApi().setLastVelaModeUpload(modeUpload);
                                                                            } else {
                                                                                Log.i(TAG, "Object Instance not found");
                                                                            }

                                                                            // Notify UI
                                                                            EventBus.getDefault().post(new UpdateEvent(convent));
                                                                        }
                                                                    } catch (Exception e) {
                                                                        Log.i(TAG, "Decode Error", e);
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
                    }

                    @Override
                    public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {
                        // 连接中断，isActiveDisConnected表示是否是主动调用了断开连接方法

                        mConnected = false;
                        mConnecting = false;

                        if (currentSession != null && currentSession.bleDevice == bleDevice) {
                            currentSession.connected = false;
                            currentSession.connecting = false;
                        }

                        // Clear UUIDs for primary session
                        if (currentSession != null) {
                            currentSession.writeService = null;
                            currentSession.writeCharacteristic = null;
                            currentSession.readService = null;
                            currentSession.readCharacteristic = null;
                        }

                        mConnectUpdateTime = System.currentTimeMillis();
                        MyApi.getInstance().getDataApi().setReminder(0);
                        EventBus.getDefault().post(new BleDeviceConnectEvent(mConnected));

                        if (!mDisConnect) {
                            boolean ret = gatt.connect();
                        }
                    }
                });
            }
        }

        return 1;
    }

    /**
     * Multi-device connect WITHOUT touching primary session.
     */
    public int connectAdditional(com.zen.api.data.BleDevice device) {
        if (device == null) return 0;

        BleDevice bleDevice = bleDeviceMap.get(device.getMac());
        if (bleDevice == null && BluetoothAdapter.checkBluetoothAddress(device.getMac())) {
            BluetoothDevice bluetoothDevice = BleManager.getInstance()
                    .getBluetoothAdapter()
                    .getRemoteDevice(device.getMac());
            bleDevice = new BleDevice(bluetoothDevice);
        }

        if (bleDevice == null) return 0;

        DeviceSession session = new DeviceSession();
        session.mac = device.getMac();
        session.zenDevice = device;
        session.bleDevice = bleDevice;
        sessionMap.put(session.mac, session);

        BleManager.getInstance().connect(bleDevice, new BleGattCallback() {

            @Override
            public void onStartConnect() {
                session.connecting = true;
            }

            @Override
            public void onConnectFail(BleException exception) {
                session.connected = false;
                session.connecting = false;
                Log.e(TAG, "Multi-connect FAILED for: " + device.getMac());
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                session.connected = true;
                session.connecting = false;

                multiConnectedDevices.put(device.getMac(), bleDevice);
                multiZenDevices.put(device.getMac(), device);

                Log.i(TAG, "Multi-connect SUCCESS: " + device.getMac());

                // Do NOT touch primary currentSession.
                subscribeNotificationsForAdditionalDevice(bleDevice, gatt, session);
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {
                session.connected = false;
                session.connecting = false;

                multiConnectedDevices.remove(device.getMac());
                multiZenDevices.remove(device.getMac());
                sessionMap.remove(device.getMac());

                Log.i(TAG, "Multi-device disconnected: " + device.getMac());
            }
        });

        return 1;
    }

    private void subscribeNotificationsForAdditionalDevice(BleDevice bleDevice,
                                                           BluetoothGatt gatt,
                                                           DeviceSession session) {
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
                                            Convent convent = (Convent) object;

                                            // For additional devices, we only fire UpdateEvent,
                                            // to keep behavior identical to original code.
                                            EventBus.getDefault().post(new UpdateEvent(convent));

                                            // Optionally update session lastData for debugging
                                            if (convent instanceof Data) {
                                                session.updateLastData((Data) convent);
                                            }
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

    /*
     * Disconnect primary device or all devices exactly as original code.
     */
    @Override
    public int disconnect() {
        mConnecting = false;

        com.zen.api.data.BleDevice currentDevice =
                currentSession != null ? currentSession.zenDevice : null;

        if (currentDevice == null) {
            BleManager.getInstance().disconnectAllDevice();
            mConnected = false;
            return 0;
        }

        if (currentDevice.isDemo()) {
            mConnected = false;
            mDemoConnected = false;
            EventBus.getDefault().post(new BleDeviceConnectEvent(mConnected));
            if (currentSession != null) {
                currentSession.connected = false;
                currentSession = null;
            }
        } else {
            BleManager.getInstance().disconnectAllDevice();
            mConnected = false;
            if (currentSession != null) {
                currentSession.connected = false;
            }
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
        byte[] data = convent.pack();

        DeviceSession session = currentSession;

        if (session != null && mConnected &&
                !TextUtils.isEmpty(session.writeService) &&
                !TextUtils.isEmpty(session.writeCharacteristic) &&
                session.bleDevice != null) {

            BleManager.getInstance().write(
                    session.bleDevice,
                    session.writeService,
                    session.writeCharacteristic,
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
            if (session != null) {
                session.currentMode = mDemoMode;
            }
        } else {
            // no-op
        }
        return false;
    }

    @Override
    public boolean readCommand() {
        DeviceSession session = currentSession;

        if (session != null &&
                !TextUtils.isEmpty(session.readService) &&
                TextUtils.isEmpty(session.readCharacteristic)) { // original logic (likely bug, preserved)
            BleManager.getInstance().read(
                    session.bleDevice,
                    session.readService,
                    session.readCharacteristic,
                    new BleReadCallback() {
                        @Override
                        public void onReadSuccess(byte[] data) {
                            try {
                                Object object = Command.unpack(data);
                                if (object != null && object instanceof Convent) {
                                    EventBus.getDefault().post(new UpdateEvent((Convent) object));
                                }
                            } catch (Exception e) {
                                // swallow
                            }
                        }

                        @Override
                        public void onReadFailure(BleException exception) {
                            // read failed
                        }
                    });
            return true;
        }
        return false;
    }

    @Override
    public boolean isConnecting() {
        // original always returned false; keep behavior
        return false;
    }

    @Override
    public int reconnect() {
        com.zen.api.data.BleDevice dev =
                currentSession == null ?
                        (lastSession != null ? lastSession.zenDevice : null) :
                        currentSession.zenDevice;
        return connect(dev);
    }

    @Override
    public com.zen.api.data.BleDevice getLastDevice() {
        if (currentSession != null) {
            return currentSession.zenDevice;
        }
        return lastSession != null ? lastSession.zenDevice : null;
    }

    @Nullable
    @Override
    public String getDeviceNumber() {
        if (currentSession != null && currentSession.zenDevice != null) {
            return currentSession.zenDevice.getSN();
        }
        return "0000";
    }

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
        calibrationCond.setA84(true);
        calibrationCond.setA1288(true);
        calibrationCond.setA1413(true);
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

        // Mirror demo calibration into session if present
        if (currentSession != null) {
            currentSession.lastCalCond = calibrationCond;
            currentSession.lastCalPh = calibrationPh;
        }

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

        mCurrentMode = mDemoMode;
        if (currentSession != null) {
            currentSession.currentMode = mDemoMode;
            currentSession.updateLastData(data);
        }

        MyApi.getInstance().getDataApi().setLastData(data);
        MyApi.getInstance().getDataApi().setLastCalibrationCond(calibrationCond);
        MyApi.getInstance().getDataApi().setLastCalibrationPh(calibrationPh);
        EventBus.getDefault().post(new UpdateEvent());
    }
}