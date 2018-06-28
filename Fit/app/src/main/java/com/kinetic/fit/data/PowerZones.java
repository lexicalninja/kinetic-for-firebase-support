package com.kinetic.fit.data;

import com.kinetic.fit.data.realm_objects.Profile;
import com.kinetic.fit.data.session_objects.InMemoryProfile;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Stewart on 11/2/15.
 */
public class PowerZones {

    static public ArrayList<Double> MeanMaxTimes = new ArrayList<>(Arrays.asList(5.0, 20.0, 60.0, 300.0, 1200.0));

    //
//    class func averagePowerPForZone(zone:Int, profile:KINProfile?) -> Float {
//        if (zone < 1) {
//            return 0
//        }
//        let zoneCeilings = powerZoneCeilingsPForProfile(profile)
//        if (zone >= zoneCeilings.count) {
//            return zoneCeilings.last! * 100
//        }
//        return (zoneCeilings[zone - 1] + (zoneCeilings[zone] - zoneCeilings[zone - 1]) * 0.5) * 100
//    }
//
    public static double maxPowerPForZone(int zone, Profile profile) {
        if (zone < 1) {
            return 0;
        }
        ArrayList<Double> zoneCeilings = powerZoneCeilingsPForProfile(profile);
        if (zone >= zoneCeilings.size()) {
            return zoneCeilings.get(zoneCeilings.size() - 1) * 100;
        }
        return zoneCeilings.get(zone) * 100;
    }

    public static double maxPowerPForZone(int zone, InMemoryProfile profile) {
        if (zone < 1) {
            return 0;
        }
        ArrayList<Double> zoneCeilings = profile.getPowerZoneCeilings();
        if (zone >= zoneCeilings.size()) {
            return zoneCeilings.get(zoneCeilings.size() - 1) * 100;
        }
        return zoneCeilings.get(zone) * 100;
    }


    public static double minPowerPForZone(int zone, Profile profile) {
        if (zone < 1) {
            return 0;
        }
        ArrayList<Double> zoneCeilings = powerZoneCeilingsPForProfile(profile);
        if (zone >= zoneCeilings.size()) {
            return zoneCeilings.get(zoneCeilings.size() - 1) * 100;
        }
        return zoneCeilings.get(zone - 1) * 100;
    }

    public static double minPowerPForZone(int zone, InMemoryProfile profile) {
        if (zone < 1) {
            return 0;
        }
        ArrayList<Double> zoneCeilings = profile.getPowerZoneCeilings();
        if (zone >= zoneCeilings.size()) {
            return zoneCeilings.get(zoneCeilings.size() - 1) * 100;
        }
        return zoneCeilings.get(zone - 1) * 100;
    }

    private static ArrayList<Double> powerZoneCeilingsPForProfile(Profile profile) {
            return profile.powerZoneCeilingsP();
    }

}
