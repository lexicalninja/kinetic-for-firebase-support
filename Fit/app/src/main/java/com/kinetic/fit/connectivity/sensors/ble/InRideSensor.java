package com.kinetic.fit.connectivity.sensors.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;

import com.kinetic.fit.connectivity.sensors.CoastCalibrationSensor;
import com.kinetic.sdk.exceptions.APIKeyException;
import com.kinetic.sdk.exceptions.InvalidDataException;
import com.kinetic.sdk.inride.ConfigData;
import com.kinetic.sdk.inride.InRide;
import com.kinetic.sdk.inride.PowerData;

import java.util.List;
import java.util.UUID;

public class InRideSensor extends BluetoothLESensor implements CoastCalibrationSensor {

    private static final String TAG = "InRideSensor";

    private byte[] mDeviceId;
    private BluetoothGattCharacteristic mControlCharacteristic;
    private PowerData mPowerData;
    private ConfigData mConfigData;

    public InRideSensor(Context context, BluetoothDevice device) {
        super(context, device);

    }

    public void setUpdateRate(ConfigData.SensorUpdateRate updateRate) {
        try {
            byte[] command = InRide.ConfigureSensorCommandData(updateRate, mDeviceId);
            mControlCharacteristic.setValue(command);
            writeCharacteristic(mControlCharacteristic);
        } catch (APIKeyException e) {

        } catch (InvalidDataException e) {

        }
    }

    @Override
    public double currentCadence() {
        if (mPowerData != null) {
            return mPowerData.cadenceRPM;
        }
        return super.currentCadence();
    }

    @Override
    public double currentSpeed() {
        if (mPowerData != null) {
            return mPowerData.speedKPH;
        }
        return super.currentSpeed();
    }

    @Override
    public int currentPower() {
        if (mPowerData != null) {
            return mPowerData.power;
        }
        return super.currentPower();
    }

    public PowerData currentPowerData() {
        return mPowerData;
    }

    public void setPeripheralName(String name) {
        try {
            byte[] command = InRide.SetPeripheralNameCommandData(name, mDeviceId);
            mControlCharacteristic.setValue(command);
            writeCharacteristic(mControlCharacteristic);

        } catch (APIKeyException e) {

        } catch (InvalidDataException e) {

        }
    }

    @Override
    public void startCalibration() {
        if (mDeviceId == null) {
            return;
        }
        try {
            byte[] command = InRide.StartCalibrationCommandData(mDeviceId);
            mControlCharacteristic.setValue(command);
            writeCharacteristic(mControlCharacteristic);
            mCalibrating = true;

        } catch (APIKeyException e) {

        } catch (InvalidDataException e) {

        }
    }

    @Override
    public void stopCalibration() {
        if (mDeviceId == null) {
            return;
        }
        try {
            byte[] command = InRide.StopCalibrationCommandData(mDeviceId);
            mControlCharacteristic.setValue(command);
            writeCharacteristic(mControlCharacteristic);
            mCalibrating = false;

        } catch (APIKeyException e) {

        } catch (InvalidDataException e) {

        }

    }

    @Override
    public double getCurrentSpindownTime() {
        if (mPowerData != null) {
            return mPowerData.spindownTime;
        }
        return -1;
    }

    @Override
    public double getLastSpindownTime() {
        if (mPowerData != null) {
            return mPowerData.lastSpindownResultTime;
        }
        return -1;
    }

    @Override
    public double getCalibrationReadySpeedKPH() {
        return 33.8;
    }

    @Override
    public double getCurrentSpeedKPH() {
        if (mPowerData != null) {
            return mPowerData.speedKPH;
        }
        return -1;
    }

    private boolean mCalibrating = false;

    @Override
    public FITCalibrateCoastState getCurrentState() {
        if (mPowerData != null) {
            switch (mPowerData.state) {
                case Normal:
                    if (mCalibrating) {
//                        mCalibrating = false;
                        return FITCalibrateCoastState.Complete;
                    }
                    return FITCalibrateCoastState.Initializing;
                case SpindownIdle:
                    return FITCalibrateCoastState.SpeedUp;
                case SpindownReady:
                    return FITCalibrateCoastState.StartCoasting;
                case SpindownActive:
                    return FITCalibrateCoastState.Coasting;
            }
        }
        return FITCalibrateCoastState.Unknown;
    }

    @Override
    public FITCalibrateCoastResult getLatestResult() {
        if (mPowerData != null) {
            switch (mPowerData.calibrationResult) {
                case Success:
                    return FITCalibrateCoastResult.Success;
                case TooFast:
                    return FITCalibrateCoastResult.TooFast;
                case TooSlow:
                    return FITCalibrateCoastResult.TooSlow;
                case Middle:
                    return FITCalibrateCoastResult.Middle;
                case Unknown:
                    return FITCalibrateCoastResult.Unknown;
            }
        }
        return FITCalibrateCoastResult.Unknown;
    }

    @Override
    protected void processServices(BluetoothGatt gatt) {
        for (BluetoothGattService service : gatt.getServices()) {
            List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
            for (BluetoothGattCharacteristic characteristic : characteristics) {
                if (service.getUuid().equals(UUID.fromString(InRide.PowerService.UUID))) {
                    if (characteristic.getUuid().equals(UUID.fromString(InRide.PowerService.Characteristics.POWER_UUID))) {
                        setNotifyForCharacteristic(characteristic, true);
                    } else if (characteristic.getUuid().equals(UUID.fromString(InRide.PowerService.Characteristics.CONTROL_POINT_UUID))) {
                        mControlCharacteristic = characteristic;
                    }
                } else if (service.getUuid().equals(UUID.fromString(InRide.DeviceInformation.UUID))) {
                    if (characteristic.getUuid().equals(UUID.fromString(InRide.DeviceInformation.Characteristics.SYSTEM_ID_UUID))) {
                        readCharacteristic(characteristic);
                    }
                }

            }
        }
    }

    @Override
    protected void processCharacteristicValue(BluetoothGattCharacteristic characteristic) {
        if (characteristic.getUuid().equals(UUID.fromString(InRide.PowerService.Characteristics.POWER_UUID)) && mDeviceId != null) {
            byte[] bytes = characteristic.getValue();
            try {
                mPowerData = InRide.ProcessPowerData(bytes, mDeviceId);
                notfifyObserversValueChanged();
            } catch (APIKeyException e) {

            } catch (InvalidDataException e) {

            }
        } else if (characteristic.getUuid().equals(UUID.fromString(InRide.DeviceInformation.Characteristics.SYSTEM_ID_UUID))) {
            mDeviceId = characteristic.getValue();
        } else if (characteristic.getUuid().equals(UUID.fromString(InRide.PowerService.Characteristics.CONFIG_UUID))) {
            byte[] bytes = characteristic.getValue();
            try {
                mConfigData = InRide.ProcessConfigurationData(bytes);

            } catch (APIKeyException e) {

            } catch (InvalidDataException e) {

            }

        }

    }

    @Override
    public boolean providesCadence() {
        return true;
    }

    @Override
    public boolean providesPower() {
        return true;
    }

    @Override
    public boolean providesSpeed() {
        return true;
    }

    @Override
    public boolean requiresCalibration() {
        return true;
    }

    public String getInRideId() {
        if (mDeviceId != null) {
            return "IR2." + SystemIdToString(mDeviceId);
        }
        return null;
    }

    static String SystemIdToString(byte[] systemId) {
        StringBuilder sb = new StringBuilder();
        for (byte b : systemId) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
