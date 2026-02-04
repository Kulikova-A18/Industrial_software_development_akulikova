/**
 * Provides essential identity information about any zoo animal.
 * Decouples reporting, logging, or UI components from concrete animal
 * implementations.
 */
interface AnimalInformationProvider {
    /**
     * Returns the given name of the animal (e.g., "Kesha").
     * 
     * @return String representing the animal's personal name
     */
    String getAnimalName();

    /**
     * Returns the biological species of the animal (e.g., "Parrot").
     * 
     * @return String representing the animal's species
     */
    String getAnimalSpecies();
}

/**
 * Contract for animals that require scheduled feeding.
 */
interface FeedableAnimal {
    /**
     * Delivers a specific type and quantity of food to the animal.
     * 
     * Example usage:
     * 
     * <pre>{@code
     * FeedableAnimal animal = new Parrot("Kesha");
     * animal.feedAnimal("seeds", 50);
     * }</pre>
     * 
     * @param foodType                     The kind of food (e.g., "seeds", "raw
     *                                     meat")
     * @param foodAmountInGramsOrKilograms The amount: interpreted as grams for
     *                                     small animals,
     *                                     kg for large animals
     */
    void feedAnimal(String foodType, int foodAmountInGramsOrKilograms);
}

/**
 * Contract for animals that must undergo health inspections.
 */
interface HealableAnimal {
    /**
     * Conducts a mandatory veterinary health assessment.
     * 
     * Example usage:
     * 
     * <pre>{@code
     * HealableAnimal animal = new Crocodile("Gena");
     * animal.performVeterinaryMedicalCheckup();
     * }</pre>
     */
    void performVeterinaryMedicalCheckup();
}

/**
 * Contract for animals whose enclosures require sanitation.
 */
interface CleanableEnclosure {
    /**
     * Cleans the physical habitat (cage, pool, pen) assigned to the animal.
     * 
     * Example usage:
     * 
     * <pre>{@code
     * CleanableEnclosure enclosure = new Crocodile("Gena");
     * enclosure.cleanAnimalEnclosure();
     * }</pre>
     */
    void cleanAnimalEnclosure();
}

/**
 * Abstract representation of any animal in the Moscow Zoo.
 * Solely responsible for storing and exposing immutable identity data.
 */
abstract class ZooAnimal implements AnimalInformationProvider {
    protected final String animalName;
    protected final String animalSpecies;

    /**
     * Constructs a new ZooAnimal with the specified name and species.
     * 
     * Example usage:
     * 
     * <pre>{@code
     * ZooAnimal animal = new Parrot("Kesha");
     * // or
     * ZooAnimal animal = new Crocodile("Gena");
     * }</pre>
     * 
     * @param animalName    The personal name of the animal
     * @param animalSpecies The biological species of the animal
     */
    public ZooAnimal(String animalName, String animalSpecies) {
        this.animalName = animalName;
        this.animalSpecies = animalSpecies;
    }

    @Override
    public String getAnimalName() {
        return animalName;
    }

    @Override
    public String getAnimalSpecies() {
        return animalSpecies;
    }
}

/**
 * A parrot — a bird that requires feeding and medical checks, but its cage
 * cleaning is handled externally.
 * 
 * Example usage:
 * 
 * <pre>{@code
 * Parrot parrot = new Parrot("Kesha");
 * parrot.feedAnimal("seeds", 50);
 * parrot.performVeterinaryMedicalCheckup();
 * System.out.println(parrot.getAnimalName()); // Output: Kesha
 * }</pre>
 */
class Parrot extends ZooAnimal implements FeedableAnimal, HealableAnimal {
    /**
     * Constructs a new Parrot with the specified name.
     * 
     * @param animalName The personal name of the parrot
     */
    public Parrot(String animalName) {
        super(animalName, "Parrot");
    }

    @Override
    public void feedAnimal(String foodType, int foodAmountInGrams) {
        System.out.println(animalName + " eats " + foodAmountInGrams + " g of " + foodType);
    }

    @Override
    public void performVeterinaryMedicalCheckup() {
        System.out.println("Medical check for parrot " + animalName + ": beak and feathers are in good condition.");
    }
}

/**
 * A crocodile — a large reptile requiring feeding, medical examination, and
 * enclosure sanitation.
 * 
 * Example usage:
 * 
 * <pre>{@code
 * Crocodile crocodile = new Crocodile("Gena");
 * crocodile.feedAnimal("raw meat", 5);
 * crocodile.performVeterinaryMedicalCheckup();
 * crocodile.cleanAnimalEnclosure();
 * System.out.println(crocodile.getAnimalSpecies()); // Output: Crocodile
 * }</pre>
 */
class Crocodile extends ZooAnimal implements FeedableAnimal, HealableAnimal, CleanableEnclosure {
    /**
     * Constructs a new Crocodile with the specified name.
     * 
     * @param animalName The personal name of the crocodile
     */
    public Crocodile(String animalName) {
        super(animalName, "Crocodile");
    }

    @Override
    public void feedAnimal(String foodType, int foodAmountInKilograms) {
        System.out.println(animalName + " ate " + foodAmountInKilograms + " kg of " + foodType);
    }

    @Override
    public void performVeterinaryMedicalCheckup() {
        System.out.println("Medical check for crocodile " + animalName + ": teeth are in good condition.");
    }

    @Override
    public void cleanAnimalEnclosure() {
        System.out.println("Enclosure for crocodile " + animalName + " has been cleaned.");
    }
}

/**
 * Employee responsible for delivering food to animals according to dietary
 * plans.
 * 
 * Example usage:
 * 
 * <pre>{@code
 * ZookeeperEmployee zookeeper = new ZookeeperEmployee();
 * FeedableAnimal parrot = new Parrot("Kesha");
 * zookeeper.feedSpecifiedAnimal(parrot, "seeds", 50);
 * }</pre>
 */
class ZookeeperEmployee {
    /**
     * Feeds a specified animal with the given type and amount of food.
     * 
     * @param animal     The animal to feed (must implement FeedableAnimal)
     * @param foodType   The type of food to provide
     * @param foodAmount The amount of food (interpreted by the specific animal
     *                   implementation)
     */
    public void feedSpecifiedAnimal(FeedableAnimal animal, String foodType, int foodAmount) {
        animal.feedAnimal(foodType, foodAmount);
    }
}

/**
 * Licensed veterinarian who performs health inspections.
 * 
 * Example usage:
 * 
 * <pre>{@code
 * VeterinarianEmployee vet = new VeterinarianEmployee();
 * HealableAnimal crocodile = new Crocodile("Gena");
 * vet.conductMedicalExaminationOnAnimal(crocodile);
 * }</pre>
 */
class VeterinarianEmployee {
    /**
     * Conducts a medical examination on a specified animal.
     * 
     * @param animal The animal to examine (must implement HealableAnimal)
     */
    public void conductMedicalExaminationOnAnimal(HealableAnimal animal) {
        animal.performVeterinaryMedicalCheckup();
    }
}

/**
 * Sanitation staff member who maintains cleanliness of animal habitats.
 * 
 * Example usage:
 * 
 * <pre>{@code
 * EnclosureCleanerEmployee cleaner = new EnclosureCleanerEmployee();
 * CleanableEnclosure crocodile = new Crocodile("Gena");
 * cleaner.cleanSpecifiedAnimalEnclosure(crocodile);
 * }</pre>
 */
class EnclosureCleanerEmployee {
    /**
     * Cleans the enclosure of a specified animal.
     * 
     * @param enclosure The animal enclosure to clean (must implement
     *                  CleanableEnclosure)
     */
    public void cleanSpecifiedAnimalEnclosure(CleanableEnclosure enclosure) {
        enclosure.cleanAnimalEnclosure();
    }
}

/**
 * Service that schedules and executes feeding routines for zoo animals.
 * Delegates actual feeding to a ZookeeperEmployee.
 * 
 * Example usage:
 * 
 * <pre>{@code
 * ZookeeperEmployee zookeeper = new ZookeeperEmployee();
 * AnimalFeedingOrchestrationService feedingService = new AnimalFeedingOrchestrationService(zookeeper);
 * FeedableAnimal parrot = new Parrot("Kesha");
 * feedingService.scheduleAndExecuteFeeding(parrot, "seeds", 50);
 * }</pre>
 */
class AnimalFeedingOrchestrationService {
    private final ZookeeperEmployee assignedZookeeper;

    /**
     * Constructs a new AnimalFeedingOrchestrationService with a specified
     * zookeeper.
     * 
     * @param zookeeper The zookeeper who will perform the feeding operations
     */
    public AnimalFeedingOrchestrationService(ZookeeperEmployee zookeeper) {
        this.assignedZookeeper = zookeeper;
    }

    /**
     * Schedules and executes feeding for a target animal.
     * 
     * @param targetAnimal The animal to feed (must implement FeedableAnimal)
     * @param foodType     The type of food to provide
     * @param foodAmount   The amount of food (interpreted by the specific animal
     *                     implementation)
     */
    public void scheduleAndExecuteFeeding(
            FeedableAnimal targetAnimal,
            String foodType,
            int foodAmount) {
        assignedZookeeper.feedSpecifiedAnimal(targetAnimal, foodType, foodAmount);
    }
}

/**
 * Service that manages mandatory veterinary inspections for all incoming or
 * resident animals.
 * 
 * Example usage:
 * 
 * <pre>{@code
 * VeterinarianEmployee vet = new VeterinarianEmployee();
 * VeterinaryMedicalCheckupService medicalService = new VeterinaryMedicalCheckupService(vet);
 * HealableAnimal crocodile = new Crocodile("Gena");
 * medicalService.initiateAndCompleteMedicalCheckup(crocodile);
 * }</pre>
 */
class VeterinaryMedicalCheckupService {
    private final VeterinarianEmployee assignedVeterinarian;

    /**
     * Constructs a new VeterinaryMedicalCheckupService with a specified
     * veterinarian.
     * 
     * @param veterinarian The veterinarian who will perform the medical checkups
     */
    public VeterinaryMedicalCheckupService(VeterinarianEmployee veterinarian) {
        this.assignedVeterinarian = veterinarian;
    }

    /**
     * Initiates and completes a medical checkup for a target animal.
     * 
     * @param targetAnimal The animal to examine (must implement HealableAnimal)
     */
    public void initiateAndCompleteMedicalCheckup(HealableAnimal targetAnimal) {
        assignedVeterinarian.conductMedicalExaminationOnAnimal(targetAnimal);
    }
}

/**
 * Main demonstration class for the Zoo Management System.
 * Simulates core zoo operations: feeding, medical checks, and enclosure
 * cleaning.
 * 
 * Example usage:
 * 
 * <pre>{@code
 * // Run the demonstration:
 * // 1. Compile: javac ZooManagementSystemDemo.java
 * // 2. Execute: java ZooManagementSystemDemo
 * 
 * // Expected output:
 * // Kesha eats 50 g of seeds
 * // Gena ate 5 kg of raw meat
 * // Medical check for parrot Kesha: beak and feathers are in good
 * // condition.
 * // Medical check for crocodile Gena: teeth are in good condition.
 * // Enclosure for crocodile Gena has been cleaned.
 * }</pre>
 */
public class ZooManagementSystemDemo {
    /**
     * Main method demonstrating the zoo management system operations.
     * 
     * @param args Command line arguments (not used in this demonstration)
     */
    public static void main(String[] args) {
        Parrot namedParrot = new Parrot("Kesha");
        Crocodile namedCrocodile = new Crocodile("Gena");

        ZookeeperEmployee primaryZookeeper = new ZookeeperEmployee();
        VeterinarianEmployee leadVeterinarian = new VeterinarianEmployee();
        EnclosureCleanerEmployee habitatCleaner = new EnclosureCleanerEmployee();

        AnimalFeedingOrchestrationService feedingService = new AnimalFeedingOrchestrationService(primaryZookeeper);
        VeterinaryMedicalCheckupService medicalService = new VeterinaryMedicalCheckupService(leadVeterinarian);

        feedingService.scheduleAndExecuteFeeding(namedParrot, "seeds", 50);
        feedingService.scheduleAndExecuteFeeding(namedCrocodile, "raw meat", 5);
        medicalService.initiateAndCompleteMedicalCheckup(namedParrot);
        medicalService.initiateAndCompleteMedicalCheckup(namedCrocodile);
        habitatCleaner.cleanSpecifiedAnimalEnclosure(namedCrocodile);
    }
}