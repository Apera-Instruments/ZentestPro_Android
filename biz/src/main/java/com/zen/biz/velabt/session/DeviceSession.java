package com.zen.biz.velabt.session;

import com.clj.fastble.data.BleDevice;
import com.zen.api.data.DeviceSetting;
import com.zen.api.protocol.CalibrationCond;
import com.zen.api.protocol.CalibrationPh;
import com.zen.api.protocol.Data;
import com.zen.api.protocol.Version;
import com.zen.api.protocol.velaprotocal.VelaParamModeDeviceToApp;

/**
 * Represents a single physical BLE meter session
 * (primary or multi-device connections).
 *
 * This class supports ALL functionality required by BtApiVela
 * including tracking UUIDs, latest data packets, calibration,
 * version, and state.
 */
public class DeviceSession {

    // ----------------------------------------------------
    // Identity
    // ----------------------------------------------------
    public String mac;                     // MAC address used as key
    public com.zen.api.data.BleDevice zenDevice;  // wrapper object (SN, settings, etc.)
    public BleDevice bleDevice;            // FastBLE device reference

    // ----------------------------------------------------
    // UUIDs (BLE services & characteristics)
    // ----------------------------------------------------
    public String writeService;
    public String writeCharacteristic;
    public String readService;
    public String readCharacteristic;

    // ----------------------------------------------------
    // Live state data (updated by NotificationParser)
    // ----------------------------------------------------
    public Data lastData;
    public CalibrationPh lastCalPh;
    public CalibrationCond lastCalCond;
    public VelaParamModeDeviceToApp lastModeUpload;
    public Version version;

    public int currentMode = -1;

    // ----------------------------------------------------
    // State flags
    // ----------------------------------------------------
    public boolean connected = false;
    public boolean connecting = false;
    public boolean isDemoDevice = false;
    public long lastUpdateTime = 0;
    public boolean manualDisconnect = false;

    // ----------------------------------------------------
    // Session helpers
    // ----------------------------------------------------

    public boolean hasData() {
        return lastData != null;
    }

    public boolean hasCalibration() {
        return lastCalPh != null || lastCalCond != null;
    }

    public DeviceSetting getDeviceSetting() {
        return zenDevice != null ? zenDevice.getSetting() : null;
    }

    public void updateLastData(Data newData) {
        this.lastData = newData;
        if (newData != null) {
            this.currentMode = newData.getMode();
        }
        this.lastUpdateTime = System.currentTimeMillis();
    }

    public void updateCalibrationPh(CalibrationPh ph) {
        this.lastCalPh = ph;
        this.lastUpdateTime = System.currentTimeMillis();

        DeviceSetting s = getDeviceSetting();
        if (s != null) {
            s.getCalibration().setCalibrationPh(ph);
        }
    }

    public void updateCalibrationCond(CalibrationCond cond) {
        this.lastCalCond = cond;
        this.lastUpdateTime = System.currentTimeMillis();

        DeviceSetting s = getDeviceSetting();
        if (s != null) {
            s.getCalibration().setCalibrationCond(cond);
        }
    }

    public void updateModeUpload(VelaParamModeDeviceToApp upload) {
        this.lastModeUpload = upload;
        this.lastUpdateTime = System.currentTimeMillis();
    }

    public void updateVersion(Version v) {
        this.version = v;
    }

    // UUID helpers
    public boolean hasWriteUUIDs() {
        return writeService != null && writeCharacteristic != null;
    }

    public boolean hasReadUUIDs() {
        return readService != null && readCharacteristic != null;
    }

    @Override
    public String toString() {
        return "DeviceSession{" +
                "mac='" + mac + '\'' +
                ", connected=" + connected +
                ", data=" + (lastData != null) +
                ", mode=" + currentMode +
                '}';
    }
}