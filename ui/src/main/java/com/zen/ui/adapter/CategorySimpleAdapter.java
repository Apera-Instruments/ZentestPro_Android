package com.zen.ui.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.orhanobut.logger.Logger;
import com.zen.api.DataApi;
import com.zen.api.MyApi;
import com.zen.api.SettingConfig;
import com.zen.api.data.Category;
import com.zen.api.data.Record;
import com.zen.api.event.FactoryEvent;
import com.zen.ui.CategoryActivity;
import com.zen.ui.CategoryDataActivity;
import com.zen.ui.R;
import com.zen.ui.Setting2Activity;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;
import lib.kingja.switchbutton.SwitchMultiButton;

public class CategorySimpleAdapter extends BaseAdapter implements View.OnClickListener, View.OnLongClickListener, CompoundButton.OnCheckedChangeListener {
    private static final int MAX = 1000;
    private static final String TAG = "CategorySimpleAdapter";
    private Context mContext;
    private List<Category> data;
    private boolean mSelect =false;

    private int layoutId= R.layout.category_item;
    private int type;
    private long[] ids;
    private Runnable mSelectCallback;


    public CategorySimpleAdapter(Context context) {
        super();
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return data == null ? 0 : data.size();
    }

    @Override
    public Category getItem(int position) {
        return data == null || position >= data.size() || position < 0 ? null : data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    protected View createView(Category category) {
        View convertView = LayoutInflater.from(mContext).inflate(layoutId, null);
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.id  = category.getId();
        viewHolder.checkBox = convertView.findViewById(R.id.check_box);
        viewHolder.name = convertView.findViewById(R.id.tv_name);
        viewHolder.value = convertView.findViewById(R.id.tv_value1);

        convertView.setTag(viewHolder);
        convertView.setOnClickListener(this);
        convertView.setOnLongClickListener(this);
        viewHolder.checkBox.setOnCheckedChangeListener(this);
        return convertView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Category category = getItem(position);
        if (convertView == null) {
            convertView = createView(category);
        }

        ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        viewHolder.name.setText(category.getName());
        viewHolder.value.setText(String.format(Locale.US,"%d",category.getNum()));

        viewHolder.id = category.getId();
        if(mSelect){
            viewHolder.checkBox.setVisibility(View.VISIBLE);
            viewHolder.checkBox.setTag(position);
            viewHolder.checkBox.setChecked(category.isChecked());
        }else {
            viewHolder.checkBox.setVisibility(View.INVISIBLE);
            viewHolder.checkBox.setTag(-1);
            viewHolder.checkBox.setChecked(false);
        }
        convertView.setOnLongClickListener(this);

        return convertView;
    }



    public void setData(List<Category> data) {
        this.data =data;
    }

    public List<Category> getData() {
        return data;
    }

    public void addData(Category category) {
        if (data == null) data = new ArrayList<>();
        if (data.size() < MAX) ;
        {

            data.add(category);
        }
    }



    public void clear() {
        if (data != null) {
            data.clear();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getTag() instanceof ViewHolder) {
            ViewHolder viewHolder = (ViewHolder) v.getTag();
            if (type == CategoryActivity.TYPE_SELECT) {
                if (ids != null && ids.length > 0) {
                    DataApi dataApi = MyApi.getInstance().getDataApi();
                    for (long id : ids) {
                        Record record = dataApi.getRecordById(id);
                        record.setCategory(viewHolder.name.getText().toString());

                        dataApi.updateRecords(record);
                    }
                }
                if(mSelectCallback!=null){
                    mSelectCallback.run();
                }
            }else {
                CategoryDataActivity.showMe(mContext,viewHolder.id);
            }
        }

    }

    @Override
    public boolean onLongClick(View v) {
        if(!mSelect){
            mSelect = true;
            notifyDataSetChanged();
            return true;
        }
        return false;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        try {
            if (buttonView.getTag() instanceof Integer) {
                Integer id = (Integer) buttonView.getTag();
                if(id>=0 && id<getCount()) getItem(id).setChecked(isChecked);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setSelected(boolean b) {
        mSelect =b;
    }

    public boolean isSelected() {
        return mSelect;
    }

    public void setSelectedAll(boolean b) {
        for(int i=0; i<getCount() ;i++){
            getItem(i).setChecked(b);
        }
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setIds(long[] ids) {
        this.ids = ids;
    }

    public long[] getIds() {
        return ids;
    }

    public void setSelectCallback(Runnable selectCallback) {
        this.mSelectCallback = selectCallback;
    }


    public static class ViewHolder {

        public TextView value;

        public Long id;
        public TextView name;
        public CheckBox checkBox;
    }
}
