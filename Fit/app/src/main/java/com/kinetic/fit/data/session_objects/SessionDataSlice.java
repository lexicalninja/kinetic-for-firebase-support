package com.kinetic.fit.data.session_objects;


import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class SessionDataSlice {

    public SessionDataSlice() {
    }

    public double timestamp = -1;

    public int currentPower = -1; // max of 32768, -1 not present
    public int currentHeartRate = -1; // max of 256, 0 = data not present
    public double currentSpeedKPH = -1;
    public double currentCadence = -1; // max of 256, -1 not present

    // All of this data can be rebuilt based on session data slices, the slice timestamp and pz + hr zone thresholds
    public double duration = -1;

    public int currentPowerZone = -1;
    public double currentPowerPercentageFTP = -1;
    public double currentPowerWattsPerKilogram = -1;

    public int currentHeartRateZone = -1;
    public double currentHeartRateReservePercent = -1;
    public double currentHeartRateMaxPercent = -1;

    public double accumulatedDistanceKM = 0;
    public double accumulatedKilojoules = 0;

    public double avgPowerRolling = 0;

    public double distanceKM() {
        if (currentSpeedKPH > 0 && duration > 0) {
            return currentSpeedKPH * (duration / 3600);
        }
        return 0;
    }

    public double kilojoules() {
        if (currentPower > 0 && duration > 0) {
            return (currentPower * duration) / 1000;
        }
        return 0;
    }

//    convenience init(json: Dictionary<String, AnyObject>) {
//        self.init()
//
//        if let value = json["ts"]?.doubleValue {
//            timestamp = value
//        }
//        if let value = json["p"]?.integerValue {
//            currentPower = value
//        }
//        if let value = json["hr"]?.integerValue {
//            currentHeartRate = value
//        }
//        if let value = json["kph"]?.doubleValue {
//            currentSpeedKPH = value
//        }
//        if let value = json["cad"]?.doubleValue {
//            currentCadence = value
//        }
//    }
//
//    func json() -> Dictionary<String, AnyObject> {
//        return [
//        "ts": timestamp,
//                "p": currentPower,
//                "hr": currentHeartRate,
//                "kph": currentSpeedKPH,
//                "cad": currentCadence
//        ]
//    }

    static public byte[] serialize(ArrayList<SessionDataSlice> dataSlices) {
        ByteBuffer bb = ByteBuffer.allocate(2 + (dataSlices.size() * 13));

        // write version header
        // Version 2!
        bb.put((byte) 0x02);

        // 1b (ts 2b or 4b | power 2b | speed 4b | cadence 1b | hr 1b | future | future | future)
        // -> 40kb for a 60 minute session with 1 second data records with all sensors active
        long pts = 0;
        for (SessionDataSlice slice : dataSlices) {
            byte flags = 0x00;

            long ts = (long) Math.floor(slice.timestamp * 1000.0);
            long dts = ts - pts;
            if (dts >= 65536) {
                flags |= 0x80;
            }
            if (slice.currentPower >= 0) {
                flags |= 0x40;
            }
            if (slice.currentSpeedKPH >= 0) {
                flags |= 0x20;
            }
            if (slice.currentCadence >= 0) {
                flags |= 0x10;
            }
            if (slice.currentHeartRate > 0) {
                flags |= 0x08;
            }
            bb.put(flags);

            if (dts >= 65536) {
                bb.putInt((int) dts);
            } else {
                bb.putShort((short) dts);
            }

            if (slice.currentPower >= 0) {
                bb.putShort((short) slice.currentPower);
            }
            if (slice.currentSpeedKPH >= 0) {
                int speedIntegral = (int) Math.floor(slice.currentSpeedKPH);
                bb.put((byte) speedIntegral);

                int speedFraction = (short) Math.floor((slice.currentSpeedKPH - speedIntegral) * 1000);
                bb.putShort((short) speedFraction);
            }
            if (slice.currentCadence >= 0) {
                byte cadence = (byte) Math.round(slice.currentCadence);
                bb.put(cadence);
            }
            if (slice.currentHeartRate > 0) {
                bb.put((byte) slice.currentHeartRate);
            }

            pts = ts;
        }

        // sanity EOF
        bb.put((byte) 0xFF);
        return Arrays.copyOfRange(bb.array(), 0, bb.position());
    }

    static public ArrayList<SessionDataSlice> deserialize(byte[] sensorData) {
        ArrayList<SessionDataSlice> dataSlices = new ArrayList<>();
        if (sensorData == null) {
            return dataSlices;
        }

        ByteBuffer bb = ByteBuffer.wrap(sensorData);
        byte version = bb.get();

        long ts = 0;
        while (bb.position() < bb.limit() - 1) {
            byte flags = bb.get();
            long dts = 0;
            if ((flags & 0x80) == 0x80) {
                dts = bb.getInt();
            } else {
                dts = bb.getShort();
            }
            ts += dts;

            SessionDataSlice sds = new SessionDataSlice();
            sds.timestamp = ts / 1000.0;

            int power = -1;
            int heartRate = -1;
            double speedKPH = -1;
            double cadence = -1;

            if ((flags & 0x40) == 0x40) {
                power = bb.getShort();
            }
            if ((flags & 0x20) == 0x20) {
                if (version == 1) {
                    speedKPH = bb.getFloat();
                } else if (version == 2) {
                    int speedIntegral = 0xFF & bb.get();
                    int speedFraction = 0xFFFF & bb.getShort();
                    speedKPH = speedIntegral + (((double) speedFraction) / 10000);
                }
            }
            if ((flags & 0x10) == 0x10) {
                cadence = 0xFF & bb.get();
            }
            if ((flags & 0x08) == 0x08) {
                heartRate = 0xFF & bb.get();
            }

            sds.currentPower = power;
            sds.currentSpeedKPH = speedKPH;
            sds.currentCadence = cadence;
            sds.currentHeartRate = heartRate;
            dataSlices.add(sds);
        }
        return dataSlices;
    }
}
