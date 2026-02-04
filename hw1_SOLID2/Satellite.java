public abstract class Satellite implements SatelliteInfoProvider {
    protected final String name;
    protected final SatelliteState state;
    protected final EnergySystem energy;

    public Satellite(String name, int initialBattery) {
        this.name = name;
        this.state = new SatelliteState(false);
        this.energy = new EnergySystem(initialBattery, 100);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isOperational() {
        return state.isActive() && energy.getBatteryLevel() > 10;
    }

    public void activate() {
        if (energy.hasEnoughEnergy(5)) {
            energy.consume(5);
            state.activate();
            System.out.println("Спутник " + name + " активирован. Энергия: " + energy.getBatteryLevel());
        } else {
            System.out.println("Недостаточно энергии для активации спутника " + name + "!");
        }
    }

    public void deactivate() {
        state.deactivate();
        System.out.println("Спутник " + name + " деактивирован");
    }

    public void recharge(int amount) {
        if (!state.isActive()) {
            energy.recharge(amount);
            System.out.println("Спутник " + name + " заряжен. Энергия: " + energy.getBatteryLevel());
        } else {
            System.out.println("Нельзя заряжать активный спутник " + name + "!");
        }
    }

    public abstract void performSpecificMission();

    public final void executeMission() {
        if (state.isActive() && isOperational()) {
            System.out.print("Спутник " + name + " (" + getType() + "): ");
            performSpecificMission();
        } else {
            System.out.println("Спутник " + name + " не может выполнить миссию: " +
                    (!state.isActive() ? "неактивен" : "недостаточно энергии"));
        }
    }
}