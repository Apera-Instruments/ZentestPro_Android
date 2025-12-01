package com.zen.api.data;

import com.zen.api.protocol.CalibrationCond;
import com.zen.api.protocol.CalibrationPh;

public class Calibration {
    public CalibrationCond getCalibrationCond() {
        return calibrationCond;
    }

    public void setCalibrationCond(CalibrationCond calibrationCond) {
        this.calibrationCond = calibrationCond;
    }

    public CalibrationPh getCalibrationPh() {
        return calibrationPh;
    }

    public void setCalibrationPh(CalibrationPh calibrationPh) {
        this.calibrationPh = calibrationPh;
    }

    private CalibrationCond calibrationCond;
    private CalibrationPh calibrationPh;
    private String refTemp;

    public String getRefTemp() {
        return refTemp;
    }

    public void setRefTemp(String refTemp) {
        this.refTemp = refTemp;
    }

    public String getTempCompensate() {
        return tempCompensate;
    }

    public void setTempCompensate(String tempCompensate) {
        this.tempCompensate = tempCompensate;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    private String tempCompensate;
    private String mode;
}
