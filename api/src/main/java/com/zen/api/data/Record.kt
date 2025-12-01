package com.zen.api.data

import android.location.Address

class Record {
    var id: Long? = null
    var dataBeanList: MutableList<DataBean> ?= mutableListOf();
    var pic: String? = null
    var noteId: String? = null
    var createTime: Long = 0
    var value: Double = 0.toDouble()
    var type: Int = 0
    var tabType: Int = 0
    var traceNo: String? = null
    var sync: Boolean = false
    var deviceNumber: String? =null
    var tempUnit: String?=null
    var tempValue: String?=null
    var potential: String?=null

    var slop: String? = null
    var location: String? = null
    var noteName: String? = null
    var operator: String? = null
    var notes: String? = null
    var category: String? = null
    var syncId: String?=null
    var userId: String?=null
    var useState: Boolean?=true
    var calibration:String?=null
    var valueUnit:String?=null
}
