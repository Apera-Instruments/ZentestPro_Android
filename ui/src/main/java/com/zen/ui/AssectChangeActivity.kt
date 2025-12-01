package com.zen.ui

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

import com.orhanobut.logger.Logger
import com.zen.ui.base.BaseActivity
import com.zen.ui.utils.AssectSaveData
import com.zen.ui.utils.PhotoUtil
import com.zen.ui.view.ActionSheetDialog
import java.io.File

import java.util.ArrayList

class AssectChangeActivity : BaseActivity() {
    private val assectList = ArrayList<AssectSaveData>()
    var imageView: ImageView ?=null

    private var lineLeftTextView: TextView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_changeassect)
        lineLeftTextView = this.findViewById<TextView>(R.id.tv_left)
        lineLeftTextView?.setOnClickListener {
            this.finish()
        }

        initAssect()
        val intent = intent

        val Textname = intent.getStringExtra("Textname")
        val Imageid = intent.getStringExtra("Imagename")
        val nameid = intent.getStringExtra("nameid")
        imageView = findViewById<View>(R.id.iv_photo) as ImageView
        imageView!!.setImageBitmap(BitmapFactory.decodeFile(Imageid))
        val textView = findViewById<View>(R.id.NewAssectName) as TextView
        textView.text = Textname

        val savebutton = findViewById<View>(R.id.tv_Okright) as Button
        savebutton.setOnClickListener {
            if (textView.text.length > 0) {
                var id = NameId(nameid.toString());
                if (path == null){
                    val beer = AssectSaveData(textView.text.toString(), assectList.get(id).imageID, "0", assectList.size.toString()+1)
                    assectList.set(id,beer)
                }else{
                    val beer = AssectSaveData(textView.text.toString(), path, "0", assectList.size.toString()+1)
                    assectList.set(id,beer)
                }
                Toast.makeText(this,R.string.Assect_changeok,Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this,R.string.Assect_NameNil,Toast.LENGTH_SHORT).show()
            }

            SaveArray()
            finish()
        }
        imageView!!.setOnClickListener() {
            val mDialog = ActionSheetDialog(this).builder()
            mDialog.setTitle(getString(R.string.select))
            mDialog.setCancelable(false)
            mDialog.addSheetItem(getString(R.string.photograph), ActionSheetDialog.SheetItemColor.Blue, object : ActionSheetDialog.OnSheetItemClickListener {
                override fun onClick(which: Int) {
                    if (checkPer(Manifest.permission.CAMERA)) {
                        // 已授权相机权限，可以打开相机
                        PhotoUtil.photograph(this@AssectChangeActivity)
                    } else {
                        // 未授权相机权限，需要请求授权
                        checkPermission(Manifest.permission.CAMERA)
                        return
                    }
                }
            }).addSheetItem(getString(R.string.select_from_the_album), ActionSheetDialog.SheetItemColor.Blue, object : ActionSheetDialog.OnSheetItemClickListener {
                override fun onClick(which: Int) {
                    if (isAndroid13()) {
                        if (checkPer(Manifest.permission.READ_MEDIA_IMAGES)) {
                            // 已授权读取外部存储权限，可以读取相册中的图片
                            PhotoUtil.selectPictureFromAlbum(this@AssectChangeActivity)
                        } else {
                            // 未授权读取外部存储权限，需要请求授权
                            checkPermission(Manifest.permission.READ_MEDIA_IMAGES)
                            return
                        }
                    } else {
                        if (checkPer(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                            // 已授权读取外部存储权限，可以读取相册中的图片
                            PhotoUtil.selectPictureFromAlbum(this@AssectChangeActivity)
                        } else {
                            // 未授权读取外部存储权限，需要请求授权
                            checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                            return
                        }
                    }
                }
            }).show()
        }

        val delbutton = findViewById<View>(R.id.bt_dels) as Button
        delbutton.setOnClickListener(View.OnClickListener {
            for (i in assectList.indices) {

                if (Integer.parseInt(nameid) == Integer.parseInt(assectList[i].nameid)) {
                    assectList.removeAt(i)
                    SaveArray()
                    finish()
                    Toast.makeText(this@AssectChangeActivity, R.string.Assect_DelegetOk, Toast.LENGTH_SHORT).show()
                    return@OnClickListener
                }
            }
        })
    }

    fun checkPer(permission : String) : Boolean{
        return  this.checkPermission(permission,android.os.Process.myPid(), android.os.Process.myUid())== PackageManager.PERMISSION_GRANTED;
    }

    fun NameId(string: String): Int{
        for (i in 0..assectList.size-1){
            if (assectList.get(i).nameid.equals(string)){
                return i;
            }
        }
        return -1;
    }

    fun SaveArray(): Boolean {

        val editor = getSharedPreferences("AssectList", MODE_PRIVATE).edit() as SharedPreferences.Editor
        editor.putInt("data_size", assectList.size);
        for (i in 0..assectList.size-1) {
            editor.putString("name_"+i, assectList[i].name)
            editor.putString("Image"+i, assectList[i].imageID)
            editor.putString("nameid"+i, assectList[i].nameid)
            editor.putString("dataid"+i, assectList[i].dataid)
        }
        return editor.commit()
    }

    private fun initAssect() {
        val sharedPreferences = getSharedPreferences("AssectList",MODE_PRIVATE);
        assectList.clear();
        val size = sharedPreferences.getInt("data_size",0);

        for (i in 0..size-1){
            val beer = AssectSaveData(sharedPreferences.getString("name_"+i,null), sharedPreferences.getString("Image"+i,null),sharedPreferences.getString("nameid"+i,null),sharedPreferences.getString("dataid"+i,null));
            assectList.add(beer);
        }
    }

    var path:String?=null;
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == PhotoUtil.NONE)
            return

        // 拍照
        if (requestCode == PhotoUtil.PHOTOGRAPH) {
            // 设置文件保存路径这里放在跟文件夹下
            var picture = File(PhotoUtil.imageName)
            if (!picture.exists()) {

                return
            }
            path = PhotoUtil.imageName // PhotoUtil.getPath(this)// 生成一个地址用于存放剪辑后的图片
            if (TextUtils.isEmpty(path)) {

                return
            }
            var w = PhotoUtil.PICTURE_WIDTH// this.iv_photo1.width;
            val bitmap2 = PhotoUtil.convertToBitmap(path, w, w)

            if (bitmap2 != null) {
                imageView?.setImageBitmap(bitmap2)
            }
        } else {
            if (data == null)
                return
            // 读取相冊缩放图片
            if (requestCode == PhotoUtil.PHOTOZOOM) {
                path = PhotoUtil.getPath(this)// 生成一个地址用于存放剪辑后的图片
                if (TextUtils.isEmpty(path)) {

                    return
                }
                var mImageCaptureUri = data.data;
                var picBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mImageCaptureUri)
                PhotoUtil.save(picBitmap, path)
                var w = PhotoUtil.PICTURE_WIDTH// this.iv_photo1.width;
                val bitmap2 = PhotoUtil.convertToBitmap(path, w, w)

                if (bitmap2 != null) {
                    imageView?.setImageBitmap(bitmap2)
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
                val bitmap = PhotoUtil.convertToBitmap(path, PhotoUtil.PICTURE_HEIGHT, PhotoUtil.PICTURE_WIDTH)
                if (bitmap != null) {
                    //  tv2.setText(bitmap.height.toString() + "x" + bitmap.width + "图")
                    //   clipImg.setImageBitmap(bitmap)
                }
                var w = PhotoUtil.PICTURE_WIDTH;//this.iv_photo1.width;
                val bitmap2 = PhotoUtil.convertToBitmap(path, w, w)

                if (bitmap2 != null) {
                    // tv1.setText(bitmap2.height.toString() + "x" + bitmap2.width + "图")
                    // smallImg.setImageBitmap(bitmap2)
                    imageView?.setImageBitmap(bitmap2)
                }
            }
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
