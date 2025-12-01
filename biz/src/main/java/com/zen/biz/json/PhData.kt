package com.zen.biz.json

class PhData {
    var id: String? = null
    var deviceNumber: String? = null
    var localid: String? = null
    var dataType: String? = null
    var showType: String? = null
    var value: String? = null
    var listValue: MutableList<Data>? = mutableListOf()
    var valueType: String? = null
    var tempType: String? = null
    var temp: String? = null
    var potential: String? = null
    var slop: String? = null
    var tempMode: String? = null
    var createtime: String? = null
    var location: String? = null
    var noteName: String? = null
    var operator: String? = null
    var notes: String? = null
    var category: String? = null


    class Data{
        var value: String? = null	//是	String
        var valueType: String? = null	//是	String	测量值单位
        var temp: String? = null//	是	String	温度
        var tempType: String? = null	//是	String	温度单位
        var createtime: String? = null	//是	String	创建时间 ：yyyy-MM-dd HH:mm:ss

    }
}