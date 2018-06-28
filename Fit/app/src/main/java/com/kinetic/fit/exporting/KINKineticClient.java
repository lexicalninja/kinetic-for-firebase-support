package com.kinetic.fit.exporting;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.FileProvider;

import com.garmin.fit.Activity;
import com.garmin.fit.ActivityMesg;
import com.garmin.fit.DateTime;
import com.garmin.fit.DeviceInfoMesg;
import com.garmin.fit.Event;
import com.garmin.fit.EventType;
import com.garmin.fit.FileCreatorMesg;
import com.garmin.fit.FileEncoder;
import com.garmin.fit.FileIdMesg;
import com.garmin.fit.FitRuntimeException;
import com.garmin.fit.LapMesg;
import com.garmin.fit.RecordMesg;
import com.garmin.fit.SessionMesg;
import com.garmin.fit.Sport;
import com.garmin.fit.SubSport;
import com.kinetic.fit.BuildConfig;
import com.kinetic.fit.data.realm_objects.Profile;
import com.kinetic.fit.data.realm_objects.Session;
import com.kinetic.fit.data.session_objects.SessionDataSlice;
import com.kinetic.fit.data.session_objects.SessionLap;
import com.kinetic.fit.data.shared_prefs.SharedPreferencesInterface;
import com.kinetic.fit.util.Conversions;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;

public class KINKineticClient {

    private static final String TAG = "KineticClient";

    public static Uri encodeSessionCSV(Context context, String uuid) throws IOException {
        String outputString = new String();
        DecimalFormat df = new DecimalFormat("0.00");
        Realm realm = Realm.getDefaultInstance();
        final Session session = realm.where(Session.class).equalTo("uuid", uuid).findFirst();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                session.rebuild();
            }
        });
        DateTime workoutTime = new DateTime(session.getWorkoutDate());

        String fileName = session.getExportFileName();
        java.io.File outputDir = context.getExternalFilesDir(null);
        java.io.File outputFile = java.io.File.createTempFile(fileName, ".csv", outputDir);

        outputString += "Name, " + Profile.getCurrentName() + "\n";
        outputString += "Device, " + android.os.Build.MODEL + "\n";
        outputString += "Date, " + workoutTime + "\n";
        outputString += "Workout Name, " + session.getWorkoutName() + "\n";
        outputString += "Workout Duration (sec), " + Math.round(session.getWorkoutDuration()) + "\n";
        outputString += "Warm up Duration (sec), " + Math.round(session.getWarmupDuration()) + "\n";
        if (SharedPreferencesInterface.isMetric()) {
            outputString += "Total Distance (KM), " + df.format(session.getDistanceKM()) + "\n";
        } else {
            outputString += "Total Distance (Miles), " + df.format(Conversions.kph_to_mph(session.getDistanceKM())) + "\n";
        }
        outputString += "Total Calories Burned (kcal), " + Math.round(session.getCaloriesBurned()) + "\n";
        outputString += "Power Average, Max Power \n";
        outputString += Math.round(session.getAvgPower()) + ", " + session.getMaxPower() + "\n";
        outputString += "Heart Rate Average, Max Heart Rate \n";
        outputString += Math.round(session.getAvgHeartRate()) + ", " + session.getMaxHeartRate() + "\n";
        outputString += "Cadence Average, Max Cadence \n";
        outputString += Math.round(session.getAvgCadence()) + ", " + session.getMaxCadence() + "\n";
        if (SharedPreferencesInterface.isMetric()) {
            outputString += "Average Speed (KPH), Max Speed (KPH) \n";
            outputString += df.format(session.getAvgSpeedKPH()) + ", " + df.format(session.getMaxSpeedKPH()) + "\n";
        } else {
            outputString += "Average Speed (MPH), Max Speed (MPH) \n";
            outputString += df.format(Conversions.kph_to_mph(session.getAvgSpeedKPH())) + ", " + df.format(Conversions.kph_to_mph(session.getMaxSpeedKPH())) + "\n";
        }
        outputString += "Time, Power, Heart Rate, Speed (MPH), Cadence, Accumulated Distance, Accumulated Calories Burned \n";
        if (SharedPreferencesInterface.isMetric()) {
            for (SessionDataSlice s : session.getDataSlices()) {
                outputString += s.timestamp + ", " + s.currentPower + ", " + s.currentHeartRate + ", " + df.format(s.currentSpeedKPH) + ", " + s.currentCadence + ", " + df.format(s.accumulatedDistanceKM) + ", " + Math.round(Conversions.kj_to_kcal(s.accumulatedKilojoules)) + "\n";
            }
        } else {
            for (SessionDataSlice s : session.getDataSlices()) {
                outputString += s.timestamp + ", " + s.currentPower + ", " + s.currentHeartRate + ", " + df.format(Conversions.kph_to_mph(s.currentSpeedKPH)) + ", " + s.currentCadence + ", " + df.format(Conversions.kph_to_mph(s.accumulatedDistanceKM)) + ", " + Math.round(Conversions.kj_to_kcal(s.accumulatedKilojoules)) + "\n";
            }
        }
        //write outputString to file
        OutputStream outputStream = new FileOutputStream(outputFile);
        Writer outputStreamWriter = new OutputStreamWriter(outputStream);
        outputStreamWriter.write(outputString);
        outputStreamWriter.close();

        //outputFile
        return FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName().concat(".ui.analysis.AnalysisActivity"), outputFile);
    }

    public static Uri encodeSession(Context context, String uuid, boolean needsFileProviderUri) throws IOException, FitRuntimeException {
        Realm realm = Realm.getDefaultInstance();
        final Session session = realm.where(Session.class).equalTo("uuid", uuid).findFirst();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                session.rebuild();
            }
        });
        DateTime workoutTime = new DateTime(session.getWorkoutDate());
        DateTime nowTime = new DateTime(new Date());

        String fileName = session.getExportFileName();

        java.io.File outputDir = context.getExternalFilesDir(null);
        java.io.File outputFile = java.io.File.createTempFile(fileName, ".fit", outputDir);
        FileEncoder encode = new FileEncoder(outputFile);

//        String build = "unknown";
        int buildNumber = BuildConfig.VERSION_CODE;
        int productNumber = 0;
        short hardwareVersion = 1;
//        int manufacturer = Manufacturer.INVALID;
        long serialNumber = 0;
        int localNum = 0;

        // FILE ID MESSAGE
        FileIdMesg fileIdMesg = new FileIdMesg(); // Every FIT file MUST contain a 'File ID' message as the first message
        fileIdMesg.setLocalNum(localNum++);
        fileIdMesg.setSerialNumber(serialNumber);   // Impossible to get the serial #
        fileIdMesg.setTimeCreated(nowTime);
        //fileIdMesg.setManufacturer(manufacturer);
        fileIdMesg.setProduct(productNumber);        // No official product #
        fileIdMesg.setType(com.garmin.fit.File.ACTIVITY);
        encode.write(fileIdMesg);

        // FILE CREATOR MESSAGE
        FileCreatorMesg fileCreatorMesg = new FileCreatorMesg();
        fileCreatorMesg.setLocalNum(localNum++);
        fileCreatorMesg.setSoftwareVersion(buildNumber);
        fileCreatorMesg.setHardwareVersion(hardwareVersion);
        encode.write(fileCreatorMesg);

        // DEVICE INFO MESSAGE
        DeviceInfoMesg deviceInfoMesg = new DeviceInfoMesg();
        deviceInfoMesg.setLocalNum(localNum++);
        deviceInfoMesg.setTimestamp(workoutTime);
        deviceInfoMesg.setDeviceIndex((short) 0);
        deviceInfoMesg.setDeviceType((short) 0);
        //deviceInfoMesg.setManufacturer(manufacturer);
        deviceInfoMesg.setSerialNumber(serialNumber);
        deviceInfoMesg.setProduct(productNumber);
        deviceInfoMesg.setSoftwareVersion((float) buildNumber);
        deviceInfoMesg.setHardwareVersion(hardwareVersion);
        encode.write(deviceInfoMesg);

        int lapCount = session.laps.size();
        int lapLocalNum = localNum;
        localNum++;
        int dataLocalNum = localNum;
        localNum++;

        List<SessionLap> laps = new ArrayList<>(session.laps);

        // Write Records :: Data slices!
        if (session.getDataSlices() != null) {
            List<SessionDataSlice> dataSlices = new ArrayList<>(session.getDataSlices());
            for (SessionDataSlice slice : dataSlices) {
                while (laps.size() > 0 && laps.get(0).getStartTime() + laps.get(0).getDuration() < slice.timestamp) {
                    encodeLap(laps.get(0), lapLocalNum, workoutTime, encode, session);
                    laps.remove(0);
                }
                encodeDataSlice(slice, dataLocalNum, workoutTime, encode, session);
            }
        }

        // Write Remaining Laps
        for (SessionLap lap : laps) {
            encodeLap(lap, lapLocalNum, workoutTime, encode, session);
        }

        SessionMesg sessionMsg = new SessionMesg();

        // Write Session :: Summary Data
        sessionMsg.setLocalNum(localNum++);
        //////////////////////////////
        sessionMsg.setTimestamp(new DateTime(workoutTime).add(session.getWorkoutDuration()));
        sessionMsg.setStartTime(workoutTime);
        sessionMsg.setTotalElapsedTime((float) (session.getWorkoutDuration() + session.getWarmupDuration()));
        sessionMsg.setTotalTimerTime((float) (session.getWorkoutDuration() + session.getWarmupDuration()));
        sessionMsg.setSport(Sport.CYCLING);
        sessionMsg.setEvent(Event.SESSION);
        sessionMsg.setEventType(EventType.STOP);
        //////////////////////////////

        sessionMsg.setAvgCadence((short) session.getAvgCadence());
        sessionMsg.setAvgHeartRate((short) session.getAvgHeartRate());
        sessionMsg.setAvgLapTime((float) session.getAvgLapTime());
        sessionMsg.setAvgPower((int) session.getAvgPower());

        sessionMsg.setAvgSpeed((float) Conversions.kph_to_mps(session.getAvgSpeedKPH()));        // meters / second
        sessionMsg.setIntensityFactor((float) session.getIntensityFactor());
        sessionMsg.setMaxCadence((short) session.getMaxCadence());
        sessionMsg.setMaxHeartRate((short) session.getMaxHeartRate());
        sessionMsg.setMaxPower(session.getMaxPower());
        sessionMsg.setMaxSpeed((float) Conversions.kph_to_mps(session.getMaxSpeedKPH()));
        sessionMsg.setMinHeartRate((short) session.getMinHeartRate());
        sessionMsg.setNormalizedPower((int) session.getNormalizedPower());
        sessionMsg.setNumLaps(lapCount);

        //        if (session.latitude != 0 || session.longitude != 0) {
        //            sessionMsg.setStartPositionLat(DEGREES_TO_SEMICIRCLES(session.latitude));
        //            sessionMsg.setStartPositionLong(DEGREES_TO_SEMICIRCLES(session.longitude));
        //        }

        // zero based index?
        for (int i = 0; i < session.getTimeInHeartRateZones().size(); i++) {
            sessionMsg.setTimeInHrZone(i, session.getTimeInHeartRateZones().get(i).floatValue());
        }
        for (int i = 0; i < session.getTimeInPowerZones().size(); i++) {
            sessionMsg.setTimeInPowerZone(i, session.getTimeInPowerZones().get(i).floatValue());
        }
        sessionMsg.setTotalWork((long) session.getKilojoules() * 1000); // joules
        sessionMsg.setTotalCalories((int) session.getCaloriesBurned()); // ? sessionMsg.setTotalFatCalories();
        sessionMsg.setTotalDistance((float) session.getDistanceKM() * 1000);  // meters
        sessionMsg.setTotalAscent(0);
        sessionMsg.setTrainingStressScore((float) session.getTrainingStressScore());
        // this is probably helpful
        // sessionMsg.setZoneCount(__, 5); ???
        // sessionMsg.setTotalCycles(0)

        encode.write(sessionMsg);

        // Write Activity Info
        ActivityMesg activityMsg = new ActivityMesg();
        activityMsg.setLocalNum(localNum++);
        activityMsg.setTimestamp(new DateTime(workoutTime).add(session.getWorkoutDuration()));
        activityMsg.setNumSessions(1);
        activityMsg.setType(Activity.AUTO_MULTI_SPORT);
        activityMsg.setEvent(Event.ACTIVITY);
        activityMsg.setEventType(EventType.STOP);
        encode.write(activityMsg);
        encode.close();
        realm.close();

        if(needsFileProviderUri) {
            return FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName().concat(".ui.analysis.AnalysisActivity"), outputFile);
        } else {
            return Uri.parse("file://"+ outputFile);
        }
    }

    private static void encodeDataSlice(SessionDataSlice dataSlice, int localNum, DateTime refTime, FileEncoder encoder, Session session) {
        RecordMesg recordMsg = new RecordMesg();
        recordMsg.setLocalNum(localNum);
        recordMsg.setTimestamp(new DateTime(refTime).add(dataSlice.timestamp));

        recordMsg.setCadence((short) dataSlice.currentCadence);
        recordMsg.setHeartRate((short) dataSlice.currentHeartRate);
        recordMsg.setPower(dataSlice.currentPower);
        recordMsg.setSpeed((float) Conversions.kph_to_mps(dataSlice.currentSpeedKPH));
        recordMsg.setDistance((float) (dataSlice.accumulatedDistanceKM * 1000)); // meters? accumulated?

        // NOTE: This is causing problems w/ Strava parsing. So don't do it. ... or test Strava HR Zone distribution before re-enabling it.
        //    if (session.latitude != 0 || session.longitude != 0) {
        //        recordMsg.setPositionLat(DEGREES_TO_SEMICIRCLES(session.latitude));
        //        recordMsg.setPositionLong(DEGREES_TO_SEMICIRCLES(session.longitude));
        //    }

        encoder.write(recordMsg);
    }

    private static void encodeLap(SessionLap lapData, int localNum, DateTime refTime, FileEncoder encoder, Session session) {
        LapMesg lapMsg = new LapMesg();
        lapMsg.setLocalNum(localNum);
        //////////////////////////////
        lapMsg.setTimestamp(new DateTime(refTime).add(lapData.getStartTime()).add(lapData.getDuration()));
        lapMsg.setStartTime(new DateTime(refTime).add(lapData.getStartTime()));
        lapMsg.setEvent(Event.LAP);
        lapMsg.setEventType(EventType.START);
        lapMsg.setSport(Sport.FITNESS_EQUIPMENT);
        lapMsg.setSubSport(SubSport.INDOOR_CYCLING);
        //////////////////////////////
        lapMsg.setAvgCadence((short) lapData.getAvgCadence());
        lapMsg.setAvgHeartRate((short) lapData.getAvgHeartRate());
        lapMsg.setAvgPower((int) lapData.getAvgPower());
        lapMsg.setAvgSpeed((float) Conversions.kph_to_mps(lapData.getAvgSpeedKPH()));        // meters / second
        lapMsg.setMaxCadence((short) lapData.getMaxCadence());
        lapMsg.setMaxHeartRate((short) lapData.getMaxHeartRate());
        lapMsg.setMaxPower(lapData.getMaxPower());
        lapMsg.setMaxSpeed((float) Conversions.kph_to_mps(lapData.getMaxSpeedKPH()));
        lapMsg.setMinHeartRate((short) lapData.getMinHeartRate());
        lapMsg.setNormalizedPower((int) lapData.getNormalizedPower());
        // zero based index?
        for (int i = 0; i < lapData.getTimeInHeartRateZones().size(); i++) {
            lapMsg.setTimeInHrZone(i, lapData.getTimeInHeartRateZones().get(i).floatValue());
        }
        for (int i = 0; i < lapData.getTimeInPowerZones().size(); i++) {
            lapMsg.setTimeInPowerZone(i, lapData.getTimeInPowerZones().get(i).floatValue());
        }
        lapMsg.setTotalWork((long) lapData.getKilojoules() * 1000); // joules
        lapMsg.setTotalCalories((int) lapData.getCaloriesBurned()); // ? sessionMsg.setTotalFatCalories();
        lapMsg.setTotalDistance((float) lapData.getDistanceKM() * 1000);  // meters
        lapMsg.setTotalAscent(0);
        //    lapMsg.setMinAltitude(session.altitudeMeters);
        //    lapMsg.setMaxAltitude(session.altitudeMeters);
        // lapMsg.setIntensity()
        // lapMsg.setTotalCycles(0)
        // lapMsg.setZoneCount(__, 5); ???
        lapMsg.setTotalElapsedTime((float) lapData.getDuration());
        lapMsg.setTotalTimerTime(refTime.getTimestamp() + (float) lapData.getStartTime() + (float) lapData.getDuration());
        encoder.write(lapMsg);
    }
}
