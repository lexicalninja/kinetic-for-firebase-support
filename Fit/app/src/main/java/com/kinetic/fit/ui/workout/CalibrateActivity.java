package com.kinetic.fit.ui.workout;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.kinetic.fit.R;
import com.kinetic.fit.connectivity.SensorDataService;
import com.kinetic.fit.connectivity.SensorDataService_;
import com.kinetic.fit.connectivity.sensors.CoastCalibrationSensor;
import com.kinetic.fit.connectivity.sensors.Sensor;
import com.kinetic.fit.controllers.SessionController;
import com.kinetic.fit.controllers.SessionController_;
import com.kinetic.fit.data.shared_prefs.SharedPreferencesInterface;
import com.kinetic.fit.ui.FitActivity;
import com.kinetic.fit.ui.workout.WorkoutActivity_;
import com.kinetic.fit.util.Conversions;
import com.kinetic.fit.util.ViewStyling;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.api.UiThreadExecutor;

import java.util.Locale;


/**
 * Created by Saxton on 12/9/16.
 */

@EActivity(R.layout.activity_calibration)
public class CalibrateActivity extends FitActivity implements Sensor.SensorObserver {

    @Extra("sensorId")
    String sensorId;

    private static final String TAG = "CalibrationActivity";
    public static final int CALIBRATION_COMPLETE = 999;

    private SensorDataService.SensorDataServiceBinder mSensorData;
    private ServiceConnection mSensorDataConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mSensorData = (SensorDataService.SensorDataServiceBinder) service;
            Sensor sensor = mSensorData.getSensorById(sensorId);
            mPowerSensor = sensor;
            startCalibration();
        }

        public void onServiceDisconnected(ComponentName className) {
            mSensorData = null;
        }
    };

    private SessionController.SessionControllerBinder mSessionController;
    private ServiceConnection mSessionConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mSessionController = (SessionController.SessionControllerBinder) service;
        }

        public void onServiceDisconnected(ComponentName className) {
            mSessionController = null;
        }
    };


    @ViewById(R.id.calibrate_phase_instruction_text)
    TextView instructText;

    @ViewById(R.id.calibrate_current_speed_text_value)
    TextView currentSpeedText;
    @ViewById(R.id.calibrate_speed_unit_text)
    TextView unitText;
    @ViewById(R.id.calibrate_previous_calibration_time_text)
    TextView previousText;
    @ViewById(R.id.button_right)
    Button mRightButton;
    @ViewById(R.id.button_middle)
    Button mMiddleButton;
    @ViewById(R.id.button_left)
    Button mLeftButton;

    private CoastCalibrationSensor.FITCalibrateCoastState mState = CoastCalibrationSensor.FITCalibrateCoastState.Unknown;

    private CoastCalibrationSensor mCoastSensor;
    private Sensor mPowerSensor;


    @Click(R.id.button_right)
    void onRightButton() {
        if (mPowerSensor != null) {
            mPowerSensor.removeObserver(this);
            mPowerSensor.stopCalibration();
        }
        UiThreadExecutor.cancelAll("calibration");
        if (getIntent().getIntExtra("sensorSettings", 0) == 999) {
            finish();
        } else {
            WorkoutActivity_.intent(this).start();
            finish();
        }
    }

    @Click(R.id.button_left)
    void onLeftButton() {
        startCalibration();
    }

    private boolean startCalibration() {
        Log.d(TAG, "startCalibration");
        updateUnitText();

        mLeftButton.setText("Reset");
        mRightButton.setText("Cancel");

        mPowerSensor = mSensorData.getPowerSensor();
        if (mPowerSensor != null && mPowerSensor.requiresCalibration()) {
            if (mPowerSensor instanceof CoastCalibrationSensor) {
                mCoastSensor = (CoastCalibrationSensor) mPowerSensor;

                double lastSpindownTime = mCoastSensor.getLastSpindownTime();
                if (lastSpindownTime < 0) {
                    previousText.setText("Previous Calibration Unknown");
                } else {
                    previousText.setText(String.format(Locale.getDefault(), "Previous Calibration: %.2f", lastSpindownTime));
                }
            } else {
                previousText.setText("Previous Calibration Unknown");
            }
            mPowerSensor.addObserver(this);
            mPowerSensor.startCalibration();
            return true;
        } else {
            mPowerSensor = null;
        }
        finish();
        return false;
    }

    private void stopCalibration() {
        if (mPowerSensor != null) {
            mPowerSensor.removeObserver(this);
            mPowerSensor.stopCalibration();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle("Calibration");
        startService(SessionController_.intent(this).get());
        bindService(SessionController_.intent(this).get(), mSessionConnection, BIND_AUTO_CREATE);
        bindService(SensorDataService_.intent(this).get(), mSensorDataConnection, Context.BIND_AUTO_CREATE);
    }


    @AfterViews
    protected void afterCreateViews() {
        mMiddleButton.setVisibility(View.GONE);
    }

    private void updateUnitText() {
        if (SharedPreferencesInterface.isMetric()) {
            unitText.setText("KPH");
        } else {
            unitText.setText("MPH");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onDetach");

        if (mPowerSensor != null) {
            mPowerSensor.removeObserver(this);
            mPowerSensor.stopCalibration();
            mPowerSensor = null;
        }
        if (mCoastSensor != null) {

        }
    }

    @Override
    protected void onPause() {
        if (mSensorData != null) {
            unbindService(mSensorDataConnection);
        }
        if (mSessionConnection != null) {
            unbindService(mSessionConnection);
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void sensorStateChanged(Sensor sensor, Sensor.State state) {
        // meh. do nothing.
    }

    @UiThread(id = "calibration")
    protected void processResult(CoastCalibrationSensor.FITCalibrateCoastResult result, double spindownTime) {

        stopCalibration();

        mRightButton.setText("Close");

        currentSpeedText.setText(String.format("%.2f", spindownTime));
        unitText.setText("");

        switch (result) {
            case Success: {
                instructText.setText("Calibration Successful");
            }
            break;
            case TooFast: {
                instructText.setText("Calibration Unsuccessful");
                ViewStyling.getCustomToast(this, getLayoutInflater(), "Loosen your Trainer's" +
                        "roller and try again").show();
            }
            break;
            case Middle: {
                instructText.setText("Calibration Unsuccessful");
                ViewStyling.getCustomToast(this, getLayoutInflater(), "Tighten your Trainer's " +
                        "roller (or loosen if you have a Pro Flywheel) and try again.").show();
            }
            break;
            case TooSlow: {
                instructText.setText("Calibration Unsuccessful");
                ViewStyling.getCustomToast(this, getLayoutInflater(), "Tighten your Trainer's " +
                        "roller and try again.").show();
            }
            break;
            case Unknown: {
                instructText.setText("Calibration Unsuccessful");
                ViewStyling.getCustomToast(this, getLayoutInflater(), "Congrats! You totally " +
                        "broke checkIsReadyToPay. Try again.").show();
            }
            break;
        }
    }


    @UiThread(id = "calibration")
    protected void setState(CoastCalibrationSensor.FITCalibrateCoastState state) {
        if (mState == state) {
            return;
        }
        mState = state;

        switch (mState) {
            case Initializing:
                instructText.setText("Preparing Calibration\nDo not pedal");
                break;
            case SpeedUp:
                instructText.setText("Start Pedaling To Calibrate");
                if (SharedPreferencesInterface.isMetric()) {
                    instructText.setText(String.format("Speed up to %.0f kph", mCoastSensor.getCalibrationReadySpeedKPH()));
                } else {
                    instructText.setText(String.format("Speed up to %.0f mph", Conversions.kph_to_mph(mCoastSensor.getCalibrationReadySpeedKPH())));
                }
                break;

            case StartCoasting:
                instructText.setText("Stop Pedaling and Coast!");
                break;

            case Coasting:
                instructText.setText("Calibrating. Do not Pedal!");
                break;

            case Complete:
                processResult(mCoastSensor.getLatestResult(), mCoastSensor.getLastSpindownTime());
                break;

            case Unknown:
                break;
        }
    }

    @UiThread(id = "calibration")
    protected void setCurrentSpeed(double speedKPH) {
//        if (!this.isDetached()) {
        if (speedKPH < 0) {
            currentSpeedText.setText("---");
        } else {
            if (SharedPreferencesInterface.isMetric()) {
                currentSpeedText.setText(String.format("%.1f", speedKPH));
            } else {
                currentSpeedText.setText(String.format("%.1f", Conversions.kph_to_mph(speedKPH)));
            }
        }
    }
//    }

    @Override
    public void sensorValueChanged(Sensor sensor) {
        setCurrentSpeed(mCoastSensor.getCurrentSpeedKPH());
        setState(mCoastSensor.getCurrentState());
    }
}
