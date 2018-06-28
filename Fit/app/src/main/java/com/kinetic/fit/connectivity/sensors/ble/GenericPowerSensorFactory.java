package com.kinetic.fit.connectivity.sensors.ble;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

import com.kinetic.fit.connectivity.sensors.Sensor;

import java.util.UUID;

/**
 * Created by Saxton on 7/27/16.
 */
public class GenericPowerSensorFactory extends BluetoothLESensorFactory {

    @Override
    public Sensor createSensor(Context context, BluetoothDevice device){
        return new GenericPowerSensor(context, device);
    }

    @Override
    public UUID getPrimaryServiceUUID(){
        return UUID.fromString(GenericPowerSensor.POWER_SERVICE_UUID);
    }
}
