package com.zen.ui.adapter;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.zen.ui.DataDetailActivity;
import com.zen.ui.R;

import java.util.List;

import static java.security.AccessController.getContext;

public class FragmentDataSimpleAdapter extends BaseAdapter implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private static final int MAX = 1000;
    private Context mContext;
    private List<Bean> data;
    private boolean selected;
    private boolean entDetail=true;
    private boolean AssectYes = false;

    public FragmentDataSimpleAdapter(Context context){
        super();
        this.mContext =context;
    }

    @Override
    public int getCount() {
        return data==null?0:data.size();
    }

    @Override
    public Bean getItem(int position) {
        return data==null||position>=data.size()|| position<0?null:data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView==null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.data_item2,null);
            ViewHolder viewHolder= new ViewHolder();
            viewHolder.select = convertView.findViewById(R.id.cb_select);
            viewHolder.layout = convertView.findViewById(R.id.lv_item);
            viewHolder.sn = convertView.findViewById(R.id.tv_1);
            viewHolder.dateTime = convertView.findViewById(R.id.tv_2);
            viewHolder.note = convertView.findViewById(R.id.tv_3);
            viewHolder.value = convertView.findViewById(R.id.tv_4);
            viewHolder.typeIcon = convertView.findViewById(R.id.iv_5);
            convertView.setTag(viewHolder);
            convertView.setOnClickListener(this);
        }
        ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        Bean bean = getItem(position);
        if(isSelected()){
            viewHolder.select.setVisibility(View.VISIBLE);
            viewHolder.select.setOnCheckedChangeListener(null);
            viewHolder.select.setTag(null);
            viewHolder.select.setChecked(bean.check);
            viewHolder.select.setTag(position);
            viewHolder.select.setOnCheckedChangeListener(this);
        }else {
            viewHolder.select.setVisibility(View.GONE);
        }

        //需要改的地方
        //String.format("%d",bean.sn)
        viewHolder.sn.setText(String.valueOf(position+1));
        viewHolder.ID = String.valueOf(bean.sn);
        viewHolder.dateTime.setText(bean.dateTime);
        viewHolder.note.setText(bean.note);
        viewHolder.value.setText(bean.value);
        viewHolder.typeIcon.setImageResource(bean.typeIconId);
     /*   if(position%2==0){
            convertView.setBackgroundColor(mContext.getResources().getColor(R.color.cell_background1_color));
        }else*/{
            setBackgroundColor(viewHolder.layout,mContext.getResources().getColor(R.color.cell_background3_color));
        }
        return convertView;
    }

    public boolean AssectChange(boolean assectYeso){
        AssectYes = assectYeso;
        return AssectYes;
    }

    private void setBackgroundColor(ViewGroup layout, int color) {
        for (int i = 0; i < layout.getChildCount(); i++) {
            {
                layout.getChildAt(i).setBackgroundColor(color);
            }
        }
    }


    public void setData(List<Bean> data) {
        this.data = data;
    }

    public List<Bean> getData() {
        return data;
    }

    public void addData(Bean bean,boolean order) {
        if (data.size() < MAX) ;
        {
          if(order){
              data.add(0,bean);
          }else {
              data.add(bean);
          }
        }
    }

    @Override
    public void onClick(View v) {
        if(isSelected() || !entDetail) return;
        try {
            if (AssectYes){

            }else {
                if (v.getTag() instanceof ViewHolder) {
                    ViewHolder viewHolder = (ViewHolder) v.getTag();
                    //修改ID以前是viewHolder.sn.getText().toString();
                    long id = Long.parseLong(viewHolder.ID);

                    DataDetailActivity.Companion.showMe(mContext, id);
                }
            }
        } catch (Exception e) {
            Log.w("Exception", "onCellClicked Exception", e);
        }
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        try {
            int position = (int) buttonView.getTag();
            Bean bean = getItem(position);
            if (bean != null) {
                bean.check = isChecked;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setEntDetail(boolean entDetail) {
        this.entDetail = entDetail;
    }

    public boolean isEntDetail() {
        return entDetail;
    }

    public static class Bean{
        public Long sn;
        public String dateTime;
        public String note;
        public String value;
        public int typeIconId;
        public boolean check;
    }

    public static class ViewHolder{
        //修改添加ID
        public String ID;
        public TextView sn;
        public TextView dateTime;
        public TextView note;
        public TextView value;
        public ImageView typeIcon;
        public ViewGroup layout;
        public CheckBox select;
    }
}
