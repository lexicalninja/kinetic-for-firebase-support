package com.kinetic.fit.cast;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.kinetic.fit.R;
import com.kinetic.fit.controllers.WorkoutTextAndTime;
import com.kinetic.fit.data.FitProperty;
import com.kinetic.fit.data.realm_objects.Profile;
import com.kinetic.fit.ui.hud.HUDpropertyViewMini;
import com.lb.auto_fit_textview.AutoResizeTextView;


/**
 * Created by Saxton on 4/11/16.
 */


public class SessionCastDialogView extends LinearLayout {

    private AutoResizeTextView lapText;
    private AutoResizeTextView durationText;
    private AutoResizeTextView powerText;
    public HUDpropertyViewMini powerHUD;
    public HUDpropertyViewMini lapHUD;
    private LayoutInflater mLayoutInflator;

    public SessionCastDialogView(Context context) {
        super(context);
        mLayoutInflator = LayoutInflater.from(context);
        init();
    }

    public SessionCastDialogView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mLayoutInflator = LayoutInflater.from(context);
        init();
    }

    public SessionCastDialogView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mLayoutInflator = LayoutInflater.from(context);
        init();
    }

    public void init() {
        mLayoutInflator.inflate(R.layout.cast_lap_dialog_view, this, true);
        lapText = (AutoResizeTextView) findViewById(R.id.cast_dialog_popup_text);
        durationText = (AutoResizeTextView) findViewById(R.id.cast_dialog_popup_duration_text);
        powerText = (AutoResizeTextView) findViewById(R.id.cast_dialog_popup_power_text);
        powerHUD = (HUDpropertyViewMini) findViewById(R.id.cast_dialog_power_view);
        lapHUD = (HUDpropertyViewMini) findViewById(R.id.cast_dialog_laps_view);
        lapHUD.setProperty(FitProperty.WorkoutDurationToGo);
    }

    public void newTextAndTime(WorkoutTextAndTime tat) {
        if (tat != null) {
            lapText.setText(tat.getText().replace("\n", "").replace("\r", ""));
            durationText.setText(tat.getDurationText());
            powerText.setText(tat.getPowerEnd() * Profile.current().getPowerFTP() / 100 + " watts");
        }
    }


}
