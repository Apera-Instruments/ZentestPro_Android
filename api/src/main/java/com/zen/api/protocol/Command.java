package com.zen.api.protocol;

import com.zen.api.protocol.velaprotocal.VelaDataInfo;
import com.zen.api.protocol.velaprotocal.VelaParamSettingUpload;

public enum Command {
    VELADATA(VelaDataInfo.CODE, VelaDataInfo.class),
    VELAPARAMSETTINGINFO(VelaParamSettingUpload.CODE, VelaParamSettingUpload.class),
    DATA(Data.CODE,  Data.class),
    ERR(Error.CODE,Error.class),
    CALIBRATION_PH(CalibrationPh.CODE,  CalibrationPh.class),
    CALIBRATION_COND(CalibrationCond.CODE,  CalibrationCond.class),
    PARM_UP(ParmUp.CODE,  ParmUp.class),
    SHUTDOWN(Shutdown.CODE,  Shutdown.class),
    //ALARM(Alarm.CODE,  Alarm.class),
    MEASURE(Measure.CODE,  Measure.class),
    MODE(Mode.CODE,  Mode.class),
    PRESS_KEY(Key.CODE,  Key.class),
    PARM_DOWN(ParmDown.CODE,  ParmDown.class),
    VERSION(Version.CODE,  Version.class),
    SYNC_TIME(Time.CODE,Time.class),
    ;
    private  Class<Convent> convent;
    private Callback callback;
    private int code=0;
    Command(int code, Class convent) {
     this.code = code;
     this.convent = convent;
    }


    public static Object unpack(byte[] data) {
        for (Command command : Command.values()) {
            if (command.code == data[0]) {
                try {
                    Convent convent = command.convent.newInstance();
                    Object object = convent.unpack(data);
                    if (command.callback != null) {
                        command.callback.unpacked(object);
                    }
                    return object;
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public void register(Callback callback) {
        this.callback = callback;
    }


    public interface Callback{
        void unpacked(Object object);
    }

}
