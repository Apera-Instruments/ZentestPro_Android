package com.zen.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.alibaba.fastjson.JSON;
import com.blankj.utilcode.constant.RegexConstants;
import com.foolchen.lib.tracker.Tracker;
import com.zen.api.MyApi;
import com.zen.api.RestApi;
import com.zen.api.data.BleDevice;
import com.zen.ui.base.BaseActivity;
import com.zen.ui.utils.ToastUtilsX;
import com.zen.ui.view.AgreeMentActivity;

import java.util.regex.Pattern;

public class CreateAccountActivity extends BaseActivity {
    public final static String ID = "id";
    public final static String DATE = "DATE";
    private Button btnprivacy;
    private Button btnservice;
    EditText mEditTextEmail;
    EditText mEditTextPassword;

    private TextView tvLeft;


    EditText mEditTextFirstName;
    EditText mEditTextLastName;
    Button mBtGoNext;

    void goNext() {
        if(TextUtils.isEmpty(mEditTextEmail.getText().toString()) || !mEditTextEmail.getText().toString().contains("@")){
//            ToastUtils.showShort(R.string.account_should_be_mail_address);
            ToastUtilsX.showActi(CreateAccountActivity.this , R.string.account_should_be_mail_address);
            return;
        }

        String content = mEditTextEmail.getText().toString();

        boolean isMatch = Pattern.matches(RegexConstants.REGEX_EMAIL, content);

        if (!isMatch) {
//            ToastUtils.showShort(R.string.account_should_be_mail_address);
            ToastUtilsX.showActi(CreateAccountActivity.this , R.string.account_should_be_mail_address);
            return;
        }
        String passwd =        mEditTextPassword.getText().toString();
        if(TextUtils.isEmpty(passwd) || passwd.contains(" ") || passwd.length()<6 || passwd.length()>12){
//            ToastUtils.showShort(R.string.password_is_incorrect);
            ToastUtilsX.showActi(CreateAccountActivity.this ,R.string.password_is_incorrect);
            return;
        }

        Companion.getMThreadPoolUtils().execute(new Runnable() {
            @Override
            public void run() {
              int ret =   MyApi.getInstance().getRestApi().register(
                      mEditTextEmail.getText().toString(),
                      mEditTextPassword.getText().toString(),
                      mEditTextFirstName.getText().toString(),
                      mEditTextLastName.getText().toString()
              );
              if(ret == RestApi.SUCCESS){
                  runOnUiThread(new Runnable() {
                      @Override
                      public void run() {
//                          ToastUtils.showShort(R.string.register_success);
                          ToastUtilsX.showActi(CreateAccountActivity.this ,R.string.register_success);
//                          startActivity(new Intent(CreateAccountActivity.this,LoginActivity.class));
//                          finish();
                          Companion.getMThreadPoolUtils().execute(new Runnable() {
                              @Override
                              public void run() {
                                  try {
                                      int ret = MyApi.getInstance().getRestApi().login(mEditTextEmail.getText().toString(), mEditTextPassword.getText().toString());

                                      if(ret == RestApi.SUCCESS){

                                          SharedPreferences sharedPreferences = MyApi.getInstance().getDataApi().getSetting();
                                          String userId = MyApi.getInstance().getRestApi().getUserId();
                                          Tracker.INSTANCE.login(userId);
                                          //  String dev1 = sharedPreferences.getString("connectedDev","");
                                          String connectedDev = sharedPreferences.getString("connectedDev_"+userId,"");

                                          if (!TextUtils.isEmpty(connectedDev) && connectedDev.startsWith("{")) {
                                              BleDevice bleDevice = parse(connectedDev);
                                              MyApi.getInstance().getBtApi().connect(bleDevice);
                                          } else {

                                          }

                                          startActivity(new Intent(CreateAccountActivity.this,HomeActivity.class));
                                          finish();
                                          return;
                                      }
                                  } catch (Exception e) {
                                      e.printStackTrace();
                                  }
//                ToastUtils.showShort( MyApi.getInstance().getRestApi().getLastErrMessage());
                                  Looper.prepare();
                                  ToastUtilsX.showActi(CreateAccountActivity.this,R.string.Login_failed);
                                  Looper.loop();
                              }
                          });
                      }
                  });
              }else {
                  runOnUiThread(new Runnable() {
                      @Override
                      public void run() {
//                          ToastUtils.showShort(""+MyApi.getInstance().getRestApi().getLastErrMessage());
                          ToastUtilsX.showActi(CreateAccountActivity.this,R.string.User_exists);
                      }
                  });
              }

            }
        });

    }

    private static BleDevice parse(String value){
        try {
            return JSON.parseObject(value, BleDevice.class);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        mEditTextEmail =  findViewById(R.id.et_email);
        mEditTextPassword =  findViewById(R.id.et_password);

        mEditTextFirstName =  findViewById(R.id.et_first_name);
        mEditTextLastName =  findViewById(R.id.et_last_name);
        mBtGoNext = findViewById(R.id.bt_go_next);
        mBtGoNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goNext();
            }
        });

        tvLeft =  findViewById(R.id.tv_left);
        tvLeft.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


        btnprivacy = findViewById(R.id.create_btnprivacy);
        btnservice =  findViewById(R.id.create_btnservice);

        btnprivacy.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CreateAccountActivity.this,PrivacyAndServiceActivity.class);
                startActivity(intent);
            }
        });

        btnservice.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CreateAccountActivity.this, AgreeMentActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public static void showMe(Context content, long id) {
        if (content == null) return;
        Intent intent = new Intent(content, CreateAccountActivity.class);
        intent.putExtra(ID, id);

        content.startActivity(intent);
    }

    public void onBackPressed(){
        // super.onBackPressed();
        startActivity(new Intent(this,LoginActivity.class));
        finish();
    }
}
