package com.zen.biz

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import android.text.format.DateUtils

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.zen.api.MyApi
import com.zen.api.data.Record
import com.zen.biz.greendao.gen.UserDao
import com.zen.biz.rest.RestUtil
import com.zen.biz.table.User
import com.zen.biz.utils.AndroidDes3Util

import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.HashMap
import java.util.Random
import java.util.concurrent.atomic.AtomicInteger


import retrofit2.Response

import com.zen.api.Constant
import com.zen.api.RestApi
import com.zen.biz.json.*
import android.icu.util.ULocale.getCountry
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.alibaba.fastjson.util.TypeUtils
import com.zen.api.data.DataBean
import com.zen.api.protocol.Data


class RestApiImpl(context: Context, url: String) : RestUtil(context, url) {



    private val random = Random()
    private val atomicInteger = AtomicInteger()

    private val traceNo: String
        get() = Integer.toHexString(hashCode()) + java.lang.Long.toHexString(System.currentTimeMillis()) + Integer.toHexString(random.nextInt(1000)) + Integer.toHexString(atomicInteger.addAndGet(1))

    private//token="17A9B47947044120941C9187E7F83F7C";
            //I4470/4tyeBW28X83AitO3e0e7xZIpiQCTKPTrzrcf8vgUb2PDCytg==
    val defaultMap: MutableMap<String, Any>
        get() {
            val map = HashMap<String, Any>()
            val formatter1 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val timestamp = formatter1.format(Date())
            val traceNo = traceNo
            val userId = this.userId

            val sign = AndroidDes3Util.encode( secretKey,"01234567",token)
            map.put("sign", sign)
            map.put("userId", userId)
            map.put("traceNo", traceNo)
            map.put("timestamp", timestamp)
            return map
        }

    override fun load() {
        getUser()
        val sharedPreferences = Install.getInstance().context.getSharedPreferences("setting_v1", Context.MODE_PRIVATE)
        userName = sharedPreferences.getString("userName",userName)
    }

    private var errMsg: String?=null

    override fun getLastErrMessage():String?{
        return errMsg;
    }

    override fun register(userName: String, userPassword: String,firstName: String,lastName: String): Int {
        try {
            val respondResponse = service.register(userName, userPassword,firstName ,lastName).execute()
            if (respondResponse.isSuccessful) {
                val respond = respondResponse.body()
                if (respond != null && CODE_SUCCESS == respond.code) {
                    return RestApi.SUCCESS

                }

                errMsg = respond?.msg;
            }
        } catch (e: Exception) {
            errMsg = e.localizedMessage
        }

        return RestApi.FAIL
    }

    private var mLocaleType: String?="en";//zh

    override fun acquireCode(userName: String): Int {
        try {
            val type =mLocaleType;//"en";

            val respondResponse = service.acquireCode(userName,type).execute()
            if (respondResponse.isSuccessful) {
                val respond = respondResponse.body()
                if (respond != null && CODE_SUCCESS == respond.code) {

                    return RestApi.SUCCESS

                }
                errMsg = respond?.msg;
                if("账号不存在！".equals(errMsg)) return ACCOUNT_NOT_FOUND_FAIL
            }
        } catch (e: Exception) {
            errMsg = e.localizedMessage
        }

        return RestApi.FAIL
    }

    override fun update(userName: String, type: Int, oldPassword: String, newPassword: String, authCode: String) {

    }

    override fun login(userName: String, userPassword: String): Int {
        try {
            val respondResponse = service.login(userName, userPassword).execute()

            if (respondResponse.isSuccessful) {
                val respond = respondResponse.body()
                if (respond!=null && CODE_SUCCESS == respond.code && respond.data != null) {
                    //{"msg":"登录成功！","code":"0000","data":{"userId":"2c90fa796242631101627f54d765000a","token":"E6D5BD21C2794B86B381A0985DAF400E"}}
                    //com.alibaba.fastjson.JSONObject
                    userId = respond.data.getString("userId")
                    token = respond.data.getString("token")

                    val editor = Install.getInstance().context.getSharedPreferences("AssectList", AppCompatActivity.MODE_PRIVATE).edit() as SharedPreferences.Editor
                    editor.putString("AdminName", respond.data.getString("lastName"));
                    editor.commit()

                    this.userName = userName
                    saveLogin()
                    return RestApi.SUCCESS

                }
                errMsg = respond?.msg;
            }
        } catch (e: Exception) {
            errMsg = e.localizedMessage
        } catch (e: Exception) {
            errMsg = e.localizedMessage
        }

        return RestApi.FAIL
    }

    private fun saveLogin() {
        val userDao = Install.getInstance().daoSession.userDao
        var user: User
        var update = false
        val list = userDao.queryBuilder().where(UserDao.Properties.UserId.eq(userId)).build().list()
        if (list != null && list.size > 0) {
            user = list[0]
            update = true
        } else {
            user = User()
            user.userId = userId
            user.createTime = System.currentTimeMillis()
        }
        user.token = token
        user.email = userName
        user.currentUser = true
        user.userStatus = RestApi.USER_LOGIN
        user.updateTime = System.currentTimeMillis()
        if (update) {
            userDao.update(user)
        } else {
            userDao.insert(user)
        }
        val sharedPreferences = Install.getInstance().context.getSharedPreferences("setting_v1", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("userName",userName).apply()
    }


    private fun getUser() {
        val userDao = Install.getInstance().daoSession.userDao
        var user: User

        val list = userDao.queryBuilder().where(UserDao.Properties.CurrentUser.eq(true), UserDao.Properties.UserStatus.eq(RestApi.USER_LOGIN)).build().list()
        if (list != null && list.size > 0) {
            user = list[0]
            userId = user.userId
            token = user.token
            userName = user.email
        } else {
            userId = null
            token = null
        }


    }

    override fun setLogout() {
        val userDao = Install.getInstance().daoSession.userDao
        var user: User? = null
        var update = false
        if (userId != null) {
            val list = userDao.queryBuilder().where(UserDao.Properties.UserId.eq(userId)).build().list()
            if (list != null && list.size > 0) {
                user = list[0]
                update = true
            }
            if (update) {
                user!!.token = null
                user.currentUser = false
                user.userStatus = RestApi.USER_LOGOUT
                user.updateTime = System.currentTimeMillis()
                userDao.update(user)
            }
        }

        super.setLogout()
    }

    override fun updateLoginTime(): Int {
        try {
            val formatter1 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val timestamp = formatter1.format(Date())
            val traceNo = traceNo
            val userId = this.userId
            //token="17A9B47947044120941C9187E7F83F7C";
            //I4470/4tyeBW28X83AitO3e0e7xZIpiQCTKPTrzrcf8vgUb2PDCytg==
            val sign = AndroidDes3Util.encode( RestUtil.secretKey,"01234567",token)

            val respondResponse = service.updateLoginTime(userId, sign, traceNo, timestamp).execute()
            if (respondResponse.isSuccessful) {
                val respond = respondResponse.body()
                if (respond != null && CODE_SUCCESS == respond.code) {

                    return RestApi.SUCCESS

                }else if(respond?.code ==CODE_LOGOUT){
                    setLogout();
                }
                errMsg = respond?.msg;
                // {"msg":"token验证失败！请重新登录","code":"1001"}
            }
        } catch (e: Exception) {
            errMsg = e.localizedMessage
        }

        return RestApi.FAIL
    }

    override fun changePassword(userName: String?, oldPassword: String?, newPassword: String?, code: String?, type: Int): Int {
        try {
            val respondResponse = service.update(userName, type, oldPassword, newPassword, code).execute()
            if (respondResponse.isSuccessful) {
                val respond = respondResponse.body()
                if (respond != null && CODE_SUCCESS == respond.code) {

                    return RestApi.SUCCESS

                }
                errMsg = respond?.msg;
            }
        } catch (e: Exception) {
            errMsg = e.localizedMessage
        }

        return RestApi.FAIL
    }

    override fun verificationCode(userName: String, code: String): Int {
        try {
            val respondResponse = service.verificationCode(userName, code).execute()
            if (respondResponse.isSuccessful) {
                val respond = respondResponse.body()
                if (respond != null && CODE_SUCCESS == respond.code) {

                    return RestApi.SUCCESS

                }
                errMsg = respond?.msg;
            }
        } catch (e: Exception) {
            errMsg = e.localizedMessage
        }

        return RestApi.FAIL
    }

     fun format(date: Date): String {
        val formatter1 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return formatter1.format(date)
    }

    fun formatParse(time: String): Long {
        try {
            val formatter1 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            return formatter1.parse(time).time
        }catch (err:java.lang.Exception){
         err.printStackTrace()
        }
        return 0
    }

    fun format(date: String?): Date {
        val formatter1 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return formatter1.parse(date)
    }

    override fun downloadList(): Int {
        try {
            if (!isLogin) {
                return RestApi.FAIL
            }
            var dataApi = MyApi.getInstance().dataApi;//.updateRecords()
            //for (i in 1 .. 6)
            //{

                var index = 1;
                val pageSize = 100;
                do {

                    val map = defaultMap
                    val page = Page()
                    page.page = index.toString()
                    page.pageSize = pageSize.toString()
                    val timestamp = format(Date(System.currentTimeMillis() - DateUtils.YEAR_IN_MILLIS))
                    page.updatetime = timestamp
                    map.put("buzParam", JSON.toJSONString(page))

                    val respondResponse: Response<ServiceRespond>
                    try {
                        respondResponse= service.phdataList(map).execute()
                        /*respondResponse = when (i) {
                            Constant.MODE_PH -> service.phdataList(map).execute()
                            Constant.MODE_COND -> service.phdataList(map).execute()
                            Constant.MODE_ORP -> service.phdataList(map).execute()
                            Constant.MODE_RES -> service.phdataList(map).execute()
                            Constant.MODE_SAL -> service.phdataList(map).execute()
                            Constant.MODE_TDS -> service.phdataList(map).execute()
                            else -> {
                                service.phdataList(map).execute()
                            }
                        }*/

                        if (respondResponse == null) {
                            break
                        }
                    } catch (e: Exception) {
                        break
                    }

                    if (respondResponse.isSuccessful) {
                        val respond = respondResponse.body()
                        if (respond != null && CODE_SUCCESS == respond.code) {

                            // {"msg":"查询成功！","code":"0000","data":{"pages":1,"size":2,"body":[
                            // {"id":"2c90fa79628be16c016295315dc70001",
                            // "userId":"2c90fa796242631101627f54d765000a",
                            // "userName":"lin@linxinhua.cn",
                            // "deviceNumber":null,
                            // "localid":"20183705173732d9a2235",
                            // "dataType":"0","showType":"0",
                            // "value":"7.14","listValue":"[]",
                            // "valueType":"1","tempType":null,
                            // "temp":null,"potential":null,"slop":null,
                            // "tempMode":null,"state":1,"createtime":"2018-04-05 17:37:33",
                            // "updatetime":"2018-04-05 17:44:23","location":null,"noteName":null,
                            // "operator":null,"notes":null,"category":null},
                            // {"id":"2c90fa79628be16c01629579cab30002","userId":"2c90fa796242631101627f54d765000a","userName":"lin@linxinhua.cn","deviceNumber":null,"localid":"20180305190328e26866a","dataType":"0","showType":"0","value":"1.73","listValue":"[]","valueType":"1","tempType":null,"temp":null,"potential":null,"slop":null,"tempMode":null,"state":1,"createtime":"2018-04-05 19:03:29","updatetime":"2018-04-05 19:03:29","location":null,"noteName":null,"operator":null,"notes":null,"category":null}],"userId":"2c90fa796242631101627f54d765000a","token":"5FB3E4F9E16F4ACFA556CEDC381C1E87"}}
                            if (respond.data is com.alibaba.fastjson.JSONObject) {
                                var body = respond.data.getJSONArray("body")
                                if (body.size == 0) {
                                    break
                                }
                                body?.forEach { value ->

                                    var jsonObject = value as JSONObject?
                                    var updateValue = Record()
                                    updateValue.syncId = jsonObject?.getString("id");
                                    updateValue.userId = jsonObject?.getString("userId");
                                    //updateValue.userName = jsonObject?.getString("userName");
                                    updateValue.deviceNumber = jsonObject?.getString("deviceNumber");
                                    updateValue.traceNo = jsonObject?.getString("localid");
                                    // updateValue.tabType = conventDataType2TabType(jsonObject?.getString("dataType"));

                                    var showType = jsonObject?.getString("showType");
                                    updateValue.tabType = conventTabType(showType)
                                    var value = jsonObject?.getDoubleValue("value");

                                    if (value is Double) updateValue.value = value;
                                    var valueType = jsonObject?.getString("valueType");
                                    var dataType = jsonObject?.getString("dataType");
                                    updateValue.type = conventModeType(dataType);
                                    //updateValue.valueUnit =conventModeTypeUnit( updateValue.type)
                                    updateValue.valueUnit  = if(isUnit(valueType)  ) valueType  else conventModeTypeUnit( updateValue.type)
                                    updateValue.tempValue = jsonObject?.getString("temp");
                                    //updateValue.tempUnit = jsonObject?.getString("tempMode");
                                    var tempType = jsonObject?.getString("tempType");

                                    updateValue.tempUnit =  if(tempType=="F" || tempType=="oF" || tempType=="℉" || tempType=="ºF") "F" else "C"
                                    var createTime = jsonObject?.getString("createtime");
                                    updateValue.createTime = format(createTime).time;
                                    //updateValue.createtime = jsonObject?.getString("createtime");
                                    updateValue.operator = jsonObject?.getString("operator");
                                    updateValue.potential = jsonObject?.getString("potential");
                                    updateValue.location = jsonObject?.getString("location");
                                    updateValue.noteName = jsonObject?.getString("noteName");
                                    updateValue.slop = jsonObject?.getString("slop");
                                    updateValue.notes = jsonObject?.getString("notes");
                                    updateValue.category = jsonObject?.getString("category");
                                    updateValue.sync = true
                                    if(jsonObject?.getJSONArray("listValue")!=null){
                                        var jsonArray = jsonObject.getJSONArray("listValue")
                                        jsonArray.forEach { d ->
                                            var bean = DataBean()
                                            var value = d as JSONObject
                                            bean.createTime =  formatParse(value.getString("createtime"))
                                            bean.temp = value.getDouble("temp")
                                            var tempType = value.getString("tempType")
                                            if(tempType!=null &&tempType!="" && TextUtils.isDigitsOnly(tempType)) {
                                                bean.unit2 =Integer.parseInt(tempType)
                                            }else {
                                                bean.unit2 = if(tempType=="F" || tempType=="oF" || tempType=="℉" || tempType=="ºF")  Data.UNIT_F else   Data.UNIT_C
                                            }
                                            var v = if( value.containsKey("value") && value.get("value")!=null ) value.getDouble("value") else  0.toDouble()

                                            bean.value = v
                                            //tempUnit

                                            bean.unitString = value.getString("valueType")
                                            updateValue.dataBeanList?.add(bean)
                                        }

                                    }
                                    dataApi.insertIfNoneRecords(updateValue)
                                }
                                val pages = respond.data.getIntValue("pages")
                                val size = respond.data.getIntValue("size")
                                if (index >= pages || (index) * pageSize >= size) {

                                    break
                                }

                            } else {
                            }
                            // return RestApi.SUCCESS
                            index++
                            continue

                        } else {
                            break
                        }
                    } else {
                        break
                    }

                } while (index < 100)
            //}


        } catch (e: Exception) {
        }


        return RestApi.SUCCESS
    }

    override fun phdataList(): Int {
        return RestApi.SUCCESS
    }
//@[@"simple",@"dial",@"graph",@"table"]
    private fun conventTabType(showType: String?): Int {
        return when {
            TextUtils.isDigitsOnly(showType) -> Integer.parseInt(showType)
            showType == "simple" -> 0
            showType == "dial" -> 1
            showType == "graph" -> 2
            showType == "table" -> 3
            else -> 0
        }
    }

    private fun conventTabType(tabType: Int?): String {
        return when  {
            null==tabType -> "simple"
            tabType==0 -> "simple"
            tabType==1 -> "dial"
            tabType==2 -> "graph"
            tabType==3 -> "table"
            else -> {
                tabType.toString()
            }
        }
    }

    private fun conventModeType(type: String?): Int {
        return when {
            TextUtils.isDigitsOnly(type) -> Integer.parseInt(type)
            type == Constant.STR_MODE_PH -> Constant.MODE_PH
            type == Constant.STR_MODE_pH -> Constant.MODE_PH
            type == Constant.STR_MODE_SAL -> Constant.MODE_SAL
            type == Constant.STR_MODE_COND -> Constant.MODE_COND
            type == Constant.STR_MODE_ORP -> Constant.MODE_ORP
            type == Constant.STR_MODE_RES -> Constant.MODE_RES
            type == Constant.STR_MODE_TDS -> Constant.MODE_TDS
            else -> 0
        }
    }

    private fun isUnit(type: String?): Boolean {
        return when {
            type == Constant.STR_UNIT_PH -> true
            type == Constant.STR_UNIT_SAL -> true
            type == Constant.STR_UNIT_COND -> true
            type == Constant.STR_UNIT_ORP -> true
            type == Constant.STR_UNIT_RES -> true
            type == Constant.STR_UNIT_TDS -> true
            type == "MΩ•cm" -> true
            type == "Ω•cm" -> true
            type == "KΩ•cm" -> true
            type == "MΩ·cm" -> true
            type == "KΩ·cm" -> true
            type == "Ω·cm" -> true
            type == "g/L" -> true
            type == "mg/L" -> true
            type == "mS/cm" -> true
            type == "mV" -> true
            type == "µS/cm" -> true
            type == "μS/cm" -> true
            type == "μS" -> true
            type == "μS" -> true
            type == "Ω" -> true
            type == "KΩ" -> true
            type == "MΩ" -> true
            else -> false
        }
    }
    private fun conventModeTypeUnit(type: Int?): String {
        return when {
            type == Constant.MODE_PH -> Constant.STR_UNIT_PH
            type == Constant.MODE_SAL -> Constant.STR_UNIT_SAL
            type == Constant.MODE_COND -> Constant.STR_UNIT_COND
            type == Constant.MODE_ORP -> Constant.STR_UNIT_ORP
            type == Constant.MODE_RES -> Constant.STR_UNIT_RES
            type == Constant.MODE_TDS -> Constant.STR_UNIT_TDS
            type == 0 -> ""
            else -> type.toString()
        }
    }

    private fun conventModeType(type: Int?): String {
        return when {
            type == Constant.MODE_PH -> Constant.STR_MODE_PH
            type == Constant.MODE_SAL -> Constant.STR_MODE_SAL
            type == Constant.MODE_COND -> Constant.STR_MODE_COND
            type == Constant.MODE_ORP -> Constant.STR_MODE_ORP
            type == Constant.MODE_RES -> Constant.STR_MODE_RES
            type == Constant.MODE_TDS -> Constant.STR_MODE_TDS
            else -> type.toString()
        }
    }

    override fun syncUpload(): Int {
        val list = MyApi.getInstance().dataApi.getSyncRecords(false);
        list?.forEach { value ->
            if (value.useState == false && value.id != null && value.syncId != null) {
                var ret = dataDelete(value.syncId, value.type)
                if (ret == RestApi.SUCCESS) {

                    MyApi.getInstance().dataApi.delRecord(value.id!!);
                }

            } else if (!value.sync) {
                var ret = dataUploadAdd(value)
                if (ret == RestApi.SUCCESS) {
                    value.sync = true;
                    MyApi.getInstance().dataApi.updateRecordsSync(value);
                }
            }
        }
        return RestApi.SUCCESS;
    }

    fun conventRecord(record: Record): String? {

        var list = record.dataBeanList;
        if (record.type == Constant.MODE_PH) {
            val data = PhData()
            val ls: List<PhData>
            //Conducitivitydata

            data.value = record.value.toString();
            data.createtime = format(Date(record.createTime))
            data.deviceNumber = record.deviceNumber
            data.localid = record.traceNo
            data.dataType ="PH" ;//conventTabType(record.tabType)
            data.showType = conventTabType(record.tabType)
            data.valueType = record.valueUnit//conventModeTypeUnit(record.type)
            data.tempType = record.tempUnit
            data.temp = record.tempValue
            data.potential = record.potential
            data.slop = record.slop
            data.tempMode = "ATC"//record.tempUnit
            data.location = record.location
            data.noteName = record.noteName
            data.operator = record.operator
            data.notes = record.notes
            data.category = record.operator
            if(record.syncId !=null) data.id=record.syncId


            list?.forEach { value ->
                val d = PhData.Data();
                d.createtime = format(Date(value.createTime))
                d.temp = value.temp.toString();
                d.tempType = if(value.unit2== Data.UNIT_C) "C" else  "F"
                d.valueType = value.unitString;
                d.value = value.value.toString();
                data.listValue!!.add(d)
            }
            ls = listOf(data)

            return JSON.toJSONString(ls)
        } else if (record.type == Constant.MODE_SAL) {
            val data = Salinitydata()
            val ls: List<Salinitydata>


            data.value = record.value.toString();
            data.createtime = format(Date(record.createTime))
            data.deviceNumber = record.deviceNumber
            data.localid = record.traceNo
            data.dataType = "Salinity" ;//conventTabType(record.tabType)
            data.showType = conventTabType(record.tabType)
            data.valueType = record.valueUnit//conventModeTypeUnit(record.type)
            data.tempType = record.tempUnit
            data.temp = record.tempValue
            data.potential = record.potential
            data.slop = record.slop
            data.tempMode = "ATC"//record.tempUnit
            data.location = record.location
            data.noteName = record.noteName
            data.operator = record.operator
            data.notes = record.notes
            data.category = record.operator
            if(record.syncId !=null) data.id=record.syncId

            list?.forEach { value ->
                val d = Salinitydata.Data();
                d.createtime = format(Date(value.createTime))
                d.temp = value.temp.toString();
                d.tempType = if(value.unit2== Data.UNIT_C) "C" else  "F"
                d.valueType = value.unitString;
                d.value = value.value.toString();
                data.listValue!!.add(d)
            }
            ls = listOf(data)

            return JSON.toJSONString(ls)
        } else if (record.type == Constant.MODE_COND) {
            val data = Conducitivitydata()
            val ls: List<Conducitivitydata>


            data.value = record.value.toString();
            data.createtime = format(Date(record.createTime))
            data.deviceNumber = record.deviceNumber
            data.localid = record.traceNo
            data.dataType ="Conductivity" ;// conventTabType(record.tabType)
            data.showType = conventTabType(record.tabType)
            data.valueType = record.valueUnit//conventModeTypeUnit(record.type)
            data.tempType = record.tempUnit
            data.temp = record.tempValue
            data.potential = record.potential
            data.slop = record.slop
            data.tempMode = "ATC"//record.tempUnit
            data.location = record.location
            data.noteName = record.noteName
            data.operator = record.operator
            data.notes = record.notes
            data.category = record.operator
            if(record.syncId !=null) data.id=record.syncId

            list?.forEach { value ->
                val d = Conducitivitydata.Data();
                d.createtime = format(Date(value.createTime))
                d.temp = value.temp.toString();
                d.tempType = if(value.unit2== Data.UNIT_C) "C" else  "F"
                d.valueType = value.unitString;
                d.value = value.value.toString();
                data.listValue!!.add(d)
            }
            ls = listOf(data)

            return JSON.toJSONString(ls)
        } else if (record.type == Constant.MODE_ORP) {
            val data = Orpdata()
            val ls: List<Orpdata>


            data.value = record.value.toString();
            data.createtime = format(Date(record.createTime))
            data.deviceNumber = record.deviceNumber
            data.localid = record.traceNo
            data.dataType = "ORP" ;//conventTabType(record.tabType)
            data.showType = conventTabType(record.tabType)
            data.valueType = record.valueUnit//conventModeTypeUnit(record.type)
            data.tempType = record.tempUnit
            data.temp = record.tempValue
            data.potential = record.potential
            data.slop = record.slop
            data.tempMode = "ATC"//record.tempUnit
            data.location = record.location
            data.noteName = record.noteName
            data.operator = record.operator
            data.notes = record.notes
            data.category = record.operator
            if(record.syncId !=null) data.id=record.syncId

            list?.forEach { value ->
                val d = Orpdata.Data();
                d.createtime = format(Date(value.createTime))
                d.temp = value.temp.toString();
                d.tempType = if(value.unit2== Data.UNIT_C) "C" else  "F"
                d.valueType = value.unitString;
                d.value = value.value.toString();
                data.listValue!!.add(d)
            }
            ls = listOf(data)

            return JSON.toJSONString(ls)
        } else if (record.type == Constant.MODE_RES) {
            val data = Resistivitydata()
            val ls: List<Resistivitydata>


            data.value = record.value.toString();
            data.createtime = format(Date(record.createTime))
            data.deviceNumber = record.deviceNumber
            data.localid = record.traceNo
            data.dataType ="Resistivity" ;// conventTabType(record.tabType)
            data.showType = conventTabType(record.tabType)
            data.valueType = record.valueUnit//conventModeTypeUnit(record.type)
            data.tempType = record.tempUnit
            data.temp = record.tempValue
            data.potential = record.potential
            data.slop = record.slop
            data.tempMode = "ATC"//record.tempUnit
            data.location = record.location
            data.noteName = record.noteName
            data.operator = record.operator
            data.notes = record.notes
            data.category = record.operator
            if(record.syncId !=null) data.id=record.syncId

            list?.forEach { value ->
                val d = Resistivitydata.Data();
                d.createtime = format(Date(value.createTime))
                d.temp = value.temp.toString();
                d.tempType = if(value.unit2== Data.UNIT_C) "C" else  "F"
                d.valueType = value.unitString;
                d.value = value.value.toString();
                data.listValue!!.add(d)
            }
            ls = listOf(data)

            return JSON.toJSONString(ls)
        } else if (record.type == Constant.MODE_TDS) {
            val data = Tdsdata()
            val ls: List<Tdsdata>
            data.value = record.value.toString();
            data.createtime = format(Date(record.createTime))
            data.deviceNumber = record.deviceNumber
            data.localid = record.traceNo
            data.dataType = "TDS" ;//conventTabType(record.tabType)
            data.showType = conventTabType(record.tabType)
            data.valueType = record.valueUnit//conventModeTypeUnit(record.type)
            data.tempType = record.tempUnit
            data.temp = record.tempValue
            data.potential = record.potential
            data.slop = record.slop
            data.tempMode = "ATC"//record.tempUnit
            data.location = record.location
            data.noteName = record.noteName
            data.operator = record.operator
            data.notes = record.notes
            data.category = record.operator
            if(record.syncId !=null) data.id=record.syncId

            list?.forEach { value ->
                val d = Tdsdata.Data();
                d.createtime = format(Date(value.createTime))
                d.temp = value.temp.toString();
                d.tempType = if(value.unit2== Data.UNIT_C) "C" else  "F"
                d.valueType = value.unitString;
                d.value = value.value.toString();
                data.listValue!!.add(d)
            }
            ls = listOf(data)

            return JSON.toJSONString(ls)
        }
        return null
    }


    fun dataUploadAdd(record: Record): Int {

        try {

//            Log.d(JSON.toJSON(record).toString(),"++==saveBitmap");
            val map = defaultMap
            var buzParam = conventRecord(record);
            if (buzParam == null) {
                return RestApi.FAIL
            }
            map.put("buzParam", buzParam)
            val respondResponse: Response<ServiceRespond>
                    = when {
                record.type == Constant.MODE_PH -> service.phdataAdd(map).execute()
                record.type == Constant.MODE_SAL -> service.phdataAdd(map).execute()
                record.type == Constant.MODE_COND -> service.phdataAdd(map).execute()
                record.type == Constant.MODE_ORP -> service.phdataAdd(map).execute()
                record.type == Constant.MODE_RES -> service.phdataAdd(map).execute()
                record.type == Constant.MODE_TDS -> service.phdataAdd(map).execute()
                else -> service.phdataAdd(map).execute()
            }
            Log.v(respondResponse.body().toString(),"==saveBitmap")
            if (respondResponse.isSuccessful) {
                val respond = respondResponse.body()
                if (respond != null && CODE_SUCCESS == respond.code) {
// {"msg":"保存成功！","code":"0000","data":{"body":[{"id":"2c90fa79628be16c016295315dc70001","  userId":"2c90fa796242631101627f54d765000a","userName":"lin@linxinhua.cn","deviceNumber":null,"localid":"20183705173732d9a2235","dataType":"0","showType":"0","value":"7.14","listValue":"[]","valueType":"1","tempType":null,"temp":null,"potential":null,"slop":null,"tempMode":null,"state":1,"createtime":"2018-04-05 17:37:33","updatetime":"2018-04-05 17:44:23","location":null,"noteName":null,"operator":null,"notes":null,"category":null}],"userId":"2c90fa796242631101627f54d765000a","token":"288D3BEBDC75482F8BBD4A38A0A46E6C"}}
                    if (respond.data is com.alibaba.fastjson.JSONObject) {
                        var body = respond.data.getJSONArray("body");
                        record.syncId = body.getJSONObject(0).getString("id");
                    }
                    return RestApi.SUCCESS
                }
                errMsg = respond?.msg;
            }else{

            }
        } catch (e: Exception) {
            errMsg = e.localizedMessage
        }

        return RestApi.FAIL
    }

    override fun setLocalType(type: String){
        mLocaleType = type;
    }
    override fun dataDelete(id: String?,type:Int): Int {
        try {
            if (!isLogin) {
                return RestApi.FAIL
            }
            if(id==null)  return RestApi.FAIL
            val map = defaultMap
            val list = ArrayList<ObjectId>()
            list.add(ObjectId(id))
            map.put("buzParam", JSON.toJSONString(list))

            val respondResponse: Response<ServiceRespond>
            try {
                respondResponse = when (type) {
                    Constant.MODE_PH  -> service.phdataDelete(map).execute()
                    Constant.MODE_COND  -> service.phdataDelete(map).execute()
                    Constant.MODE_ORP  -> service.phdataDelete(map).execute()
                    Constant.MODE_RES  -> service.phdataDelete(map).execute()
                    Constant.MODE_SAL  -> service.phdataDelete(map).execute()
                    Constant.MODE_TDS  -> service.phdataDelete(map).execute()
                    else -> {
                        service.phdataDelete(map).execute()
                    }
                }


                if (respondResponse == null) {
                    return RestApi.FAIL
                }
                if (respondResponse.isSuccessful) {
                    val respond = respondResponse.body()
                    if (respond != null && CODE_SUCCESS == respond.code) {
                        return RestApi.SUCCESS
                    }
                    errMsg = respond?.msg;
                }
            } catch (e: Exception) {
                errMsg = e.localizedMessage

            }
           // val respondResponse = service.phdataDelete(map).execute()

        } catch (e: Exception) {
        }

        return RestApi.FAIL
       }

    override fun phdataDelete(id: String?): Int {
        try {
            if (!isLogin) {
                return RestApi.FAIL
            }
            if(id==null)  return RestApi.FAIL
            val map = defaultMap
            val list = ArrayList<ObjectId>()
            list.add(ObjectId(id))
            map.put("buzParam", JSON.toJSONString(list))

            val respondResponse = service.phdataDelete(map).execute()
            if (respondResponse.isSuccessful) {
                val respond = respondResponse.body()
                if (respond != null && CODE_SUCCESS == respond.code) {
                    return RestApi.SUCCESS
                }
                errMsg = respond?.msg;
            }
        } catch (e: Exception) {
            errMsg = e.localizedMessage
        }

        return RestApi.FAIL
    }

    override fun phdataDelete(): Int {
        try {
            if (!isLogin) {
                return RestApi.FAIL
            }
            val map = defaultMap

            val list = ArrayList<ObjectId>()
            list.add(ObjectId("1"))
            map.put("buzParam", JSON.toJSONString(list))

            val respondResponse = service.phdataDelete(map).execute()
            if (respondResponse.isSuccessful) {
                val respond = respondResponse.body()
                if (respond != null && CODE_SUCCESS == respond.code) {

                    return RestApi.SUCCESS

                }
                errMsg = respond?.msg;
            }
        } catch (e: Exception) {
            errMsg = e.localizedMessage
        }

        return RestApi.FAIL
    }


    companion object {
        private val CODE_SUCCESS = "0000"
        private val CODE_LOGOUT = "1001"
    }
}
