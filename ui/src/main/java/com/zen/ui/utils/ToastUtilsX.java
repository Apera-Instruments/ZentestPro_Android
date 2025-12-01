package com.zen.ui.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Looper;
import androidx.annotation.StringRes;
import androidx.core.widget.TextViewCompat;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.Utils;
import com.orhanobut.logger.Logger;
import com.zen.ui.R;

public class ToastUtilsX {

    public static void showTextActi(Activity activity, String message) {
        if (Looper.myLooper() == null)
            Looper.prepare();
        Toast toast = Toast.makeText( activity, message, Toast.LENGTH_SHORT);
//        if (Build.VERSION.SDK_INT < 30){
//            Logger.i("get device = " + 1111111);
//            View view =toast.getView();
//            TextView tvMessage = (TextView) view.findViewById(R.id.message);
//            int msgColor = tvMessage.getCurrentTextColor();
//            //it solve the font of toast
//            TextViewCompat.setTextAppearance(tvMessage, android.R.style.TextAppearance);
//            tvMessage.setTextColor(msgColor);
//        }
        toast.show();
        Looper.loop();
    }


    public static void showActi(Activity activity, @StringRes final int resId) {

        Toast toast = Toast.makeText( activity, Utils.getApp().getResources().getText(resId).toString(), Toast.LENGTH_SHORT);
//        if (Build.VERSION.SDK_INT < 30){
//            View view =toast.getView();
//            TextView tvMessage = (TextView) view.findViewById(R.id.message);
//            int msgColor = tvMessage.getCurrentTextColor();
//            //it solve the font of toast
//            TextViewCompat.setTextAppearance(tvMessage, android.R.style.TextAppearance);
//            tvMessage.setTextColor(msgColor);
//        }
        toast.show();
    }
}
