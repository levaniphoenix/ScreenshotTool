package com.levaniphoenix.app.Views;

import static android.content.Context.WINDOW_SERVICE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class ResizeView extends LinearLayout implements View.OnTouchListener
{
    protected Context context;
    protected WindowManager windowManager;
    protected ViewGroup.LayoutParams params;
    protected WindowManager.LayoutParams windowLayoutParams;
    protected View rootView;
    protected int rootWidth;
    protected int rootHeight;
    private static final String TAG = ResizeView.class.getName();

    private long mParamUpdateTimer = System.currentTimeMillis();
    private int mDX;
    private int mDY;

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

    @SuppressLint("ClickableViewAccessibility")
    private void init(Context context)
    {
        this.context = context;
        windowManager = (WindowManager) this.context.getSystemService(WINDOW_SERVICE);
        this.setOnTouchListener(this);
    }
    public void passView(View rootView){
        this.rootView = rootView;
        params = rootView.getLayoutParams();
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                rootHeight = rootView.getHeight();
                rootWidth = rootView.getWidth();
            }
        });
    }
    public void passWindowsLayout(WindowManager.LayoutParams windowLayoutParams){
        this.windowLayoutParams = windowLayoutParams;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDX = rootWidth - (int) event.getRawX();
                mDY = rootHeight - (int) event.getRawY();
                return true;
            case MotionEvent.ACTION_MOVE:
                params.width = mDX + (int) event.getRawX();
                params.height = mDY + (int) event.getRawY();
                long currTime = System.currentTimeMillis();
                if (currTime - mParamUpdateTimer > 50) {
                    mParamUpdateTimer = currTime;
                    windowManager.updateViewLayout(rootView, params);
                    rootHeight = params.height;
                    rootWidth = params.width;
                }
                return true;
        }
        return false;
    }
}