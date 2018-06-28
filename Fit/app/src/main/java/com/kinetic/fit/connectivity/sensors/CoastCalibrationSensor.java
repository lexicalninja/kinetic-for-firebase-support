package com.kinetic.fit.connectivity.sensors;

public interface CoastCalibrationSensor {
    void startCalibration();

    void stopCalibration();

    double getCurrentSpindownTime();

    double getLastSpindownTime();

    double getCalibrationReadySpeedKPH();

    double getCurrentSpeedKPH();

    FITCalibrateCoastState getCurrentState();

    FITCalibrateCoastResult getLatestResult();

    enum FITCalibrateCoastState {
        Unknown,
        Initializing,
        SpeedUp,
        StartCoasting,
        Coasting,
        Complete
    }

    enum FITCalibrateCoastResult {
        Success,
        TooFast,
        TooSlow,
        Middle,
        Unknown,
    }


//    protocol FITCoastCalibrateService: FITCalibrationService, FITSpeedService {
//        func startCalibration() -> Bool
//        func stopCalibration() -> Bool
//        var onCoastState: Signal<FITCalibrateCoastState> { get }
//        var onCoastResult: Signal<(Double, FITCalibrateCoastResult)> { get }
//        var currentSpindownTime: Double { get }
//        var calibrationReadySpeedKPH: Double { get }
//        var onCurrentSpeedKPH: Signal<Double> { get }
//    }
}
