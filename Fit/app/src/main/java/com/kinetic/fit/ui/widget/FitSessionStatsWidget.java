package com.kinetic.fit.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kinetic.fit.R;
import com.kinetic.fit.util.Conversions;
import com.kinetic.fit.util.ViewStyling;

/**
 * Created by Saxton on 4/4/17.
 */

public class FitSessionStatsWidget extends LinearLayout {
    TextView duration;
    TextView calories;
    TextView distance;


    public FitSessionStatsWidget(Context context) {
        super(context);
        init();
    }

    public FitSessionStatsWidget(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FitSessionStatsWidget(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FitSessionStatsWidget(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init(){
        inflate(getContext(), R.layout.session_stats_layout, this);
        duration = (TextView) findViewById(R.id.session_duration);
        calories = (TextView) findViewById(R.id.session_calories);
        distance = (TextView) findViewById(R.id.session_distance);
    }

    public void setDuration(double duration){
        this.duration.setText(duration > 0 ? ViewStyling.timeToStringMS(duration) : getContext().getString(R.string.empty_string));
    }

    public void setCalories(double calories){
        this.calories.setText(calories > 0 ? getContext().getString(R.string.analysis_calories_burned_formatter, calories) : getContext().getString(R.string.empty_string));
    }

    public void setDistance(double distance, boolean isMetric){
        if(distance > 0) {
            if (isMetric) {
                this.distance.setText(getContext().getString(R.string.analysis_distance_formatter_km, distance));
            } else {
                this.distance.setText(getContext().getString(R.string.analysis_distance_formatter_mi, Conversions.kph_to_mph(distance)));
            }
        }else {
            this.distance.setText(getContext().getString(R.string.empty_string));
        }
    }
}
