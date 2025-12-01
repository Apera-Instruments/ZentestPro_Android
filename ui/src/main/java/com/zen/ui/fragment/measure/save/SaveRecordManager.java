package com.zen.ui.fragment.measure.save;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.zen.api.MyApi;
import com.zen.api.RestApi;
import com.zen.api.data.Record;
import com.zen.api.protocol.CalibrationCond;
import com.zen.api.protocol.CalibrationPh;
import com.zen.api.data.DeviceSetting;
import com.zen.api.Constant;

import java.io.File;

public class SaveRecordManager {

    private static final String TAG = "SaveRecordManager";

    public static class SnapshotInfo {
        public String traceNo;
        public double value;
        public String valueUnit;
        public String tempText;      // e.g. "25.0"
        public String tempUnitStr;   // "C"/"F"
        public int modeType;
        public int tabType;
        public String slopeText;
        public File imageFile;
    }

    public void saveSnapshot(SnapshotInfo info) {
        if (info == null || info.imageFile == null) return;

        Record record = new Record();
        record.setTraceNo(info.traceNo);
        record.setTempValue(info.tempText);
        record.setPotential(MyApi.getInstance().getBtApi().getLastDevice().getModle());
        record.setOperator("Admin");
        record.setCategory("Default");
        record.setSlop(info.slopeText);
        record.setPic(info.imageFile.getAbsolutePath());
        record.setType(info.modeType);
        record.setTabType(info.tabType);
        record.setValue(info.value);
        record.setValueUnit(info.valueUnit);
        record.setUserId(MyApi.getInstance().getRestApi().getUserId());
        record.setCreateTime(System.currentTimeMillis());

        try {
            record.setDeviceNumber(MyApi.getInstance().getBtApi().getDeviceNumber());
            record.setLocation(MyApi.getInstance().getDataApi().getLocation());
            record.setTempUnit(info.tempUnitStr);

            DeviceSetting setting = MyApi.getInstance().getBtApi().getLastDevice().getSetting();
            if (setting != null) {
                if (info.modeType == Constant.MODE_PH) {
                    CalibrationPh ph = setting.getCalibrationPh();
                    if (ph != null) record.setCalibration(ph.toString());
                } else if (info.modeType == Constant.MODE_COND) {
                    CalibrationCond cond = setting.getCalibrationCond();
                    if (cond != null) record.setCalibration(cond.toString());
                }
            }
        } catch (Exception ignored) { }

        MyApi.getInstance().getDataApi().saveRecord(record);
        Log.d(TAG, "Saved record: " + JSON.toJSONString(record));
    }

    public void deleteRecord(Record record) {
        if (record == null) return;
        String id = MyApi.getInstance().getDataApi().setDelRecord(record.getId());
        int ret = MyApi.getInstance().getRestApi().dataDelete(id, record.getType());
        if (ret == RestApi.SUCCESS) {
            MyApi.getInstance().getDataApi().delRecord(record.getId());
        }
    }
}
