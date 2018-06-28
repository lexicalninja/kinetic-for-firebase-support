package com.kinetic.fit.ui.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kinetic.fit.R;
import com.kinetic.fit.data.FitProperty;
import com.kinetic.fit.data.realm_objects.Session;
import com.kinetic.fit.data.session_objects.SessionDataSpan;
import com.kinetic.fit.util.ViewStyling;

/**
 * Created by Saxton on 3/28/17.
 */

public class AnalysisZoneWidget extends LinearLayout {

    TextView zoneName;
    TextView zoneDescription;
    TextView zonePercent;
    TextView zoneTime;
    TextView zoneRange;
    FitProperty zonePropertyType;
    int zoneNumber;
    SessionDataSpan span;
    Session session;

    public AnalysisZoneWidget(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AnalysisZoneWidget(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public AnalysisZoneWidget(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public AnalysisZoneWidget(Context context, Session session) {
        super(context);
        this.session = session;
        init();
    }

    public void setZoneProperties(FitProperty type, SessionDataSpan span, int zone) {
        this.zonePropertyType = type;
        this.zoneNumber = zone;
        this.span = span;
        zoneName.setText(getContext().getString(R.string.analysis_zone_number_formatter, (zoneNumber + 1)));
        zoneDescription.setText(getZoneDescription());
        zoneTime.setText(getZoneTime());
        zoneRange.setText(getZoneRange());
        zonePercent.setText(getResources().getString(R.string.percent_formatter, getZonePercent()));
        setZoneTypeAttributes();
    }

    String getZoneDescription() {
        if (zonePropertyType == FitProperty.Power) {
            switch (zoneNumber) {
                case 0: {
                    return getResources().getString(R.string.analysis_zone_name_recovery);
                }
                case 1: {
                    return getResources().getString(R.string.analysis_zone_name_endurance);
                }
                case 2: {
                    return getResources().getString(R.string.analysis_zone_name_tempo);
                }
                case 3: {
                    return getResources().getString(R.string.analysis_zone_name_threshold);
                }
                case 4: {
                    return getResources().getString(R.string.analysis_zone_name_vo2max);
                }
                case 5: {
                    return getResources().getString(R.string.analysis_zone_name_anaerobic);
                }
                case 6: {
                    return getResources().getString(R.string.analysis_zone_name_neuromuscular);
                }
                default: {
                    return null;
                }
            }
        } else if (zonePropertyType == FitProperty.HeartRate) {
            switch (zoneNumber) {
                case 0: {
                    return getResources().getString(R.string.analysis_zone_name_recovery);
                }
                case 1: {
                    return getResources().getString(R.string.analysis_zone_name_aerobic);
                }
                case 2: {
                    return getResources().getString(R.string.analysis_zone_name_tempo);
                }
                case 3: {
                    return getResources().getString(R.string.analysis_zone_name_threshold);
                }
                case 4: {
                    return getResources().getString(R.string.analysis_zone_name_anaerobic);
                }
                default: {
                    return null;
                }
            }
        } else {
            return null;
        }
    }

    String getZoneTime() {
        if (span != null) {
            if (zonePropertyType == FitProperty.Power) {
                return ViewStyling.timeToStringMS(span.getTimeInPowerZones().get(zoneNumber));
            } else if (zonePropertyType == FitProperty.HeartRate) {
                return ViewStyling.timeToStringMS(span.getTimeInHeartRateZones().get(zoneNumber));
            } else {
                return "--:--";
            }
        } else {
            return "--:--";
        }
    }

    String getZoneRange() {
        if (session != null) {
            if (zonePropertyType == FitProperty.Power) {
                if (zoneNumber == 0) {
//                return "<" + mProfile.getPowerZonesCache().get(1);
                    return "<" + session.getProfilePowerZones().get(1);
                } else if (zoneNumber == 6) {
                    return ">" + session.getProfilePowerZones().get(6);
                } else {
                    return session.getProfilePowerZones().get(zoneNumber) + " - " + (session.getProfilePowerZones().get(zoneNumber + 1) - 1);
                }
            } else if (zonePropertyType == FitProperty.HeartRate) {
                if (zoneNumber == 0) {
                    return "<" + session.getProfileHeartZones().get(1);
                } else if (zoneNumber == 4) {
                    return ">" + session.getProfileHeartZones().get(4);
                } else {
                    return session.getProfileHeartZones().get(zoneNumber) + " - " + (session.getProfileHeartZones().get(zoneNumber + 1) - 1);
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    void setZoneTypeAttributes() {
        if (zonePropertyType == FitProperty.Power) {
            zonePercent.setTextColor(ViewStyling.getColor(R.attr.colorFitPower, getContext()));
        } else if (zonePropertyType == FitProperty.HeartRate) {
            zonePercent.setTextColor(ViewStyling.getColor(R.attr.colorFitHeart, getContext()));
        }
    }

    void init() {
        inflate(getContext(), R.layout.widget_analysis_zone, this);
        zoneName = (TextView) findViewById(R.id.zone_name);
        zoneDescription = (TextView) findViewById(R.id.zone_description);
        zonePercent = (TextView) findViewById(R.id.zone_percent);
        zoneTime = (TextView) findViewById(R.id.zone_time);
        zoneRange = (TextView) findViewById(R.id.zone_range);
    }

    int getZonePercent() {
        if (span != null) {
            if (zonePropertyType == FitProperty.Power) {
                return (int) ((span.getTimeInPowerZones().get(zoneNumber) / span.getDuration()) * 100);
            } else if (zonePropertyType == FitProperty.HeartRate) {
                return (int) ((span.getTimeInHeartRateZones().get(zoneNumber) / span.getDuration()) * 100);
            } else return 0;
        } else {
            return 0;
        }
    }
}
