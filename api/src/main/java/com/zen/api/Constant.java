package com.zen.api;

public interface Constant {
    int MODE_PH=1;
    int MODE_SAL=2;
    int MODE_ORP=3;
    int MODE_TDS=4;
    int MODE_COND=5;
    int MODE_RES=6;
    int MODE_VELA_PH=0;
    int MODE_VELA_MV=1;
    int MODE_VELA_COND=2;
    int MODE_VELA_TDS=3;
    int MODE_VELA_SALT=4;
    int MODE_VELA_RES=5;
    int MODE_VELA_ORP=6;

    String STR_UNIT_PH="pH";
    String STR_UNIT_SAL="ppt";
    String STR_UNIT_ORP="mV";
    String STR_UNIT_TDS="ppm";
    String STR_UNIT_COND="µS/cm";
    String STR_UNIT_RES="MΩ·cm";
    String STR_MODE_PH="PH";
    String STR_MODE_pH="pH";
    String STR_MODE_SAL="SAL";
    String STR_MODE_ORP="ORP";
    String STR_MODE_TDS="TDS";
    String STR_MODE_COND="COND";
    String STR_MODE_RES="RES";
    String ALARM_PH = "ALARM_PH";
    String ALARM_ORP = "ALARM_ORP";
    String ALARM_COND = "ALARM_COND";
    String ALARM_SALINITY = "ALARM_SALINITY";
    String ALARM_TDS = "ALARM_TDS";
    String ALARM_RESISTIVITY = "ALARM_RESISTIVITY";
    String ON = "ON";
    String OFF = "OFF";
    String lowSwitchValue="lowSwitchValue";
    String highSwitchValue="highSwitchValue";
    String highValue="highValue";
    String lowValue="lowValue";
    String highValueUnit="highValueUnit";
    String lowValueUnit="lowValueUnit";
    String ALARMs[] ={Constant.ALARM_PH,
            Constant.ALARM_ORP,
            Constant.ALARM_COND,
            Constant.ALARM_SALINITY,
            Constant.ALARM_TDS,
            Constant.ALARM_RESISTIVITY};
    String DEFAULT_ALARMs[]={
            "{\"data\":{\"alarmTipsId\":\"alarm_ph_tips\",\"rangTipsId\":\"alarm_ph_rang_tips\",\"lowValue\":\"-2\",\"lowSwitchValue\":\"OFF\",\"highValue\":\"16\",\"highSwitchValue\":\"OFF\" ,\"highValueUnit\":\"pH\",\"lowValueUnit\":\"pH\"  },\"name\":\"ALARM_PH\",\"lowHit\":\"-2 - 16 pH\",\"highHit\":\"-2 - 16 pH\"} ",
            "{\"data\":{\"alarmTipsId\":\"alarm_orp_tips\",\"rangTipsId\":\"alarm_orp_rang_tips\",\"lowValue\":\"-1000\",\"lowSwitchValue\":\"OFF\",\"highValue\":\"1000\",\"highSwitchValue\":\"OFF\",\"highValueUnit\":\"mV\",\"lowValueUnit\":\"mV\"},\"name\":\"ALARM_ORP\",\"lowHit\":\"-1000mV\",\"highHit\":\"1000mV\"} ",
            "{\"data\":{\"alarmTipsId\":\"alarm_cond_tips\",\"rangTipsId\":\"alarm_cond_rang_tips\",\"lowValue\":\"0\",\"lowSwitchValue\":\"OFF\",\"highValue\":\"20\",\"highSwitchValue\":\"OFF\",\"highValueUnit\":\"mS\",\"lowValueUnit\":\"mS\"},\"name\":\"ALARM_COND\",\"lowHit\":\"0-20mS\",\"highHit\":\"0-20mS\"} ",
            "{\"data\":{\"alarmTipsId\":\"alarm_salinity_tips\",\"rangTipsId\":\"alarm_salinity_rang_tips\",\"lowValue\":\"0\",\"lowSwitchValue\":\"OFF\",\"highValue\":\"10\",\"highSwitchValue\":\"OFF\",\"highValueUnit\":\"ppt\",\"lowValueUnit\":\"ppt\"},\"name\":\"ALARM_SALINITY\",\"lowHit\":\"0-10ppt\",\"highHit\":\"0-10ppt\"} ",
            "{\"data\":{\"alarmTipsId\":\"alarm_tds_tips\",\"rangTipsId\":\"alarm_tds_rang_tips\",\"lowValue\":\"0\",\"lowSwitchValue\":\"OFF\",\"highValue\":\"10\",\"highSwitchValue\":\"OFF\",\"highValueUnit\":\"ppt\",\"lowValueUnit\":\"ppt\"},\"name\":\"ALARM_TDS\",\"lowHit\":\"0-10ppt\",\"highHit\":\"0-10ppt\"} ",
            "{\"data\":{\"alarmTipsId\":\"alarm_res_tips\",\"rangTipsId\":\"alarm_res_rang_tips\",\"lowValue\":\"50\",\"lowSwitchValue\":\"OFF\",\"highValue\":\"20000000\",\"highSwitchValue\":\"OFF\",\"highValueUnit\":\"MΩ\",\"lowValueUnit\":\"MΩ\"},\"name\":\"ALARM_RESISTIVITY\",\"lowHit\":\"0Ω-20MΩ\",\"highHit\":\"0Ω-20MΩ\"} ",
    };

    String rangTips="rangTips";
    String rangTipsId="rangTipsId";
    String alarmTipsId="alarmTipsId";
    String DateTimeFormat = "MM/dd/yyyy hh:mm a";
    String TimeFormat = "hh:mm a";
    String Time2Format = "hh:mm:ss a";
    String DateFormat = "MM/dd/yyyy";
}
