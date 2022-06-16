package com.levaniphoenix.app

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import androidx.core.view.GestureDetectorCompat
import com.levaniphoenix.app.Views.ResizeView


class Window(private val context: Context) : View.OnTouchListener, GestureDetector.OnDoubleTapListener, GestureDetector.OnGestureListener {

    private val mView: View
    private var mParams: WindowManager.LayoutParams? = null
    private val mWindowManager: WindowManager
    private val layoutInflater: LayoutInflater
    private lateinit var mDetector: GestureDetectorCompat
    private val TAG: String = ResizableLayout::class.java.getName()

    var initialX = 0
    var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private val windowBorderSizeDp = 2

    @SuppressLint("ClickableViewAccessibility")
    fun open() {
        try {
            // check if the view is already
            // inflated or present in the window
            if (mView.windowToken == null) {
                if (mView.parent == null) {
                    mWindowManager.addView(mView, mParams)
                    mView.findViewById<ResizableLayout>(R.id.resize).setOnTouchListener(this)
                    mView.findViewById<ResizeView>(R.id.resize_view).passView(mView)
                    mView.findViewById<ResizeView>(R.id.resize_view).passWindowsLayout(mParams)
                    mView.findViewById<ResizableLayout>(R.id.resize).passWindowsLayout(mParams)
                }
            }
        } catch (e: Exception) {
            Log.d("Error1", e.toString())
        }
    }

    fun close() {
        try {
            // remove the view from the window
            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).removeView(mView)
            // invalidate the view
            mView.invalidate()
            // remove all views
            (mView.parent as ViewGroup).removeAllViews()
        } catch (e: Exception) {
            Log.d("Error2", e.toString())
        }
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // set the layout parameters of the window
            mParams = WindowManager.LayoutParams( // Shrink the window to wrap the content rather
                // than filling the screen
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,  // Display it on top of other application windows
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,  // Don't let it grab the input focus
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,  // Make the underlying application window visible
                // through any transparent parts
                PixelFormat.TRANSLUCENT
            )
            mParams!!.gravity= Gravity.TOP or Gravity.START
            mParams!!.x = 0
            mParams!!.y= 0
            mParams!!.height= dpToPx(context,200)

            mDetector = GestureDetectorCompat(context, this)
            mDetector.setOnDoubleTapListener(this)

        }
        // getting a LayoutInflater
        layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        // inflating the view with the custom layout we created
        mView = layoutInflater.inflate(R.layout.popup_window, null)
        mWindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (event != null) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    Log.d("AD", "Action Down")
                    initialX = mParams!!.x
                    initialY = mParams!!.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    mDetector.onTouchEvent(event)
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    Log.d("AD", "Action Up")
                    val Xdiff = (event.rawX - initialTouchX).toInt()
                    val Ydiff = (event.rawY - initialTouchY).toInt()
                    mDetector.onTouchEvent(event)
                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    Log.d("AD", "Action Move")
                    Log.d(
                        "AD",
                        String.format(
                            "position %d %d",
                            mParams?.x,
                            mParams?.y
                        )
                    )

                    //todo make api 14 compliant
                    val metrics = mWindowManager.maximumWindowMetrics
                    if (initialX + (event.rawX - initialTouchX).toInt() > metrics.bounds.width()- mView.width)
                        mParams?.x =  metrics.bounds.width() - mView.width
                    else
                        mParams?.x = initialX + (event.rawX - initialTouchX).toInt()

                    if (initialY + (event.rawY - initialTouchY).toInt() > metrics.bounds.height()- mView.height)
                        mParams?.y =  metrics.bounds.height() - mView.height
                    else
                        mParams?.y = initialY + (event.rawY - initialTouchY).toInt()

                    //mParams?.y = initialY + (event.rawY - initialTouchY).toInt()
                    if(mParams!!.x < 0 ) mParams!!.x = 0
                    if(mParams!!.y < 0 ) mParams!!.y = 0
                    mWindowManager.updateViewLayout(mView, mParams)
                    mDetector.onTouchEvent(event)
                    return true
                }
            }
        }
        return false
    }

    override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
        Log.d(TAG, "single tap")
        return true
    }

    @SuppressLint("RestrictedApi")
    override fun onDoubleTap(e: MotionEvent?): Boolean {
        val intent = Intent(
            context,
            ForegroundService::class.java
        )
        intent.putExtra("screenshotRequest", 1)
        intent.putExtra("width", mView.width)
        intent.putExtra("height", mView.height)
        intent.putExtra("windowX", mParams!!.x)
        intent.putExtra("windowY", mParams!!.y)
        intent.putExtra("borderSize", dpToPx(context, windowBorderSizeDp))
        context.startService(intent)

        val relativeLayout = mView.findViewById<RelativeLayout>(R.id.border)
        val animation = AnimationUtils.loadAnimation(context,R.anim.fade_repeat)
        relativeLayout.startAnimation(animation)
        return true
    }

    override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
        Log.d(TAG,"double tap event bruh")
        return true
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
    fun dpToPx(context: Context, dp: Int): Int
    {
        val displayMetrics = context.resources.displayMetrics
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))
    }
}