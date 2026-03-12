package com.example.factories;
import com.example.enums.SatelliteType;


public class ImagingSatelliteFactory implements SatelliteFactory {

    @Override
    public Satellite createSatelliteWithParameter(SatelliteParam param) {
        if (!isSatelliteTypeSupported(param.getType())) {
            throw new SpaceOperationException(
                "ImagingSatelliteFactory не поддерживает тип спутника: " + param.getType()
            );
        }

        if (!(param instanceof ImagingSatelliteParam)) {
            throw new SpaceOperationException(
                "Ожидался параметр типа ImagingSatelliteParam, получен: " + param.getClass().getSimpleName()
            );
        }

        ImagingSatelliteParam imgParam = (ImagingSatelliteParam) param;
        
        // Создаем спутник с кастомной батареей
        EnergySystem energySystem = new EnergySystem.EnergySystemBuilder()
                .initialBattery(imgParam.getBatteryLevel())
                .build();

        ImagingSatellite satellite = new ImagingSatellite(
            imgParam.getName(), 
            imgParam.getResolution()
        );
        
        // Используем рефлексию для установки кастомной EnergySystem
        try {
            java.lang.reflect.Field energyField = Satellite.class.getDeclaredField("energy");
            energyField.setAccessible(true);
            energyField.set(satellite, energySystem);
        } catch (Exception e) {
            throw new SpaceOperationException("Не удалось установить кастомную EnergySystem", e);
        }
        
        return satellite;
    }

    @Override
    public boolean isSatelliteTypeSupported(SatelliteType type) {
        return SatelliteType.IMAGE.equals(type);
    }

    // Дополнительные удобные методы
    public Satellite createHighResolutionSatellite(String name, double batteryLevel) {
        return createSatelliteWithParameter(
            new ImagingSatelliteParam(name, batteryLevel, 0.5)
        );
    }

    public Satellite createLowResolutionSatellite(String name, double batteryLevel) {
        return createSatelliteWithParameter(
            new ImagingSatelliteParam(name, batteryLevel, 5.0)
        );
    }
}
