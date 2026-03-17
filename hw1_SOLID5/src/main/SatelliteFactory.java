package com.example;

public abstract class SatelliteFactory {

    public abstract Satellite createSatellite(String name);

    public abstract Satellite createSatelliteWithParameter(String name, double parameter);

    // Фабричный метод для создания с дополнительной конфигурацией
    public Satellite createConfiguredSatellite(String name, double batteryLevel, double parameter) {
        Satellite satellite = createSatelliteWithParameter(name, parameter);
        // Дополнительная конфигурация при необходимости
        return satellite;
    }
}