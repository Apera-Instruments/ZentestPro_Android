package com.zen.ui.view;

import android.os.Build;
import androidx.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.zen.ui.R;

public class PolicyDialog extends BaseDialogFragment{
    private Button btnprivacy;
    private Button btnservice;
    private Button btndisagree;
    private Button btnagree;

    @Override
    protected void initView(View view) {
        btnprivacy = (Button) view.findViewById(R.id.policy_btnprivacy);
        btnservice = (Button) view.findViewById(R.id.policy_btnservice);
        btndisagree = (Button) view.findViewById(R.id.policy_btndisagree);
        btnagree = (Button) view.findViewById(R.id.policy_btnagree);

        btnagree.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                ionConfirmClick.onClcik(1);
                ionConfirmClick.onClickStr("123");
            }
        });

        btndisagree.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                ionConfirmClick.onClcik(2);
                ionConfirmClick.onClickStr("234");
            }
        });

        btnprivacy.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                ionConfirmClick.onClcik(3);
                ionConfirmClick.onClickStr("345");
            }
        });

        btnservice.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                ionConfirmClick.onClcik(4);
                ionConfirmClick.onClickStr("456");
            }
        });

    }

    @Override
    protected int getLayoutResId() {
        return R.layout.policy_dialog;
    }

    @Override
    protected void setSubView() {

    }

    @Override
    protected void initEvent() {

    }

    @Override
    protected void onCancel() {

    }
    private IonConfirmClick ionConfirmClick;

    public void setOnConfirmClcik(IonConfirmClick mOnConfirmClick){
        ionConfirmClick=  mOnConfirmClick;
    }

    public interface IonConfirmClick{
        void onClcik(int type);//0 取消  1 确认
        void onClickStr(String string);
    }
}
