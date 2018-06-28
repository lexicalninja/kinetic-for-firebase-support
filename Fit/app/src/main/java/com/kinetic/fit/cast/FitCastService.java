/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kinetic.fit.cast;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.google.android.gms.cast.CastRemoteDisplayLocalService;
import com.kinetic.fit.connectivity.SensorDataService;
import com.kinetic.fit.connectivity.SensorValues;
import com.kinetic.fit.controllers.SessionController;
import com.kinetic.fit.controllers.SessionController_;
import com.kinetic.fit.controllers.WorkoutTextAndTime;

import org.androidannotations.annotations.EService;
import org.androidannotations.annotations.Receiver;


/**
 * Service to keep the remote display running even when the app goes into the background
 */
@EService
public class FitCastService extends CastRemoteDisplayLocalService implements SessionController.SessionControllerObserver {

    private static final String TAG = "FitCastService";

    private SessionController.SessionControllerBinder mSessionController;
    private ServiceConnection mSessionConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            SessionController.SessionControllerBinder binder = (SessionController.SessionControllerBinder) service;
            setSessionController(binder);
        }

        public void onServiceDisconnected(ComponentName className) {
            setSessionController(null);
            bindToSessionController();
            dismissPresentation();
        }
    };

    private void bindToSessionController() {
        bindService(SessionController_.intent(FitCastService.this).get(), mSessionConnection, 0);
    }

    public void setSessionController(SessionController.SessionControllerBinder sessionController) {
        if (mSessionController != null) {
            mSessionController.unregisterObserver(this);
        }
        mSessionController = sessionController;
        if (mSessionController != null) {
            mSessionController.registerObserver(this);
        }
        createPresentation(getDisplay());
    }

    // Current presentation
    private FitCastPresentation mPresentation;


    @Override
    public void onCreate() {
        super.onCreate();

        bindToSessionController();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unbindService(mSessionConnection);
        setSessionController(null);
    }

    @Override
    public void onCreatePresentation(Display display) {
        createPresentation(display);
    }

    @Override
    public void onDismissPresentation() {
        dismissPresentation();
    }

    private void dismissPresentation() {
        if (mPresentation != null) {
            mPresentation.dismiss();
            mPresentation = null;
        }
    }

    private void createPresentation(Display display) {
        if (display == null) {
            return;
        }

        FitCastPresentation newPresentation = null;
        if (mSessionController != null && mSessionController.getState() != null) {
            switch (mSessionController.getState()) {
                case Idle:
                    if (mPresentation == null || !mPresentation.getClass().equals(RootCastPresentation.class)) {
                        newPresentation = new RootCastPresentation(this, display);
                    }
                    break;
                case Warmup:
                    // make the warmup presentation
                    if (mPresentation == null || !mPresentation.getClass().equals(WarmupCastPresentation.class)) {
                        newPresentation = new WarmupCastPresentation(this, display);
                    }
                    break;
                case WarmupPaused:
                    // make the warmup presentation
                    if (mPresentation == null || !mPresentation.getClass().equals(WarmupCastPresentation.class)) {
                        newPresentation = new WarmupCastPresentation(this, display);
                    }
                    break;
                case Calibration:
                    // make the Calibration presentation
                    if (mPresentation == null || !mPresentation.getClass().equals(CalibrationCastPresentation.class)) {
                        newPresentation = new CalibrationCastPresentation(this, display);
                    }
                    break;
                case Workout:
                    // make the Workout presentation
                    if (mPresentation == null || !mPresentation.getClass().equals(SessionCastPresentation.class)) {
                        newPresentation = new SessionCastPresentation(this, display);
                        ((SessionCastPresentation) newPresentation).setWorkout(mSessionController.getWorkout());
                    }
                    break;
                case WorkoutPaused:
                    // make the Workout presentation
                    if (mPresentation == null || !mPresentation.getClass().equals(SessionCastPresentation.class)) {
                        newPresentation = new SessionCastPresentation(this, display);
                    }
                    break;
                case Complete:
                    if (mPresentation == null || !mPresentation.getClass().equals(RootCastPresentation.class)) {
                        newPresentation = new RootCastPresentation(this, display);
                    }
                    break;
                case Cancelled:
                    if (mPresentation == null || !mPresentation.getClass().equals(RootCastPresentation.class)) {
                        newPresentation = new RootCastPresentation(this, display);
                    }
                    break;
            }
        } else {
            if (mPresentation == null || !mPresentation.getClass().equals(RootCastPresentation.class)) {
                newPresentation = new RootCastPresentation(this, display);
            }
        }

        if (mPresentation != newPresentation && newPresentation != null) {
            dismissPresentation();
            try {
                mPresentation = newPresentation;
                mPresentation.show();
            } catch (WindowManager.InvalidDisplayException ex) {
                Log.e(TAG, "Unable to show presentation, display was removed.", ex);
                dismissPresentation();
            }
        }
    }

    @Override
    public void sessionTick(double timeDelta) {
        if (mPresentation != null) {
            mPresentation.sessionTick(mSessionController, timeDelta);
        }
    }

    @Override
    public void sessionStateChanged(SessionController.SessionState state) {
        createPresentation(getDisplay());

        if (mPresentation != null) {
            mPresentation.sessionStateChanged(mSessionController, state);
        }
    }

    @Override
    public void newWorkoutTextAndTime(WorkoutTextAndTime tat) {
//        mPresentation.showLapDialog(tat);
        if (mPresentation != null) {
            mPresentation.newWorkoutTextAndTime(mSessionController, tat);
        }
    }

    @Receiver(actions = SensorDataService.SENSOR_DATA, registerAt = Receiver.RegisterAt.OnCreateOnDestroy)
    protected void onSensorData(Intent intent) {
        if (mPresentation != null) {
            SensorValues values = SensorValues.getFromBundle(intent.getExtras());
            mPresentation.sensorValues(mSessionController, values);
        }
    }

    public FitCastPresentation getmPresentation() {
        return mPresentation;
    }
}
