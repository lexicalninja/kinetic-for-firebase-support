package com.kinetic.fit.connectivity.sensors;


public interface DynamicResistanceSensor {

    void setResistanceErg(int targetWatts);
    void setResistanceBrake(float percent);
    void setResistanceFluid(int level);

}
