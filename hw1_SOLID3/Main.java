public class Main {
    public static void main(String[] args) {
        System.out.println("ЗАПУСК СИСТЕМЫ УПРАВЛЕНИЯ СПУТНИКОВОЙ ГРУППИРОВКОЙ");
        System.out.println("============================================================\n");

        // Создаем компоненты вручную (без DI контейнера)
        ConstellationRepository constellationRepository = new ConstellationRepository();
        SpaceOperationCenterService operationCenter = new SpaceOperationCenterService(constellationRepository);

        System.out.println("СОЗДАНИЕ СПЕЦИАЛИЗИРОВАННЫХ СПУТНИКОВ:");
        System.out.println("---------------------------------------------");

        CommunicationSatellite comsat1 = new CommunicationSatellite("Связь-1", 500.0);
        CommunicationSatellite comsat2 = new CommunicationSatellite("Связь-2", 1000.0);
        ImagingSatellite imgsat1 = new ImagingSatellite("ДЗЗ-1", 2.5);
        ImagingSatellite imgsat2 = new ImagingSatellite("ДЗЗ-2", 1.0);
        ImagingSatellite imgsat3 = new ImagingSatellite("ДЗЗ-3", 0.5);

        System.out.println("Создан спутник: " + comsat1.getName() + " (" + comsat1.getBandwidth() + ")");
        System.out.println("Создан спутник: " + comsat2.getName() + " (" + comsat2.getBandwidth() + ")");
        System.out.println("Создан спутник: " + imgsat1.getName() + " (" + imgsat1.getResolution() + ")");
        System.out.println("Создан спутник: " + imgsat2.getName() + " (" + imgsat2.getResolution() + ")");
        System.out.println("Создан спутник: " + imgsat3.getName() + " (" + imgsat3.getResolution() + ")");

        System.out.println("---------------------------------------------");

        operationCenter.createAndSaveConstellation("Орбита-1");
        System.out.println("Создана спутниковая группировка: Орбита-1");
        System.out.println("Сохранена группировка: Орбита-1");

        operationCenter.createAndSaveConstellation("Орбита-2");
        System.out.println("Создана спутниковая группировка: Орбита-2");
        System.out.println("Сохранена группировка: Орбита-2");

        System.out.println("---------------------------------------------\n");

        System.out.println("ДОБАВЛЕНИЕ СПУТНИКОВ:");
        operationCenter.addSatelliteToConstellation("Орбита-1", comsat1);
        System.out.println(comsat1.getName() + " добавлен в группировку 'Орбита-1'");
        operationCenter.addSatelliteToConstellation("Орбита-1", imgsat1);
        System.out.println(imgsat1.getName() + " добавлен в группировку 'Орбита-1'");
        operationCenter.addSatelliteToConstellation("Орбита-1", imgsat2);
        System.out.println(imgsat2.getName() + " добавлен в группировку 'Орбита-1'");
        operationCenter.addSatelliteToConstellation("Орбита-2", comsat2);
        System.out.println(comsat2.getName() + " добавлен в группировку 'Орбита-2'");
        operationCenter.addSatelliteToConstellation("Орбита-2", imgsat3);
        System.out.println(imgsat3.getName() + " добавлен в группировку 'Орбита-2'");
        System.out.println("-----------------------------------\n");

        System.out.println("=== АКТИВАЦИЯ СПУТНИКОВ В ГРУППИРОВКЕ: Орбита-1 ===");
        operationCenter.activateAllSatellites("Орбита-1");
        System.out.println("\n=== ВЫПОЛНЕНИЕ МИССИЙ ДЛЯ ГРУППИРОВКИ: Орбита-1 ===");
        operationCenter.executeConstellationMission("Орбита-1");
        System.out.println("\n=== СТАТУС ГРУППИРОВКИ: Орбита-1 ===");
        operationCenter.showConstellationStatus("Орбита-1");

        System.out.println("\nВСЕ ГРУППИРОВКИ В РЕПОЗИТОРИИ:");
        operationCenter.printAllConstellations();
    }
}