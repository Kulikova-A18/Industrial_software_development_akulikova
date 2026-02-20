package com.example;

public class Main {
    public static void main(String[] args) {
        System.out.println("ЗАПУСК СИСТЕМЫ УПРАВЛЕНИЯ СПУТНИКОВОЙ ГРУППИРОВКОЙ");
        System.out.println("============================================================\n");

        ConstellationRepository constellationRepository = new ConstellationRepository();
        SpaceOperationCenterService operationCenter = new SpaceOperationCenterService(constellationRepository);

        System.out.println("СОЗДАНИЕ СПЕЦИАЛИЗИРОВАННЫХ СПУТНИКОВ ЧЕРЕЗ ФАБРИКИ:");
        System.out.println("---------------------------------------------");

        CommunicationSatelliteFactory comFactory = new CommunicationSatelliteFactory();
        ImagingSatelliteFactory imgFactory = new ImagingSatelliteFactory();

        CommunicationSatellite comsat1 = (CommunicationSatellite) comFactory.createSatelliteWithParameter("Связь-1",
                500.0);
        CommunicationSatellite comsat2 = (CommunicationSatellite) comFactory.createSatelliteWithParameter("Связь-2",
                1000.0);

        ImagingSatellite imgsat1 = (ImagingSatellite) imgFactory.createSatelliteWithParameter("ДЗЗ-1", 2.5);
        ImagingSatellite imgsat2 = (ImagingSatellite) imgFactory.createSatelliteWithParameter("ДЗЗ-2", 1.0);
        ImagingSatellite imgsat3 = (ImagingSatellite) imgFactory.createHighResolutionSatellite("ДЗЗ-3");

        System.out.println("Создан спутник: " + comsat1.getName() + " (полоса: " + comsat1.getBandwidth() + " Мбит/с)");
        System.out.println("Создан спутник: " + comsat2.getName() + " (полоса: " + comsat2.getBandwidth() + " Мбит/с)");
        System.out.println(
                "Создан спутник: " + imgsat1.getName() + " (разрешение: " + imgsat1.getResolution() + " м/пиксель)");
        System.out.println(
                "Создан спутник: " + imgsat2.getName() + " (разрешение: " + imgsat2.getResolution() + " м/пиксель)");
        System.out.println(
                "Создан спутник: " + imgsat3.getName() + " (разрешение: " + imgsat3.getResolution() + " м/пиксель)");

        System.out.println("\nСОЗДАНИЕ ГРУППИРОВОК ЧЕРЕЗ ФАБРИКУ И BUILDER:");
        System.out.println("---------------------------------------------");

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