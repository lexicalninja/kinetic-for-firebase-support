package com.kinetic.fit.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class FitLinearLayout extends LinearLayout {

    public FitLinearLayout(Context context) {
        super(context);
    }

    public FitLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FitLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

//    public FitLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
//        super(context, attrs, defStyleAttr, defStyleRes);
//    }

    public float getXFraction() {
        return getX() / getWidth(); // TODO: guard divide-by-zero
    }

    public void setXFraction(float xFraction) {
        // TODO: cache width
        final int width = getWidth();
        setX((width > 0) ? (xFraction * width) : -9999);
    }
}
