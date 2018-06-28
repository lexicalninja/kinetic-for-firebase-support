package com.kinetic.fit.connectivity.sensors.ble;

import android.support.annotation.Nullable;

/**
 * Created by Saxton on 7/14/16.
 */
public class CyclingSerializer {

    public class SensorLocation {
        public int other = 0;
        public int TopOfShoe = 1;
        public int InShoe = 2;
        public int Hip = 3;
        public int FrontWheel = 4;
        public int LeftCrank = 5;
        public int RightCrank = 6;
        public int LeftPedal = 7;
        public int RightPedal = 8;
        public int FrontHub = 9;
        public int RearDropout = 10;
        public int Chainstay = 11;
        public int RearWheel = 12;
        public int RearHub = 13;
        public int Chest = 14;
        public int Spider = 15;
        public int ChainRing = 16;
    }

    public static int readSensorLocation(byte[] bytes) {
        return (int) bytes[0];
    }

    @Nullable
    public static Double calculateWheelKPH(CyclingMeasurementData current,
                                           CyclingMeasurementData previous,
                                           Double wheelCircumferenceCM,
                                           Integer wheelTimeResolution) {
        double CM_PER_KM = 0.00001;
        double MINS_PER_HOUR = 60.0;

        int cwr1 = 0;
        int cwr2 = 0;
        short lwet1 = 0;
        short lwet2 = 0;
        int wheelRevsDelta;
        short wheeltimeDelta = 0;
        double wheelTimeSeconds;
        double wheelRPM;


        if (current.getCumulativeWheelRevolutions() != null) {
            cwr1 = current.getCumulativeWheelRevolutions();
        } else {
            return null;
        }
        if (previous.getCumulativeWheelRevolutions() != null) {
            cwr2 = previous.getCumulativeWheelRevolutions();
        } else {
            return null;
        }
        if (current.getLastWheelEventTime() != null) {
            lwet1 = current.getLastWheelEventTime();
        } else {
            return null;
        }
        if (previous.getLastWheelEventTime() != null) {
            lwet2 = previous.getLastWheelEventTime();
        } else {
            return null;
        }

        wheelRevsDelta = deltaWithRollover(cwr1, cwr2, Integer.MAX_VALUE);
        wheeltimeDelta = (short) deltaWithRollover(lwet1, lwet2, Short.MAX_VALUE);

        wheelTimeSeconds = (double) wheeltimeDelta / (double) wheelTimeResolution;

        if (wheelTimeSeconds > 0.0) {
            wheelRPM = (double) wheelRevsDelta / (wheelTimeSeconds / 60.0);
            return wheelRPM * wheelCircumferenceCM * CM_PER_KM * MINS_PER_HOUR;
        }

        return 0.0;
    }

    @Nullable
    public static Double calculateCrankRPM(CyclingMeasurementData current,
                                           CyclingMeasurementData previous) {
        short ccr1 = 0;
        short ccr2 = 0;
        short lcet1 = 0;
        short lcet2 = 0;
        short crankRevsDelta;
        short crankTimeDelta;
        double crankTimeSeconds;

        if (current.getCumulativeCrankRevolutions() != null) {
            ccr1 = current.getCumulativeCrankRevolutions();
        } else {
            return null;
        }
        if (previous.getCumulativeCrankRevolutions() != null) {
            ccr2 = previous.getCumulativeCrankRevolutions();
        } else {
            return null;
        }
        if (current.getLastCrankEventTime() != null) {
            lcet1 = current.getLastCrankEventTime();
        } else {
            return null;
        }
        if (previous.getLastCrankEventTime() != null) {
            lcet2 = previous.getLastCrankEventTime();
        } else {
            return null;
        }

        crankRevsDelta = (short) deltaWithRollover(ccr1, ccr2, Short.MAX_VALUE);
        crankTimeDelta = (short) deltaWithRollover(lcet1, lcet2, Short.MAX_VALUE);

        crankTimeSeconds = (double) crankTimeDelta / 1024;
        if (crankTimeSeconds > 0) {
            return (double) crankRevsDelta / (crankTimeSeconds / 60);
        }

        return 0.0;
    }

    private static int deltaWithRollover(int now, int last, int max) {
        return last > now ? max - last + now : now - last;
    }

}
