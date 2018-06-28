package com.kinetic.fit.connectivity.sensors.ble;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

import com.kinetic.fit.connectivity.sensors.Sensor;

import java.util.UUID;

/**
 * Created by Stewart on 9/26/15.
 */
public abstract class BluetoothLESensorFactory {

    public abstract Sensor createSensor(Context context, BluetoothDevice device);
    public abstract UUID getPrimaryServiceUUID();

}
