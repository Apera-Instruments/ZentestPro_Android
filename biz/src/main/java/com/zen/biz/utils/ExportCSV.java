package com.zen.biz.utils;

import android.database.Cursor;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zen.api.MyApi;
import com.zen.api.data.BleDevice;
import com.zen.api.data.Calibration;
import com.zen.api.data.DataBean;
import com.zen.api.data.Record;
import com.zen.api.protocol.Data;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ExportCSV {
    public static void exportToCSV(Cursor c, String fileName) {

        int rowCount = 0;
        int colCount = 0;
        FileWriter fw;
        BufferedWriter bfw;
        File sdCardDir = Environment.getExternalStorageDirectory();
        File saveFile = new File(sdCardDir, fileName);
        try {

            rowCount = c.getCount();
            colCount = c.getColumnCount();
            fw = new FileWriter(saveFile);
            bfw = new BufferedWriter(fw);
            if (rowCount > 0) {
                c.moveToFirst();
                // 写入表头
                for (int i = 0; i < colCount; i++) {
                    if (i != colCount - 1)
                        bfw.write(c.getColumnName(i) + ',');
                    else
                        bfw.write(c.getColumnName(i));
                }
                // 写好表头后换行
                bfw.newLine();
                // 写入数据
                for (int i = 0; i < rowCount; i++) {
                    c.moveToPosition(i);
                    // Toast.makeText(mContext, "正在导出第"+(i+1)+"条",
                    // Toast.LENGTH_SHORT).show();
                    Log.v("ExportCSV", "Go " + (i + 1) + "");
                    for (int j = 0; j < colCount; j++) {
                        String text = c.getString(j);
                        if(TextUtils.isEmpty(text)){
                            text = " ";
                        }
                        if (j != colCount - 1)
                            bfw.write(text + ',');
                        else
                            bfw.write(text);
                    }
                    // 写好每条记录后换行
                    bfw.newLine();
                }
            }
            // 将缓存数据写入文件
            bfw.flush();
            // 释放缓存
            bfw.close();
            // Toast.makeText(mContext, "导出完毕！", Toast.LENGTH_SHORT).show();
            Log.v("ExportCSV", "Success！");
        } catch (IOException e) {

            e.printStackTrace();
        } finally {
            c.close();
        }
    }
    protected static String getDataString(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy , hh:mm:ss a", Locale.US);
        return simpleDateFormat.format(date);

    }

    public static File exportToCSV(Record c, File saveFile,String cols[]) {
        Log.v("ExportCSV","exportToCSV start");
        if(cols==null) return null;
        int rowCount = 0;
        int colCount = cols.length;
        FileWriter fw;
        BufferedWriter bfw;
        //File sdCardDir = Environment.getExternalStorageDirectory();
        // File saveFile = fileName;//new File( fileName);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(saveFile);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream,"utf-8");//iso-8859-1 GB2312 unicode
            rowCount = c.getDataBeanList().size();
            // colCount = 10;//c.getColumnCount();
            // fw = new FileWriter(saveFile);
            bfw = new BufferedWriter(outputStreamWriter);
            byte [] bs = { (byte)0xEF, (byte)0xBB, (byte)0xBF};
            fileOutputStream.write(bs,0,bs.length);
            DataBean dataBeans =   c.getDataBeanList().get(0);
            String MVTEMPER = JSON.toJSONString(dataBeans);
            JSONObject objects = JSON.parseObject(MVTEMPER);

            String s2 = objects.getString("attach");
            Log.v("ExportCSV",MVTEMPER);
            JSONObject attachs = !TextUtils.isEmpty(s2) ? JSON.parseObject(s2) : null;
          ;
            String s3 = attachs.getString("calibration");
            JSONObject attach3 = !TextUtils.isEmpty(s3) ? JSON.parseObject(s3) : null;
            String Measuring = null;
            String info=null;
            String calibration=null;

            if (objects.getString("unitString").equals("µS")) Measuring = "µS/mS";
            else if (objects.getString("unitString").equals("mS")) Measuring = "µS/mS";
            else if (objects.getString("unitString").equals("ppt")) Measuring = "ppt/ppm";
            else if (objects.getString("unitString").equals("ppm")) Measuring = "ppt/ppm";
            else if (objects.getString("unitString").equals("Ω")) Measuring = "Ω/KΩ/MΩ";
            else if (objects.getString("unitString").equals("KΩ")) Measuring = "Ω/KΩ/MΩ";
            else if (objects.getString("unitString").equals("MΩ")) Measuring = "Ω/KΩ/MΩ";
            else Measuring = objects.getString("unitString");
            Log.v("ExportCSV",Measuring);
            if (rowCount > 0) {

                // 写入表头
                for (int i = 0; i < colCount; i++) {
                    String out ;
                    out = (cols[i]);
                    if("unitString".equals(out)) {
                        out = "";
                        continue;
                    }
                    else if("sn".equals(out)) out = "Serial Number";
                    else if("location".equals(out)) {
                        out = "GPS Location";
                        continue;
                    }
                    else if("calibration".equals(out)) {
                        out = "Calibration Information";
                        continue;
                    }
                    else if("value".equals(out)) out = "Measuring Values-"+Measuring;
                    else if("temp".equals(out)) out = "Temperature-"+attachs.getString("tempUnit");
                    else if("tempUnit".equals(out)) {
                        out = "";
                        continue;
                    }
                    else if("createTime".equals(out)) out = "Date , Time";
                    if (i != colCount - 1)
                        out = (out + ',');


                    bfw.write(out);
                }
                // 写好表头后换行
                bfw.newLine();
                // 写入数据
                for (int i = 0; i < rowCount; i++) {
                    DataBean dataBean =   c.getDataBeanList().get(i);
                    String json = JSON.toJSONString(dataBean);
                    JSONObject object = JSON.parseObject(json);
                    String s1 = object.getString("attach");
                    JSONObject attach = !TextUtils.isEmpty(s1) ? JSON.parseObject(s1) : null;
                    // Toast.makeText(mContext, "正在导出第"+(i+1)+"条",
                    // Toast.LENGTH_SHORT).show();
                    Log.v("ExportCSV", "Go " + (i + 1) + " "+json);
                    for (int j = 0; j < colCount; j++) {

                        if ("tempUnit".equals(cols[j]) || "unitString".equals(cols[j])) {
                            continue;
                        }
                        String out ;
                        out =  object.getString(cols[j]);
                        if ("sn".equals(cols[j])) {
                            if (!TextUtils.isEmpty(out)) {
                                int sn = Integer.parseInt(out);
                                sn += 1;
                                out = String.valueOf(sn);
                            }
                        }

                        if (out == null && attach != null) {
                            out = attach.getString(cols[j]);
                        }

                        if ("calibration".equals(cols[j])) {
                            calibration = out;
                            continue;
                        }


                        if ("location".equals(cols[j])) {
                            info = out;
                            continue;
                        }
//                        if("tempUnit".equals(cols[j]) && TextUtils.isEmpty(out)){
//                            int unit2 = object.containsKey("unit2") && object.get("unit2")!=null?object.getInteger("unit2"):Data.UNIT_C;
//                            if (unit2 == Data.UNIT_C) {
//                                out = "C";
//                            } else {
//                                out = "F";
//                            }
//                        }

                        if(out==null) out="";
                        if("createTime".equals(cols[j]) && !TextUtils.isEmpty(out)){
                            out =   getDataString(new Date(Long.parseLong(out)));
                        }

                        if(TextUtils.isEmpty(out)){
                            out = " ";
                        }else if(out.contains(",")&& !"createTime".equals(cols[j]))
                        {
                            out = out.replace(","," ");
                        }
                        // && (!"value".equals(cols[j])) && (!"temp".equals(cols[j]))  原来时单位
                        if (j != colCount - 1 )
                            out =  out + ',';// bfw.write();
                        // else
                        //    out = object.getString(cols[j]);//  bfw.write();

                        bfw.write(out);
                        Log.v("------ExportCSV",out);
                    }
                    // 写好每条记录后换行
                    bfw.newLine();
                }
                bfw.newLine();
                bfw.write("Calibrated Points,Offset,Slope,Last Calibration,Reference Temp. ,Temp. Compensation Coefficient,Admin,Asset,Memo");
                bfw.newLine();
                if(calibration==null){
                    try {
                        BleDevice dev = MyApi.getInstance().getBtApi().getLastDevice();
                        if (dev != null) {
                            //改过
                            //calibration = dev.getSetting().getCalibrationPh().toString();
                            calibration = attachs.getString("calibration");
                            Log.v("ExportCSV", "getSetting calibration=" + calibration);
                        } else {
                            Log.v("ExportCSV", "BleDevice nul");
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                if(calibration!=null) {
                    Log.v("ExportCSV","calibration="+calibration);
                    //Calibrated Points	Offset	Slope	Last Calibration	Reference Temp.	Temp. Compensation Coefficient
                    //CalibrationPh pointNum:0 cailIndex:4 offset1:-3.1 slope1:100.0 offset2:0.0 slope2:100.0
                    bfw.write(formatCal(calibration, c));
                    bfw.newLine();
                }
                bfw.newLine();
                if(info!=null) {
                    Log.v("ExportCSV","gps info="+info);
                    bfw.write("GPS");
                    bfw.newLine();
                    bfw.write(formatGPS(info));
                    bfw.newLine();
                }
                bfw.newLine();
            }
            // 将缓存数据写入文件
            bfw.flush();
            // 释放缓存
            bfw.close();
            // Toast.makeText(mContext, "导出完毕！", Toast.LENGTH_SHORT).show();
            Log.i("ExportCSV", "Compilete！");
        } catch (IOException e) {
            Log.w("ExportCSV", "IOException！",e);

        } finally {

        }

        return  saveFile;
    }

    private static String formatCal(String calibration, Record c) {

        if(calibration.startsWith("{") && calibration.endsWith("}")){
            //Calibrated Points,Offset,Slope,Last Calibration,Reference Temp. ,Temp. Compensation Coefficient
            Calibration jsonObject =  JSON.parseObject(calibration,Calibration.class);
            JSONObject attach3 = !TextUtils.isEmpty(calibration) ? JSON.parseObject(calibration) : null;
            if(jsonObject==null) return "";

            String out ="";
            if(jsonObject.getCalibrationPh()!=null) {
                if (jsonObject.getCalibrationPh().is168()) {
                    out += "1.68";
                }
                if (jsonObject.getCalibrationPh().is400()) {
                    out += (TextUtils.isEmpty(out)?"":"|")+"4.00";
                }
                if (jsonObject.getCalibrationPh().is700()) {
                    out += (TextUtils.isEmpty(out)?"":"|")+"7.00";
                }
                if (jsonObject.getCalibrationPh().is1001()) {
                    out += (TextUtils.isEmpty(out)?"":"|")+"10.01";
                }
                if (jsonObject.getCalibrationPh().is1245()) {
                    out +=(TextUtils.isEmpty(out)?"":"|")+"12.45";
                }

            }
            if(jsonObject.getCalibrationCond()!=null) {
                if (jsonObject.getCalibrationCond().is84()) {
                    out += (TextUtils.isEmpty(out)?"":"|")+"84";
                }
                if (jsonObject.getCalibrationCond().is1288()) {
                    out += (TextUtils.isEmpty(out)?"":"|")+"1288";
                }
                if (jsonObject.getCalibrationCond().is1413()) {
                    out += (TextUtils.isEmpty(out)?"":"|")+"1413";
                }

            }
            out+=",";
            if(jsonObject.getCalibrationPh()!=null) {
                if (attach3.getString("mode").equals("COND")){
                    out+= "  ";
                }else {
                    out += getFormatDouble(jsonObject.getCalibrationPh().getOffset1(), 2) + "mV";
                }
            }
            out+=",";
            if(jsonObject.getCalibrationPh()!=null) {
                Log.v("ExportCSV",attach3.getString("mode"));
                if (attach3.getString("mode").equals("COND")){
                    out+= "  ";
                }else {
                    out += getFormatDouble(jsonObject.getCalibrationPh().getSlope2(), 2) + "%";
                }
            }
            out+=",";

            if(jsonObject.getCalibrationPh()!=null) {
                out += formatDate(jsonObject.getCalibrationPh().getDate())+" ";

            }
            else if(jsonObject.getCalibrationCond()!=null) {
                out += formatDate(jsonObject.getCalibrationCond().getDate())+" ";

            }
            out+=",";
            //Reference Temp. ,Temp. Compensation Coefficient
            out+=jsonObject.getRefTemp()+",";
            out += TextUtils.isEmpty(jsonObject.getTempCompensate()) ? "  ," : jsonObject.getTempCompensate() + ",";
            out += TextUtils.isEmpty(c.getOperator()) ? "  ," : c.getOperator() + ",";
            out += TextUtils.isEmpty(c.getNoteName()) ? "  ," : c.getNoteName() + ",";
            out += TextUtils.isEmpty(c.getNotes()) ? "  " : c.getNotes();
            Log.d("ExportCSV","calibration format : "+out);
            return out;
        }
        Log.v("ExportCSV","12345");
        return calibration;
    }

    private static String formatDate(Date data) {
        if(data==null) return "";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd a hh:mm:ss", Locale.US);
        return simpleDateFormat.format(data);
    }

    private static String formatGPS(String info) {
        return "["+info.replace(",","-")+"]";
    }
    private static String getFormatDouble(double d, int n){
        DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();
        df.setMaximumFractionDigits(n);
        df.setMinimumFractionDigits(n);
        return df.format(d);
    }
}