package com.kinetic.fit.data.objects;

import com.kinetic.fit.data.PowerZones;
import com.kinetic.fit.data.realm_objects.Profile;
import com.kinetic.fit.data.realm_objects.Workout;
import com.kinetic.fit.data.session_objects.InMemoryProfile;
import com.kinetic.fit.util.WorkoutParser;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class WorkoutInterval {

    public static ArrayList<WorkoutInterval> jsonToWorkoutIntervals(JSONArray json) {
        ArrayList<WorkoutInterval> workoutIntervals = new ArrayList<WorkoutInterval>();
        try {
            for (int i = 0; i < json.length(); ++i) {
                workoutIntervals.add(new WorkoutInterval(json.getString(i)));
            }
        } catch (JSONException e) {

        }
        return workoutIntervals;
    }

    public double duration = 0;
    public int endCadence = 0;
    public int endPower = 0;
    public boolean lapCue = false;
    public int startCadence = 0;
    public int startPower = 0;
    public String text;
    public String title;
    public double textAdvance = 5;
    public double textDuration = 5;
    public boolean ftpCalcInterval = false;

    public int distance = 0;
    public double grade = 0.0;

    public WorkoutInterval(String jsonString) {
        String[] elements = jsonString.split("\\|");
        if (elements.length >= 10) {
            title = elements[0];
            duration = Double.parseDouble(elements[1]);
            startPower = Integer.parseInt(elements[2]);
            endPower = Integer.parseInt(elements[3]);
            startCadence = Integer.parseInt(elements[4]);
            endCadence = Integer.parseInt(elements[5]);
            lapCue = elements[6].equals("1");
            text = elements[7];
            textAdvance = Double.parseDouble(elements[8]);
            textDuration = Double.parseDouble(elements[9]);

        }
        if (elements.length >= 11) {
            ftpCalcInterval = elements[10].equals("1");
        }
    }

    public WorkoutInterval(WorkoutParser.WorkoutDefinition.Interval interval){
        this.duration = interval.duration;
        this.startPower = interval.percentFTPStart;
        this.endPower = interval.percentFTPEnd;
        this.startCadence = interval.cadenceStart;
        this.endCadence = interval.cadenceEnd;
    }

    public double startPowerMin(Profile profile) {
        if (startPower >= 0) {
            return startPower;
        }
        return PowerZones.minPowerPForZone(Math.abs(startPower), profile);
    }

    public double startPowerMin(InMemoryProfile profile) {
        if (startPower >= 0) {
            return startPower;
        }
        return PowerZones.minPowerPForZone(Math.abs(startPower), profile);
    }

    public double startPowerMax(Profile profile) {
        if (startPower >= 0) {
            return startPower;
        }
        return PowerZones.maxPowerPForZone(Math.abs(startPower), profile);
    }

    public double startPowerMax(InMemoryProfile profile) {
        if (startPower >= 0) {
            return startPower;
        }
        return PowerZones.maxPowerPForZone(Math.abs(startPower), profile);
    }

    public double endPowerMin(Profile profile) {
        if (endPower >= 0) {
            return endPower;
        }
        return PowerZones.minPowerPForZone(Math.abs(endPower), profile);
    }

    public double endPowerMin(InMemoryProfile profile) {
        if (endPower >= 0) {
            return endPower;
        }
        return PowerZones.minPowerPForZone(Math.abs(endPower), profile);
    }

    public double endPowerMax(Profile profile) {
        if (endPower >= 0) {
            return endPower;
        }
        return PowerZones.maxPowerPForZone(Math.abs(endPower), profile);
    }

    public double endPowerMax(InMemoryProfile profile) {
        if (endPower >= 0) {
            return endPower;
        }
        return PowerZones.maxPowerPForZone(Math.abs(endPower), profile);
    }

    public double startPower(Profile profile) {
        return (startPowerMin(profile) + startPowerMax(profile)) * 0.5;
    }

    public double endPower(Profile profile) {
        return (endPowerMin(profile) + endPowerMax(profile)) * 0.5;
    }

    public double averagePower(Profile profile) {
        return (startPower(profile) + endPower(profile)) * 0.5;
    }

    public double getKilojoules(Profile profile) {
        double avgP = averagePower(profile) / 100;
        double avgW = avgP * profile.getPowerFTP();
        return (avgW * duration) / 1000;
    }

    @Override
    public String toString() {
        String t = this.title == null ? "" : this.title;
        String txt = this.text == null ? "" : this.text;
        String cue = lapCue ? "1" : "0";
        String inter = ftpCalcInterval ? "1" : "0";
        return t + "|" + (int)duration + "|" + startPower + "|" + endPower+ "|" + startCadence
                + "|" + endCadence + "|" + cue + "|" + txt + "|" + (int)textAdvance
                + "|" + (int)textDuration + "|" + inter + "|" + distance + "|" + grade;
    }
}
