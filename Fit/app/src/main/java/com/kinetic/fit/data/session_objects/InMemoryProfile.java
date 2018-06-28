package com.kinetic.fit.data.session_objects;

import com.kinetic.fit.data.realm_objects.Profile;

import java.util.ArrayList;
import java.util.Arrays;

import io.realm.Realm;
import io.realm.annotations.Ignore;

/**
 * Created by Saxton on 1/9/18.
 */

public class InMemoryProfile {

    static ArrayList<Double> PowerZoneDefaultCeilings = new ArrayList<>(Arrays.asList(0.0, 0.55, 0.75, 0.90, 1.05, 1.20, 1.50));
    static ArrayList<Double> HeartZoneDefaultCeilings = new ArrayList<>(Arrays.asList(0.0, 0.60, 0.70, 0.77, 0.88));
    private double weightKG;
    private double heightCM;
    private int powerFTP;
    private int heartResting;
    private int heartMax;
    private ArrayList<Integer> heartZones;
    private ArrayList<Integer> powerZones;

    public InMemoryProfile(Profile p){
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        this.weightKG = p.getWeightKG();
        this.heightCM = p.getHeightCM();
        this.powerFTP = p.getPowerFTP();
        this.heartResting = p.getHeartResting();
        this.heartMax = p.getHeartMax();
        this.powerZones = p.getPowerZonesCache();
        this.heartZones = p.getHeartZonesCache();
        realm.commitTransaction();
    }

    public double getWeightKG() {
        return weightKG;
    }

    public double getHeightCM() {
        return heightCM;
    }

    public int getPowerFTP() {
        return powerFTP;
    }

    public int getHeartResting() {
        return heartResting;
    }

    public int getHeartMax() {
        return heartMax;
    }

    public ArrayList<Integer> getHeartZones() {
        return heartZones;
    }

    public ArrayList<Integer> getPowerZones() {
        return powerZones;
    }

    public ArrayList<Double> getPowerZoneCeilings() {
        if (getPowerFTP() > 0) {
            ArrayList<Double> cache = new ArrayList<>();
            double ftp = this.getPowerFTP();
            ArrayList<Integer> powerZones = this.getPowerZones();
            for (int i = 0; i < powerZones.size(); i++) {
                cache.add((double) powerZones.get(i) / ftp);
            }
            return cache;
        } else {
            return PowerZoneDefaultCeilings;
        }
    }
}
