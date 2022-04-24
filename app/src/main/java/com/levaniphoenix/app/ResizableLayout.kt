package com.levaniphoenix.app

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.GestureDetectorCompat
import java.io.File


class ResizableLayout : RelativeLayout,View.OnTouchListener, GestureDetector.OnDoubleTapListener, GestureDetector.OnGestureListener  {

    //todo make double tap work on the whole view
    private lateinit var mDetector: GestureDetectorCompat
    private var windowLayoutParams: WindowManager.LayoutParams? = null
    private lateinit var mContext: Context
    private val windowBorderSizeDp = 2

    private val TAG: String = ResizableLayout::class.java.getName()
    constructor(context: Context?) : super(context) {
        init(context)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    fun init(context: Context?) {
        mContext = context!!
        mDetector = GestureDetectorCompat(context, this)
        mDetector.setOnDoubleTapListener(this)

    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        dragHandle = findViewById(R.id.resize_view)
    }
    private var dragHandle: View? = null

    override fun onInterceptTouchEvent(motionEvent: MotionEvent): Boolean {
        onTouch(this, motionEvent)
        return false
    }

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
    mDetector.onTouchEvent(motionEvent)
    Log.d(TAG, "onTouchEvent")
    return true
    }

    override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
        return false
    }

    override fun onDoubleTap(e: MotionEvent?): Boolean {
        Log.d(TAG,"taking a screenshot")
        Log.d(TAG,rootView.width.toString())
        val intent = Intent(
            context,
            ForegroundService::class.java
        )
        intent.putExtra("screenshotRequest", 1)
        intent.putExtra("width", rootView.width)
        intent.putExtra("height", rootView.height)
        intent.putExtra("windowX", windowLayoutParams!!.x)
        intent.putExtra("windowY", windowLayoutParams!!.y)
        intent.putExtra("borderSize", dpToPx(mContext, windowBorderSizeDp))
        context.startService(intent)
        return true
    }

    override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
        Log.d(TAG,"on single tap")
        return false
    }

    override fun onDown(e: MotionEvent?): Boolean {
        return false
    }

    override fun onShowPress(e: MotionEvent?) {

    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        return false
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        return false
    }

    override fun onLongPress(e: MotionEvent?) {

    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        return false
    }
//    private fun takeScreenshot() {
//        val now = Date()
//        DateFormat.format("yyyy-MM-dd_hh:mm:ss", now)
//        try {
//            // image naming and path  to include sd card  appending name you choose for file
//            val mPath = context.getExternalFilesDir(null).toString() + "/" + now + ".jpg"
//
//            // create bitmap screen capture
//            val v1: View =  getActivity().getWindow().getDecorView()
//            val bitmap = getBitmapFromView(v1)
//            val imageFile = File(mPath)
//            val outputStream = FileOutputStream(imageFile)
//            val quality = 100
//            bitmap?.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
//            outputStream.flush()
//            outputStream.close()
//            openScreenshot(imageFile)
//        } catch (e: Throwable) {
//            // Several error may come out with file handling or DOM
//            e.printStackTrace()
//        }
//    }
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
    fun dpToPx(context: Context, dp: Int): Int
    {
        val displayMetrics = context.resources.displayMetrics
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))
    }
}