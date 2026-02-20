package com.example;

public class CommunicationSatelliteFactory extends SatelliteFactory {

    private static final double DEFAULT_BATTERY_LEVEL = 0.85;
    private static final double DEFAULT_BANDWIDTH = 100.0;

    @Override
    public Satellite createSatellite(String name) {
        return new CommunicationSatellite(name, DEFAULT_BANDWIDTH);
    }

    @Override
    public Satellite createSatelliteWithParameter(String name, double bandwidth) {
        return new CommunicationSatellite(name, bandwidth);
    }

    public Satellite createSatelliteWithCustomBattery(String name, double bandwidth, double batteryLevel) {
        EnergySystem customEnergy = new EnergySystem.EnergySystemBuilder()
                .initialBattery(batteryLevel)
                .build();

        CommunicationSatellite satellite = new CommunicationSatellite(name, bandwidth);
        return satellite;
    }
}