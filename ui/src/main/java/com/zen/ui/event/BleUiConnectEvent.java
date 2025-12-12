package com.zen.ui.event;

import com.zen.api.data.BleDevice;

public class BleUiConnectEvent {
    public final BleDevice device;

    public BleUiConnectEvent(BleDevice device) {
        this.device = device;
    }
}
