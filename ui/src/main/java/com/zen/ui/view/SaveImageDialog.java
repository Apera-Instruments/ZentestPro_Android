package com.zen.ui.view;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigkoo.pickerview.OptionsPickerView;
import com.blankj.utilcode.util.ToastUtils;
import com.orhanobut.logger.Logger;
import com.zen.api.Constant;
import com.zen.api.DataApi;
import com.zen.api.MyApi;
import com.zen.api.data.DataBean;
import com.zen.api.data.Record;
import com.zen.api.event.UpdateEvent;
import com.zen.api.protocol.CalibrationCond;
import com.zen.api.protocol.CalibrationPh;
import com.zen.api.protocol.Data;
import com.zen.api.protocol.ParmUp;
import com.zen.ui.CalibrationActivity;
import com.zen.ui.R;
import com.zen.ui.adapter.AssectListAdapter;

import com.zen.ui.utils.AssectSaveData;
import com.zen.ui.utils.DisplayUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import com.zen.api.event.SyncEventUpload;
import com.zen.ui.utils.ToastUtilsX;
import com.zen.ui.utils.ZipUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.List;
import java.util.Locale;

import im.dacer.androidcharts.LineView;

import static android.content.Context.MODE_PRIVATE;


/**
 * @author Allen
 * @date 2019/11/6.
 * @description 弹出图片
 */
public class SaveImageDialog extends BaseDialogFragment {

    private LayoutInflater mInflater;
    private ViewPager viewPager;

    private ImageView mImageViewPoint1;
    private ImageView mImageViewPoint2;
    private ImageView mImageViewPoint3;
    private boolean mNewMode=true;
    private TextView mTextViewOffset;
    private TextView mTextViewSlop;
    private TextView mTextViewCalDate;
    private float mZeroAngle;
    private int mTempUnit=0;
    private ViewGroup mViewGroupSlop;
    private ViewGroup mViewGroupZeroPoint;
    private ViewGroup mViewGroupPoints;
    private TextView lineLeftTextView;
    private TextView lineRightTextView;

    private TextView   mTextViewTempRef;
    private  ViewGroup   mViewGroupTempRef ;

    private TextView  mTextViewTempCoeffc ;
    private ViewGroup   mViewGroupTempCoeffc ;

    private TextView   mTextViewSalinity ;
    private  ViewGroup   mViewGroupSalinity ;

    private TextView   mTextViewTDS ;
    private  ViewGroup  mViewGroupTDS ;
    private ViewGroup mViewGroupLastDate;

    private LinearLayout sendBt;
    private LinearLayout AssectAdmin;

    private LinearLayout categoryll;
    private LinearLayout ly_del;
    private ImageView Image_back;
    private TextView NoteButton;
    private TextView TVdate;
    private ImageView imageShow;
    private TextView AssecttixiHome;
    private ImageView ImageAssect;
    private TextView AssectnameHome;
    private RelativeLayout Re_id;
    private RecyclerView Re;
    private Record record;
    private AssectListAdapter adapter;
    private int intIDList;
    private TextView ly_type;
    private TextView Ty_number;
    private TextView Tv_type;
    private TextView Tv_temps;
    private TextView Tv_tempType;

    private List<AssectSaveData> assectList = new ArrayList<>();
//    private String Dataid;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        record = MyApi.getInstance().getDataApi().getRecords().get(0);
        EventBus.getDefault().register(this);

    }

    private LayoutInflater getMyLayoutInflater() {
        if (Build.VERSION.SDK_INT >= 26) {
            return super.getLayoutInflater();
        } else {
            return mInflater;
        }
    }

    @Override
    protected void initView(View view) {

        AssecttixiHome = (TextView) view.findViewById(R.id.ass_tixihome);
        AssectnameHome = (TextView) view.findViewById(R.id.ass_namehome);
        ImageAssect = (ImageView) view.findViewById(R.id.assect_imagehome);
        Image_back = (ImageView) view.findViewById(R.id.Btn_back);
        NoteButton = (TextView) view.findViewById(R.id.tv_note);
        TVdate = (TextView) view.findViewById(R.id.tv_date);
        mPicDate = record.getCreateTime();
        TVdate.setText(getDataString(mPicDate));

        sendBt = (LinearLayout) view.findViewById(R.id.ly_send);
        AssectAdmin = (LinearLayout) view.findViewById(R.id.ly_Asset);
        categoryll = (LinearLayout) view.findViewById(R.id.ly_category);
        ly_del = (LinearLayout) view.findViewById(R.id.ly_del);
        imageShow = (ImageView) view.findViewById(R.id.iv_pic);
        imageShow.setImageBitmap(BitmapFactory.decodeFile(record.getPic()));

        Re = (RecyclerView) view.findViewById(R.id.Recycler_View);
        Re_id = (RelativeLayout) view.findViewById(R.id.Recycler_id);
        imageShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Re_id.setVisibility(View.INVISIBLE);
            }
        });

        SharedPreferences sharedPreferences = view.getContext().getSharedPreferences("AssectList", MODE_PRIVATE);
        assectList.clear();
        int size = sharedPreferences.getInt("data_size", 0);
        for (int i = 0; i < size; i++) {
            AssectSaveData beer = new AssectSaveData(sharedPreferences.getString("name_" + i, null), sharedPreferences.getString("Image" + i, null), sharedPreferences.getString("nameid" + i, null), sharedPreferences.getString("dataid" + i, null));
            assectList.add(beer);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        layoutManager.setOrientation(OrientationHelper.VERTICAL);
        Re.setLayoutManager(layoutManager);

        Re.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                float msu = recyclerView.getHeight();

            }
        });

        adapter = new AssectListAdapter(assectList, new AssectListAdapter.OnReCyclerItemClickListener() {

            @Override
            public void onItemClick(View V, int position) {
                intIDList = position;
                adapter.setmPosition(position);

            }
        });

        Re.setAdapter(adapter);

        TextView cancle = view.findViewById(R.id.id_cancel);
        cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Re_id.setVisibility(View.INVISIBLE);
            }
        });

        TextView buttonOk = view.findViewById(R.id.id_ok);
        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (intIDList >= 0) {
                    AssectSaveData assectSaveData = assectList.get(intIDList);
                    AssectnameHome.setText(assectSaveData.getName());
                    ImageAssect.setImageBitmap(BitmapFactory.decodeFile(assectSaveData.getImageID()));
                    AssectnameHome.setVisibility(View.VISIBLE);
//                    AssecttixiHome.setVisibility(View.VISIBLE);
                    ImageAssect.setVisibility(View.VISIBLE);

                    save();
                }

                Re_id.setVisibility(View.INVISIBLE);
            }
        });

//        ly_type = (TextView) view.findViewById(R.id.ly_Type);
//        Tv_type = (TextView) view.findViewById(R.id.Tv_type);
//        Ty_number = (TextView) view.findViewById(R.id.Tv_number);
//        Tv_temps = (TextView) view.findViewById(R.id.TV_temps);
//        Tv_tempType = (TextView) view.findViewById(R.id.TV_tempType);

//        String Types = "";
//        if (record.getType()==1){
//            Types = "pH";
//        }
//        if (record.getType()==2){
//            Types = "Sal.";
//        }
//        if (record.getType()==3){
//            Types = "ORP";
//        }
//        if (record.getType()==4){
//            Types = "TDS.";
//        }
//        if (record.getType()==5){
//            Types = "Cond.";
//        }
//        if (record.getType()==6){
//            Types = "Res.";
//        }
//
//        String Temps = "";
//        if (record.getTempUnit()=="C"){
//            Temps = "℃";
//        }
//        if (record.getTempUnit()=="F"){
//            Temps = "℉";
//        }

//        ly_type.setText(String.valueOf(record.getValue()));
//        Tv_type.setText(record.getValueUnit());
//        Ty_number.setText(Types);
//        Tv_temps.setText(record.getTempValue());
//        Tv_tempType.setText(Temps);

//        viewPager = (ViewPager) view.findViewById(R.id.viewpagers);
//        LayoutInflater inflater = getMyLayoutInflater();

//        View view1 = inflater.inflate(R.layout.viewpager_measure_1, null);
//        View view3 = inflater.inflate(R.layout.viewpager_measure_3, null);
//        ViewGroup view4 = (ViewGroup) inflater.inflate(R.layout.viewpager_measure_4, null);
//
//        final List<View> viewList = new ArrayList<View>();// 将要分页显示的View装入数组中
//        if (record.getTabType()==0||record.getTabType()==1){
//            initView1(view1);
//
//            viewList.add(view1);
//        }else {
//            initView1(view1);
//            initView3(view3);
//            initView4(view4);
//
//            viewList.add(view1);
//            viewList.add(view3);
//            viewList.add(view4);
//        }

//        PagerAdapter pagerAdapter = new PagerAdapter() {
//
//            @Override
//            public boolean isViewFromObject(View arg0, Object arg1) {
//
//                return arg0 == arg1;
//            }
//
//            @Override
//            public int getCount() {
//
//                return viewList.size();
//            }
//
//            @Override
//            public void destroyItem(ViewGroup container, int position,
//                                    Object object) {
//
//                container.removeView(viewList.get(position));
//            }
//
//            @Override
//            public Object instantiateItem(ViewGroup container, int position) {
//
//                container.addView(viewList.get(position));
//
//                return viewList.get(position);
//            }
//        };

//        viewPager.setAdapter(pagerAdapter);
//        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//            @Override
//            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//
//            }
//
//            @Override
//            public void onPageSelected(int position) {
////                updateTableView();
//            }
//
//            @Override
//            public void onPageScrollStateChanged(int state) {
//
//            }
//        });
//        setHeadrView(Re);
    }

    public void save(){
        record = MyApi.getInstance().getDataApi().getRecordById(record.getId());
        String noteName  = this.AssectnameHome.getText().toString();
        String operator  =  record.getOperator();
        String notes  = record.getNotes();
        boolean dirty=false;

        if(noteName!=record.getNoteName()){
            dirty = true;
            record.setNoteName(noteName);
        }

        if(dirty){
            record.setSync(false);
            MyApi.getInstance().getDataApi().updateRecords(record);
        }

        EventBus.getDefault().post(new SyncEventUpload());
    }

    //需要修改的地方
    public void onResume(){
        super.onResume();
        record = MyApi.getInstance().getDataApi().getRecordById(record.getId());
        if (!TextUtils.isEmpty(record.getNoteName())) {
            SharedPreferences sharedPreferences  = getActivity().getSharedPreferences("AssectList", MODE_PRIVATE);
            int size = sharedPreferences.getInt("data_size", 0);
            for (int i = 0; i < size; i++) {
                if (record.getNoteName().equals(sharedPreferences.getString("name_" + i, null))) {
                    ImageAssect.setImageBitmap(
                            BitmapFactory.decodeFile(
                                    sharedPreferences.getString(
                                            "Image" + i,
                                            null
                                    )
                            )
                    );
                }
            }
            AssectnameHome.setVisibility(View.VISIBLE);
            AssectnameHome.setText(record.getNoteName());
//            AssecttixiHome.setVisibility(View.VISIBLE);
            ImageAssect.setVisibility(View.VISIBLE);
        } else {
            AssectnameHome.setVisibility(View.INVISIBLE);
//            AssecttixiHome.setVisibility(View.INVISIBLE);
            ImageAssect.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setGravity(Gravity.BOTTOM);
        getDialog().getWindow().setLayout(DisplayUtil.getScreenWidthPx(activity), getDialog().getWindow().getAttributes().height);

        WindowManager.LayoutParams lp = getDialog().getWindow().getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        //设置dialog的位置在底部
        lp.gravity = Gravity.CENTER;
        //设置dialog的动画
//        lp.windowAnimations = R.style.FragmentDialogAnimation;
        getDialog().getWindow().setAttributes(lp);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.dialog_save_img;
    }

    @Override
    protected void setSubView() {

    }

    private ArrayList<String> SendArray = new ArrayList<>();
    private String emailTitle;
    private Long mPicDate;
    private String eMailText;
    private Boolean sending=false;
    @Override
    protected void initEvent() {

        sendBt.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                ionConfirmClick.onClcik(1);
                ionConfirmClick.onClickStr("123");

                if (record.getPic() == null){
//                    ToastUtils.showShort(getString(R.string.picture_none));
                    ToastUtilsX.showActi(getActivity(),R.string.picture_none);
                }

                try {
                    mPicDate = record.getCreateTime();
                    String date = getDataString(mPicDate);
                    emailTitle = "ZenTest ["+record.getId()+"] "+date;

                    if(record.getNoteName()!=null){
                        eMailText = record.getNoteName()+"\r\n";
                    }
                    if(record.getNotes()!=null){
                        eMailText += record.getNotes()+"\r\n";
                    }
                    if(record.getOperator()!=null){
                        eMailText += record.getOperator()+"\r\n";
                    }
                    if(date!=null){
                        eMailText += date+"\r\n";
                    }

                    SendArray.clear();
                    SendArray.add("createTime");
                    SendArray.add("value");
                    SendArray.add("unitString");
                    SendArray.add("temp");
                    SendArray.add("tempUnit");

                    SharedPreferences sharedPreferences = MyApi.getInstance().getDataApi().getSetting();
                    boolean b = sharedPreferences.getBoolean("SettingFragmentData:ser_no", false);
                    if (b){
                        SendArray.add(0,"sn");
                    }
                    b = sharedPreferences.getBoolean("SettingFragmentData:gps", false);
                    if (b) {
                        SendArray.add("location");
                    }
                    b = sharedPreferences.getBoolean("SettingFragmentData:calibration", false);
                    if (b) {
                        SendArray.add("calibration");
                    }
                    File file;
                    if (null != record.getPic()){
                        file = new File(record.getPic());
                    }else {
                        file = null;
                    }

                    File fileCSV = MyApi.getInstance().getDataApi().exportCSV(getContext(), record, SendArray.toArray(new String[SendArray.size()]));
                    if (fileCSV != null){
                        File out = new File(getContext().getExternalFilesDir("share"), String.valueOf(System.currentTimeMillis()) + ".zip");

                        if (out.exists()) out.delete();
                        File fileOut = out.getAbsoluteFile();
                        try {
                            if (file != null) {
                                ZipUtil.zip(out, file.getAbsolutePath(), fileCSV.getAbsolutePath());
                            } else {
                                ZipUtil.zip(out, fileCSV.getAbsolutePath());
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        shareFile(getContext(), out,emailTitle,eMailText);
                    }else {
                        if(file!=null) {
                            shareFile(getContext(), file,emailTitle,eMailText);
                        }else {
//                            ToastUtils.showShort(getString(R.string.file_none));
                            ToastUtilsX.showActi(getActivity(),R.string.file_none);
                        }
                    }
                    sending = true;
                }catch ( Exception e){

                };
            }
        });

        AssectAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ionConfirmClick.onClcik(2);
                ionConfirmClick.onClickStr("345");

                Re_id.setVisibility(View.VISIBLE);
            }
        });

        categoryll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ionConfirmClick.onClcik(3);
                ionConfirmClick.onClickStr("567");
            }
        });

        ly_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ionConfirmClick.onClcik(4);
                ionConfirmClick.onClickStr("789");
            }
        });

        NoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ionConfirmClick.onClcik(5);
                ionConfirmClick.onClickStr("789");
            }
        });

        Image_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ionConfirmClick.onClcik(6);
                ionConfirmClick.onClickStr("679");
            }
        });
    }

    public String getDataString(Long date) {
        SimpleDateFormat simpleDateFormat =new SimpleDateFormat("yyyy/MM/dd a hh:mm:ss", Locale.US);
        return simpleDateFormat.format(date);
    }

    // 調用系統方法分享文件
    public void shareFile(Context context, File file, String subject, String text) {
        if (null != file && file.exists()) {

            Intent share = new Intent(Intent.ACTION_SEND);
            //share.addCategory(Intent.CATEGORY_APP_EMAIL)
            share.putExtra(Intent.EXTRA_STREAM, getUriForFile(context,file));
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

        }
    }

    public Uri getUriForFile(Context context, File file) {
        if (context == null || file == null) {
//            throw NullPointerException();
        }
        Uri uri;
        if (Build.VERSION.SDK_INT >= 24) {
            uri = FileProvider.getUriForFile(context, "com.zen.zentest.fileprovider", file);
        } else {
            uri = Uri.fromFile(file);
        }
        return uri;
    }

    @Override
    protected void onCancel() {

    }

    private IonConfirmClick ionConfirmClick;

    public void setOnConfirmClcik(IonConfirmClick mOnConfirmClick){
        ionConfirmClick=mOnConfirmClick;
    }

    public interface IonConfirmClick{
        void onClcik(int type);//0 取消  1 确认
        void onClickStr(String string);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpdateEvent event) {

    }

//    private void initView1(View view) {
//        mImageViewPoint1 = (ImageView)   view.findViewById(R.id.iv_point1);
//        mImageViewPoint2 = (ImageView)   view.findViewById(R.id.iv_point2);
//        mImageViewPoint3 = (ImageView)   view.findViewById(R.id.iv_point3);
//        mTextViewOffset = (TextView)   view.findViewById(R.id.tv_offset1);
//        mTextViewSlop = (TextView)   view.findViewById(R.id.tv_slop1);
//        mTextViewCalDate = (TextView)   view.findViewById(R.id.tv_cal_date);
//        mViewGroupLastDate = (ViewGroup)   view.findViewById(R.id.layout_last_cailb);
//        mViewGroupSlop = (ViewGroup)   view.findViewById(R.id.layout_slope);
//        mViewGroupZeroPoint = (ViewGroup)   view.findViewById(R.id.layout_zero_point);
//        mViewGroupPoints = (ViewGroup)   view.findViewById(R.id.layout_points);
//
//        mTextViewTempRef = (TextView)   view.findViewById(R.id.tv_temp_ref);
//        mViewGroupTempRef = (ViewGroup)   view.findViewById(R.id.layout_temp_ref);
//
//        mTextViewTempCoeffc = (TextView)   view.findViewById(R.id.tv_temp_coeffc);
//        mViewGroupTempCoeffc = (ViewGroup)   view.findViewById(R.id.layout_temp_coeff);
//
//        mTextViewSalinity = (TextView)   view.findViewById(R.id.tv_salinity);
//        mViewGroupSalinity = (ViewGroup)   view.findViewById(R.id.layout_salinity);
//
//        mTextViewTDS = (TextView)   view.findViewById(R.id.tv_tds);
//        mViewGroupTDS = (ViewGroup)   view.findViewById(R.id.layout_tds);
//    }

//    private void initView3(View view) {
//
//    }
//
//    private void initView4(ViewGroup view) {
//
//    }
}
