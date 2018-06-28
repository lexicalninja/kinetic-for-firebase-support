package com.kinetic.fit.data.session_objects;

import com.kinetic.fit.data.realm_objects.Session;
import com.kinetic.fit.util.Conversions;

import java.util.ArrayList;

public class SessionLap implements SessionDataSpan {

    private double startTime = 0;
    // All of this data can be rebuilt based on session data slices, the lap start time and pz + hr zone thresholds
    private int lapNumber = 0;
    private double avgCadence = 0;
    private double avgHeartRate = 0;
    private double avgHeartRateMaxPercent = 0;
    private double avgHeartRateReservePercent = 0;
    private double avgPower = 0;
    private double avgSpeedKPH = 0;
    private double distanceKM = 0;
    private double duration = 0;
    private double intensityFactor = -1;
    private double kilojoules = 0;
    private double maxCadence = -1;
    private int maxHeartRate = -1;
    private int maxPower = -1;
    private double maxSpeedKPH = -1;
    private int minHeartRate = -1;
    private double normalizedPower = -1;
    private double normalizedPowerTime = 0;
    private double trainingStressScore = -1;
    private ArrayList<Double> timeInHeartRateZones = new ArrayList<>();
    private ArrayList<Double> timeInPowerZones = new ArrayList<>();
    private ArrayList<Double> apr4s = new ArrayList<>();
    private ArrayList<Double> meanMaximums = new ArrayList<>();
    private ArrayList<Double> meanMaximumTimes = new ArrayList<>();

    public SessionLap() {
        Session.clear(this);
    }

    public SessionLap(double startTime) {
        Session.clear(this);
        this.startTime = startTime;
    }

    public int getLapNumber() {
        return lapNumber;
    }

    public void setLapNumber(int lapNumber) {
        this.lapNumber = lapNumber;
    }

    public double getStartTime() {
        return startTime;
    }

    public void setStartTime(double startTime) {
        this.startTime = startTime;
    }

    public ArrayList<Double> getTimeInHeartRateZones() {
        return timeInHeartRateZones;
    }

    public void setTimeInHeartRateZones(ArrayList<Double> timeInHeartRateZones) {
        this.timeInHeartRateZones = timeInHeartRateZones;
    }

    public ArrayList<Double> getTimeInPowerZones() {
        return timeInPowerZones;
    }

    public void setTimeInPowerZones(ArrayList<Double> timeInPowerZones) {
        this.timeInPowerZones = timeInPowerZones;
    }

    public ArrayList<Double> getApr4s() {
        return apr4s;
    }

    public void setApr4s(ArrayList<Double> apr4s) {
        this.apr4s = apr4s;
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
        return meanMaximums;
    }

    public void setMeanMaximums(ArrayList<Double> meanMaximums) {
        this.meanMaximums = meanMaximums;
    }

    public ArrayList<Double> getMeanMaximumTimes() {
        return meanMaximumTimes;
    }

    public void setMeanMaximumTimes(ArrayList<Double> meanMaximumTimes) {
        this.meanMaximumTimes = meanMaximumTimes;
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

    public double getNormalizedPowerTime() {
        return normalizedPowerTime;
    }

    public void setNormalizedPowerTime(double normalizedPowerTime) {
        this.normalizedPowerTime = normalizedPowerTime;
    }

    public double getTrainingStressScore() {
        return trainingStressScore;
    }

    public void setTrainingStressScore(double trainingStressScore) {
        this.trainingStressScore = trainingStressScore;
    }

    public double getCaloriesBurned() {
        return Conversions.kj_to_kcal(getKilojoules());
    }

    public double getEndTime() {
        return getStartTime() + getDuration();
    }

    public void addTimeInPowerZone(int zone, double duration) {
        double time = timeInPowerZones.get(zone);
        timeInPowerZones.set(zone, time + duration);
    }

    public void addTimeInHeartRateZone(int zone, double duration) {
        double time = timeInHeartRateZones.get(zone);
        timeInHeartRateZones.set(zone, time + duration);
    }


}
