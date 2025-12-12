package com.zen.ui.event;

public class BleUiDisconnectEvent {
    public final String name;
    public final String mac;


    public BleUiDisconnectEvent(String name, String mac) {
        this.name = name;
        this.mac = mac;
    }
}
