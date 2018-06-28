package com.kinetic.fit.connectivity.sensors.ble;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

import com.kinetic.fit.connectivity.sensors.Sensor;

import java.util.UUID;

public class HeartRateSensorFactory extends BluetoothLESensorFactory {

    @Override
    public Sensor createSensor(Context context, BluetoothDevice device) {
        return new HeartRateSensor(context, device);
    }
    @Override
    public UUID getPrimaryServiceUUID() {
        return UUID.fromString(HeartRateSensor.HEART_RATE_SERVICE_UUID);
    }
}
