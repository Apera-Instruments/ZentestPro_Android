package com.zen.ui;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.blankj.utilcode.util.ToastUtils;
import com.zen.api.MyApi;
import com.zen.api.RestApi;
import com.zen.ui.base.BaseActivity;
import com.zen.ui.fragment.ChangePasswordFragment;
import com.zen.ui.fragment.GetCodeFragment;
import com.zen.ui.utils.ToastUtilsX;

public class ForgotPasswordActivity extends BaseActivity {
    public final static String ID = "id";
    public final static String DATE = "DATE";
    private GetCodeFragment mGetCodeFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        mGetCodeFragment = new GetCodeFragment();
        try {
            fragmentTransaction.replace(R.id.framelayout,mGetCodeFragment);
            fragmentTransaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static void showMe(Context content, long id) {
        if (content == null) return;
        Intent intent = new Intent(content, ForgotPasswordActivity.class);
        intent.putExtra(ID, id);

        content.startActivity(intent);

    }


    public void onSubmit() {
        Companion.getMThreadPoolUtils().execute(new Runnable() {
            @Override
            public void run() {
                int ret =MyApi.getInstance().getRestApi().verificationCode(mGetCodeFragment.getUserName(),mGetCodeFragment.getCode());
                if(ret== RestApi.SUCCESS){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                            ChangePasswordFragment changePasswordFragment = new ChangePasswordFragment();
                            changePasswordFragment.setUserName(mGetCodeFragment.getUserName());
                            changePasswordFragment.setCode(mGetCodeFragment.getCode());
                            try {
                                fragmentTransaction.replace(R.id.framelayout, changePasswordFragment);
                                fragmentTransaction.commit();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            ToastUtils.showShort(R.string.auth_code_is_incorrect);
                            ToastUtilsX.showActi(ForgotPasswordActivity.this, R.string.auth_code_is_incorrect);
                        }
                    });
                }
            }
        });

    }

    public void onBackPressed(){
       // super.onBackPressed();
        startActivity(new Intent(this,LoginActivity.class));
        finish();
    }
}
