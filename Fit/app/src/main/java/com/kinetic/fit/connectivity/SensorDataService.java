package com.kinetic.fit.connectivity;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.kinetic.fit.FitApplication;
import com.kinetic.fit.connectivity.sensors.DynamicResistanceSensor;
import com.kinetic.fit.connectivity.sensors.Sensor;
import com.kinetic.fit.connectivity.sensors.TrainerMode;
import com.kinetic.fit.data.realm_objects.Profile;
import com.kinetic.fit.ui.settings.SensorUpdateActivity_;
import com.kinetic.fit.ui.settings.SettingsActivity;

import org.androidannotations.annotations.EService;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.UiThread;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.WeakHashMap;

@EService
public class SensorDataService extends Service implements Sensor.SensorObserver {

    private static final String TAG = "SensorDataService";

    public static final String SENSOR_DATA = "SensorDataService.SENSOR_DATA";

    public static final String SENSOR_DATA_POWER = "SensorDataService.SENSOR_DATA_POWER";
    public static final String SENSOR_DATA_SPEED = "SensorDataService.SENSOR_DATA_SPEED";
    public static final String SENSOR_DATA_HEART_RATE = "SensorDataService.SENSOR_DATA_HEART_RATE";
    public static final String SENSOR_DATA_CADENCE = "SensorDataService.SENSOR_DATA_CADENCE";

    public static final String SENSOR_ID = "SensorDataService.SENSOR_ID";
    public static final String SENSOR_FIRMWARE_UPDATE_AVAILABLE = "SensorDataService.FIRMWARE_UPDATE_AVAILABLE";
    public static final String SENSOR_FIRMWARE_UPDATE_START = "SensorDataService.SENSOR_FIRMWARE_UPDATE_START";
    public static final String SENSOR_FIRMWARE_UPDATE_STARTED = "SensorDataService.SENSOR_FIRMWARE_UPDATE_STARTED";
    public static final String SENSOR_FIRMWARE_UPDATE_PROGRESS = "SensorDataService.SENSOR_FIRMWARE_UPDATE_PROGRESS";
    public static final String SENSOR_FIRMWARE_UPDATE_COMPLETE = "SensorDataService.SENSOR_FIRMWARE_UPDATE_COMPLETE";
    public static final String SENSOR_FIRMWARE_UPDATE_PROGRESS_PERCENT = "SensorDataService.SENSOR_FIRMWARE_UPDATE_PROGRESS_PERCENT";
    public static final String SENSOR_FIRMWARE_PROGRESS_TOAST = "SensorDataService.SENSOR_FIRMWARE_PROGRESS_TOAST";

    public interface SensorDataObserver {
        void sensorAdded(Sensor sensor);

        void sensorRemoved(Sensor sensor);

        void sensorAssignmentsChanged();
    }

    private TrainerMode trainerMode = new TrainerMode();

    public TrainerMode getTrainerMode() {
        return trainerMode;
    }

    protected Set<SensorDataObserver> mObservers = Collections.newSetFromMap(new WeakHashMap<SensorDataObserver, Boolean>());

    public class SensorDataServiceBinder extends Binder {

        public void startForegroung(int id, Notification notification) {
            startForeground(id, notification);
        }

        public void stopService() {
            stopSelf();
        }

        public void registerObserver(SensorDataObserver observer) {
            mObservers.add(observer);
        }

        public void unregisterObserver(SensorDataObserver observer) {
            mObservers.remove(observer);
        }

        public Sensor getHeartRateSensor() {
            return mHeartRateSensor;
        }

        public void setHeartRateSensor(Sensor sensor) {
            SensorDataService.this.setHeartRateSensor(sensor);
        }

        public Sensor getPowerSensor() {
            return mPowerSensor;
        }

        public void setPowerSensor(Sensor sensor) {
            SensorDataService.this.setPowerSensor(sensor);
        }

        public Sensor getSpeedSensor() {
            return mSpeedSensor;
        }

        public void setSpeedSensor(Sensor sensor) {
            SensorDataService.this.setSpeedSensor(sensor);
        }

        public Sensor getCadenceSensor() {
            return mCadenceSensor;
        }

        public void setCadenceSensor(Sensor sensor) {
            SensorDataService.this.setCadenceSensor(sensor);
        }

        public Collection<Sensor> getSensors() {
            return mConnectedSensors.values();
        }

        public void addSensor(Sensor sensor) {
            SensorDataService.this.addSensor(sensor);
        }

        public void removeSensor(Sensor sensor) {
            SensorDataService.this.removeSensor(sensor);
        }

        public Sensor getSensorById(String sensorId) {
            return mConnectedSensors.get(sensorId);
        }

        public TrainerMode getTrainerMode() {
            return SensorDataService.this.getTrainerMode();
        }

        public void setWorkoutSessionActive(boolean active) {
            SensorDataService.this.workoutSessionActive = active;
        }
    }

    private final SensorDataServiceBinder mBinder = new SensorDataServiceBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private Sensor mPowerSensor;
    private Sensor mSpeedSensor;
    private Sensor mCadenceSensor;
    private Sensor mHeartRateSensor;
    SharedPreferences sharedPreferences;

    private HashMap<String, Sensor> mConnectedSensors = new HashMap<String, Sensor>();

    private boolean workoutSessionActive = false;

    @Override
    public void onCreate() {
        super.onCreate();
        sharedPreferences = getSharedPreferences(SettingsActivity.getSettingsNamespace(), MODE_PRIVATE);
        // restore sensors?
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void addSensor(Sensor sensor) {
        String recentUUID;
        if (sensor != null && !mConnectedSensors.containsKey(sensor.getSensorId())) {
            mConnectedSensors.put(sensor.getSensorId(), sensor);
            sensor.addObserver(this);
            for (SensorDataObserver observer : mObservers) {
                observer.sensorAdded(sensor);
            }
            if (mSpeedSensor == null) {
                recentUUID = sharedPreferences.getString(SettingsActivity.PREV_SPEED_SENSOR + Profile.getUUID(), null);
                if (sensor.getSensorId().equals(recentUUID)) {
                    setSpeedSensor(sensor);
                    sensor.connect();
                }
            }
            if (mPowerSensor == null) {
                recentUUID = sharedPreferences.getString(SettingsActivity.PREV_POWER_SENSOR + Profile.getUUID(), null);
                if (sensor.getSensorId().equals(recentUUID)) {
                    setPowerSensor(sensor);
                    sensor.connect();
                }
            }
            if (mCadenceSensor == null) {
                recentUUID = sharedPreferences.getString(SettingsActivity.PREV_CADENCE_SENSOR + Profile.getUUID(), null);
                if (sensor.getSensorId().equals(recentUUID)) {
                    setCadenceSensor(sensor);
                    sensor.connect();
                }
            }
            if (mHeartRateSensor == null) {
                recentUUID = sharedPreferences.getString(SettingsActivity.PREV_HEART_SENSOR + Profile.getUUID(), null);
                if (sensor.getSensorId().equals(sensor)) {
                    setHeartRateSensor(sensor);
                    sensor.connect();
                }
            }
        }
    }

    private void notifySensorAssignmentChange() {
        for (SensorDataObserver observer : mObservers) {
            observer.sensorAssignmentsChanged();
        }
    }

    private void removeSensor(Sensor sensor) {
        Sensor removed = mConnectedSensors.remove(sensor.getSensorId());
        if (removed != null) {
            removed.removeObserver(this);
            for (SensorDataObserver observer : mObservers) {
                observer.sensorRemoved(sensor);
            }
        }
    }

    private void autoAssignSensor(Sensor sensor) {
        if (sensor == null) {
            return;
        }
        boolean changed = false;
        if (mCadenceSensor == null && sensor.providesCadence()) {
            setCadenceSensor(sensor);
            changed = true;
        }
        if (mSpeedSensor == null && sensor.providesSpeed()) {
            setSpeedSensor(sensor);
            changed = true;
        }
        if (mHeartRateSensor == null && sensor.providesHeartRate()) {
            setHeartRateSensor(sensor);
            changed = true;
        }
        if (mPowerSensor == null && sensor.providesPower()) {
            setPowerSensor(sensor);
            changed = true;
        }
        if (changed) {
            notifySensorAssignmentChange();
        }
    }

    private void setHeartRateSensor(Sensor sensor) {
        if (sensor != null && sensor.providesHeartRate()) {
            mHeartRateSensor = sensor;
            sharedPreferences.edit().putString(SettingsActivity.PREV_HEART_SENSOR + Profile.getUUID(), mHeartRateSensor.getSensorId()).apply();
        } else if (sensor == null) {
            mHeartRateSensor = null;
        } else {
            Log.e(TAG, "Sensor does not provide Heart Rate");
        }
        notifySensorAssignmentChange();
    }

    private void setPowerSensor(Sensor sensor) {
        if (sensor != null && sensor.providesPower()) {
            mPowerSensor = sensor;
            sharedPreferences.edit().putString(SettingsActivity.PREV_POWER_SENSOR + Profile.getUUID(), mPowerSensor.getSensorId()).apply();
            if (sensor instanceof DynamicResistanceSensor) {
                trainerMode.setDynamicResistance((DynamicResistanceSensor) sensor);
            }
        } else if (sensor == null) {
            mPowerSensor = null;
            trainerMode.setDynamicResistance(null);
        } else {
            Log.e(TAG, "Sensor does not provide Power");
        }
        notifySensorAssignmentChange();
    }

    private void setSpeedSensor(Sensor sensor) {
        if (sensor != null && sensor.providesSpeed()) {
            mSpeedSensor = sensor;
            sharedPreferences.edit().putString(SettingsActivity.PREV_SPEED_SENSOR + Profile.getUUID(), mSpeedSensor.getSensorId()).apply();
        } else if (sensor == null) {
            mSpeedSensor = null;
        } else {
            Log.e(TAG, "Sensor does not provide Speed");
        }
        notifySensorAssignmentChange();
    }

    private void setCadenceSensor(Sensor sensor) {
        if (sensor != null && sensor.providesCadence()) {
            mCadenceSensor = sensor;
            sharedPreferences.edit().putString(SettingsActivity.PREV_CADENCE_SENSOR + Profile.getUUID(), mCadenceSensor.getSensorId()).apply();
        } else if (sensor == null) {
            mCadenceSensor = null;
        } else {
            Log.e(TAG, "Sensor does not provide Cadence");
        }

        notifySensorAssignmentChange();
    }


    @Override
    public void sensorStateChanged(Sensor sensor, Sensor.State state) {
        switch (state) {
            case Disconnected:
                sensorDisconnected(sensor);
                break;
            case Connecting:
                break;
            case Connected:
                autoAssignSensor(sensor);
                break;
            case Disconnecting:
                break;
        }
    }

    private void sensorDisconnected(Sensor sensor) {
        if (mCadenceSensor == sensor) {
            mCadenceSensor = null;
        }
        if (mSpeedSensor == sensor) {
            mSpeedSensor = null;
        }
        if (mPowerSensor == sensor) {
            mPowerSensor = null;
        }
        if (mHeartRateSensor == sensor) {
            mHeartRateSensor = null;
        }
        notifySensorAssignmentChange();
    }

    @Override
    public void sensorValueChanged(Sensor sensor) {
        if (sensor.equals(mCadenceSensor) || sensor.equals(mSpeedSensor) || sensor.equals(mPowerSensor) || sensor.equals(mHeartRateSensor)) {
            Intent intent = new Intent(SENSOR_DATA);

            intent.putExtra("SensorId", sensor.getSensorId());
            if (mCadenceSensor != null) {
                intent.putExtra(SENSOR_DATA_CADENCE, mCadenceSensor.currentCadence());
            }
            if (mSpeedSensor != null) {
                intent.putExtra(SENSOR_DATA_SPEED, mSpeedSensor.currentSpeed());
            }
            if (mPowerSensor != null) {
                intent.putExtra(SENSOR_DATA_POWER, mPowerSensor.currentPower());
            }
            if (mHeartRateSensor != null) {
                intent.putExtra(SENSOR_DATA_HEART_RATE, mHeartRateSensor.currentHeartRate());
            }
            sendBroadcast(intent);
        }
    }

    private boolean inactive = false;

    @Receiver(actions = FitApplication.APPLICATION_IN_BACKGROUND)
    protected void onApplicationInBackground() {
        if (workoutSessionActive) {
            return;
        }
        for (Sensor sensor : mConnectedSensors.values()) {
            sensor.disconnect();
        }
        inactive = true;
    }

    @Receiver(actions = FitApplication.APPLICATION_IN_FOREGROUND)
    protected void onApplicationInForeground(Intent intent) {
        // check if we are returning from a disconnected state
        if (!inactive) {
            return;
        }
        if (mCadenceSensor != null) {
            mCadenceSensor.connect();
        }
        if (mSpeedSensor != null) {
            mSpeedSensor.connect();
        }
        if (mHeartRateSensor != null) {
            mHeartRateSensor.connect();
        }
        if (mPowerSensor != null) {
            mPowerSensor.connect();
        }
        inactive = false;
    }

    @Receiver(actions = SensorDataService.SENSOR_FIRMWARE_UPDATE_START)
    protected void onFirmwareUpdateStart(Intent intent) {
        String sensorId = intent.getStringExtra(SensorDataService.SENSOR_ID);
        if (sensorId != null) {
            Sensor sensor = mConnectedSensors.get(sensorId);
            if (sensor != null) {
                sensor.startFirmwareUpdate();
            }
        }
    }

    @UiThread
    @Receiver(actions = SensorDataService.SENSOR_FIRMWARE_UPDATE_STARTED)
    protected void onFirmwareUpdateStarted(Intent intent) {
        SensorUpdateActivity_.intent(getBaseContext()).flags(Intent.FLAG_ACTIVITY_NEW_TASK).start();
//        Intent i = new Intent(SENSOR_FIRMWARE_PROGRESS_TOAST);
//        i.putExtra("message", "Firmware Update Started. This will take about 5 minutes. Please be patient. " +
//                "\n\nIt is not recommended to have an active workout at this time.\n\n You will need " +
//                "to restart the app and cycle the power on your smart trainer once the update is " +
//                "complete");
//        sendBroadcast(i);
    }

//    @UiThread
//    @Receiver(actions = SensorDataService.SENSOR_FIRMWARE_UPDATE_PROGRESS)
//    protected void onFirmwareUpdateProgress(Intent intent) {
//        int progress = intent.getIntExtra(SENSOR_FIRMWARE_UPDATE_PROGRESS_PERCENT, 0);
//        if (progress > 0 && progress % 5 == 0) {
//            Log.d(TAG, "Making Toast");
//            Intent i = new Intent(SENSOR_FIRMWARE_PROGRESS_TOAST);
//            i.putExtra("message", "Firmware Update " + progress + "% Complete");
//            sendBroadcast(i);
//        }
//        Log.d(TAG, "Firmware Update " + progress + "% Complete");
//    }

    @UiThread(delay = 1000)
    @Receiver(actions = SensorDataService.SENSOR_FIRMWARE_UPDATE_COMPLETE)
    protected void onFirmwareUpdateComplete(Intent intent) {
//        Build System Notification to persist instructions
//        NotificationCompat.Builder notification = new NotificationCompat.Builder(this)
//                .setSmallIcon(R.drawable.ic_stat_name)
//                .setLargeIcon(((BitmapDrawable) getResources().getDrawable(R.mipmap.ic_launcher)).getBitmap())
//                .setContentTitle("Kinetic Fit Notification (Slide Down")
//                .setContentText("Firmware Update Complete")
//                .setDefaults(DEFAULT_ALL)
//                .setPriority(PRIORITY_MAX)
//                .setColor(ContextCompat.getColor(this, R.color.fit_dark_green))
//                .setStyle(new NotificationCompat.BigTextStyle()
//                        .bigText("Your Smart Controller Firmware has updated. Please quit the Fit " +
//                                "app completely, unplug your Smart Control for one minute, then " +
//                                "plug it back it and restart Kinetic Fit."));
//
//        Intent resultIntent = new Intent(this, com.kinetic.fit.ui.settings.SensorsActivity_.class);
//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
//        stackBuilder.addParentStack(com.kinetic.fit.ui.settings.SensorsActivity_.class);
//
////          Adds the Intent that starts the Activity to the top of the stack
//        stackBuilder.addNextIntent(resultIntent);
//        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
//        notification.setContentIntent(resultPendingIntent);
//
//        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
////          notificationID allows you to update the notification later on.
//        mNotificationManager.notify(1, notification.build());
        String sensorId = intent.getStringExtra(SensorDataService.SENSOR_ID);
        if (sensorId != null) {
            Sensor sensor = mConnectedSensors.get(sensorId);
            if (sensor != null) {
                sensor.disconnect();
            }
        }
    }

}
