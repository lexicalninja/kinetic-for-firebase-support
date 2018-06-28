package com.kinetic.fit.cast;

import android.content.Context;
import android.os.Bundle;
import android.view.Display;

import com.kinetic.fit.R;
import com.kinetic.fit.connectivity.SensorValues;
import com.kinetic.fit.controllers.SessionController;
import com.kinetic.fit.data.FitProperty;
import com.kinetic.fit.ui.hud.HUDPropertyView;

public class RootCastPresentation extends FitCastPresentation {

    private final String TAG = "RootCastPresentation";

    public RootCastPresentation(Context context, Display display) {
        super(context, display);
    }

    HUDPropertyView powerView;
    HUDPropertyView speedView;
    HUDPropertyView cadenceView;
    HUDPropertyView heartView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.cast_root);

        powerView = (HUDPropertyView) findViewById(R.id.powerPropertyView);
        cadenceView = (HUDPropertyView) findViewById(R.id.cadencePropertyView);
        speedView = (HUDPropertyView) findViewById(R.id.speedPropertyView);
        heartView = (HUDPropertyView) findViewById(R.id.heartPropertyView);

        powerView.setProperty(FitProperty.Power);
        speedView.setProperty(FitProperty.SpeedKPH);
        cadenceView.setProperty(FitProperty.Cadence);
        heartView.setProperty(FitProperty.HeartRate);
    }

    @Override
    protected void onStart() {

    }

    @Override
    void sessionStateChanged(SessionController.SessionControllerBinder controller, SessionController.SessionState state) {

    }

    @Override
    void sessionTick(SessionController.SessionControllerBinder controller, double timeDelta) {

    }

    @Override
    void sensorValues(SessionController.SessionControllerBinder controller, SensorValues values) {
        powerView.updateValue(null, values);
        speedView.updateValue(null, values);
        cadenceView.updateValue(null, values);
        heartView.updateValue(null, values);
    }
}

