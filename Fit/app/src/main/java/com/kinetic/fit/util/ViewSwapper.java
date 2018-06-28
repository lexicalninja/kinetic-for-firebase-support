package com.kinetic.fit.util;

import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Saxton on 7/17/17.
 */

public class ViewSwapper {
    public static ViewGroup getParent(View view) {
        return (ViewGroup)view.getParent();
    }

    public static void removeView(View view) {
        ViewGroup parent = getParent(view);
        if(parent != null) {
            parent.removeView(view);
        }
    }

    public static void replaceView(View currentView, View newView) {
        ViewGroup parent = getParent(currentView);
        if(parent == null) {
            return;
        }
        final int index = parent.indexOfChild(currentView);
        removeView(currentView);
        parent.addView(newView, index);
    }

}
