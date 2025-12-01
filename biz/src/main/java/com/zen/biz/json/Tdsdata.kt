package com.zen.biz.json

class Tdsdata {
    /*
    * deviceNumber	是	String	设备标识
localid	是	String	app数据id
dataType	是	String	数据类型
showType	是	String	数据展示类型
valueType	是	String	测量值单位
value	是	String	ORP值
tempType	是	String	温度单位
temp	是	int	温度
tempMode	是	String
createtime	是	String	创建时间 ：yyyy-MM-dd HH:mm:ss
location	否	String
noteName	否	String
operator	否	String	操作者
notes	否	string	说明
category	否	String	种类
listValue数集
value	是	String
valueType	是	String	测量值单位
temp	是	String	温度
tempType	是	String	温度单位
createtime	是	String	创建时间 ：yyyy-MM-dd HH:mm:ss

    *
    * */
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