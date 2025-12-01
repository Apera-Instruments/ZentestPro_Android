package com.zen.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.zen.ui.view.PolicyDialog;

import java.util.regex.Pattern;



public class LoginActivity extends BaseActivity implements View.OnClickListener {

    Button mButtonDemo;
    EditText mEditTextUsername;
    EditText mEditTextPassword;


    PolicyDialog policyDialog;
    private Button btnprivacy;
    private Button btnservice;

    void demo() {
         MyApi.getInstance().getRestApi().setDemoLogin(true);
         startActivity(new Intent(this,HomeActivity.class));
         finish();
    }

    void createAccount() {
        startActivity(new Intent(this,CreateAccountActivity.class));
        finish();
    }
    void forgotPassword() {
        startActivity(new Intent(this,ForgotPasswordActivity.class));
        finish();
    }

    void facebook() {

    }

    void signIn() {
        String content;

        if(TextUtils.isEmpty(mEditTextUsername.getText().toString()))
        {
//            ToastUtils.showShort(R.string.username_is_empty);
            ToastUtilsX.showActi(LoginActivity.this,R.string.username_is_empty);
            return;
        }

        content = mEditTextUsername.getText().toString();

        boolean isMatch = Pattern.matches(RegexConstants.REGEX_EMAIL, content);

        if (!isMatch) {
//            ToastUtils.showShort(R.string.username_is_not_email);
            ToastUtilsX.showActi(LoginActivity.this,R.string.username_is_not_email);
            return;
        }

        if (TextUtils.isEmpty(mEditTextPassword.getText().toString()) || mEditTextPassword.getText().toString().contains(" ")) {
//            ToastUtils.showShort(R.string.password_is_empty_or_space);
            ToastUtilsX.showActi(LoginActivity.this,R.string.password_is_empty_or_space);
            return;
        }

        Companion.getMThreadPoolUtils().execute(new Runnable() {
            @Override
            public void run() {
                try {
                   int ret = MyApi.getInstance().getRestApi().login(mEditTextUsername.getText().toString(), mEditTextPassword.getText().toString());

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

                       startActivity(new Intent(LoginActivity.this,HomeActivity.class));
                       finish();
                       return;
                   }
                } catch (Exception e) {
                    e.printStackTrace();
                }
//                ToastUtils.showShort( MyApi.getInstance().getRestApi().getLastErrMessage());
                Looper.prepare();
                ToastUtilsX.showActi(LoginActivity.this,R.string.Login_failed);
                Looper.loop();
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
    Button bt_sign_in,bt_demo;
    ImageView iv_facebook;
    TextView tv_forgot_password,tv_create_account;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SharedPreferences sharedPreferences = getSharedPreferences("policy-agreement",MODE_PRIVATE);
        int i = sharedPreferences.getInt("Version",0);
        mButtonDemo = findViewById(R.id.bt_demo);
        mEditTextUsername = findViewById(R.id.username);
        mEditTextPassword = findViewById(R.id.password);
        bt_sign_in = findViewById(R.id.bt_sign_in);

        btnprivacy = findViewById(R.id.policy_btnprivacy);
        btnservice =  findViewById(R.id.policy_btnservice);
        bt_sign_in.setOnClickListener(this);
        iv_facebook = findViewById(R.id.iv_facebook);
        iv_facebook.setOnClickListener(this);
        tv_create_account = findViewById(R.id.tv_create_account);
        tv_create_account.setOnClickListener(this);
        mButtonDemo.setOnClickListener(this);



        tv_forgot_password = findViewById(R.id.tv_forgot_password);
        tv_forgot_password.setOnClickListener(this);


        btnprivacy.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this,PrivacyAndServiceActivity.class);
                startActivity(intent);
            }
        });

        btnservice.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, AgreeMentActivity.class);
                startActivity(intent);
            }
        });
        if (i==1){
        }else {

            Bundle bundle = new Bundle();
//            bundle.putBoolean(PolicyDialog.DIALOG_BACK,true);
            bundle.putBoolean(PolicyDialog.DIALOG_CANCELABLE_TOUCH_OUT_SIDE,false);

            policyDialog = PolicyDialog.newInstance(PolicyDialog.class, bundle);
            policyDialog.show(getFragmentManager(), PolicyDialog.class.getName());
            policyDialog.setOnConfirmClcik(new PolicyDialog.IonConfirmClick() {
                @Override
                public void onClcik(int type) {
                    if (type==1){
                        //同意协议
                        SharedPreferences.Editor editor = getSharedPreferences("policy-agreement",MODE_PRIVATE).edit();
                        editor.putInt("Version",1);
                        editor.apply();
                        policyDialog.dismiss();
                    }
                    if (type==2){
                        //不同意协议
                        finish();
                    }
                    if (type==3){
                        //隐私政策
                        Intent intent = new Intent(LoginActivity.this,PrivacyAndServiceActivity.class);
                        startActivity(intent);
                    }
                    if (type==4){
                        //服务协议
                        Intent intent = new Intent(LoginActivity.this, AgreeMentActivity.class);
                        startActivity(intent);
                    }
                }
                @Override
                public void onClickStr(String string) {
                }
            });
        }

        mEditTextUsername.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(v instanceof  EditText)
                if(hasFocus ) {
                    ((EditText)v).setHint("");
                }else{
                    ((EditText)v).setHint(R.string.user_name);
                }
            }
        });
        mEditTextPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(v instanceof  EditText)
                    if(hasFocus ) {
                        ((EditText)v).setHint("");
                    }else{
                        ((EditText)v).setHint(R.string.password);
                    }
            }
        });
        mEditTextUsername.setText(MyApi.getInstance().getRestApi().getUserName());

    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (keyCode==KeyEvent.KEYCODE_BACK){
            return true;
        }
        return false;
    }
    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.bt_sign_in) {
            signIn();
        } else if (id == R.id.iv_facebook) {
            facebook();
        } else if (id == R.id.tv_forgot_password) {
            forgotPassword();
        } else if (id == R.id.tv_create_account) {
            createAccount();
        } else if (id == R.id.bt_demo) {
            demo();
        }
    }
}
