package com.kinetic.fit.ui.widget;

import android.graphics.drawable.AnimationDrawable;
import android.os.SystemClock;

/**
 * Created by Saxton on 5/4/17.
 */

public class FitAnimationDrawable extends AnimationDrawable {

    private volatile int duration;//its volatile because another thread will update its value
    private int currentFrame;

    public FitAnimationDrawable() {
        currentFrame = 0;
    }

    @Override
    public void run() {
        int n = getNumberOfFrames();
        currentFrame++;
        if (currentFrame >= n) {
            currentFrame = 0;
        }
        selectDrawable(currentFrame);
        scheduleSelf(this, SystemClock.uptimeMillis() + duration);
    }

    public void setDuration(int duration) {
        this.duration = duration;
        //we have to do the following or the next frame will be displayed after the old duration
        unscheduleSelf(this);
        selectDrawable(currentFrame);
        scheduleSelf(this, SystemClock.uptimeMillis() + duration);
    }
}