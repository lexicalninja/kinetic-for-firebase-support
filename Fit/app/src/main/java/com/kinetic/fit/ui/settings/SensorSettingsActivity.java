package com.kinetic.fit.ui.settings;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kinetic.fit.R;
import com.kinetic.fit.connectivity.SensorDataService;
import com.kinetic.fit.connectivity.SensorDataService_;
import com.kinetic.fit.connectivity.sensors.Sensor;
import com.kinetic.fit.connectivity.sensors.ble.InRideSensor;
import com.kinetic.fit.connectivity.sensors.ble.SmartControlSensor;
import com.kinetic.fit.data.shared_prefs.SharedPreferencesInterface;
import com.kinetic.fit.ui.workout.CalibrateActivity_;
import com.kinetic.fit.ui.FitActivity;
import com.kinetic.fit.ui.widget.FitButton;
import com.kinetic.fit.util.Conversions;
import com.kinetic.fit.util.ViewStyling;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Saxton on 12/6/16.
 */

@EActivity(R.layout.activity_sensor_settings)
public class SensorSettingsActivity extends FitActivity implements Sensor.SensorObserver, SensorDataService.SensorDataObserver {

    @ViewById(R.id.sensor_settings_sensor_name)
    TextView sensorNameText;

    @ViewById(R.id.sensor_settings_speed_icon)
    ImageView speedSensorIcon;

    @ViewById(R.id.sensor_settings_speed_value)
    TextView speedValueText;

    @ViewById(R.id.sensor_settings_cadence_icon)
    ImageView cadenceSensorIcon;

    @ViewById(R.id.sensor_settings_cadence_value)
    TextView cadenceValueText;

    @ViewById(R.id.sensor_settings_power_icon)
    ImageView powerSensorIcon;

    @ViewById(R.id.sensor_setting_power_value)
    TextView powerValueText;

    @ViewById(R.id.sensor_settings_heart_rate_icon)
    ImageView heartRateSensorIcon;

    @ViewById(R.id.sensor_settings_heart_rate_value)
    TextView heartRateValueText;

    @ViewById(R.id.sensor_settings_button_calibrate_button)
    FitButton calibrateButton;

    @ViewById(R.id.sensor_settings_button_update_firmware)
    FitButton firmwareButton;

    @ViewById(R.id.sensor_settings_button_disconnect)
    FitButton disconnectButton;

    @ViewById(R.id.sensor_settings_firmware_layout)
    LinearLayout firmwareLayout;

    @ViewById(R.id.firmware_version_text)
    TextView firmwareVersion;

    @ViewById(R.id.sensor_settings_calibration_layout)
    LinearLayout calibrateLayout;

    @Extra("sensorId")
    String sensorId;


    Sensor mSensor;

    SensorDataService.SensorDataObserver sensorDataObserver;

    Intent updateIntent;

    private static final String TAG = "SensorActivity";

    private SensorDataService.SensorDataServiceBinder mSensorData;
    private ServiceConnection mSensorDataConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mSensorData = (SensorDataService.SensorDataServiceBinder) service;
            mSensorData.registerObserver(SensorSettingsActivity.this);
            Sensor sensor = mSensorData.getSensorById(sensorId);
            setSensor(sensor);
            if(sensor != null) {
                updateStateViews(sensor.getState());
            }else {
                finish();
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            mSensorData = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindService(SensorDataService_.intent(this).get(), mSensorDataConnection, Context.BIND_AUTO_CREATE);
    }

    @AfterViews
    void afterViews() {
        updateViewsForSensorState();
        firmwareButton.setFitButtonStyle(FitButton.DISABLED);
    }

    @Override
    protected void onDestroy() {
        unbindService(mSensorDataConnection);
        if(mSensor != null) {
            mSensor.removeObserver(this);
        }
        super.onDestroy();
    }

    @UiThread
    public void updateViewsForSensorState() {
        if (mSensor != null) {
            sensorNameText.setText(mSensor.getName());
            if (mSensorData != null) {
                if (mSensor.equals(mSensorData.getPowerSensor())) {
                    powerSensorIcon.setColorFilter(ViewStyling.getColor(R.attr.colorFitPrimary, this));
                    powerValueText.setText(getString(R.string.sensor_settings_power_formatter, mSensor.currentPower()));
                }
                if (mSensor.equals(mSensorData.getSpeedSensor())) {
                    speedSensorIcon.setColorFilter(ViewStyling.getColor(R.attr.colorFitPrimary, this));
                    if (SharedPreferencesInterface.isMetric()) {
                        speedValueText.setText(getString(R.string.sensor_settings_speed_formatter, mSensor.currentSpeed()));
                    } else {
                        speedValueText.setText(getString(R.string.sensor_settings_speed_formatter, Conversions.kph_to_mph(mSensor.currentSpeed())));
                    }
                }
                if (mSensor.equals(mSensorData.getCadenceSensor())) {
                    cadenceSensorIcon.setColorFilter(ViewStyling.getColor(R.attr.colorFitPrimary, this));
                    cadenceValueText.setText(getString(R.string.sensor_settings_cadence_formatter, mSensor.currentCadence()));
                }
                if (mSensor.equals(mSensorData.getHeartRateSensor())) {
                    heartRateSensorIcon.setColorFilter(ViewStyling.getColor(R.attr.colorFitPrimary, this));
                    heartRateValueText.setText(getString(R.string.heart_rate_formatter, mSensor.currentHeartRate()));
                }
            }
            if (mSensor instanceof SmartControlSensor) {
                //TODO check if a firmware update is needed and activate firmware update button
                firmwareVersion.setText(getString(R.string.sensor_settings_activity_firmware_version_formatter, ((SmartControlSensor)mSensor).getmFirmwareVersion()));
            }
        }

    }

    public void setSensor(Sensor sensor) {
        mSensor = sensor;
        if (!(mSensor instanceof SmartControlSensor) && !(mSensor instanceof InRideSensor)) {
            calibrateLayout.setVisibility(View.GONE);
            firmwareLayout.setVisibility(View.GONE);
        } else if (mSensor instanceof SmartControlSensor) {
            if (((SmartControlSensor) mSensor).firmwareUpdateAvailable()) {
                firmwareButton.setFitButtonStyle(FitButton.BASIC);
            }
        }
    }

    @Click(R.id.sensor_settings_button_disconnect)
    public void disconnect() {
        if (mSensor.getState() == Sensor.State.Connected) {
            mSensor.disconnect();
            mSensor.removeObserver(this);
            finish();
        }
    }

    @Click(R.id.sensor_settings_button_update_firmware)
    public void updateFirmware() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Smart Control Update");
        builder.setMessage("There is a firmware update available for your Smart Control. Update Now?");
        builder.setNegativeButton(android.R.string.no, null);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent ni = new Intent(SensorDataService.SENSOR_FIRMWARE_UPDATE_START);
                ni.putExtra(SensorDataService.SENSOR_ID, mSensor.getSensorId());
                sendBroadcast(ni);
            }
        });
        builder.show();
    }

    @Click(R.id.sensor_settings_cadence_icon)
    public void toggleCadence() {
        if (mSensor != null && mSensor.providesCadence()) {
            if (mSensorData != null) {
                if (mSensor.equals(mSensorData.getCadenceSensor())) {
                    mSensorData.setCadenceSensor(null);
                    cadenceSensorIcon.setColorFilter(ContextCompat.getColor(this, R.color.fit_light_gray));
                    cadenceValueText.setText(getString(R.string.empty_string));
                } else {
                    mSensorData.setCadenceSensor(mSensor);
                    cadenceSensorIcon.setColorFilter(ViewStyling.getColor(R.attr.colorFitPrimary, this));
                }
            }
        }
    }

    @Click(R.id.sensor_settings_speed_icon)
    public void toggleSpeed() {
        if (mSensor != null && mSensor.providesSpeed()) {
            if (mSensorData != null) {
                if (mSensor.equals(mSensorData.getSpeedSensor())) {
                    mSensorData.setSpeedSensor(null);
                    speedSensorIcon.setColorFilter(ContextCompat.getColor(this, R.color.fit_light_gray));
                    speedValueText.setText(getString(R.string.empty_string));
                } else {
                    mSensorData.setSpeedSensor(mSensor);
                    speedSensorIcon.setColorFilter(ViewStyling.getColor(R.attr.colorFitPrimary, this));
                }
            }
        }
    }

    @Click(R.id.sensor_settings_power_icon)
    public void togglePower() {
        if (mSensor != null && mSensor.providesPower()) {
            if (mSensorData != null) {
                if (mSensor.equals(mSensorData.getPowerSensor())) {
                    mSensorData.setPowerSensor(null);
                    powerSensorIcon.setColorFilter(ContextCompat.getColor(this, R.color.fit_light_gray));
                    powerValueText.setText(getString(R.string.empty_string));
                } else {
                    mSensorData.setPowerSensor(mSensor);
                    powerSensorIcon.setColorFilter(ViewStyling.getColor(R.attr.colorFitPrimary, this));
                }
            }
        }
    }

    @Click(R.id.sensor_settings_heart_rate_icon)
    public void toggleHeartRate() {
        if (mSensor != null && mSensor.providesHeartRate()) {
            if (mSensorData != null) {
                if (mSensor.equals(mSensorData.getHeartRateSensor())) {
                    mSensorData.setHeartRateSensor(null);
                    heartRateSensorIcon.setColorFilter(ContextCompat.getColor(this, R.color.fit_light_gray));
                    heartRateValueText.setText(getString(R.string.empty_string));
                } else {
                    mSensorData.setHeartRateSensor(mSensor);
                    heartRateSensorIcon.setColorFilter(ViewStyling.getColor(R.attr.colorFitPrimary, this));
                }
            }
        }
    }

    @Click(R.id.sensor_settings_button_calibrate_button)
    public void calibrateSensor() {
        if (mSensor != null) {
            CalibrateActivity_.intent(this)
                    .extra("sensorId", mSensor.getSensorId())
                    .extra("sensorSettings", 999)
                    .start();
        }
    }

    @Override
    public void sensorStateChanged(Sensor sensor, Sensor.State state) {
        updateStateViews(state);
    }

    @Override
    public void sensorValueChanged(Sensor sensor) {

    }

    @Override
    public void sensorAdded(Sensor sensor) {

    }

    @Override
    public void sensorRemoved(Sensor sensor) {

    }

    @Override
    public void sensorAssignmentsChanged() {
        updateViewsForSensorState();
    }

    public void updateStateViews(Sensor.State state) {
        // sensor state!
        switch (state) {
            case Disconnected:
                finish();
                break;
            case Connecting:
                sensorNameText.setText("Connecting");
                disconnectButton.setFitButtonStyle(FitButton.DISABLED);
                firmwareButton.setFitButtonStyle(FitButton.DISABLED);
                calibrateButton.setFitButtonStyle(FitButton.DISABLED);
                break;
            case Connected:
                disconnectButton.setFitButtonStyle(FitButton.DESTRUCTIVE);
                disconnectButton.setText("Disconnect");
                if (mSensor.requiresCalibration()) {
                    calibrateButton.setFitButtonStyle(FitButton.BASIC);
                }

                break;
            case Disconnecting:
                sensorNameText.setText("Disconnecting");
                disconnectButton.setFitButtonStyle(FitButton.DISABLED);
                firmwareButton.setFitButtonStyle(FitButton.DISABLED);
                calibrateButton.setFitButtonStyle(FitButton.DISABLED);
                break;
        }
        updateViewsForSensorState();
    }

    @Receiver(actions = SensorDataService.SENSOR_DATA, registerAt = Receiver.RegisterAt.OnCreateOnDestroy)
    protected void onSensorData(Intent intent) {
        updateViewsForSensorState();
    }

}
