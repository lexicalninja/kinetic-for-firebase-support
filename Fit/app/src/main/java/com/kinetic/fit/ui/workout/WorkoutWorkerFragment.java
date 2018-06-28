package com.kinetic.fit.ui.workout;

import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import java.util.ArrayList;

/**
 * Created by Saxton on 5/24/17.
 */

public class WorkoutWorkerFragment extends Fragment {

    private ArrayList<PointF> mPowerLineArray;
    private ArrayList<PointF> mHeartRateLineArray;
    private ArrayList<PointF> mCadenceLineArray;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public ArrayList<PointF> getmPowerLineArray() {
        return mPowerLineArray;
    }

    public void setmPowerLineArray(ArrayList<PointF> mPowerLineArray) {
        this.mPowerLineArray = mPowerLineArray;
    }

    public ArrayList<PointF> getmHeartRateLineArray() {
        return mHeartRateLineArray;
    }

    public void setmHeartRateLineArray(ArrayList<PointF> mHeartRateLineArray) {
        this.mHeartRateLineArray = mHeartRateLineArray;
    }

    public ArrayList<PointF> getmCadenceLineArray() {
        return mCadenceLineArray;
    }

    public void setmCadenceLineArray(ArrayList<PointF> mCadenceLineArray) {
        this.mCadenceLineArray = mCadenceLineArray;
    }
}
