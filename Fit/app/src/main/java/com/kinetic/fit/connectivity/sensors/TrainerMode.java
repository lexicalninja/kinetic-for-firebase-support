package com.kinetic.fit.connectivity.sensors;

import java.lang.ref.WeakReference;

public class TrainerMode {

    public enum Mode {
        ERG,
        Fluid,
        Brake,
        Simulation
    }

    private WeakReference<DynamicResistanceSensor> dynamicResistance = new WeakReference<>(null);
    public void setDynamicResistance(DynamicResistanceSensor sensor) {
        if (sensor != null) {
            dynamicResistance = new WeakReference<>(sensor);
        } else {
            dynamicResistance.clear();
        }
        setMode(currentMode);
    }

    private Mode currentMode = Mode.Fluid;

    public boolean setMode(Mode mode) {
        if (modeSupported(mode)) {
            currentMode = mode;
            apply();
            return true;
        } else {
            currentMode = Mode.Fluid;
            apply();
        }
        return false;
    }

    public boolean modeSupported(Mode mode) {
        return true;
    }

    public void apply() {
        DynamicResistanceSensor sensor = dynamicResistance.get();
        if (sensor == null) {
            return;
        }
        switch (currentMode) {
            case ERG:
                sensor.setResistanceErg(targetWatts);
                break;
            case Fluid:
                sensor.setResistanceFluid(fluidLevel);
                break;
            case Brake:
                sensor.setResistanceBrake(brakePercent);
                break;
            case Simulation:
                break;
        }
    }

    private int targetWatts = 100;
    public void setTargetWatts(int targetWatts) {
        this.targetWatts = targetWatts;
        if (currentMode == Mode.ERG) {
            apply();
        }
    }

    private int fluidLevel = 0;
    public void setFluidLevel(int fluidLevel) {
        this.fluidLevel = fluidLevel;
        if (currentMode == Mode.Fluid) {
            apply();
        }
    }

    private float brakePercent = 0;
    public void setBrakePercent(float brakePercent) {
        this.brakePercent =  Math.min(1, Math.max(0, brakePercent));
        if (currentMode == Mode.Brake) {
            apply();
        }
    }

}
