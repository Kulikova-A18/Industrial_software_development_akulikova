package com.example.services;

import com.example.annotations.Timed;
import com.example.enums.SatelliteType;
import com.example.exceptions.SpaceOperationException;
import com.example.factories.SatelliteFactory;
import com.example.models.Satellite;
import com.example.params.SatelliteParam;
import java.util.List;

public class SatelliteServiceImpl implements SatelliteService {

    private final List<SatelliteFactory> factories;

    public SatelliteServiceImpl(List<SatelliteFactory> factories) {
        this.factories = factories;
    }

    @Override
    @Timed
    public Satellite createSatellite(SatelliteParam param) {
        if (param == null) {
            throw new IllegalArgumentException("Параметр спутника не может быть null");
        }

        SatelliteFactory factory = findSuitableFactory(param.getType());

        if (factory == null) {
            throw new SpaceOperationException(
                    "Не найдена фабрика для типа спутника: " + param.getType());
        }

        return factory.createSatelliteWithParameter(param);
    }

    private SatelliteFactory findSuitableFactory(SatelliteType type) {
        return factories.stream()
                .filter(factory -> factory.isSatelliteTypeSupported(type))
                .findFirst()
                .orElse(null);
    }
}