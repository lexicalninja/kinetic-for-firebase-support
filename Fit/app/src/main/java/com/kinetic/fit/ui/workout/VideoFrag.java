package com.kinetic.fit.ui.workout;

import android.support.v4.app.Fragment;

/**
 * Created by Saxton on 7/27/17.
 */

public abstract class VideoFrag extends Fragment {
    abstract public void start();
    abstract public void pause();
    abstract public void resume();
    abstract public void updateValues();
}
