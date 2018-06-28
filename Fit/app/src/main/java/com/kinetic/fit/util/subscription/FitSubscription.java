package com.kinetic.fit.util.subscription;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.gson.JsonObject;
import com.kinetic.fit.connectivity.SensorDataService;
import com.kinetic.fit.connectivity.SensorDataService_;
import com.kinetic.fit.connectivity.sensors.Sensor;
import com.kinetic.fit.connectivity.sensors.ble.InRideSensor;
import com.kinetic.fit.data.DataSync;
import com.kinetic.fit.data.DataSync_;
import com.kinetic.fit.data.realm_objects.Profile;
import com.koushikdutta.async.future.FutureCallback;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EService;
import org.androidannotations.annotations.Receiver;

import java.util.HashSet;

@EService
public class FitSubscription extends Service implements SensorDataService.SensorDataObserver, Sensor.SensorObserver {

    private static final String TAG = "FitSubscription";
    private static final String FUNCTION_NAME = "kineticDeviceConnected";

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        FitSubscription getService() {
            return FitSubscription.this;
        }
    }

    private HashSet<String> inRidesChecked;
    SharedPreferences sharedPreferences;

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private SensorDataService.SensorDataServiceBinder mSensorsData;
    private ServiceConnection mSensorsDataConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mSensorsData = (SensorDataService.SensorDataServiceBinder) service;
            mSensorsData.registerObserver(FitSubscription.this);
        }

        public void onServiceDisconnected(ComponentName className) {
            mSensorsData = null;
        }
    };

    private DataSync.DataSyncBinder mDataSync;
    private ServiceConnection mDataSyncConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mDataSync = (DataSync.DataSyncBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    public void onCreate() {
        super.onCreate();
        sharedPreferences = getSharedPreferences(TAG, MODE_PRIVATE);
        inRidesChecked = (HashSet<String>) sharedPreferences.getStringSet("inridesChecked", new HashSet<String>());
        bindService(SensorDataService_.intent(this).get(), mSensorsDataConnection, Context.BIND_AUTO_CREATE);
        bindService(DataSync_.intent(this).get(), mDataSyncConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(mDataSyncConnection);
        unbindService(mSensorsDataConnection);
    }

    @Background(delay = 3000)
    void checkForId(final InRideSensor inRide) {
        if (inRidesChecked.contains(inRide.getInRideId())) {
            return;
        }
        if (inRide.getInRideId() != null) {
            Log.d(TAG, "Connected to inRide: " + inRide.getInRideId());
            if (Profile.current() != null) {
                inRidesChecked.add(inRide.getInRideId());
                sharedPreferences.edit().putStringSet("inridesChecked", inRidesChecked).apply();
                if (mDataSync != null) {
                    mDataSync.sendTrialParseFunction(inRide.getInRideId(), FUNCTION_NAME, new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject results) {
                            if (e == null) {
                                if (results.getAsJsonObject("result").get("eligible").getAsBoolean()) {
                                    startActivateActivity(inRide.getInRideId(), results.getAsJsonObject("result").get("months").getAsInt());
                                }
                            } else {
                                Crashlytics.logException(e);
//                                Log.e(TAG, e.getLocalizedMessage());
                            }
                        }
                    });
                }
            }
        }
    }

    private void startActivateActivity(final String inRideId, int months) {
        Intent intent = new Intent(this, FitActivateTrialActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(FitActivateTrialActivity.INRIDE_ID, inRideId);
        intent.putExtra(FitActivateTrialActivity.TRIAL_MONTHS, months);
        startActivity(intent);
    }

    @Override
    public void sensorAdded(Sensor sensor) {
        if (sensor.getClass().equals(InRideSensor.class)) {
            sensor.addObserver(this);
        }
    }

    @Override
    public void sensorAssignmentsChanged() {

    }

    @Override
    public void sensorRemoved(Sensor sensor) {

    }

    @Override
    public void sensorStateChanged(Sensor sensor, Sensor.State state) {
        if (state == Sensor.State.Connected) {
            InRideSensor inRide = (InRideSensor) sensor;
            checkForId(inRide);
        }
    }

    @Override
    public void sensorValueChanged(Sensor sensor) {

    }

    @Receiver(actions = Profile.LOGGED_OUT, registerAt = Receiver.RegisterAt.OnCreateOnDestroy)
    protected void onProfileLoggedOut() {
        inRidesChecked.clear();
    }

}
