package com.zen.ui;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.orhanobut.logger.Logger;
import com.zen.api.MyApi;
import com.zen.api.data.Category;
import com.zen.api.data.Record;
import com.zen.api.event.UpdateEvent;
import com.zen.ui.adapter.CategorySimpleAdapter;
import com.zen.ui.base.BaseActivity;
import com.zen.ui.fragment.DataFragment;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class CategoryDataActivity extends BaseActivity {
    public final static  String ID ="ID";


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpdateEvent event) {

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_data);



        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        DataFragment fragment = new DataFragment();
        long id = getIntent().getLongExtra(ID,-1);
        fragment.setCategoryId(id);
        fragment.setType(DataFragment.TypeCategory);
        fragmentTransaction.replace(R.id.framelayout, fragment);
        fragmentTransaction.commit();
    }

    public void onResume() {
        super.onResume();
        updateView();
    }


    public static void showMe(Context content, long id) {
        if (content == null) return;
        Intent intent = new Intent(content, CategoryDataActivity.class);
        intent.putExtra(ID, id);

        content.startActivity(intent);

    }


}
