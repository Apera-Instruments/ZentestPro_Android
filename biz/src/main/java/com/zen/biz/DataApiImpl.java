package com.zen.biz;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.zen.api.Constant;
import com.zen.api.DataApi;
import com.zen.api.MyApi;
import com.zen.api.data.BleDevice;
import com.zen.api.data.Category;
import com.zen.api.data.DataBean;
import com.zen.api.data.DeviceSetting;
import com.zen.api.data.NoteData;
import com.zen.api.data.Record;
import com.zen.api.protocol.CalibrationCond;
import com.zen.api.protocol.CalibrationPh;
import com.zen.api.protocol.Data;
import com.zen.api.protocol.Error;
import com.zen.api.protocol.ParmUp;
import com.zen.api.protocol.velaprotocal.VelaParamModeDeviceToApp;
import com.zen.biz.greendao.gen.CategoryDao;
import com.zen.biz.greendao.gen.DaoSession;
import com.zen.biz.greendao.gen.DataBeanDao;
import com.zen.biz.greendao.gen.DeviceDao;
import com.zen.biz.greendao.gen.NoteDao;
import com.zen.biz.greendao.gen.RecordDao;
import com.zen.biz.table.Device;
import com.zen.biz.table.Note;
import com.zen.biz.utils.ExportCSV;

import org.greenrobot.greendao.query.QueryBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataApiImpl implements DataApi {
    private static final String TAG = DataApiImpl.class.getSimpleName();

    private Data mLastData;
    private DataBean zeroDataBean = new DataBean();
    private ParmUp mLastParmUp;
    private VelaParamModeDeviceToApp mLastModeUpload;
    private CalibrationCond mLastCalibrationCond;
    private CalibrationPh mLastCalibrationPh;
    private boolean mUpdate = true;
    private int mReminder = 0;
    private Map<Integer, Error> mErrorMap = new ConcurrentHashMap<>();
    private Location location;
    private boolean isCalibration;

    public DataApiImpl() {
        zeroDataBean.setEc(0);
        zeroDataBean.setPh(0);
        zeroDataBean.setTemp(0);
        zeroDataBean.setResistivity(0);
        zeroDataBean.setOrp(0);
        zeroDataBean.setTds(0);
        zeroDataBean.setSalinity(0);
        zeroDataBean.setMode(0);

    }


    @Override
    public DataBean getData() {
        if (mLastData == null) {
            return zeroDataBean;
        }
        DataBean dataBean = new DataBean();
        dataBean.setData(mLastData);
        //Random random= new Random();
        dataBean.setPointDigit(mLastData.getPointDigit());
        dataBean.setEc(mLastData.getEC());
        dataBean.setPh(mLastData.getPH());
        dataBean.setTemp(mLastData.getTemp());
        dataBean.setTempPointDigit(mLastData.getPointDigit2());
        dataBean.setResistivity(mLastData.getResistivity());
        dataBean.setOrp(mLastData.getORP());
        dataBean.setTds(mLastData.getTDS());
        dataBean.setSalinity(mLastData.getSalinity());
        dataBean.setMode(conventMode(mLastData.getMode()));
        dataBean.setH(mLastData.isH());
        dataBean.setL(mLastData.isL());
        dataBean.setM(mLastData.isM());
        dataBean.setHold(mLastData.isHold());
        dataBean.setLaughFace(mLastData.isLaughFace());
        dataBean.setUpperAlarm(mLastData.isUpperAlarm());
        dataBean.setLowerAlarm(mLastData.isLowerAlarm());
        dataBean.setReminder(mReminder > 0);
        if (mLastCalibrationPh != null) {
            dataBean.setCalibrationPh(mLastCalibrationPh);
   /*         mLastCalibrationPh.getDate();
            mLastCalibrationPh.getSlop1();
            mLastCalibrationPh.getOffset1();
            mLastCalibrationPh.getSlop2();
            mLastCalibrationPh.getOffset2();
            mLastCalibrationPh.is168();
            mLastCalibrationPh.is400();
            mLastCalibrationPh.is700();
            mLastCalibrationPh.is1001();
            mLastCalibrationPh.is1245();
            mLastCalibrationPh.getPointCount();*/
        }
        if (mLastCalibrationCond != null) {
            dataBean.setCalibrationCond(mLastCalibrationCond);
         /*   mLastCalibrationCond.getDate();
            mLastCalibrationCond.getSlop1();
            mLastCalibrationCond.getOffset1();
            mLastCalibrationCond.getSlop2();
            mLastCalibrationCond.getOffset2();
            mLastCalibrationCond.is84();
            mLastCalibrationCond.is1413();
            mLastCalibrationCond.is1288();
            mLastCalibrationCond.getPointCount();*/
        }

        // --------------------------------------------------
        // NEW: merge multi-mode from VelaParamModeUpload
        // --------------------------------------------------
        if (mLastModeUpload != null) {

            // ----- PH block -----
            dataBean.setPhSelected(mLastModeUpload.isPhmvSelected());
            dataBean.setPhMode(
                    (mLastModeUpload.getPhmvMode() == VelaParamModeDeviceToApp.PhMvMode.MV)
                            ? DataBean.PhMode.MV
                            : DataBean.PhMode.PH
            );

            // ----- COND block -----
            dataBean.setCondSelected(mLastModeUpload.isCondSelected());
            switch (mLastModeUpload.getCondMode()) {
                case TDS:
                    dataBean.setCondMode(DataBean.CondMode.TDS);
                    break;
                case SALT:
                    dataBean.setCondMode(DataBean.CondMode.SAL);
                    break;
                case RESISTIVITY:
                    dataBean.setCondMode(DataBean.CondMode.RES);
                    break;
                default:
                    dataBean.setCondMode(DataBean.CondMode.COND);
                    break;
            }

            // ----- ORP block -----
            dataBean.setOrpSelected(mLastModeUpload.isOrpSelected());
            dataBean.setOrpMode(DataBean.OrpMode.ORP);
        }

        return dataBean;
    }

    public int conventMode(int mode) {
        switch (mode) {
            case Data.pH:
                return Constant.MODE_PH;
            case Data.Salt:
                return Constant.MODE_SAL;
            case Data.mV:
                return Constant.MODE_ORP;
            case Data.TDS:
                return Constant.MODE_TDS;
            case Data.Cond:
                return Constant.MODE_COND;
            case Data.Res:
                return Constant.MODE_RES;
        }
        return 0;
    }

    @Override
    public void updateBleDevice(BleDevice bleDevice, int reason) {
        if (bleDevice == null) {
            Log.w(TAG, "bleDevice is null");
            return;
        }
        DaoSession daoSession = Install.getInstance().getDaoSession();
        DeviceDao deviceDao = daoSession.getDeviceDao();
        QueryBuilder<Device> queryBuilder = deviceDao.queryBuilder();
        queryBuilder.where(DeviceDao.Properties.Mac.eq(bleDevice.getMac()));
        List<Device> list = queryBuilder.build().list();
        Device device;
        if (list == null || list.isEmpty()) {
            device = new Device();
            device.setMac(bleDevice.getMac());
            device.setName(bleDevice.getName());
            device.setCreateTime(System.currentTimeMillis());
            if (reason == CONNECTED) device.setConnectTime(System.currentTimeMillis());
            if (reason == DISCONNECTED) {
                device.setDisconnectTime(System.currentTimeMillis());
                device.setLastDataTime(System.currentTimeMillis());
            }
            if (reason == CALIBRATION) device.setLastCalibrationTime(System.currentTimeMillis());
            if (reason == SETTING) {
                device.setLastSettingTime(System.currentTimeMillis());
            }
            if (bleDevice.getSetting() != null) {
                device.setAttach(JSON.toJSONString(bleDevice.getSetting()));
                Log.d(TAG, "setAttach " + device.getAttach());
            }
            long ret = deviceDao.insert(device);
            log("deviceDao.insert(device) " + ret + " " + device.getMac());
        } else {
            device = list.get(0);
            if (reason == CONNECTED) {
                device.setConnectTime(System.currentTimeMillis());
                if (device.getAttach() != null) {
                    bleDevice.setSetting(JSON.parseObject(device.getAttach(), DeviceSetting.class));
                    mLastCalibrationPh = bleDevice.getSetting().getCalibrationPh();
                    mLastCalibrationCond = bleDevice.getSetting().getCalibrationCond();
                    Log.d(TAG, "getAttach " + device.getAttach());
                }

            } else if (reason == DISCONNECTED) {
                device.setDisconnectTime(System.currentTimeMillis());
                device.setLastDataTime(System.currentTimeMillis());
                if (bleDevice.getSetting() != null) {
                    bleDevice.getSetting().setCalibrationCond(mLastCalibrationCond);
                    bleDevice.getSetting().setCalibrationPh(mLastCalibrationPh);
                    device.setAttach(JSON.toJSONString(bleDevice.getSetting()));
                }
            } else if (reason == CALIBRATION) {
                device.setLastCalibrationTime(System.currentTimeMillis());
            } else if (reason == SETTING) {
                device.setLastSettingTime(System.currentTimeMillis());
                if (bleDevice.getSetting() != null) {
                    device.setAttach(JSON.toJSONString(bleDevice.getSetting()));
                }
            }

            deviceDao.update(device);
            log("deviceDao.update(device) " + device.getMac());
        }


    }
    @Override
    public String setDelRecord(long dataId){
        RecordDao recordDao = Install.getInstance().getDaoSession().getRecordDao();
        com.zen.biz.table.Record record = recordDao.loadByRowId(dataId);
        record.setUseState(false);
        record.setSync(false);
        recordDao.update(record);

        return record.getSyncId();
    }

    @Override
    public void delRecordByUser(String userId) {
        if (userId == null) return;
        try {
            RecordDao recordDao = Install.getInstance().getDaoSession().getRecordDao();
            recordDao.queryBuilder().where(RecordDao.Properties.UserId.eq(userId)).buildDelete().executeDeleteWithoutDetachingEntities();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void putLocation(@NotNull Location location) {
        this.location = location;
    }

    @Override
    public String getLocation() {
        if (location == null) return "0,0";
        return ""+location.getLatitude() +","+location.getLongitude();
    }


    ;
    public boolean isCalibration(){return isCalibration;}
    public void setCalibration(boolean b){ isCalibration=b;}

    @Override
    public Category getCategoryById(long categoryId) {
        CategoryDao categoryDao = Install.getInstance().getDaoSession().getCategoryDao();
        com.zen.biz.table.Category category = categoryDao.loadByRowId(categoryId);
        if(category==null) return null;
        Category category1 = new Category();
        category1.setId(category.getId());
        category1.setName(category.getName());
        category1.setCreateTime(category.getCreateTime());
        category1.setUpdateTime(category.getUpdateTime());


        return category1;
    }

    @Override
    public List<Category> getCategory() {
        CategoryDao categoryDao =  Install.getInstance().getDaoSession().getCategoryDao();
        List<com.zen.biz.table.Category> list= categoryDao.loadAll();
        if(list==null ||list.isEmpty())
        return null;
        List<Category> categoryList = new ArrayList<>();
        for(com.zen.biz.table.Category category:list){
            Category category1 = new Category();
            category1.setId(category.getId());
            category1.setName(category.getName());
            category1.setCreateTime(category.getCreateTime());
            category1.setUpdateTime(category.getUpdateTime());
            categoryList.add(category1);
        }
        return  categoryList;
    }

    @Override
    public long addCategory(Category c) {
        CategoryDao categoryDao = Install.getInstance().getDaoSession().getCategoryDao();
        List<com.zen.biz.table.Category> list = categoryDao.queryBuilder().where(CategoryDao.Properties.Name.eq(c.getName())).build().list();
        if (list.size() > 0) {
            return -1;
        }
        com.zen.biz.table.Category category = new com.zen.biz.table.Category();
        category.setCreateTime(System.currentTimeMillis());
        category.setUpdateTime(System.currentTimeMillis());
        category.setUserId(getUserId());
        category.setName(c.getName());
        long ret = categoryDao.insert(category);

        return ret;
    }
    @Override
    public void delCategory(long id){
        CategoryDao categoryDao =  Install.getInstance().getDaoSession().getCategoryDao();
        categoryDao.deleteByKey(id);

    }

    @Override
    public NoteData getNoteById(@Nullable String noteId) {
        try {
            if(noteId==null) return null;
            NoteDao dao = Install.getInstance().getDaoSession().getNoteDao();
            Note note = dao.loadByRowId(Long.parseLong(noteId));
            NoteData noteData = new NoteData();
            noteData.setId(note.getId());
            noteData.setNoteName(note.getNoteName());
            noteData.setAttachedPhotos(note.getAttachedPhotos());
            noteData.setNoteAdmin(note.getNoteAdmin());
            noteData.setNotes(note.getNotes());
            noteData.setNoteStatus(note.getNoteStatus());
            noteData.setUserId(note.getUserId());
            noteData.setUpdateTime(note.getUpdateTime());
            return noteData;
        }catch (Exception e){

        }
        return null;
    }

    @Override
    public void updateNote(@NotNull NoteData note) {
        try {
            NoteDao dao = Install.getInstance().getDaoSession().getNoteDao();
            Note noteData = new Note();
            noteData.setId(note.getId());
            noteData.setNoteName(note.getNoteName());
            noteData.setAttachedPhotos(note.getAttachedPhotos());
            noteData.setNoteAdmin(note.getNoteAdmin());
            noteData.setNotes(note.getNotes());
            noteData.setNoteStatus(note.getNoteStatus());
            noteData.setUserId(note.getUserId());
            noteData.setUpdateTime(note.getUpdateTime());
            dao.update(noteData);
        }catch (Exception e){

        }
    }

    @Nullable
    @Override
    public Long insertNote(@NotNull NoteData note) {
        try {
            NoteDao dao = Install.getInstance().getDaoSession().getNoteDao();

            Note noteData = new Note();
            noteData.setId(note.getId());
            noteData.setNoteName(note.getNoteName());
            noteData.setAttachedPhotos(note.getAttachedPhotos());
            noteData.setNoteAdmin(note.getNoteAdmin());
            noteData.setNotes(note.getNotes());
            noteData.setNoteStatus(note.getNoteStatus());
            noteData.setUserId(note.getUserId());
            noteData.setUpdateTime(note.getUpdateTime());
            return  dao.insert(noteData);
        }catch (Exception e){

        }
        return null;
    }

    @Override
    public void delRecord(long dataId) {
        RecordDao recordDao = Install.getInstance().getDaoSession().getRecordDao();
        com.zen.biz.table.Record record =recordDao.loadByRowId(dataId);
        recordDao.deleteByKey(dataId);

        if(record!=null){
            if (!TextUtils.isEmpty(record.getPic())) {
                try {
                    File file = new File(record.getPic());
                    if (file.exists()) {
                        boolean r = file.delete();
                        if (r) {
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            if (!TextUtils.isEmpty(record.getLocalPic())) {
                try {
                    File file2 = new File(record.getLocalPic());
                    if (file2.exists()) {
                        boolean r = file2.delete();
                        if (r) {

                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @Override
    public void setReminder(int b) {
        if (b > 0) {
            for (int i = 0; i < b; i++) {
                Error error = new Error();
                error.setErr(i + 3 + 1);
                putReminder(error);
            }
        } else {
            mErrorMap.clear();
            this.mReminder = 0;
        }

    }

    @Override
    public void readReminder() {
        mReminder--;
    }

    @Override
    public int getReminder() {
        return mReminder;
    }

    @Override
    public void autoSave() {

    }

    @Override
    public SharedPreferences getSetting() {
        SharedPreferences sharedPreferences = Install.getInstance().getContext().getSharedPreferences("setting_v1", Context.MODE_PRIVATE);

        return sharedPreferences;
    }

    @Override
    public DataBean getDataById(long id) {
        return null;
    }

    @Override
    public List<Record> getRecordByCategory(String name) {
        List<com.zen.biz.table.Record> records = Install.getInstance().getDaoSession().getRecordDao().queryBuilder().where(
                RecordDao.Properties.UseState.eq(true),
                RecordDao.Properties.UserId.eq(getUserId()),
                RecordDao.Properties.Category.eq(name)
        ).orderDesc(RecordDao.Properties.CreateTime).build().list();
        if (records != null && records.size() > 0) {
            log("getRecords " + records.size());
            List<Record> list = new ArrayList<>();
            for (com.zen.biz.table.Record record : records) {
                Record record1 = new Record();
                record1.setId(record.getId());
                record1.setNoteId(record.getNoteId());
                record1.setNoteName(record.getNoteName());//
                record1.setNotes(record.getNotes());//
                record1.setCategory(record.getCategory());//
                record1.setLocation(record.getLocation());//
                record1.setDeviceNumber(record.getDeviceNumber());//
                record1.setOperator(record.getOperator());//
                record1.setPotential(record.getPotential());//
                record1.setTempValue(record.getTempValue());//
                record1.setTempUnit(record.getTempUnit());//
                record1.setTraceNo(record.getTraceNo());
                record1.setValue(record.getValue());
                record1.setValueUnit(record.getValueUnit());
                record1.setType(record.getType());
                record1.setPic(TextUtils.isEmpty(record.getPic()) ? record.getLocalPic() : record.getPic());
                record1.setCreateTime(record.getCreateTime());
                record1.setTabType(record.getTableType());
                record1.setSync(record.getSync());
                record1.setSyncId(record.getSyncId());
                record1.setUserId(record.getUserId());
                record1.setUseState(record.getUseState());
                record1.setDataBeanList(getDataBean(record.getTraceNo()));
                record1.setCalibration(record.getCalibration());
                list.add(record1);
            }
            return list;
        }
        return null;
    }

    @Override
    public Record getRecordById(long id) {
        com.zen.biz.table.Record record = Install.getInstance().getDaoSession().getRecordDao().loadByRowId(id);
        if (record != null) {
            Record record1 = new Record();
            record1.setId(record.getId());
            record1.setNoteId(record.getNoteId());
            record1.setNoteName(record.getNoteName());//
            record1.setNotes(record.getNotes());//
            record1.setCategory(record.getCategory());//
            record1.setTraceNo(record.getTraceNo());
            record1.setLocation(record.getLocation());//
            record1.setDeviceNumber(record.getDeviceNumber());//
            record1.setOperator(record.getOperator());//
            record1.setPotential(record.getPotential());//
            record1.setTempValue(record.getTempValue());//
            record1.setTempUnit(record.getTempUnit());
            record1.setValue(record.getValue());
            record1.setValueUnit(record.getValueUnit());
            record1.setType(record.getType());
            record1.setPic(TextUtils.isEmpty(record.getPic())?record.getLocalPic():record.getPic());
            record1.setCreateTime(record.getCreateTime());
            record1.setTabType(record.getTableType());
            record1.setSync(record.getSync());
            record1.setSyncId(record.getSyncId());
            record1.setUserId(record.getUserId());
            record1.setUseState(record.getUseState());
            record1.setDataBeanList(getDataBean(record.getTraceNo()));
            record1.setCalibration(record.getCalibration());

            //  DataBeanDao dataBeanDao = Install.getInstance().getDaoSession().getDataBeanDao();
/*            List<com.zen.biz.table.DataBean> list = dataBeanDao.queryBuilder().where(
                    DataBeanDao.Properties.RecordId.eq(record.getId())).list();
            List<DataBean> beanList = new ArrayList<>();
            if (beanList != null && beanList.size() > 0) {
                for (com.zen.biz.table.DataBean dataBean : list) {
                    DataBean dataBean1 = new DataBean();
                    dataBean1.setSalinity(dataBean.getSalinity());
                    dataBean1.setPH(dataBean.getPH());
                    beanList.add(dataBean1);
                }

            }*/

            record1.setDataBeanList(getDataBean(record.getTraceNo()));

            return record1;
        }
        return null;
    }

    @Override
    public void saveRecord(Record record) {
        RecordDao recordDao = Install.getInstance().getDaoSession().getRecordDao();
        com.zen.biz.table.Record record2 = new com.zen.biz.table.Record();
        record2.setSync(record.getSync());
        record2.setUserId(record.getUserId());
        record2.setSyncId(record.getSyncId());
        record2.setNoteId(record.getNoteId());
        record2.setNoteName(record.getNoteName());//
        record2.setNotes(record.getNotes());//
        record2.setCategory(record.getCategory());//
        record2.setLocation(record.getLocation());//
        record2.setDeviceNumber(record.getDeviceNumber());//
        record2.setOperator(record.getOperator());//
        record2.setPotential(record.getPotential());//
        record2.setTempValue(record.getTempValue());//
        record2.setTempUnit(record.getTempUnit());//
        record2.setTraceNo(record.getTraceNo());
        record2.setValue(record.getValue());
        record2.setValueUnit(record.getValueUnit());
        record2.setValueUnit(record.getValueUnit());
        record2.setType(record.getType());
        record2.setPic(record.getPic());
        record2.setCreateTime(record.getCreateTime());
        record2.setTableType(record.getTabType());
        record2.setSync(record.getSync());
        record2.setSyncId(record.getSyncId());
        record2.setUserId(record.getUserId());
        record2.setId(record.getId());
        record2.setUseState(true);

        record2.setLocalPic(record.getPic());
        //record2.setUserId(getUserId());
        //record2.setCreateTime(System.currentTimeMillis());
        record2.setUpdateTime(record.getCreateTime());
        //record2.setType(record.getType());
        //record2.setValue(record.getValue());
        //record2.setTableType(record.getTabType());
        //record2.setTraceNo(record.getTraceNo());
        record2.setSync(false);
        record2.setUseState(true);
        record2.setCalibration(record.getCalibration());
        Long i = recordDao.insert(record2);
        log("recordDao.insert " + i + " " + JSON.toJSONString(record2));
    }

    @Override
    public List<DataBean> getDataBean(String traceNo) {
        DataBeanDao dataBeanDao = Install.getInstance().getDaoSession().getDataBeanDao();
        List<com.zen.biz.table.DataBean> dataBeans = dataBeanDao.queryBuilder().where(DataBeanDao.Properties.TraceNo.eq(traceNo)).build().list();
        if (dataBeans != null && dataBeans.size() > 0) {
          /*  File file = new File(Install.getInstance().getContext().getExternalFilesDir("csv"),traceNo+".csv");
            for(com.zen.biz.table.DataBean dataBean:dataBeans){

            }*/
            List<DataBean> list = new ArrayList<>(dataBeans.size());
            for (com.zen.biz.table.DataBean dataBean : dataBeans) {
                DataBean bean = new DataBean();
                bean.setCreateTime(dataBean.getCreateTime());
                bean.setUpdateTime(dataBean.getUpdateTime());
                bean.setSn(dataBean.getSn());
                bean.setEc(dataBean.getEC());
                bean.setMode(dataBean.getMode());
                bean.setOrp(dataBean.getORP());
                bean.setPh(dataBean.getPH());
                bean.setTds(dataBean.getTDS());
                bean.setTemp(dataBean.getTemp());
                bean.setResistivity(dataBean.getResistivity());
                bean.setSalinity(dataBean.getSalinity());
                bean.setUnitString(dataBean.getUnitString());
                bean.setUnit2(dataBean.getUnit2());
                bean.setPointDigit2(dataBean.getPointDigit2());
                bean.setPointDigit(dataBean.getPointDigit());
                bean.setValue(dataBean.getValue());
                bean.setValue2(dataBean.getValue2());
                bean.setAttach(dataBean.getAttach());
                list.add(bean);
            }
            return list;
        }
        return null;
    }

    @Override
    public File exportCSV(Context context, Record record, String[] cols) {
        if (record.getDataBeanList() == null || record.getDataBeanList().size() == 0) {
            Log.i("exportCSV","getDataBeanList none");
            return null;
        }
        File file = new File(context.getExternalFilesDir("save"), record.getTraceNo() + ".csv");
        return ExportCSV.exportToCSV(record, file, cols);

    }

    @Override
    public void putReminder(Error error) {
        if (error != null) {
            switch (error.getErrCode()) {
                case Error.Err1:
                    if(isCalibration()) {
                        error.setErrString(MyApi.getInstance().getString(R.string.err1));
                        error.setFixString(MyApi.getInstance().getString(R.string.fix1));
                    }else{
                        return;
                    }
                    break;
                case Error.Err2:
                    if(isCalibration()) {
                        error.setErrString(MyApi.getInstance().getString(R.string.err2));
                        error.setFixString(MyApi.getInstance().getString(R.string.fix2));
                    }else{
                        return;
                    }
                    break;
                case Error.Err3:
                    if(isCalibration()) {
                        error.setErrString(MyApi.getInstance().getString(R.string.err3));
                        error.setFixString(MyApi.getInstance().getString(R.string.fix3));
                    }else{
                        return;
                    }
                    break;
                case Error.Err4:
                    if(isCalibration()) return;
                    error.setErrString(MyApi.getInstance().getString(R.string.err4));
                    error.setFixString(MyApi.getInstance().getString(R.string.fix4));
                    break;
                case Error.Err5:
                    if(isCalibration()) return;
                    error.setErrString(MyApi.getInstance().getString(R.string.err5));
                    error.setFixString(MyApi.getInstance().getString(R.string.fix5));
                    break;
                case Error.Err6:
                    if(isCalibration()) return;
                    error.setErrString(MyApi.getInstance().getString(R.string.err6));
                    error.setFixString(MyApi.getInstance().getString(R.string.fix6));
                    break;
                default:
                    break;

            }
            if (error.getErrCode() >= 4) {
                mErrorMap.put(error.getErrCode(), error);
            }
            mReminder = mErrorMap.size();
        }
    }

    @Override
    public void readReminder(Error error) {
        if (error != null) {
            mErrorMap.remove(error.getErrCode());
            mReminder = mErrorMap.size();
        }
    }

    @Override
    public Error getReminderError() {
        if (mErrorMap.size() == 0)
            return null;
        Error error = mErrorMap.entrySet().iterator().next().getValue();
        if (isCalibration) {
            if (error.getErrCode() == Error.Err1
                    || error.getErrCode() == Error.Err2
                    || error.getErrCode() == Error.Err3
                    ) {
                return error;
            } else {
                readReminder(error);
                return null;
            }
        } else {
            if (error.getErrCode() == Error.Err4
                    || error.getErrCode() == Error.Err5
                    || error.getErrCode() == Error.Err6
                    ) {
                return error;
            } else {
                readReminder(error);
                return null;
            }
        }

    }

    @Override
    public void updateRecords(@Nullable Record value) {
        if (value == null ) {
            log("updateRecords some is null");
            return;
        }
        RecordDao recordDao = Install.getInstance().getDaoSession().getRecordDao();
        List<com.zen.biz.table.Record> records = recordDao.queryBuilder().where(
                RecordDao.Properties.TraceNo.eq(value.getTraceNo()),
                RecordDao.Properties.UserId.eq(value.getUserId())
        ).build().list();
        if (records == null || records.isEmpty()) {
            com.zen.biz.table.Record record1 = new com.zen.biz.table.Record();
            record1.setSync(value.getSync());
            record1.setUserId(value.getUserId());
            record1.setSyncId(value.getSyncId());
            record1.setNoteId(value.getNoteId());
            record1.setNoteName(value.getNoteName());//
            record1.setNotes(value.getNotes());//
            record1.setCategory(value.getCategory());//
            record1.setLocation(value.getLocation());//
            record1.setDeviceNumber(value.getDeviceNumber());//
            record1.setOperator(value.getOperator());//
            record1.setPotential(value.getPotential());//
            record1.setTempValue(value.getTempValue());//
            record1.setTempUnit(value.getTempUnit());//
            record1.setTraceNo(value.getTraceNo());
            record1.setValue(value.getValue());
            record1.setType(value.getType());
            record1.setPic(value.getPic());
            record1.setCreateTime(value.getCreateTime());
            record1.setTableType(value.getTabType());
            record1.setSync(value.getSync());
            record1.setSyncId(value.getSyncId());
            record1.setUserId(value.getUserId());
            record1.setUseState(true);
            record1.setCalibration(value.getCalibration());
            long ret = recordDao.insert(record1);
            log("insert "+ret);
        } else {
            com.zen.biz.table.Record record1 = records.get(0);
            record1.setSync(value.getSync());
            record1.setUserId(value.getUserId());
            record1.setSyncId(value.getSyncId());
            record1.setNoteId(value.getNoteId());
            record1.setNoteName(value.getNoteName());//
            record1.setNotes(value.getNotes());//
            record1.setCategory(value.getCategory());//
            record1.setLocation(value.getLocation());//
            record1.setDeviceNumber(value.getDeviceNumber());//
            record1.setOperator(value.getOperator());//
            record1.setPotential(value.getPotential());//
            record1.setTempValue(value.getTempValue());//
            record1.setTempUnit(value.getTempUnit());//
            record1.setTraceNo(value.getTraceNo());
            record1.setValue(value.getValue());
            record1.setType(value.getType());
            record1.setPic(value.getPic());
            record1.setCreateTime(value.getCreateTime());
            record1.setTableType(value.getTabType());
            record1.setSync(value.getSync());
            record1.setSyncId(value.getSyncId());
            record1.setUserId(value.getUserId());
            record1.setUseState(value.getUseState());
            record1.setCalibration(value.getCalibration());

            recordDao.update(record1);
            log("update " + record1.getId());
            log("zheli no log" + record1.getId());
        }
    }

    public void insertIfNoneDataBean(String traceNo,@NotNull DataBean bean) {

        DataBeanDao dataBeanDao = Install.getInstance().getDaoSession().getDataBeanDao();
        List<com.zen.biz.table.DataBean> dataBeanList  =  dataBeanDao.queryBuilder().where(
                DataBeanDao.Properties.TraceNo.eq(traceNo),
                DataBeanDao.Properties.UserId.eq(getUserId())
        ).build().list();
        if(dataBeanList==null || dataBeanList.isEmpty()){
            com.zen.biz.table.DataBean dataBean = new com.zen.biz.table.DataBean();
            dataBean.setTraceNo(traceNo);
            dataBean.setUserId(getUserId());
            dataBean.setCreateTime(bean.getCreateTime());
            dataBean.setTemp(bean.getTemp());
            dataBean.setUnit2(bean.getUnit2());
            dataBean.setUnitString(bean.getUnitString());
            dataBean.setValue(bean.getValue());
            Long i = dataBeanDao.insert(dataBean);
            log("dataBeanDao.insert " + i + " " + dataBean);
        }else {
            com.zen.biz.table.DataBean dataBean = dataBeanList.get(0);
            dataBean.setCreateTime(bean.getCreateTime());
            dataBean.setTemp(bean.getTemp());
            dataBean.setUnit2(bean.getUnit2());
            dataBean.setUnitString(bean.getUnitString());
            dataBean.setValue(bean.getValue());
            dataBeanDao.update(dataBean);
            log("dataBeanDao.found "+traceNo);
        }

    }


    @Override
    public void insertIfNoneRecords(@Nullable Record value) {
        if (value == null ){
            Log.w(TAG,"insertIfNoneRecords some is null");
            return;
        }
        if(TextUtils.isEmpty(value.getTraceNo())){
            Log.w(TAG,"insertIfNoneRecords getTraceNo is null");
            return;
        }

        if(value.getDataBeanList()!=null && value.getDataBeanList().size()>0){

            for(DataBean bean: value.getDataBeanList()){
                insertIfNoneDataBean(value.getTraceNo(),bean);
            }

        }

        RecordDao recordDao = Install.getInstance().getDaoSession().getRecordDao();
        List<com.zen.biz.table.Record> records  =  recordDao.queryBuilder().where(
                RecordDao.Properties.TraceNo.eq(value.getTraceNo()),
                RecordDao.Properties.UserId.eq(value.getUserId())
        ).build().list();
        if(records==null ||records.isEmpty()){
            com.zen.biz.table.Record record1 = new com.zen.biz.table.Record ();
            record1.setSync(value.getSync());

            record1.setNoteId(value.getNoteId());
            record1.setNoteName(value.getNoteName());//
            record1.setNotes(value.getNotes());//
            record1.setCategory(value.getCategory());//
            record1.setLocation(value.getLocation());//
            record1.setDeviceNumber(value.getDeviceNumber());//
            record1.setOperator(value.getOperator());//
            record1.setPotential(value.getPotential());//
            record1.setTempValue(value.getTempValue());//
            record1.setTempUnit(value.getTempUnit());//
            record1.setTraceNo(value.getTraceNo());
            record1.setValue(value.getValue());
            record1.setType(value.getType());
            record1.setPic(value.getPic());
            record1.setCreateTime(value.getCreateTime());
            record1.setTableType(value.getTabType());
            record1.setSync(value.getSync());
            record1.setSyncId(value.getSyncId());
            record1.setUserId(value.getUserId());
            record1.setUseState(true);
            record1.setCalibration(value.getCalibration());
            record1.setValueUnit(value.getValueUnit());

            long ret = recordDao.insert(record1);
            log("insert "+ret);
        }else {
            log("found  "+records.get(0).getId() +" by "+value.getTraceNo());

            com.zen.biz.table.Record record1 = records.get(0);
            if(value.getNoteName()!=null)record1.setNoteName(value.getNoteName());//
            if(value.getNotes()!=null)record1.setNotes(value.getNotes());//
            if(value.getCategory()!=null)record1.setCategory(value.getCategory());//
            if(value.getLocation()!=null)record1.setLocation(value.getLocation());//
            if(value.getDeviceNumber()!=null)record1.setDeviceNumber(value.getDeviceNumber());//
            if(value.getOperator()!=null)record1.setOperator(value.getOperator());//
            if(value.getPotential()!=null)record1.setPotential(value.getPotential());//
            if(value.getTempValue()!=null)record1.setTempValue(value.getTempValue());//
            if(value.getTempUnit()!=null)record1.setTempUnit(value.getTempUnit());//
            if(value.getTraceNo()!=null)record1.setTraceNo(value.getTraceNo());
            if(value.getPic()!=null)record1.setPic(value.getPic());
            if(value.getSyncId()!=null)record1.setSyncId(value.getSyncId());
            if(value.getCalibration()!=null)record1.setCalibration(value.getCalibration());
            if(value.getValueUnit()!=null)record1.setValueUnit(value.getValueUnit());
            recordDao.update(record1);
            log("recordDao update");

        }

    }


    @Override
    public void updateRecordsSync(@Nullable Record value) {
        if (value == null || value.getId()==null){
            log("updateRecordsSync some is null");
            return;
        }
        RecordDao recordDao = Install.getInstance().getDaoSession().getRecordDao();
        recordDao.loadByRowId(value.getId());
        com.zen.biz.table.Record record1 = recordDao.loadByRowId(value.getId());
        record1.setSync(value.getSync());
        record1.setSyncId(value.getSyncId());
        recordDao.update(record1);
        log("update " + record1.getId());
        log("zheli" + record1.getId());
    }


    private void log(String msg) {

    }

    @Override
    public List<Record> getSyncRecords(boolean sync) {
        RecordDao recordDao =  Install.getInstance().getDaoSession().getRecordDao();
        List<com.zen.biz.table.Record> records  = recordDao.queryBuilder().where(/*RecordDao.Properties.UseState.eq(true),*/RecordDao.Properties.Sync.eq(false),RecordDao.Properties.UserId.eq(getUserId())).build().list();
       // List<com.zen.biz.table.Record> records = Install.getInstance().getDaoSession().getRecordDao().loadAll();
        if (records != null && records.size() > 0) {
            log("getSyncRecords " + records.size());
            List<Record> list = new ArrayList<>();
            for (com.zen.biz.table.Record record : records) {
                Record record1 = new Record();
                record1.setId(record.getId());
                record1.setNoteId(record.getNoteId());
                record1.setNoteName(record.getNoteName());//
                record1.setNotes(record.getNotes());//
                record1.setCategory(record.getCategory());//
                record1.setTraceNo(record.getTraceNo());
                record1.setLocation(record.getLocation());//
                record1.setDeviceNumber(record.getDeviceNumber());//
                record1.setOperator(record.getOperator());//
                record1.setPotential(record.getPotential());//
                record1.setTempValue(record.getTempValue());//
                record1.setTempUnit(record.getTempUnit());
                record1.setValue(record.getValue());
                record1.setValueUnit(record.getValueUnit());
                record1.setType(record.getType());
                record1.setPic(TextUtils.isEmpty(record.getPic())?record.getLocalPic():record.getPic());
                record1.setCreateTime(record.getCreateTime());
                record1.setTabType(record.getTableType());
                record1.setSync(record.getSync());
                record1.setSyncId(record.getSyncId());
                record1.setUserId(record.getUserId());
                record1.setUseState(record.getUseState());
                record1.setDataBeanList(getDataBean(record.getTraceNo()));
                record1.setCalibration(record.getCalibration());
                list.add(record1);
            }
            return list;
        }else{
            log("getSyncRecords none" );
        }
        return null;
    }


    @Override
    public List<Record> getRecords() {
       // List<com.zen.biz.table.Record> records = Install.getInstance().getDaoSession().getRecordDao().loadAll();
        List<com.zen.biz.table.Record> records  = Install.getInstance().getDaoSession().getRecordDao()
                .queryBuilder()
                .where(RecordDao.Properties.UseState.eq(true)
                        ,RecordDao.Properties.UserId.eq(getUserId()))
                .orderDesc(RecordDao.Properties.CreateTime)
                .build().list();
        if (records != null && records.size() > 0) {
            log("getRecords " + records.size());
            List<Record> list = new ArrayList<>();
            for (com.zen.biz.table.Record record : records) {
                Record record1 = new Record();
                record1.setId(record.getId());
                record1.setNoteId(record.getNoteId());
                record1.setNoteName(record.getNoteName());//
                record1.setNotes(record.getNotes());//
                record1.setCategory(record.getCategory());//
                record1.setLocation(record.getLocation());//
                record1.setDeviceNumber(record.getDeviceNumber());//
                record1.setOperator(record.getOperator());//
                record1.setPotential(record.getPotential());//
                record1.setTempValue(record.getTempValue());//
                record1.setTempUnit(record.getTempUnit());//
                record1.setTraceNo(record.getTraceNo());
                record1.setValue(record.getValue());
                record1.setValueUnit(record.getValueUnit());
                record1.setType(record.getType());
                record1.setPic(TextUtils.isEmpty(record.getPic())?record.getLocalPic():record.getPic());
                record1.setCreateTime(record.getCreateTime());
                record1.setTabType(record.getTableType());
                record1.setSync(record.getSync());
                record1.setSyncId(record.getSyncId());
                record1.setUserId(record.getUserId());
                record1.setUseState(record.getUseState());
                record1.setDataBeanList(getDataBean(record.getTraceNo()));
                record1.setCalibration(record.getCalibration());
                list.add(record1);
            }
            return list;
        }
        return null;
    }

    @Override
    public void setLastData(Data data) {

        this.mLastData = data;
    }

    @Override
    public void saveData(int sn, String traceNo, String attach) {
        DataBeanDao dataBeanDao = Install.getInstance().getDaoSession().getDataBeanDao();
        com.zen.biz.table.DataBean dataBean = new com.zen.biz.table.DataBean();
        dataBean.setTraceNo(traceNo);
        dataBean.setUserId(getUserId());
        dataBean.setCreateTime(System.currentTimeMillis());
        dataBean.setUpdateTime(System.currentTimeMillis());
        if (mLastData != null) {
            dataBean.setSn(sn);
            dataBean.setEC(mLastData.getEC());
            dataBean.setMode(mLastData.getMode());
            dataBean.setORP(mLastData.getORP());
            dataBean.setPH(mLastData.getPH());
            dataBean.setTDS(mLastData.getTDS());
            dataBean.setTemp(mLastData.getTemp());
            dataBean.setResistivity(mLastData.getResistivity());
            dataBean.setSalinity(mLastData.getSalinity());
            dataBean.setUnitString(mLastData.getUnitString());
            dataBean.setUnit2(mLastData.getUnit2());
            dataBean.setPointDigit2(mLastData.getPointDigit2());
            dataBean.setPointDigit(mLastData.getPointDigit());
            dataBean.setValue(mLastData.getValue());
            dataBean.setValue2(mLastData.getValue2());
        }
        dataBean.setAttach(attach);
        Long i = dataBeanDao.insert(dataBean);
        log("dataBeanDao.insert " + i + " " + JSON.toJSONString(dataBean));
    }

    private String getUserId() {
        return MyApi.getInstance().getRestApi().getUserId();
    }

    @Override
    public void setLastParmUp(ParmUp parm) {
        mUpdate = true;
        this.mLastParmUp = parm;
    }

    @Override
    public void setLastVelaModeUpload(VelaParamModeDeviceToApp upload) {
        this.mLastModeUpload = upload;
    }

    @Override
    public void setLastCalibrationCond(CalibrationCond calibrationCond) {
        mUpdate |= this.mLastCalibrationCond != calibrationCond;
        this.mLastCalibrationCond = calibrationCond;


    }

    @Override
    public void setLastCalibrationPh(CalibrationPh calibrationPh) {
        mUpdate |= this.mLastCalibrationPh != calibrationPh;
        this.mLastCalibrationPh = calibrationPh;

    }

    @Override
    public boolean readUpdate() {
        boolean ret = mUpdate;
        mUpdate = false;
        return ret;
    }

    @Override
    public ParmUp getLastParm() {
        return mLastParmUp;
    }
}
