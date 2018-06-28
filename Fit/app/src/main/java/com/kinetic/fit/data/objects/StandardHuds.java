package com.kinetic.fit.data.objects;

import com.kinetic.fit.data.FitProperty;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by Saxton on 5/19/17.
 */

public class StandardHuds {

    public static JSONArray getStandardHudPagerData(){
        try {
            return new JSONArray("[" +
                    "[" +
                    "[" + FitProperty.Power.ordinal() + "," + FitProperty.PowerTarget.ordinal() + "]," +
                    "[" + FitProperty.Cadence.ordinal() + "," + FitProperty.CadenceTarget.ordinal() + "]," +
                    "[" + FitProperty.WorkoutIntervalDurationToGo.ordinal() + "," + FitProperty.HeartRate.ordinal() + "]" +
                    "]," +

                    "[" +
                    "[" + FitProperty.Power.ordinal() + "," + FitProperty.HeartRate.ordinal() + "]," +
                    "[" + FitProperty.Cadence.ordinal() + "," + FitProperty.Calories.ordinal() + "]," +
                    "[" + FitProperty.SpeedKPH.ordinal() + "," + FitProperty.Distance.ordinal() + "]" +
                    "]," +

                    "[" +
                    "[" + FitProperty.Power.ordinal() + "]," +
                    "[" + FitProperty.PowerTarget.ordinal() + "," + FitProperty.WorkoutIntervalDurationToGo.ordinal() + "]," +
                    "[" + FitProperty.Cadence.ordinal() + "," + FitProperty.PowerAverageLap.ordinal() + "," + FitProperty.HeartRate.ordinal() + "]" +
                    "]," +

                    "[" +
                    "[" + FitProperty.PowerAverageLapPrevious.ordinal() + "]," +
                    "[" + FitProperty.HeartRateLapAveragePrevious.ordinal() + "]," +
                    "[" + FitProperty.CadenceLapAveragePrevious.ordinal() + "," + FitProperty.SpeedKPHAverageLapPrevious.ordinal() + "," + FitProperty.LapDistancePrevious.ordinal() + "]" +
                    "]" +

                    "]");
        } catch (JSONException e) {

        }
        return null;
    }

    public static JSONArray getSingleHudArray(){
        try {
            return new JSONArray("[" +
                    "[" + FitProperty.Power.ordinal() + "," + FitProperty.Cadence.ordinal() + "]," +
                    "[" + FitProperty.HeartRate.ordinal() + "," + FitProperty.SpeedKPH.ordinal() + "]" +
                    "]");
        } catch (JSONException e) {

        }
        return null;
    }
}
