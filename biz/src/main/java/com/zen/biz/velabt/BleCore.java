package com.zen.biz.velabt;

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
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.zen.api.DataApi;
import com.zen.api.MyApi;
import com.zen.api.event.BleDeviceConnectEvent;
import com.zen.api.event.BleDeviceFoundEvent;
import com.zen.api.event.ModePatternChangedFromDevice;
import com.zen.api.event.UpdateEvent;
import com.zen.api.protocol.CalibrationCond;
import com.zen.api.protocol.CalibrationPh;
import com.zen.api.protocol.Command;
import com.zen.api.protocol.Convent;
import com.zen.api.protocol.Data;
import com.zen.api.protocol.Error;
import com.zen.api.protocol.ParmUp;
import com.zen.api.protocol.Shutdown;
import com.zen.api.protocol.Time;
import com.zen.api.protocol.Version;
import com.zen.api.protocol.velaprotocal.VelaDataInfo;
import com.zen.api.protocol.velaprotocal.VelaParamModeDeviceToApp;
import com.zen.biz.velabt.session.DeviceSession;

import org.greenrobot.eventbus.EventBus;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_NOTIFY;

public class BleCore {

    private static final String TAG = "BleCore";

    /*
     * 写：service uuid  FFE5  characteristic FFE9
     * 读：service uuid  FFE0  characteristic FFE4
     */
    private static final String WRITE_SERVICE_UUID = "0000ffe5-0000-1000-8000-00805f9b34fb";
    private static final String WRITE_CHARAC_UUID  = "0000ffe9-0000-1000-8000-00805f9b34fb";
    private static final String READ_SERVICE_UUID  = "0000ffe0-0000-1000-8000-00805f9b34fb";
    private static final String READ_CHARAC_UUID   = "0000ffe4-0000-1000-8000-00805f9b34fb";

    private static BleCore sInstance;

    // All sessions per MAC (non-demo)
    private final Map<String, DeviceSession> sessionMap = new ConcurrentHashMap<>();
    // BLE scan results, used for connect
    private final Map<String, BleDevice> scanDeviceMap = new ConcurrentHashMap<>();

    private volatile boolean scanning = false;

    private BleCore() {}

    public static BleCore getInstance() {
        if (sInstance == null) {
            synchronized (BleCore.class) {
                if (sInstance == null) {
                    sInstance = new BleCore();
                }
            }
        }
        return sInstance;
    }

    public DeviceSession getSession(String mac) {
        return sessionMap.get(mac);
    }

    public DeviceSession ensureSession(String mac) {
        DeviceSession s = sessionMap.get(mac);
        if (s == null) {
            s = new DeviceSession();
            s.mac = mac;
            sessionMap.put(mac, s);
        }
        return s;
    }

    public Map<String, BleDevice> getScanDeviceMap() {
        return scanDeviceMap;
    }

    // ------------------------------------------------------------------------
    // SCAN
    // ------------------------------------------------------------------------

    public void startScan() {
        BleManager.getInstance().scan(new BleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {
                scanning = true;
            }

            @Override
            public void onScanning(BleDevice bleDevice) {
                scanDeviceMap.put(bleDevice.getMac(), bleDevice);

                String displayName = mapNameToDisplayName(bleDevice.getName());
                if (!TextUtils.isEmpty(displayName)) {
                    EventBus.getDefault().post(
                            new BleDeviceFoundEvent(displayName, bleDevice.getMac(), bleDevice.getRssi())
                    );
                }
            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
                scanning = false;
            }
        });
    }

    public void stopScan() {
        BleManager.getInstance().cancelScan();
        scanning = false;
    }

    public boolean isScanning() {
        return scanning;
    }

    /**
     * Name mapping for scan list only (no side effects, unlike original getLocalName which changed flags).
     */
    private String mapNameToDisplayName(String name) {
        if (name == null) return null;
        if (name.startsWith("6") && name.length() >= 10) {
            String sn2 = name.substring(name.length() - 4);
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
        } else if (name.startsWith("24") && name.length() >= 10) {
            String sn2 = name.substring(name.length() - 4);
            return "UWS_Waterboy" + "-" + sn2;
        }
        return null;
    }

    // ------------------------------------------------------------------------
    // CONNECT / DISCONNECT (real devices, not demo)
    // ------------------------------------------------------------------------

    public int connectRealDevice(com.zen.api.data.BleDevice apiDevice, BleDevice bleDevice) {
        if (apiDevice == null || bleDevice == null) return 0;

        final String mac = apiDevice.getMac();
        final DeviceSession session = ensureSession(mac);
        session.bleDevice = bleDevice;
        session.zenDevice = apiDevice;
        session.isDemoDevice = false;
        session.manualDisconnect = false;

        BleManager.getInstance().connect(bleDevice, new BleGattCallback() {
            @Override
            public void onStartConnect() {
                session.connecting = true;
            }

            @Override
            public void onConnectFail(BleException exception) {
                Log.e(TAG, "Connect FAIL: " + mac);
                session.connected = false;
                session.connecting = false;
                MyApi.getInstance().getDataApi().setReminder(0);
                EventBus.getDefault().post(new BleDeviceConnectEvent(false));
            }

            @Override
            public void onConnectSuccess(BleDevice dev, BluetoothGatt gatt, int status) {
                Log.i(TAG, "Connect SUCCESS: " + mac);
                session.connected = true;
                session.connecting = false;
                session.bleDevice = dev;

                EventBus.getDefault().post(new BleDeviceConnectEvent(true));

                setupServicesAndNotify(session, gatt);
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice dev, BluetoothGatt gatt, int status) {
                Log.i(TAG, "Disconnected: " + mac + " active=" + isActiveDisConnected);

                session.connected = false;
                session.connecting = false;
                session.writeService = null;
                session.writeCharacteristic = null;
                session.readService = null;
                session.readCharacteristic = null;

                MyApi.getInstance().getDataApi().setReminder(0);
                EventBus.getDefault().post(new BleDeviceConnectEvent(false));

                if (!session.manualDisconnect) {
                    // auto-reconnect to mimic original behavior
                    try {
                        gatt.connect();
                    } catch (Exception e) {
                        Log.e(TAG, "Auto-reconnect failed for " + mac, e);
                    }
                }
            }
        });

        return 1;
    }

    public void disconnect(String mac) {
        DeviceSession session = sessionMap.get(mac);
        if (session != null && session.bleDevice != null) {
            session.manualDisconnect = true;
            BleManager.getInstance().disconnect(session.bleDevice);
        } else {
            BleManager.getInstance().disconnectAllDevice();
        }
    }

    // ------------------------------------------------------------------------
    // SERVICE DISCOVERY + NOTIFY
    // ------------------------------------------------------------------------

    private void setupServicesAndNotify(DeviceSession session, BluetoothGatt gatt) {
        List<BluetoothGattService> list = gatt.getServices();
        if (list == null || list.isEmpty()) return;

        for (BluetoothGattService service : list) {
            String uuidService = service.getUuid().toString();

            if (uuidService.equalsIgnoreCase(WRITE_SERVICE_UUID)) {
                session.writeService = uuidService;
            }
            if (uuidService.equalsIgnoreCase(READ_SERVICE_UUID)) {
                session.readService = uuidService;
            }

            List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
            if (characteristics == null || characteristics.isEmpty()) continue;

            for (BluetoothGattCharacteristic characteristic : characteristics) {
                String uuidChar = characteristic.getUuid().toString();

                if (uuidService == session.writeService &&
                        uuidChar.equalsIgnoreCase(WRITE_CHARAC_UUID)) {
                    session.writeCharacteristic = uuidChar;
                }

                if (uuidService == session.readService &&
                        uuidChar.equalsIgnoreCase(READ_CHARAC_UUID)) {
                    session.readCharacteristic = uuidChar;

                    if ((characteristic.getProperties() & PROPERTY_NOTIFY) == PROPERTY_NOTIFY) {
                        enableNotify(session, uuidService, uuidChar);
                    }
                }
            }
        }
    }

    private void enableNotify(DeviceSession session, String serviceUuid, String charUuid) {
        BleManager.getInstance().notify(
                session.bleDevice,
                serviceUuid,
                charUuid,
                new BleNotifyCallback() {
                    @Override
                    public void onNotifySuccess() {
                        // mimic original: send Time command when notify opens
                        Time time = new Time();
                        write(session.mac, time, null);
                    }

                    @Override
                    public void onNotifyFailure(BleException exception) {
                        Log.e(TAG, "Notify FAILED for " + session.mac);
                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        Log.i(TAG, "Notification from " + session.mac + ": " + Arrays.toString(data));
                        handleNotification(session, data);
                    }
                }
        );
    }

    // ------------------------------------------------------------------------
    // NOTIFICATION DECODING
    // ------------------------------------------------------------------------

    private void handleNotification(DeviceSession session, byte[] data) {
        try {
            Object object = Command.unpack(data);
            if (!(object instanceof Convent)) return;

            Convent convent = (Convent) object;

            if (convent instanceof Data) {
                Data d = (Data) convent;
                session.updateLastData(d);
                MyApi.getInstance().getDataApi().setLastData(d);

            } else if (convent instanceof Shutdown) {
                Shutdown s = (Shutdown) convent;
                if (s.isOFF()) {
                    disconnect(session.mac);
                }

            } else if (convent instanceof ParmUp) {
                MyApi.getInstance().getDataApi().setLastParmUp((ParmUp) convent);

            } else if (convent instanceof CalibrationCond) {
                CalibrationCond cond = (CalibrationCond) convent;
                session.updateCalibrationCond(cond);

                MyApi.getInstance().getDataApi().setLastCalibrationCond(cond);
                MyApi.getInstance().getDataApi().updateBleDevice(session.zenDevice, DataApi.SETTING);

            } else if (convent instanceof CalibrationPh) {
                CalibrationPh ph = (CalibrationPh) convent;
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
                session.updateLastData(dataPack);
                MyApi.getInstance().getDataApi().setLastData(dataPack);
                Log.i(TAG, "Received VelaData: " + vela);

            } else if (convent instanceof VelaParamModeDeviceToApp) {
                VelaParamModeDeviceToApp modeUpload = (VelaParamModeDeviceToApp) convent;
                session.updateModeUpload(modeUpload);

                // Notify MeasureFragmentVela
                EventBus.getDefault().post(new ModePatternChangedFromDevice());
                MyApi.getInstance().getDataApi().setLastVelaModeUpload(modeUpload);

            } else {
                Log.i(TAG, "Unknown convent instance: " + convent.getClass().getSimpleName());
            }

            EventBus.getDefault().post(new UpdateEvent(convent));

        } catch (Exception e) {
            Log.e(TAG, "Decode Error", e);
        }
    }

    // ------------------------------------------------------------------------
    // WRITE / READ
    // ------------------------------------------------------------------------

    public boolean write(String mac, Convent convent, Runnable callback) {
        DeviceSession session = sessionMap.get(mac);
        if (session == null || session.bleDevice == null || !session.hasWriteUUIDs()) {
            return false;
        }

        byte[] data = convent.pack();

        BleManager.getInstance().write(
                session.bleDevice,
                session.writeService,
                session.writeCharacteristic,
                data,
                new com.clj.fastble.callback.BleWriteCallback() {
                    @Override
                    public void onWriteSuccess() {
                        if (callback != null) callback.run();
                    }

                    @Override
                    public void onWriteFailure(BleException exception) {
                        Log.e(TAG, "Write FAILED for " + mac);
                    }
                }
        );
        return true;
    }

    public boolean read(String mac) {
        DeviceSession session = sessionMap.get(mac);
        if (session == null || session.bleDevice == null || !session.hasReadUUIDs()) {
            return false;
        }

        BleManager.getInstance().read(
                session.bleDevice,
                session.readService,
                session.readCharacteristic,
                new BleReadCallback() {
                    @Override
                    public void onReadSuccess(byte[] data) {
                        handleNotification(session, data);
                    }

                    @Override
                    public void onReadFailure(BleException exception) {
                        Log.e(TAG, "Read FAILED for " + mac);
                    }
                }
        );
        return true;
    }

    // ------------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------------

    /**
     * Finds or builds a BleDevice for a given MAC using scan cache or BluetoothAdapter.
     */
    public BleDevice resolveBleDevice(String mac) {
        BleDevice d = scanDeviceMap.get(mac);
        if (d == null && BluetoothAdapter.checkBluetoothAddress(mac)) {
            BluetoothDevice bluetoothDevice =
                    BleManager.getInstance().getBluetoothAdapter().getRemoteDevice(mac);
            d = new BleDevice(bluetoothDevice);
        }
        return d;
    }
}