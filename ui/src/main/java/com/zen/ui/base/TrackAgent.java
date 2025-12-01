package com.zen.ui.base;

import android.app.Activity;
import android.util.Log;

public class TrackAgent {
    public final String TAG = this.getClass().getSimpleName();

    public void onActivityCreate(Activity activity) {
        Log.v(TAG, "onActivityCreate " + activity);
    }

    public void onActivityResume(Activity activity) {
        Log.v(TAG, "onActivityResume " + activity);
    }

    public void onActivityPause(Activity activity) {
        Log.v(TAG, "onActivityPause " + activity);
    }

    public void onActivityDestroy(Activity activity) {
        Log.v(TAG, "onActivityDestroy " + activity);
    }

    public void onActivityWindowFocusChanged(Activity activity, boolean hasFocus) {
        Log.v(TAG, "onActivityWindowFocusChanged " + activity + " " + hasFocus);
    }
}
