package com.kinetic.fit.ui.analysis;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kinetic.fit.R;
import com.kinetic.fit.data.FitProperty;
import com.kinetic.fit.data.realm_objects.Profile;
import com.kinetic.fit.data.realm_objects.Session;
import com.kinetic.fit.data.session_objects.SessionDataSpan;
import com.kinetic.fit.ui.widget.AnalysisZoneWidget;
import com.kinetic.fit.ui.widget.SubscriptionCalloutWidget;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.Locale;

import static android.view.View.GONE;

/**
 * Created by Saxton on 3/28/17.
 */

@EFragment(R.layout.fragment_lap_isolation)
public class SessionDataSpanFragment extends Fragment {

    Profile mProfile;
    SessionDataSpan dataSpan;
    @ViewById(R.id.power_zones_frame_layout)
    LinearLayout powerZonesFrame;
    @ViewById(R.id.heart_zones_frame_layout)
    LinearLayout heartZoneFrame;
    @ViewById(R.id.lap_isolation_tss_value)
    TextView tssValue;
    @ViewById(R.id.lap_isolation_if_value)
    TextView ifValue;
    @ViewById(R.id.lap_isolation_kj_value)
    TextView kjValue;
    @ViewById(R.id.lap_isolation_normalized_value)
    TextView normalizedValue;
    @ViewById(R.id.lap_isolation_heart_avg_max_value)
    TextView avgHeartMaxValue;
    @ViewById(R.id.lap_isolation_heart_avg_reserve_value)
    TextView avgHeartReserveValue;
    @ViewById(R.id.lap_isolation_heart_min_value)
    TextView minHeartValue;
    @ViewById(R.id.lap_isolation_mean_max_5s_value)
    TextView mm5sValue;
    @ViewById(R.id.lap_isolation_mean_max_20s_value)
    TextView mm20sValue;
    @ViewById(R.id.lap_isolation_mean_max_1m_value)
    TextView mm1mValue;
    @ViewById(R.id.lap_isolation_mean_max_5m_value)
    TextView mm5mValue;
    @ViewById(R.id.lap_isolation_mean_max_20m_value)
    TextView mm20mValue;
    @ViewById(R.id.subscription_callout)
    SubscriptionCalloutWidget calloutText;
    @ViewById(R.id.sub_req_text)
    TextView subscriptionRequired;
    private boolean hasSubscription = false;

    Session session;

    public static SessionDataSpanFragment newInstance() {
        Bundle args = new Bundle();
        SessionDataSpanFragment fragment = new SessionDataSpanFragment();
        fragment.setArguments(args);
        fragment.setRetainInstance(false);
        return fragment;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public void setSpan(SessionDataSpan span) {
        this.dataSpan = span;
    }

    public void setHasSubscription(boolean hasSubscription) {
        this.hasSubscription = hasSubscription;
    }

    public void redraw() {
        this.setSpanPowerValues();
        this.setSpanHeartValues();
        this.setSpanMeanMaxValues();
    }

    @AfterViews
    void afterFragViews() {
        mProfile = Profile.current();
        for (int i = 0; i < mProfile.getPowerZonesCache().size(); i++) {
            AnalysisZoneWidget widget = new AnalysisZoneWidget(getContext(), session);
            widget.setZoneProperties(FitProperty.Power, dataSpan, i);
            powerZonesFrame.addView(widget);
        }
        for (int i = 0; i < mProfile.getHeartZonesCache().size(); i++) {
            AnalysisZoneWidget widget = new AnalysisZoneWidget(getContext(), session);
            widget.setZoneProperties(FitProperty.HeartRate, dataSpan, i);
            heartZoneFrame.addView(widget);
        }
        setSpanPowerValues();
        setSpanHeartValues();
        if (hasSubscription) {
            calloutText.setVisibility(GONE);
            subscriptionRequired.setVisibility(GONE);
            setSpanMeanMaxValues();
        } else {
            calloutText.setCalloutText(getString(R.string.analysis_activity_subscription_callout_text));
        }
    }

    void setSpanPowerValues() {
        if (dataSpan != null) {
            tssValue.setText(Math.round(dataSpan.getTrainingStressScore()) > 0 ? String.format(Locale.getDefault(), "%1$d", Math.round(dataSpan.getTrainingStressScore())) : getString(R.string.empty_string));
            ifValue.setText(dataSpan.getIntensityFactor() > 0 ? String.format(Locale.getDefault(), "%1$.2f", dataSpan.getIntensityFactor()) : getString(R.string.empty_string));
            kjValue.setText(getResources().getString(R.string.analysis_kj_formatter, String.format(Locale.getDefault(), "%1$.2f", dataSpan.getKilojoules())));
            normalizedValue.setText(dataSpan.getNormalizedPower() > 0 ? getResources().getString(R.string.watt_formatter_integer, (int) dataSpan.getNormalizedPower()) : getString(R.string.empty_string));
        }
    }

    void setSpanHeartValues() {
        if (dataSpan != null) {
            avgHeartMaxValue.setText(dataSpan.getAvgHeartRateMaxPercent() > 0 ? getResources().getString(R.string.analysis_zone_heart_rate_percent_formatter, dataSpan.getAvgHeartRateMaxPercent()) : getString(R.string.empty_string));
            avgHeartReserveValue.setText(dataSpan.getAvgHeartRateReservePercent() > 0 ? getResources().getString(R.string.analysis_zone_heart_rate_percent_formatter, dataSpan.getAvgHeartRateReservePercent()) : getString(R.string.empty_string));
            minHeartValue.setText(dataSpan.getMinHeartRate() > 0 ? getResources().getString(R.string.heart_rate_formatter, dataSpan.getMinHeartRate()) : getString(R.string.empty_string));
        }
    }

    void setSpanMeanMaxValues() {
        if (dataSpan != null) {
            mm5sValue.setText(getResources().getString(R.string.watt_formatter_integer, dataSpan.getMeanMaximums().get(0).intValue()));
            mm20sValue.setText(getResources().getString(R.string.watt_formatter_integer, dataSpan.getMeanMaximums().get(1).intValue()));
            mm1mValue.setText(getResources().getString(R.string.watt_formatter_integer, dataSpan.getMeanMaximums().get(2).intValue()));
            mm5mValue.setText(getResources().getString(R.string.watt_formatter_integer, dataSpan.getMeanMaximums().get(3).intValue()));
            mm20mValue.setText(getResources().getString(R.string.watt_formatter_integer, dataSpan.getMeanMaximums().get(4).intValue()));
        }
    }
}
