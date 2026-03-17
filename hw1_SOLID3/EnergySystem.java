public class EnergySystem {
    private double batteryLevel;
    private final double maxBatteryLevel = 1.0;

    public EnergySystem(double initialBattery) {
        this.batteryLevel = initialBattery;
    }

    public double getBatteryLevel() {
        return batteryLevel;
    }

    public void consume(double amount) {
        if (batteryLevel >= amount) {
            batteryLevel -= amount;
        }
    }

    public void recharge(double amount) {
        batteryLevel = Math.min(batteryLevel + amount, maxBatteryLevel);
    }

    public boolean hasEnoughEnergy(double amount) {
        return batteryLevel >= amount;
    }

    @Override
    public String toString() {
        return "EnergySystem{batteryLevel=" + batteryLevel + "}";
    }
}