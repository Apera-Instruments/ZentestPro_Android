package com.zen.api;

import java.util.ArrayList;
import java.util.List;

public class SettingConfig {

    public final static String ph_selection_SAVE_ID = "param:pH:ph_selection";
    public final static String ph_resolution_SAVE_ID = "param:pH:ph_resolution";
    public final static String ReferenceTemperature_SAVE_ID = "param:COND:ReferenceTemperature";
    public final static String TemperatureCoefficient_SAVE_ID = "param:COND:TemperatureCoefficient";
    public final static String PH_DueCalibration_SAVE_ID = "param:pH:DueCalibration";
    public final static String COND_DueCalibration_SAVE_ID = "param:COND:DueCalibration";
    public final static String COND_FactoryReset_SAVE_ID = "param:COND:FactoryReset";
    public final static String PH_FactoryReset_SAVE_ID = "param:pH:FactoryReset";
    public final static String TDSFactor_SAVE_ID = "param:TDS:TDSFactor";
    public final static String SaltType_SAVE_ID = "param:Salinity:SaltType";
    public final static String salt_unit_SAVE_ID = "param:Salinity:salt_unit";
    public final static String autoPowerOff_SAVE_ID = "param:autoPowerOff";

    public final static String autoHold_SAVE_ID = "general:autoHold";

    public final static String backLight_SAVE_ID = "general:backLight";
    public final static String tempUnit_SAVE_ID = "general:tempUnit";
    public final static String gps_SAVE_ID = "general:gps";

    public final static String VALUE_TYPE = "VALUE_TYPE";
    public final static String LIST_VALUE_TYPE = "LIST_VALUE_TYPE";
    public final static String MORE_TYPE = "MORE_TYPE";
    public final static String MULTI_SWITCH_TYPE = "MULTI_SWITCH_TYPE";
    public final static String SWITCH_TYPE = "SWITCH_TYPE";
    public final static String RESET_FACTORY_TYPE = "RESET_FACTORY_TYPE";
    public final static SettingConfig pH =SettingConfig.build("pH");
    public final static SettingConfig COND =SettingConfig.build("COND");
    public final static SettingConfig TDS =SettingConfig.build("TDS");
    public final static SettingConfig Salinity =SettingConfig.build("Salinity");


    public SettingConfig(){

    }
    private List<Config> configList = new ArrayList<>();

    public static SettingConfig build(String param){
        SettingConfig settingConfig= new SettingConfig();
        Config config;
        switch (param){
            case "pH":
                config = new Config();
                config.type = MULTI_SWITCH_TYPE;
                config.title = "Buffer Class Selection";
                config.titleId = 0;
                config.defaultValue = "USA";
                config.mSwitchTabsResId = R.array.ph_selection;
                config.saveId =ph_selection_SAVE_ID;
                settingConfig.configList.add(config);
                config = new Config();
                config.type = MULTI_SWITCH_TYPE;
                config.title = "Resolution";
                config.titleId = 0;
                config.defaultValue = "0.1";
                config.mSwitchTabsResId = R.array.ph_resolution;
                config.saveId =ph_resolution_SAVE_ID;
                settingConfig.configList.add(config);
                config = new Config();
                config.type = MORE_TYPE;
                config.title = "Due Calibration";
                config.titleId = 0;
                config.defaultValue = "OFF";
                config.mSwitchTabsResId = R.array.hole_time_array;
                config.saveId =PH_DueCalibration_SAVE_ID;
                settingConfig.configList.add(config);
                config = new Config();
                config.type = RESET_FACTORY_TYPE;
                config.title = "Reset to Factory Default";
                config.titleId = 0;
                config.saveId =PH_FactoryReset_SAVE_ID;
                settingConfig.configList.add(config);
                break;
            case "COND":
                config = new Config();
                config.type = LIST_VALUE_TYPE;
                config.title = "Reference Temperature";
                config.titleId = 0;
                config.mSwitchTabsResId = R.array.reference_temp;
                config.saveId =ReferenceTemperature_SAVE_ID;
                config.defaultValue = "25";
                settingConfig.configList.add(config);
                config = new Config();
                config.type = LIST_VALUE_TYPE;
                config.title = "Temperature Coefficient";
                config.titleId = 0;
                config.mSwitchTabsResId = R.array.temp_coe;
                config.saveId =TemperatureCoefficient_SAVE_ID;
                config.defaultValue = "2.00%";
                settingConfig.configList.add(config);
                config = new Config();
                config.type = MORE_TYPE;
                config.title = "Due Calibration";
                config.titleId = 0;
                config.mSwitchTabsResId = R.array.hole_time_array;
                config.saveId =COND_DueCalibration_SAVE_ID;
                config.defaultValue = "OFF";
                settingConfig.configList.add(config);
                config = new Config();
                config.type = RESET_FACTORY_TYPE;
                config.title = "Reset to Factory Default";
                config.titleId = 0;
                config.saveId =COND_FactoryReset_SAVE_ID;
                settingConfig.configList.add(config);
                break;
            case "TDS":
                config = new Config();
                config.type =LIST_VALUE_TYPE;// VALUE_TYPE;
                config.title = "TDS Factor";
                config.titleId = 0;
                config.defaultValue = "0.71";
                config.mSwitchTabsResId = R.array.tds_factor;
                config.saveId =TDSFactor_SAVE_ID;
                settingConfig.configList.add(config);
                break;
            case "Salinity":
       /*         config = new Config();
                config.type = VALUE_TYPE;
                config.title = "Salt Type";
                config.titleId = 0;
                config.defaultValue = "0.71";
                config.mSwitchTabsResId = R.array.hole_time_array;
                config.saveId =SaltType_SAVE_ID;
                settingConfig.configList.add(config);*/
                config = new Config();
                config.type = MULTI_SWITCH_TYPE;
                config.title = "Salt Unit";
                config.titleId = 0;
                config.defaultValue = "ppt";
                config.saveId =salt_unit_SAVE_ID;
                config.mSwitchTabsResId = R.array.salt_unit;
                settingConfig.configList.add(config);
                break;
        }
        return settingConfig;
    }

    public List<Config> getList() {
        return configList;
    }

    public static class  Config{
        public String type;
        public String title;
        public int titleId;
        public String value;
        public String defaultValue;
        public String defaultUnit;
        public int mSwitchTabsResId;
        public String saveId;
    }
}
