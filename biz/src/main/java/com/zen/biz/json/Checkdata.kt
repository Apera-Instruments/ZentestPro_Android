package com.zen.biz.json

import android.location.Address

class Checkdata {

    /*
    *

















listValue数集


temp	是	String	温度
tempType	是	String	温度单位
createtime	是	String	创建时间 ：yyyy-MM-dd HH:mm:ss

    *
    * */
    var id: String? = null
    var deviceNumber: String? = null// deviceNumber	是	String	设备标识
    var localid: String? = null//localid	是	String	app数据id
    var dataType: String? = null//dataType	是	String	数据类型
    var showType: String? = null//    showType	是	String	数据展示类型
    //calibrationValue1	是	String	值1
    //calibrationValue2	是	String	值2
    //calibrationValue3	是	String	值3
    var value: String? = null
    var listValue: MutableList<Data>? = mutableListOf()
    var valueType: String? = null//valueType	是	String	测量值单位
    var tempType: String? = null//tempType	是	String	温度单位
    var temp: String? = null//temp	是	int	温度
    var potential: String? = null//potential	是	String	零电位
    var slop: String? = null//slop	是	String
    var tempMode: String? = null//tempMode	是	String
    var createtime: String? = null//createtime	是	String	创建时间 ：yyyy-MM-dd HH:mm:ss
    var location: String? = null//location	否	String
    var noteName: String? = null//noteName	否	String
    var operator: String? = null//operator	否	String	操作者
    var notes: String? = null//notes	否	string	说明
    var category: String? = null//category	否	String	种类
    var Address: String? = null

    class Data{
        var value: String? = null	//是	String value	是	String
        var valueType: String? = null	//是	String	测量值单位
        var temp: String? = null//	是	String	温度
        var tempType: String? = null	//是	String	温度单位
        var createtime: String? = null	//是	String	创建时间 ：yyyy-MM-dd HH:mm:ss

    }
}