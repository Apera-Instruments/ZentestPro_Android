package com.zen.biz.velabt.velaApi;

import android.text.TextUtils;

import com.clj.fastble.BleManager;
import com.clj.fastble.data.BleDevice;
import com.zen.api.BtApi;
import com.zen.api.MyApi;
import com.zen.api.data.DeviceSetting;
import com.zen.api.protocol.CalibrationCond;
import com.zen.api.protocol.CalibrationPh;
import com.zen.api.protocol.Convent;
import com.zen.api.protocol.Data;
import com.zen.api.protocol.Mode;
import com.zen.api.protocol.Version;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.zen.api.event.UpdateEvent;
import com.zen.biz.velabt.BleCore;
import com.zen.biz.velabt.session.DeviceSession;

public class VelaDeviceApi implements BtApi {

    public static final String TAG = "BtApiVela";

    private final String mac;
    private final ScheduledThreadPoolExecutor demoScheduler =
            new ScheduledThreadPoolExecutor(1);
    private final Random random = new Random();

    // Demo state
    private boolean demoConnected = false;
    private int demoMode;
    private final Data demoData = new Data();
    private int demoTemp;
    private int demoPh;
    private int demoMv;
    private int demoTds;
    private int demoRes;
    private int demoCond;
    private int demoSalt;
    private CalibrationCond demoCalibrationCond;
    private CalibrationPh demoCalibrationPh;

    // Mode capability flags (per-device)
    private boolean modePH = true;
    private boolean modeSAL = true;
    private boolean modeORP = true;
    private boolean modeTDS = true;
    private boolean modeCOND = true;
    private boolean modeRES = true;

    // ------------------------------------------------------------------------
    // Constructor / factory
    // ------------------------------------------------------------------------

    public static VelaDeviceApi forDevice(String mac) {
        return new VelaDeviceApi(mac);
    }

    public VelaDeviceApi(String mac) {
        this.mac = mac;
    }

    private DeviceSession session() {
        return BleCore.getInstance().getSession(mac);
    }

    // ------------------------------------------------------------------------
    // Helper: model + display name + capability flags
    // ------------------------------------------------------------------------

    private String getLocalNameAndSetModes(String name) {
        if (name == null) return null;

        // reset all
        modePH = false;
        modeSAL = false;
        modeORP = false;
        modeTDS = false;
        modeCOND = false;
        modeRES = false;

        if (name.startsWith("6") && name.length() >= 10) {
            String sn2 = name.substring(name.length() - 4);
            if (name.startsWith("60")) {
                modePH = true;
                modeORP = true;
                return "PH60-" + sn2;
            } else if (name.startsWith("61")) {
                modePH = true;
                modeORP = true;
                return "PH60S-" + sn2;
            } else if (name.startsWith("62")) {
                modePH = true;
                modeORP = true;
                return "PH60F-" + sn2;
            } else if (name.startsWith("63")) {
                modeORP = true;
                return "ORP60-" + sn2;
            } else if (name.startsWith("64")) {
                modePH = true;
                return "PHO60-" + sn2;
            } else if (name.startsWith("65")) { // EC
                modeCOND = true;
                modeSAL = true;
                modeRES = true;
                modeTDS = true;
                return "EC60-" + sn2;
            } else if (name.startsWith("66")) {
                modeCOND = true;
                modeSAL = true;
                modeRES = true;
                modeTDS = true;
                return "ECS60-" + sn2;
            } else if (name.startsWith("67")) {
                modePH = true;
                modeCOND = true;
                modeSAL = true;
                modeRES = true;
                modeTDS = true;
                modeORP = true;
                return "PC60-" + sn2;
            } else if (name.startsWith("68")) {
                modePH = true;
                modeCOND = true;
                modeSAL = true;
                modeRES = true;
                modeTDS = true;
                modeORP = true;
                return "PCS60-" + sn2;
            }
        } else if (name.startsWith("24") && name.length() >= 10) {
            String sn2 = name.substring(name.length() - 4);
            modePH = true;
            modeCOND = true;
            modeSAL = true;
            modeRES = true;
            modeTDS = true;
            modeORP = true;
            return "UWS_Waterboy-" + sn2;
        }

        // unknown model, default all true
        modePH = true;
        modeSAL = true;
        modeORP = true;
        modeTDS = true;
        modeCOND = true;
        modeRES = true;
        return name;
    }

    private String getModelName(String name) {
        if (name == null) return null;
        if (name.startsWith("6") && name.length() >= 10) {
            String sn2 = "Z";
            if (name.startsWith("60")) return "PH60-" + sn2;
            if (name.startsWith("61")) return "PH60S-" + sn2;
            if (name.startsWith("62")) return "PH60F-" + sn2;
            if (name.startsWith("63")) return "ORP60-" + sn2;
            if (name.startsWith("64")) return "PHO60-" + sn2;
            if (name.startsWith("65")) return "EC60-" + sn2;
            if (name.startsWith("66")) return "ECS60-" + sn2;
            if (name.startsWith("67")) return "PC60-" + sn2;
            if (name.startsWith("68")) return "PCS60-" + sn2;
        } else if (name.startsWith("24")) {
            return "UWS_Waterboy";
        }
        return null;
    }

    // ------------------------------------------------------------------------
    // Mode queries (per-instance)
    // ------------------------------------------------------------------------

    private int currentMode() {
        DeviceSession s = session();
        return (s != null) ? s.currentMode : demoMode;
    }

    // original logic: (mode != mV || (!isDemo())) && mModePH ...
    private boolean isDemoFlag() {
        // original isDemo() = mCurrentDevice == null || !mCurrentDevice.isDemo()
        // here we approximate: demoConnected means "using demo generator"
        return !demoConnected;
    }

    public boolean isModePH() {
        return (currentMode() != Data.mV || (!isDemoFlag())) && modePH;
    }

    public boolean isModeSAL() {
        return (currentMode() != Data.mV || (!isDemoFlag())) && modeSAL;
    }

    public boolean isModeORP() {
        return (currentMode() == Data.mV || (!isDemoFlag())) && modeORP;
    }

    public boolean isModeTDS() {
        return (currentMode() != Data.mV || (!isDemoFlag())) && modeTDS;
    }

    public boolean isModeCOND() {
        return (currentMode() != Data.mV || (!isDemoFlag())) && modeCOND;
    }

    public boolean isModeRES() {
        return (currentMode() != Data.mV || (!isDemoFlag())) && modeRES;
    }

    // ------------------------------------------------------------------------
    // BtApi interface implementations
    // ------------------------------------------------------------------------

    @Override
    public void startScan() {
        BleCore.getInstance().startScan();
    }

    @Override
    public void stopScan() {
        BleCore.getInstance().stopScan();
    }

    @Override
    public boolean isScanning() {
        return BleCore.getInstance().isScanning();
    }

    @Override
    public boolean open() {
        try {
            BleManager.getInstance().enableBluetooth();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isBtEnable() {
        return BleManager.getInstance().isBlueEnable();
    }

    @Override
    public int connect(com.zen.api.data.BleDevice device) {
        if (device == null) return 0;

        if (device.isDemo()) {
            // Demo-only session (no BLE connect)
            setupDemo(device);
            return 1;
        }

        // Real BLE device
        BleCore core = BleCore.getInstance();
        BleDevice bleDevice = core.resolveBleDevice(device.getMac());
        if (bleDevice == null) {
            // Try fallback from BluetoothAdapter via BleCore
            bleDevice = core.resolveBleDevice(device.getMac());
        }
        if (bleDevice == null) return 0;

        // Configure name / model / SN and mode flags
        String rawName = bleDevice.getName();
        String displayName = getLocalNameAndSetModes(rawName);
        if (!TextUtils.isEmpty(displayName)) {
            device.setName(displayName);
        }
        device.setModle(getModelName(rawName));
        device.setSN(rawName);

        // Ensure session exists / bound
        DeviceSession s = core.ensureSession(mac);
        s.zenDevice = device;
        s.isDemoDevice = false;

        return core.connectRealDevice(device, bleDevice);
    }

    @Override
    public int disconnect() {
        if (demoConnected) {
            demoConnected = false;
            DeviceSession s = session();
            if (s != null) {
                s.isDemoDevice = false;
                s.connected = false;
            }
            EventBus.getDefault().post(new com.zen.api.event.BleDeviceConnectEvent(false));
        } else {
            BleCore.getInstance().disconnect(mac);
        }
        return 1;
    }

    @Override
    public boolean isConnected() {
        if (demoConnected) return true;
        DeviceSession s = session();
        return s != null && s.connected;
    }

    @Override
    public boolean sendCommand(Convent convent) {
        return sendCommand(convent, null);
    }

    public boolean sendCommand(Convent convent, Runnable callback) {
        if (demoConnected) {
            if (convent instanceof Mode) {
                demoMode = ((Mode) convent).getMode();
            }
            return true;
        }
        return BleCore.getInstance().write(mac, convent, callback);
    }

    @Override
    public boolean readCommand() {
        if (demoConnected) {
            // no real BLE read
            return false;
        }
        return BleCore.getInstance().read(mac);
    }

    @Override
    public boolean isConnecting() {
        DeviceSession s = session();
        return s != null && s.connecting;
    }

    @Override
    public int reconnect() {
        DeviceSession s = session();
        com.zen.api.data.BleDevice dev =
                (s != null && s.zenDevice != null) ? s.zenDevice : null;
        return connect(dev);
    }

    @Override
    public com.zen.api.data.BleDevice getLastDevice() {
        DeviceSession s = session();
        return s != null ? s.zenDevice : null;
    }

    @Nullable
    @Override
    public String getDeviceNumber() {
        DeviceSession s = session();
        if (s != null && s.zenDevice != null) {
            return s.zenDevice.getSN();
        }
        return "0000";
    }

    public Version getDeviceVersion() {
        DeviceSession s = session();
        return s != null ? s.version : null;
    }

    @NotNull
    @Override
    public float getLife() {
        // mimic original: use last calibrationPh slope1
        DeviceSession s = session();
        CalibrationPh ph = (s != null ? s.lastCalPh : demoCalibrationPh);
        if (ph != null) {
            return (float) ph.getSlope1();
        }
        return 0.99f;
    }

    // ------------------------------------------------------------------------
    // DEMO MODE
    // ------------------------------------------------------------------------

    private void setupDemo(com.zen.api.data.BleDevice device) {
        demoConnected = true;
        DeviceSession s = BleCore.getInstance().ensureSession(mac);
        s.zenDevice = device;
        s.isDemoDevice = true;
        s.connected = true;

        // ensure SN set
        if (TextUtils.isEmpty(device.getSN())) {
            device.setSN(device.getName());
        }

        initDemoValues();
        scheduleDemoUpdates();

        EventBus.getDefault().post(new com.zen.api.event.BleDeviceConnectEvent(true));
    }

    private void initDemoValues() {
        demoData.setL(random.nextBoolean());
        demoData.setM(random.nextBoolean());
        demoData.setH(random.nextBoolean());
        demoData.setUnit2(random.nextBoolean() ? Data.UNIT_C : Data.UNIT_F);
        demoTemp = random.nextInt(350) + 100;

        demoPh   = random.nextInt(1400 - 140);
        demoMv   = random.nextInt(2000 - 200) - 1000;
        demoTds  = random.nextInt(10000 - 1000);
        demoRes  = random.nextInt(100 - 10);
        demoCond = random.nextInt(2000 - 200);
        demoSalt = random.nextInt(1000 - 100);

        demoCalibrationCond = new CalibrationCond();
        demoCalibrationCond.setA84(true);
        demoCalibrationCond.setA1288(true);
        demoCalibrationCond.setA1413(true);
        demoCalibrationCond.setDate(new Date());

        demoCalibrationPh = new CalibrationPh();
        demoCalibrationPh.setA168(false);
        demoCalibrationPh.setA400(true);
        demoCalibrationPh.setA1001(true);
        demoCalibrationPh.setA1245(false);
        demoCalibrationPh.setA700(true);
        demoCalibrationPh.setDate(new Date());
        demoCalibrationPh.setSlope2(random.nextInt(50));
        demoCalibrationPh.setSlope1(random.nextInt(60));
        demoCalibrationPh.setOffset1(random.nextInt(50));
        demoCalibrationPh.setOffset2(random.nextInt(100));

        int count = 0;
        if (demoCalibrationPh.is1245()) count++;
        if (demoCalibrationPh.is400()) count++;
        if (demoCalibrationPh.is1001()) count++;
        if (demoCalibrationPh.is168()) count++;
        if (demoCalibrationPh.is700()) count++;
        demoCalibrationPh.setPointCount(count);

        MyApi.getInstance().getDataApi().setLastCalibrationCond(demoCalibrationCond);
        MyApi.getInstance().getDataApi().setLastCalibrationPh(demoCalibrationPh);
        MyApi.getInstance().getDataApi().setReminder(random.nextInt(4));
    }

    private void scheduleDemoUpdates() {
        demoScheduler.schedule(new Runnable() {
            @Override
            public void run() {
                if (!demoConnected) return;
                updateDemoDataOnce();
                scheduleDemoUpdates();
            }
        }, 1, TimeUnit.SECONDS);
    }

    private void updateDemoDataOnce() {
        Data data = demoData;
        data.setMode(demoMode);

        if (demoMode == Data.pH) {
            data.setPointDigit(2);
            data.setValue(demoPh + random.nextInt(140));
            data.setUnit(Data.UNIT_pH);
        } else if (demoMode == Data.mV) { // ORP
            data.setPointDigit(0);
            data.setValue(demoMv + random.nextInt(200));
            data.setUnit(Data.UNIT_mV);
        } else if (demoMode == Data.TDS) {
            data.setPointDigit(1);
            data.setValue(demoTds + random.nextInt(1000));
            data.setUnit(Data.UNIT_ppm);
        } else if (demoMode == Data.Res) {
            data.setPointDigit(0);
            data.setValue(demoRes + random.nextInt(1000));
            data.setUnit(Data.UNIT_Ω);
        } else if (demoMode == Data.Cond) {
            data.setPointDigit(1);
            data.setValue(demoCond + random.nextInt(200));
            data.setUnit(Data.UNIT_uS);
        } else if (demoMode == Data.Salt) {
            data.setPointDigit(2);
            data.setValue(demoSalt + random.nextInt(100));
            data.setUnit(Data.UNIT_ppt);
        } else {
            data.setPointDigit(0);
            data.setValue(random.nextInt(4000) - 2000);
        }

        data.setPointDigit2(1);
        data.setValue2(demoTemp + random.nextInt(50));

        MyApi.getInstance().getDataApi().setLastData(data);
        MyApi.getInstance().getDataApi().setLastCalibrationCond(demoCalibrationCond);
        MyApi.getInstance().getDataApi().setLastCalibrationPh(demoCalibrationPh);

        DeviceSession s = session();
        if (s != null) {
            s.updateLastData(data);
            s.lastCalCond = demoCalibrationCond;
            s.lastCalPh = demoCalibrationPh;
        }

        EventBus.getDefault().post(new UpdateEvent());
    }
}

