package com.zen.ui.fragment.measure.save;

import android.os.Handler;

import com.zen.ui.fragment.measure.utils.TimeUtils;

public class AutoSaveManager {

    public interface AutoSaveCallback {
        void onAutoSaveTick(int sn, String traceNo);
    }

    private final Handler handler;
    private final AutoSaveCallback callback;

    private boolean autoSaveEnabled = false;
    private boolean running = false;
    private long intervalMs = 10_000L;
    private int sn = 0;
    private String traceNo;

    private final Runnable task = new Runnable() {
        @Override
        public void run() {
            if (!running || !autoSaveEnabled) return;
            if (callback != null) {
                callback.onAutoSaveTick(sn++, traceNo);
            }
            handler.postDelayed(this, intervalMs);
        }
    };

    public AutoSaveManager(Handler handler, AutoSaveCallback callback) {
        this.handler = handler;
        this.callback = callback;
    }

    public void setIntervalMs(long ms) {
        this.intervalMs = ms;
    }

    public long getIntervalMs() {
        return intervalMs;
    }

    public boolean isAutoSaveEnabled() {
        return autoSaveEnabled;
    }

    public boolean isRunning() {
        return running;
    }

    public String getTraceNo() {
        return traceNo;
    }

    public int getCurrentSn() {
        return sn;
    }

    public void enableAutoSave(boolean enable) {
        this.autoSaveEnabled = enable;
        if (!enable) {
            stop();
        }
    }

    public void start() {
        if (!autoSaveEnabled) return;
        if (running) return;

        running = true;
        sn = 0;
        traceNo = TimeUtils.makeTraceId();
        handler.postDelayed(task, intervalMs);
    }

    public void stop() {
        running = false;
        handler.removeCallbacks(task);
    }
}

