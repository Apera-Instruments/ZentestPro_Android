package com.zen.ui.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.zen.ui.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MeasureDataSimpleAdapter extends BaseAdapter {
    private static final int MAX = 1000;
    private Context mContext;
    private List<Bean> data;

    public MeasureDataSimpleAdapter(Context context){
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.data_item,null);
            ViewHolder viewHolder= new ViewHolder();
            viewHolder.layout = convertView.findViewById(R.id.lv_item);
            viewHolder.sn = convertView.findViewById(R.id.tv_1);
            viewHolder.date = convertView.findViewById(R.id.tv_2);
            viewHolder.time = convertView.findViewById(R.id.tv_3);
            viewHolder.value = convertView.findViewById(R.id.tv_4);
            viewHolder.temp = convertView.findViewById(R.id.tv_5);
            convertView.setTag(viewHolder);
        }
        ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        Bean bean = getItem(position);
        viewHolder.sn.setText(String.format("%3d",bean.sn));
        viewHolder.date.setText(bean.date);
        viewHolder.time.setText(bean.time);
        viewHolder.value.setText(bean.value);
        viewHolder.temp.setText(bean.temp);
        if(position%2==0){
           setBackgroundColor( viewHolder.layout,mContext.getResources().getColor(R.color.cell_background1_color));
        }else{
            setBackgroundColor(viewHolder.layout,mContext.getResources().getColor(R.color.cell_background2_color));
        }
        return convertView;
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

    public void addData(Bean bean) {
        if (data.size() < MAX) ;
        {
            data.add(0,bean);
        }
    }

    public static class Bean{
        public int sn;
        public String date;
        public String time;
        public String value;
        public String temp;
    }

    public static class ViewHolder{
        public TextView sn;
        public TextView date;
        public TextView time;
        public TextView value;
        public TextView temp;
        public ViewGroup layout;
    }
}
