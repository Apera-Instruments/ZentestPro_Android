package com.zen.ui

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.recyclerview.widget.LinearLayoutManager

import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.TextView

import com.zen.ui.adapter.AssectListAdapter
import com.zen.ui.base.BaseActivity
import com.zen.ui.utils.AssectListview
import com.zen.ui.utils.AssectSaveData

import java.io.File
import java.util.ArrayList
import java.util.Random
import java.util.concurrent.atomic.AtomicInteger
import java.util.logging.Logger


class AssecTwoActivity : BaseActivity() {

    private val assectList = ArrayList<AssectSaveData>()


    private val pth: String? = null
    private var lineLeftTextView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assect)

        lineLeftTextView = this.findViewById<TextView>(R.id.tv_left)
        lineLeftTextView?.setOnClickListener {
            this.finish()
        }

        val textView = this.findViewById<TextView>(R.id.tv_newright)
        textView.visibility = View.INVISIBLE

        initAssect()
        val recyclerView = this.findViewById<View>(R.id.Recycler_View) as RecyclerView
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = OrientationHelper.VERTICAL
        recyclerView.layoutManager = layoutManager

        val adapter = AssectListAdapter(assectList, AssectListAdapter.OnReCyclerItemClickListener { V, position ->
            val assectListview = assectList[position]
            val intent = Intent(this, AssectDataActivity::class.java)
            intent.putExtra("Textname", assectListview.name)
            intent.putExtra("Imagename", assectListview.imageID)
            startActivity(intent)
        })
        recyclerView.adapter = adapter
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
}
