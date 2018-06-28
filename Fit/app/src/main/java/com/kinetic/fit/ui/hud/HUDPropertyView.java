package com.kinetic.fit.ui.hud;

import android.content.Context;
import android.support.v4.widget.TextViewCompat;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kinetic.fit.R;
import com.kinetic.fit.connectivity.SensorValues;
import com.kinetic.fit.controllers.SessionController;
import com.kinetic.fit.data.FitProperty;
import com.kinetic.fit.data.shared_prefs.SharedPreferencesInterface;
import com.kinetic.fit.util.Conversions;
import com.kinetic.fit.util.ViewStyling;

import org.androidannotations.annotations.EView;
import org.androidannotations.annotations.ViewById;

@EView
public class HUDPropertyView extends LinearLayout {

    @ViewById(R.id.hud_property_title)
    TextView mPropertyTitle;
    @ViewById(R.id.hud_property_image)
    ImageView mPropertyImage;
    @ViewById(R.id.hud_property_value)
    TextView mPropertyValue;

    private FitProperty mProperty = FitProperty.None;

    public HUDPropertyView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setProperty(FitProperty property) {
        mProperty = property;

        mPropertyTitle.setText(getResources().getString(property.keyword).toLowerCase());
        mPropertyTitle.setTextColor(property.getColor(getContext()));
        mPropertyImage.setImageResource(property.image);
        mPropertyImage.setColorFilter(property.getColor(getContext()));
    }

    public void updateValue(SessionController.SessionControllerBinder sc, SensorValues svs) {
        SensorValues sensorValues = svs;
        if (sensorValues == null && sc != null) {
            sensorValues = sc.getSensorValues();
        }
        switch (mProperty) {
            case None:
                mPropertyValue.setText("---");
                break;
            case Cadence:
                mPropertyValue.setText(mProperty.getStringValue(sensorValues.currentCadence, "%.0f"));
                break;
            case CadenceAverage:
                mPropertyValue.setText(mProperty.getStringValue(sc.getSession().getAvgCadence(), "%.0f"));
                break;
            case CadenceLapAverage:
                mPropertyValue.setText(mProperty.getStringValue(sc.getCurrentLap().getAvgCadence(), "%.0f"));
                break;
            case CadenceLapAveragePrevious:
                if (sc.getPreviousLap() == null) {
                    mPropertyValue.setText("---");
                    break;
                }
                mPropertyValue.setText(mProperty.getStringValue(sc.getPreviousLap().getAvgCadence(), "%.0f"));
                break;
            case CadenceTarget:
                mPropertyValue.setText(mProperty.getStringValue(sc.getTargets().getCadence(), "%.0f"));
                break;
            case Calories:
                mPropertyValue.setText(mProperty.getStringValue(sc.getSession().getCaloriesBurned(), "%.0f"));
                break;
            case Distance: {
                if (SharedPreferencesInterface.isMetric()) {
                    mPropertyValue.setText(mProperty.getStringValue(sc.getSession().getDistanceKM(), "%.1f"));
                } else {
                    mPropertyValue.setText(mProperty.getStringValue(Conversions.kph_to_mph(sc.getSession().getDistanceKM()), "%.1f"));
                }
                break;
            }
            case HeartRate:
                mPropertyValue.setText(mProperty.getStringValue(sensorValues.currentHeartRate, "%d"));
                break;
            case HeartRateAverage:
                mPropertyValue.setText(mProperty.getStringValue(sc.getSession().getAvgHeartRate(), "%.0f"));
                break;
            case HeartRateLapAverage:
                mPropertyValue.setText(mProperty.getStringValue(sc.getCurrentLap().getAvgHeartRate(), "%.0f"));
                break;
            case HeartRateLapAveragePrevious: {
                if (sc.getPreviousLap() == null) {
                    mPropertyValue.setText("---");
                    break;
                }
                mPropertyValue.setText(mProperty.getStringValue(sc.getPreviousLap().getAvgHeartRate(), "%.0f"));
                break;
            }
            case HeartRateLapPercentageMaxAverage:
                mPropertyValue.setText(mProperty.getStringValue((int) (sc.getCurrentLap().getAvgHeartRateMaxPercent() * 100), "%d"));
                break;
            case HeartRateLapPercentageReserveAverage:
                mPropertyValue.setText(mProperty.getStringValue((int) (sc.getCurrentLap().getAvgHeartRateReservePercent() * 100), "%d"));
                break;
            case HeartRatePercentageMax:
                mPropertyValue.setText(mProperty.getStringValue((int) (sc.getCurrentDataSlice().currentHeartRateMaxPercent * 100), "%d"));
                break;
            case HeartRatePercentageMaxAverage:
                mPropertyValue.setText(mProperty.getStringValue((int) (sc.getSession().getAvgHeartRateMaxPercent() * 100), "%d"));
                break;
            case HeartRatePercentageReserve:
                mPropertyValue.setText(mProperty.getStringValue((int) (sc.getCurrentDataSlice().currentHeartRateReservePercent * 100), "%d"));
                break;
            case HeartRatePercentageReserveAverage:
                mPropertyValue.setText(mProperty.getStringValue((int) (sc.getSession().getAvgHeartRateReservePercent() * 100), "%d"));
                break;
            case HeartRateTimeInZone1: {
                double time = sc.getSession().getTimeInHeartRateZones().get(0);
                mPropertyValue.setText(ViewStyling.timeToStringHMS(time, true));
                break;
            }
            case HeartRateTimeInZone2: {
                double time = sc.getSession().getTimeInHeartRateZones().get(1);
                mPropertyValue.setText(ViewStyling.timeToStringHMS(time, true));
                break;
            }
            case HeartRateTimeInZone3: {
                double time = sc.getSession().getTimeInHeartRateZones().get(2);
                mPropertyValue.setText(ViewStyling.timeToStringHMS(time, true));
                break;
            }
            case HeartRateTimeInZone4: {
                double time = sc.getSession().getTimeInHeartRateZones().get(3);
                mPropertyValue.setText(ViewStyling.timeToStringHMS(time, true));
                break;
            }
            case HeartRateTimeInZone5: {
                double time = sc.getSession().getTimeInHeartRateZones().get(4);
                mPropertyValue.setText(ViewStyling.timeToStringHMS(time, true));
                break;
            }
            case HeartRateZone:
                mPropertyValue.setText(mProperty.getStringValue(sc.getCurrentDataSlice().currentHeartRateZone, "%d"));
                break;
            case KilocaloriesPerMinute:
                break;
            case LapCount: {
                mPropertyValue.setText(mProperty.getStringValue(sc.getSession().laps.size(), "%d"));
                break;
            }
            case LapDistance:
                if (SharedPreferencesInterface.isMetric()) {
                    mPropertyValue.setText(mProperty.getStringValue(sc.getCurrentLap().getDistanceKM(), "%.1f"));
                } else {
                    mPropertyValue.setText(mProperty.getStringValue(Conversions.kph_to_mph(sc.getCurrentLap().getDistanceKM()), "%.1f"));
                }
                break;
            case LapDistancePrevious:
                if (sc.getPreviousLap() == null) {
                    mPropertyValue.setText("---");
                    break;
                }
                if (SharedPreferencesInterface.isMetric()) {
                    mPropertyValue.setText(mProperty.getStringValue(sc.getPreviousLap().getDistanceKM(), "%.1f"));
                } else {
                    mPropertyValue.setText(mProperty.getStringValue(Conversions.kph_to_mph(sc.getPreviousLap().getDistanceKM()), "%.1f"));
                }
                break;
            case LapTime: {
                double time = sc.getDurations().lapDuration;
                mPropertyValue.setText(ViewStyling.timeToStringHMS(time, true));
                break;
            }
            case LapTimeAverage: {
                double time = sc.getSession().getAvgLapTime();
                mPropertyValue.setText(ViewStyling.timeToStringHMS(time, true));
                break;
            }
            case LapTimePrevious: {
                if (sc.getPreviousLap() == null) {
                    mPropertyValue.setText("---");
                    break;
                }
                double time = sc.getPreviousLap().getDuration();
                mPropertyValue.setText(ViewStyling.timeToStringHMS(time, true));
                break;
            }
            case Power:
                mPropertyValue.setText(mProperty.getStringValue(sensorValues.currentPower, "%d"));
                break;
            case Power1mAverage:
                mPropertyValue.setText(mProperty.getStringValue(sc.powerAverageForPreviousTime(60), "%.0f"));
                break;
            case Power20mAverage:
                mPropertyValue.setText(mProperty.getStringValue(sc.powerAverageForPreviousTime(60 * 20), "%.0f"));
                break;
            case Power20sAverage:
                mPropertyValue.setText(mProperty.getStringValue(sc.powerAverageForPreviousTime(20), "%.0f"));
                break;
            case Power5mAverage:
                mPropertyValue.setText(mProperty.getStringValue(sc.powerAverageForPreviousTime(60 * 5), "%.0f"));
                break;
            case Power5sAverage:
                mPropertyValue.setText(mProperty.getStringValue(sc.powerAverageForPreviousTime(5), "%.0f"));
                break;
            case PowerAverage:
                mPropertyValue.setText(mProperty.getStringValue(sc.getSession().getAvgPower(), "%.0f"));
                break;
            case PowerAverageLap:
                mPropertyValue.setText(mProperty.getStringValue(sc.getCurrentLap().getAvgPower(), "%.0f"));
                break;
            case PowerAverageLapPrevious: {
                if (sc.getPreviousLap() == null) {
                    mPropertyValue.setText("---");
                    break;
                }
                mPropertyValue.setText(mProperty.getStringValue(sc.getPreviousLap().getAvgPower(), "%.0f"));
                break;
            }
            case PowerIntensityFactor:
                mPropertyValue.setText(mProperty.getStringValue(sc.getSession().getIntensityFactor(), "%.1f"));
                break;
            case PowerKilojoules:
                mPropertyValue.setText(mProperty.getStringValue(sc.getSession().getKilojoules(), "%.1f"));
                break;
            case PowerMax:
                mPropertyValue.setText(mProperty.getStringValue(sc.getSession().getMaxPower(), "%d"));
                break;
            case PowerNormalized:
                mPropertyValue.setText(mProperty.getStringValue(sc.getSession().getNormalizedPower(), "%.0f"));
                break;
            case PowerNormalizedLap:
                mPropertyValue.setText(mProperty.getStringValue(sc.getCurrentLap().getNormalizedPower(), "%.0f"));
                break;
            case PowerNormalizedLapPrevious: {
                if (sc.getPreviousLap() == null) {
                    mPropertyValue.setText("---");
                    break;
                }
                mPropertyValue.setText(mProperty.getStringValue(sc.getPreviousLap().getNormalizedPower(), "%.0f"));
                break;
            }
            case PowerPercentageFTP:
                mPropertyValue.setText(mProperty.getStringValue((int) (sc.getCurrentDataSlice().currentPowerPercentageFTP * 100), "%d"));
                break;
            case PowerTarget:
                mPropertyValue.setText(mProperty.getStringValue(sc.getIntervalTargetPower(), "%.0f"));
                break;
            case PowerTimeInZone1: {
                double time = sc.getSession().getTimeInPowerZones().get(0);
                mPropertyValue.setText(ViewStyling.timeToStringHMS(time, true));
                break;
            }
            case PowerTimeInZone2: {
                double time = sc.getSession().getTimeInPowerZones().get(1);
                mPropertyValue.setText(ViewStyling.timeToStringHMS(time, true));
                break;
            }
            case PowerTimeInZone3: {
                double time = sc.getSession().getTimeInPowerZones().get(2);
                mPropertyValue.setText(ViewStyling.timeToStringHMS(time, true));
                break;
            }
            case PowerTimeInZone4: {
                double time = sc.getSession().getTimeInPowerZones().get(3);
                mPropertyValue.setText(ViewStyling.timeToStringHMS(time, true));
                break;
            }
            case PowerTimeInZone5: {
                double time = sc.getSession().getTimeInPowerZones().get(4);
                mPropertyValue.setText(ViewStyling.timeToStringHMS(time, true));
                break;
            }
            case PowerTimeInZone6: {
                double time = sc.getSession().getTimeInPowerZones().get(5);
                mPropertyValue.setText(ViewStyling.timeToStringHMS(time, true));
                break;
            }
            case PowerTimeInZone7: {
                double time = sc.getSession().getTimeInPowerZones().get(6);
                mPropertyValue.setText(ViewStyling.timeToStringHMS(time, true));
                break;
            }
            case PowerTSS:
                mPropertyValue.setText(mProperty.getStringValue(sc.getSession().getTrainingStressScore(), "%.1f"));
                break;
            case PowerWattsKilogram:
                mPropertyValue.setText(mProperty.getStringValue(sc.getCurrentDataSlice().currentPowerWattsPerKilogram, "%.0f"));
                break;
            case PowerZone:
                mPropertyValue.setText(mProperty.getStringValue(sc.getCurrentDataSlice().currentPowerZone, "%d"));
                break;
            case SpeedKPH: {
                if (SharedPreferencesInterface.isMetric()) {
                    mPropertyValue.setText(mProperty.getStringValue(sensorValues.currentSpeedKPH, "%.1f"));
                } else {
                    mPropertyValue.setText(mProperty.getStringValue(Conversions.kph_to_mph(sensorValues.currentSpeedKPH), "%.1f"));
                }
                break;
            }
            case SpeedKPHAverage: {
                if (SharedPreferencesInterface.isMetric()) {
                    mPropertyValue.setText(mProperty.getStringValue(sc.getSession().getAvgSpeedKPH(), "%.1f"));
                } else {
                    mPropertyValue.setText(mProperty.getStringValue(Conversions.kph_to_mph(sc.getSession().getAvgSpeedKPH()), "%.1f"));
                }
                break;
            }
            case SpeedKPHAverageLap: {
                if (SharedPreferencesInterface.isMetric()) {
                    mPropertyValue.setText(mProperty.getStringValue(sc.getCurrentLap().getAvgSpeedKPH(), "%.1f"));
                } else {
                    mPropertyValue.setText(mProperty.getStringValue(Conversions.kph_to_mph(sc.getCurrentLap().getAvgSpeedKPH()), "%.1f"));
                }
                break;
            }
            case SpeedKPHAverageLapPrevious: {
                if (sc.getPreviousLap() == null) {
                    break;
                }
                if (SharedPreferencesInterface.isMetric()) {
                    mPropertyValue.setText(mProperty.getStringValue(sc.getPreviousLap().getAvgSpeedKPH(), "%.1f"));
                } else {
                    mPropertyValue.setText(mProperty.getStringValue(Conversions.kph_to_mph(sc.getPreviousLap().getAvgSpeedKPH()), "%.1f"));
                }
                break;
            }
            case SpeedKPHMax: {
                if (SharedPreferencesInterface.isMetric()) {
                    mPropertyValue.setText(mProperty.getStringValue(sc.getSession().getMaxSpeedKPH(), "%.1f"));
                } else {
                    mPropertyValue.setText(mProperty.getStringValue(Conversions.kph_to_mph(sc.getSession().getMaxSpeedKPH()), "%.1f"));
                }
                break;
            }
            case WorkoutDurationToGo: {
                double time = sc.getDurations().workoutTimeRemaining;
                mPropertyValue.setText(ViewStyling.timeToStringHMS(time, true));
                break;
            }
            case WorkoutIntervalDurationToGo: {
                double time = sc.getDurations().intervalTimeRemaining;
                mPropertyValue.setText(ViewStyling.timeToStringHMS(time, true));
                break;
            }
        }
    }

    public TextView getmPropertyValue() {
        return mPropertyValue;
    }
}
