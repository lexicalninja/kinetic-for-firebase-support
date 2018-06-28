package com.kinetic.fit.connectivity.sensors.ble;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

import com.kinetic.fit.connectivity.sensors.Sensor;

import java.util.UUID;

/**
 * Created by Saxton on 7/14/16.
 */
public class GenericSpeedAndCadenceSensorFactory extends BluetoothLESensorFactory {

    @Override
    public Sensor createSensor(Context context, BluetoothDevice device){
        return new GenericSpeedAndCadenceSensor(context, device);
    }

    @Override
    public UUID getPrimaryServiceUUID(){
        return UUID.fromString(GenericSpeedAndCadenceSensor.SPEED_CADENCE_SERVICE_UUID);
    }
}
