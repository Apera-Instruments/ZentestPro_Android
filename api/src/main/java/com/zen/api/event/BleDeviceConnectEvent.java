package com.zen.api.event;

public class BleDeviceConnectEvent {
    boolean isConnected =false;
    public BleDeviceConnectEvent(boolean c){
        isConnected =c;
    }

    public boolean isConnected() {
        return isConnected;
    }
}
