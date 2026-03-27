import java.util.ArrayList;
import java.util.List;

public class SatelliteConstellation {
    private final String constellationName;
    private final List<Satellite> satellites;

    public SatelliteConstellation(String constellationName) {
        this.constellationName = constellationName;
        this.satellites = new ArrayList<>();
    }

    public String getConstellationName() {
        return constellationName;
    }

    public void addSatellite(Satellite satellite) {
        satellites.add(satellite);
    }

    public List<Satellite> getSatellites() {
        return new ArrayList<>(satellites);
    }

    public void activateAll() {
        for (Satellite satellite : satellites) {
            satellite.activate();
        }
    }

    public void executeAllMissions() {
        for (Satellite satellite : satellites) {
            satellite.executeMission();
        }
    }

    public void showStatus() {
        System.out.println("Количество спутников: " + satellites.size());
        for (Satellite satellite : satellites) {
            System.out.println(satellite);
        }
    }

    @Override
    public String toString() {
        return "SatelliteConstellation{constellationName='" + constellationName + "', satellites=" + satellites + "}";
    }
}