import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class representing a generic satellite in orbit.
 * Contains common attributes and behavior for all satellite types.
 */
abstract class Satellite {
    /** The name of the satellite. */
    protected String name;
    
    /** Indicates whether the satellite is currently active. */
    protected boolean isActive;
    
    /** Current battery level as a fraction (0.0 to 1.0). */
    protected double batteryLevel;

    /**
     * Constructs a new Satellite instance.
     *
     * @param name         the name of the satellite
     * @param batteryLevel the initial battery level (0.0–1.0)
     */
    public Satellite(String name, double batteryLevel) {
        this.name = name;
        this.batteryLevel = batteryLevel;
        this.isActive = false;
    }

    /**
     * Attempts to activate the satellite.
     * Activation succeeds only if battery level is greater than 0.2.
     *
     * @return true if activation was successful, false otherwise
     */
    public boolean activate() {
        if (batteryLevel > 0.2) {
            isActive = true;
            return true;
        }
        return false;
    }

    /**
     * Deactivates the satellite if it is currently active.
     */
    public void deactivate() {
        if (isActive) {
            isActive = false;
        }
    }

    /**
     * Consumes a specified amount of battery power.
     * If the battery level drops to 0.2 or below, the satellite is automatically deactivated.
     *
     * @param amount the amount of battery to consume
     */
    protected void consumeBattery(double amount) {
        batteryLevel -= amount;
        if (batteryLevel <= 0.2) {
            deactivate();
        }
    }

    /**
     * Abstract method representing the satellite's mission.
     * Must be implemented by all subclasses.
     */
    protected abstract void performMission();

    /**
     * Returns the name of the satellite.
     *
     * @return the satellite name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the activation status of the satellite.
     *
     * @return true if active, false otherwise
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Returns the current battery level.
     *
     * @return battery level as a fraction (0.0–1.0)
     */
    public double getBatteryLevel() {
        return batteryLevel;
    }
}

/**
 * Represents an Earth observation (imaging) satellite specialized in capturing high-resolution photos.
 */
class ImagingSatellite extends Satellite {
    /** Spatial resolution of captured images in meters per pixel. */
    private double resolution;
    
    /** Number of photos taken by this satellite. */
    private int photosTaken;

    /**
     * Constructs a new ImagingSatellite.
     *
     * @param name         the satellite name
     * @param batteryLevel initial battery level (0.0–1.0)
     * @param resolution   image resolution in meters per pixel
     */
    public ImagingSatellite(String name, double batteryLevel, double resolution) {
        super(name, batteryLevel);
        this.resolution = resolution;
        this.photosTaken = 0;
    }

    /**
     * Captures a photo if the satellite is active, incrementing the photo counter.
     */
    public void takePhoto() {
        if (isActive) {
            photosTaken++;
            System.out.println(name + ": Снимок#" + photosTaken + " сделан!");
        }
    }

    /**
     * Performs the imaging mission: captures a photo and consumes battery.
     * If inactive, prints a warning message.
     */
    @Override
    protected void performMission() {
        if (isActive) {
            System.out.println(name + ": Съемка территории с разрешением " + resolution + " м/пиксель");
            takePhoto();
            consumeBattery(0.08);
        } else {
            System.out.println(name + ": Не может выполнить съемку - не активен");
        }
    }

    /**
     * Returns the image resolution of this satellite.
     *
     * @return resolution in meters per pixel
     */
    public double getResolution() {
        return resolution;
    }

    /**
     * Returns the total number of photos taken.
     *
     * @return number of photos
     */
    public int getPhotosTaken() {
        return photosTaken;
    }

    /**
     * Returns a string representation of this ImagingSatellite.
     *
     * @return formatted string with key attributes
     */
    @Override
    public String toString() {
        return "ImagingSatellite{resolution=" + resolution +
                ", photosTaken=" + photosTaken +
                ", name='" + name + "'" +
                ", isActive=" + isActive +
                ", batteryLevel=" + batteryLevel + "}";
    }
}

/**
 * Represents a communication satellite responsible for data transmission.
 */
class CommunicationSatellite extends Satellite {
    /** Bandwidth capacity in Mbps. */
    private double bandwidth;

    /**
     * Constructs a new CommunicationSatellite.
     *
     * @param name         the satellite name
     * @param batteryLevel initial battery level (0.0–1.0)
     * @param bandwidth    data transmission bandwidth in Mbps
     */
    public CommunicationSatellite(String name, double batteryLevel, double bandwidth) {
        super(name, batteryLevel);
        this.bandwidth = bandwidth;
    }

    /**
     * Sends data over the communication channel if the satellite is active.
     * Prints the amount of data sent to the console.
     *
     * @param dataAmount amount of data in Mbps
     */
    public void sendData(double dataAmount) {
        if (isActive) {
            System.out.println(name + ": Отправил " + dataAmount + " Мбит данных!");
        }
    }

    /**
     * Performs the communication mission: transmits data and consumes battery.
     * If inactive, prints a warning message.
     */
    @Override
    protected void performMission() {
        if (isActive) {
            System.out.println(name + ": Передача данных со скоростью " + bandwidth + " Мбит/с");
            sendData(bandwidth);
            consumeBattery(0.05);
        } else {
            System.out.println(name + ": Не может передать данные - не активен");
        }
    }

    /**
     * Returns the communication bandwidth of this satellite.
     *
     * @return bandwidth in Mbps
     */
    public double getBandwidth() {
        return bandwidth;
    }

    /**
     * Returns a string representation of this CommunicationSatellite.
     *
     * @return formatted string with key attributes
     */
    @Override
    public String toString() {
        return "CommunicationSatellite{bandwidth=" + bandwidth +
                ", name='" + name + "'" +
                ", isActive=" + isActive +
                ", batteryLevel=" + batteryLevel + "}";
    }
}

/**
 * Represents a satellite constellation — a collection of heterogeneous satellites.
 */
class SatelliteConstellation {
    /** Name of the satellite constellation. */
    private String constellationName;
    
    /** List of satellites belonging to this constellation. */
    private List<Satellite> satellites;

    /**
     * Constructs a new SatelliteConstellation with a given name.
     *
     * @param name the name of the constellation
     */
    public SatelliteConstellation(String name) {
        this.constellationName = name;
        this.satellites = new ArrayList<>();
    }

    /**
     * Adds a satellite to the constellation.
     * Uses polymorphism: accepts any subclass of Satellite.
     *
     * @param satellite the satellite to add
     */
    public void addSatellite(Satellite satellite) {
        satellites.add(satellite);
        System.out.println(satellite.getName() + " добавлен в группировку ' " + constellationName + " '");
    }

    /**
     * Executes the mission of every satellite in the constellation.
     * Prints a header before starting.
     */
    public void executeAllMissions() {
        System.out.println("ВЫПОЛНЕНИЕ МИССИЙ ГРУППИРОВКИ " + constellationName.toUpperCase());
        System.out.println("==================================================");
        for (Satellite sat : satellites) {
            sat.performMission();
        }
        System.out.println(satellites);
    }

    /**
     * Returns an unmodifiable copy of the satellite list.
     *
     * @return list of satellites in the constellation
     */
    public List<Satellite> getSatellites() {
        return new ArrayList<>(satellites);
    }

    /**
     * Returns the name of the constellation.
     *
     * @return constellation name
     */
    public String getConstellationName() {
        return constellationName;
    }
}

/**
 * Entry point of the satellite management system.
 * Demonstrates object creation, activation, and mission execution
 * according to the BMSTU assignment specification.
 */
public class Main {
    /**
     * Main method that simulates the satellite constellation workflow.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        System.out.println("ЗАПУСК СИСТЕМЫ УПРАВЛЕНИЯ СПУТНИКОВОЙ ГРУППИРОВКОЙ");
        System.out.println("============================================================");

        System.out.println("СОЗДАНИЕ СПЕЦИАЛИЗИРОВАННЫХ СПУТНИКОВ:");
        System.out.println("---------------------------------------------");

        CommunicationSatellite comm1 = new CommunicationSatellite("Связь-1", 0.85, 500.0);
        CommunicationSatellite comm2 = new CommunicationSatellite("Связь-2", 0.75, 1000.0);
        ImagingSatellite img1 = new ImagingSatellite("ДЗЗ-1", 0.92, 2.5);
        ImagingSatellite img2 = new ImagingSatellite("ДЗЗ-2", 0.45, 1.0);
        ImagingSatellite img3 = new ImagingSatellite("ДЗЗ-3", 0.15, 0.5);

        System.out.println("Создан спутник: " + comm1.getName() + " (заряд: " + (int)(comm1.getBatteryLevel()*100) + "%)");
        System.out.println("Создан спутник: " + comm2.getName() + " (заряд: " + (int)(comm2.getBatteryLevel()*100) + "%)");
        System.out.println("Создан спутник: " + img1.getName() + " (заряд: " + (int)(img1.getBatteryLevel()*100) + "%)");
        System.out.println("Создан спутник: " + img2.getName() + " (заряд: " + (int)(img2.getBatteryLevel()*100) + "%)");
        System.out.println("Создан спутник: " + img3.getName() + " (заряд: " + (int)(img3.getBatteryLevel()*100) + "%)");

        System.out.println("---------------------------------------------");
        SatelliteConstellation constellation = new SatelliteConstellation("RU Basic");
        System.out.println("Создана спутниковая группировка: " + constellation.getConstellationName());
        System.out.println("---------------------------------------------");

        System.out.println("ФОРМИРОВАНИЕ ГРУППИРОВКИ:");
        System.out.println("-----------------------------------");
        constellation.addSatellite(comm1);
        constellation.addSatellite(comm2);
        constellation.addSatellite(img1);
        constellation.addSatellite(img2);
        constellation.addSatellite(img3);
        System.out.println("-----------------------------------");
        System.out.println(constellation.getSatellites());
        System.out.println("-----------------------------------");

        System.out.println("АКТИВАЦИЯ СПУТНИКОВ:");
        System.out.println("-------------------------");
        activateAndReport(comm1);
        activateAndReport(comm2);
        activateAndReport(img1);
        activateAndReport(img2);
        activateAndReport(img3);

        System.out.println();
        constellation.executeAllMissions();
    }

    /**
     * Attempts to activate a satellite and prints the result.
     *
     * @param sat the satellite to activate
     */
    private static void activateAndReport(Satellite sat) {
        if (sat.activate()) {
            System.out.println(sat.getName() + ": Активация успешна");
        } else {
            System.out.println(sat.getName() + ": Ошибка активации (заряд: " +
                    (int)(sat.getBatteryLevel()*100) + "%)");
        }
    }
}