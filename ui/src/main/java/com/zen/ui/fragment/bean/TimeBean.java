package com.zen.ui.fragment.bean;

import com.bigkoo.pickerview.model.IPickerViewData;

public class TimeBean implements IPickerViewData {
    private long id;
    private String name;
    private int  value;


    public TimeBean(long id,String name,int value){
        this.id = id;
        this.name = name;
        this.value = value;

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }


    //这个用来显示在PickerView上面的字符串,PickerView会通过getPickerViewText方法获取字符串显示出来。
    @Override
    public String getPickerViewText() {
        return name;
    }
}