package com.zen.ui.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.bigkoo.pickerview.OptionsPickerView;
import com.orhanobut.logger.Logger;
import com.zen.api.MyApi;
import com.zen.api.SettingConfig;
import com.zen.api.event.FactoryEvent;
import com.zen.api.event.SettingDeviceEvent;
import com.zen.api.protocol.ParmDown;
import com.zen.ui.R;
import com.zen.ui.Setting2Activity;
import com.zen.ui.fragment.bean.TimeBean;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;
import lib.kingja.switchbutton.SwitchMultiButton;

import static com.zen.api.SettingConfig.COND_FactoryReset_SAVE_ID;
import static com.zen.api.SettingConfig.PH_FactoryReset_SAVE_ID;

public class ParameterSimpleAdapter extends BaseAdapter {
    private static final int MAX = 1000;
    private static final String TAG = "ParameterSimpleAdapter";
    private Context mContext;
    private List<ParameterSimpleAdapter.Bean> data;
    public final static int VALUE_ID = R.layout.parameter_item_value;
    public final static int MORE_ID = R.layout.parameter_item_more;
    public final static int FACTORY_RESET_ID = R.layout.parameter_item_reset;
    public final static int MULTI_SWITCH_ID = R.layout.parameter_item_switch_button;
    public final static int SWITCH_ID = R.layout.parameter_item_switch;
    public final static int LIST_VALUE_ID = R.layout.parameter_list_item;
    private int mOptionPickerTitleId = R.string.temp_coeff;

    private int mOptionDataType = 2;
    private int mOptionDataValue1;
    private int mOptionDataValue2;


    public ParameterSimpleAdapter(Context context) {
        super();
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return data == null ? 0 : data.size();
    }

    @Override
    public ParameterSimpleAdapter.Bean getItem(int position) {
        return data == null || position >= data.size() || position < 0 ? null : data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    protected View createView(Bean bean) {
        View convertView = LayoutInflater.from(mContext).inflate(bean.layoutId, null);
        ParameterSimpleAdapter.ViewHolder viewHolder = new ParameterSimpleAdapter.ViewHolder();
        viewHolder.layoutId = bean.layoutId;
        viewHolder.value = convertView.findViewById(R.id.tv_value1);
        viewHolder.title = convertView.findViewById(R.id.tv_title);
        viewHolder.aSwitch = convertView.findViewById(R.id.switch1);
        viewHolder.switchMultiButton = convertView.findViewById(R.id.sw_multi_button1);
        convertView.setTag(viewHolder);

        return convertView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ParameterSimpleAdapter.Bean bean = getItem(position);
        if (convertView == null) {
            convertView = createView(bean);
        }

        ParameterSimpleAdapter.ViewHolder viewHolder = (ParameterSimpleAdapter.ViewHolder) convertView.getTag();
        if (viewHolder.layoutId != bean.layoutId) {
            convertView = createView(bean);
            viewHolder = (ParameterSimpleAdapter.ViewHolder) convertView.getTag();
        }

//        viewHolder.value.setText(bean.value);

        viewHolder.title.setText(getTitleId(bean.saveId));//bean.title);
        SharedPreferences sharedPreferences = MyApi.getInstance().getDataApi().getSetting();
        bean.value = sharedPreferences.getString(bean.saveId, bean.value);


        if (viewHolder.layoutId == FACTORY_RESET_ID) {
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "onClick FACTORY_RESET_ID");
                    new SweetAlertDialog(mContext, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText(mContext.getString(R.string.are_you_sure))
                            .setContentText(mContext.getString(R.string.factory_reset_default))
                            .setConfirmText(mContext.getString(R.string.yes_factory_reset))
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    int type = 0;
                                    if (bean.saveId != null && bean.saveId.equals(PH_FactoryReset_SAVE_ID)) {
                                        type = ParmDown.TYPE1;
                                        EventBus.getDefault().post(new FactoryEvent(type));
                                    } else if (bean.saveId != null && bean.saveId.equals(COND_FactoryReset_SAVE_ID)) {
                                        type = ParmDown.TYPE2;
                                        EventBus.getDefault().post(new FactoryEvent(type));
                                    } else {

                                    }

                                    sweetAlertDialog.dismissWithAnimation();
                                }
                            })
                            .show();
                }
            });
        } else if (viewHolder.layoutId == LIST_VALUE_ID) {
            if (viewHolder.value != null) {
                viewHolder.value.setText(bean.value);
            }
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "onClick LIST_VALUE_ID " + bean.title);
                    showList(bean);
                }
            });
        } else if (viewHolder.layoutId == MORE_ID) {
            if (viewHolder.value != null) {
                viewHolder.value.setText(bean.value);
            }
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "onClick MORE_ID");
                    Bean bean1 = bean;
                    Setting2Activity.showMe(mContext, bean1.saveId, bean1.title, bean1.value, bean1.mSwitchTabsResId);
                }
            });
        } else if (viewHolder.layoutId == VALUE_ID) {
            if (viewHolder.value instanceof EditText) {
                EditText editText = (EditText) viewHolder.value;

                final String saveId = bean.saveId;
                Object o = editText.getTag();
                if (o instanceof TextWatcher) {
                    editText.removeTextChangedListener((TextWatcher) o);
                }

                viewHolder.value.setText(bean.value);

                TextWatcher textWatcher = new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        Log.i(TAG, "afterTextChanged " + saveId + " -> " + s);
                        SharedPreferences sharedPreferences = MyApi.getInstance().getDataApi().getSetting();

                        boolean ret = sharedPreferences.edit().putString(saveId, s.toString()).commit();
                        if (ret) {
                            EventBus.getDefault().post(new SettingDeviceEvent());
                        }
                    }
                };

                editText.setTag(textWatcher);
                editText.addTextChangedListener(textWatcher);
            }
        } else if (viewHolder.layoutId == SWITCH_ID) {
            if (viewHolder.aSwitch != null) {
                viewHolder.aSwitch.setTag(bean);
                viewHolder.aSwitch.setChecked(Boolean.parseBoolean(bean.value));
                final ParameterSimpleAdapter.Bean swBean = bean;
                viewHolder.aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        Log.i(TAG, "onCheckedChanged ");
                        SharedPreferences sharedPreferences = MyApi.getInstance().getDataApi().getSetting();
                        swBean.value = String.valueOf(isChecked);
                        boolean ret = sharedPreferences.edit().putString(swBean.saveId, swBean.value).commit();
                        if (ret) EventBus.getDefault().post(new SettingDeviceEvent());
                    }
                });
            } else {
                Log.i(TAG, "aSwitch null");
            }
        } else if (viewHolder.layoutId == MULTI_SWITCH_ID) {

            Log.i(TAG, "it is MULTI_SWITCH_ID " + bean.value + " " + position);
            if (viewHolder.switchMultiButton != null) {
                viewHolder.switchMultiButton.setTag(bean);
                viewHolder.switchMultiButton.setOnSwitchListener(null);
                if (bean.mSwitchTabsResId > 0) {
                    bean.mSwitchTabs = mContext.getResources().getStringArray(bean.mSwitchTabsResId);
                    viewHolder.switchMultiButton.setText(bean.mSwitchTabs);
                    Log.i(TAG, "SwitchTabsResId = " + bean.mSwitchTabsResId + " : " + Arrays.toString(bean.mSwitchTabs) + " / " + bean.value);
                    viewHolder.switchMultiButton.setSelectedTab(getStringIndex(bean.mSwitchTabs, bean.value));
                } else {
                    Log.i(TAG, "mSwitchTabsResId zero");
                }
                // viewHolder.switchMultiButton.setSelectedTab(getStringIndex(bean.mSwitchTabs,bean.value));

                viewHolder.switchMultiButton.setOnSwitchListener(new SwitchMultiButton.OnSwitchListener() {
                    public SwitchMultiButton.OnSwitchListener setBean(String s) {
                        this.saveId = s;
                        return this;
                    }

                    private String saveId;

                    @Override
                    public void onSwitch(int position, String tabText) {
                        Log.i(TAG, "position " + position + " tabText " + tabText + " " + saveId);
                        SharedPreferences sharedPreferences = MyApi.getInstance().getDataApi().getSetting();
                        boolean ret = sharedPreferences.edit().putString(saveId, tabText).commit();
                        if (ret) EventBus.getDefault().post(new SettingDeviceEvent());
                        Log.i(TAG, "it is MULTI_SWITCH_ID " + bean.value + " " + position +saveId);
                    }
                }.setBean(bean.saveId));
            } else {
                Log.i(TAG, "switchMultiButton null");
            }
        } else {

            if (viewHolder.value != null) {
                viewHolder.value.setText(bean.value);
            }
            convertView.setOnClickListener(null);
            Log.i(TAG, "setOnClickListener null");
        }


        return convertView;
    }


    private int getStringIndex(String[] mSwitchTabs, String value) {
        if (mSwitchTabs == null || mSwitchTabs.length == 0)
            return 0;
        for (int i = 0; i < mSwitchTabs.length; i++) {
            if (mSwitchTabs[i].equals(value))
                return i;
        }
        return 0;
    }


    public void setData(List<ParameterSimpleAdapter.Bean> data) {
        this.data = data;
    }

    public List<ParameterSimpleAdapter.Bean> getData() {
        return data;
    }

    public void addData(ParameterSimpleAdapter.Bean bean) {
        if (data == null) data = new ArrayList<>();
        if (data.size() < MAX) ;
        {
            data.add(bean);
        }
    }

    public static int getLayoutId(String type) {
        if (SettingConfig.VALUE_TYPE.equals(type)) {
            return VALUE_ID;
        } else if (SettingConfig.MORE_TYPE.equals(type)) {
            return MORE_ID;
        } else if (SettingConfig.MULTI_SWITCH_TYPE.equals(type)) {
            return MULTI_SWITCH_ID;
        } else if (SettingConfig.SWITCH_TYPE.equals(type)) {
            return SWITCH_ID;
        } else if (SettingConfig.RESET_FACTORY_TYPE.equals(type)) {
            return FACTORY_RESET_ID;
        } else if (SettingConfig.LIST_VALUE_TYPE.equals(type)) {
            return LIST_VALUE_ID;
        }
        return 0;
    }

    /*
    *
    *    public final static String ph_selection_SAVE_ID = "param:pH:ph_selection";
    public final static String ph_resolution_SAVE_ID = "param:pH:ph_resolution";
    public final static String  = "param:COND:ReferenceTemperature";
    public final static String  = "param:COND:TemperatureCoefficient";
    public final static String  = "param:pH:DueCalibration";
    public final static String  = "param:COND:DueCalibration";
    public final static String  = "param:COND:FactoryReset";
    public final static String  = "param:pH:FactoryReset";
    public final static String  = "param:TDS:TDSFactor";
    public final static String  = "param:Salinity:SaltType";
    public final static String salt_unit_SAVE_ID = "param:Salinity:salt_unit";
    *
    * */

    public static int getTitleId(String saveId) {
        if (SettingConfig.ph_selection_SAVE_ID.equals(saveId)) {
            return R.string.buffer_class_selection;//"Buffer Class Selection"
        } else if (SettingConfig.ph_resolution_SAVE_ID.equals(saveId)) {
            return R.string.resolution;// "Resolution"
        } else if (SettingConfig.ReferenceTemperature_SAVE_ID.equals(saveId)) {
            return R.string.ref_temperature;//"Reference Temperature"
        } else if (SettingConfig.TemperatureCoefficient_SAVE_ID.equals(saveId)) {
            return R.string.temp_coeff;//"Temperature Coefficient"
        } else if (SettingConfig.PH_DueCalibration_SAVE_ID.equals(saveId)) {
            return R.string.due_calib;//"Due Calibration"
        } else if (SettingConfig.COND_DueCalibration_SAVE_ID.equals(saveId)) {
            return R.string.due_calib;//"Due Calibration"
        } else if (SettingConfig.COND_FactoryReset_SAVE_ID.equals(saveId)) {
            return R.string.factory_reset_default;//"Reset to Factory Default"
        } else if (PH_FactoryReset_SAVE_ID.equals(saveId)) {
            return R.string.factory_reset_default;//"Reset to Factory Default";
        } else if (SettingConfig.TDSFactor_SAVE_ID.equals(saveId)) {
            return R.string.tds_factor;//"TDS Factor";
        } else if (SettingConfig.SaltType_SAVE_ID.equals(saveId)) {
            return R.string.salt_type;//"Salt Type";
        } else if (SettingConfig.salt_unit_SAVE_ID.equals(saveId)) {
            return R.string.salt_unit;//"Salt Unit";
        }
        return 0;
    }


    public void clear() {
        if (data != null) {
            data.clear();
        }
    }

    public static class Bean {
        public int sn;
        public String title;
        public String value;
        public String type;
        public int layoutId;
        public int mSwitchTabsResId;
        public String mSwitchTabs[];
        public String saveId;

        @Override
        public String toString() {
            return "Bean{" +
                    "sn=" + sn +
                    ", title='" + title + '\'' +
                    ", value='" + value + '\'' +
                    ", type='" + type + '\'' +
                    ", layoutId=" + layoutId +
                    ", mSwitchTabsResId=" + mSwitchTabsResId +
                    ", mSwitchTabs=" + Arrays.toString(mSwitchTabs) +
                    ", saveId='" + saveId + '\'' +
                    '}';
        }
    }

    public static class ViewHolder {
        int layoutId;
        public TextView title;
        public TextView value;
        public Switch aSwitch;
        public lib.kingja.switchbutton.SwitchMultiButton switchMultiButton;

    }

    private void showList(Bean bean) {
        mOptionDataValue1 = 0;
        mOptionDataValue2 = 0;
        pvOptions = null;
        if (bean.saveId.equals(SettingConfig.ReferenceTemperature_SAVE_ID)) {
            mOptionDataType = 1;
            if (bean.value != null) {
                try {
                    mOptionDataValue2 = 0;// getString(R.string.temp_degree_c);
                    mOptionDataValue1 =
                            Integer.parseInt(
                                    bean.value.replace(getString(R.string.temp_degree_c), "")
                            ) - 15;
                } catch (Exception e) {
                    mOptionDataValue1 = 0;
                    mOptionDataValue2 = 0;
                }
            }

        } else if (bean.saveId.equals(SettingConfig.TemperatureCoefficient_SAVE_ID)) {
            mOptionDataType = 2;
            if (bean.value != null) {
                try {
                    String str[] = bean.value.split("\\.");
                    if (str != null && str.length >= 2) {
                        mOptionDataValue2 = Integer.parseInt(str[1].replace("%", ""));
                        mOptionDataValue1 = Integer.parseInt(str[0]);
                    }
                } catch (Exception e) {
                    mOptionDataValue1 = 0;
                    mOptionDataValue2 = 0;
                }
            }
        }  else if (bean.saveId.equals(SettingConfig.TDSFactor_SAVE_ID)) {
            mOptionDataType = 3;
            if (bean.value != null) {
                try {
                    mOptionDataValue2 = 0;// getString(R.string.temp_degree_c);
                    mOptionDataValue1 = (int) (Float.parseFloat( bean.value)*100-40);
                    //Integer.parseInt(   bean.value ) ;
                } catch (Exception e) {
                    mOptionDataValue1 = 0;
                    mOptionDataValue2 = 0;
                }
            }
        } else {
            return;
        }
        onShowPickerView();
    }

    private OptionsPickerView pvOptions;

    public void onShowPickerView() {
        if (pvOptions == null) {
            initOptionData();
            initOptionPicker();
        }
        pvOptions.show();
    }

    private ArrayList<TimeBean> options1Items = new ArrayList<>();

    private ArrayList<ArrayList<String>> options2Items = new ArrayList<>();


    private void initOptionData() {
        options2Items.clear();
        options1Items.clear();
        if (mOptionDataType == 1) {
            ArrayList<String> options2Items_01 = new ArrayList<>();
            options2Items_01.add(getString(R.string.temp_degree_c));
            mOptionPickerTitleId = R.string.ref_temperature;
            for (int i = 15; i <= 30; i++) {
                options1Items.add(new TimeBean(i, "" + (i), (i)));
                options2Items.add(options2Items_01);
            }
        } else if (mOptionDataType == 2) {
            ArrayList<String> options2Items_01 = new ArrayList<>();

            for (int i = 0; i <= 99; i++) {
                options2Items_01.add("." + i + "%");
            }
            // options2Items_01.add(getString(R.string.minutes));
            mOptionPickerTitleId = R.string.temp_coeff;
            for (int i = 0; i <= 9; i++) {
                options1Items.add(new TimeBean(i, "" + (i), (i)));
                options2Items.add(options2Items_01);
            }
        }else if (mOptionDataType == 3) {
            ArrayList<String> options2Items_01 = new ArrayList<>();
            options2Items_01.add("");
            mOptionPickerTitleId = R.string.tds_factor;
            for (int i = 0; i <= 60; i++) {
                options1Items.add(new TimeBean(i, String.format(Locale.US,"%.2f",(((float)(i+40))/100)), (i)));
                options2Items.add(options2Items_01);
            }
        }
    }


    private void initOptionPicker() {//条件选择器初始化

        /**
         * 注意 ：如果是三级联动的数据(省市区等)，请参照 JsonDataActivity 类里面的写法。
         */
        Log.i(TAG, "initOptionPicker " + mOptionDataValue1 + " " + mOptionDataValue2);
        int opt2 = mOptionDataValue2;//!=null&&TextUtils.isDigitsOnly(mOptionDataValue2)?Integer.parseInt(mOptionDataValue2):0;;// mTimer>60*1000?1:0;
        int opt1 = mOptionDataValue1;//!=null && TextUtils.isDigitsOnly(mOptionDataValue1)?Integer.parseInt(mOptionDataValue1):0;// (int) (mTimer/(1000*(opt2==1?60:1))) -1;
        if (opt1 >= options1Items.size()) opt1 = options1Items.size() - 1;
        pvOptions = new OptionsPickerView.Builder(getContext(), new OptionsPickerView.OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int options2, int options3, View v) {
                //返回的分别是三个级别的选中位置
                String tx = options1Items.get(options1).getPickerViewText()
                        + options2Items.get(options1).get(options2)
                        /* + options3Items.get(options1).get(options2).get(options3).getPickerViewText()*/;
                //btn_Options.setText(tx);

                updateOptins(tx);
                Log.i(TAG, tx);
                pvOptions = null;

            }
        })

                .setTitleText(getString(mOptionPickerTitleId))
                .setContentTextSize(20)//设置滚轮文字大小
                .setDividerColor(Color.LTGRAY)//设置分割线的颜色
                .setSelectOptions(opt1, opt2)//默认选中项
                .setBgColor(Color.WHITE)
                .setTitleBgColor(Color.WHITE)
                .setTitleColor(Color.BLACK)
                .setCancelColor(Color.WHITE)
                .setSubmitColor(getResources().getColor(R.color.colorPrimary))
                .setTextColorCenter(Color.LTGRAY)
                .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
                .setLabels("", "", "").setCancelText(getString(R.string.cancel))
                .setBackgroundId(0x66000000) //设置外部遮罩颜色
                .build();

        pvOptions.setPicker(options1Items, options2Items);//二级选择器

    }

    private void updateOptins(String tx) {
        String saveId = "";
        if (mOptionDataType == 1) {
            saveId = SettingConfig.ReferenceTemperature_SAVE_ID;
        } else if (mOptionDataType == 2) {
            saveId = SettingConfig.TemperatureCoefficient_SAVE_ID;
        } else if (mOptionDataType == 3) {
            saveId = SettingConfig.TDSFactor_SAVE_ID;
        }else {
            return;
        }
        SharedPreferences sharedPreferences = MyApi.getInstance().getDataApi().getSetting();

        boolean ret = sharedPreferences.edit().putString(saveId, tx).commit();
        if (ret) {
            EventBus.getDefault().post(new SettingDeviceEvent());
            notifyDataSetChanged();
        }
    }

    private Context getContext() {
        return mContext;
    }

    private String getString(int id) {
        return mContext.getString(id);
    }

    private Resources getResources() {
        return mContext.getResources();
    }

}
