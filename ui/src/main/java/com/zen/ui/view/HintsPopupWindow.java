package com.zen.ui.view;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.zen.api.MyApi;
import com.zen.api.protocol.Error;
import com.zen.ui.R;


public class HintsPopupWindow extends PopupWindow implements View.OnClickListener {
    private static final String TAG = "HintsPopupWindow";
    private final TextView mTextViewReminder;
    private final TextView mTextViewHowFix;

    private com.zen.api.protocol.Error error;
    private onButtonClickListener onClickListener;


    public HintsPopupWindow(Context context) {
        super(context);
        View contentView = LayoutInflater.from(context).inflate(R.layout.hints_popup_layout, null);
        setContentView(contentView);
        //setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        //setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setFocusable(true);
        setOutsideTouchable(true);
        setBackgroundDrawable(new ColorDrawable(0x00000000));
        contentView.findViewById(R.id.bt_go_next).setOnClickListener(this);
        contentView.findViewById(R.id.view_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        mTextViewReminder = contentView.findViewById(R.id.tv_reminder);
        mTextViewHowFix = contentView.findViewById(R.id.tv_how_fix);


    }


    public void showAsDropDown(View anchor, int xoff, int yoff) {
        super.showAsDropDown(anchor, xoff, yoff);

    }

    public void setError(Error error) {
        this.error = error;
       if(mTextViewReminder!=null) mTextViewReminder.setText(error==null?"":error.getErrString());
       if(mTextViewHowFix!=null) mTextViewHowFix.setText(error==null?"":error.getFixString());
    }

    public Error getError() {
        return error;
    }


    public interface onButtonClickListener {
        void onClick(Object index);
    }

    public void setOnClickListener(onButtonClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    @Override
    public void onClick(View v) {
        //  int id = v.getId();
        // int ret = -1;

        if (onClickListener != null) {
            onClickListener.onClick(v.getTag());
        }
        MyApi.getInstance().getDataApi().readReminder(error);
        dismiss();
    }

    public void dismiss() {
        super.dismiss();

    }

}
