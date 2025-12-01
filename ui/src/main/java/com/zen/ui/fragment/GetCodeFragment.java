package com.zen.ui.fragment;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.zen.api.MyApi;
import com.zen.api.RestApi;
import com.zen.ui.ForgotPasswordActivity;
import com.zen.ui.R;
import com.zen.ui.base.BaseFragment;
import com.zen.ui.utils.ToastUtilsX;


/**
 * Created by louis on 17-12-21.
 */
public class GetCodeFragment extends BaseFragment {

    Button mButtonGetCode;

    EditText mEditTextCode;
    EditText mEditTextEmail;
    private String userName;
    private String code;

    Button bt_go_next;

    TextView tvLeft;


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forgot_password, container, false);

        mButtonGetCode = view.findViewById(R.id.bt_get_code);
        mEditTextCode = view.findViewById(R.id.et_code);
        mEditTextEmail = view.findViewById(R.id.et_email);
        tvLeft = view.findViewById(R.id.tv_left);
        bt_go_next = view.findViewById(R.id.bt_go_next);

        mEditTextEmail.setText(MyApi.getInstance().getRestApi().getUserName());
        tvLeft.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        mButtonGetCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onGetCode();
            }
        });

        bt_go_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSubmit();
            }
        });

        return view;
    }

    public void onGetCode() {
        if (TextUtils.isEmpty(mEditTextEmail.getText().toString()) || !mEditTextEmail.getText().toString().contains("@")) {
//            ToastUtils.showShort(getString(R.string.account_should_be_mail_address));
            ToastUtilsX.showActi(getActivity(), R.string.account_should_be_mail_address);
            return;
        }

        Companion.getMThreadPoolUtils().execute(new Runnable() {
            @Override
            public void run() {
                int ret = MyApi.getInstance().getRestApi().acquireCode(mEditTextEmail.getText().toString());
                if (ret == RestApi.SUCCESS) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            ToastUtils.showShort(R.string.acquire_code_success);
                            ToastUtilsX.showActi(getActivity(), R.string.acquire_code_success);
                            mButtonGetCode.setEnabled(false);
                        }
                    });
                } else {
                    // //账号不存在！
                    if (RestApi.ACCOUNT_NOT_FOUND_FAIL == ret) {
//                        ToastUtils.showShort(R.string.account_not_found_fail);
                        ToastUtilsX.showActi(getActivity(), R.string.account_not_found_fail);

                    } else {
//                        ToastUtils.showShort(R.string.acquire_code_fail);
                        ToastUtilsX.showActi(getActivity(), R.string.acquire_code_fail);
                    }
                }
            }
        });

    }

    public void onSubmit() {
        userName = mEditTextEmail.getText().toString();
        code = mEditTextCode.getText().toString();
        if (getActivity() instanceof ForgotPasswordActivity) {
            ((ForgotPasswordActivity) getActivity()).onSubmit();
        } else {
            Log.w(getTAG(), "onSubmit getActivity()  is not match ForgotPasswordActivity");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateView();
        //   API.getResourceApi().update(ResourceApi.HOT_VIEW, ResourceApi.APP_LIST);
    }

    public void updateView() {
        super.updateView();


    }


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCode() {
        return code;
    }
}