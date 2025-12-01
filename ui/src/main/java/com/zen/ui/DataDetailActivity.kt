package com.zen.ui

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.TextView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.zen.api.MyApi
import com.zen.api.RestApi
import com.zen.api.data.NoteData
import com.zen.api.event.SyncEventUpload
import com.zen.ui.adapter.AssectListAdapter
import com.zen.ui.base.BaseActivity
import com.zen.ui.utils.AssectSaveData
import com.zen.ui.utils.ToastUtilsX
import com.zen.ui.utils.ZipUtil
import kotlinx.android.synthetic.main.activity_data_detail.*
import nl.komponents.kovenant.task
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.IOException
import java.util.*

class DataDetailActivity : BaseActivity(), Runnable {
    override fun run() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private var mPicDate: Long = 0


    private var dataId: Long = 0


   // @OnClick(R2.id.tv_note)
    fun onNote() {
        NoteActivity.showMe(this, dataId)
    }


    fun onDelete() {
        SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText(getString(R.string.are_you_sure))
                .setContentText(getString(R.string.delete_the_record))
                .setConfirmText(getString(R.string.yes_delete))
                .setCancelText(getString(android.R.string.cancel))
                .setConfirmClickListener { sweetAlertDialog ->
                    delete()
                    sweetAlertDialog.dismissWithAnimation()
                }
                .show()

    }

    private fun delete() {
       var record =  MyApi.getInstance().dataApi.getRecordById(dataId);
        val id = MyApi.getInstance().dataApi.setDelRecord(dataId)
        task {
          var ret =  MyApi.getInstance().restApi.dataDelete(id, record.type)
            if(ret==RestApi.SUCCESS){
                MyApi.getInstance().dataApi.delRecord(dataId)
            }

        } success {
            runOnUiThread { finish() }
        }

    }

    private var emailTitle: String=""

    private var sending: Boolean=false;

    //@OnClick(R2.id.ly_send, R.id.bt_send)
    fun onSend() {
        val record = MyApi.getInstance().dataApi.getRecordById(dataId)
        if (record.pic == null) {
//            ToastUtils.showShort(getString(R.string.picture_none))
            ToastUtilsX.showTextActi(this, getString(R.string.picture_none))
            //return
        }
        try {

            val cols1 = arrayOf(/*"sn",*/ "createTime", "value", "unitString", "temp", "tempUnit")//,"deviceNumber","location","calibration"};

            val strings = ArrayList(Arrays.asList(*cols1))

            val sharedPreferences = MyApi.getInstance().dataApi.setting
            var b = sharedPreferences.getBoolean("SettingFragmentData:ser_no", false)
            if (b) {
                strings.add(0, "sn")
            }
            b = sharedPreferences.getBoolean("SettingFragmentData:gps", false)
            if (b) {
                strings.add("location")
            }
            b = sharedPreferences.getBoolean("SettingFragmentData:calibration", false)
            if (b) {
                strings.add("calibration")
            }

            val cols = arrayOfNulls<String>(strings.size)
//            val file = if(record.pic!=null) File(record.pic!!) else null
            val fileCSV = MyApi.getInstance().dataApi.exportCSV(this, record, strings.toArray(cols))

            if (fileCSV != null) {
                val out = File(
                    getExternalFilesDir("share"),
                    System.currentTimeMillis().toString() + ".zip"
                )
                if (out.exists()) out.delete()
                val fileOut = out.absolutePath
                try {
                    ZipUtil.zip(out, fileCSV.absolutePath)
//                    if (file != null) {
//                        ZipUtil.zip(out, file.absolutePath, fileCSV.absolutePath)
//                    } else {
//                        ZipUtil.zip(out, fileCSV.absolutePath)
//                    }

                } catch (e: IOException) {
                    e.printStackTrace()
                }

                shareFile(this, out, emailTitle, eMailText)
            } else {
//                ToastUtils.showShort(getString(R.string.file_none))
                ToastUtilsX.showTextActi(this, getString(R.string.file_none))
//                if(file!=null) {
//                    shareFile(this, file,emailTitle,eMailText)
//                }else {
//                    ToastUtils.showShort(getString(R.string.file_none))
//                }
            }
            sending = true;
        } catch (e: Exception) {

        }

        //shareMultiFile(this,file,fileCSV);
    }

  //  @OnClick(R2.id.ly_category, R.id.bt_category)
    fun onCategory() {
        CategoryActivity.showMe(this, dataId)
    }

    override fun onResume(){
        super.onResume();

        dataId = intent.getLongExtra(ID, -1)
        if (dataId > 0) {
            try {
                val record = MyApi.getInstance().dataApi.getRecordById(dataId)

                if (record.noteName.isNullOrBlank()) {
                    this.assect_images.visibility = View.INVISIBLE;
                    this.ass_tixi.visibility = View.INVISIBLE;
                    this.ass_name.visibility = View.INVISIBLE;
                } else {
                    val sharedPreferences = getSharedPreferences("AssectList", MODE_PRIVATE);
                    val size = sharedPreferences.getInt("data_size", 0);

                    for (i in 0..size-1){
                        if (sharedPreferences.getString("name_" + i, null)==record.noteName){
                            this.assect_images.setImageBitmap(
                                BitmapFactory.decodeFile(
                                    sharedPreferences.getString(
                                        "Image" + i,
                                        null
                                    )
                                )
                            )
                        }
                    }

                    this.ass_name.setText(record.noteName)
                    this.assect_images.visibility = View.VISIBLE;
                    this.ass_tixi.visibility = View.VISIBLE;
                    this.ass_name.visibility = View.VISIBLE;
                }
            }catch (e: Exception) {
                Log.w(TAG, "", e)
            }
        }
        if(sending){
            onBackPressed()
            sending = false;
        };
    }

    private fun initAssect() {
        val sharedPreferences = getSharedPreferences("AssectList", MODE_PRIVATE);
        assectList.clear();
        val size = sharedPreferences.getInt("data_size", 0);

        for (i in 0..size-1){
            val beer = AssectSaveData(
                sharedPreferences.getString("name_" + i, null), sharedPreferences.getString(
                    "Image" + i,
                    null
                ), sharedPreferences.getString("nameid" + i, null), sharedPreferences.getString(
                    "dataid" + i,
                    null
                )
            );
            assectList.add(beer);
        }
    }

    private var eMailText: String=""
    private var adapter : AssectListAdapter?= null
    var path:String?=null;
    var intIDList:Int?=null
    private var lineLeftTextView: TextView? = null

    private val assectList = ArrayList<AssectSaveData>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_detail)
        lineLeftTextView = this.findViewById<TextView>(R.id.tv_left)
        lineLeftTextView?.setOnClickListener {
            this.finish()
        }
        initAssect();

        val recyclerView = this.findViewById(R.id.Recycler_View) as RecyclerView
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = OrientationHelper.VERTICAL
        recyclerView.layoutManager = layoutManager
        val viewid = this.findViewById(R.id.Recycler_id) as View
        viewid.visibility = View.INVISIBLE

        adapter = AssectListAdapter(
            assectList,
            AssectListAdapter.OnReCyclerItemClickListener { V, position ->
                intIDList = position;
                adapter!!.setmPosition(position)
            })
        recyclerView.adapter = adapter

        val cancle = this.findViewById<TextView>(R.id.id_cancel)
        cancle.setOnClickListener {
            viewid.visibility = View.INVISIBLE;
        }


        val buttonok = this.findViewById<TextView>(R.id.id_ok)
        buttonok.setOnClickListener {
            if (intIDList!=null){
                val assectListview = assectList[intIDList!!]
                this.assect_images.setImageBitmap(BitmapFactory.decodeFile(assectListview.imageID))
                this.ass_name.setText(assectListview.name)
                this.assect_images.visibility = View.VISIBLE;
                this.ass_tixi.visibility = View.VISIBLE;
                this.ass_name.visibility = View.VISIBLE;

                path = null;
                saveAssect()
            }
            viewid.visibility= View.INVISIBLE

        }

        this.ly_category.setOnClickListener{
            onCategory()
        }
        this.bt_category.setOnClickListener{
            onCategory()
        }
        //@OnClick(R2.id.ly_send, R.id.bt_send)
        this.ly_send.setOnClickListener{
            onSend()
        }
        this.bt_send.setOnClickListener{
            onSend()
        }

        this.ly_Asset.setOnClickListener{
            if (viewid.visibility==0){
                viewid.visibility= View.INVISIBLE;
            }else{
                viewid.visibility= View.VISIBLE;
            }
        }
        this.bt_Asset.setOnClickListener(){
            if (viewid.visibility==0){
                viewid.visibility= View.INVISIBLE;
            }else{
                viewid.visibility= View.VISIBLE;
            }
        }

        // @OnClick(R2.id.ly_del, R.id.bt_del)
        this.ly_del.setOnClickListener{
            onDelete()
        }
        this.bt_del.setOnClickListener{
            onDelete()
        }
        //@OnClick(R2.id.tv_note)
        this.tv_note.setOnClickListener{
            onNote()
        }

        dataId = intent.getLongExtra(ID, -1)
        //  mPicDate = getIntent().getLongExtra(DATE, 0);
        if (dataId > 0) {
            try {
                val record = MyApi.getInstance().dataApi.getRecordById(dataId)
                this.iv_pic.setImageBitmap(BitmapFactory.decodeFile(record.pic))

                if (record.noteName!=null){
                    this.assect_images.visibility = View.VISIBLE;
                    this.ass_tixi.visibility = View.VISIBLE;
                    this.ass_name.visibility = View.VISIBLE;
                }else{
                    this.assect_images.visibility = View.INVISIBLE;
                    this.ass_tixi.visibility = View.INVISIBLE;
                    this.ass_name.visibility = View.INVISIBLE;
                }

                mPicDate = record.createTime
                val date = getDataString(Date(mPicDate));
                emailTitle = "ZenTest ["+record.id+"] "+date

                if(record.noteName!=null){
                    eMailText = record.noteName+"\r\n"
                }
                if(record.notes!=null){
                    eMailText += record.notes+"\r\n"
                }
                if(record.operator!=null){
                    eMailText += record.operator+"\r\n"
                }
                if(date!=null){
                    eMailText += date+"\r\n"
                }
                this.tv_date.text = date
            } catch (e: Exception) {
                Log.w(TAG, "", e)
            }

        }
    }

//    fun setHeadrView(recyclerView: RecyclerView) {
//        val headr = LayoutInflater.from(this).inflate(R.layout.item_assheadr,recyclerView,false) as View
//        val cancle = headr.findViewById<TextView>(R.id.id_cancel)
//        cancle.setOnClickListener {
//            recyclerView.visibility = View.INVISIBLE;
//        }
//
//        val buttonok = headr.findViewById<TextView>(R.id.id_ok)
//        buttonok.setOnClickListener {
//            if (intIDList!=null){
//                val assectListview = assectList[intIDList!!-1]
//                this.assect_images.setImageBitmap(BitmapFactory.decodeFile(assectListview.imageID))
//                this.ass_name.setText(assectListview.name)
//                this.assect_images.visibility = View.VISIBLE;
//                this.ass_tixi.visibility = View.VISIBLE;
//                this.ass_name.visibility = View.VISIBLE;
//
//                path = null;
//
//                saveAssect()
//            }
//
//            recyclerView.visibility= View.INVISIBLE
//
//        }
//        adapter!!.headerView = headr;
//    }

    fun saveAssect(){
        val record = MyApi.getInstance().dataApi.getRecordById(dataId!!)
        val noteName  = this.ass_name.text.toString()
        val operator  =  record.operator
        val notes  = record.notes
        var dirty=false;
        if(noteName!=record.noteName){
            dirty = true;
            record.noteName = noteName
        }
        if(operator!=record.operator){
            dirty = true;
            record.operator = operator
        }
        if(notes!=record.notes){
            dirty = true;
            record.notes = notes
        }

        if(path!=null){
            var   noteData: NoteData?=null;
            if(!TextUtils.isEmpty(record.noteId)){
                noteData =   MyApi.getInstance().dataApi.getNoteById(record.noteId)
            };
            if(noteData==null){
                noteData = NoteData();
            }
            if(noteData.attachedPhotos!=path){
                dirty = true;

            }
            noteData.attachedPhotos = path;
            noteData.noteName = noteName
            noteData.noteAdmin = operator
            noteData.notes = notes

            if (dirty) {
                if (noteData.id!=null&&noteData.id> 0) {
                    MyApi.getInstance().dataApi.updateNote(noteData)
                } else {
                    var id = MyApi.getInstance().dataApi.insertNote(noteData)
                    record.noteId = id?.toString()
                }
            }
        }

        if(dirty){
            record.sync = false
            MyApi.getInstance().dataApi.updateRecords(record)
        }

        EventBus.getDefault().post(SyncEventUpload())
    }


    companion object {
        val ID = "id"
        val DATE = "DATE"


        fun showMe(content: Context?, id: Long) {
            Log.e("==DataSave", id.toString());
            if (content == null) return
            val intent = Intent(content, DataDetailActivity::class.java)
            intent.putExtra(ID, id)
            //  intent.putExtra(DATE, date);
            content.startActivity(intent)
        }

        fun shareMultiFile(context: Context, vararg files: File) {
            if (null != files && files.size > 0) {
                Log.i(BaseActivity.sTAG, "分享文件 " + files.size)
                val fileUris = ArrayList<Uri>()
                for (file in files) {
                    if (file.exists()) {
                        //file:///storage/emulated/0/Android/data/com.zen.zentest/files/save/1522071860903.png
                        //content://com.honjane.providerdemo.fileprovider/save/files/b7d4b092822da.pdf
                        fileUris.add(getUriForFile(context, file))
                    } else {
                        Log.i(BaseActivity.sTAG, "分享文件不存在 " + file.absolutePath)
                    }
                }
                if (fileUris.size == 0) {
                    Log.i(BaseActivity.sTAG, "分享文件不存在 ")
                    return
                }
                //分享文件
                val intent = Intent(Intent.ACTION_SEND_MULTIPLE)//发送多个文件
                // intent.setType("*/*");//多个文件格式
                intent.type = "application/octet-stream"
                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, fileUris)//Intent.EXTRA_STREAM同于传输文件流
                context.startActivity(Intent.createChooser(intent, "Send to"))
            } else {
                // ToastUtils.showToast("分享文件不存在");
                Log.i(BaseActivity.sTAG, "分享文件列表为空 ")
            }
        }


        // 調用系統方法分享文件
        fun shareFile(context: Context, file: File?, subject: String, text: String) {
            if (null != file && file.exists()) {
                Log.i(BaseActivity.sTAG, "shareFile " + file.absolutePath)
                val share = Intent(Intent.ACTION_SEND)
                //share.addCategory(Intent.CATEGORY_APP_EMAIL)
                share.putExtra(Intent.EXTRA_STREAM, getUriForFile(context, file))
                // share.setType(getMimeType(file.getAbsolutePath()));//此处可发送多种文件
                share.type = "*/*"
                share.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
               // share.data = Uri.parse("mailto:abc@gmail.com");
               // share.putExtra(Intent.EXTRA_EMAIL, "abc@gmail.com");
                share.putExtra(Intent.EXTRA_SUBJECT, subject);
                share.putExtra(Intent.EXTRA_TEXT, text);
                //var intSendrt:IntentSender = IntentSender();
                context.startActivity(
                    Intent.createChooser(
                        share,
                        context.getString(R.string.send_file)
                    )
                )
            } else {
                // ToastUtils.showToast("分享文件不存在");
                Log.i(BaseActivity.sTAG, "shareFile not exists " + file?.absolutePath)
            }
        }

        // 根据文件后缀名获得对应的MIME类型。
        private fun getMimeType(filePath: String?): String {
            val mmr = MediaMetadataRetriever()
            var mime = "*/*"
            if (filePath != null) {
                try {
                    mmr.setDataSource(filePath)
                    if (!mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE).isNullOrEmpty()) {
                        mime = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE).toString()
                    }
                } catch (e: IllegalStateException) {
                    return mime
                } catch (e: IllegalArgumentException) {
                    return mime
                } catch (e: RuntimeException) {
                    return mime
                }

            }
            return mime
        }
    }
}
