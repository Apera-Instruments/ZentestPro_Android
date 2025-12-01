package com.zen.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatTextView;
import android.graphics.Bitmap;

import com.zen.ui.R;

public class ArrowTextView extends AppCompatTextView {
    public ArrowTextView(Context context) {
        super(context);
        init();
    }

    public ArrowTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ArrowTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // 设置背景图片资源
        setBackgroundResource(R.drawable.bluetoothal);
    }
}