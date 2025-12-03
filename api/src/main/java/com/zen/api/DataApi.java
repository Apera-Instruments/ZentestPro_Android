package com.zen.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

import com.zen.api.data.BleDevice;
import com.zen.api.data.Category;
import com.zen.api.data.DataBean;
import com.zen.api.data.NoteData;
import com.zen.api.data.Record;
import com.zen.api.protocol.CalibrationCond;
import com.zen.api.protocol.CalibrationPh;
import com.zen.api.protocol.Data;
import com.zen.api.protocol.Error;
import com.zen.api.protocol.ParmUp;
import com.zen.api.protocol.velaprotocal.VelaParamModeDeviceToApp;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

public interface DataApi {
    int CONNECTED = 0;
    int DISCONNECTED = 1;
    int CALIBRATION = 2;
    int SETTING = 3;

    DataBean getData();

    DataBean getDataById(long id);

    Record getRecordById(long id);

    void saveRecord(Record record);

    List<Record> getRecords();
    List<Record> getSyncRecords(boolean sync);

    void setLastData(Data data);

    void saveData(int sn,String traceNo ,String att);

    void setLastParmUp(ParmUp parm);

    public void setLastVelaModeUpload(VelaParamModeDeviceToApp upload);

    void setLastCalibrationCond(CalibrationCond calibrationCond);

    void setLastCalibrationPh(CalibrationPh calibrationPh);

    boolean readUpdate();

    ParmUp getLastParm();

    int conventMode(int mode);

    void updateBleDevice(BleDevice bleDevice ,int r);

    void delRecord(long dataId);

    void setReminder(int b);

    void readReminder();

    int getReminder();

    void autoSave();
    SharedPreferences getSetting();

    List<DataBean>  getDataBean(String traceNo);

    File exportCSV(Context context , Record record, String[] cols);

    void putReminder(Error error);

    void readReminder(Error error);

    Error getReminderError();

    void updateRecords(@Nullable Record value);
    void insertIfNoneRecords(@Nullable Record value);
    void updateRecordsSync(@Nullable Record value);

    String setDelRecord(long dataId);

    void delRecordByUser(String userId);

    void putLocation(@NotNull Location location);

    String getLocation();

    List<Category> getCategory();

    long addCategory(Category category);

    void delCategory(long id);

    NoteData getNoteById(@Nullable String noteId);

    void updateNote(@NotNull NoteData noteData);

    @Nullable
    Long insertNote(@NotNull NoteData noteData);

    List<Record> getRecordByCategory(String name);

    Category getCategoryById(long categoryId);

    boolean isCalibration();

    void setCalibration(boolean b);

}
