import java.util.HashMap;
import java.util.Map;

public class ConstellationRepository {
    private final Map<String, SatelliteConstellation> constellations = new HashMap<>();

    public void save(SatelliteConstellation constellation) {
        constellations.put(constellation.getConstellationName(), constellation);
        System.out.println("Добавлен спутник " + constellation.getConstellationName() + " в группировку "
                + constellation.getConstellationName());
    }

    public SatelliteConstellation findByName(String name) {
        return constellations.get(name);
    }

    public Map<String, SatelliteConstellation> findAll() {
        return new HashMap<>(constellations);
    }

    public boolean existsByName(String name) {
        return constellations.containsKey(name);
    }
}