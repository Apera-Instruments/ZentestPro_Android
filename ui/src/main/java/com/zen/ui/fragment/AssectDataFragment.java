package com.zen.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.alibaba.fastjson.JSON;
import com.zen.api.Constant;
import com.zen.api.MyApi;
import com.zen.api.data.Category;
import com.zen.api.data.Record;
import com.zen.api.event.SyncEventUpload;
import com.zen.ui.CategoryActivity;
import com.zen.ui.R;
import com.zen.ui.adapter.FragmentDataSimpleAdapter;
import com.zen.ui.base.BaseFragment;
import com.zen.ui.utils.ToastUtilsX;
import com.zen.ui.utils.ZipUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class AssectDataFragment extends BaseFragment implements View.OnClickListener{
    public static final int TypeCategory = 1;

    private static final String[] COL_HEADER = {"SN", "Date/Time", "Note", "Value", "Type"};
    public static final int COLUMN_SIZE = COL_HEADER.length;
    public static final int ROW_SIZE = 100;


    private ViewGroup mTableViewGroup;
    private ListView mListView;
    private FragmentDataSimpleAdapter dataSimpleAdapter;

    ImageView mImageView;
    TextView mTextView;

    ImageButton mButtonCategory;

    ImageButton mButtonSend;

    ImageButton mButtonDel;

    ViewGroup mViewGroupCategory;
    ViewGroup mViewGroupSend;
    public TextView mTextViewCategory;
    public TextView mTextViewRight;
    public TextView mTextViewTitle;
    private String emailTitle;
    private String eMailText;
    private long categoryId;
    private int type;
    private Category category;
    private boolean mSending = false;
    private boolean mAddCategory = false;

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.ly_del || view.getId() == R.id.bt_del) {
            onDelete();
        } else if (view.getId() == R.id.ly_category || view.getId() == R.id.bt_category) {
            onCategory();
        } else if (view.getId() == R.id.ly_send || view.getId() == R.id.bt_send) {
            onSend();
        } else if (view.getId() == R.id.tv_right) {
            onRight();
        }
    }

    public void onDelete() {
        if (dataSimpleAdapter.getCount() == 0 || !dataSimpleAdapter.isSelected()) return;
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(getContext(),
                SweetAlertDialog.WARNING_TYPE)
                .setTitleText(getString(R.string.are_you_sure))
                .setContentText(getString(R.string.delete_the_record))
                .setConfirmText(getString(R.string.yes_delete))
                .setCancelText(getString(android.R.string.cancel))
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {

                        int count = 0;
                        for (int i = 0; i < dataSimpleAdapter.getCount(); i++) {
                            if (dataSimpleAdapter.getItem(i).check) {
                                MyApi.getInstance().getDataApi().setDelRecord(dataSimpleAdapter.getItem(i).sn);
                                count++;

                            }
                        }
                        if (count > 0) {
                            EventBus.getDefault().post(new SyncEventUpload());
                            loadData();
                        } else {
//                            ToastUtils.showShort(R.string.select_none);
                            ToastUtilsX.showActi(getActivity(),R.string.select_none);
                        }
                        sweetAlertDialog.dismissWithAnimation();
                    }
                });
        sweetAlertDialog.show();
    }

    public void onCategory() {
        if (dataSimpleAdapter.isSelected()) {
            mAddCategory = true;
            if (dataSimpleAdapter.getCount() == 0) {
//                ToastUtils.showShort(R.string.select_none);
                ToastUtilsX.showActi(getActivity(),R.string.select_none);
                return;
            }
            List<Long> longs = new ArrayList<>();
            for (int i = 0; i < dataSimpleAdapter.getCount(); i++) {
                if (dataSimpleAdapter.getItem(i).check) {
                    longs.add(dataSimpleAdapter.getItem(i).sn);
                }
            }
            if (longs.size() > 0) {


                CategoryActivity.showSelect(getContext(), longs);
            } else {
//                ToastUtils.showShort(R.string.select_none);
                ToastUtilsX.showActi(getActivity(),R.string.select_none);
            }

        } else {
            CategoryActivity.showMe(getContext(), 0);
        }
    }

    public void onSend() {
        if (dataSimpleAdapter.getCount() == 0 || !dataSimpleAdapter.isSelected()) return;
        File out = new File(getContext().getExternalFilesDir("share"), "" + System.currentTimeMillis() + ".zip");
        if (out.exists()) out.delete();
        List<String> fileCSVList = new ArrayList<>();
        emailTitle = "ZenTest [] " + System.currentTimeMillis();
        eMailText = "";
        StringBuilder stringBuilder = new StringBuilder();
        int selectCount = 0;
        for (int i = 0; i < dataSimpleAdapter.getCount(); i++) {
            if (!dataSimpleAdapter.getItem(i).check) {
                continue;
            }
            selectCount++;
            boolean pic = true;
            long dataId = dataSimpleAdapter.getItem(i).sn;
            Record record = MyApi.getInstance().getDataApi().getRecordById(dataId);
            Log.i(getTAG(),"record: "+JSON.toJSONString(record));
            if (record.getPic() == null) {

                pic = false;
                //continue;
            }
            stringBuilder.append(record.getId()).append(" ");
            stringBuilder.append(record.getTraceNo()).append(" ");
            stringBuilder.append(record.getNoteName()).append(" ");
            stringBuilder.append(record.getOperator()).append(" ");
            stringBuilder.append(record.getCreateTime()).append(" \r\n");
            //eMailText += record.getTraceNo();
            try {
                File file =pic? new File(record.getPic()):null;

                String cols1[] = {/*"sn",*/ "createTime", "value", "unitString", "temp", "tempUnit"};//,"deviceNumber","location","calibration"};
                List<String> strings = new ArrayList<>(Arrays.asList(cols1));
                SharedPreferences sharedPreferences = MyApi.getInstance().getDataApi().getSetting();
                boolean b = sharedPreferences.
                        getBoolean("SettingFragmentData:ser_no", false);
                if (b) {
                    strings.add(0, "sn");
                }
                b = sharedPreferences.
                        getBoolean("SettingFragmentData:gps", false);
                if (b) {
                    strings.add("location");
                }
                b = sharedPreferences.
                        getBoolean("SettingFragmentData:calibration", false);
                if (b) {
                    strings.add("calibration");
                }
                String cols[] = new String[strings.size()];
                ;
                File fileCSV = MyApi.getInstance().getDataApi().exportCSV(getContext(), record, strings.toArray(cols));
                if(file!=null) fileCSVList.add(file.getAbsolutePath());
                if (fileCSV != null) {
                    fileCSVList.add(fileCSV.getAbsolutePath());
                }else {

                }

            } catch (Exception e) {

            }
        }
        eMailText = stringBuilder.toString();

        if (fileCSVList != null && fileCSVList.size() > 0) {

            //String fileOut = out.getAbsolutePath();
            try {
                String[] strings = new String[fileCSVList.size()];
                ZipUtil.zip(out, fileCSVList.toArray(strings));
                shareFile(getContext(), out, emailTitle, eMailText);
                mSending = true;
            } catch (IOException e) {
                e.printStackTrace();
            }

            // shareFile(this, out,emailTitle,eMailText)
        } else {
            if (selectCount == 0) {
//                ToastUtils.showShort(R.string.select_none);
                ToastUtilsX.showActi(getActivity(),R.string.select_none);
            } else {
//                ToastUtils.showShort(R.string.file_none);
                ToastUtilsX.showActi(getActivity(),R.string.file_none);
            }
        }

    }

    private void shareFile(Context context, File file, String subject, String text) {
        if (null != file && file.exists()) {
            Log.i(getTAG(), "shareFile " + file.getAbsolutePath());
            Intent share = new Intent(Intent.ACTION_SEND);
            //share.addCategory(Intent.CATEGORY_APP_EMAIL)
            //share.setData(Uri.parse("mailto:a@abc.com"));
            share.putExtra(Intent.EXTRA_STREAM, getUriForFile(context, file));
            // share.setType(getMimeType(file.getAbsolutePath()));//此处可发送多种文件
            share.setType("*/*");
            share.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            // share.data = Uri.parse("mailto:abc@gmail.com");
            // share.putExtra(Intent.EXTRA_EMAIL, "abc@gmail.com");
            share.putExtra(Intent.EXTRA_SUBJECT, subject);
            share.putExtra(Intent.EXTRA_TEXT, text);
            //var intSendrt:IntentSender = IntentSender();
            context.startActivity(Intent.createChooser(share, context.getString(R.string.send_file)));
        } else {
            // ToastUtils.showToast("分享文件不存在");
            Log.i(getTAG(), "shareFile not exists " + file);
        }
    }

    public void onRight() {
        if (dataSimpleAdapter.isSelected()) {
            cancelSelect();
        } else {
            mTextViewRight.setText(R.string.cancel);
            dataSimpleAdapter.setSelected(true);
            dataSimpleAdapter.notifyDataSetChanged();
            mButtonSend.setEnabled(true);
            mButtonDel.setEnabled(true);
            mTextViewCategory.setText(R.string.add_to_category);
        }
    }

    private void cancelSelect() {
        mTextViewRight.setText(R.string.select);
        dataSimpleAdapter.setSelected(false);
        dataSimpleAdapter.notifyDataSetChanged();
        mButtonSend.setEnabled(false);
        mButtonDel.setEnabled(false);
        mTextViewCategory.setText(R.string.category);
    }

    private TextView tvLeft;
    private LinearLayout ly_del;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_assect_data, container, false);
        mTableViewGroup = view.findViewById(R.id.layoutAssect_view);
        initTableView(mTableViewGroup);

        mImageView = view.findViewById(R.id.iv_photo);
        mTextView = view.findViewById(R.id.Text_name);

        mButtonCategory = view.findViewById(R.id.bt_category);
        mButtonSend = view.findViewById(R.id.bt_send);
        mButtonDel = view.findViewById(R.id.bt_del);
        mViewGroupCategory = view.findViewById(R.id.ly_category);
        mViewGroupSend = view.findViewById(R.id.ly_send);
        mTextViewCategory = view.findViewById(R.id.tv_category);
        mTextViewRight = view.findViewById(R.id.tv_right);
        mTextViewTitle = view.findViewById(R.id.tv_title);
        ly_del = view.findViewById(R.id.ly_del);

        mButtonDel.setOnClickListener(this);
        ly_del.setOnClickListener(this);
        mViewGroupCategory.setOnClickListener(this);

        mButtonCategory.setOnClickListener(this);
        mViewGroupSend.setOnClickListener(this);

        mButtonSend.setOnClickListener(this);
        mTextViewRight.setOnClickListener(this);

        tvLeft = view.findViewById(R.id.tv_left);
        tvLeft.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });

        Bundle bundle = this.getArguments();
        mTextView.setText(bundle.getString("Textname"));
        mImageView.setImageBitmap(BitmapFactory.decodeFile(bundle.getString("Imagename")));

        mTextViewRight.setText(R.string.select);
        mButtonSend.setEnabled(false);
        mButtonDel.setEnabled(false);
        mTextViewCategory.setText(R.string.category);
        dataSimpleAdapter.setSelected(false);
        if (type == TypeCategory && categoryId >= 0) {
            category = MyApi.getInstance().getDataApi().getCategoryById(categoryId);
            if (category != null) mTextViewTitle.setText(category.getName());
            mViewGroupCategory.setVisibility(View.GONE);
            mViewGroupSend.setVisibility(View.GONE);
            dataSimpleAdapter.setEntDetail(false);
        }

        return view;
    }

    private void initTableView(ViewGroup mTableViewGroup) {
        mListView = mTableViewGroup.findViewById(R.id.asslist_view);
        dataSimpleAdapter = new FragmentDataSimpleAdapter(getContext());
        dataSimpleAdapter.setData(new LinkedList<FragmentDataSimpleAdapter.Bean>());
        dataSimpleAdapter.AssectChange(true);
        mListView.setAdapter(dataSimpleAdapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void onResume() {
        super.onResume();
        if (!dataSimpleAdapter.isSelected()) {
            loadData();
        }
        if (mSending || mAddCategory) {
            cancelSelect();
            mSending = false;
            mAddCategory = false;
        }
    }

    private void loadData() {
        //List<List<Cell>> list = new ArrayList<>();
        SimpleDateFormat formatter1 = new SimpleDateFormat(Constant.DateFormat + " \n " + Constant.Time2Format, Locale.US);
        // SimpleDateFormat formatter2 = new SimpleDateFormat("HH:mm:ss");
        List<Record> recordList = null;

        if (type == TypeCategory && categoryId >= 0 && category != null) {
            recordList = MyApi.getInstance().getDataApi().getRecordByCategory(category.getName());
        } else {
            recordList = MyApi.getInstance().getDataApi().getRecords();
        }
        dataSimpleAdapter.getData().clear();
        Bundle bundle = this.getArguments();
        String Assectname = bundle.getString("Textname");
        if (recordList != null && recordList.size() > 0) {

            for (Record record : recordList) {
                FragmentDataSimpleAdapter.Bean bean = new FragmentDataSimpleAdapter.Bean();
                bean.sn = record.getId();
                bean.dateTime = formatter1.format(new Date(record.getCreateTime()));
                bean.value = "" + record.getValue() + " " + record.getValueUnit();
                bean.note = record.getNoteName();
                bean.typeIconId = getTypeIconId(record.getTabType());
                if (Assectname.equals(record.getNoteName())){
                    dataSimpleAdapter.addData(bean, false);
                }
                Log.i(getTAG(), "record  " + JSON.toJSONString(record));
            }
        }

        dataSimpleAdapter.notifyDataSetChanged();
    }

    private int getTypeIconId(int type) {
        Log.v("111","66");
        if (0 == type)
            return R.mipmap.type_ico_simple;
        if (1 == type)
            return R.mipmap.type_ico_dial;
        if (2 == type)
            return R.mipmap.type_ico_graph;
        if (3 == type)
            return R.mipmap.type_ico_table;
        return R.mipmap.type_ico_simple;
    }

    public void setCategoryId(long catgoryId) {
        this.categoryId = catgoryId;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}


