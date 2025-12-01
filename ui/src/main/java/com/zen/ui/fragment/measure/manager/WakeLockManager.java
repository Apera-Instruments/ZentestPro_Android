// file: com/zen/ui/fragment/measure/WakeLockManager.java
package com.zen.ui.fragment.measure.manager;

import android.content.Context;
import android.os.PowerManager;
import android.app.Service;

/**
 * Encapsulates WakeLock handling for continuous measurement / autosave.
 */
public class WakeLockManager {

    private final Context appContext;
    private PowerManager.WakeLock wakeLock;

    public WakeLockManager(Context context) {
        this.appContext = context.getApplicationContext();
    }

    public void acquire() {
        try {
            if (wakeLock != null && wakeLock.isHeld()) return;

            PowerManager pm = (PowerManager) appContext.getSystemService(Service.POWER_SERVICE);
            if (pm != null) {
                wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "Zen:MeasureWakeLock");
                wakeLock.setReferenceCounted(false);
                wakeLock.acquire();
            }
        } catch (Exception ignore) { }
    }

    public void release() {
        try {
            if (wakeLock != null && wakeLock.isHeld()) {
                wakeLock.release();
            }
        } catch (Exception ignore) { }
        wakeLock = null;
    }

    public boolean isHeld() {
        return wakeLock != null && wakeLock.isHeld();
    }
}