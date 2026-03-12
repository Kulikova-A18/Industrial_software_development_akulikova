package com.example.factories;
import com.example.enums.SatelliteType;


public interface SatelliteFactory {
    Satellite createSatelliteWithParameter(SatelliteParam param);
    boolean isSatelliteTypeSupported(SatelliteType type);
}
