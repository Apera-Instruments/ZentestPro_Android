package com.zen.ui.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.zen.ui.HomeActivity
import com.zen.ui.R
import com.zen.ui.base.BaseFragment

import com.zen.api.MyApi
import kotlinx.android.synthetic.main.fragment_info.*
import kotlinx.android.synthetic.main.fragment_info.view.*
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by louis on 17-12-21. 信息页面
 */
class InfoFragment : BaseFragment() {



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_info, container, false)
        view.iv_menu.setOnClickListener{
            onMenu();
        }
        var dev = MyApi.getInstance().btApi.lastDevice;
        if(dev?.eleModle!=null)view.tv_ele_model.text=dev.eleModle
        view.tv_ins_model.text=dev?.modle
        view.tv_serial_num.text=dev?.sn
        var life =com.zen.api.MyApi.getInstance().btApi.getLife();

        if (life >= 0.93) {
            view.tv_life_value.text = getString(R.string.life_excellent);
        } else if (life >= 0.85) {
            view.tv_life_value.text = getString(R.string.life_good);
        } /*else if (life >= 0.85) {
            view.tv_life_value.text = getString(R.string.life_ok);
        }*/ else {
            view.tv_life_value.text = getString(R.string.life_replacement);
        }

        var version= MyApi.getInstance().btApi.deviceVersion?.version;
        if(TextUtils.isEmpty(version)){
            view.tv_version.text= "V 1.0";
        }else {
            view.tv_version.text= "V" + version;
        }
        if (dev != null) {
            var modle = dev.modle
            if(modle!=null) {
                if (modle.startsWith("PC60")) {
                    dev.measuringParameterTextId = R.string.info_measure1
                    dev.suitableAppTextId = R.string.suitable_app_str1
                    dev.compEleTextId = R.string.other_comp_ele_str1
                } else if (modle.startsWith("PH60S")) {
                    dev.measuringParameterTextId = R.string.info_measure3
                    dev.suitableAppTextId = R.string.suitable_app_str3
                    dev.compEleTextId = R.string.other_comp_ele_str3
                } else if (modle.startsWith("PH60F")) {
                    dev.measuringParameterTextId = R.string.info_measure4
                    dev.suitableAppTextId = R.string.suitable_app_str4
                    dev.compEleTextId = R.string.other_comp_ele_str4
                } else if (modle.startsWith("EC60")) {
                    dev.measuringParameterTextId = R.string.info_measure5
                    dev.suitableAppTextId = R.string.suitable_app_str5
                    dev.compEleTextId = R.string.other_comp_ele_str5
                    view.tv_life_value.text = "N/A"
                    view.other_comp_ele_title.setText(R.string.other_ele1)
                } else if (modle.startsWith("ORP60")) {
                    dev.measuringParameterTextId = R.string.info_measure6
                    dev.suitableAppTextId = R.string.suitable_app_str6
                    dev.compEleTextId = R.string.other_comp_ele_str6
                    view.tv_life_value.text = "N/A"
                    view.other_comp_ele_title.setText(R.string.other_ele1)
                } else if (modle.startsWith("PH60")) {
                    dev.measuringParameterTextId = R.string.info_measure2
                    dev.suitableAppTextId = R.string.suitable_app_str2
                    dev.compEleTextId = R.string.other_comp_ele_str2
                } else if (modle.startsWith("UWS_Waterboy")) {
                    dev.measuringParameterTextId = R.string.info_measure1
                    dev.suitableAppTextId = R.string.suitable_app_str1
                    dev.compEleTextId = R.string.other_comp_ele_str1
                }
            }
            if (dev.measuringParameterTextId > 0
                    && dev.suitableAppTextId > 0
                    && dev.compEleTextId > 0) {
                view.measuring_parameters_text.text = getString(dev.measuringParameterTextId)
                view.suitable_app_text.text = getString(dev.suitableAppTextId)
                view.other_comp_ele_text.text = getString(dev.compEleTextId)
            }
        }
        view.tv_cal_date.text=getDataString(dev?.setting?.getCalibrationDate())
        return view
    }

    private fun getDataString(date: Date?): String {
        if(date==null) return ""

        val simpleDateFormat = SimpleDateFormat("yyyy/MM/dd a hh:mm", Locale.US)
        return simpleDateFormat.format(date)

    }


    fun onMenu() {
        (activity as HomeActivity).showMenu()
    }

    override fun onResume() {
        super.onResume()
        updateView()
        //   API.getResourceApi().update(ResourceApi.HOT_VIEW, ResourceApi.APP_LIST);
    }

    override fun onDestroy() {

        super.onDestroy()

    }


    override fun updateView() {
        super.updateView()


    }


}