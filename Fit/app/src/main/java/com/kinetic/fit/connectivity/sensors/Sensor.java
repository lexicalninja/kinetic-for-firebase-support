package com.kinetic.fit.connectivity.sensors;

import android.content.Context;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public abstract class Sensor {

    public interface SensorObserver {
        void sensorStateChanged(Sensor sensor, State state);

        void sensorValueChanged(Sensor sensor);
    }

    public enum State {
        Disconnected,
        Connecting,
        Connected,
        Disconnecting
    }

    private final Context mContext;

    public Sensor(Context context) {
        mContext = context;
    }

    public Context getContext() {
        return mContext;
    }

    protected Set<SensorObserver> mObservers = Collections.newSetFromMap(new WeakHashMap<SensorObserver, Boolean>());

    protected Set<SensorObserver> getObservers() {
        return mObservers;
    }

    public void addObserver(SensorObserver observer) {
        mObservers.add(observer);
    }

    public void removeObserver(SensorObserver observer) {
        mObservers.remove(observer);
    }

    public abstract String getSensorId();

    public abstract String getName();

    public abstract State getState();

    public abstract void connect();

    public abstract void disconnect();

    public boolean providesPower() {
        return false;
    }

    public boolean providesCadence() {
        return false;
    }

    public boolean providesSpeed() {
        return false;
    }

    public boolean providesHeartRate() {
        return false;
    }

    public int currentPower() {
        return -1;
    }

    public double currentCadence() {
        return -1;
    }

    public double currentSpeed() {
        return -1;
    }

    public int currentHeartRate() {
        return -1;
    }

    public boolean requiresCalibration() {
        return false;
    }

    public void startCalibration() {
    }

    public void stopCalibration() {
    }

    public void startFirmwareUpdate() {

    }

    public boolean firmwareUpdateAvailable() {
        return false;
    }

    protected void notfifyObserversValueChanged() {
        for (SensorObserver observer : mObservers) {
            observer.sensorValueChanged(this);
        }
    }

}
