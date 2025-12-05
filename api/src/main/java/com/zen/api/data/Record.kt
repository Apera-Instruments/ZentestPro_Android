package com.zen.api.data

class Record {
    var id: Long? = null

    // Measurement curve (time-series)
    var dataBeanList: MutableList<DataBean>? = mutableListOf()

    // Main (summary) fields
    var pic: String? = null
    var noteId: String? = null
    var createTime: Long = 0
    var value: Double = 0.0
    var type: Int = 0
    var tabType: Int = 0
    var traceNo: String? = null
    var sync: Boolean = false

    // Device info
    var deviceNumber: String? = null
    var tempUnit: String? = null
    var tempValue: String? = null
    var potential: String? = null

    // Additional metadata
    var slop: String? = null
    var location: String? = null
    var noteName: String? = null
    var operator: String? = null
    var notes: String? = null
    var category: String? = null
    var syncId: String? = null
    var userId: String? = null
    var useState: Boolean? = true
    var calibration: String? = null
    var valueUnit: String? = null

    // Numeric measured values (for record summary table)
    var phValue: Double? = null
    var condValue: Double? = null
    var orpValue: Double? = null

    // Units of above values
    var pHUnit: String? = null
    var condUnit: String? = null
    var orpUnit: String? = null
}