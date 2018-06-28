package com.kinetic.fit.data.realm_objects;

import android.util.Base64;

import com.google.gson.JsonObject;
import com.kinetic.fit.data.PowerZones;
import com.kinetic.fit.data.session_objects.SessionDataSlice;
import com.kinetic.fit.data.session_objects.SessionDataSpan;
import com.kinetic.fit.data.session_objects.SessionLap;
import com.kinetic.fit.ui.root.HistoryRecyclerAdapter;
import com.kinetic.fit.util.Conversions;
import com.kinetic.fit.util.RealmUtils;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Saxton on 1/23/17.
 */

public class Session extends RealmObject implements SessionDataSpan {
    public static final String CLASS_NAME = "Session";
    public static final int OK_FLAG = 0;
    public static final int CREATE_FLAG = 1;
    public static final int UPDATE_FLAG = 2;
    public static final int DELETE_FLAG = 3;


    @PrimaryKey
    private String uuid;
    private String objectId;
    private Profile profile;
    @Ignore
    public ArrayList<SessionDataSlice> dataSlices = new ArrayList<>();
    private String dataSlicesString;
    @Ignore
    public ArrayList<SessionLap> laps = new ArrayList<>();
    private String lapsString;
    @Ignore
    private ArrayList<Double> apr4s = new ArrayList<>();
    private String apr4sString;
    private double normalizedPowerTime = 0;
    private double avgCadence;
    private double avgHeartRate;
    private double avgHeartRateMaxPercent;
    private double avgHeartRateReservePercent;
    private double avgPower;
    private double avgSpeedKPH;
    private double distanceKM;
    private double duration;
    private double intensityFactor;
    private double kilojoules;
    private double maxCadence;
    private int maxHeartRate;
    private int maxPower;
    private double maxSpeedKPH;
    @Ignore
    private ArrayList<Double> meanMaximums = new ArrayList<>();
    private String meanMaximumsString;
    @Ignore
    private ArrayList<Double> meanMaximumTimes = new ArrayList<>();
    private String meanMaximumTimesString;
    private int minHeartRate;
    private double normalizedPower;
    private double startTime;
    @Ignore
    private ArrayList<Double> timeInHeartRateZones = new ArrayList<>();
    private String timeInHeartRateZonesString;
    @Ignore
    private ArrayList<Double> timeInPowerZones = new ArrayList<>();
    private String timeInPowerZonesString;

    private double trainingStressScore;
    private double avgLapTime;
    @Ignore
    private ArrayList<Double> lapMarkers = new ArrayList<>();
    private String lapMarkersString;
    //    TODO Location as a what? Location.class? Make Coordinate class?
    private double profileWeightKG;
    private double profileHeightCM;
    private int profilePowerFTP;
    private int profileHeartResting;
    private int profileHeartMax;
    @Ignore
    private ArrayList<Integer> profileHeartZones = new ArrayList<>();
    private String profileHeartZonesString;
    @Ignore
    private ArrayList<Integer> profilePowerZones = new ArrayList<>();
    private String profilePowerZonesString;
    private double warmupDuration;
    private int calculatedFTP;
    private String workoutId;
    private Date workoutDate;
    private String workoutDescription;
    private byte[] sensorData;
    private double workoutDuration;
    private String workoutName;
    private int parseFlag;
    private int viewTypeIndex;

    public Session() {
        this.uuid = UUID.randomUUID().toString();
//        initialize(Profile.current());
    }

    public Session(int viewTypeIndex) {
        this.viewTypeIndex = viewTypeIndex;
    }

    public Session(JsonObject jsonObject) {
        if (jsonObject.get("uuid") != null) {
            this.uuid = jsonObject.get("uuid").getAsString();
        } else {
            this.uuid = UUID.randomUUID().toString();
//            TODO save this back to parse
        }
        this.objectId = jsonObject.get("objectId").getAsString();
        this.profile = Profile.current();
        if (jsonObject.get("avgCadence") != null) {
            this.avgCadence = jsonObject.get("avgCadence").getAsInt();
        }
        if (jsonObject.get("avgHeartRate") != null) {
            this.avgHeartRate = jsonObject.get("avgHeartRate").getAsDouble();
        }
        if (jsonObject.get("avgPower") != null) {
            this.avgPower = jsonObject.get("avgPower").getAsDouble();
        }
        if (jsonObject.get("avgSpeedKPH") != null) {
            this.avgSpeedKPH = jsonObject.get("avgSpeedKPH").getAsDouble();
        }
        if (jsonObject.get("distanceKM") != null) {
            this.distanceKM = jsonObject.get("distanceKM").getAsDouble();
        }
        if (jsonObject.get("duration") != null) {
            this.duration = jsonObject.get("duration").getAsDouble();
        }
        if (jsonObject.get("intensityFactor") != null) {
            this.intensityFactor = jsonObject.get("intensityFactor").getAsDouble();
        }
        if (jsonObject.get("kilojoules") != null) {
            this.kilojoules = jsonObject.get("kilojoules").getAsDouble();
        }
        if (jsonObject.get("maxCadence") != null) {
            this.maxCadence = jsonObject.get("maxCadence").getAsDouble();
        }
        if (jsonObject.get("maxHeartRate") != null) {
            this.maxHeartRate = jsonObject.get("maxHeartRate").getAsInt();
        }
        if (jsonObject.get("maxPower") != null) {
            this.maxPower = jsonObject.get("maxPower").getAsInt();
        }
        if (jsonObject.get("maxSpeedKPH") != null) {
            this.maxSpeedKPH = jsonObject.get("maxSpeedKPH").getAsDouble();
        }
        if (jsonObject.get("meanMaximums") != null) {
            setMeanMaximumsString(jsonObject.getAsJsonArray("meanMaximums").toString());
        }
        if (jsonObject.get("meanMaximumTimes") != null) {
            setMeanMaximumTimesString(jsonObject.get("meanMaximumTimes").toString());
        }
        if (jsonObject.get("minHeartRate") != null) {
            this.minHeartRate = jsonObject.get("minHeartRate").getAsInt();
        }
        if (jsonObject.get("normalizedPower") != null) {
            this.normalizedPower = jsonObject.get("normalizedPower").getAsDouble();
        }
        if (jsonObject.get("startTime") != null) {
            this.startTime = jsonObject.get("startTime").getAsDouble();
        }
        if (jsonObject.get("timeInHeartRateZones") != null) {
            setTimeInHeartRateZonesString(jsonObject.get("timeInHeartRateZones").toString());
        }
        if (jsonObject.get("timeInPowerZones") != null) {
            setTimeInPowerZonesString(jsonObject.get("timeInPowerZones").toString());
        }
        if (jsonObject.get("trainingStressScore") != null) {
            this.trainingStressScore = jsonObject.get("trainingStressScore").getAsDouble();
        }
        if (jsonObject.get("avgLapTime") != null) {
            this.avgLapTime = jsonObject.get("avgLapTime").getAsDouble();
        }
        if (jsonObject.get("lapMarkers") != null) {
            setLapMarkersString(jsonObject.get("lapMarkers").toString()); //TODO nullable, need example
        }
        if (jsonObject.get("profileWeightKG") != null) {
            this.profileWeightKG = jsonObject.get("profileWeightKG").getAsDouble();
        }
        if (jsonObject.get("profilePowerFTP") != null) {
            this.profilePowerFTP = jsonObject.get("profilePowerFTP").getAsInt();
        }
        if (jsonObject.get("profileHeartResting") != null) {
            this.profileHeartResting = jsonObject.get("profileHeartResting").getAsInt();
        }
        if (jsonObject.get("profileHeartMax") != null) {
            this.profileHeartMax = jsonObject.get("profileHeartMax").getAsInt();
        }
        if (jsonObject.get("profileHeartZones") != null) {
            setProfileHeartZonesString(jsonObject.get("profileHeartZones").toString());
        }
        if (jsonObject.get("profilePowerZones") != null) {
            setProfilePowerZonesString(jsonObject.get("profilePowerZones").toString());
        }
        if (jsonObject.get("warmupDuration") != null) {
            this.warmupDuration = jsonObject.get("warmupDuration").getAsDouble();
        }
        if (jsonObject.get("calculatedFTP") != null) {
            this.calculatedFTP = jsonObject.get("calculatedFTP").getAsInt();
        }
        if (jsonObject.get("workoutId") != null && !jsonObject.get("workoutId").isJsonNull()) {
            this.workoutId = jsonObject.get("workoutId").getAsString();
        }
        DateTimeFormatter df = ISODateTimeFormat.dateTime();
        JsonObject workoutDate = jsonObject.getAsJsonObject("workoutDate");
        this.setWorkoutDate(df.parseDateTime(workoutDate.get("iso").getAsString()).toDate());
        if (jsonObject.get("workoutDescription") != null && !jsonObject.get("workoutDescription").isJsonNull()) {
            this.workoutDescription = jsonObject.get("workoutDescription").getAsString();
        }
        if (jsonObject.get("sensorData") != null) {
            this.sensorData = Base64.decode(jsonObject.getAsJsonObject("sensorData").get("base64").toString(), Base64.DEFAULT);
        }
        if (jsonObject.get("workoutDuration") != null) {
            this.workoutDuration = jsonObject.get("workoutDuration").getAsInt();
        }
        if (jsonObject.get("workoutName") != null && !jsonObject.get("workoutName").isJsonNull()) {
            this.workoutName = jsonObject.get("workoutName").getAsString();
        }
        this.viewTypeIndex = HistoryRecyclerAdapter.SESSION_VIEW;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public static String getClassName() {
        return CLASS_NAME;
    }

    public ArrayList<SessionDataSlice> getDataSlices() {
        return dataSlices;
    }

    public void setDataSlices(ArrayList<SessionDataSlice> dataSlices) {
        this.dataSlices = dataSlices;
    }

    public String getDataSlicesString() {
        return dataSlicesString;
    }

    public void setDataSlicesString(String dataSlicesString) {
        this.dataSlicesString = dataSlicesString;
    }

    public ArrayList<SessionLap> getLaps() {
        return laps;
    }

    public void setLaps(ArrayList<SessionLap> laps) {
        this.laps = laps;
    }

    public String getLapsString() {
        return lapsString;
    }

    public void setLapsString(String lapsString) {
        this.lapsString = lapsString;
    }

    public ArrayList<Double> getApr4s() {
        if (getApr4sString() != null && apr4s.size() == 0) {
            RealmUtils.deSerializeDoubleArrayString(getApr4sString(), apr4s);
        }
        return apr4s;
    }

    public int getParseFlag() {
        return parseFlag;
    }

    public void setParseFlag(int parseFlag) {
        this.parseFlag = parseFlag;
    }

    public double getProfileHeightCM() {
        return profileHeightCM;
    }

    public void setProfileHeightCM(double profileHeightCM) {
        this.profileHeightCM = profileHeightCM;
    }

    public void setApr4s(ArrayList<Double> apr4s) {
        this.apr4s = apr4s;
        this.apr4sString = apr4s.toString();
    }

    public String getApr4sString() {
        return apr4sString;
    }

    public void setApr4sString(String apr4sString) {
        this.apr4sString = apr4sString;
        RealmUtils.deSerializeDoubleArrayString(apr4sString, this.apr4s);
    }

    public double getNormalizedPowerTime() {
        return normalizedPowerTime;
    }

    public void setNormalizedPowerTime(double normalizedPowerTime) {
        this.normalizedPowerTime = normalizedPowerTime;
    }

    public double getAvgCadence() {
        return avgCadence;
    }

    public void setAvgCadence(double avgCadence) {
        this.avgCadence = avgCadence;
    }

    public double getAvgHeartRate() {
        return avgHeartRate;
    }

    public void setAvgHeartRate(double avgHeartRate) {
        this.avgHeartRate = avgHeartRate;
    }

    public double getAvgHeartRateMaxPercent() {
        return avgHeartRateMaxPercent;
    }

    public void setAvgHeartRateMaxPercent(double avgHeartRateMaxPercent) {
        this.avgHeartRateMaxPercent = avgHeartRateMaxPercent;
    }

    public double getAvgHeartRateReservePercent() {
        return avgHeartRateReservePercent;
    }

    public void setAvgHeartRateReservePercent(double avgHeartRateReservePercent) {
        this.avgHeartRateReservePercent = avgHeartRateReservePercent;
    }

    public double getAvgPower() {
        return avgPower;
    }

    public void setAvgPower(double avgPower) {
        this.avgPower = avgPower;
    }

    public double getAvgSpeedKPH() {
        return avgSpeedKPH;
    }

    public void setAvgSpeedKPH(double avgSpeedKPH) {
        this.avgSpeedKPH = avgSpeedKPH;
    }

    public double getDistanceKM() {
        return distanceKM;
    }

    public void setDistanceKM(double distanceKM) {
        this.distanceKM = distanceKM;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public double getIntensityFactor() {
        return intensityFactor;
    }

    public void setIntensityFactor(double intensityFactor) {
        this.intensityFactor = intensityFactor;
    }

    public double getKilojoules() {
        return kilojoules;
    }

    public void setKilojoules(double kilojoules) {
        this.kilojoules = kilojoules;
    }

    public double getMaxCadence() {
        return maxCadence;
    }

    public void setMaxCadence(double maxCadence) {
        this.maxCadence = maxCadence;
    }

    public int getMaxHeartRate() {
        return maxHeartRate;
    }

    public void setMaxHeartRate(int maxHeartRate) {
        this.maxHeartRate = maxHeartRate;
    }

    public int getMaxPower() {
        return maxPower;
    }

    public void setMaxPower(int maxPower) {
        this.maxPower = maxPower;
    }

    public double getMaxSpeedKPH() {
        return maxSpeedKPH;
    }

    public void setMaxSpeedKPH(double maxSpeedKPH) {
        this.maxSpeedKPH = maxSpeedKPH;
    }

    public ArrayList<Double> getMeanMaximums() {
        return RealmUtils.getStringAsDoubleArray(getMeanMaximumsString());
    }

    public void setMeanMaximums(ArrayList<Double> meanMaximums) {
        this.meanMaximums = meanMaximums;
        meanMaximumsString = meanMaximums.toString();
    }

    public String getMeanMaximumsString() {
        return meanMaximumsString;
    }

    public void setMeanMaximumsString(String meanMaximumsString) {
        this.meanMaximumsString = meanMaximumsString;
        RealmUtils.deSerializeDoubleArrayString(meanMaximumsString, this.meanMaximums);
    }

    public ArrayList<Double> getMeanMaximumTimes() {
        return RealmUtils.getStringAsDoubleArray(getMeanMaximumTimesString());
    }

    public void setMeanMaximumTimes(ArrayList<Double> meanMaximumTimes) {
        this.meanMaximumTimes = meanMaximumTimes;
        this.meanMaximumTimesString = meanMaximumTimes.toString();
    }

    public String getMeanMaximumTimesString() {
        return meanMaximumTimesString;
    }

    public void setMeanMaximumTimesString(String meanMaximumTimesString) {
        this.meanMaximumTimesString = meanMaximumTimesString;
        RealmUtils.deSerializeDoubleArrayString(meanMaximumTimesString, this.meanMaximumTimes);
    }

    public int getMinHeartRate() {
        return minHeartRate;
    }

    public void setMinHeartRate(int minHeartRate) {
        this.minHeartRate = minHeartRate;
    }

    public double getNormalizedPower() {
        return normalizedPower;
    }

    public void setNormalizedPower(double normalizedPower) {
        this.normalizedPower = normalizedPower;
    }

    public double getStartTime() {
        return startTime;
    }

    public void setStartTime(double startTime) {
        this.startTime = startTime;
    }

    public ArrayList<Double> getTimeInHeartRateZones() {
        return RealmUtils.getStringAsDoubleArray(getTimeInHeartRateZonesString());
    }

    public void setTimeInHeartRateZones(ArrayList<Double> timeInHeartRateZones) {
        this.timeInHeartRateZones = timeInHeartRateZones;
        this.timeInHeartRateZonesString = timeInHeartRateZones.toString();
    }

    public String getTimeInHeartRateZonesString() {
        return timeInHeartRateZonesString;
    }

    public void setTimeInHeartRateZonesString(String timeInHeartRateZonesString) {
        this.timeInHeartRateZonesString = timeInHeartRateZonesString;
        RealmUtils.deSerializeDoubleArrayString(timeInHeartRateZonesString, this.timeInHeartRateZones);
    }

    public ArrayList<Double> getTimeInPowerZones() {
        return RealmUtils.getStringAsDoubleArray(getTimeInPowerZonesString());
    }

    public void setTimeInPowerZones(ArrayList<Double> timeInPowerZones) {
        this.timeInPowerZones = timeInPowerZones;
        this.timeInPowerZonesString = timeInPowerZones.toString();
    }

    public String getTimeInPowerZonesString() {
        return timeInPowerZonesString;
    }

    public void setTimeInPowerZonesString(String timeInPowerZonesString) {
        this.timeInPowerZonesString = timeInPowerZonesString;
        RealmUtils.deSerializeDoubleArrayString(timeInPowerZonesString, this.timeInPowerZones);
    }

    public double getTrainingStressScore() {
        return trainingStressScore;
    }

    public void setTrainingStressScore(double trainingStressScore) {
        this.trainingStressScore = trainingStressScore;
    }

    public double getAvgLapTime() {
        return avgLapTime;
    }

    public void setAvgLapTime(double avgLapTime) {
        this.avgLapTime = avgLapTime;
    }

    public ArrayList<Double> getLapMarkers() {
        if (getLapMarkersString() != null && lapMarkers.size() == 0) {
            RealmUtils.deSerializeDoubleArrayString(getLapMarkersString(), lapMarkers);
        }
        return lapMarkers;
    }

    public void addLapMarker(double sessionTime) {
        ArrayList<Double> lm = getLapMarkers();
        lm.add(sessionTime);
        setLapMarkers(lm);
    }

    public void setLapMarkers(ArrayList<Double> lapMarkers) {
        this.lapMarkers = lapMarkers;
        this.lapMarkersString = lapMarkers.toString();
    }

    public String getLapMarkersString() {
        return lapMarkersString;
    }

    public void setLapMarkersString(String lapMarkersString) {
        this.lapMarkersString = lapMarkersString;
        RealmUtils.deSerializeDoubleArrayString(lapMarkersString, this.lapMarkers);
    }

    public double getProfileWeightKG() {
        return profileWeightKG;
    }

    public void setProfileWeightKG(double profileWeightKG) {
        this.profileWeightKG = profileWeightKG;
    }

    public int getProfilePowerFTP() {
        return profilePowerFTP;
    }

    public void setProfilePowerFTP(int profilePowerFTP) {
        this.profilePowerFTP = profilePowerFTP;
    }

    public int getProfileHeartResting() {
        return profileHeartResting;
    }

    public void setProfileHeartResting(int profileHeartResting) {
        this.profileHeartResting = profileHeartResting;
    }

    public int getProfileHeartMax() {
        return profileHeartMax;
    }

    public void setProfileHeartMax(int profileHeartMax) {
        this.profileHeartMax = profileHeartMax;
    }

    public ArrayList<Integer> getProfileHeartZones() {
        return RealmUtils.getStringAsIntegerArray(getProfileHeartZonesString());
    }

    public void setProfileHeartZones(ArrayList<Integer> profileHeartZones) {
        this.profileHeartZones = profileHeartZones;
        this.profileHeartZonesString = profileHeartZones.toString();
    }

    public String getProfileHeartZonesString() {
        return profileHeartZonesString;
    }

    public void setProfileHeartZonesString(String profileHeartZonesString) {
        this.profileHeartZonesString = profileHeartZonesString;
        RealmUtils.deSerializeIntegerArrayString(profileHeartZonesString, this.profileHeartZones);
    }

    public ArrayList<Integer> getProfilePowerZones() {
        return RealmUtils.getStringAsIntegerArray(getProfilePowerZonesString());
    }

    public void setProfilePowerZones(ArrayList<Integer> profilePowerZones) {
        this.profilePowerZones = profilePowerZones;
        this.profilePowerZonesString = profilePowerZones.toString();
    }

    public String getProfilePowerZonesString() {
        return profilePowerZonesString;
    }

    public void setProfilePowerZonesString(String profilePowerZonesString) {
        this.profilePowerZonesString = profilePowerZonesString;
//        RealmUtils.deSerializeIntegerArrayString(profilePowerZonesString, this.profilePowerZones);
    }

    public double getWarmupDuration() {
        return warmupDuration;
    }

    public void setWarmupDuration(double warmupDuration) {
        this.warmupDuration = warmupDuration;
    }

    public int getCalculatedFTP() {
        return calculatedFTP;
    }

    public void setCalculatedFTP(int calculatedFTP) {
        this.calculatedFTP = calculatedFTP;
    }

    public String getWorkoutId() {
        return workoutId;
    }

    public void setWorkoutId(String workoutId) {
        this.workoutId = workoutId;
    }

    public Date getWorkoutDate() {
        return workoutDate;
    }

    public void setWorkoutDate(Date workoutDate) {
        this.workoutDate = workoutDate;
    }

    public String getWorkoutDescription() {
        return workoutDescription;
    }

    public void setWorkoutDescription(String workoutDescription) {
        this.workoutDescription = workoutDescription;
    }

    public byte[] getSensorData() {
        return sensorData;
    }

    public void setSensorData(byte[] sensorData) {
        this.sensorData = sensorData;
    }

    public double getWorkoutDuration() {
        return workoutDuration;
    }

    public void setWorkoutDuration(double workoutDuration) {
        this.workoutDuration = workoutDuration;
    }

    public String getWorkoutName() {
        return workoutName;
    }

    public void setWorkoutName(String workoutName) {
        this.workoutName = workoutName;
    }

    public double getCaloriesBurned() {
        return Conversions.kj_to_kcal(getKilojoules());
    }

    public double getEndTime() {
        return getStartTime() + getDuration();
    }

    public int getViewTypeIndex() {
        return viewTypeIndex;
    }

    public void addTimeInHeartRateZone(int zone, double duration) {
        setTimeInHeartRateZones(addTimeToArray(getTimeInHeartRateZones(), zone, duration));
    }

    public void addTimeInPowerZone(int zone, double duration) {
        setTimeInPowerZones(addTimeToArray(getTimeInPowerZones(), zone, duration));
    }

    private ArrayList<Double> addTimeToArray(ArrayList<Double> array, int zone, double duration) {
        ArrayList<Double> cache = array;
        double newDuration = cache.get(zone) + duration;
        cache.set(zone, newDuration);
        return cache;
    }

    public void initialize(Profile profile) {

        setProfile(Profile.current());
        setProfileWeightKG(Profile.getProfileWeightKG());
        setProfileHeartMax(Profile.getProfileHeartMax());
        setProfilePowerFTP(Profile.getProfileFTP());
        setProfileHeartResting(Profile.getProfileHeartMax());

        setProfileHeartZones(Profile.getProfileHRZones());
        setProfilePowerZones(Profile.getProfilePowerZones());

        dataSlices.clear();
        laps.clear();

        setWorkoutDate(new Date());

        clear(this);
    }

    public static void clear(SessionDataSpan span) {
        span.setAvgCadence(-1);
        span.setAvgHeartRate(-1);
        span.setAvgHeartRateMaxPercent(-1);
        span.setAvgHeartRateReservePercent(-1);
        span.setAvgPower(-1);
        span.setAvgSpeedKPH(-1);

        span.setDistanceKM(0);
//        span.setDuration(0);
        span.setIntensityFactor(-1);
        span.setKilojoules(0);
        span.setMaxCadence(-1);
        span.setMaxHeartRate(-1);
        span.setMaxPower(-1);
        span.setMaxSpeedKPH(-1);

        ArrayList<Double> meanMaximums = new ArrayList<>();
        fillArray(meanMaximums, 0.0, PowerZones.MeanMaxTimes.size());
        span.setMeanMaximums(meanMaximums);

        span.setMeanMaximumTimes(PowerZones.MeanMaxTimes);

        span.setMinHeartRate(-1);
        span.setNormalizedPower(-1);
        span.setStartTime(0);

        ArrayList<Double> hrTimes = new ArrayList<>();
        fillArray(hrTimes, 0.0, Profile.HeartZoneDefaultCeilings.size());
        span.setTimeInHeartRateZones(hrTimes);

        ArrayList<Double> pzTimes = new ArrayList<>();
        fillArray(pzTimes, 0.0, Profile.PowerZoneDefaultCeilings.size());
        span.setTimeInPowerZones(pzTimes);

        span.setTrainingStressScore(-1);

        span.getApr4s().clear();
        span.setNormalizedPowerTime(0);
    }

    public void addDataSlice(SessionDataSlice slice) {
        dataSlices.add(slice);
        populateProfileValues(slice);

        addDataSlice(slice, this, dataSlices, getProfilePowerFTP());
        slice.accumulatedDistanceKM = getDistanceKM();
        slice.accumulatedKilojoules = getKilojoules();
    }

    static public void addDataSlice(SessionDataSlice slice, SessionDataSpan span, ArrayList<SessionDataSlice> slices, int profileFTP) {
        //iOS flag, nothing in Android yet
//        if(span.getDuration() == 0){
//            slice.lapMarker = true;
//        }
        span.setKilojoules(span.getKilojoules() + slice.kilojoules());
        span.setDistanceKM(span.getDistanceKM() + slice.distanceKM());

        double oldDuration = slice.timestamp - span.getStartTime();
        double newDuration = oldDuration + slice.duration;

        if (newDuration > 0) {
            if (slice.currentCadence > -1) {
                if (span.getAvgCadence() < 0) {
                    span.setAvgCadence(0);
                }
                span.setAvgCadence(((span.getAvgCadence() * oldDuration) + (slice.currentCadence * slice.duration)) / newDuration);
                span.setMaxCadence(Math.max(span.getMaxCadence(), slice.currentCadence));
            }
            if (slice.currentHeartRate > -1) {
                if (span.getAvgHeartRate() < 0) {
                    span.setAvgHeartRate(0);
                    span.setAvgHeartRateMaxPercent(0);
                    span.setAvgHeartRateReservePercent(0);
                }
                span.setAvgHeartRate(((span.getAvgHeartRate() * oldDuration) + (slice.currentHeartRate * slice.duration)) / newDuration);
                span.setAvgHeartRateMaxPercent(((span.getAvgHeartRateMaxPercent() * oldDuration) + (slice.currentHeartRateMaxPercent * slice.duration)) / newDuration);
                span.setAvgHeartRateReservePercent(((span.getAvgHeartRateReservePercent() * oldDuration) + (slice.currentHeartRateReservePercent * slice.duration)) / newDuration);

                if (span.getMinHeartRate() == -1) {
                    span.setMinHeartRate(slice.currentHeartRate);
                }
                span.setMinHeartRate(Math.min(span.getMinHeartRate(), slice.currentHeartRate));
                span.setMaxHeartRate(Math.max(span.getMaxHeartRate(), slice.currentHeartRate));
            }
            if (slice.currentPower > -1) {
                if (span.getAvgPower() < 0) {
                    span.setAvgPower(0);
                }
                span.setAvgPower(((span.getAvgPower() * oldDuration) + (slice.currentPower * slice.duration)) / newDuration);
                span.setMaxPower(Math.max(span.getMaxPower(), slice.currentPower));
            }
            if (slice.currentSpeedKPH > -1) {
                if (span.getAvgSpeedKPH() < 0) {
                    span.setAvgSpeedKPH(0);
                }
                span.setAvgSpeedKPH(((span.getAvgSpeedKPH() * oldDuration) + (slice.currentSpeedKPH * slice.duration)) / newDuration);
                span.setMaxSpeedKPH(Math.max(span.getMaxSpeedKPH(), slice.currentSpeedKPH));
            }
        }

        span.setDuration(newDuration);

        if (slice.currentPowerZone > 0) {
            span.addTimeInPowerZone(slice.currentPowerZone - 1, slice.duration);
        }
        if (slice.currentHeartRateZone > 0) {
            span.addTimeInHeartRateZone(slice.currentHeartRateZone - 1, slice.duration);
        }


        ArrayList<Double> meanMaxTimes = span.getMeanMaximumTimes();
        ArrayList<Double> meanMaximums = span.getMeanMaximums();
        for (int i = 0; i < meanMaxTimes.size(); i++) {
            double time = meanMaxTimes.get(i);
            if (span.getDuration() >= time) {
                double meanMax = powerAverageForPreviousTime(time, span.getDuration(), slices);
                meanMaximums.set(i, Math.max(meanMaximums.get(i), meanMax));
            }
        }
        span.setMeanMaximums(meanMaximums);


        DataSliceSet s30s = dataSlicesForTime(slice.timestamp - 31, slice.timestamp, slices);
        if (s30s.interval > 30) {
            double rolling = 0;
            double rollingTime = 0;
            for (SessionDataSlice roll : s30s.slices) {
                rolling += roll.currentPower * roll.duration;
                rollingTime += roll.duration;
            }
            if (rollingTime > 0) {
                slice.avgPowerRolling = rolling / rollingTime;
            }
            span.setNormalizedPowerTime(span.getNormalizedPowerTime() + slice.duration);
        }

        if (slice.avgPowerRolling > 0) {
            span.getApr4s().add(Math.pow(slice.avgPowerRolling, 4));

        }

        if (span.getApr4s().size() > 0) {
            double apr4Sum = 0;
            for (double apr4 : span.getApr4s()) {
                apr4Sum += apr4;
            }
            span.setNormalizedPower(Math.pow(apr4Sum / span.getApr4s().size(), 0.25));
        }

        if (profileFTP > 0 && span.getNormalizedPower() > 0) {
            span.setIntensityFactor(span.getNormalizedPower() / profileFTP);

            if (span.getNormalizedPowerTime() > 0) {
                double numer = span.getNormalizedPowerTime() * span.getNormalizedPower() * span.getIntensityFactor();
                double denom = profileFTP * 3600;
                span.setTrainingStressScore((numer / denom) * 100);
            }
        }
    }

    static private DataSliceSet dataSlicesForTime(double start, double end, ArrayList<SessionDataSlice> slices) {
        ArrayList<SessionDataSlice> filtered = new ArrayList<>((int) Math.ceil(end - start));
        for (SessionDataSlice slice : slices) {
            if (slice.timestamp >= start && slice.timestamp <= end) {
                filtered.add(slice);
            }
        }

        double interval = 0;
        if (filtered.size() == 1) {
            interval = filtered.get(0).duration;
        } else if (filtered.size() > 1) {
            interval = filtered.get(filtered.size() - 1).timestamp - filtered.get(0).timestamp;
        }

        boolean discarded = slices.size() != filtered.size();

        DataSliceSet dss = new DataSliceSet();
        dss.discarded = discarded;
        dss.slices = filtered;
        dss.interval = interval;

        return dss;
    }

    static public double powerAverageForPreviousTime(double time, double current, ArrayList<SessionDataSlice> slices) {
        double timePadding = 0.5;
        double sessionTime = (current - time) - timePadding;
        double accumulator = 0;
        double totalTime = 0;

        for (int i = slices.size() - 1; i >= 0; --i) {
            SessionDataSlice slice = slices.get(i);
            if (slice.timestamp >= sessionTime && slice.timestamp <= current) {
                accumulator += slice.currentPower * slice.duration;
                totalTime += slice.duration;
            } else {
                break;
            }
        }
        if (totalTime > 0) {
            return accumulator / totalTime;
        }
        return 0;
    }

    public void populateProfileValues(SessionDataSlice slice) {
        if (slice.currentPower > -1) {
            slice.currentPowerZone = Profile.zoneForValue(slice.currentPower, getProfilePowerZones());
            slice.currentPowerPercentageFTP = Profile.percentOfFTP(slice.currentPower, getProfilePowerFTP());
            slice.currentPowerWattsPerKilogram = slice.currentPower / getProfileWeightKG();
        }
        if (slice.currentHeartRate > -1) {
            slice.currentHeartRateZone = Profile.zoneForValue(slice.currentHeartRate, getProfileHeartZones());
            slice.currentHeartRateReservePercent = Profile.percentOfReserve(slice.currentHeartRate, getProfileHeartResting(), getProfileHeartMax());
            slice.currentHeartRateMaxPercent = Profile.percentOfMax(slice.currentHeartRate, getProfileHeartMax());
        }
    }

    static public class DataSliceSet {
        ArrayList<SessionDataSlice> slices;
        double interval;
        boolean discarded;
    }

    static private void fillArray(ArrayList<Double> array, double value, int count) {
        for (int i = 0; i < count; i++) {
            array.add(value);
        }
    }


    public void rebuild() {

        clear(this);

        ArrayList<SessionDataSlice> rSlices = SessionDataSlice.deserialize(getSensorData());

        for (int i = 1; i <= rSlices.size(); i++) {
            SessionDataSlice a = rSlices.get(i - 1);
            if (i == 1) {
                a.duration = a.timestamp;
            } else if (i < rSlices.size()) {
                SessionDataSlice b = rSlices.get(i);
                a.duration = b.timestamp - a.timestamp;
            } else {
                a.duration = getDuration() - a.timestamp;
            }
            addDataSlice(a);
        }

        ArrayList<SessionLap> rLaps = new ArrayList<>();
        for (double timestamp : getLapMarkers()) {
            rLaps.add(new SessionLap(timestamp));
        }

        for (int i = 1; i <= rLaps.size(); i++) {
            SessionLap a = rLaps.get(i - 1);
            a.setLapNumber(i - 1);

            if (i == 1){
                a.setDuration(a.getDuration());
            }else if(i < rLaps.size()) {
                SessionLap b = rLaps.get(i);
                a.setDuration(b.getStartTime() - a.getStartTime());
            } else {
                a.setDuration(getDuration() - a.getStartTime());
            }
        }
        laps.clear();
        for (SessionLap lap : rLaps) {
            laps.add(lap);
            rebuildSpan(lap);
        }
//        lapsString = laps.toString();
    }

    private void rebuildSpan(SessionDataSpan span) {
        // assumes dataslices are intact
//        clear(span);
        ArrayList<SessionDataSlice> slices = dataSlicesForSpan(span);

        for (SessionDataSlice slice : slices) {
            addDataSlice(slice, span, slices, getProfilePowerFTP());
        }
    }

    public String getExportFileName() {
        return Profile.getUserName() + "-" + new org.joda.time.DateTime(getWorkoutDate()).toString(DateTimeFormat.forPattern("YYYYMMdd-HHmm"));
    }

    public String getFormattedWorkoutDate() {
        String longDate = new DateTime(getWorkoutDate()).toString(DateTimeFormat.longDate());
        String shortTime = new DateTime(getWorkoutDate()).toString(DateTimeFormat.shortTime());
        return longDate + " at " + shortTime;
    }

    private ArrayList<SessionDataSlice> dataSlicesForSpan(SessionDataSpan span) {
        if (span != null) {
            return dataSlicesForTime(span.getStartTime(), span.getEndTime(), dataSlices).slices;
        }
        return dataSlices;
    }

    private JsonObject getACLs() {
        JsonObject j = new JsonObject();
        j.addProperty("read", true);
        j.addProperty("write", true);
        JsonObject acl = new JsonObject();
        acl.add(Profile.current().getObjectId(), j);
        return acl;

    }

    public JsonObject serializeToJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("avgCadence", getAvgCadence());
        jsonObject.addProperty("normalizedPower", getNormalizedPower());
        jsonObject.addProperty("avgHeartRateReservePercent", getAvgHeartRateReservePercent());
        jsonObject.addProperty("maxCadence", getMaxCadence());
        jsonObject.addProperty("minHeartRate", getMinHeartRate());
        jsonObject.addProperty("profileWeightKG", getProfileWeightKG());
        jsonObject.addProperty("profileHeartMax", getProfileHeartMax());
        jsonObject.addProperty("duration", getDuration());
        jsonObject.addProperty("workoutName", getWorkoutName());
        jsonObject.addProperty("avgLapTime", getAvgLapTime());
        jsonObject.addProperty("avgHeartRateMaxPercent", getAvgHeartRateMaxPercent());
        jsonObject.addProperty("workoutDuration", getWorkoutDuration());
        jsonObject.addProperty("maxPower", getMaxPower());
        jsonObject.addProperty("distanceKM", getDistanceKM());
        jsonObject.addProperty("avgSpeedKPH", getAvgSpeedKPH());
        jsonObject.addProperty("profilePowerFTP", getProfilePowerFTP());
        jsonObject.addProperty("startTime", getStartTime());
        jsonObject.addProperty("avgHeartRate", getAvgHeartRate());
        jsonObject.addProperty("kilojoules", getKilojoules());
        jsonObject.addProperty("workoutDescription", getWorkoutDescription());
        jsonObject.addProperty("maxHeartRate", getMaxHeartRate());
        jsonObject.addProperty("trainingStressScore", getTrainingStressScore());
        jsonObject.addProperty("warmupDuration", getWarmupDuration());
        jsonObject.addProperty("intensityFactor", getIntensityFactor());
        jsonObject.addProperty("maxSpeedKPH", getMaxSpeedKPH());
        jsonObject.addProperty("avgPower", getAvgPower());
        jsonObject.addProperty("profileHeartResting", getProfileHeartResting());
//        jsonObject.addProperty("profileHeightCM", getProfileHeightCM());
        jsonObject.addProperty("workoutId", getWorkoutId());
        jsonObject.addProperty("uuid", getUuid());
        jsonObject.addProperty("calculatedFTP", getCalculatedFTP());
        if (getSensorData() != null) {
            jsonObject.add("sensorData", RealmUtils.serializeToJsonByte64(getSensorData()));
        }
        jsonObject.add("workoutDate", RealmUtils.serializeToJson(getWorkoutDate()));
        jsonObject.add("meanMaximums", RealmUtils.serializeArrayToJson(getMeanMaximums()));
        jsonObject.add("timeInHeartRateZones", RealmUtils.serializeArrayToJson(getTimeInHeartRateZones()));
        jsonObject.add("timeInPowerZones", RealmUtils.serializeArrayToJson(getTimeInPowerZones()));
        jsonObject.add("profilePowerZones", RealmUtils.serializeArrayToJson(getProfilePowerZones()));
        jsonObject.add("meanMaximumTimes", RealmUtils.serializeArrayToJson(getMeanMaximumTimes()));
        if (getLapMarkers() != null && lapMarkers.size() > 0) {
            jsonObject.add("lapMarkers", RealmUtils.serializeArrayToJson(getLapMarkers()));
        }
        jsonObject.add("profileHeartZones", RealmUtils.serializeArrayToJson(getProfileHeartZones()));
        jsonObject.add("profile", RealmUtils.serializeProfilePointerToJson());
        jsonObject.add("ACL", getACLs());
//        Log.d(CLASS_NAME, jsonObject.toString());
//        TODO workout tags?
//        TODO location information
        return jsonObject;
    }


}
