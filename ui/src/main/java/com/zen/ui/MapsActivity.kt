package com.zen.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.webkit.GeolocationPermissions
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.TextView
import com.zen.api.MyApi
import com.zen.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_maps.*

class MapsActivity : BaseActivity() {



    private var dataId: Long?=0


    private var location: String?=null

    private var lineLeftTextView: TextView? = null


    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        lineLeftTextView = this.findViewById<TextView>(R.id.tv_left)
        lineLeftTextView?.setOnClickListener {
            this.finish()
        }

        this.web_view.settings.javaScriptEnabled=true
        this.web_view.settings.setGeolocationEnabled(true)
        this.web_view.settings.domStorageEnabled=true

        this.web_view.webChromeClient = object : WebChromeClient() {

            //配置权限（同样在WebChromeClient中实现）
            override fun onGeolocationPermissionsShowPrompt(
                origin: String,
                callback: GeolocationPermissions.Callback
            ) {
                callback.invoke(origin, true, false)
                super.onGeolocationPermissionsShowPrompt(origin, callback)
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                val str = title?.substring(0, 6)


                if (str.equals("Google")){

                }else{
                    val intent = Intent()
                    intent.putExtra("name", title)
                    setResult(2, intent)
                    finish()
                }
            }

        }

        location = intent.getStringExtra("location")
        if(location == "0,0" || TextUtils.isEmpty(location)) location = MyApi.getInstance().dataApi.location
        this.web_view.webViewClient= WebViewClient()
        loadUrl("http://www.google.com/maps/@" + location + ",12z")



//http://www.google.cn/maps/@44.8786302,-75.5409309,4z
    }

    private fun loadUrl(url: String) {

        this.web_view.loadUrl(url)
    }

//    companion object {
//        val ID = "id"
//        val DATE = "DATE"
//
//
//        fun showMe(content: Context,location:String?) {
//           val intent = Intent(content, MapsActivity::class.java)
//            if(location!=null) intent.putExtra("location", location)
//
//            content.startActivity(intent)
//
//        }
//    }

    class  WebViewClient: android.webkit.WebViewClient(){

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {

            view.loadUrl(url)
            return true
            //return super.shouldOverrideUrlLoading(view,url)
        }
    }
}

