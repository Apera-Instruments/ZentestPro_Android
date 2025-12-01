package com.zen.ui.base

import android.app.Fragment
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.core.content.FileProvider
import android.util.DisplayMetrics
import android.util.Log
import com.foolchen.lib.tracker.Tracker

import com.foolchen.lib.tracker.lifecycle.ITrackerHelper
import com.foolchen.lib.tracker.lifecycle.ITrackerIgnore
import com.zen.api.event.UpdateEvent
import com.zen.api.protocol.Data
import com.zen.api.utils.ThreadPoolUtils

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File

import java.text.DecimalFormat
import java.text.NumberFormat

/**
 * Created by louis on 17-12-21.
 */
open class BaseFragment : Fragment(), ITrackerHelper, ITrackerIgnore {
    val TAG = this.javaClass.simpleName
    private var width: Int = 0
    private var height: Int = 0
    private var density: Float = 0.toFloat()
    private var densityDpi: Int = 0
    private var mResume: Boolean = false
    fun ObjectsEquals(obj1: Any?, obj2: Any?): Boolean {
        return if (obj1 == null) {
            obj2 == null
        } else {
            obj1 == obj2
        }
    }

    init {
        if (mThreadPoolUtils == null) {
            mThreadPoolUtils = ThreadPoolUtils(ThreadPoolUtils.CachedThread, 3)
        }
    }

    override fun onResume() {
        Log.v(TAG, this.toString() + " onResume")
        super.onResume()
        mResume = true
        updateView()

    }

    override fun onPause() {
        Log.v(TAG, this.toString() + " onPause")
//        mResume = false
        super.onPause()


    }

    override fun onStart() {
        Log.v(TAG, this.toString() + " onStart")
        super.onStart()


    }

    override fun onStop() {
        Log.v(TAG, this.toString() + " onPause")
        super.onStop()
    }

    override fun onAttach(context: Context) {
        Log.v(TAG, this.toString() + " onAttach")
        super.onAttach(context)
    }

    override fun onDetach() {
        Log.v(TAG, this.toString() + " onDetach")
        super.onDetach()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.v(TAG, this.toString() + " onCreate")
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        Log.v(TAG, this.toString() + " onDestroy")
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    open fun onEvent(event: UpdateEvent) {
        if (mResume) updateView()
    }

    open fun updateView() {
        Log.v(TAG, this.toString() + " updateView")
    }

    override fun getContext(): Context {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            super.getContext()
        } else {
            super.getActivity()
        }
    }

    open fun remove() {

    }

    fun getColor(resId: Int): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            resources.getColor(resId, null)
        } else {
            resources.getColor(resId)
        }
    }


    fun getDisplayMetrics() {
        try {
            val metric = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(metric)
            width = metric.widthPixels     // 屏幕宽度（像素）
            height = metric.heightPixels   // 屏幕高度（像素）
            density = metric.density      // 屏幕密度（0.75 / 1.0 / 1.5）
            densityDpi = metric.densityDpi  // 屏幕密度DPI（120 / 160 / 240）
        } catch (e: Exception) {
            Log.w(TAG, "getDisplayMetrics Exception", e)
        }

    }

    fun getWidth(): Int {
        if (width == 0) {
            getDisplayMetrics()
        }
        return width
    }

    fun getHeight(): Int {
        if (height == 0) {
            getDisplayMetrics()
        }
        return height
    }

    fun getFormatDouble(d: Double?, n: Int): String {
        val df = NumberFormat.getInstance() as DecimalFormat
        df.maximumFractionDigits = n
        df.minimumFractionDigits = n
        return df.format(d)
    }

    fun getUnitString(unit: Int): String {
        var string = ""
        when (unit) {
            Data.UNIT_pH -> string = "pH"
            Data.UNIT_mV -> string = "mV"
            Data.UNIT_mS -> string = "mS"
            Data.UNIT_uS -> string = "µS"
            Data.UNIT_ppm -> string = "ppm"
            Data.UNIT_ppt -> string = "ppt"
            Data.UNIT_gl -> string = "g/L"
            Data.UNIT_mgl -> string = "mg/L"
            Data.UNIT_Ω -> string = "Ω"
            Data.UNIT_KΩ -> string = "KΩ"
            Data.UNIT_MΩ -> string = "MΩ"
            else -> {
            }
        }
        return string
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
        Log.i(TAG,uri.toString());
        return uri
    }
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        //Tracker.setUserVisibleHint(this, isVisibleToUser)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        //Tracker.onHiddenChanged(this, hidden)
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
        public var mThreadPoolUtils: ThreadPoolUtils? = null
    }
}
