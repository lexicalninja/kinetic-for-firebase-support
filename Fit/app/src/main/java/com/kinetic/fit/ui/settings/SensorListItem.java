package com.kinetic.fit.ui.settings;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kinetic.fit.R;
import com.kinetic.fit.connectivity.SensorDataService;
import com.kinetic.fit.connectivity.sensors.Sensor;
import com.kinetic.fit.util.ViewStyling;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EView;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

@EView
public class SensorListItem extends LinearLayout implements Sensor.SensorObserver, SensorDataService.SensorDataObserver {

    @ViewById(R.id.list_item_sensor_name)
    TextView mSensorName;
    @ViewById(R.id.list_item_sensor_status)
    TextView mSensorStatus;
    @ViewById(R.id.list_item_sensor_connect_button)
    ImageButton mConnectButton;
    @ViewById(R.id.list_item_sensor_config_button)
    ImageButton mConfigButton;
    @ViewById(R.id.list_item_sensor_cadence_assigned)
    ImageButton mCadenceButton;
    @ViewById(R.id.list_item_sensor_power_assigned)
    ImageButton mPowerButton;
    @ViewById(R.id.list_item_sensor_speed_assigned)
    ImageButton mSpeedButton;
    @ViewById(R.id.list_item_sensor_heart_assigned)
    ImageButton mHeartButton;

    public SensorListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private Sensor mSensor;
    private SensorDataService.SensorDataServiceBinder mSensorServiceBinder;
    private Resources mResources;
    private Context mContext;

    public void setSensor(Sensor sensor, Context context, SensorDataService.SensorDataServiceBinder sensorServiceBinder) {
        if (mSensor != null) {
            mSensor.removeObserver(this);
        }
        if (mSensorServiceBinder != null) {
            mSensorServiceBinder.unregisterObserver(this);
        }

        mSensor = sensor;
        mSensorServiceBinder = sensorServiceBinder;
        mContext = context;
        mResources = context.getResources();

        mSensorName.setText(sensor.getName());

        mSensor.addObserver(this);
        mSensorServiceBinder.registerObserver(this);

        updateSensorState();
    }

    @Click(R.id.list_item_sensor_connect_button)
    void handleConnectButton() {
        if (mSensor.getState() == Sensor.State.Connected) {
            mSensor.disconnect();
        } else if (mSensor.getState() == Sensor.State.Disconnected) {
            mSensor.connect();
        }
    }

    @Click(R.id.list_item_sensor_config_button)
    void handleConfigSensor() {
         SensorSettingsActivity_.intent(getContext()).extra("sensorId", mSensor.getSensorId()).start();
    }

    @Click(R.id.list_item_sensor_cadence_assigned)
    void handleAssignCadence() {
        if (mSensorServiceBinder.getCadenceSensor() == mSensor) {
            mSensorServiceBinder.setCadenceSensor(null);
        } else {
            mSensorServiceBinder.setCadenceSensor(mSensor);
        }
    }

    @Click(R.id.list_item_sensor_speed_assigned)
    void handleAssignSpeed() {
        if (mSensorServiceBinder.getSpeedSensor() == mSensor) {
            mSensorServiceBinder.setSpeedSensor(null);
        } else {
            mSensorServiceBinder.setSpeedSensor(mSensor);
        }
    }

    @Click(R.id.list_item_sensor_power_assigned)
    void handleAssignPower() {
        if (mSensorServiceBinder.getPowerSensor() == mSensor) {
            mSensorServiceBinder.setPowerSensor(null);
        } else {
            mSensorServiceBinder.setPowerSensor(mSensor);
        }
    }

    @Click(R.id.list_item_sensor_heart_assigned)
    void handleAssignHeart() {
        if (mSensorServiceBinder.getHeartRateSensor() == mSensor) {
            mSensorServiceBinder.setHeartRateSensor(null);
        } else {
            mSensorServiceBinder.setHeartRateSensor(mSensor);
        }
    }

    @Override
    public void sensorStateChanged(Sensor sensor, Sensor.State state) {
        updateSensorState();
    }

    @UiThread
    protected void updateSensorState() {
        switch (mSensor.getState()) {
            case Disconnected:
                mSensorStatus.setText("Disconnected");
                disablePropertyButtons();
                mConnectButton.setColorFilter(ViewStyling.getColor(R.attr.colorFitPrimary, mContext));
                break;
            case Connecting:
                mSensorStatus.setText("Connecting");
                disablePropertyButtons();
                break;
            case Connected:
                mSensorStatus.setText("Connected");
                mConnectButton.setColorFilter(ViewStyling.getColor(R.attr.colorFitPrimary, mContext));
                mConnectButton.setImageResource(R.mipmap.button_connected);
                mConfigButton.setEnabled(true);
                mConfigButton.setColorFilter(ViewStyling.getColor(R.attr.colorFitPrimary, getContext()));
                if (mSensor.providesCadence()) {
                    mCadenceButton.setEnabled(true);
                    mCadenceButton.setAlpha(1.0f);
                } else {
                    mCadenceButton.setEnabled(false);
                    mCadenceButton.setAlpha(0.25f);
                }
                if (mSensor.providesSpeed()) {
                    mSpeedButton.setEnabled(true);
                    mSpeedButton.setAlpha(1.0f);
                } else {
                    mSpeedButton.setEnabled(false);
                    mSpeedButton.setAlpha(0.25f);
                }
                if (mSensor.providesPower()) {
                    mPowerButton.setEnabled(true);
                    mPowerButton.setAlpha(1.0f);
                } else {
                    mPowerButton.setEnabled(false);
                    mPowerButton.setAlpha(0.25f);
                }
                if (mSensor.providesHeartRate()) {
                    mHeartButton.setEnabled(true);
                    mHeartButton.setAlpha(1.0f);
                } else {
                    mHeartButton.setEnabled(false);
                    mHeartButton.setAlpha(0.25f);
                }

                if (mSensorServiceBinder.getCadenceSensor() == mSensor) {
                    mCadenceButton.setColorFilter(ViewStyling.getColor(R.attr.colorFitPrimary, mContext));
                } else {
                    mCadenceButton.setColorFilter(ViewStyling.getColor(R.attr.colorFitDisabled, mContext));
                }
                if (mSensorServiceBinder.getSpeedSensor() == mSensor) {
                    mSpeedButton.setColorFilter(ViewStyling.getColor(R.attr.colorFitPrimary, mContext));
                } else {
                    mSpeedButton.setColorFilter(ViewStyling.getColor(R.attr.colorFitDisabled, mContext));
                }
                if (mSensorServiceBinder.getPowerSensor() == mSensor) {
                    mPowerButton.setColorFilter(ViewStyling.getColor(R.attr.colorFitPrimary, mContext));
                } else {
                    mPowerButton.setColorFilter(ViewStyling.getColor(R.attr.colorFitDisabled, mContext));
                }
                if (mSensorServiceBinder.getHeartRateSensor() == mSensor) {
                    mHeartButton.setColorFilter(ViewStyling.getColor(R.attr.colorFitPrimary, mContext));
                } else {
                    mHeartButton.setColorFilter(ViewStyling.getColor(R.attr.colorFitDisabled, mContext));
                }
                break;
            case Disconnecting:
                mSensorStatus.setText("Disconnecting");
                disablePropertyButtons();
                break;
        }
    }

    private void disablePropertyButtons() {
        mConnectButton.setColorFilter(ViewStyling.getColor(R.attr.colorFitDisabled, mContext));
        mConnectButton.setImageResource(R.mipmap.button_disconnected);
        mConfigButton.setColorFilter(ContextCompat.getColor(getContext(), R.color.fit_light_gray));
        mConfigButton.setEnabled(false);
        mCadenceButton.setEnabled(false);
        mSpeedButton.setEnabled(false);
        mPowerButton.setEnabled(false);
        mHeartButton.setEnabled(false);
        mCadenceButton.setAlpha(0.25f);
        mSpeedButton.setAlpha(0.25f);
        mPowerButton.setAlpha(0.25f);
        mHeartButton.setAlpha(0.25f);
        mCadenceButton.setColorFilter(ViewStyling.getColor(R.attr.colorFitDisabled, mContext));
        mSpeedButton.setColorFilter(ViewStyling.getColor(R.attr.colorFitDisabled, mContext));
        mPowerButton.setColorFilter(ViewStyling.getColor(R.attr.colorFitDisabled, mContext));
        mHeartButton.setColorFilter(ViewStyling.getColor(R.attr.colorFitDisabled, mContext));
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
        updateSensorState();
    }
}
