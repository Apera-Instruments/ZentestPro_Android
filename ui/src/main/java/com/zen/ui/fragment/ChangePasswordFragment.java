package com.zen.ui.fragment;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.zen.api.MyApi;
import com.zen.api.RestApi;
import com.zen.ui.LoginActivity;
import com.zen.ui.R;
import com.zen.ui.base.BaseFragment;
import com.zen.ui.utils.ToastUtilsX;



/**
 * Created by louis on 17-12-21.
 */
public class ChangePasswordFragment extends BaseFragment   {

    EditText mEditTextPassword;
    EditText mEditTextReenterPassword;
    Button btGoNext;
    private String mUserName;
    private String mCode;

    private TextView tvLeft;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_change_password, container, false);
        tvLeft = view.findViewById(R.id.tv_left);

        mEditTextPassword = view.findViewById(R.id.et_password);
        mEditTextReenterPassword = view.findViewById(R.id.et_reenter_pw);
        btGoNext = view.findViewById(R.id.bt_go_next);
        btGoNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSubmit();
            }
        });

        tvLeft.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });
        return view;
    }

    public ChangePasswordFragment(){

    }

    public void setUserName(String userName) {
        this.mUserName = userName;
    }

    public void setCode(String code) {
        this.mCode = code;
    }

    public void onSubmit() {

        if(TextUtils.isEmpty(mEditTextPassword.getText().toString())){
//            ToastUtils.showShort(getString(R.string.password_is_incorrect));
            ToastUtilsX.showActi(getActivity(),R.string.password_is_incorrect);
            return;
        }
        if(TextUtils.isEmpty(mEditTextReenterPassword.getText().toString())){
//            ToastUtils.showShort(getString(R.string.password_is_incorrect));
            ToastUtilsX.showActi(getActivity(),R.string.password_is_incorrect);
            return;
        }
        if(!(mEditTextReenterPassword.getText().toString().equals(mEditTextPassword.getText().toString()))){
//            ToastUtils.showShort(getString(R.string.password_is_not_match));
            ToastUtilsX.showActi(getActivity(),R.string.password_is_not_match);
            return;
        }

        Companion.getMThreadPoolUtils().execute(new Runnable() {
            String oldPassword=null ,userName,newPassword,code;
            int type=1;
            @Override
            public void run() {
                newPassword  = mEditTextReenterPassword.getText().toString();
                userName = mUserName;
                code = mCode;
                int ret = MyApi.getInstance().getRestApi().changePassword(userName,oldPassword,newPassword,code,type);
                if (ret == RestApi.SUCCESS) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            ToastUtils.showShort(R.string.change_password_success);
                            ToastUtilsX.showActi(getActivity(),R.string.change_password_success);
                            getActivity().finish();
                            startActivity(new Intent(getContext(),LoginActivity.class));
                        }
                    });
                } else {
//                    ToastUtils.showShort(R.string.change_password_fail);
                    ToastUtilsX.showActi(getActivity(),R.string.change_password_fail);
                }
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        updateView();
     //   API.getResourceApi().update(ResourceApi.HOT_VIEW, ResourceApi.APP_LIST);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    public void updateView() {
        super.updateView();


    }


}