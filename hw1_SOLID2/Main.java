public class Main {
    public static void main(String[] args) {
        System.out.println("=== Система управления спутниковой группировкой ===\n");

        CommunicationSatellite comsat1 = new CommunicationSatellite("КомСат-1");
        CommunicationSatellite comsat2 = new CommunicationSatellite("КомСат-2");
        ImagingSatellite imgsat1 = new ImagingSatellite("ДЗЗ-1");
        ImagingSatellite imgsat2 = new ImagingSatellite("ДЗЗ-2");

        SatelliteConstellation constellation = new SatelliteConstellation();
        constellation.addSatellite(comsat1);
        constellation.addSatellite(comsat2);
        constellation.addSatellite(imgsat1);
        constellation.addSatellite(imgsat2);

        constellation.showStatus();

        constellation.activateAll();
        constellation.executeAllMissions();

        constellation.showStatus();

        System.out.println("\n=== Демонстрация специфичных методов ===");
        comsat1.establishConnection();
        imgsat1.calibrateSensors();

        System.out.println("\n=== Перезарядка группировки ===");
        constellation.rechargeAll();
        constellation.activateAll();
        constellation.executeAllMissions();

        constellation.showStatus();

        System.out.println("\n=== Демонстрация завершена ===");
    }
}