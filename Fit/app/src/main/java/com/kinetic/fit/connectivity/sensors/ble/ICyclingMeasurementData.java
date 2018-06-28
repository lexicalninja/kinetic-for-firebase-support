package com.kinetic.fit.connectivity.sensors.ble;

/**
 * Created by Saxton on 7/14/16.
 */
public interface ICyclingMeasurementData {

    double getTimeStamp();
    Integer getCumulativeWheelRevolutions();
    Short getLastWheelEventTime();
    Short getCumulativeCrankRevolutions();
    Short getLastCrankEventTime();

}
