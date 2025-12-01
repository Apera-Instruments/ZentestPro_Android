package com.zen.ui.fragment.measure.manager;

import com.zen.api.Constant;
import com.zen.api.MyApi;
import com.zen.api.protocol.Mode;

/**
 * Handles measurement mode & graph mode switching.
 */
public class ModeManager {

    public interface ModeChangeListener {
        void onModeChanged(int newMode);
        void onGraphModeChanged(int newGraphMode);
        void onClearHistory();
    }

    private final ModeChangeListener listener;
    private int modeType = Constant.MODE_PH;          // UI “mode” (ph/cond/orp/sal/tds/res)
    private int graphModeType = Constant.MODE_VELA_PH; // which parameter’s graph is currently shown

    public ModeManager(ModeChangeListener listener) {
        this.listener = listener;
    }

    public int getModeType() {
        return modeType;
    }

    public int getGraphModeType() {
        return graphModeType;
    }

    public void setModeFromData(int newMode) {
        if (newMode != 0 && newMode != modeType) {
            modeType = newMode;
            if (listener != null) {
                listener.onModeChanged(modeType);
            }
        }
    }

    public void setGraphModeType(int mode) {
        if (graphModeType != mode) {
            graphModeType = mode;
            if (listener != null) {
                listener.onClearHistory();
                listener.onGraphModeChanged(graphModeType);
            }
        }
    }

    public void changeMode(int newModeType) {
        if (newModeType == modeType) return;
        modeType = newModeType;

        if (listener != null) {
            listener.onClearHistory();
            listener.onModeChanged(modeType);
        }

        Mode modeCmd = new Mode();
        modeCmd.setMode(convertMode(modeType));
        MyApi.getInstance().getBtApi().sendCommand(modeCmd);
    }

    private int convertMode(int type) {
        switch (type) {
            case Constant.MODE_COND:
                return Mode.Cond;
            case Constant.MODE_PH:
                return Mode.pH;
            case Constant.MODE_SAL:
                return Mode.Salt;
            case Constant.MODE_ORP:
                return Mode.mV;
            case Constant.MODE_TDS:
                return Mode.TDS;
            case Constant.MODE_RES:
                return Mode.Res;
            default:
                return 0;
        }
    }
}