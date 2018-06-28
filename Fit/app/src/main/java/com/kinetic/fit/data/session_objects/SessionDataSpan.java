package com.kinetic.fit.data.session_objects;

import android.os.Parcelable;

import java.util.ArrayList;

public interface SessionDataSpan{

    double getAvgCadence();

    void setAvgCadence(double avgCadence);

    double getAvgHeartRate();

    void setAvgHeartRate(double avgHeartRate);

    double getAvgHeartRateMaxPercent();

    void setAvgHeartRateMaxPercent(double avgHeartRateMaxPercent);

    double getAvgHeartRateReservePercent();

    void setAvgHeartRateReservePercent(double avgHeartRateReservePercent);

    double getAvgPower();

    void setAvgPower(double avgPower);

    double getAvgSpeedKPH();

    void setAvgSpeedKPH(double avgSpeedKPH);

    double getDistanceKM();

    void setDistanceKM(double distanceKM);

    double getDuration();

    void setDuration(double duration);

    double getIntensityFactor();

    void setIntensityFactor(double intensityFactor);

    double getKilojoules();

    void setKilojoules(double kilojoules);

    double getMaxCadence();

    void setMaxCadence(double maxCadence);

    int getMaxHeartRate();

    void setMaxHeartRate(int maxHeartRate);

    int getMaxPower();

    void setMaxPower(int maxPower);

    double getMaxSpeedKPH();

    void setMaxSpeedKPH(double maxSpeedKPH);

    ArrayList<Double> getMeanMaximums();

    void setMeanMaximums(ArrayList<Double> meanMaximums);

    ArrayList<Double> getMeanMaximumTimes();

    void setMeanMaximumTimes(ArrayList<Double> meanMaximumTimes);

    int getMinHeartRate();

    void setMinHeartRate(int minHeartRate);

    double getNormalizedPower();

    void setNormalizedPower(double normalizedPower);

    double getStartTime();

    void setStartTime(double startTime);

    ArrayList<Double> getTimeInHeartRateZones();

    void setTimeInHeartRateZones(ArrayList<Double> timeInHeartRateZones);

    ArrayList<Double> getTimeInPowerZones();

    void setTimeInPowerZones(ArrayList<Double> timeInPowerZones);

    ArrayList<Double> getApr4s();

    void setApr4s(ArrayList<Double> apr4s);

    double getNormalizedPowerTime();

    void setNormalizedPowerTime(double normalizedPowerTime);

    double getTrainingStressScore();

    void setTrainingStressScore(double trainingStressScore);

    double getCaloriesBurned();

    double getEndTime();

    void addTimeInPowerZone(int zone, double duration);
    void addTimeInHeartRateZone(int zone, double duration);

}
