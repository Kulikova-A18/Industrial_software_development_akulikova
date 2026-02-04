import java.util.ArrayList;
import java.util.List;

public class SatelliteConstellation {
    private final List<Satellite> satellites;

    public SatelliteConstellation() {
        this.satellites = new ArrayList<>();
    }

    public void addSatellite(Satellite satellite) {
        satellites.add(satellite);
        System.out.println("Спутник " + satellite.getName() + " добавлен в группировку");
    }

    public void activateAll() {
        System.out.println("Активация всей группировки...");
        for (Satellite satellite : satellites) {
            satellite.activate();
        }
    }

    public void executeAllMissions() {
        System.out.println("\nВыполнение всех миссий...");
        for (Satellite satellite : satellites) {
            satellite.executeMission();
        }
    }

    public void showStatus() {
        System.out.println("\n=== Статус группировки спутников ===");
        for (Satellite satellite : satellites) {
            System.out.println(satellite.getName() + " (" + satellite.getType() + "): " +
                    (satellite.isOperational() ? "ОПЕРАЦИОНЕН" : "НЕ ОПЕРАЦИОНЕН"));
        }
    }

    public void rechargeAll() {
        System.out.println("Зарядка всех спутников...");
        for (Satellite satellite : satellites) {
            satellite.deactivate();
            satellite.recharge(50);
        }
    }
}