package com.kinetic.fit.data;

import android.content.Context;
import android.util.TypedValue;

import com.kinetic.fit.R;

import java.util.ArrayList;
import java.util.Arrays;

public enum FitProperty {
    None(Category.None, R.mipmap.icon_null, R.string.prop_none, R.string.prop_none_short, Icons.None),
    Cadence(Category.Cadence, R.mipmap.icon_cadence, R.string.prop_cadence, R.string.prop_cadence_short, Icons.None),
    CadenceAverage(Category.Cadence, R.mipmap.icon_cadence, R.string.prop_cadence_avg, R.string.prop_cadence_avg_short, Icons.Avg),
    CadenceLapAverage(Category.Cadence, R.mipmap.icon_cadence, R.string.prop_cadence_average_lap, R.string.prop_cadence_average_lap_short, Icons.Current),
    CadenceLapAveragePrevious(Category.Cadence, R.mipmap.icon_cadence, R.string.prop_cadence_average_lap_previous, R.string.prop_cadence_average_lap_previous_short, Icons.PreviousAvg),
    CadenceTarget(Category.Cadence, R.mipmap.icon_cadence, R.string.prop_cadence_intervalTarget, R.string.prop_cadence_intervalTarget_short, Icons.None),
    Calories(Category.Power, R.mipmap.icon_calories, R.string.prop_calories, R.string.prop_calories_short, Icons.None),
    Distance(Category.Distance, R.mipmap.icon_distance, R.string.prop_distance, R.string.prop_distance_short, Icons.None),
    HeartRate(Category.Heart, R.mipmap.icon_heart, R.string.prop_heartRate, R.string.prop_heartRate_short, Icons.None),
    HeartRateAverage(Category.Heart, R.mipmap.icon_heart, R.string.prop_heartRate_avg, R.string.prop_heartRate_avg_short, Icons.Avg),
    HeartRateLapAverage(Category.Heart, R.mipmap.icon_heart, R.string.prop_heartRate_avg_lap, R.string.prop_heartRate_avg_lap_short, Icons.CurrentAvg),
    HeartRateLapAveragePrevious(Category.Heart, R.mipmap.icon_heart, R.string.prop_heartRate_avg_lap_previous, R.string.prop_heartRate_avg_lap_previous_short, Icons.PreviousAvg),
    HeartRateLapPercentageMaxAverage(Category.Heart, R.mipmap.icon_heart, R.string.prop_heartRate_percentMax_avg_lap, R.string.prop_heartRate_percentMax_avg_lap_short, Icons.CurrentAvg),
    HeartRateLapPercentageReserveAverage(Category.Heart, R.mipmap.icon_heart, R.string.prop_heartRate_percentReserved_avg_lap, R.string.prop_heartRate_percentReserved_avg_lap_short, Icons.CurrentAvg),
    HeartRatePercentageMax(Category.Heart, R.mipmap.icon_heart, R.string.prop_heartRate_percentMax, R.string.prop_heartRate_percentMax_short, Icons.None),
    HeartRatePercentageMaxAverage(Category.Heart, R.mipmap.icon_heart, R.string.prop_heartRate_percentMax_avg, R.string.prop_heartRate_percentMax_avg_short, Icons.Avg),
    HeartRatePercentageReserve(Category.Heart, R.mipmap.icon_heart, R.string.prop_heartRate_percentReserved, R.string.prop_heartRate_percentReserved_short, Icons.None),
    HeartRatePercentageReserveAverage(Category.Heart, R.mipmap.icon_heart, R.string.prop_heartRate_percentReserved_avg, R.string.prop_heartRate_percentReserved_avg_short, Icons.Avg),
    HeartRateTimeInZone1(Category.Time, R.mipmap.icon_heart, R.string.prop_heartRate_time_zone_1, R.string.prop_heartRate_time_zone_1_short, Icons.None),
    HeartRateTimeInZone2(Category.Time, R.mipmap.icon_heart, R.string.prop_heartRate_time_zone_2, R.string.prop_heartRate_time_zone_2_short, Icons.None),
    HeartRateTimeInZone3(Category.Time, R.mipmap.icon_heart, R.string.prop_heartRate_time_zone_3, R.string.prop_heartRate_time_zone_3_short, Icons.None),
    HeartRateTimeInZone4(Category.Time, R.mipmap.icon_heart, R.string.prop_heartRate_time_zone_4, R.string.prop_heartRate_time_zone_4_short, Icons.None),
    HeartRateTimeInZone5(Category.Time, R.mipmap.icon_heart, R.string.prop_heartRate_time_zone_5, R.string.prop_heartRate_time_zone_5_short, Icons.None),
    HeartRateZone(Category.Time, R.mipmap.icon_heart, R.string.prop_heartRate_currentZone, R.string.prop_heartRate_currentZone_short, Icons.None),
    KilocaloriesPerMinute(Category.Power, R.mipmap.icon_power, R.string.prop_kcalPerMinute, R.string.prop_kcalPerMinute_short, Icons.None),
    LapCount(Category.Time, R.mipmap.icon_distance, R.string.prop_lap_count, R.string.prop_lap_count_short, Icons.None),
    LapDistance(Category.Distance, R.mipmap.icon_distance, R.string.prop_distance_lap, R.string.prop_distance_lap_short, Icons.Current),
    LapDistancePrevious(Category.Distance, R.mipmap.icon_distance, R.string.prop_distance_lap_previous, R.string.prop_distance_lap_previous_short, Icons.Previous),
    LapTime(Category.Time, R.mipmap.icon_time, R.string.prop_lap_time, R.string.prop_lap_time_short, Icons.None),
    LapTimeAverage(Category.Time, R.mipmap.icon_time, R.string.prop_lap_time_average, R.string.prop_lap_time_average_short, Icons.Avg),
    LapTimePrevious(Category.Time, R.mipmap.icon_time, R.string.prop_lap_time_previous, R.string.prop_lap_time_previous_short, Icons.Previous),
    Power(Category.Power, R.mipmap.icon_power, R.string.prop_power_current, R.string.prop_power_current_short, Icons.None),
    Power1mAverage(Category.Power, R.mipmap.icon_power, R.string.prop_power_average_1m, R.string.prop_power_average_1m_short, Icons.Avg),
    Power20mAverage(Category.Power, R.mipmap.icon_power, R.string.prop_power_average_20m, R.string.prop_power_average_20m_short, Icons.Avg),
    Power20sAverage(Category.Power, R.mipmap.icon_power, R.string.prop_power_average_20s, R.string.prop_power_average_20s_short, Icons.Avg),
    Power5mAverage(Category.Power, R.mipmap.icon_power, R.string.prop_power_average_5m, R.string.prop_power_average_5m_short, Icons.Avg),
    Power5sAverage(Category.Power, R.mipmap.icon_power, R.string.prop_power_average_5s, R.string.prop_power_average_5s_short, Icons.Avg),
    PowerAverage(Category.Power, R.mipmap.icon_power, R.string.prop_power_average, R.string.prop_power_average_short, Icons.Avg),
    PowerAverageLap(Category.Power, R.mipmap.icon_power, R.string.prop_power_average_lap, R.string.prop_power_average_lap_short, Icons.Avg),
    PowerAverageLapPrevious(Category.Power, R.mipmap.icon_power, R.string.prop_power_average_lap_previous, R.string.prop_power_average_lap_previous_short, Icons.PreviousAvg),
    PowerIntensityFactor(Category.Power, R.mipmap.icon_power, R.string.prop_power_intensityFactor, R.string.prop_power_intensityFactor_short, Icons.None),
    PowerKilojoules(Category.Power, R.mipmap.icon_power, R.string.prop_power_kilojoules, R.string.prop_power_kilojoules_short, Icons.None),
    PowerMax(Category.Power, R.mipmap.icon_power, R.string.prop_power_max, R.string.prop_power_max_short, Icons.None),
    PowerNormalized(Category.Power, R.mipmap.icon_power, R.string.prop_power_normalized, R.string.prop_power_normalized_short, Icons.None),
    PowerNormalizedLap(Category.Power, R.mipmap.icon_power, R.string.prop_power_normalized_lap, R.string.prop_power_normalized_lap_short, Icons.Current),
    PowerNormalizedLapPrevious(Category.Power, R.mipmap.icon_power, R.string.prop_power_normalized_lap_previous, R.string.prop_power_normalized_lap_previous_short, Icons.Previous),
    PowerPercentageFTP(Category.Power, R.mipmap.icon_power, R.string.prop_power_currentPercentFTP, R.string.prop_power_currentPercentFTP_short, Icons.None),
    PowerTarget(Category.Power, R.mipmap.icon_power, R.string.prop_power_intervalTarget, R.string.prop_power_intervalTarget_short, Icons.None),
    PowerTimeInZone1(Category.Time, R.mipmap.icon_power, R.string.prop_power_time_zone_1, R.string.prop_power_time_zone_1_short, Icons.None),
    PowerTimeInZone2(Category.Time, R.mipmap.icon_power, R.string.prop_power_time_zone_2, R.string.prop_power_time_zone_2_short, Icons.None),
    PowerTimeInZone3(Category.Time, R.mipmap.icon_power, R.string.prop_power_time_zone_3, R.string.prop_power_time_zone_3_short, Icons.None),
    PowerTimeInZone4(Category.Time, R.mipmap.icon_power, R.string.prop_power_time_zone_4, R.string.prop_power_time_zone_4_short, Icons.None),
    PowerTimeInZone5(Category.Time, R.mipmap.icon_power, R.string.prop_power_time_zone_5, R.string.prop_power_time_zone_5_short, Icons.None),
    PowerTimeInZone6(Category.Time, R.mipmap.icon_power, R.string.prop_power_time_zone_6, R.string.prop_power_time_zone_6_short, Icons.None),
    PowerTimeInZone7(Category.Time, R.mipmap.icon_power, R.string.prop_power_time_zone_7, R.string.prop_power_time_zone_7_short, Icons.None),
    PowerTSS(Category.Power, R.mipmap.icon_power, R.string.prop_power_TSS, R.string.prop_power_TSS_short, Icons.None),
    PowerWattsKilogram(Category.Power, R.mipmap.icon_power, R.string.prop_power_wattsPerKilogram, R.string.prop_power_wattsPerKilogram_short, Icons.None),
    PowerZone(Category.Power, R.mipmap.icon_power, R.string.prop_power_currentZone, R.string.prop_power_currentZone_short, Icons.None),
    SpeedKPH(Category.Speed, R.mipmap.icon_speed, R.string.prop_speed, R.string.prop_speed_short, Icons.None),
    SpeedKPHAverage(Category.Speed, R.mipmap.icon_speed, R.string.prop_speed_avg, R.string.prop_speed_avg_short, Icons.Avg),
    SpeedKPHAverageLap(Category.Speed, R.mipmap.icon_speed, R.string.prop_speed_avg_lap, R.string.prop_speed_avg_lap_short, Icons.Current),
    SpeedKPHAverageLapPrevious(Category.Speed, R.mipmap.icon_speed, R.string.prop_speed_avg_lap_previous, R.string.prop_speed_avg_lap_previous_short, Icons.PreviousAvg),
    SpeedKPHMax(Category.Speed, R.mipmap.icon_speed, R.string.prop_speed_max, R.string.prop_speed_max_short, Icons.Max),
    WorkoutDuration(Category.Time, R.mipmap.icon_time, R.string.prop_time_workout_duration, R.string.prop_time_workout_duration_short, Icons.None),
    WorkoutDurationToGo(Category.Time, R.mipmap.icon_time, R.string.prop_time_workoutRemaining, R.string.prop_time_workoutRemaining_short, Icons.None),
    WorkoutIntervalDurationToGo(Category.Time, R.mipmap.icon_time, R.string.prop_time_intervalRemaining, R.string.prop_time_intervalRemaining_short, Icons.None);

    public enum Icons {

        None(null, null),
        Previous(R.mipmap.property_previous, null),
        Current(R.mipmap.property_lap, null),
        Avg(null, R.mipmap.property_average),
        Max(null, R.mipmap.property_max),
        PreviousAvg(R.mipmap.property_previous, R.mipmap.property_average),
        PreviousMax(R.mipmap.property_previous, R.mipmap.property_max),
        CurrentAvg(R.mipmap.property_lap, R.mipmap.property_average),
        CurrentMax(R.mipmap.property_lap, R.mipmap.property_max);

        public Integer lapIcon;
        public Integer valueIcon;

        Icons(Integer lapIcon, Integer valueIcon) {
            this.lapIcon = lapIcon;
            this.valueIcon = valueIcon;
        }

        public Integer getValueIcon() {
            return valueIcon;
        }

        public Integer getLapIcon() {
            return lapIcon;
        }
    }

    public enum Category {
        Power(R.attr.colorFitPower),
        Heart(R.attr.colorFitHeart),
        Speed(R.attr.colorFitSpeed),
        Time(R.attr.colorFitTime),
        Cadence(R.attr.colorFitCadence),
        Distance(R.attr.colorFitDistance),
        None(R.attr.colorFitBody);

        public int colorAttribute;

        Category(int colorAttribute) {
            this.colorAttribute = colorAttribute;
        }
    }

    public int title;
    public int keyword;
    public Category category;
    public int image;
    private int colorAttribute;
    public Icons icons;

    FitProperty(Category category, int image, int title, int keyword, Icons icons) {
        this.category = category;
        this.image = image;
        this.title = title;
        this.keyword = keyword;
        this.colorAttribute = category.colorAttribute;
        this.icons = icons;
    }

    public int getColor(Context context) {
        int attr = colorAttribute;
        if (category.equals(Category.Power) && this.ordinal() == FitProperty.Calories.ordinal()) {
            attr = R.attr.colorFitPower;
        }
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attr, typedValue, true);
        return typedValue.data;
    }

    public String getStringValue(int value, String format) {
        if (value >= 0) {
            return String.format(format, value);
        }
        return "---";

    }

    public String getStringValue(double value, String format) {
        if (value >= 0) {
            return String.format(format, value);
        }
        return "---";
    }

    public static ArrayList<FitProperty> getPropertyArray(){
        return new ArrayList<>(Arrays.asList(FitProperty.None,
                FitProperty.Power,
                FitProperty.Power1mAverage,
                FitProperty.Power20mAverage,
                FitProperty.Power20sAverage,
                FitProperty.Power5mAverage,
                FitProperty.Power5sAverage,
                FitProperty.PowerAverage,
                FitProperty.PowerAverageLap,
                FitProperty.PowerAverageLapPrevious,
                FitProperty.PowerIntensityFactor,
                FitProperty.PowerKilojoules,
                FitProperty.PowerMax,
                FitProperty.PowerNormalized,
                FitProperty.PowerNormalizedLap,
                FitProperty.PowerNormalizedLapPrevious,
                FitProperty.PowerPercentageFTP,
                FitProperty.PowerTarget,
                FitProperty.PowerTimeInZone1,
                FitProperty.PowerTimeInZone2,
                FitProperty.PowerTimeInZone3,
                FitProperty.PowerTimeInZone4,
                FitProperty.PowerTimeInZone5,
                FitProperty.PowerTimeInZone6,
                FitProperty.PowerTimeInZone7,
                FitProperty.PowerTSS,
                FitProperty.PowerWattsKilogram,
                FitProperty.PowerZone,
                FitProperty.KilocaloriesPerMinute,
                FitProperty.Calories,
                FitProperty.HeartRate,
                FitProperty.HeartRateAverage,
                FitProperty.HeartRateLapAverage,
                FitProperty.HeartRateLapAveragePrevious,
                FitProperty.HeartRateLapPercentageMaxAverage,
                FitProperty.HeartRateLapPercentageReserveAverage,
                FitProperty.HeartRatePercentageMax,
                FitProperty.HeartRatePercentageMaxAverage,
                FitProperty.HeartRatePercentageReserve,
                FitProperty.HeartRatePercentageReserveAverage,
                FitProperty.HeartRateTimeInZone1,
                FitProperty.HeartRateTimeInZone2,
                FitProperty.HeartRateTimeInZone3,
                FitProperty.HeartRateTimeInZone4,
                FitProperty.HeartRateTimeInZone5,
                FitProperty.HeartRateZone,
                FitProperty.SpeedKPH,
                FitProperty.SpeedKPHAverage,
                FitProperty.SpeedKPHAverageLap,
                FitProperty.SpeedKPHAverageLapPrevious,
                FitProperty.SpeedKPHMax,
                FitProperty.LapTime,
                FitProperty.LapTimeAverage,
                FitProperty.LapTimePrevious,
                FitProperty.WorkoutDuration,
                FitProperty.WorkoutDurationToGo,
                FitProperty.WorkoutIntervalDurationToGo,
                FitProperty.Cadence,
                FitProperty.CadenceAverage,
                FitProperty.CadenceLapAverage,
                FitProperty.CadenceLapAveragePrevious,
                FitProperty.CadenceTarget,
                FitProperty.Distance,
                FitProperty.LapCount,
                FitProperty.LapDistance,
                FitProperty.LapDistancePrevious));
    }

}
