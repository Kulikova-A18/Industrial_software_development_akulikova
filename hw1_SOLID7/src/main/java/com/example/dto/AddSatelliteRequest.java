package com.example.dto;

import com.example.enums.SatelliteType;
import java.util.List;

public class AddSatelliteRequest {
    private final String constellationName;
    private final List<SatelliteSpec> satellites;

    public AddSatelliteRequest(String constellationName, List<SatelliteSpec> satellites) {
        this.constellationName = constellationName;
        this.satellites = satellites;
    }

    public String getConstellationName() {
        return constellationName;
    }

    public List<SatelliteSpec> getSatellites() {
        return satellites;
    }

    public static class SatelliteSpec {
        private final SatelliteType type;
        private final String name;
        private final double batteryLevel;
        private final double specialParam;

        public SatelliteSpec(SatelliteType type, String name, double batteryLevel, double specialParam) {
            this.type = type;
            this.name = name;
            this.batteryLevel = batteryLevel;
            this.specialParam = specialParam;
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

        public double getSpecialParam() {
            return specialParam;
        }
    }
}