package com.zen.api.data;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.zen.api.Constant;
import com.zen.api.protocol.CalibrationCond;
import com.zen.api.protocol.CalibrationPh;
import com.zen.api.protocol.ParmUp;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceSetting {
    public General general = new General();
    public final Calibration mCalibration=new Calibration();
    public  DataStore dataStore = new DataStore();
    public List<Alarm> alarms = new ArrayList<>();
    public List<Param>  params= new ArrayList<>();
    public  GPS GPS =new GPS();

    public Date getCalibrationDate(){
       Date date1 = mCalibration.getCalibrationCond()==null?null: mCalibration.getCalibrationCond().getDate();
       Date date2 = mCalibration.getCalibrationPh()==null?null: mCalibration.getCalibrationPh().getDate();
       if(date1==null) return date2;
       if(date2 == null) return null;
       if(date1.after(date2)){
           return date1;
       }else return date2;
    }

    public Param getParam(String name) {
        for (Param param : params) {
            if (param!=null) {
                if (param.name != null)
                    if (param.name.equals(name)) {
                        return param;
                    }
            }
        }
        Param p = new Param();
        p.name = name;
        params.add(p);
        return p;
    }

    public Alarm getAlarm(String name) {
        for (Alarm alarm : alarms) {
            if (alarm.name != null && alarm.name.equals(name))
                return alarm;
        }
        Alarm p = new Alarm();
        p.name = name;
        alarms.add(p);
        return p;
    }

    public CalibrationPh getCalibrationPh() {
        return mCalibration.getCalibrationPh();
    }

    public void setCalibrationPh(CalibrationPh calibrationPh) {
        mCalibration.setCalibrationPh(calibrationPh);
    }

    public CalibrationCond getCalibrationCond() {
        return mCalibration.getCalibrationCond();
    }

    public void setCalibrationCond(CalibrationCond calibrationCond) {
        mCalibration.setCalibrationCond(calibrationCond);
    }

    public String getCalibrationJson() {
        return JSON.toJSONString(mCalibration);
    }

    public Calibration getCalibration() {
        return mCalibration;
    }

    public static class General{
        public int tempUnit;
        public int autoHold;
        public int stableReading;
        public int calibrationReminder;
        public int backLightTime;
        public int autoPowerTime;
    }

    public static class Param{
         public String name;
         public String lowHit;
         public String highHit;
         public Map<String,Object> data = new HashMap<>();

        public int getInt(String name) {
            Object o = data.get(name);
            if (o == null) return -1;

            return (int) o;
        }
        public String getString(String name) {
            Object o = data.get(name);
            if (o == null) return null;

            return (String) o;
        }
    }

    public static class DataStore{
        public Map<String,Object> data = new HashMap<>();
    }

    public static class Alarm{
        public String name;
        public float low;
        public float high;
    }

    public static class GPS{
        public String location;
    }
}
