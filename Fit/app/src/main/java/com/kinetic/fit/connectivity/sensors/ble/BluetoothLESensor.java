package com.kinetic.fit.connectivity.sensors.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import com.kinetic.fit.connectivity.sensors.Sensor;

import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;


public abstract class BluetoothLESensor extends Sensor {

    private static final String TAG = "BluetoothLESensor";

    private interface BluetoothOperation {
        void execute(BluetoothGatt gatt);
    }

    private class BluetoothOperationWriteDescriptor implements BluetoothOperation {
        BluetoothGattDescriptor mDescriptor;

        protected BluetoothOperationWriteDescriptor(BluetoothGattDescriptor descriptor) {
            mDescriptor = descriptor;
        }

        @Override
        public void execute(BluetoothGatt gatt) {
            gatt.writeDescriptor(mDescriptor);
        }
    }

    private class BluetoothOperationReadDescriptor implements BluetoothOperation {
        BluetoothGattDescriptor mDescriptor;

        protected BluetoothOperationReadDescriptor(BluetoothGattDescriptor descriptor) {
            mDescriptor = descriptor;
        }

        @Override
        public void execute(BluetoothGatt gatt) {
            gatt.readDescriptor(mDescriptor);
        }
    }

    private class BluetoothOperationReadCharacteristic implements BluetoothOperation {
        BluetoothGattCharacteristic mCharacteristic;

        protected BluetoothOperationReadCharacteristic(BluetoothGattCharacteristic characteristic) {
            mCharacteristic = characteristic;
        }

        @Override
        public void execute(BluetoothGatt gatt) {
            gatt.readCharacteristic(mCharacteristic);
        }
    }

    private class BluetoothOperationWriteCharacteristic implements BluetoothOperation {
        BluetoothGattCharacteristic mCharacteristic;

        protected BluetoothOperationWriteCharacteristic(BluetoothGattCharacteristic characteristic) {
            mCharacteristic = characteristic;
        }

        @Override
        public void execute(BluetoothGatt gatt) {
            gatt.writeCharacteristic(mCharacteristic);
        }
    }


    private BluetoothDevice mDevice;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;

    private Queue<BluetoothOperation> mOperationQueue = new LinkedList<BluetoothOperation>();

    public BluetoothLESensor(Context context, BluetoothDevice device) {
        super(context);
        mDevice = device;
        mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
    }

    private void processQueue() {
        if (mOperationQueue.size() > 0) {
            BluetoothOperation qItem = mOperationQueue.peek();
            qItem.execute(mBluetoothGatt);
        }
    }

    protected void setNotifyForCharacteristic(BluetoothGattCharacteristic characteristic, boolean notify) {
        mBluetoothGatt.setCharacteristicNotification(characteristic, true);
        for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
            if (descriptor.getUuid().equals(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))) {
                if (notify) {
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                } else {
                    descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                }
                writeDescriptor(descriptor);
            }
        }
    }

    private void writeDescriptor(BluetoothGattDescriptor descriptor) {
        BluetoothOperationWriteDescriptor operation = new BluetoothOperationWriteDescriptor(descriptor);
        mOperationQueue.add(operation);

        if (mOperationQueue.size() == 1) {
            processQueue();
        }
    }

    private void readDescriptor(BluetoothGattDescriptor descriptor) {
        BluetoothOperationReadDescriptor operation = new BluetoothOperationReadDescriptor(descriptor);
        mOperationQueue.add(operation);

        if (mOperationQueue.size() == 1) {
            processQueue();
        }
    }

    protected void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        BluetoothOperationReadCharacteristic operation = new BluetoothOperationReadCharacteristic(characteristic);
        mOperationQueue.add(operation);

        if (mOperationQueue.size() == 1) {
            processQueue();
        }
    }

    protected void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        BluetoothOperationWriteCharacteristic operation = new BluetoothOperationWriteCharacteristic(characteristic);
        mOperationQueue.add(operation);

        if (mOperationQueue.size() == 1) {
            processQueue();
        }
    }

    @Override
    public String getSensorId() {
        return mDevice.getAddress();
    }

    @Override
    public String getName() {
        return mDevice.getName();
    }


    @Override
    public State getState() {
        if (mBluetoothGatt == null || mDevice == null) {
            return State.Disconnected;
        }

        int state = mBluetoothManager.getConnectionState(mDevice, BluetoothProfile.GATT);
        switch (state) {
            case BluetoothProfile.STATE_DISCONNECTED:
                return State.Disconnected;
            case BluetoothProfile.STATE_CONNECTING:
                return State.Connecting;
            case BluetoothProfile.STATE_CONNECTED:
                return State.Connected;
            case BluetoothProfile.STATE_DISCONNECTING:
                return State.Disconnecting;
        }
        return State.Disconnected;
    }

    @Override
    public void connect() {
        Log.d(TAG, "Connecting to sensor...");
        if (mBluetoothGatt == null) {
            mBluetoothGatt = mDevice.connectGatt(getContext(), false, mGattCallbacks);
            for (SensorObserver observer : mObservers) {
                observer.sensorStateChanged(this, getState());
            }
            mOperationQueue.clear();
        } else {
            int state = mBluetoothManager.getConnectionState(mDevice, BluetoothProfile.GATT);
            if (state != BluetoothProfile.STATE_CONNECTED && state != BluetoothProfile.STATE_CONNECTING) {
                mBluetoothGatt.connect();
                for (SensorObserver observer : mObservers) {
                    observer.sensorStateChanged(this, getState());
                }
                mOperationQueue.clear();
            }
        }
    }

    @Override
    public void disconnect() {
        if (mBluetoothGatt != null) {
            Log.d(TAG, "Disconnecting from sensor...");
            mBluetoothGatt.disconnect();
            for (SensorObserver observer : mObservers) {
                observer.sensorStateChanged(this, getState());
            }
            mOperationQueue.clear();
        }
    }

    protected abstract void processServices(BluetoothGatt services);
    protected abstract void processCharacteristicValue(BluetoothGattCharacteristic characteristic);
    protected void characteristicValueWritten(BluetoothGattCharacteristic characteristic) {

    }

    private BluetoothGattCallback mGattCallbacks = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(TAG, "onConnectionStateChange (" + mDevice.getName() + ") " + newState + " status: " + status);

            try {
                switch (newState) {
                    case BluetoothProfile.STATE_CONNECTED:
                        mBluetoothGatt.discoverServices();
                        break;
                    case BluetoothProfile.STATE_DISCONNECTED:
                        mBluetoothGatt.close();
                        mBluetoothGatt = null;
                        break;
                    case BluetoothProfile.STATE_CONNECTING:
                        break;
                    case BluetoothProfile.STATE_DISCONNECTING:
                        break;
                    default:
                        break;
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }

            for (SensorObserver observer : mObservers) {
                observer.sensorStateChanged(BluetoothLESensor.this, getState());
            }
        }


        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            processServices(gatt);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            processCharacteristicValue(characteristic);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            processCharacteristicValue(characteristic);

            mOperationQueue.remove();
            processQueue();
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            characteristicValueWritten(characteristic);

            mOperationQueue.remove();
            processQueue();
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            mOperationQueue.remove();
            processQueue();
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            mOperationQueue.remove();
            processQueue();
        }
    };

}
