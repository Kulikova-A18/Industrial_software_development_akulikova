package com.example.params;
import com.example.enums.SatelliteType;


public abstract class SatelliteParam {
    private final SatelliteType type;
    private final String name;
    private final double batteryLevel;

    public SatelliteParam(SatelliteType type, String name, double batteryLevel) {
        this.type = type;
        this.name = name;
        this.batteryLevel = batteryLevel;
    }

    public SatelliteType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public double getBatteryLevel() {
        return batteryLevel;
    }
}
