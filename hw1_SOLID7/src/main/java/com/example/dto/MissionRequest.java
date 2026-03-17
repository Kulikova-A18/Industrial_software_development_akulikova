package com.example.dto;

import com.example.enums.MissionType;
import java.util.List;

public class MissionRequest {
    private final String constellationName;
    private final List<String> satelliteNames;
    private final MissionType missionType;
    private final int repeatCount;

    public MissionRequest(String constellationName, List<String> satelliteNames,
            MissionType missionType, int repeatCount) {
        this.constellationName = constellationName;
        this.satelliteNames = satelliteNames;
        this.missionType = missionType;
        this.repeatCount = repeatCount;
    }

    public String getConstellationName() {
        return constellationName;
    }

    public List<String> getSatelliteNames() {
        return satelliteNames;
    }

    public MissionType getMissionType() {
        return missionType;
    }

    public int getRepeatCount() {
        return repeatCount;
    }
}