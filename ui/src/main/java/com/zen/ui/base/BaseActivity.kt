package com.zen.ui.base

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.appcompat.app.AppCompatActivity
import android.util.DisplayMetrics
import android.util.Log
import android.widget.Toast

import com.foolchen.lib.tracker.lifecycle.ITrackerHelper
import com.foolchen.lib.tracker.lifecycle.ITrackerIgnore
import com.zen.api.MyApi
import com.zen.api.event.LogoutEvent
import com.zen.api.event.SyncEventUpload
//import com.pgyersdk.feedback.PgyFeedbackShakeManager
import com.zen.api.event.UpdateEvent
import com.zen.api.utils.ThreadPoolUtils
import com.zen.ui.*
import com.zen.ui.fragment.SettingFragment1
import com.zen.ui.utils.StatusBarUtil

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Logger
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
//import com.alipay.mobile.tianyanadapter.autotracker.agent.TrackAgentManager;

import java.io.File
import java.net.URI
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.Locale


/**
 * Created by xunlong.wxl on 16/12/7.
 */

open class BaseActivity() : AppCompatActivity(), ITrackerHelper, ITrackerIgnore {
    val TAG = this.javaClass.simpleName
    private var mTop: Boolean = false
    private var width: Int = 0
    private var height: Int = 0
    private var density: Float = 0.toFloat()
    private var densityDpi: Int = 0

    private val REQUEST_CODE_ASK_PERMISSIONS = 123//权限请求码

    public fun isAndroid13(): kotlin.Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    }

    /**
     * 获取状态栏高度
     *///获取status_bar_height资源的ID
    //根据资源ID获取响应的尺寸值
    val statusBar: Int
        get() {
            var statusBarHeight1 = -1
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                statusBarHeight1 = resources.getDimensionPixelSize(resourceId)
            }
            return statusBarHeight1
        }

    init {
        if (mThreadPoolUtils == null) {
            mThreadPoolUtils = ThreadPoolUtils(ThreadPoolUtils.CachedThread, 3)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        StatusBarUtil.setStatusBarColor(this, resources.getColor(R.color.colorPrimaryDark))
        super.onCreate(savedInstanceState)
        TrackAgentManager.getInstance().trackAgent.onActivityCreate(this)
        EventBus.getDefault().register(this)
    }

    override protected fun onStart() {
        super.onStart()
        EventBus.getDefault().post(SyncEventUpload())
    }

    override fun onResume() {
        super.onResume()
        TrackAgentManager.getInstance().trackAgent.onActivityResume(this)
        mTop = true
        // custom sensitivity, defaults to 950, the smaller the number higher sensitivity.
       // PgyFeedbackShakeManager.setShakingThreshold(1000)

        // Open as a dialog
        //PgyFeedbackShakeManager.register(this)

        //  Open as an Activity, in the case you must configure FeedbackActivity in the file of AndroidManifest.xml
       // PgyFeedbackShakeManager.register(this, false)

        if (this.javaClass != LoginActivity::class.java
                &&this.javaClass != CreateAccountActivity::class.java
                &&this.javaClass != ForgotPasswordActivity::class.java
                && this.javaClass != ChangePasswordActivity::class.java ) {
            if ((!MyApi.getInstance().restApi.isLogin) && (!MyApi.getInstance().restApi.isDemoLogin)) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
    }

    override fun onPause() {
        mTop = false
        super.onPause()
        TrackAgentManager.getInstance().trackAgent.onActivityPause(this)
      //  PgyFeedbackShakeManager.unregister()
    }

/*
    override fun onStop() {
        super.onStop()
    }
*/

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
        TrackAgentManager.getInstance().trackAgent.onActivityDestroy(this)
    }

    override fun onRestart() {
        super.onRestart()
    }

    override fun finish() {
        super.finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        TrackAgentManager.getInstance().trackAgent.onActivityWindowFocusChanged(this, hasFocus)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    open fun onEvent(event: UpdateEvent) {
        try {

            if (mTop) {
                updateView()
            } else {
                Log.d(TAG, "onEvent UpdateEvent do not updateView()")
            }
        } catch (e: Exception) {
            Log.w(TAG, "onEvent UpdateEvent  Exception", e)
        }

    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    open fun onEvent(event: LogoutEvent) {
        if (this.javaClass == LoginActivity::class.java || this.javaClass != CreateAccountActivity::class.java
                ||  this.javaClass != ChangePasswordActivity::class.java  || this.javaClass != ForgotPasswordActivity::class.java )
        {
            return
        }
        if (MyApi.getInstance().restApi.isLogin || MyApi.getInstance().restApi.isDemoLogin) {

        } else {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
    open fun updateView() {

    }

    fun checkPermission(vararg permissions: String): Boolean {

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                val strings = ArrayList<String>()
                for (permission in permissions) {
                    val hasWriteContactsPermission = checkSelfPermission(permission)//权限检查
                    if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
                        strings.add(permission)
                    }
                }

                if (strings.size > 0) {
                    //val strings1 = arrayOfNulls<String>(strings.size)
                    //strings.toTypedArray()
                    requestPermissions(strings.toTypedArray(),
                            REQUEST_CODE_ASK_PERMISSIONS)
                    return false//没有权限，结束
                } else {
                    //做自己的操作
                    return true
                }
            } else
                return true
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "权限异常", Toast.LENGTH_SHORT).show()
            return false
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.i(TAG, "onActivityResult $requestCode $resultCode")
        super.onActivityResult(requestCode, resultCode, data);

    }



    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        Log.i(TAG, "onActivityResult $requestCode $permissions")
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    }


    fun getDisplayMetrics() {

        val metric = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metric)
        width = metric.widthPixels     // 屏幕宽度（像素）
        height = metric.heightPixels   // 屏幕高度（像素）
        density = metric.density      // 屏幕密度（0.75 / 1.0 / 1.5）
        densityDpi = metric.densityDpi  // 屏幕密度DPI（120 / 160 / 240）
        Log.i(TAG, "width = $width height =$height density=$density densityDpi=$densityDpi")

        //width = 1080 height =1821 density=2.75 densityDpi=440 mate8
    }


    fun getWidth(): Int {
        if (width == 0) {
            getDisplayMetrics()
        }
        return width
    }

    protected fun getDataString(date: Date): String {
        val simpleDateFormat = SimpleDateFormat("yyyy/MM/dd a hh:mm:ss", Locale.US)
        return simpleDateFormat.format(date)

    }

    override fun getTrackName(): String? {
        return null
    }

    override fun getTrackProperties(): Map<String, *>? {
        return null
    }

    override fun isIgnored(): Boolean {
        return false
    }

    companion object {
        val sTAG = BaseActivity::class.java.simpleName
        public var mThreadPoolUtils: ThreadPoolUtils? = null


        private fun isNetworkConnected(context: Context?): Boolean {
            if (context != null) {
                val mConnectivityManager = context
                        .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val mNetworkInfo = mConnectivityManager.activeNetworkInfo
                if (mNetworkInfo != null) {
                    return mNetworkInfo.isAvailable
                }
            }
            return false
        }

        fun getUriForFile(context: Context?, name: String?): Uri {
            if (context == null || name == null) {
                throw NullPointerException()
            }
            val file = File(name)
            val uri: Uri
            if (Build.VERSION.SDK_INT >= 24) {
                uri = FileProvider.getUriForFile(context, "com.zen.zentest.fileprovider", file)
            } else {
                uri = Uri.fromFile(file)
            }
            Log.i(sTAG,uri.toString());
            return uri
        }

        fun getUriForFile(context: Context?, file: File?): Uri {
            if (context == null || file == null) {
                throw NullPointerException()
            }
            val uri: Uri
            if (Build.VERSION.SDK_INT >= 24) {
                uri = FileProvider.getUriForFile(context, "com.zen.zentest.fileprovider", file)
            } else {
                uri = Uri.fromFile(file)
            }
            Log.i(sTAG,uri.toString());
            return uri
        }
    }
}
