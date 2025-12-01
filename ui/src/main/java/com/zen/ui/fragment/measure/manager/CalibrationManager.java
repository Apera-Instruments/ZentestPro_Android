package com.zen.ui.fragment.measure.manager;

import com.zen.api.Constant;
import com.zen.api.data.DataBean;
import com.zen.api.protocol.CalibrationCond;
import com.zen.api.protocol.CalibrationPh;
import com.zen.ui.fragment.measure.utils.FormatUtils;
import com.zen.ui.fragment.measure.utils.TimeUtils;

public class CalibrationManager {

    public static class PhCalibrationUI {
        public boolean showSlop;
        public boolean showZeroPoint;
        public boolean showPoints;
        public boolean showLastDate;
        public String offsetText;
        public String slopeText;
        public String dateText;
        public boolean[] buffers = new boolean[5]; // 1.68 / 4.00 / 7.00 / 10.01 / 12.45
    }

    public static class CondCalibrationUI {
        public boolean show;
        public String dateText;
        public boolean is84;
        public boolean is1413;
        public boolean is1288;
    }

    public PhCalibrationUI buildPhUI(DataBean bean, int pHSelect) {
        PhCalibrationUI ui = new PhCalibrationUI();
        CalibrationPh ph = bean.getCalibrationPh();
        if (ph == null) return ui;

        ui.offsetText = FormatUtils.formatDouble(ph.getOffset1(), 1) + "mV";
        ui.slopeText = FormatUtils.formatDouble(ph.getSlope1(), 1) + "%";
        ui.dateText = TimeUtils.formatDateTime(ph.getDate());

        int c = 0;
        if (ph.is168()) { ui.buffers[0] = true; c++; }
        if (ph.is400()) { ui.buffers[1] = true; c++; }
        if (ph.is700()) { ui.buffers[2] = true; c++; }
        if (ph.is1001()) { ui.buffers[3] = true; c++; }
        if (ph.is1245()) { ui.buffers[4] = true; c++; }

        ui.showSlop = (c > 1);
        ui.showZeroPoint = true;
        ui.showPoints = true;
        ui.showLastDate = true;

        return ui;
    }

    public CondCalibrationUI buildCondUI(DataBean bean) {
        CondCalibrationUI ui = new CondCalibrationUI();
        CalibrationCond cond = bean.getCalibrationCond();
        if (cond == null) return ui;

        ui.show = true;
        ui.dateText = TimeUtils.formatDateTime(cond.getDate());
        ui.is84 = cond.is84();
        ui.is1413 = cond.is1413();
        ui.is1288 = cond.is1288();
        return ui;
    }

    public boolean usesSalinity(int modeType) {
        return modeType == Constant.MODE_SAL;
    }

    public boolean usesTds(int modeType) {
        return modeType == Constant.MODE_TDS;
    }
}