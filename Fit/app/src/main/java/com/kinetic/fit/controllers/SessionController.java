package com.kinetic.fit.controllers;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.kinetic.fit.connectivity.SensorDataService;
import com.kinetic.fit.connectivity.SensorDataService_;
import com.kinetic.fit.connectivity.SensorValues;
import com.kinetic.fit.connectivity.sensors.Sensor;
import com.kinetic.fit.connectivity.sensors.TrainerMode;
import com.kinetic.fit.data.DataSync;
import com.kinetic.fit.data.DataSync_;
import com.kinetic.fit.data.objects.WorkoutInterval;
import com.kinetic.fit.data.realm_objects.Profile;
import com.kinetic.fit.data.realm_objects.Session;
import com.kinetic.fit.data.realm_objects.Workout;
import com.kinetic.fit.data.session_objects.InMemoryProfile;
import com.kinetic.fit.data.session_objects.SessionDataSlice;
import com.kinetic.fit.data.session_objects.SessionLap;
import com.kinetic.fit.ui.login.LoginDispathActivity;
import com.kinetic.fit.ui.settings.SettingsActivity;
import com.kinetic.fit.util.FITZoneMonitor;
import com.kinetic.fit.util.ViewStyling;

import org.androidannotations.annotations.EService;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.UiThread;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.WeakHashMap;

import io.realm.Realm;

@EService
public class SessionController extends Service implements FITZoneMonitor.AutoLapObserver {

    private static final String TAG = "SessionController";
    public static final String START_SENSOR_SCAN = "SessionController.START_SENSOR_SCAN";

    public interface SessionControllerObserver {
        void sessionTick(double timeDelta);

        void sessionStateChanged(SessionState state);

        void newWorkoutTextAndTime(WorkoutTextAndTime tat);
    }

    protected Set<SessionControllerObserver> mObservers = Collections.newSetFromMap(new WeakHashMap<SessionControllerObserver, Boolean>());


    public enum SessionState {
        Idle,
        Warmup,
        WarmupPaused,
        Calibration,
        Workout,
        WorkoutPaused,
        Complete,
        Cancelled
    }


    private SensorDataService.SensorDataServiceBinder mSensorDataService;
    private ServiceConnection mSensorDataServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mSensorDataService = (SensorDataService.SensorDataServiceBinder) service;
            if (autoLap) {
                zoneMonitor.addObserver(SessionController.this);
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            mSensorDataService = null;
        }
    };

    private DataSync.DataSyncBinder mDataSync;
    private ServiceConnection mDataSyncConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mDataSync = (DataSync.DataSyncBinder) service;

        }

        public void onServiceDisconnected(ComponentName className) {
            Crashlytics.log(1, TAG, "onServiceDisconnected: service disconnected and mDataSync set to null");
            mDataSync = null;
        }
    };

    public class TargetPowerCadenceRange {
        private double powerMin = -1;
        private double powerMax = -1;
        private double cadence = -1;

        private double powerAvg() {
            return (powerMin + powerMax) * 0.5;
        }

        private double targetPower() {
            double targetPowerAvg = powerAvg();
            try {
                int ftp = Profile.getProfileFTP();
                if (ftp > 0) {
                    return (targetPowerAvg / 100) * ftp;
                }
            } catch (Exception e) {
                Crashlytics.logException(e);
            }
            return targetPowerAvg / 100;

        }

        public double getCadence() {
            return cadence;
        }
    }

    public class SessionControllerBinder extends Binder {

        boolean mIsBound;

        public void stopTimer() {
            if (sessionTimer != null) {
                sessionTimer.cancel();
                sessionTimer = null;
            }
        }

//        private void startForeground(int id, Notification notification) {
//            startForeground(id, notification);
//        }

//        public void stopService() {
//            stopSelf();
//        }

        public WorkoutInterval getCurrentInterval() {
            return currentInterval;
        }

        public int getNewFTP() {
            return newFTP;
        }

        public void registerObserver(SessionControllerObserver observer) {
            mObservers.add(observer);
        }

        public void unregisterObserver(SessionControllerObserver observer) {
            mObservers.remove(observer);
        }

        public void startResumeWarmup() {
            setSessionState(SessionState.Warmup);
        }

        public void pauseWarmup() {
            setSessionState(SessionState.WarmupPaused);
        }

        public void restartWarmup() {
            durations.warmupTimeRemaining = WARMUP_TIME;
            setSessionState(SessionState.Warmup);
        }

        public void endWarmup() {
            durations.warmupTimeRemaining = 0;
            setSessionState(SessionState.Idle);
        }

        public void startCalibration() {
            setSessionState(SessionState.Calibration);
        }

        public void endCalibration() {
            setSessionState(SessionState.Idle);
        }

        public void startResumeWorkout() {
            setSessionState(SessionState.Workout);
        }

        public void pauseWorkout() {
            setSessionState(SessionState.WorkoutPaused);
        }

//        public void endWorkout() {
//            setSessionState(SessionState.Idle);
//        }

//        public void cancelSession() {
//            setSessionState(SessionState.Cancelled);
//        }

        public void deleteSession() {
            mDataSync.deleteSession(mSession);
        }

        public void completeSession() {
            setSessionState(SessionState.Complete);
        }

        public Durations getDurations() {
            return durations;
        }

        public Session getSession() {
            return mSession;
        }

        public SessionLap getCurrentLap() {
            return currentLap;
        }

        public SessionLap getPreviousLap() {
            return previousLap;
        }

        public Session finishAndCleanup(String caller) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    sessionComplete();
                    mSession = realm.copyToRealmOrUpdate(SessionController.this.mSession);
                }
            });
            stopSelf();
            Log.d(TAG, "finish and clean up called by " + caller);
            return mSession;
        }

        public void refreshSettings() {
            difficultyModifier = 1 + ((double) sharedPreferences.getInt(SettingsActivity.DIFFICULTY_PCT + Profile.getUUID(), 0) / 100.0);
            autoLap = sharedPreferences.getBoolean(SettingsActivity.AUTO_LAP_INDICATORS + Profile.getUUID(), false);


        }

        public SessionDataSlice getCurrentDataSlice() {
            return currentDataSlice;
        }

        public SensorValues getSensorValues() {
            return sensorValues;
        }

        public boolean sessionRunning() {
            return SessionController.this.sessionRunning();
        }

        public boolean sessionComplete() {
            return sessionState == SessionState.Complete;
        }

        public Workout getWorkout() {
            return workout;
        }

        public void markLap() {
            SessionController.this.markLap();
        }

        public TargetPowerCadenceRange getTargets() {
            return intervalTarget;
        }

        public double getIntervalTargetPower() {
            return intervalTarget.targetPower() * difficultyModifier;
        }

        public SessionState getState() {
            return sessionState;
        }

        public double powerAverageForPreviousTime(double time) {
            return Session.powerAverageForPreviousTime(time, mSession.getDuration(), mSession.dataSlices);
        }

        public Sensor getPowerSensor() {
            return mSensorDataService.getPowerSensor();
        }

        public String getCadenceSensorName() {
            if (mSensorDataService.getCadenceSensor() != null &&
                    mSensorDataService.getCadenceSensor().getName() != null) {
                return mSensorDataService.getCadenceSensor().getName();
            } else {
                return "None";
            }
        }

        public String getPowerSensorName() {
            if (mSensorDataService.getPowerSensor() != null &&
                    mSensorDataService.getPowerSensor().getName() != null) {
                return mSensorDataService.getPowerSensor().getName();
            } else {
                return "None";
            }
        }

        public String getSpeedSensorName() {
            if (mSensorDataService.getSpeedSensor() != null &&
                    mSensorDataService.getSpeedSensor().getName() != null) {
                return mSensorDataService.getSpeedSensor().getName();
            } else {
                return "None";
            }
        }

        public String getHeartSensorName() {
            if (mSensorDataService.getHeartRateSensor() != null &&
                    mSensorDataService.getHeartRateSensor().getName() != null) {
                return mSensorDataService.getHeartRateSensor().getName();
            } else {
                return "None";
            }
        }

        public void setWorkout(Workout workout) {
            SessionController.this.workout = workout;
            if (profile != null) {
                if (mDataSync != null) {
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            mSession = mDataSync.createSession();
                        }
                    });
                }
            } else {
                startActivity(new Intent(getApplicationContext(), LoginDispathActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                stopSelf();
            }
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    mSession.initialize(profile);
                }
            });
            if (workout != null) {
                workoutIntervals = workout.getIntervals();
                mSession.setWorkoutId(workout.getObjectId());
                mSession.setWorkoutName(workout.getName());
                mSession.setWorkoutDescription(workout.getOverview());
                textAndTimesArray.clear();

                double iStart = 0;
                for (WorkoutInterval interval : workoutIntervals) {
                    WorkoutTextAndTime tat = new WorkoutTextAndTime();
                    double textAdvance = 5;
                    double textDuration = 5;
                    if (interval.text != null) {
                        tat.text = interval.text;
                        textAdvance = interval.textAdvance;
                        textDuration = interval.textDuration;
                    }
                    tat.countdown = textAdvance;
                    tat.durationText = ViewStyling.timeToStringMS(interval.duration);
                    tat.timeStart = iStart - textAdvance;
                    tat.timeEnd = tat.timeStart + textDuration;
                    tat.powerStart = interval.startPower;
                    tat.powerEnd = interval.endPower;

                    textAndTimesArray.add(tat);
                    iStart += interval.duration;
                }

                Collections.sort(textAndTimesArray, new Comparator<WorkoutTextAndTime>() {
                    @Override
                    public int compare(WorkoutTextAndTime tat1, WorkoutTextAndTime tat2) {
                        if (tat1.timeStart < tat2.timeStart) {
                            return 1;
                        } else if (tat1.timeStart > tat2.timeStart) {
                            return -1;
                        } else if (tat1.timeStart == tat2.timeStart) {
                            if (tat1.timeEnd < tat2.timeEnd) {
                                return 1;
                            } else if (tat1.timeEnd > tat2.timeEnd) {
                                return -1;
                            }
                        }
                        return 0;
                    }
                });

            } else {
                // TODO: Resource look up. i8ln
                mSession.setWorkoutName("Free Ride");
                mSession.setWorkoutDescription("Free Ride");
                workoutIntervals = null;
                textAndTimesArray.clear();
            }

            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    mSession = realm.copyToRealmOrUpdate(mSession);
                }
            });
        }

        public SessionControllerBinder getSessionControllerService() {
            return SessionControllerBinder.this;
        }

        public boolean isBound() {
            return mIsBound;
        }

        public void setmIsBound(boolean mIsBound) {
            this.mIsBound = mIsBound;
        }
    }

    private final SessionControllerBinder binder = new SessionControllerBinder();

    @Override
    public IBinder onBind(Intent intent) {
        if (mSensorDataService == null) {
            bindService(SensorDataService_.intent(this).get(), mSensorDataServiceConnection, Context.BIND_AUTO_CREATE);
        }
        return binder;
    }


//    KINDispatcher.instance.onAppDidReceiveMemoryWarning.listen(self) { [weak self] note in
//        self?.snapshotSession()
//    }
//    KINDispatcher.instance.onAppWillTerminate.listen(self) { [weak self] note in
//        self?.completeSession()
//    }

    static private double IDLE_SPEED_PAUSE_TIME = 2;
    static private double WARMUP_TIME = 10 * 60;

    public class Durations {
        public double sessionDuration = 0;
        public double warmupDuration = 0;
        public double workoutDuration = 0;
        public double intervalDuration = 0;
        public double lapDuration = 0;
        public double warmupTimeRemaining = WARMUP_TIME;
        public double workoutTimeRemaining = 0;
        public double intervalTimeRemaining = 0;
        private double intervalStart = 0;
    }

    private Durations durations = new Durations();

    private SensorValues sensorValues = new SensorValues();

    private Profile profile;
    private Workout workout;
    private ArrayList<WorkoutInterval> workoutIntervals;

    private SessionState sessionState;
    private Session mSession;

    private SessionLap currentLap;
    private SessionLap previousLap;
    private WorkoutInterval currentInterval;
    private WorkoutInterval nextInterval;
    private SessionDataSlice currentDataSlice;

    private ArrayList<WorkoutTextAndTime> textAndTimesArray = new ArrayList<>();
    private WorkoutTextAndTime currentWorkoutText;
    private TargetPowerCadenceRange intervalTarget = new TargetPowerCadenceRange();

    private int newFTP;

    private Timer sessionTimer;
    private long sessionTimerFireTime;
    private long sessionStart;
    private long lastSensorDataTime;


    private void setCurrentInterval(WorkoutInterval interval) {
        currentInterval = interval;
        if (currentInterval != null) {
            if (currentInterval.lapCue) {
                markLapAtTime(mSession.getWarmupDuration() + durations.intervalStart);
            }
            if (workoutIntervals != null) {
                int intervalIndex = workoutIntervals.indexOf(currentInterval);
                if (intervalIndex + 1 < workoutIntervals.size()) {
                    nextInterval = workoutIntervals.get(intervalIndex + 1);
                } else {
                    nextInterval = null;
                }
            }
        }
    }


    public void setSessionState(SessionState state) {
        SessionState oldState = sessionState;

        sessionState = state;

        mSensorDataService.setWorkoutSessionActive(sessionRunning());

        switch (sessionState) {
            case Idle: {
                stopSessionTimer();
            }
            break;
            case Warmup: {
                mSensorDataService.getTrainerMode().setMode(TrainerMode.Mode.Fluid);
                startSessionTimer();
            }
            break;
            case Calibration: {
                startSessionTimer();
            }
            break;
            case WarmupPaused: {
                stopSessionTimer();
            }
            break;
            case Complete: {
                stopSessionTimer();
                mSensorDataService.getTrainerMode().setMode(TrainerMode.Mode.Fluid);
                snapshotSession();
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        profile.setTotalTime(Profile.getProfileTotalTime() + durations.sessionDuration);
                        profile.setTotalDistanceKM(Profile.getProfileTotalDistanceKM() + mSession.getDistanceKM());
                        profile.setTotalKilojoules(Profile.getProfileTotalKJ() + mSession.getKilojoules());
                        mDataSync.saveOrUpdateSession(mSession);
                    }
                });

                newFTP = -1;
                if (workout != null) {
                    if (workout.isFTPTest() && workout.getftpCalcMod() > 0 && ftpPowerTotal > 0
                            && ftpTimeTotal > 0) {
                        newFTP = (int) (Math.round((ftpPowerTotal / ftpTimeTotal)
                                * ((double) workout.getftpCalcMod()) / 100.0));
                    }
                }
            }
            break;
            case Workout: {
                if (workout == null || workout.isFTPTest()) {
                    mSensorDataService.getTrainerMode().setMode(TrainerMode.Mode.Fluid);
                } else {
                    mSensorDataService.getTrainerMode().setMode(TrainerMode.Mode.ERG);
                }
                startSessionTimer();
            }
            break;
            case WorkoutPaused: {
                mSensorDataService.getTrainerMode().setMode(TrainerMode.Mode.Fluid);
                stopSessionTimer();
                snapshotSession();
            }
            break;
            case Cancelled: {
                mSensorDataService.getTrainerMode().setMode(TrainerMode.Mode.Fluid);
                stopSessionTimer();

                stopSelf();
            }
            break;
        }
        if (oldState != sessionState) {
            for (SessionControllerObserver observer : mObservers) {
                observer.sessionStateChanged(sessionState);
            }
        }
    }


    private boolean sessionComplete() {
        return sessionState == SessionState.Complete;
    }

    private boolean sessionRunning() {
        return sessionState == SessionState.Warmup || sessionState == SessionState.Calibration || sessionState == SessionState.Workout;
    }

    //    TODO when to call this, as it needs to update Realm and then fire off web hook
    private void snapshotSession() {
        calculateAveragesForTick();

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                byte[] sensorData = SessionDataSlice.serialize(mSession.dataSlices);
                mSession.setSensorData(sensorData);
//        mSession.lapMarkers = mSession.laps.map { $0.startTime
                mSession.setWorkoutDuration(durations.workoutDuration);
                mSession.setWorkoutDuration(durations.workoutDuration);
                mSession.setDuration(durations.sessionDuration);
                mDataSync.saveOrUpdateSession(mSession);
            }
        });

    }

//    private void commitChanges() {
//        Realm instance = Realm.getDefaultInstance();
//        instance.beginTransaction();
//        instance.copyToRealmOrUpdate(mSession);
//        instance.commitTransaction();
//        instance.close();
//    }


    private static final long TIMER_TICK_MS = 50;

    private void startSessionTimer() {
        if (sessionTimer == null) {
            sessionTimer = new Timer(true);
            sessionTimerFireTime = System.currentTimeMillis();
            sessionTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    sessionTimerHandler();
                }
            }, 0, TIMER_TICK_MS);
        }

        if (sessionStart == 0) {
            sessionStart = sessionTimerFireTime;
            commitAndCreateDataSlice(true);
            markLap();
        }
    }

    private void stopSessionTimer() {
        if (sessionTimer != null) {
            sessionTimer.cancel();
            sessionTimer = null;
        }
    }

    private void markLapAtTime(final double sessionTime) {
        if (sessionRunning()) {
            calculateAveragesForTick();

            SessionLap newLap = new SessionLap(sessionTime);
            newLap.setLapNumber(mSession.laps.size());
            mSession.laps.add(newLap);
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    mSession.addLapMarker(sessionTime);
                }
            });
            previousLap = currentLap;
            currentLap = newLap;
            durations.lapDuration = 0;
        }
    }

    public void markLap() {
        markLapAtTime(durations.sessionDuration);
    }

    private double lastSliceCreationTime = 0;
    private double ftpPowerTotal = 0.0;
    private double ftpTimeTotal = 0.0;

    private FITZoneMonitor zoneMonitor;

    private void commitAndCreateDataSlice(boolean force) {
        if (Math.floor(lastSliceCreationTime) < Math.floor(durations.sessionDuration) || force) {
            currentDataSlice = new SessionDataSlice();
            currentDataSlice.timestamp = durations.sessionDuration;
            currentDataSlice.duration = currentDataSlice.timestamp - lastSliceCreationTime;
            currentDataSlice.currentHeartRate = sensorValues.currentHeartRate;
            currentDataSlice.currentPower = sensorValues.currentPower;
            currentDataSlice.currentSpeedKPH = sensorValues.currentSpeedKPH;
            currentDataSlice.currentCadence = sensorValues.currentCadence;

            if (mSession == null) {
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        mSession = mDataSync.createSession();
                    }
                });
            }
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    mSession.addDataSlice(currentDataSlice);
                    if (currentLap != null) {
                        Session.addDataSlice(currentDataSlice, currentLap, mSession.dataSlices, Profile.getProfileFTP());
                    }
                }
            });

            lastSliceCreationTime = durations.sessionDuration;
            recordFTPData(currentDataSlice);

            zoneMonitor.setCurrentPowerZone(currentDataSlice.currentPowerZone);
            zoneMonitor.setCurrentHeartZone(currentDataSlice.currentHeartRateZone);
        }
    }

    private void recordFTPData(SessionDataSlice slice) {
        if (currentInterval != null) {
            if (currentInterval.ftpCalcInterval && workout != null) {
                if (slice.currentPower > -1) {
                    if (workout.getftpCalcProp() > 0) {
                        ftpPowerTotal += (double) slice.currentPower;
                        ftpTimeTotal += slice.duration;
                    }
                }
            }
        }
    }

    @UiThread
    protected void sessionTimerHandler() {
        long now = System.currentTimeMillis();
        double timeInterval = (now - sessionTimerFireTime) / 1000.0;
        sessionTimerFireTime = now;

        if (sessionRunning()) {
            if (currentLap != null) {
                durations.lapDuration += timeInterval;
            }

            durations.sessionDuration += timeInterval;

            commitAndCreateDataSlice(false);

            if (sessionState == SessionState.Warmup || sessionState == SessionState.Calibration) {
                durations.warmupDuration += timeInterval;
                if (sessionState == SessionState.Warmup) {
                    durations.warmupTimeRemaining -= timeInterval;
                    durations.warmupTimeRemaining = Math.max(0, durations.warmupTimeRemaining);
                }
                if (mSession != null) {
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            mSession.setWarmupDuration(durations.warmupDuration);
                        }
                    });
                }
            } else if (sessionState == SessionState.Workout) {
                durations.workoutDuration += timeInterval;
                if (workout != null) {
                    durations.workoutTimeRemaining = Math.max(0, workout.getDuration() - durations.workoutDuration);
                }
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        mSession.setWorkoutDuration(durations.workoutDuration);
                    }
                });

                updateWorkoutValues();
                updateWorkoutIntervalText();
            }

            if (currentInterval != null) {
                durations.intervalDuration = durations.workoutDuration - durations.intervalStart;
                durations.intervalTimeRemaining = Math.max(0, (durations.intervalStart + currentInterval.duration) - durations.workoutDuration);
            }
        }

        // Auto-pause warmup if they stop pedaling.
        if (sessionState == SessionState.Warmup && idleSpeedTime >= IDLE_SPEED_PAUSE_TIME) {
            setSessionState(SessionState.WarmupPaused);
        }

        for (SessionControllerObserver observer : mObservers) {
            observer.sessionTick(timeInterval);
        }

        zoneMonitor.addTime(timeInterval, currentDataSlice.currentPowerZone, currentDataSlice.currentHeartRateZone);
    }

    private void updateWorkoutValues() {
        if (workout != null) {

            if (workoutIntervals.size() > 0) {

                if (currentInterval == null) {
                    durations.intervalStart = 0;
                    setCurrentInterval(workoutIntervals.get(0));
                }

                if (durations.workoutDuration > durations.intervalStart + currentInterval.duration) {
                    int intervalIndex = workoutIntervals.indexOf(currentInterval);
                    if (intervalIndex < workoutIntervals.size() - 1) {
                        durations.intervalStart += currentInterval.duration;
                        setCurrentInterval(workoutIntervals.get(intervalIndex + 1));
                    }
                }

                double percentOfDuration = Math.min(1, durations.intervalDuration / currentInterval.duration);

                InMemoryProfile inMemoryProfile = new InMemoryProfile(profile);

                double pStartMin = currentInterval.startPowerMin(inMemoryProfile);
                double pStartMax = currentInterval.startPowerMax(inMemoryProfile);
                double pEndMin = currentInterval.endPowerMin(inMemoryProfile);
                double pEndMax = currentInterval.endPowerMax(inMemoryProfile);
                double pMin = pStartMin + (pEndMin - pStartMin) * percentOfDuration;
                double pMax = pStartMax + (pEndMax - pStartMax) * percentOfDuration;

                int cStart = currentInterval.startCadence;
                int cEnd = currentInterval.endCadence;
                intervalTarget.cadence = cStart + (cEnd - cStart) * percentOfDuration;
                intervalTarget.powerMin = pMin;
                intervalTarget.powerMax = pMax;
                if (workout != null && !workout.isFTPTest()) {
                    mSensorDataService.getTrainerMode().setTargetWatts((int) Math.round(intervalTarget.targetPower()));
                }
            }

            if (durations.workoutDuration >= workout.getDuration()) {
                setSessionState(SessionState.Complete);
            }
        }
    }

    private void updateWorkoutIntervalText() {
        if (textAndTimesArray != null) {
            if (textAndTimesArray.size() > 0) {

                WorkoutTextAndTime tat = textAndTimesArray.get(textAndTimesArray.size() - 1);
                if (durations.workoutDuration >= tat.timeEnd) {
                    setWorkoutTextAndTime(null);

                    textAndTimesArray.remove(textAndTimesArray.size() - 1);

                } else if (durations.workoutDuration >= tat.timeStart && currentWorkoutText == null) {
                    setWorkoutTextAndTime(tat);
                }
            }
        }
    }

    public void setWorkoutTextAndTime(WorkoutTextAndTime tat) {
        currentWorkoutText = tat;
        ArrayList<SessionControllerObserver> obs = new ArrayList(mObservers);
        for (SessionControllerObserver observer : obs) {
            observer.newWorkoutTextAndTime(tat);
        }
    }

    private long idleSpeedTime = -1;   // -1 means no speed sensor connected

    @Receiver(actions = SensorDataService.SENSOR_DATA, registerAt = Receiver.RegisterAt.OnCreateOnDestroy)
    protected void onSensorData(Intent intent) {
        Log.d(TAG, "onSensorData");

        sensorValues.currentHeartRate = intent.getIntExtra(SensorDataService.SENSOR_DATA_HEART_RATE, -1);
        sensorValues.currentPower = intent.getIntExtra(SensorDataService.SENSOR_DATA_POWER, -1);
        sensorValues.currentSpeedKPH = intent.getDoubleExtra(SensorDataService.SENSOR_DATA_SPEED, -1);
        sensorValues.currentCadence = intent.getDoubleExtra(SensorDataService.SENSOR_DATA_CADENCE, -1);

        long now = System.currentTimeMillis();
        // Auto-Resume Warmup if paused and speed is detected
        if (lastSensorDataTime > 0 && (sessionState == SessionState.WarmupPaused || sessionState == SessionState.Warmup)) {
            long timeDelta = now - lastSensorDataTime;

            if (sensorValues.currentSpeedKPH < 0) {
                idleSpeedTime = -1;
            } else if (sensorValues.currentSpeedKPH == 0) {
                idleSpeedTime += timeDelta;
            } else {
                idleSpeedTime = 0;
                if (sessionState == SessionState.WarmupPaused) {
                    setSessionState(SessionState.Warmup);
                }
            }
        }

        lastSensorDataTime = now;
    }

    private void calculateAveragesForTick() {
        double lapCount = mSession.laps.size();
        double avgLapTime = 0.0;
        for (SessionLap lap : mSession.laps) {
            avgLapTime += lap.getDuration() / lapCount;
        }
        final double avlaptime = avgLapTime;
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                mSession.setAvgLapTime(avlaptime);
            }
        });
    }


    private double difficultyModifier;
    private SharedPreferences sharedPreferences;
    boolean autoLap;
    private Realm realm;

    @Override
    public void onCreate() {
        super.onCreate();
        profile = Profile.current();
        sharedPreferences = getSharedPreferences(SettingsActivity.getSettingsNamespace(), MODE_PRIVATE);
        zoneMonitor = new FITZoneMonitor(getApplication().getApplicationContext());
        autoLap = SettingsActivity.AutoLapEnabled && sharedPreferences.getBoolean(SettingsActivity.AUTO_LAP_INDICATORS + Profile.getUUID(), false);
        bindService(DataSync_.intent(this).get(), mDataSyncConnection, Context.BIND_AUTO_CREATE);
        bindService(SensorDataService_.intent(this).get(), mSensorDataServiceConnection, Context.BIND_AUTO_CREATE);
        realm = Realm.getDefaultInstance();
    }

    @Override
    public boolean stopService(Intent name) {
        return super.stopService(name);
    }

    @Override
    public void onDestroy() {
        if (mSession != null) {
            if (sessionComplete()) {
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        mDataSync.saveOrUpdateSession(mSession);
                    }
                });
            } else {
//                mDataSync.deleteSession(mSession);
            }
        }

        unbindService(mDataSyncConnection);
        unbindService(mSensorDataServiceConnection);
        Log.d(TAG, "Destroying SessionController");
        realm.close();
        super.onDestroy();
    }

    @Override
    public void autoLap() {
        if (mSensorDataService != null && sensorValues.currentPower >= 0 && sessionState == SessionState.Workout) {
            markLap();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
}
