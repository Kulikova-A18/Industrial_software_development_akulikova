package com.example;

public class EnergySystem {
    private double batteryLevel;
    private final double maxBatteryLevel;
    private final double minBatteryLevel;
    private final double lowBatteryThreshold;

    private EnergySystem(EnergySystemBuilder builder) {
        this.batteryLevel = builder.initialBattery;
        this.maxBatteryLevel = builder.maxBattery;
        this.minBatteryLevel = builder.minBattery;
        this.lowBatteryThreshold = builder.lowBatteryThreshold;
    }

    public double getBatteryLevel() {
        return batteryLevel;
    }

    public void consume(double amount) {
        if (batteryLevel >= amount) {
            batteryLevel -= amount;
        }
    }

    public boolean consumeWithCheck(double amount) {
        if (hasEnoughEnergy(amount)) {
            batteryLevel -= amount;
            return true;
        }
        return false;
    }

    public void recharge(double amount) {
        batteryLevel = Math.min(batteryLevel + amount, maxBatteryLevel);
    }

    public boolean hasEnoughEnergy(double amount) {
        return batteryLevel >= amount;
    }

    public boolean hasSufficientPower() {
        return batteryLevel > lowBatteryThreshold;
    }

    public boolean isCritical() {
        return batteryLevel <= minBatteryLevel;
    }

    @Override
    public String toString() {
        return "EnergySystem{batteryLevel=" + batteryLevel +
                ", maxBatteryLevel=" + maxBatteryLevel +
                ", lowBatteryThreshold=" + lowBatteryThreshold + "}";
    }

    public static class EnergySystemBuilder {
        private double initialBattery = 1.0;
        private double maxBattery = 1.0;
        private double minBattery = 0.0;
        private double lowBatteryThreshold = 0.2;

        public EnergySystemBuilder initialBattery(double initialBattery) {
            if (initialBattery < 0 || initialBattery > 1.0) {
                throw new IllegalArgumentException("Начальный заряд должен быть от 0 до 1.0");
            }
            this.initialBattery = initialBattery;
            return this;
        }

        public EnergySystemBuilder maxBattery(double maxBattery) {
            if (maxBattery <= 0) {
                throw new IllegalArgumentException("Максимальный заряд должен быть положительным");
            }
            this.maxBattery = maxBattery;
            return this;
        }

        public EnergySystemBuilder minBattery(double minBattery) {
            if (minBattery < 0) {
                throw new IllegalArgumentException("Минимальный заряд не может быть отрицательным");
            }
            this.minBattery = minBattery;
            return this;
        }

        public EnergySystemBuilder lowBatteryThreshold(double threshold) {
            if (threshold < 0 || threshold > 1.0) {
                throw new IllegalArgumentException("Порог низкого заряда должен быть от 0 до 1.0");
            }
            this.lowBatteryThreshold = threshold;
            return this;
        }

        public EnergySystemBuilder withHighCapacity() {
            this.maxBattery = 2.0;
            this.initialBattery = 2.0;
            return this;
        }

        public EnergySystemBuilder withLowPowerMode() {
            this.lowBatteryThreshold = 0.1;
            return this;
        }

        public EnergySystem build() {
            if (initialBattery > maxBattery) {
                throw new IllegalStateException("Начальный заряд не может превышать максимальный");
            }
            if (minBattery > maxBattery) {
                throw new IllegalStateException("Минимальный заряд не может превышать максимальный");
            }
            if (lowBatteryThreshold < minBattery) {
                throw new IllegalStateException("Порог низкого заряда не может быть меньше минимального");
            }

            return new EnergySystem(this);
        }
    }
}