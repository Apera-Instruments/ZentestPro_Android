package com.zen.ui;
import com.orhanobut.logger.Logger;
import com.zen.api.event.UpdateEvent;
import com.zen.ui.fragment.AssectDataFragment;
import com.zen.ui.base.BaseActivity;
import com.zen.ui.fragment.DataFragment;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class AssectDataActivity extends BaseActivity {
    public final static  String ID ="ID";



    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpdateEvent event) {

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assect_datafragment);

        Intent intent = getIntent();
        Bundle bundle = new Bundle();
        bundle.putString("Textname",intent.getStringExtra("Textname"));
        bundle.putString("Imagename",intent.getStringExtra("Imagename"));

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        AssectDataFragment fragment = new AssectDataFragment();
        long id = getIntent().getLongExtra(ID,-1);

        fragment.setArguments(bundle);

        fragment.setCategoryId(id);
        fragment.setType(DataFragment.TypeCategory);
        fragmentTransaction.replace(R.id.framelayoutAssect, fragment);
        fragmentTransaction.commit();
    }

    public void onResume() {
        super.onResume();
        updateView();
    }



    public static void showMe(Context content, long id) {
        if (content == null) {
            return;
        }
        Intent intent = new Intent(content, AssectDataActivity.class);
        intent.putExtra(ID, id);

        content.startActivity(intent);

    }
}
