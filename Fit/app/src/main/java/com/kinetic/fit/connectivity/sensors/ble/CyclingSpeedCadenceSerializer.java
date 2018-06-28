package com.kinetic.fit.connectivity.sensors.ble;

import android.support.annotation.Nullable;

/**
 * Created by Saxton on 7/14/16.
 */
public class CyclingSpeedCadenceSerializer {

    public class MeasurementFlags {

        final static byte wheelRevolutionDataPresent = 0x1;
        final static byte crankRevolutionDataPresent = 0x2;
    }

    public class Features {

        private int rawFeatures;

        private final static byte wheelRevolutionDataSupported = 0x1;
        private final static byte crankRevolutionDataSupported = 0x2;
        private final static byte multipleSensorLocationsSupported = 0x4;

        public Features(int rawFeatures) {
            this.rawFeatures = rawFeatures;
        }

        public boolean isWheelRevolutionDataSupported(){
            return ((rawFeatures & wheelRevolutionDataSupported) == wheelRevolutionDataSupported);
        }

        public boolean isCrankRevolutionDataSupported(){
            return ((rawFeatures & crankRevolutionDataSupported) == crankRevolutionDataSupported);
        }

        public boolean isMultipleSensorLocationsSupported(){
            return ((rawFeatures & multipleSensorLocationsSupported) == multipleSensorLocationsSupported);
        }
    }

    public class MeasurementData implements CyclingMeasurementData {
        public double timestamp = 0;
        @Nullable
        public Integer cumulativeWheelRevolutions;
        @Nullable
        public Short lastWheelEventTime;
        @Nullable
        public Short cumulativeCrankRevolutions;
        @Nullable
        public Short lastCrankEventTime;

        public MeasurementData() {
            super();
        }

        @Override
        public double getTimeStamp() {
            return this.timestamp;
        }

        @Override
        @Nullable
        public Short getCumulativeCrankRevolutions() {
            return this.cumulativeCrankRevolutions;
        }

        @Override
        @Nullable
        public Integer getCumulativeWheelRevolutions() {
            return cumulativeWheelRevolutions;
        }

        @Override
        @Nullable
        public Short getLastCrankEventTime() {
            return lastCrankEventTime;
        }

        @Override
        @Nullable
        public Short getLastWheelEventTime() {
            return lastWheelEventTime;
        }
    }

    public static Features readFeatures(byte[] bytes) {
        short rawFeatures = (short) ((bytes[0] & 0xFF) | (bytes[1] | 0xFF) << 8);
        return new CyclingSpeedCadenceSerializer().new Features(rawFeatures);
    }

    public static MeasurementData readMeasurement(byte[] bytes) {
        MeasurementData measurement = new CyclingSpeedCadenceSerializer().new MeasurementData();

        int index = 0;
        byte rawFlags = bytes[index++];

        if ((rawFlags & MeasurementFlags.wheelRevolutionDataPresent) == MeasurementFlags.wheelRevolutionDataPresent) {
            measurement.cumulativeWheelRevolutions = ((int) bytes[index++] & 0xFF)
                    | (((int) bytes[index++] & 0xFF) << 8)
                    | (((int) bytes[index++] & 0xFF) << 16)
                    | (((int) bytes[index++] & 0xFF) << 24);
            measurement.lastWheelEventTime = (short) ((bytes[index++] & 0xff)
                    | ((bytes[index++] & 0xff) << 8));
        }

        if ((rawFlags & MeasurementFlags.crankRevolutionDataPresent) == MeasurementFlags.crankRevolutionDataPresent) {
            measurement.cumulativeCrankRevolutions = (short) (((short) bytes[index++] & 0xFF)
                    | ((short) bytes[index++] & 0xFF) << 8);
            measurement.lastCrankEventTime = (short) (((short) bytes[index++] & 0xFF)
                    | ((short) bytes[index++] & 0xFF) << 8);

        }

        measurement.timestamp = System.currentTimeMillis();

        return measurement;
    }
}
