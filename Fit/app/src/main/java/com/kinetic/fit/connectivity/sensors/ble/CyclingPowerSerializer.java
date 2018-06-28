package com.kinetic.fit.connectivity.sensors.ble;

import android.support.annotation.Nullable;

/**
 * Created by Saxton on 7/27/16.
 */
public class CyclingPowerSerializer {
    public class MeasurementFlags {
        public static final int pedalPowerBalancePresent = (1 << 0);
        public static final int AccumulatedTorquePresent = (1 << 2);
        public static final int WheelRevolutionDataPresent = (1 << 4);
        public static final int CrankRevolutionDataPresent = (1 << 5);
        public static final int ExtremeForceMagnitudesPresent = (1 << 6);
        public static final int ExtremeTorqueMagnitudesPresent = (1 << 7);
        public static final int ExtremeAnglesPresent = (1 << 8);
        public static final int TopDeadSpotAnglePresent = (1 << 9);
        public static final int BottomDeadSpotAnglePresent = (1 << 10);
        public static final int AccumulatedEnergyPresent = (1 << 11);
        public static final int OffsetCompensationIndicator = (1 << 12);
    }

    public class Features {

        int rawFeatures;

        public int PedalPowerBalanceSupported = (1 << 0);
        public int AccumulatedTorqueSupported = (1 << 1);
        public int WheelRevolutionDataSupported = (1 << 2);
        public int CrankRevolutionDataSupported = (1 << 3);
        public int ExtremeMagnitudesSupported = (1 << 4);
        public int ExtremeAnglesSupported = (1 << 5);
        public int TopAndBottomDeadSpotAnglesSupported = (1 << 6);
        public int AccumulatedEnergySupported = (1 << 7);
        public int OffsetCompensationIndicatorSupported = (1 << 8);
        public int OffsetCompensationSupported = (1 << 9);
        public int ContentMaskingSupported = (1 << 10);
        public int MultipleSensorLocationsSupported = (1 << 11);
        public int CrankLengthAdjustmentSupported = (1 << 12);
        public int ChainLengthAdjustmentSupported = (1 << 13);
        public int ChainWeightAdjustmentSupported = (1 << 14);
        public int SpanLengthAdjustmentSupported = (1 << 15);
        public int SensorMeasurementContext = (1 << 16);
        public int InstantaneousMeasurementDirectionSupported = (1 << 17);
        public int FactoryCalibrationDateSupported = (1 << 18);

        public Features(int rawFeatures){
            this.rawFeatures = rawFeatures;
        }

        public boolean isWheelRevolutionDataSupported(){
            return ((rawFeatures & WheelRevolutionDataSupported) == WheelRevolutionDataSupported);
        }

        public boolean isCrankRevolutionDataSupported(){
            return ((rawFeatures & CrankRevolutionDataSupported) == CrankRevolutionDataSupported);
        }
    }

    public class MeasurementData implements CyclingMeasurementData {
        public double timeStamp = 0;
        public short instantaneousPower = 0;
        public
        @Nullable
        Byte pedalPowerBalance;
        public
        @Nullable
        boolean pedalPowerBalanceReference;
        public
        @Nullable
        Short accumulatedTorque;

        public double timestamp = 0;

        public
        @Nullable
        Integer cumulativeWheelRevolutions;
        public
        @Nullable
        Short lastWheelEventTime;
        public
        @Nullable
        Short cumulativeCrankRevolutions;
        public
        @Nullable
        Short lastCrankEventTime;

        public
        @Nullable
        Short maximumForceMagnitude;
        public
        @Nullable
        Short minimumForceMagnitude;
        public
        @Nullable
        Short maximumTorqueMagnitude;
        public
        @Nullable
        Short minimumTorqueMagnitude;
        public
        @Nullable
        Short maximumAngle;
        public
        @Nullable
        Short minimumAngle;
        public
        @Nullable
        Short topDeadSpotAngle;
        public
        @Nullable
        Short bottomDeadSpotAngle;
        public
        @Nullable
        Short accumulatedEnergy;

        @Override
        @Nullable
        public Integer getCumulativeWheelRevolutions() {
            return cumulativeWheelRevolutions;
        }

        @Override
        @Nullable
        public Short getLastWheelEventTime() {
            return lastWheelEventTime;
        }

        @Override
        @Nullable
        public Short getCumulativeCrankRevolutions() {
            return cumulativeCrankRevolutions;
        }

        @Override
        @Nullable
        public Short getLastCrankEventTime() {
            return lastCrankEventTime;
        }

        @Override
        public double getTimeStamp() {
            return timeStamp;
        }
    }

    public static Features readFeatures(byte[] bytes) {
        int rawFeatures = ((bytes[0] & 0xFFFF)) | ((bytes[1] & 0xFFFF)) << 8 | ((bytes[2] & 0xFFFF)) << 16 | ((bytes[3] & 0xFFFF) << 24);
        return new CyclingPowerSerializer().new Features(rawFeatures);
    }

    public static MeasurementData readMeasurement(byte[] bytes){
        MeasurementData measurement = new CyclingPowerSerializer().new MeasurementData();

        int index = 0;
        byte rawFlags = bytes[index++];

        measurement.instantaneousPower = (short)(((short)(bytes[index++] & 0xFF)) |
                ((short)bytes[index++] & 0xFF));

        if((rawFlags & MeasurementFlags.pedalPowerBalancePresent) == MeasurementFlags.pedalPowerBalancePresent){
            measurement.pedalPowerBalance = bytes[index++];
            measurement.pedalPowerBalanceReference = (rawFlags & 0x2) == 0x2;
        }

        if((rawFlags & MeasurementFlags.AccumulatedTorquePresent) == MeasurementFlags.AccumulatedTorquePresent){
            measurement.accumulatedTorque = (short)(((short)bytes[index++] & 0xFF) |
                    ((short)(bytes[index++] &0xFF) << 8));
        }

        if((rawFlags & MeasurementFlags.WheelRevolutionDataPresent) == MeasurementFlags.WheelRevolutionDataPresent){
            measurement.cumulativeWheelRevolutions = (((bytes[index++] & 0xFFF)) |
                    ((bytes[index++] & 0xFFFF) << 8) | ((bytes[index++] & 0xFFFF) << 16) |
                    ((bytes[index++] & 0xFFFF) << 24));
            measurement.lastWheelEventTime = (short)((bytes[index++] &0xFF) |
                    ((bytes[index++] &0xFF) <<8));
        }

        if((rawFlags & MeasurementFlags.CrankRevolutionDataPresent) == MeasurementFlags.CrankRevolutionDataPresent){
            measurement.cumulativeCrankRevolutions = (short)((bytes[index++] & 0xFF) |
                    ((bytes[index++] & 0xFF) << 8));
            measurement.lastCrankEventTime = (short)((bytes[index++] & 0xFF) |
                    ((bytes[index++] & 0xFF) << 8));
        }

        if((rawFlags & MeasurementFlags.ExtremeForceMagnitudesPresent) == MeasurementFlags.ExtremeForceMagnitudesPresent){
            measurement.maximumForceMagnitude = (short)((bytes[index++] & 0xFF) |
                    ((bytes[index++] & 0xFF) << 8));
            measurement.minimumForceMagnitude = (short)((bytes[index++] & 0xFF) |
                    ((bytes[index++] & 0xFF) << 8));
        }

        if((rawFlags & MeasurementFlags.ExtremeTorqueMagnitudesPresent) == MeasurementFlags.ExtremeTorqueMagnitudesPresent){
            measurement.maximumTorqueMagnitude = (short)((bytes[index++] & 0xFF) |
                    ((bytes[index++] & 0xFF) << 8));
            measurement.minimumTorqueMagnitude = (short)((bytes[index++] & 0xFF) |
                    ((bytes[index++] & 0xFF) << 8));
        }

        if((rawFlags & MeasurementFlags.ExtremeAnglesPresent) == MeasurementFlags.ExtremeAnglesPresent){
            measurement.minimumAngle = (short)((bytes[index++] & 0xFF) |
                    ((bytes[index] & 0xF0) << 4));
            measurement.maximumAngle = (short)((bytes[index++] & 0xFF) |
                    ((bytes[index++] & 0xFF) << 4));
        }

        if((rawFlags & MeasurementFlags.TopDeadSpotAnglePresent) == MeasurementFlags.TopDeadSpotAnglePresent){
            measurement.topDeadSpotAngle = (short)((bytes[index++] & 0xFF) |
                    ((bytes[index++] & 0xFF) << 8));
        }

        if((rawFlags & MeasurementFlags.BottomDeadSpotAnglePresent) == MeasurementFlags.BottomDeadSpotAnglePresent){
            measurement.bottomDeadSpotAngle = (short)((bytes[index++] & 0xFF) |
                    ((bytes[index++] & 0xFF) << 8));
        }

        if((rawFlags & MeasurementFlags.AccumulatedEnergyPresent) == MeasurementFlags.AccumulatedEnergyPresent){
            measurement.accumulatedEnergy = (short)((bytes[index++] & 0xFF) |
                    ((bytes[index++] & 0xFF) << 8));
        }

        measurement.timeStamp = System.currentTimeMillis();

        return measurement;
    }



}