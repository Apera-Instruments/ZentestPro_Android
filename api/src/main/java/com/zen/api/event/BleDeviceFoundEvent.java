package com.zen.api.event;

public class BleDeviceFoundEvent {
    private String name;
    private String mac;
    private int rssi;
    public BleDeviceFoundEvent(String name, String mac, int rssi) {
        this.name = name;
        this.mac = mac;
        this.rssi = rssi;
    }

    public String getName() {
        return name;
    }

    public String getMac() {
        return mac;
    }
}
