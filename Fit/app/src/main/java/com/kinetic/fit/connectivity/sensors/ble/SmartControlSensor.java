package com.kinetic.fit.connectivity.sensors.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.goebl.david.Response;
import com.goebl.david.Webb;
import com.goebl.david.WebbException;
import com.kinetic.fit.connectivity.SensorDataService;
import com.kinetic.fit.connectivity.sensors.CoastCalibrationSensor;
import com.kinetic.fit.connectivity.sensors.DynamicResistanceSensor;
import com.kinetic.sdk.smartcontrol.ConfigData;
import com.kinetic.sdk.smartcontrol.FirmwarePosition;
import com.kinetic.sdk.smartcontrol.PowerData;
import com.kinetic.sdk.smartcontrol.SmartControl;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.UUID;

public class SmartControlSensor extends BluetoothLESensor implements DynamicResistanceSensor, CoastCalibrationSensor {

    private static final String TAG = "SmartControlSensor";

    private byte[] mDeviceId;
    private String mFirmwareVersion;
    private BluetoothGattCharacteristic mControlCharacteristic;
    private PowerData mPowerData;
    private ConfigData mConfigData;

    public SmartControlSensor(Context context, BluetoothDevice device) {
        super(context, device);
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

    @Override
    public void startCalibration() {
        try {
            byte[] command = SmartControl.StartCalibrationCommandData();
            mControlCharacteristic.setValue(command);
            writeCharacteristic(mControlCharacteristic);
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage());
        }
    }

    @Override
    public void stopCalibration() {
        try {
            byte[] command = SmartControl.StopCalibrationCommandData();
            mControlCharacteristic.setValue(command);
            writeCharacteristic(mControlCharacteristic);
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage());
        }
    }

    @Override
    public double getCurrentSpindownTime() {
        if (mConfigData != null) {
            return mConfigData.spindownTime;
        }
        return -1;
    }

    @Override
    public double getLastSpindownTime() {
        if (mConfigData != null) {
            return mConfigData.spindownTime;
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

    @Override
    public FITCalibrateCoastState getCurrentState() {
        if (mConfigData != null) {
            switch (mConfigData.calibrationState) {
                case Initializing:
                    return FITCalibrateCoastState.Initializing;
                case Coasting:
                    return FITCalibrateCoastState.Coasting;
                case Complete:
                    return FITCalibrateCoastState.Complete;
                case NotPerformed:
                    return FITCalibrateCoastState.Unknown;
                case SpeedUp:
                    return FITCalibrateCoastState.SpeedUp;
                case SpeedUpDetected:
                    return FITCalibrateCoastState.SpeedUp;
                case StartCoasting:
                    return FITCalibrateCoastState.StartCoasting;
            }
        }
        return FITCalibrateCoastState.Unknown;
    }

    @Override
    public FITCalibrateCoastResult getLatestResult() {
        if (mConfigData != null) {
            if (mConfigData.spindownTime < 4.0) {
                return FITCalibrateCoastResult.TooFast;
            } else if (mConfigData.spindownTime > 15.0) {
                return FITCalibrateCoastResult.TooSlow;
            } else {
                return FITCalibrateCoastResult.Success;
            }
        }
        return FITCalibrateCoastResult.Unknown;
    }

    @Override
    protected void processServices(BluetoothGatt gatt) {
        for (BluetoothGattService service : gatt.getServices()) {
            List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
            for (BluetoothGattCharacteristic characteristic : characteristics) {
                if (service.getUuid().equals(UUID.fromString(SmartControl.PowerService.UUID))) {
                    if (characteristic.getUuid().equals(UUID.fromString(SmartControl.PowerService.Characteristics.POWER_UUID))) {
                        setNotifyForCharacteristic(characteristic, true);
                    } else if (characteristic.getUuid().equals(UUID.fromString(SmartControl.PowerService.Characteristics.CONTROL_POINT_UUID))) {
                        setNotifyForCharacteristic(characteristic, true);
                        mControlCharacteristic = characteristic;
                    } else if (characteristic.getUuid().equals(UUID.fromString(SmartControl.PowerService.Characteristics.CONFIG_UUID))) {
                        setNotifyForCharacteristic(characteristic, true);
                    }
                } else if (service.getUuid().equals(UUID.fromString(SmartControl.DeviceInformation.UUID))) {
                    if (characteristic.getUuid().equals(UUID.fromString(SmartControl.DeviceInformation.Characteristics.SYSTEM_ID_UUID))) {
                        readCharacteristic(characteristic);
                    } else if (characteristic.getUuid().equals(UUID.fromString(SmartControl.DeviceInformation.Characteristics.FIRMWARE_REVISION_STRING_UUID))) {
                        readCharacteristic(characteristic);
                    }
                }
            }
        }
    }

    @Override
    protected void characteristicValueWritten(BluetoothGattCharacteristic characteristic) {
        if (characteristic.getUuid().equals(UUID.fromString(SmartControl.PowerService.Characteristics.CONTROL_POINT_UUID))) {
            if (mFirmwarUpdatePosition > 0) {
                writeFirmwareChunk(mFirmwareVersion);
            }
        }
    }

    @Override
    protected void processCharacteristicValue(BluetoothGattCharacteristic characteristic) {
        if (characteristic.getUuid().equals(UUID.fromString(SmartControl.PowerService.Characteristics.POWER_UUID)) && mDeviceId != null) {
            byte[] bytes = characteristic.getValue();
            try {
                mPowerData = SmartControl.ProcessPowerData(bytes, mDeviceId);
                notfifyObserversValueChanged();
            } catch (Exception e) {
                Log.e(TAG, e.getLocalizedMessage());
            }
        } else if (characteristic.getUuid().equals(UUID.fromString(SmartControl.PowerService.Characteristics.CONFIG_UUID))) {
            byte[] bytes = characteristic.getValue();
            try {
                mConfigData = SmartControl.ProcessConfigurationData(bytes);
                notfifyObserversValueChanged();
            } catch (Exception e) {
                Log.e(TAG, e.getLocalizedMessage());
            }
        } else if (characteristic.getUuid().equals(UUID.fromString(SmartControl.DeviceInformation.Characteristics.SYSTEM_ID_UUID))) {
            mDeviceId = characteristic.getValue();
        } else if (characteristic.getUuid().equals(UUID.fromString(SmartControl.DeviceInformation.Characteristics.FIRMWARE_REVISION_STRING_UUID))) {
            mFirmwareVersion = characteristic.getStringValue(0);
            checkForFirmwareUpdate();
        }
    }

    public String getmFirmwareVersion() {
        return mFirmwareVersion;
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

    public String getSmartControlId() {
        if (mDeviceId != null) {
            return "SC." + SystemIdToString(mDeviceId);
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

    @Override
    public void setResistanceBrake(float percent) {
        try {
            byte[] command = SmartControl.SetResistanceMode(percent);
            mControlCharacteristic.setValue(command);
            writeCharacteristic(mControlCharacteristic);
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage());
        }
    }

    @Override
    public void setResistanceErg(int targetWatts) {
        try {
            byte[] command = SmartControl.SetERGMode(targetWatts);
            mControlCharacteristic.setValue(command);
            writeCharacteristic(mControlCharacteristic);
        } catch (Exception e) {
            Log.e(TAG, "" + e.getLocalizedMessage());
        }
    }

    @Override
    public void setResistanceFluid(int level) {
        try {
            byte[] command = SmartControl.SetFluidMode(level);
            mControlCharacteristic.setValue(command);
            writeCharacteristic(mControlCharacteristic);
        } catch (Exception e) {
            if (e.getLocalizedMessage() != null) {
                Log.e(TAG, e.getLocalizedMessage());
            } else {
                Log.e(TAG, "No Message Included with error");
            }
        }

    }

    private boolean mUpdateAvailable = false;
    private String mFirmwareReleaseNotes;
    private byte[] mFirmwareData;
    private int mFirmwarUpdatePosition = 0;

    @Override
    public void startFirmwareUpdate() {
        Log.d(TAG, "Start Firmware Update for Smart Control");
        if (mControlCharacteristic == null || mFirmwareData == null || mFirmwareVersion == null) {
            return;
        }
        if (mFirmwareData.length >= 0x10000 || mFirmwarUpdatePosition != 0) {
            return;
        }
        mFirmwarUpdatePosition = 0;

        Intent intent = new Intent(SensorDataService.SENSOR_FIRMWARE_UPDATE_STARTED);
        intent.putExtra(SensorDataService.SENSOR_ID, getSensorId());
        getContext().sendBroadcast(intent);

        writeFirmwareChunk(mFirmwareVersion);
    }

    @Override
    public boolean firmwareUpdateAvailable() {
        return mUpdateAvailable;
    }

    private void writeFirmwareChunk(String currentVersion) {
        if (mControlCharacteristic == null || mFirmwareData == null || currentVersion == null) {
            return;
        }
//
//        if () {
//            if (mFirmwarUpdatePosition > 0) {
//            }
//            return;
//        }

        byte[] systemId = null;
        if (Integer.parseInt(currentVersion) < 1024) {
            systemId = mDeviceId;
        }
        int oldPosition = mFirmwarUpdatePosition;
        FirmwarePosition pos = new FirmwarePosition(mFirmwarUpdatePosition);
        pos.setPosition(mFirmwarUpdatePosition);
        byte[] chunk = SmartControl.FirmwareUpdateChunk(mFirmwareData, pos, systemId);
        mFirmwarUpdatePosition = pos.getPosition();
        mControlCharacteristic.setValue(chunk);
        writeCharacteristic(mControlCharacteristic);

        if (mFirmwarUpdatePosition < mFirmwareData.length) {
            double oldProgress = oldPosition / (double) mFirmwareData.length;
            double progress = mFirmwarUpdatePosition / (double) mFirmwareData.length;

            int oldProgressI = (int) Math.floor(oldProgress * 100);
            int progressI = (int) Math.floor(progress * 100);
            if (progressI > oldProgressI) {
                Intent intent = new Intent(SensorDataService.SENSOR_FIRMWARE_UPDATE_PROGRESS);
                intent.putExtra(SensorDataService.SENSOR_ID, getSensorId());
                intent.putExtra(SensorDataService.SENSOR_FIRMWARE_UPDATE_PROGRESS_PERCENT, progressI);
                getContext().sendBroadcast(intent);
            }
        } else {
            mFirmwareData = null;
            mFirmwarUpdatePosition = 0;

            Intent intent = new Intent(SensorDataService.SENSOR_FIRMWARE_UPDATE_COMPLETE);
            intent.putExtra(SensorDataService.SENSOR_ID, getSensorId());
            getContext().sendBroadcast(intent);
        }
    }


    private void checkForFirmwareUpdate() {
        mUpdateAvailable = false;

        String url = "https://developer.kinetic.fit/firmware/sc-latest.json";
        try {
            Response<JSONObject> response = Webb.create().get(url).useCaches(false).ensureSuccess().asJsonObject();
            String latestVersion = response.getBody().getString("version");
            String firmwareUrl = response.getBody().getString("url");
            String notes = response.getBody().getString("notes");

            if (mFirmwareVersion != null && !mFirmwareVersion.equalsIgnoreCase(latestVersion)) {
                mFirmwareReleaseNotes = notes;

                Response<byte[]> fwResponse = Webb.create().get(firmwareUrl).useCaches(false).ensureSuccess().asBytes();
                mUpdateAvailable = true;
                mFirmwareData = fwResponse.getBody();

                Intent intent = new Intent(SensorDataService.SENSOR_FIRMWARE_UPDATE_AVAILABLE);
                intent.putExtra(SensorDataService.SENSOR_ID, getSensorId());
                getContext().sendBroadcast(intent);

            } else {
                mFirmwareReleaseNotes = null;
            }

        } catch (WebbException we) {
            Log.d(TAG, we.getLocalizedMessage());
        } catch (JSONException je) {
            Log.d(TAG, je.getLocalizedMessage());
        }
    }

}
