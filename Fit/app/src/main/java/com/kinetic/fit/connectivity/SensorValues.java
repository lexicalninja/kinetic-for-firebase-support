package com.kinetic.fit.connectivity;


import android.os.Bundle;

public class SensorValues {
    public int currentPower = -1;
    public int currentHeartRate = -1;
    public double currentSpeedKPH = -1;
    public double currentCadence = -1;

    public static SensorValues getFromBundle(Bundle bundle) {
        SensorValues values = new SensorValues();
        values.currentHeartRate = bundle.getInt(SensorDataService.SENSOR_DATA_HEART_RATE, -1);
        values.currentPower = bundle.getInt(SensorDataService.SENSOR_DATA_POWER, -1);
        values.currentSpeedKPH = bundle.getDouble(SensorDataService.SENSOR_DATA_SPEED, -1);
        values.currentCadence = bundle.getDouble(SensorDataService.SENSOR_DATA_CADENCE, -1);
        return values;
    }
}