package com.levaniphoenix.app

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.GestureDetectorCompat
import java.io.File

class ResizableLayout : RelativeLayout  {
    private var windowLayoutParams: WindowManager.LayoutParams? = null
    private lateinit var mContext: Context

    private val TAG: String = ResizableLayout::class.java.getName()
    constructor(context: Context?) : super(context) {
        init(context)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    fun init(context: Context?) {
        mContext = context!!

    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        dragHandle = findViewById(R.id.resize_view)
    }
    private var dragHandle: View? = null

    private fun openScreenshot(imageFile: File) {
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        val uri: Uri = Uri.fromFile(imageFile)
        intent.setDataAndType(uri, "image/*")
        startActivity(context,intent,null)
    }
    fun passWindowsLayout(windowLayoutParams: WindowManager.LayoutParams?) {
        this.windowLayoutParams = windowLayoutParams
    }
}