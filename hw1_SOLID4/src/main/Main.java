package com.example;

public class Main {
    public static void main(String[] args) {
        System.out.println("ЗАПУСК СИСТЕМЫ УПРАВЛЕНИЯ СПУТНИКОВОЙ ГРУППИРОВКОЙ");
        System.out.println("============================================================\n");

        // Создаем компоненты
        ConstellationRepository constellationRepository = new ConstellationRepository();
        SpaceOperationCenterService operationCenter = new SpaceOperationCenterService(constellationRepository);

        System.out.println("СОЗДАНИЕ СПЕЦИАЛИЗИРОВАННЫХ СПУТНИКОВ:");
        System.out.println("---------------------------------------------");

        CommunicationSatellite comsat1 = new CommunicationSatellite("Связь-1", 500.0);
        CommunicationSatellite comsat2 = new CommunicationSatellite("Связь-2", 1000.0);
        ImagingSatellite imgsat1 = new ImagingSatellite("ДЗЗ-1", 2.5);
        ImagingSatellite imgsat2 = new ImagingSatellite("ДЗЗ-2", 1.0);
        ImagingSatellite imgsat3 = new ImagingSatellite("ДЗЗ-3", 0.5);

        System.out.println("Создан спутник: " + comsat1.getName() + " (полоса: " + comsat1.getBandwidth() + " Мбит/с)");
        System.out.println("Создан спутник: " + comsat2.getName() + " (полоса: " + comsat2.getBandwidth() + " Мбит/с)");
        System.out.println(
                "Создан спутник: " + imgsat1.getName() + " (разрешение: " + imgsat1.getResolution() + " м/пиксель)");
        System.out.println(
                "Создан спутник: " + imgsat2.getName() + " (разрешение: " + imgsat2.getResolution() + " м/пиксель)");
        System.out.println(
                "Создан спутник: " + imgsat3.getName() + " (разрешение: " + imgsat3.getResolution() + " м/пиксель)");

        System.out.println("\nСОЗДАНИЕ ГРУППИРОВОК:");
        System.out.println("---------------------------------------------");

        operationCenter.createAndSaveConstellation("Орбита-1");
        operationCenter.createAndSaveConstellation("Орбита-2");

        System.out.println("\nДОБАВЛЕНИЕ СПУТНИКОВ В ГРУППИРОВКИ:");
        System.out.println("---------------------------------------------");

        operationCenter.addSatelliteToConstellation("Орбита-1", comsat1);
        operationCenter.addSatelliteToConstellation("Орбита-1", imgsat1);
        operationCenter.addSatelliteToConstellation("Орбита-1", imgsat2);
        operationCenter.addSatelliteToConstellation("Орбита-2", comsat2);
        operationCenter.addSatelliteToConstellation("Орбита-2", imgsat3);

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