package com.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты для EnergySystem Builder")
class EnergySystemBuilderTest {

    @Test
    @DisplayName("Builder создает EnergySystem с значениями по умолчанию")
    void builderCreatesDefaultEnergySystem() {
        EnergySystem energySystem = new EnergySystem.EnergySystemBuilder().build();

        assertNotNull(energySystem);
        assertEquals(1.0, energySystem.getBatteryLevel());
        assertTrue(energySystem.hasSufficientPower());
        assertFalse(energySystem.isCritical());
    }

    @Test
    @DisplayName("Builder создает EnergySystem с заданным начальным зарядом")
    void builderCreatesEnergySystemWithInitialBattery() {
        double initialBattery = 0.5;
        EnergySystem energySystem = new EnergySystem.EnergySystemBuilder()
                .initialBattery(initialBattery)
                .build();

        assertEquals(initialBattery, energySystem.getBatteryLevel());
    }

    @Test
    @DisplayName("Builder создает высокоемкую EnergySystem")
    void builderCreatesHighCapacityEnergySystem() {
        EnergySystem energySystem = new EnergySystem.EnergySystemBuilder()
                .withHighCapacity()
                .build();

        energySystem.consume(1.5);
        assertTrue(energySystem.getBatteryLevel() > 0);
    }

    @Test
    @DisplayName("Builder создает энергосберегающую EnergySystem")
    void builderCreatesLowPowerEnergySystem() {
        EnergySystem energySystem = new EnergySystem.EnergySystemBuilder()
                .withLowPowerMode()
                .initialBattery(0.15)
                .build();

        assertTrue(energySystem.hasSufficientPower());
    }

    @Test
    @DisplayName("Builder выбрасывает исключение при некорректном начальном заряде")
    void builderThrowsExceptionForInvalidInitialBattery() {
        assertThrows(IllegalArgumentException.class, () -> {
            new EnergySystem.EnergySystemBuilder()
                    .initialBattery(1.5)
                    .build();
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new EnergySystem.EnergySystemBuilder()
                    .initialBattery(-0.1)
                    .build();
        });
    }

    @Test
    @DisplayName("Builder выбрасывает исключение при некорректном пороге")
    void builderThrowsExceptionForInvalidThreshold() {
        assertThrows(IllegalArgumentException.class, () -> {
            new EnergySystem.EnergySystemBuilder()
                    .lowBatteryThreshold(1.5)
                    .build();
        });
    }

    @Test
    @DisplayName("Builder выбрасывает исключение при превышении начальным зарядом максимального")
    void builderThrowsExceptionWhenInitialExceedsMax() {
        EnergySystem.EnergySystemBuilder builder = new EnergySystem.EnergySystemBuilder()
                .initialBattery(0.9);

        assertThrows(IllegalStateException.class, () -> {
            builder.maxBattery(0.5).build();
        });
    }

    @Test
    @DisplayName("Builder выбрасывает исключение при превышении минимального заряда максимальным")
    void builderThrowsExceptionWhenMinExceedsMax() {
        assertThrows(IllegalStateException.class, () -> {
            new EnergySystem.EnergySystemBuilder()
                    .minBattery(0.8)
                    .maxBattery(0.5)
                    .build();
        });
    }

    @Test
    @DisplayName("Builder выбрасывает исключение при пороге ниже минимального")
    void builderThrowsExceptionWhenThresholdBelowMin() {
        assertThrows(IllegalStateException.class, () -> {
            new EnergySystem.EnergySystemBuilder()
                    .minBattery(0.3)
                    .lowBatteryThreshold(0.2)
                    .build();
        });
    }

    @Test
    @DisplayName("EnergySystem корректно потребляет энергию")
    void energySystemConsumesCorrectly() {
        EnergySystem energySystem = new EnergySystem.EnergySystemBuilder()
                .initialBattery(0.5)
                .build();

        assertTrue(energySystem.hasEnoughEnergy(0.3));
        energySystem.consume(0.3);
        assertEquals(0.2, energySystem.getBatteryLevel(), 0.001);

        assertFalse(energySystem.hasEnoughEnergy(0.3));
        energySystem.consume(0.3);
        assertEquals(0.2, energySystem.getBatteryLevel(), 0.001);
    }

    @Test
    @DisplayName("EnergySystem корректно заряжается")
    void energySystemRechargesCorrectly() {
        EnergySystem energySystem = new EnergySystem.EnergySystemBuilder()
                .initialBattery(0.3)
                .build();

        energySystem.recharge(0.5);
        assertEquals(0.8, energySystem.getBatteryLevel(), 0.001);

        energySystem.recharge(0.5);
        assertEquals(1.0, energySystem.getBatteryLevel(), 0.001);
    }

    @Test
    @DisplayName("Метод consumeWithCheck возвращает false при недостатке энергии")
    void consumeWithCheckReturnsFalseWhenNotEnoughEnergy() {
        EnergySystem energySystem = new EnergySystem.EnergySystemBuilder()
                .initialBattery(0.2)
                .build();

        assertFalse(energySystem.consumeWithCheck(0.3));
        assertEquals(0.2, energySystem.getBatteryLevel(), 0.001);

        assertTrue(energySystem.consumeWithCheck(0.15));
        assertEquals(0.05, energySystem.getBatteryLevel(), 0.001);
    }

    @Test
    @DisplayName("Метод hasSufficientPower работает корректно")
    void hasSufficientPowerWorksCorrectly() {
        EnergySystem energySystem = new EnergySystem.EnergySystemBuilder()
                .initialBattery(0.25)
                .lowBatteryThreshold(0.2)
                .build();

        assertTrue(energySystem.hasSufficientPower());

        energySystem.consume(0.1);
        assertFalse(energySystem.hasSufficientPower());
    }

    @Test
    @DisplayName("Метод isCritical работает корректно")
    void isCriticalWorksCorrectly() {
        EnergySystem energySystem = new EnergySystem.EnergySystemBuilder()
                .initialBattery(0.1)
                .minBattery(0.05)
                .build();

        assertFalse(energySystem.isCritical());

        energySystem.consume(0.06);
        assertTrue(energySystem.isCritical());
    }
}