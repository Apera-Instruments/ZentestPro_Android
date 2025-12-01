package com.zen.api.data


import com.zen.api.protocol.CalibrationCond
import com.zen.api.protocol.CalibrationPh
import com.zen.api.protocol.Data

class DataBean {
    // -------------------------------------------------------------
    // ENUMS for per-block measurement mode
    // -------------------------------------------------------------
    enum class PhMode { PH, MV }
    enum class CondMode { COND, TDS, SAL, RES }
    enum class OrpMode { ORP }

    // -------------------------------------------------------------
    // Selection flags (for UI: selected/unselected)
    // -------------------------------------------------------------
    var isPhSelected: Boolean = true
    var isCondSelected: Boolean = true
    var isOrpSelected: Boolean = true

    // -------------------------------------------------------------
    // Mode fields
    // -------------------------------------------------------------
    var phMode: PhMode = PhMode.PH
    var condMode: CondMode = CondMode.COND
    var orpMode: OrpMode = OrpMode.ORP

    var id: Long? = null

    var devId: String? = null
    var noteId: String? = null
    var userId: String? = null
    var locationId: String? = null
    var traceNo: String? = null

    var sn: Int = 0
    var type: Int = 0//mode
    //主显示	指针显示
    //测量范围	单位
    var ph: Double = 0.toDouble()  //ph
    //	pH/温度	0~7~14	 pH
    var orp: Double = 0.toDouble()  //mV
    //ORP/温度	-1000~0~1000	mV
    var ec: Double = 0.toDouble()//电导率  μS/cm
    //电导率/温度	0~200	μS/cm
    //0~2000	μS/cm
    //0~20	mS/cm
    var tds: Double = 0.toDouble()//ppm
    //TDS/温度	0~1000	ppm
    //0~10	ppt
    var salinity: Double = 0.toDouble()//盐度 ppt
    //盐度/温度	盐度：0~10	ppt
    // 海水盐度：0~50	ppt
    //NaCl盐度：0~100	ppt
    var resistivity: Double = 0.toDouble()//电阻率 Ω·cm
    //电阻率/温度	0~100	Ω·cm
    //1~1000	KΩ·cm
    //1~20	MΩ·cm

    var temp: Double = 0.toDouble() //温度 .C

    var timestamp: Long = 0

    var longitude: String? = null//经度

    var latitude: String? = null //纬度


    var remarks: String? = null

    var uploadStatus: Int = 0
    var dataStatus: Int = 0

    var xInfo1: String? = null
    var xInfo2: String? = null
    var xInfo3: Int = 0
    var xInfo4: Int = 0


    var attach: String? = null

    var updateTime: Long = 0
    var createTime: Long = 0
    var mode: Int = 0
    var pointDigit: Int = 0
    var tempPointDigit: Int = 0
    var isH: Boolean = false
    var isL: Boolean = false
    var isM: Boolean = false
    var isHold: Boolean = false
    var isLaughFace: Boolean = false
    var isUpperAlarm: Boolean = false
    var isLowerAlarm: Boolean = false
    var calibrationPh: CalibrationPh? = null
    var calibrationCond: CalibrationCond? = null
    var data: Data? = null
    private var mReminder: Boolean = false
    private var value2: Double = 0.toDouble()
    var value: Double = 0.toDouble()
    var pointDigit2: Int = 0
    var unit2: Int = 0
    var unitString: String? = null




    override fun toString(): String {
        return "temp=$temp pH$ph"
    }

    fun hasReminder(): Boolean {
        return mReminder
    }

    fun setReminder(reminder: Boolean) {
        this.mReminder = reminder
    }

    fun setValue2(value2: Double) {
        this.value2 = value2
    }
}
