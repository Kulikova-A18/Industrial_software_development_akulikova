package com.example;

public class ImagingSatelliteFactory extends SatelliteFactory {

    private static final double DEFAULT_BATTERY_LEVEL = 0.9;
    private static final double DEFAULT_RESOLUTION = 1.0;

    @Override
    public Satellite createSatellite(String name) {
        return new ImagingSatellite(name, DEFAULT_RESOLUTION);
    }

    @Override
    public Satellite createSatelliteWithParameter(String name, double resolution) {
        return new ImagingSatellite(name, resolution);
    }

    public Satellite createHighResolutionSatellite(String name) {
        return new ImagingSatellite(name, 0.5);
    }

    public Satellite createLowResolutionSatellite(String name) {
        return new ImagingSatellite(name, 5.0);
    }
}