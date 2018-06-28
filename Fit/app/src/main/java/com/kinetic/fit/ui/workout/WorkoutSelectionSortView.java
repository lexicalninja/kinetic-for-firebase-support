package com.kinetic.fit.ui.workout;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;

import com.kinetic.fit.R;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import info.hoang8f.android.segmented.SegmentedGroup;



/**
 * Created by Saxton on 2/11/16.
 */

@EViewGroup
public class WorkoutSelectionSortView extends LinearLayout {

    public final static String TAG = "WorkoutSelectionSortVie";

    public interface WorkoutSelectionSortViewListener {
        void workoutSortViewSelected(SortGroups sortType);
    }

    public enum SortGroups{
        NAME, DURATION, IF, TSS
    }
    private WorkoutSelectionSortViewListener mListener;

    public WorkoutSelectionSortView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    void setListener(WorkoutSelectionSortViewListener listener) {
        mListener = listener;
    }

    @ViewById
    SegmentedGroup workoutsort;

    @Click(R.id.workout_name_sort)
    void sortByName() {
        Log.d(TAG, "@Click");
        if (mListener != null) {
            mListener.workoutSortViewSelected(SortGroups.NAME);
        }
    }

    @Click(R.id.workout_duration_sort)
     void sortByDuration() {
        Log.d(TAG, "@Click");

        if (mListener != null) {
            mListener.workoutSortViewSelected(SortGroups.DURATION);
        }
    }

    @Click(R.id.workout_if_sort)
    void sortByIF() {
        Log.d(TAG, "@Click");

        if (mListener != null) {
            mListener.workoutSortViewSelected(SortGroups.IF);
        }
    }

    @Click(R.id.workout_tss_sort)
    void sortByTSS() {
        Log.d(TAG, "@Click");

        if (mListener != null) {
            mListener.workoutSortViewSelected(SortGroups.TSS);
        }
    }

    public SegmentedGroup getWorkoutsort() {
        return workoutsort;
    }
}
