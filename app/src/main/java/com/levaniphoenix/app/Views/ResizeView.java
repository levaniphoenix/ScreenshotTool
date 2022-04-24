package com.levaniphoenix.app.Views;

import static android.content.Context.WINDOW_SERVICE;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.core.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class ResizeView extends LinearLayout implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener
{
    protected Context context;
    private GestureDetectorCompat mDetector;
    protected WindowManager windowManager;
    protected ViewGroup.LayoutParams params;
    protected WindowManager.LayoutParams windowLayoutParams;
    protected View rootView;
    protected int initialWidth;
    protected int initialHeight;
    protected float distanceX = 0f;
    protected float distanceY= 0f;
    private static final String TAG = ResizeView.class.getName();

    public ResizeView(Context context)
    {
        super(context);
        init(context);
    }

    public ResizeView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context);
    }

    public ResizeView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context)
    {
        mDetector = new GestureDetectorCompat(context, this);
        mDetector.setOnDoubleTapListener(this);
        this.context = context;
        windowManager = (WindowManager) this.context.getSystemService(WINDOW_SERVICE);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent e)
    {
        mDetector.onTouchEvent(e);

        Log.d(TAG,"onTouchEvent");

        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e)
    {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e)
    {
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e)
    {
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e)
    {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e)
    {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e)
    {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
    {
        //Log.d(TAG,"onScroll");
        //Log.d(TAG,String.format("distanceX %f", distanceX));
        //Log.d(TAG,String.format("distanceY %f", distanceY));
        //Log.d(TAG,params.toString());
//        if (e1 == null || e2 == null){
//            return false;
//        }

        this.distanceX = this.distanceX + distanceX;
        this.distanceY = this.distanceY + distanceY;
        params.width = initialWidth - (int) this.distanceX;
        params.height = initialHeight - (int) this.distanceY;

        Log.d(TAG,String.format("setting width and height %d %d", params.width ,params.height));
        Log.d(TAG,String.format("position %d %d", windowLayoutParams.x ,windowLayoutParams.y));
        //todo fix box bounds
        //fixBoxBounds();
        windowManager.updateViewLayout(rootView, params);
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e)
    {
        Log.d(TAG, "long press");
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
    {
        return false;
    }
    public void passView(View rootView){
        this.rootView = rootView;
        params = rootView.getLayoutParams();
        initialWidth = rootView.getWidth();
        initialHeight = rootView.getHeight();
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                initialHeight = rootView.getHeight();
                initialWidth = rootView.getWidth();
            }
        });
    }
    public void passWindowsLayout(WindowManager.LayoutParams windowLayoutParams){
        this.windowLayoutParams = windowLayoutParams;
    }
}