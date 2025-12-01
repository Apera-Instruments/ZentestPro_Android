package com.zen.ui.adapter;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.orhanobut.logger.Logger;
import com.zen.ui.R;
import com.zen.ui.utils.AssectSaveData;

import java.util.List;

public class AssectListAdapter extends RecyclerView.Adapter<AssectListAdapter.ViewHolder> {
    private OnReCyclerItemClickListener listeners;
    private List<AssectSaveData> mAssectList;
    public View mHeaderView;
    public View mFooterView;
    public int mPosition;

    public static final int TYPE_HEADER = 0; //说明是带有Header的
    public static final int TYPE_FOOTER = 1; //说明是带有Footer的
    public static final int TYPE_NORMAL = 2; //说明是不带有header和footer的

    //在这里面加载ListView中的每个item的布局
    class ListHolder extends RecyclerView.ViewHolder{
        public ListHolder(View itemView) {
            super(itemView);
            //如果是headerview或者是footerview,直接返回

        }
    }

     class ViewHolder extends  RecyclerView.ViewHolder{
        ImageView AssectImage;
        TextView AssectText;
        public ViewHolder(View view){
            super(view);

            if (itemView == mHeaderView){
                return;
            }
            if (itemView == mFooterView){
                return;
            }
            AssectImage = (ImageView) view.findViewById(R.id.Assect_image);
            AssectText = (TextView) view.findViewById(R.id.Assect_text);
        }
    }

    public View getHeaderView() {
        return mHeaderView;
    }

    public void setHeaderView(View headerView) {
        mHeaderView = headerView;
        notifyItemInserted(0);
    }

    public View getFooterView() {
        return mFooterView;
    }

    public void setFooterView(View footerView) {
        mFooterView = footerView;
        notifyItemInserted(getItemCount()-1);
    }

    @Override
    public int getItemViewType(int position) {
        if (mHeaderView == null && mFooterView == null){
            return TYPE_NORMAL;
        }
        if (position == 0){
            //第一个item应该加载Header
            return TYPE_HEADER;
        }
//        if (position == getItemCount()-1){
//            //最后一个,应该加载Footer
//            return TYPE_FOOTER;
//        }
        return TYPE_NORMAL;
    }

    public interface OnReCyclerItemClickListener{
        void onItemClick(View view, int position);
    }

    public AssectListAdapter(List<AssectSaveData> fruitList, OnReCyclerItemClickListener onReCyclerItemClickListener){
        mAssectList = fruitList;
        listeners = onReCyclerItemClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){

        if(mHeaderView != null && viewType == TYPE_HEADER) {

            return new ViewHolder(mHeaderView);
        }
        if(mFooterView != null && viewType == TYPE_FOOTER){
            return new ViewHolder(mFooterView);
        }

        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.assectlistview, parent, false);
        final ViewHolder holder = new ViewHolder(view);

        return holder;
    }

    //返回View中Item的个数，这个时候，总的个数应该是ListView中Item的个数加上HeaderView和FooterView
    @Override
    public int getItemCount() {
        if(mHeaderView == null && mFooterView == null){
            return  mAssectList.size();
        }else if(mHeaderView == null && mFooterView != null){
            return  mAssectList.size() + 1;
        }else if (mHeaderView != null && mFooterView == null){
            return  mAssectList.size() + 1;
        }else {
            return  mAssectList.size() + 2;
        }
    }

    int row_index = -1;
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position){

        if(getItemViewType(position) == TYPE_NORMAL){
            AssectSaveData assect;
            if (mHeaderView!=null){
                assect = mAssectList.get(position-1);
            }else {
                assect = mAssectList.get(position);
            }


            holder.AssectImage.setImageBitmap(BitmapFactory.decodeFile(assect.getImageID()));
            holder.AssectText.setText(assect.getName());

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View V) {

                    if (listeners!=null){
                        row_index = position;
                        notifyDataSetChanged();
                        listeners.onItemClick(holder.itemView, position);
                    }
                }
            });

            if (position == row_index){
                int color = Color.argb(255,233,233,233);
                holder.itemView.setBackgroundColor(color);
            }else {
                holder.itemView.setBackgroundColor(Color.WHITE);
            }

            return;
        }else if(getItemViewType(position) == TYPE_HEADER){
            return;
        }else{
            return;
        }
    }



    public int getmPosition(){
        return mPosition;
    }

    public void setmPosition(int id){
        this.mPosition = id;
    }
//   @Override
//    public int getItemCount(){
//        return mAssectList.size();
//    }

}
