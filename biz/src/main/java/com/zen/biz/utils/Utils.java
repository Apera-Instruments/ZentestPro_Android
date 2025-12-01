package com.zen.biz.utils;

import com.alibaba.fastjson.JSON;
import com.zen.api.data.BleDevice;

public class Utils {
   public static BleDevice parse(String value){
       try {
           return JSON.parseObject(value, BleDevice.class);
       }catch (Exception e){
           e.printStackTrace();
       }
       return null;
   }
}
