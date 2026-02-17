package com.example;

public abstract class Satellite implements SatelliteInfoProvider {
    protected final String name;
    protected final SatelliteState state;
    protected final EnergySystem energy;

    public Satellite(String name, double initialBattery) {
        this.name = name;
        this.state = new SatelliteState(false);
        this.energy = new EnergySystem(initialBattery);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isOperational() {
        return state.isActive() && energy.getBatteryLevel() > 0.1;
    }

    public void activate() {
        if (energy.hasEnoughEnergy(0.05)) {
            energy.consume(0.05);
            state.activate();
            System.out.println(name + ": Активация успешна");
        }
    }

    public void deactivate() {
        state.deactivate();
    }

    public void recharge(double amount) {
        if (!state.isActive()) {
            energy.recharge(amount);
        }
    }

    public abstract void performSpecificMission();

    public final void executeMission() {
        if (state.isActive() && isOperational()) {
            performSpecificMission();
        }
    }

    public SatelliteState getState() {
        return state;
    }

    public EnergySystem getEnergy() {
        return energy;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{name='" + name + "', state=" + state + ", energy=" + energy + "}";
    }
}