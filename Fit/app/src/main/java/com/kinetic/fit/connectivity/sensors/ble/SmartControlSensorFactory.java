package com.kinetic.fit.connectivity.sensors.ble;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

import com.kinetic.fit.connectivity.sensors.Sensor;
import com.kinetic.sdk.smartcontrol.SmartControl;

import java.util.UUID;

public class SmartControlSensorFactory extends BluetoothLESensorFactory {

    @Override
    public Sensor createSensor(Context context, BluetoothDevice device) {
        return new SmartControlSensor(context, device);
    }

    @Override
    public UUID getPrimaryServiceUUID() {
        return UUID.fromString(SmartControl.PowerService.UUID);
    }
}
