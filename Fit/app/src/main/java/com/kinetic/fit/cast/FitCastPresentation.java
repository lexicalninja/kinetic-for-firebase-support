package com.kinetic.fit.cast;

import android.content.Context;
import android.view.Display;
import android.widget.Toast;

import com.google.android.gms.cast.CastPresentation;
import com.kinetic.fit.R;
import com.kinetic.fit.connectivity.SensorValues;
import com.kinetic.fit.controllers.SessionController;
import com.kinetic.fit.controllers.WorkoutTextAndTime;

public abstract class FitCastPresentation extends CastPresentation {
    Context mContext;

    public FitCastPresentation(Context context, Display display) {
        super(context, display, R.style.KineticTheme);
        mContext = context;
    }

    abstract void sessionTick(SessionController.SessionControllerBinder controller, double timeDelta);

    abstract void sessionStateChanged(SessionController.SessionControllerBinder controller, SessionController.SessionState state);

    abstract void sensorValues(SessionController.SessionControllerBinder controller, SensorValues values);

    void newWorkoutTextAndTime(SessionController.SessionControllerBinder controller, WorkoutTextAndTime tat) {

    }


}
