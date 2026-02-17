public class ImagingSatellite extends Satellite {
    public ImagingSatellite(String name) {
        super(name, 90);
    }

    @Override
    public String getType() {
        return "Спутник дистанционного зондирования";
    }

    @Override
    public void performSpecificMission() {
        System.out.println("Съемка земной поверхности...");
        energy.consume(25);
        System.out.println("  Снимки получены и сохранены. Остаток энергии: " + energy.getBatteryLevel());
    }

    public void calibrateSensors() {
        if (state.isActive() && energy.hasEnoughEnergy(5)) {
            energy.consume(5);
            System.out.println("Спутник " + name + ": датчики откалиброваны");
        }
    }
}