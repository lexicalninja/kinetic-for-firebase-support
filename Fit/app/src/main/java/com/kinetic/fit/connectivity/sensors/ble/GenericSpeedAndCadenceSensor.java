package com.kinetic.fit.connectivity.sensors.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;

import java.util.List;
import java.util.UUID;

/**
 * Created by Saxton on 7/14/16.
 */
public class GenericSpeedAndCadenceSensor extends BluetoothLESensor {

    public GenericSpeedAndCadenceSensor(Context context, BluetoothDevice device) {
        super(context, device);
    }

    public static final String SPEED_CADENCE_SERVICE_UUID = "1816-0000-1000-8000-00805f9b34fb";
    private static final String SPEED_CADENCE_CSC_MEASUREMENT_UUID = "2a5b-0000-1000-8000-00805f9b34fb";
    private static final String SPEED_CADENCE_CSC_FEATURES_UUID = "2a5c-0000-1000-8000-00805f9b34fb";


    private CyclingSpeedCadenceSerializer.MeasurementData previous;
    private CyclingSpeedCadenceSerializer.MeasurementData current;

    public double mSpeedKPH = -1;
    public double mCadenceRPM = -1;
    private double wheelCircumferenceCM = 213.3;
    private static final int wheelTimeResolution = 1024;

    public CyclingSpeedCadenceSerializer.Features features;

    @Override
    protected void processServices(BluetoothGatt gatt) {
        for (BluetoothGattService service : gatt.getServices()) {
            if (service.getUuid().equals(UUID.fromString(SPEED_CADENCE_SERVICE_UUID))) {

                List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                for (BluetoothGattCharacteristic characteristic : characteristics) {
                    if (characteristic.getUuid().equals(UUID.fromString(SPEED_CADENCE_CSC_MEASUREMENT_UUID))) {
                        setNotifyForCharacteristic(characteristic, true);
                    } else if (characteristic.getUuid().equals(UUID.fromString(SPEED_CADENCE_CSC_FEATURES_UUID))) {
                        readCharacteristic(characteristic);
                        setNotifyForCharacteristic(characteristic, true);
                    }

                }
            }
        }
    }

    @Override
    protected void processCharacteristicValue(BluetoothGattCharacteristic characteristic) {
        if (characteristic.getUuid().equals(UUID.fromString(SPEED_CADENCE_CSC_MEASUREMENT_UUID))) {
            byte[] reportData = characteristic.getValue();

            previous = current;
            current = CyclingSpeedCadenceSerializer.readMeasurement(reportData);

            if (current != null && previous != null) {
                if (CyclingSerializer.calculateWheelKPH(current, previous, wheelCircumferenceCM,
                        wheelTimeResolution) != null) {
                    mSpeedKPH = CyclingSerializer.calculateWheelKPH(current, previous,
                            wheelCircumferenceCM, wheelTimeResolution);
                }
                if (CyclingSerializer.calculateCrankRPM(current, previous) != null) {
                    mCadenceRPM = CyclingSerializer.calculateCrankRPM(current, previous);
                }
            }
        } else if (characteristic.getUuid().equals(UUID.fromString(SPEED_CADENCE_CSC_FEATURES_UUID))) {
            byte[] featureBytes = characteristic.getValue();
            features = CyclingSpeedCadenceSerializer.readFeatures(featureBytes);
        }
        notfifyObserversValueChanged();
    }

    @Override
    public boolean providesCadence() {
//        return features.isCrankRevolutionDataSupported();
        return true;
    }

    @Override
    public boolean providesSpeed() {
//        return features.isWheelRevolutionDataSupported();
        return true;
    }

    @Override
    public double currentCadence() {
        return mCadenceRPM;
    }

    @Override
    public double currentSpeed() {
        return mSpeedKPH;
    }
}
