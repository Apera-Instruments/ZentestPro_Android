package com.zen.biz.rest;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Log;
import com.alibaba.fastjson.JSONObject;
import okhttp3.*;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;

import javax.net.ssl.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by louis on 17-4-28.
 */
public class RestBase {
    public static final String SALT = "salt";
    protected static final String TAG = "RestUtil";
    protected static final String KEY = "123456781234567812345678";
    protected final boolean DEBUG = true;
    protected OkHttpClient client;
    protected Map<String, Object> mDataMap = new ConcurrentHashMap<>();
    protected Map<String, String> headers;
    protected Cache mCache = null;
    private byte[] mMaskKey = null;
    private byte[] mSalt = new byte[16];

    protected void setDefault() {
        SharedPreferences sharedPreferences = MyRestApplication.getInstance().getSharedPreferences();
        sharedPreferences.edit().clear().apply();
        mDataMap.clear();
    }

    protected Cache getCache() {

        Cache cache = null;
        try {
            //改过
//            File cacheDirectory = new File("http_cache");
//            if (!cacheDirectory.exists()) {
//                cacheDirectory.mkdir();
//            }
//            File file1 = new File(cacheDirectory, "download_cache");
//            if (!file1.exists()) {
//                file1.mkdir();
//            }
//
//            cache = new Cache(new File(cacheDirectory, "okhttp_cache"), 10 * 1024 * 1024);
            //client.setCache(cache);
        } catch (Exception e) {
            Log.w("Exception", e.getLocalizedMessage(), e);
            cache = null;
        }
        return cache;
    }

    protected void initHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.readTimeout(9, TimeUnit.SECONDS);
        builder.dns(HTTP_DNS);
        builder.addInterceptor(new LoggingInterceptor());
        builder.addNetworkInterceptor(new NetworkLoggingInterceptor());
        if (mCache != null) {
            builder.cache(mCache);
        }

        setClientTrustAllCerts(builder);
        builder.cookieJar(new CookiesManager());
        client = builder.build();

    }

    public Map<String, Object> getMap() {
        return mDataMap;
    }


    private class CookiesManager implements CookieJar {
        private final Map<String, List<Cookie>> cookieStore = new HashMap<>();
        private final int MAXSIZE = 20;

        @Override
        public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {

            if (DEBUG) Log.i(TAG, "Cookie saveFromResponse " + url);
            if (cookies != null) {
                if (cookieStore.size() > MAXSIZE) {
                    cookieStore.clear();
                }
                cookieStore.put(url.host(), cookies);
            }
        }

        @Override
        public List<Cookie> loadForRequest(HttpUrl url) {
            if (DEBUG) Log.v(TAG, "Cookie loadForRequest " + url.toString().hashCode());
            List<Cookie> cookies = cookieStore.get(url.host());
            return cookies == null ? new ArrayList<Cookie>() : cookies;
        }

    }

    private void setClientTrustAllCerts(OkHttpClient.Builder builder) {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[]{};
                }

                public void checkClientTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {
                }
            }};


            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            builder
                    .sslSocketFactory(sc.getSocketFactory(), (X509TrustManager) trustAllCerts[0])
                    .hostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    });

        } catch (Exception e) {
            Log.w("Exception", e.getLocalizedMessage(), e);

        }

    }

    private OkHttpClient httpDnsClient;
    private Dns HTTP_DNS = new Dns() {
        @Override
        public List<InetAddress> lookup(String hostname) throws UnknownHostException {

            if (hostname == null) throw new UnknownHostException("hostname == null");
            if (httpDnsClient == null) return Dns.SYSTEM.lookup(hostname);

            HttpUrl httpUrl = new HttpUrl.Builder().scheme("http")
                    .host("119.29.29.29")
                    .addPathSegment("d")
                    .addQueryParameter("dn", hostname)
                    .build();
            Request dnsRequest = new Request.Builder().url(httpUrl).get().build();
            try {
                String s = httpDnsClient.newCall(dnsRequest).execute().body().string();

                if (DEBUG) Log.i(TAG, hostname + ":" + s);
                if (s.contains(";")) {
                    String s1[] = s.split(";");
                    List<InetAddress> list = new ArrayList<>();
                    for (String s2 : s1) {
                        if (s2.matches("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b")) {
                            list.add(InetAddress.getByName(s2));
                        }
                    }
                    if (list.size() > 0) return list;
                }
                if (!s.matches("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b")) {
                    return Dns.SYSTEM.lookup(hostname);
                }


                return Arrays.asList(InetAddress.getAllByName(s));
            } catch (IOException e) {
                Log.i(TAG, "HTTP_DNS fail " + e.getMessage());
                return Dns.SYSTEM.lookup(hostname);
            }
        }
    };


    private class LoggingInterceptor implements Interceptor {
        @Override
        public Response intercept(Interceptor.Chain chain) throws IOException {

            Request request = chain.request();
            Log.i("NetworkInterceptor","" + request.url());
 /*           Request.Builder builder=request.newBuilder();
            builder.addHeader("access_id",Constants.JILINTONG_ACCESS_ID);
            request = builder.build();*/
            long t1 = System.nanoTime();
            String id = Integer.toHexString(request.url().hashCode());
            if (DEBUG) {
                Log.i("Interceptor", String.format(request.method() + " request %s(%s) on %s%n%s",
                        request.url(), id, chain.connection(), request.headers()));


            }

            Response response = chain.proceed(request);
            if (response != null) {
                long t2 = System.nanoTime();
                if (DEBUG) {
                    String stringBuilder = String.format("Received response for %s in %.1fms%n%s",
                            id, (t2 - t1) / 1e6d, response.headers()) +
                            "\nResponse  response:          " + response +
                            "\nResponse  cache response:    " + response.cacheResponse() +
                            "\nResponse  network response:  " + response.networkResponse();
                    Log.i("Interceptor", stringBuilder);


                }
            }
            return response;
        }
    }

    private class NetworkLoggingInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            long t1 = System.nanoTime();

            Log.i("NetworkInterceptor","" + request.url());
            String id = Integer.toHexString(request.url().hashCode());
            if (DEBUG) {
                Log.i("NetworkInterceptor", String.format("Sending request %s on %s%n%s",
                        id, chain.connection(), request.headers()));
                if (!"GET".equals(request.method())) {
                    try {
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        BufferedSink sink = Okio.buffer(Okio.sink(byteArrayOutputStream));
                        request.body().writeTo(sink);
                        sink.flush();
                        Log.i("NetworkInterceptor", id + " / " + byteArrayOutputStream.toString());
                    } catch (Exception e) {

                    }
                }
            }

            Response response = chain.proceed(request);
            if (response != null) {
                long t2 = System.nanoTime();
                if (DEBUG) {
                    Log.i("NetworkInterceptor", String.format("Received response for %s in %.1fms%n%s",
                            id, (t2 - t1) / 1e6d, response.headers()));
                /*    if (response.code() >= 500 && response.body().contentLength() > 0) {
                        Log.i("NetworkInterceptor", "response code:"+response.code() +"\n"+response.body().string());
                    }*/
                }
                // Log.i("NetworkInterceptor","Response  response:          " + response);
                //Log.i("Response  cache response:    " + response.cacheResponse());
                //Log.i("Response  network response:  " + response.networkResponse());

            }
            return response;
        }
    }

    class ResponseException extends IOException {
        ResponseException() {
            super();
        }

        ResponseException(String msg) {
            super(msg);
        }
    }

    ;

    private retrofit2.Response<JSONObject> callExecute(retrofit2.Call<JSONObject> call) throws IOException {
        retrofit2.Response<JSONObject> response = call.execute();
        if (response.isSuccessful()) {
            int code = response.body().getIntValue("code");
            //{"res_code":"10503","res_desc":"用户验证请求数据失败"}
            if (code == 0) {
                return response;
            } /*else if ("10503".equals(res_code)) {
                boolean login = login();
                if (login) {
                    return null;
                } else {
                    throw new ResponseException(res_code);
                }
            } */ else {
                throw new ResponseException(String.valueOf(code));
            }

        } else {
            throw new IOException(String.valueOf(response.code()));
        }
        //return null;
    }


    public byte[] getImage(String url) {
        Log.i(TAG, "getImage " + url);

        CacheControl cacheControl = new CacheControl.Builder()
                .maxAge(3, TimeUnit.MINUTES)//default maxAge =  (server Date:  - Last-Modified)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .cacheControl(cacheControl)
                .addHeader("User-Agent", "Android Mobile")
                .get()
                .build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                byte[] result = response.body().bytes();

                response.body().close();
                return result;
            } else {
                Log.w(TAG, "get response fail " + response.code());
                // response.body().close();
                //throw new IOException("Unexpected code " + response);
            }
        } catch (IOException e) {
            Log.w("IOException", e.getLocalizedMessage(), e);

        } catch (Exception e) {
            Log.w("Exception", e.getLocalizedMessage(), e);
        } finally {
            if (response != null && response.body() != null) {
                response.body().close();
            }
        }
        return null;
    }

    public String httpget(String url) {
        Log.i(TAG, "get " + url);
        CacheControl cacheControl = new CacheControl.Builder()
                .maxAge(3, TimeUnit.MINUTES)//default maxAge =  (server Date:  - Last-Modified)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .cacheControl(cacheControl)
                .addHeader("User-Agent", "Android Mobile")
                .get()
                .build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String result = response.body().string();
                // if (BuildConfig.DEBUG) com.application.Log.i(TAG, response.headers().toString());
                Log.i(TAG, "get response length = " + result.length());
                if (result.length() < 100) {
                    Log.i(TAG, "get response  = " + result);
                }
                response.body().close();
                return result;
            } else {
                Log.w(TAG, "get response fail " + response.code());
                response.body().close();
                //throw new IOException("Unexpected code " + response);
            }
        } catch (Exception e) {
            Log.w("Exception", e.getLocalizedMessage(), e);
        }
        return null;
    }

    public static String ReadSysVer() {
        return android.os.Build.MODEL + ","
                + android.os.Build.VERSION.SDK + ","
                + android.os.Build.VERSION.RELEASE;
    }

    //版本名
    public static String getVersionName(Context context) {
        return getPackageInfo(context).versionName;
    }

    //版本号
    public static int getVersionCode(Context context) {
        return getPackageInfo(context).versionCode;
    }

    private static PackageInfo getPackageInfo(Context context) {
        PackageInfo pi = null;

        try {
            PackageManager pm = context.getPackageManager();
            pi = pm.getPackageInfo(context.getPackageName(),
                    PackageManager.GET_CONFIGURATIONS | PackageManager.GET_SIGNATURES);

            return pi;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return pi;
    }

    public static String getAppVersion() {
        try {
            Context context = MyRestApplication.getInstance().getApplicationContext();
            context.getPackageName();
            PackageInfo packageInfo = getPackageInfo(context);
            StringBuilder builder = new StringBuilder();
            try {
                Signature[] signatures = packageInfo.signatures;
                /******* 循环遍历签名数组拼接应用签名 *******/
                for (Signature signature : signatures) {
                    builder.append(signature.toCharsString());

                }
                /************** 得到应用签名 **************/
            } catch (Exception e) {

            }
            String signature = okio.ByteString.decodeBase64(builder.toString()).sha1().hex();

            // packageInfo.signatures[0];
            return packageInfo.packageName + "_" + packageInfo.versionName + "_" + packageInfo.versionCode + "_" + signature;
        } catch (Exception e) {
            return "";
        }
    }

    public static String getAppPackageInfoVersion() {
        try {
            Context context =MyRestApplication.getInstance().getApplicationContext(); //Application.getInstance().getLauncherApplicationAgent().getApplicationContext();
            context.getPackageName();
            PackageInfo packageInfo = getPackageInfo(context);

            // packageInfo.signatures[0];
            return packageInfo.versionName;
        } catch (Exception e) {
            return "";
        }
    }

    public static String getAppPackageName() {
        try {
            Context context = MyRestApplication.getInstance().getApplicationContext();//MyApplication.getInstance().getLauncherApplicationAgent().getApplicationContext();
            context.getPackageName();
            PackageInfo packageInfo = getPackageInfo(context);

            // packageInfo.signatures[0];
            return packageInfo.packageName;
        } catch (Exception e) {
            return "";
        }
    }

    protected String sign(Object data) {
        //字典序排序
        HashMap<String, String> map = new HashMap<String, String>();
        Field[] fields = data.getClass().getFields();
        for (Field field : fields) {
            try {
                if ("sign".equals(field.getName())) continue;
                Object obj = field.get(data);
                String value;
                if (obj instanceof String) {
                    value = (String) obj;
                } else {
                    value = String.valueOf(field.get(data));
                }
                //Log.i(TAG,value);
                //value = value.replaceAll("\\n","");
                map.put(field.getName(), value);
                //Log.i(TAG,value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        Collection<String> keyset = map.keySet();

        List list = new ArrayList<String>(keyset);

        Collections.sort(list);
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                stringBuilder.append("&");
            }

            stringBuilder.append(list.get(i) + "=" + map.get(list.get(i)));
        }
        String out = String.format(stringBuilder.toString());
     /*   if(LogUtil.isEnable()){
            Log.i(TAG,out);
        }*/
        return okio.ByteString.encodeUtf8(out).sha256().hex();

    }
}
