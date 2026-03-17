package com.example.params;
import com.example.enums.SatelliteType;


public class CommunicationSatelliteParam extends SatelliteParam {
    private final double bandwidth;

    public CommunicationSatelliteParam(String name, double batteryLevel, double bandwidth) {
        super(SatelliteType.COMMUNICATION, name, batteryLevel);
        this.bandwidth = bandwidth;
    }

    public double getBandwidth() {
        return bandwidth;
    }
}
