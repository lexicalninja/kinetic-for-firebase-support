package com.kinetic.fit.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;

import com.kinetic.fit.R;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

import info.hoang8f.android.segmented.SegmentedGroup;

/**
 * Created by Saxton on 3/14/17.
 */

@EViewGroup
public class CalendarInformationUpdateView extends LinearLayout {
    public final static String TAG = "CalInfoUpdateView";

    public interface CalendarInformationUpdateViewListener {
        void infoTypeSelected(CalendarInformationUpdateView.SortGroups infoType);
    }

    public enum SortGroups{
        DURATION, IF, TSS
    }

    private List<CalendarInformationUpdateViewListener> mListeners;

    public CalendarInformationUpdateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mListeners = new ArrayList<>();
    }

    public void setListener(CalendarInformationUpdateViewListener listener) {
        mListeners.add(listener);
    }

    @ViewById
    SegmentedGroup calendarViewUpdate;

    @Click(R.id.calendar_duration_view)
    void sortByDuration() {
        Log.d(TAG, "@Click");

        if (mListeners != null) {
            for(CalendarInformationUpdateViewListener listener : mListeners) {
                listener.infoTypeSelected(CalendarInformationUpdateView.SortGroups.DURATION);
            }
        }
    }

    @Click(R.id.calendar_if_view)
    void sortByIF() {
        Log.d(TAG, "@Click");

        if (mListeners != null) {
            for(CalendarInformationUpdateViewListener listener : mListeners) {
                listener.infoTypeSelected(CalendarInformationUpdateView.SortGroups.IF);
            }
        }
    }

    @Click(R.id.calendar_tss_view)
    void sortByTSS() {
        Log.d(TAG, "@Click");

        if (mListeners != null) {
            for(CalendarInformationUpdateViewListener listener : mListeners) {
                listener.infoTypeSelected(CalendarInformationUpdateView.SortGroups.TSS);
            }
        }
    }

    public SegmentedGroup getCalendarViewUpdate() {
        return calendarViewUpdate;
    }
}
