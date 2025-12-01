package com.zen.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.ArrayAdapter
import android.widget.TextView
import com.zen.api.MyApi
import com.zen.api.data.NoteData
import com.zen.api.event.SyncEventUpload
import com.zen.ui.adapter.AssectListAdapter
import com.zen.ui.base.BaseActivity
import com.zen.ui.utils.AssectSaveData
import com.zen.ui.utils.PhotoUtil
import com.zen.ui.utils.ToastUtilsX
import com.zen.ui.view.ActionSheetDialog
import kotlinx.android.synthetic.main.activity_data_detail.*
import kotlinx.android.synthetic.main.activity_note.*
import kotlinx.android.synthetic.main.activity_note.tv_right
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.util.*


class NoteActivity : BaseActivity(), TextWatcher {
    private var adapter : AssectListAdapter?= null
    private val CAMERA_PERMISSION_REQUEST_CODE = 100
    private val GALLERY_PERMISSION_REQUEST_CODE = 110
    override fun afterTextChanged(s: Editable?) {
     }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        showSave()
    }


    private var dataId: Long?=0
    private val assectList = ArrayList<AssectSaveData>()
    var intIDList:Int?=null
    private var lineLeftTextView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)

        lineLeftTextView = this.findViewById<TextView>(R.id.tv_left)
        lineLeftTextView?.setOnClickListener {
            this.finish()
        }

        initAssect();

        val recyclerView = this.findViewById(R.id.Recycler_View) as RecyclerView
        val viewid = this.findViewById(R.id.Recycler_id) as View
        viewid.visibility = View.INVISIBLE

        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = OrientationHelper.VERTICAL
        recyclerView.layoutManager = layoutManager


        adapter = AssectListAdapter(
            assectList,
            AssectListAdapter.OnReCyclerItemClickListener { V, position ->
                intIDList = position;
                adapter!!.setmPosition(position)
            })
        recyclerView.adapter = adapter

//        setHeadrView(recyclerView);

        val cancle = this.findViewById<TextView>(R.id.id_cancel)
        cancle.setOnClickListener {
            viewid.visibility = View.INVISIBLE;
        }

        val buttonok = this.findViewById<TextView>(R.id.id_ok)
        buttonok.setOnClickListener {
            if (intIDList!=null){
                val assectListview = assectList[intIDList!!]

                this.et_name.text = assectListview.name
                showSave()

                path = null;
            }
            viewid.visibility= View.INVISIBLE
        }

        this.iv_photo.setOnClickListener{
            options()//   PhotoUtil.selectPictureFromAlbum(this)
        }
        this.iv_photo1.setOnClickListener{
            options()//  PhotoUtil.selectPictureFromAlbum(this)
        }
        this.ly_location.setOnClickListener {
            val record = MyApi.getInstance().dataApi.getRecordById(dataId!!)
            if(TextUtils.isEmpty(record.location)){
//                ToastUtils.showShort(getString(R.string.location_none))
                ToastUtilsX.showTextActi(this, getString(R.string.location_none))
            }
            else {

                //ToastUtils.showShort("Location:" + record.location)
            }
//            MapsActivity.showMe(this,record.location)
            val intent = Intent(this, MapsActivity::class.java)
            intent.putExtra("location", record.location)

            startActivityForResult(intent, 1)
        }

        this.et_name.setOnClickListener(){
            if (viewid.visibility==0){
                viewid.visibility= View.INVISIBLE;
            }else{
                viewid.visibility= View.VISIBLE;
            }

        }

        dataId = intent.getLongExtra(ID, -1);

        Log.e("---DataSave", dataId.toString());
        if (dataId == null) {
            return
        }
        val record = MyApi.getInstance().dataApi.getRecordById(dataId!!)
        this.et_name.setText(record.noteName)

        val sharedPreferences = getSharedPreferences("AssectList",MODE_PRIVATE);
        val AdminName = sharedPreferences.getString("AdminName","")
        if (AdminName!!.length>0){
            this.et_admin.setText(AdminName);
        }else{
            this.et_admin.setText(record.noteName)
        }

        this.et_notes.setText(record.notes)
        this.et_admin.addTextChangedListener(this);
        this.et_notes.addTextChangedListener(this);

        var noteData =   MyApi.getInstance().dataApi.getNoteById(record.noteId)
        if(noteData!=null) {
            this.iv_photo.visibility= View.GONE;
            this.iv_photo1.visibility= View.VISIBLE;

            val vto: ViewTreeObserver = this.iv_photo1.getViewTreeObserver()
            vto.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    val width: Int = iv_photo1.getWidth() // 获取ImageView的宽度
                    iv_photo1.visibility = View.VISIBLE;
                    var w = iv_photo1.width;//this.iv_photo1.width;
                    var size = PhotoUtil.getBitmapSize(noteData.attachedPhotos)
                    var h = w * size[1] / size[0]
                    Log.v("bitmap2 w", "" + w)
                    Log.v("bitmap2 h", "" + h)
                    val bitmap2 = PhotoUtil.convertToBitmap(noteData.attachedPhotos, w, h)
                    Log.v("bitmap2", "" + bitmap2.height.toString() + "x" + bitmap2.width + "图")
                    if (bitmap2 != null) {
                        iv_photo1.visibility = View.VISIBLE;
                        // tv1.setText(bitmap2.height.toString() + "x" + bitmap2.width + "图")
                        // smallImg.setImageBitmap(bitmap2)
                        var layoutParams = iv_photo1.layoutParams
                        layoutParams.width = w
                        layoutParams.height = h
                        iv_photo1.layoutParams = layoutParams
                        iv_photo1.setImageBitmap(bitmap2)
                        iv_photo.visibility = View.GONE;
                    }

                    // 无论是否需要继续监听布局变化，都建议在结束时将监听移除，以避免重复调用
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        iv_photo1.getViewTreeObserver().removeOnGlobalLayoutListener(this)
                    } else {
                        iv_photo1.getViewTreeObserver().removeGlobalOnLayoutListener(this)
                    }
                }
            })



//            val bitmap = PhotoUtil.convertToBitmap(
//                noteData.attachedPhotos,
//                PhotoUtil.PICTURE_WIDTH,
//                PhotoUtil.PICTURE_HEIGHT
//            )
//            if (bitmap != null) {
//                //  tv2.setText(bitmap.height.toString() + "x" + bitmap.width + "图")
//                //   clipImg.setImageBitmap(bitmap)
//                this.iv_photo.visibility= View.GONE;
//                this.iv_photo1.visibility= View.VISIBLE;
//                this.iv_photo1.setImageBitmap(bitmap)
//
//            }
        }

        var lists = mutableListOf<String>()
        var cs =  MyApi.getInstance().dataApi.category;
        cs?.forEach { c ->
            lists.add(c.name)
        }
        var  arrayAdapter = ArrayAdapter<String>(
            this,
            android.R.layout.simple_dropdown_item_1line, lists
        );

//        this.et_name.setAdapter(arrayAdapter)
        this.tv_right.setOnClickListener{
            save()
            finish()
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
//
//                this.et_name.text = assectListview.name
//                showSave()
//
//                path = null;
//            }
//            recyclerView.visibility= View.INVISIBLE
//        }
//        adapter!!.headerView = headr;
//    }


    //需要改的地方
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


    fun showSave(){
        this.tv_right.visibility= View.VISIBLE;

    }

    fun hideSave(){
        this.tv_right.visibility= View.INVISIBLE;

    }
    fun save(){
        val record = MyApi.getInstance().dataApi.getRecordById(dataId!!)
        val noteName  = this.et_name.text.toString()
        val operator  =  this.et_admin.text.toString()
        val notes  = this.et_notes.text.toString()
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
            var   noteData:NoteData?=null;
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

        val editor = getSharedPreferences("AssectList", MODE_PRIVATE).edit() as SharedPreferences.Editor
        editor.putString("AdminName", this.et_admin.text.toString());
        editor.commit()

        hideSave();
        EventBus.getDefault().post(SyncEventUpload())
    }

    var path:String?=null;

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (resultCode==2){
            val str = data?.getStringExtra("name")
            if (str!=null|| str?.length !=0){
                this.ly_locationText.setText(str?.substringBefore("-"))
                showSave()
            }
            return
        }
        if (resultCode == PhotoUtil.NONE)
            return

        // 拍照
        if (requestCode == PhotoUtil.PHOTOGRAPH) {
            // 设置文件保存路径这里放在跟文件夹下
            var picture =    File(PhotoUtil.imageName)
      /*      if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                picture = File(PhotoUtil.imageName)
                if (!picture!!.exists()) {
                    picture = File(PhotoUtil.imageName)
                }
            } else {
                picture = File(PhotoUtil.imageName)
                if (!picture!!.exists()) {
                    picture = File(PhotoUtil.imageName)
                }
            }*/
            if (!picture.exists()) {
                return
            }
            path =PhotoUtil.imageName // PhotoUtil.getPath(this)// 生成一个地址用于存放剪辑后的图片
            if (TextUtils.isEmpty(path)) {
                return
            }


            //val bitmap = PhotoUtil.convertToBitmap(path, PhotoUtil.PICTURE_HEIGHT, PhotoUtil.PICTURE_WIDTH)

            var w =  this.iv_photo1.width; // PhotoUtil.PICTURE_WIDTH// this.iv_photo1.width;
            var size = PhotoUtil.getBitmapSize(path)
            var h = w * size[1] / size[0]
            Log.v("iv_photo1 w", "" + w)
            Log.v("iv_photo1 h", "" + h)
            val bitmap2 = PhotoUtil.convertToBitmap(path, w, h)
            Log.v("bitmap2", "" + bitmap2.height.toString() + "x" + bitmap2.width + "图")
            if (bitmap2 != null) {
                // tv1.setText(bitmap2.height.toString() + "x" + bitmap2.width + "图")
                // smallImg.setImageBitmap(bitmap2)
                var layoutParams = this.iv_photo1.layoutParams
                layoutParams.width = w
                layoutParams.height = h
                this.iv_photo1.layoutParams = layoutParams
                this.iv_photo1.setImageBitmap(bitmap2)
                this.iv_photo.visibility = View.GONE;
                this.iv_photo1.visibility = View.VISIBLE;
                showSave();
            }

            //val imageUri = Uri.fromFile( File(path))
            //PhotoUtil.startPhotoZoom(this@NoteActivity, getUriForFile(this,picture), PhotoUtil.PICTURE_HEIGHT, PhotoUtil.PICTURE_WIDTH, imageUri)
        }else {

            if (data == null)
                return

            // 读取相冊缩放图片
            if (requestCode == PhotoUtil.PHOTOZOOM) {

                path = PhotoUtil.getPath(this)// 生成一个地址用于存放剪辑后的图片
                if (TextUtils.isEmpty(path)) {
                    return
                }
                //val imageUri = Uri.fromFile(File(path))
                //PhotoUtil.startPhotoZoom(this@NoteActivity, data.data, PhotoUtil.PICTURE_HEIGHT, PhotoUtil.PICTURE_WIDTH, imageUri)
                var mImageCaptureUri =  data.data;
                var picBitmap = MediaStore.Images.Media.getBitmap(
                    getContentResolver(),
                    mImageCaptureUri
                )
                PhotoUtil.save(picBitmap, path)
                var w = this.iv_photo1.width;//PhotoUtil.PICTURE_WIDTH// this.iv_photo1.width;

                var size = PhotoUtil.getBitmapSize(path)
                var h = w * size[1] / size[0]
                Log.v("iv_photo1 w", "" + w)
                Log.v("iv_photo1 h", "" + h)
                val bitmap2 = PhotoUtil.convertToBitmap(path, w, h)
                Log.v("bitmap2", "" + bitmap2.height.toString() + "x" + bitmap2.width + "图")
                if (bitmap2 != null) {
                    // tv1.setText(bitmap2.height.toString() + "x" + bitmap2.width + "图")
                    // smallImg.setImageBitmap(bitmap2)
                    var layoutParams = this.iv_photo1.layoutParams
                    layoutParams.width = w
                    layoutParams.height = h
                    this.iv_photo1.layoutParams = layoutParams
                    this.iv_photo1.setImageBitmap(bitmap2)
                    this.iv_photo.visibility = View.GONE;
                    this.iv_photo1.visibility = View.VISIBLE;
                    showSave();
                }
            }
            // 处理结果
            else if (requestCode == PhotoUtil.PHOTORESOULT) {
                /**
                 * 在这里处理剪辑结果。能够获取缩略图，获取剪辑图片的地址。得到这些信息能够选则用于上传图片等等操作
                 */

                /**
                 * 如。依据path获取剪辑后的图片
                 */
                val bitmap = PhotoUtil.convertToBitmap(
                    path,
                    PhotoUtil.PICTURE_HEIGHT,
                    PhotoUtil.PICTURE_WIDTH
                )
                if (bitmap != null) {
                    //  tv2.setText(bitmap.height.toString() + "x" + bitmap.width + "图")
                    //   clipImg.setImageBitmap(bitmap)
                }
                var w = this.iv_photo1.width;//this.iv_photo1.width;
                var size = PhotoUtil.getBitmapSize(path)
                var h = w * size[1] / size[0]
                Log.v("iv_photo1 w", "" + w)
                Log.v("iv_photo1 h", "" + h)
                val bitmap2 = PhotoUtil.convertToBitmap(path, w, h)
                Log.v("bitmap2", "" + bitmap2.height.toString() + "x" + bitmap2.width + "图")
                if (bitmap2 != null) {
                    // tv1.setText(bitmap2.height.toString() + "x" + bitmap2.width + "图")
                    // smallImg.setImageBitmap(bitmap2)
                    var layoutParams = this.iv_photo1.layoutParams
                    layoutParams.width = w
                    layoutParams.height = h
                    this.iv_photo1.layoutParams = layoutParams
                    this.iv_photo1.setImageBitmap(bitmap2)
                    this.iv_photo.visibility = View.GONE;
                    this.iv_photo1.visibility = View.VISIBLE;
                    showSave();
                }

                //			Bundle extras = data.getExtras();
                //			if (extras != null) {
                //				Bitmap photo = extras.getParcelable("data");
                //				ByteArrayOutputStream stream = new ByteArrayOutputStream();
                //				photo.compress(Bitmap.CompressFormat.JPEG, 100, stream);// (0-100)压缩文件
                //				InputStream isBm = new ByteArrayInputStream(stream.toByteArray());
                //			}

            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun checkPer(permission: String) : Boolean{
        return  this.checkPermission(
            permission,
            android.os.Process.myPid(),
            android.os.Process.myUid()
        )==PackageManager.PERMISSION_GRANTED;
    }

    protected fun options() {
        val mDialog = ActionSheetDialog(this).builder()
        mDialog.setTitle(getString(R.string.select))
        mDialog.setCancelable(false)
        mDialog.addSheetItem(
            getString(R.string.photograph),
            ActionSheetDialog.SheetItemColor.Blue,
            object : ActionSheetDialog.OnSheetItemClickListener {
                override fun onClick(which: Int) {
                    if (checkPer(Manifest.permission.CAMERA)) {
                        // 已授权相机权限，可以打开相机
                        PhotoUtil.photograph(this@NoteActivity)
                    } else {
                        // 未授权相机权限，需要请求授权
                        checkPermission(Manifest.permission.CAMERA)
                        return
                    }
                }
            }).addSheetItem(
            getString(R.string.select_from_the_album),
            ActionSheetDialog.SheetItemColor.Blue,
            object : ActionSheetDialog.OnSheetItemClickListener {
                override fun onClick(which: Int) {
                    if (isAndroid13()) {
                        if (checkPer(Manifest.permission.READ_MEDIA_IMAGES)) {
                            // 已授权读取外部存储权限，可以读取相册中的图片
                            PhotoUtil.selectPictureFromAlbum(this@NoteActivity)
                        } else {
                            // 未授权读取外部存储权限，需要请求授权
                            checkPermission(Manifest.permission.READ_MEDIA_IMAGES)
                            return
                        }
                    } else {
                        if (checkPer(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                            // 已授权读取外部存储权限，可以读取相册中的图片
                            PhotoUtil.selectPictureFromAlbum(this@NoteActivity)
                        } else {
                            // 未授权读取外部存储权限，需要请求授权
                            checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                            return
                        }
                    }
                }
            }).show()
    }


    companion object {
        val ID = "id"
        val DATE = "DATE"


        fun showMe(content: Context?, id: Long) {
            if (content == null) return

            Log.e("11111DataSave", content.toString());
            val intent = Intent(content, NoteActivity::class.java)
            intent.putExtra(ID, id)

            content.startActivity(intent)
        }
    }
}

