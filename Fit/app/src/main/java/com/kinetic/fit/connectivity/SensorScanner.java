package com.kinetic.fit.connectivity;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanFilter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Log;

import com.kinetic.fit.FitApplication;
import com.kinetic.fit.connectivity.sensors.Sensor;
import com.kinetic.fit.connectivity.sensors.ble.BluetoothLESensorFactory;
import com.kinetic.fit.connectivity.sensors.ble.GenericPowerSensorFactory;
import com.kinetic.fit.connectivity.sensors.ble.GenericSpeedAndCadenceSensorFactory;
import com.kinetic.fit.connectivity.sensors.ble.HeartRateSensorFactory;
import com.kinetic.fit.connectivity.sensors.ble.InRideSensorFactory;
import com.kinetic.fit.connectivity.sensors.ble.SmartControlSensorFactory;
import com.kinetic.fit.controllers.SessionController;
import com.neovisionaries.bluetooth.ble.advertising.ADPayloadParser;
import com.neovisionaries.bluetooth.ble.advertising.ADStructure;
import com.neovisionaries.bluetooth.ble.advertising.UUIDs;

import org.androidannotations.annotations.EService;
import org.androidannotations.annotations.Receiver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@EService
public class SensorScanner extends Service {

    private static final String TAG = "SensorScanner";

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;

    // map UUIDs to Factories
    private HashMap<UUID, BluetoothLESensorFactory> mSensorFactories = new HashMap<UUID, BluetoothLESensorFactory>();
    private List<ScanFilter> mScanFilters = new ArrayList<>();

    // set of sensors that have been scanned already --> have to do this because 4.3 doesn't filter BLE sensors in the LeScan
    private HashSet<BluetoothDevice> mDevicesScanned = new HashSet<BluetoothDevice>();

    private SensorDataService.SensorDataServiceBinder mSensorData;
    private ServiceConnection mSensorDataConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mSensorData = (SensorDataService.SensorDataServiceBinder) service;

            // process connected devices...
            List<BluetoothDevice> devices = mBluetoothManager.getConnectedDevices(BluetoothProfile.GATT);
            for (BluetoothDevice device : devices) {
//                    createSensorForDevice(device, device.getUuids());
            }
            startScan();
        }

        public void onServiceDisconnected(ComponentName className) {
            mSensorData = null;
        }
    };

    public class SensorScannerBinder extends Binder {

        public void addSensorFactory(BluetoothLESensorFactory factory) {
            this.addSensorFactory(factory);
        }

        public void scan() {
            startScan();
        }

    }

    private final SensorScannerBinder mBinder = new SensorScannerBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public SensorScanner() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
//
        addSensorFactory(new InRideSensorFactory());
        addSensorFactory(new SmartControlSensorFactory());
        addSensorFactory(new HeartRateSensorFactory());
        addSensorFactory(new GenericSpeedAndCadenceSensorFactory());
        addSensorFactory(new GenericPowerSensorFactory());

        mBluetoothManager = (BluetoothManager) getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
        }

        bindService(SensorDataService_.intent(this).get(), mSensorDataConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        stopScan();
        unbindService(mSensorDataConnection);
        super.onDestroy();
    }

//    @Override
//    protected void onHandleIntent(Intent intent) {
//        // ... ? start Scan?
//    }

    private void addSensorFactory(BluetoothLESensorFactory factory) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mScanFilters.add(new ScanFilter.Builder().setServiceUuid(new ParcelUuid(factory.getPrimaryServiceUUID())).build());
        }
        mSensorFactories.put(factory.getPrimaryServiceUUID(), factory);

    }

    private void startScan() {
        if (mBluetoothAdapter != null) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                ScanSettings.Builder settingsBuilder = new ScanSettings.Builder()
//                        .setReportDelay(0)
//                        .setScanMode(ScanSettings.SCAN_MODE_BALANCED);
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    settingsBuilder.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
//                            .setMatchMode(ScanSettings.MATCH_MODE_STICKY)
//                            .setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT);
//                }
//                ScanSettings settings = settingsBuilder.build();
//                mBluetoothAdapter.getBluetoothLeScanner().startScan(mScanFilters, settings, mScanCallback2);
//            } else {
            if (mBluetoothAdapter.startLeScan(mScanCallback)) {
                Log.d(TAG, "Started LE Scan");
            } else {
                Log.d(TAG, "Unable to start LE Scan");
            }
        }
//        }


    }

    private void stopScan() {
        if (mBluetoothAdapter != null) {
//            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
//                mBluetoothAdapter.getBluetoothLeScanner().startScan(mScanCallback2);
//            } else {
            mBluetoothAdapter.stopLeScan(mScanCallback);
//            }
        }
    }


    boolean createSensorForDevice(BluetoothDevice device, UUID[] uuids) {
        boolean isSensor = false;
        for (UUID uuid : uuids) {
            BluetoothLESensorFactory factory = mSensorFactories.get(uuid);
            if (factory != null && mSensorData != null) {
                isSensor = true;
                Sensor sensor = factory.createSensor(getApplicationContext(), device);
                mSensorData.addSensor(sensor);
                break;
            }
        }
        return isSensor;
    }

    private BluetoothAdapter.LeScanCallback mScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (!mDevicesScanned.contains(device)) {
                mDevicesScanned.add(device);

                boolean isSensor = false;
                List<ADStructure> structures = ADPayloadParser.getInstance().parse(scanRecord);
                for (ADStructure structure : structures) {
                    if (isSensor) {
                        break;
                    }
                    if (structure instanceof UUIDs) {
                        UUIDs uuids = (UUIDs) structure;

                        isSensor = createSensorForDevice(device, uuids.getUUIDs());
                    }
                }
            }
        }
    };

//    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
//    ScanCallback mScanCallback2 = new ScanCallback() {
//        @Override
//        public void onScanResult(int callbackType, ScanResult result) {
//            super.onScanResult(callbackType, result);
//            if (callbackType == ScanSettings.CALLBACK_TYPE_ALL_MATCHES) {
//                if (result.getScanRecord() != null && result.getScanRecord().getServiceUuids() != null) {
//                    UUID[] uuids = new UUID[result.getScanRecord().getServiceUuids().size()];
//                    for (int i = 0; i < result.getScanRecord().getServiceUuids().size(); i++) {
//                        uuids[i] = result.getScanRecord().getServiceUuids().get(i).getUuid();
//                    }
//                    createSensorForDevice(result.getDevice(), uuids);
//                }
//            }
//        }
//
//    };


    private boolean inactive = false;

    @Receiver(actions = FitApplication.APPLICATION_IN_BACKGROUND)
    protected void onApplicationInBackground() {
        stopScan();
    }

    @Receiver(actions = FitApplication.APPLICATION_IN_FOREGROUND)
    protected void onApplicationInForeground(Intent intent) {
        startScan();
    }

    @Receiver(actions = SessionController.START_SENSOR_SCAN)
    protected void onSessionControllerScan(Intent intent) {
        startScan();
    }
}
