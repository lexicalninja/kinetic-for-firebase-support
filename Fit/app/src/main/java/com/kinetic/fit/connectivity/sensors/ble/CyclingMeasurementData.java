package com.kinetic.fit.connectivity.sensors.ble;

import android.support.annotation.Nullable;

/**
 * Created by Saxton on 7/21/16.
 */
public interface CyclingMeasurementData {
    public double getTimeStamp();

    @Nullable
    public Short getCumulativeCrankRevolutions();

    @Nullable
    public Integer getCumulativeWheelRevolutions();

    @Nullable
    public Short getLastCrankEventTime();

    @Nullable
    public Short getLastWheelEventTime();
}
