package com.kinetic.fit.connectivity.sensors.ble;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

import com.kinetic.fit.connectivity.sensors.Sensor;
import com.kinetic.sdk.inride.InRide;

import java.util.UUID;

public class InRideSensorFactory extends BluetoothLESensorFactory {

    @Override
    public Sensor createSensor(Context context, BluetoothDevice device) {
        return new InRideSensor(context, device);
    }

    @Override
    public UUID getPrimaryServiceUUID() {
        return UUID.fromString(InRide.PowerService.UUID);
    }
}
