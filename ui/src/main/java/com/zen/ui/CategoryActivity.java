package com.zen.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.zen.ui.utils.ToastUtilsX;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
import java.util.List;

public class CategoryActivity extends BaseActivity {
    public final static String ID = "id";
    public final static String IDs = "ids";
    public final static String TYPE = "Type";
    public final static int TYPE_SELECT = 1;
    public final static String DATE = "DATE";

    ListView mListView;

    private CategorySimpleAdapter mCategorySimpleAdapter;
    private long dataId;
    private int type;
    private long[] ids;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpdateEvent event) {

    }

    ImageButton mButtonAdd;

    ImageButton mButtonDel;

    public void onAdd() {
        if (mCategorySimpleAdapter.isSelected()) return;
/*        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.CUSTOM_IMAGE_TYPE)
               .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        MyApi.getInstance().getDataApi().addCategory();
                        updateView();
                        sweetAlertDialog.dismissWithAnimation();
                    }
                });

        sweetAlertDialog.setContentView(R.layout.dialog_input);
        sweetAlertDialog.show();*/

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.input);
        final EditText editText = new EditText(this);
        builder.setView(editText);
        //builder.setIcon(android.R.drawable.btn_star);
        builder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (TextUtils.isEmpty(editText.getText().toString())) {
                    //修改屏蔽
//                    ToastUtils.showShort(R.string.empty_str);

                    dialog.dismiss();
                    return;
                }
                long ret = MyApi.getInstance().getDataApi().addCategory(new Category(editText.getText().toString()));


                if (ret >= 0) {
                    updateView();
                } else {
//                    ToastUtils.showShort(R.string.exists);
                    ToastUtilsX.showActi(CategoryActivity.this, R.string.exists);

                }
                dialog.dismiss();
            }
        });
        builder.show();
    }


    TextView mTextViewLeft;
    TextView mTextViewRight;
    TextView mTextViewLeft1;

    void onRight() {
        if (!mCategorySimpleAdapter.isSelected()) {
            mCategorySimpleAdapter.setSelected(true);
            mCategorySimpleAdapter.notifyDataSetChanged();
            mTextViewRight.setText(R.string.cancel);
            mTextViewLeft.setText(R.string.select_all);
            mButtonAdd.setEnabled(false);
            mButtonDel.setEnabled(true);
            mTextViewLeft1.setVisibility(View.GONE);
        } else {
            mCategorySimpleAdapter.setSelected(false);
            mCategorySimpleAdapter.notifyDataSetChanged();
            mTextViewRight.setText(R.string.select);
            mTextViewLeft.setText("");
            mButtonAdd.setEnabled(true);
            mButtonDel.setEnabled(false);
            mTextViewLeft1.setVisibility(View.VISIBLE);
        }
    }

    void onLeft() {

        if (mCategorySimpleAdapter.isSelected()) {
            mCategorySimpleAdapter.setSelectedAll(true);
            mCategorySimpleAdapter.notifyDataSetChanged();

        }
    }

    void back() {
        this.finish();
    }

    public void onDelete() {
        if (mCategorySimpleAdapter.isSelected()) {
            List<Category> categoryList = mCategorySimpleAdapter.getData();
            if (categoryList != null && !categoryList.isEmpty()) {
                for (Category category : categoryList) {
                    if (category.isChecked()) {
                        MyApi.getInstance().getDataApi().delCategory(category.getId());
                    }
                }
            }
            //mCategorySimpleAdapter.setSelected(false);
            updateView();
        }

        //  MyApi.getInstance().getDataApi().delCategory();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        mButtonAdd = findViewById(R.id.bt_add);
        mButtonDel = findViewById(R.id.bt_del);

        mTextViewLeft = findViewById(R.id.tv_left);
        mTextViewRight = findViewById(R.id.tv_right);
        mTextViewLeft1 = findViewById(R.id.tv_left1);

        mListView = findViewById(R.id.list_view);
        mButtonDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onDelete();
            }
        });

        mButtonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onAdd();
            }
        });

        mTextViewRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRight();
            }
        });

        mTextViewLeft1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                back();
            }
        });

        mTextViewLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onLeft();
            }
        });


        mButtonAdd.setEnabled(true);
        mButtonDel.setEnabled(false);
        dataId = getIntent().getLongExtra(ID, -1);

        ids = getIntent().getLongArrayExtra(IDs);
        type = getIntent().getIntExtra(TYPE, -1);
        mCategorySimpleAdapter = new CategorySimpleAdapter(this);
        mCategorySimpleAdapter.setType(type);
        mCategorySimpleAdapter.setIds(ids);
        if (type == TYPE_SELECT) {
            mCategorySimpleAdapter.setSelectCallback(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            });
        }
        mListView.setAdapter(mCategorySimpleAdapter);

    }

    public void onResume() {
        super.onResume();
        updateView();
    }

    //刷新界面
    public void updateView() {
        List<Category> list = MyApi.getInstance().getDataApi().getCategory();
        if (list != null && list.size() > 0) {
            for (Category category : list) {
                List<Record> list1 = MyApi.getInstance().getDataApi().getRecordByCategory(category.getName());
                ;
                category.setNum(list1 == null ? 0 : list1.size());
            }
        }

        mCategorySimpleAdapter.setData(list);
        mCategorySimpleAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        mCategorySimpleAdapter.setSelectCallback(null);

        super.onDestroy();
    }


    public static void showMe(Context content, long id) {
        if (content == null) return;
        Intent intent = new Intent(content, CategoryActivity.class);
        intent.putExtra(ID, id);

        content.startActivity(intent);

    }

    public static void showSelect(Context content, List<Long> longs) {
        if (content == null || longs == null) return;
        Intent intent = new Intent(content, CategoryActivity.class);
        //intent.putExtra(ID, id);
        long[] longs1 = new long[longs.size()];
        for (int i = 0; i < longs.size(); i++) {
            longs1[i] = longs.get(i);
        }
        intent.putExtra(IDs, longs1);
        intent.putExtra(TYPE, TYPE_SELECT);
        content.startActivity(intent);

    }

}
