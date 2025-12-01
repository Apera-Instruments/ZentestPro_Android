package com.zen.api;

import com.zen.api.data.BleDevice;
import com.zen.api.protocol.Convent;
import com.zen.api.protocol.Version;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface BtApi {
    void startScan();

    boolean isScanning();

    int connect(BleDevice device);
    int disconnect();
    boolean isConnected();

    void stopScan();

    boolean isBtEnable();

    boolean sendCommand(Convent convent);

    boolean readCommand();

    boolean isConnecting();

    int reconnect();

    BleDevice getLastDevice();

    @Nullable
    String getDeviceNumber();

    Version getDeviceVersion();

    @NotNull
    float getLife();



     boolean isModePH();


     boolean isModeSAL();


     boolean isModeORP();



     boolean isModeTDS();


     boolean isModeCOND();



     boolean isModeRES() ;

    boolean open();
}
