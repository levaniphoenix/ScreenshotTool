package com.levaniphoenix.app.Interfaces;

import android.view.GestureDetector;
import android.view.MotionEvent;

public interface WindowListener extends GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
    boolean onTouch(MotionEvent e);
    boolean onResize(MotionEvent e);
}