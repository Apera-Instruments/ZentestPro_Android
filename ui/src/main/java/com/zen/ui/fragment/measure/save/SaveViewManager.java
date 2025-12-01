package com.zen.ui.fragment.measure.save;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SaveViewManager {

    private static final String TAG = "SaveViewManager";
    private final Context context;

    public SaveViewManager(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * Capture a view to PNG and return the resulting file.
     */
    public File captureToFile(View layout, String traceNo) {
        layout.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(layout.getDrawingCache());
        layout.setDrawingCacheEnabled(false);

        File dir = context.getExternalFilesDir("save");
        if (dir == null) return null;

        if (!dir.exists()) dir.mkdirs();

        File file = new File(dir, traceNo + Long.toHexString(System.currentTimeMillis()) + ".png");
        if (file.exists()) file.delete();

        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            Log.i(TAG, "Saved view snapshot: " + file.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "captureToFile error", e);
            return null;
        }

        return file;
    }
}

