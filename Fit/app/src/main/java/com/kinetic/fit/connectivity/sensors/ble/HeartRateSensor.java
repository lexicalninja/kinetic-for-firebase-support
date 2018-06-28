package com.kinetic.fit.connectivity.sensors.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;

import java.util.List;
import java.util.UUID;

public class HeartRateSensor extends BluetoothLESensor {

    private int mHeartRate;

    private static final String TAG = "HeartRateSensor";

    public static final String HEART_RATE_SERVICE_UUID = "180d-0000-1000-8000-00805f9b34fb";


    private static final String HEART_RATE_SERVICE_HRCHAR_UUID = "2a37-0000-1000-8000-00805f9b34fb";


    public HeartRateSensor(Context context, BluetoothDevice device) {
        super(context, device);
    }

    @Override
    protected void processServices(BluetoothGatt gatt) {
        for (BluetoothGattService service : gatt.getServices()) {
            if (service.getUuid().equals(UUID.fromString(HEART_RATE_SERVICE_UUID))) {

                List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                for (BluetoothGattCharacteristic characteristic : characteristics) {
                    if (characteristic.getUuid().equals(UUID.fromString(HEART_RATE_SERVICE_HRCHAR_UUID))) {

                        setNotifyForCharacteristic(characteristic, true);
                    }
                }
            }
        }
    }

    @Override
    protected void processCharacteristicValue(BluetoothGattCharacteristic characteristic) {
        byte[] reportData = characteristic.getValue();
        int bpm;
        if ((reportData[0] & 0x01) == 0) {
            // uint8 bpm
            bpm = reportData[1] & 0xff;
        } else {
            // uint16 bpm
            bpm = (reportData[1] & 0xff) << 8;
            bpm |= (reportData[2] & 0xff);
        }
        mHeartRate = bpm > 0 ? bpm : -1;
        notfifyObserversValueChanged();
    }

    @Override
    public boolean providesHeartRate() {
        return true;
    }

    @Override
    public int currentHeartRate() {
        return mHeartRate;
    }
}
