package com.kinetic.fit.cast;

import android.content.Context;
import android.os.Bundle;
import android.view.Display;

import com.kinetic.fit.R;
import com.kinetic.fit.connectivity.SensorValues;
import com.kinetic.fit.controllers.SessionController;

public class CalibrationCastPresentation extends FitCastPresentation {

    private final String TAG = "CalibrationCastPresentation";

    public CalibrationCastPresentation(Context context, Display display) {
        super(context, display);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.cast_calibration);
    }

    @Override
    protected void onStart() {

    }

    @Override
    void sessionStateChanged(SessionController.SessionControllerBinder controller,  SessionController.SessionState state) {

    }

    @Override
    void sessionTick(SessionController.SessionControllerBinder controller,  double timeDelta) {

    }

    @Override
    void sensorValues(SessionController.SessionControllerBinder controller,  SensorValues values) {

    }
}
