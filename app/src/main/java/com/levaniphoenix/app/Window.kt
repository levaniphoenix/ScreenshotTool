package com.levaniphoenix.app

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.*
import androidx.annotation.RequiresApi
import com.levaniphoenix.app.Views.ResizeView


class Window(  // declaring required variables
    private val context: Context
) {
    private val mView: View
    private var mParams: WindowManager.LayoutParams? = null
    private val mWindowManager: WindowManager
    private val layoutInflater: LayoutInflater
    fun open() {
        try {
            // check if the view is already
            // inflated or present in the window
            if (mView.windowToken == null) {
                if (mView.parent == null) {
                    mWindowManager.addView(mView, mParams)
                    mView.findViewById<ResizableLayout>(R.id.resize).setOnTouchListener(object: View.OnTouchListener{
                        var initialX = 0
                        var initialY = 0
                        private var initialTouchX = 0f
                        private var initialTouchY = 0f

                        @RequiresApi(Build.VERSION_CODES.R)
                        override fun onTouch(v: View, event: MotionEvent): Boolean {
                            when (event.action) {
                                MotionEvent.ACTION_DOWN -> {
                                    Log.d("AD", "Action Down")
                                    initialX = mParams!!.x
                                    initialY = mParams!!.y
                                    initialTouchX = event.rawX
                                    initialTouchY = event.rawY
                                    return true
                                }
                                MotionEvent.ACTION_UP -> {
                                    Log.d("AD", "Action Up")
                                    val Xdiff = (event.rawX - initialTouchX).toInt()
                                    val Ydiff = (event.rawY - initialTouchY).toInt()
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
                                    return true
                                }
                            }
                            return false
                        }
                    })

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
            mParams!!.y= 0;

        }
        // getting a LayoutInflater
        layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        // inflating the view with the custom layout we created
        mView = layoutInflater.inflate(R.layout.popup_window, null)
        //mView = layoutInflater.inflate(R.layout.sample_resizable_layout, null)
        // Define the position of the
        // window within the screen
        mWindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }
}