package com.kinetic.fit.connectivity.sensors.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;

import java.util.List;
import java.util.UUID;

/**
 * Created by Saxton on 7/27/16.
 */
public class GenericPowerSensor extends BluetoothLESensor{

    public final static String POWER_SERVICE_UUID = "1818-0000-1000-8000-00805f9b34fb";
    private static final String POWER_MEASUREMENT_UUID ="2A63-0000-1000-8000-00805f9b34fb";
    private static final String POWER_FEATURES_UUID ="2A65-0000-1000-8000-00805f9b34fb";

    double wheelCircumferenceCM = 213.3;
    int instantaneousPower = -1;
    public double mSpeedKPH = -1;
    public double mCadenceRPM = -1;
    int wheelTimeResolution = 2048;

    CyclingPowerSerializer.MeasurementData current;
    CyclingPowerSerializer.MeasurementData previous;

    CyclingPowerSerializer.Features features;

    public GenericPowerSensor(Context context, BluetoothDevice device){
        super(context, device);
    }

    @Override
    protected void processCharacteristicValue(BluetoothGattCharacteristic characteristic) {
        if (characteristic.getUuid().equals(UUID.fromString(POWER_MEASUREMENT_UUID))) {
            byte[] reportData = characteristic.getValue();

            previous = current;
            current = CyclingPowerSerializer.readMeasurement(reportData);

            if (current != null && previous != null) {
                instantaneousPower = current.instantaneousPower;

                if (CyclingSerializer.calculateWheelKPH(current, previous, wheelCircumferenceCM,
                        wheelTimeResolution) != null) {
                    mSpeedKPH = CyclingSerializer.calculateWheelKPH(current, previous,
                            wheelCircumferenceCM, wheelTimeResolution);
                }
                if (CyclingSerializer.calculateCrankRPM(current, previous) != null) {
                    mCadenceRPM = CyclingSerializer.calculateCrankRPM(current, previous);
                }
            }
        } else if (characteristic.getUuid().equals(UUID.fromString(POWER_FEATURES_UUID))) {
            byte[] featureBytes = characteristic.getValue();
            features = CyclingPowerSerializer.readFeatures(featureBytes);
        }
        notfifyObserversValueChanged();
    }

    @Override
    protected void processServices(BluetoothGatt gatt) {
        for (BluetoothGattService service : gatt.getServices()) {
            if (service.getUuid().equals(UUID.fromString(POWER_SERVICE_UUID))) {
                List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                for (BluetoothGattCharacteristic characteristic : characteristics) {
                    if (characteristic.getUuid().equals(UUID.fromString(POWER_MEASUREMENT_UUID))) {
                        setNotifyForCharacteristic(characteristic, true);
                    }else if(characteristic.getUuid().equals((UUID.fromString(POWER_FEATURES_UUID)))){
                        setNotifyForCharacteristic(characteristic, true);
                        readCharacteristic(characteristic);

                    }
                }
            }
        }
    }

    @Override
    public boolean providesPower() {
//        return features.isCrankRevolutionDataSupported();
        return true;
    }

    @Override
    public int currentPower() {
        return instantaneousPower;
    }
}
