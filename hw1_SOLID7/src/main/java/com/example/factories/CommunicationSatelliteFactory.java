package com.example.factories;
import com.example.enums.SatelliteType;


public class CommunicationSatelliteFactory implements SatelliteFactory {

    @Override
    public Satellite createSatelliteWithParameter(SatelliteParam param) {
        if (!isSatelliteTypeSupported(param.getType())) {
            throw new SpaceOperationException(
                "CommunicationSatelliteFactory не поддерживает тип спутника: " + param.getType()
            );
        }

        if (!(param instanceof CommunicationSatelliteParam)) {
            throw new SpaceOperationException(
                "Ожидался параметр типа CommunicationSatelliteParam, получен: " + param.getClass().getSimpleName()
            );
        }

        CommunicationSatelliteParam comParam = (CommunicationSatelliteParam) param;
        
        // Создаем спутник с кастомной батареей
        EnergySystem energySystem = new EnergySystem.EnergySystemBuilder()
                .initialBattery(comParam.getBatteryLevel())
                .build();

        CommunicationSatellite satellite = new CommunicationSatellite(
            comParam.getName(), 
            comParam.getBandwidth()
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
        return SatelliteType.COMMUNICATION.equals(type);
    }
}
