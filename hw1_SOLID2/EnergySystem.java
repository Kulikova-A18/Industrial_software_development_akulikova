public class EnergySystem {
    private int batteryLevel;
    private final int maxBatteryLevel;

    public EnergySystem(int initialBattery, int maxBattery) {
        this.batteryLevel = initialBattery;
        this.maxBatteryLevel = maxBattery;
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }

    public int getMaxBatteryLevel() {
        return maxBatteryLevel;
    }

    public void consume(int amount) {
        if (batteryLevel >= amount) {
            batteryLevel -= amount;
        }
    }

    public void recharge(int amount) {
        batteryLevel = Math.min(batteryLevel + amount, maxBatteryLevel);
    }

    public boolean hasEnoughEnergy(int amount) {
        return batteryLevel >= amount;
    }
}