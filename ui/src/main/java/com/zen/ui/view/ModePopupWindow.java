package com.zen.ui.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;

import com.zen.api.BtApi;
import com.zen.api.Constant;
import com.zen.api.MyApi;
import com.zen.ui.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ModePopupWindow extends PopupWindow implements View.OnClickListener {

    private Button bt1, bt2, bt3, bt4, bt5, bt6;
    private List<Button> buttonList = new ArrayList<>();

    // Mapping Button -> Mode Constant
    private final Map<Integer, Integer> buttonModeMap = new HashMap<>();

    // Multi-select states
    private boolean selPH = false;
    private boolean selCOND = false;
    private boolean selORP = false;

    private static final int MAX_SELECTED = 3;

    private onButtonClickListener onClickListener;

    public interface onButtonClickListener {
        void onClick(int index);
    }

    public void setOnClickListener(onButtonClickListener listener) {
        this.onClickListener = listener;
    }

    public ModePopupWindow(Context context) {
        super(context);
        View contentView = LayoutInflater.from(context).inflate(R.layout.modepopuplayout, null);
        setContentView(contentView);
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setFocusable(true);
        setOutsideTouchable(true);
        setBackgroundDrawable(new ColorDrawable(0x00000000));

        bt1 = contentView.findViewById(R.id.pop_bt1);
        bt2 = contentView.findViewById(R.id.pop_bt2);
        bt3 = contentView.findViewById(R.id.pop_bt3);
        bt4 = contentView.findViewById(R.id.pop_bt4);
        bt5 = contentView.findViewById(R.id.pop_bt5);
        bt6 = contentView.findViewById(R.id.pop_bt6);

        buttonList.add(bt1);
        buttonList.add(bt2);
        buttonList.add(bt3);
        buttonList.add(bt4);
        buttonList.add(bt5);
        buttonList.add(bt6);

        // Setup click listeners
        for (Button b : buttonList) {
            b.setOnClickListener(this);
        }

        // Map button ID -> mode constant
        buttonModeMap.put(R.id.pop_bt1, Constant.MODE_PH);
        buttonModeMap.put(R.id.pop_bt2, Constant.MODE_SAL);
        buttonModeMap.put(R.id.pop_bt3, Constant.MODE_ORP);
        buttonModeMap.put(R.id.pop_bt4, Constant.MODE_TDS);
        buttonModeMap.put(R.id.pop_bt5, Constant.MODE_COND);
        buttonModeMap.put(R.id.pop_bt6, Constant.MODE_RES);
    }

    @Override
    public void onClick(View v) {
        Integer mode = buttonModeMap.get(v.getId());
        if (mode == null) return;

        if (onClickListener != null) {
            onClickListener.onClick(mode);
        }
    }

    // ------------------------------
    //  Update Button State (enable/disable)
    // ------------------------------
    public void updateButtonView() {
        BtApi btApi = MyApi.getInstance().getBtApi();

        updateOneButton(bt1, btApi.isModePH());
        updateOneButton(bt2, btApi.isModeSAL());
        updateOneButton(bt3, btApi.isModeORP());
        updateOneButton(bt4, btApi.isModeTDS());
        updateOneButton(bt5, btApi.isModeCOND());
        updateOneButton(bt6, btApi.isModeRES());
    }

    private void updateOneButton(Button btn, boolean enabled) {
        if (!enabled) {
            btn.setTextColor(Color.GRAY);
            btn.setEnabled(false);
            btn.setBackgroundResource(R.drawable.bt_disable_bg);
            btn.setTag(null);
            btn.setOnClickListener(null);
        } else {
            btn.setEnabled(true);
            btn.setTag(Boolean.TRUE);
            btn.setOnClickListener(this);
        }
    }

    // ------------------------------
    //  Highlight Selected Button (single-select)
    // ------------------------------
    public void setButtonEnable(int index) {
        for (Button b : buttonList) {
            if (b.getTag() != null) {
                b.setEnabled(true);
            }
        }

        // disable selected
        for (Map.Entry<Integer, Integer> e : buttonModeMap.entrySet()) {
            if (e.getValue() == index) {
                Button btn = ((View) getContentView()).findViewById(e.getKey());
                if (btn != null) btn.setEnabled(false);
                break;
            }
        }
    }

    // ------------------------------
    //  Multi-select Logic (PH + COND + ORP only)
    // ------------------------------
    public void setMultiSelectState(boolean ph, boolean cond, boolean orp) {
        this.selPH = ph;
        this.selCOND = cond;
        this.selORP = orp;
        refreshMultiSelectUI();
    }

    private void refreshMultiSelectUI() {
        // PH
        if (selPH) highlight(bt1); else normalize(bt1);

        // SAL / TDS / RES are not selectable in this mode

        // COND
        if (selCOND) highlight(bt5); else normalize(bt5);

        // ORP
        if (selORP) highlight(bt3); else normalize(bt3);
    }

    private void highlight(Button b) {
        b.setBackgroundResource(R.drawable.mode_menu_bt_selected);
        b.setTextColor(Color.WHITE);
    }

    private void normalize(Button b) {
        b.setBackgroundResource(R.drawable.mode_menu_bt_bg);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            b.setTextColor(b.getContext().getColor(R.color.mode_menu_bt));
        }
    }
}