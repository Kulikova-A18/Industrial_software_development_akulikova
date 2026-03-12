package com.example;


import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        System.out.println("ЗАПУСК СИСТЕМЫ УПРАВЛЕНИЯ СПУТНИКОВОЙ ГРУППИРОВКОЙ");
        System.out.println("============================================================\n");

        // Создаем фабрики и сервис
        CommunicationSatelliteFactory comFactory = new CommunicationSatelliteFactory();
        ImagingSatelliteFactory imgFactory = new ImagingSatelliteFactory();
        
        SatelliteService satelliteService = new SatelliteServiceImpl(
            Arrays.asList(comFactory, imgFactory)
        );

        ConstellationRepository constellationRepository = new ConstellationRepository();
        SpaceOperationCenterService operationCenter = new SpaceOperationCenterService(constellationRepository);

        System.out.println("СОЗДАНИЕ СПЕЦИАЛИЗИРОВАННЫХ СПУТНИКОВ ЧЕРЕЗ СЕРВИС:");
        System.out.println("---------------------------------------------");

        // Создаем спутники через единый сервис
        Satellite comsat1 = satelliteService.createSatellite(
            new CommunicationSatelliteParam("Связь-1", 0.85, 500.0)
        );
        
        Satellite comsat2 = satelliteService.createSatellite(
            new CommunicationSatelliteParam("Связь-2", 0.9, 1000.0)
        );

        Satellite imgsat1 = satelliteService.createSatellite(
            new ImagingSatelliteParam("ДЗЗ-1", 0.9, 2.5)
        );
        
        Satellite imgsat2 = satelliteService.createSatellite(
            new ImagingSatelliteParam("ДЗЗ-2", 0.95, 1.0)
        );

        // Используем специализированные методы фабрик
        Satellite imgsat3 = imgFactory.createHighResolutionSatellite("ДЗЗ-3", 0.88);

        System.out.println("Создан спутник: " + comsat1.getName() + 
            " (полоса: " + ((CommunicationSatellite) comsat1).getBandwidth() + " Мбит/с, батарея: " + 
            comsat1.getEnergy().getBatteryLevel() + ")");
        
        System.out.println("Создан спутник: " + comsat2.getName() + 
            " (полоса: " + ((CommunicationSatellite) comsat2).getBandwidth() + " Мбит/с, батарея: " + 
            comsat2.getEnergy().getBatteryLevel() + ")");
        
        System.out.println("Создан спутник: " + imgsat1.getName() + 
            " (разрешение: " + ((ImagingSatellite) imgsat1).getResolution() + " м/пиксель, батарея: " + 
            imgsat1.getEnergy().getBatteryLevel() + ")");
        
        System.out.println("Создан спутник: " + imgsat2.getName() + 
            " (разрешение: " + ((ImagingSatellite) imgsat2).getResolution() + " м/пиксель, батарея: " + 
            imgsat2.getEnergy().getBatteryLevel() + ")");
        
        System.out.println("Создан спутник: " + imgsat3.getName() + 
            " (разрешение: " + ((ImagingSatellite) imgsat3).getResolution() + " м/пиксель, батарея: " + 
            imgsat3.getEnergy().getBatteryLevel() + ")");

        System.out.println("\nСОЗДАНИЕ ГРУППИРОВОК ЧЕРЕЗ ФАБРИКУ И BUILDER:");
        System.out.println("---------------------------------------------");

        // Создаем группировки
        SatelliteConstellation constellation1 = SatelliteConstellationFactory.createMixedConstellation(
                "Орбита-1", 1, 500.0, 2, 2.5);

        SatelliteConstellation constellation2 = new SatelliteConstellationFactory.ConstellationBuilder()
                .withName("Орбита-2")
                .addSatellite(comsat2)
                .addSatellite(imgsat3)
                .addCommunicationSatellite("Связь-3", 750.0)
                .addImagingSatellite("ДЗЗ-4", 0.3)
                .build();

        constellationRepository.save(constellation1);
        constellationRepository.save(constellation2);

        System.out.println("\nДЕМОНСТРАЦИЯ РАБОТЫ ENERGY SYSTEM BUILDER:");
        System.out.println("---------------------------------------------");

        EnergySystem standardEnergy = new EnergySystem.EnergySystemBuilder()
                .initialBattery(1.0)
                .build();

        EnergySystem highCapacityEnergy = new EnergySystem.EnergySystemBuilder()
                .withHighCapacity()
                .lowBatteryThreshold(0.15)
                .build();

        EnergySystem lowPowerEnergy = new EnergySystem.EnergySystemBuilder()
                .initialBattery(0.5)
                .withLowPowerMode()
                .minBattery(0.05)
                .build();

        System.out.println("Стандартная EnergySystem: " + standardEnergy);
        System.out.println("Высокоемкая EnergySystem: " + highCapacityEnergy);
        System.out.println("Энергосберегающая EnergySystem: " + lowPowerEnergy);

        System.out.println("\n=== АКТИВАЦИЯ СПУТНИКОВ В ГРУППИРОВКЕ: Орбита-1 ===");
        operationCenter.activateAllSatellites("Орбита-1");

        System.out.println("\n=== ВЫПОЛНЕНИЕ МИССИЙ ДЛЯ ГРУППИРОВКИ: Орбита-1 ===");
        operationCenter.executeConstellationMission("Орбита-1");

        System.out.println("\n=== СТАТУС ГРУППИРОВКИ: Орбита-1 ===");
        operationCenter.showConstellationStatus("Орбита-1");

        System.out.println("\n=== АКТИВАЦИЯ СПУТНИКОВ В ГРУППИРОВКЕ: Орбита-2 ===");
        operationCenter.activateAllSatellites("Орбита-2");

        System.out.println("\n=== ВЫПОЛНЕНИЕ МИССИЙ ДЛЯ ГРУППИРОВКИ: Орбита-2 ===");
        operationCenter.executeConstellationMission("Орбита-2");

        System.out.println("\n=== СТАТУС ГРУППИРОВКИ: Орбита-2 ===");
        operationCenter.showConstellationStatus("Орбита-2");

        System.out.println("\nВСЕ ГРУППИРОВКИ В РЕПОЗИТОРИИ:");
        System.out.println("---------------------------------------------");
        operationCenter.printAllConstellations();
    }
}