package com.zen.zentest;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogService extends Service {
    private static final String TAG = "LogService";
    private static final boolean DEBUG = true;
    private static final String DES_KEY = "12345678";
    private Thread mThread;
    private final boolean EncryptDes=false;


    public LogService() {
    }

    public void onCreate() {
        super.onCreate();
        //先不用
//        startNotificationForegroud();
        try {
            if (DEBUG) {
                startLog();
            }
        } catch (Exception e) {
            Log.w("Exception",e.getLocalizedMessage(),e);
        }
    }

    //先不用
//    private void startNotificationForegroud(){
//        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
//            String CHANNEL_ID = "NFCService";
//            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "主服务", NotificationManager.IMPORTANCE_HIGH);
//
//            channel.enableLights(true);
//            channel.setLightColor(Color.RED);
//            channel.setShowBadge(true);
//            channel.setDescription("bottombar Notofication");
//            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
//            if (manager!=null){
//                manager.createNotificationChannel(channel);
//            }
//
//            Notification notification = new Notification.Builder(this).setChannelId(CHANNEL_ID).setAutoCancel(false).setContentTitle("主服务").setContentText("运行中。。。")
//            .setWhen(System.currentTimeMillis()).setSmallIcon(R.mipmap.ic_launcher).setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher)).build();
//            startForeground(1,notification);
//        }
//    }

    public void onDestroy() {
        try {
            stopLog();
        } catch (Exception e) {
            Log.w("Exception",e.getLocalizedMessage(),e);
        }
        super.onDestroy();

    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void stopLog() {
        if (mThread != null) {
            mThread.interrupt();
        }
        mThread = null;
    }

    private void startLog() {
        mThread = new Thread() {
            public void run() {
                try {
                    SharedPreferences sharedPreferences = getSharedPreferences("policy-agreement",MODE_PRIVATE);
                    int i = sharedPreferences.getInt("Version",0);

                    if(i==1){
                    }else {
                        return;
                    }
                    Log.i(TAG, "start log thread...");
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
                    String timestamp = formatter.format(new Date());
                    File file = new File(getExternalFilesDir("logs")  , "log_"+timestamp+".txt");
                    FileWriter fileWriter;
                    fileWriter = new FileWriter(file, file.length() < 5 * 1024 * 1024);
                    fileWriter.append("\r\n");
                    Process p = Runtime.getRuntime().exec("logcat -v threadtime");
                    Log.i(TAG, "start logcat...");
                    BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String line;
                    Log.i(TAG, "start file writer...");

                    if (EncryptDes) {
                        while ((line = in.readLine()) != null) {
                            line+="\r\n";
                            byte[] r=encryptDes(line.getBytes(),DES_KEY);
                            if (r != null) {
                                String ret = okio.ByteString.of(r).base64();
                                fileWriter.write(ret);
                                fileWriter.write("\r\n");
                            }
                            if (interrupted()) break;
                        }
                    } else {
                        while ((line = in.readLine()) != null) {
                            fileWriter.write(line);
                            fileWriter.append("\r\n");
                            if (interrupted()) break;
                        }
                    }

                    fileWriter.flush();
                    fileWriter.close();
                } catch (Exception e) {
                    Log.w("Exception",e.getLocalizedMessage(),e);
                }
                Log.i(TAG, "log thread exit.");
            }
        };
        mThread.start();
    }

    public static byte[] encryptDes(byte[] source, String key) {
        try {
            SecureRandom random = new SecureRandom();
            DESKeySpec desKey = new DESKeySpec(key.getBytes());
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey securekey = keyFactory.generateSecret(desKey);
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.ENCRYPT_MODE, securekey, random);
            byte ret[] = cipher.doFinal(source);

            return ret;
        } catch (Throwable e) {
            Log.w("Exception", e.getLocalizedMessage(), e);
        }
        return null;
    }

    /**
     * @param source byte[]
     * @param key    String
     * @return byte[]
     * @throws Exception
     */
    public static byte[] decryptDes(byte[] source, String key) throws Exception {
        SecureRandom random = new SecureRandom();
        DESKeySpec desKey = new DESKeySpec(key.getBytes());
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey securekey = keyFactory.generateSecret(desKey);
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(Cipher.DECRYPT_MODE, securekey, random);

        byte ret[] = cipher.doFinal(source);

        return ret;
    }
}
