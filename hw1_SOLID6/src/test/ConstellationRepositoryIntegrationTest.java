package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Интеграционные тесты для ConstellationRepository")
class ConstellationRepositoryIntegrationTest {

    private ConstellationRepository repository;
    private SpaceOperationCenterService operationCenter;

    private static final String CONSTELLATION_NAME = "Тестовая-Группировка";
    private static final String SATELLITE_NAME_1 = "Связь-1";
    private static final String SATELLITE_NAME_2 = "ДЗЗ-1";

    @BeforeEach
    void setUp() {
        repository = new ConstellationRepository();
        operationCenter = new SpaceOperationCenterService(repository);
    }

    @Test
    @DisplayName("Полный жизненный цикл: создание группировки через сервис")
    void fullLifecycle_CreateConstellation() {
        // Act: создаем группировку через сервис
        operationCenter.createAndSaveConstellation(CONSTELLATION_NAME);

        // Assert: проверяем, что группировка создана в репозитории
        assertTrue(repository.existsByName(CONSTELLATION_NAME));
        SatelliteConstellation saved = repository.findByName(CONSTELLATION_NAME);
        assertNotNull(saved);
        assertEquals(CONSTELLATION_NAME, saved.getConstellationName());
        assertTrue(saved.getSatellites().isEmpty());
    }

    @Test
    @DisplayName("Полный жизненный цикл: создание группировки и добавление спутников")
    void fullLifecycle_CreateAndAddSatellites() {
        // Arrange: создаем спутники
        CommunicationSatellite comSat = new CommunicationSatellite(SATELLITE_NAME_1, 500.0);
        ImagingSatellite imgSat = new ImagingSatellite(SATELLITE_NAME_2, 2.5);

        // Act: создаем группировку и добавляем спутники
        operationCenter.createAndSaveConstellation(CONSTELLATION_NAME);
        operationCenter.addSatelliteToConstellation(CONSTELLATION_NAME, comSat);
        operationCenter.addSatelliteToConstellation(CONSTELLATION_NAME, imgSat);

        // Assert: проверяем добавление спутников
        SatelliteConstellation saved = repository.findByName(CONSTELLATION_NAME);
        assertEquals(2, saved.getSatellites().size());

        Satellite firstSatellite = saved.getSatellites().get(0);
        Satellite secondSatellite = saved.getSatellites().get(1);

        assertEquals(SATELLITE_NAME_1, firstSatellite.getName());
        assertEquals(SATELLITE_NAME_2, secondSatellite.getName());
        assertTrue(firstSatellite instanceof CommunicationSatellite);
        assertTrue(secondSatellite instanceof ImagingSatellite);
    }

    @Test
    @DisplayName("Полный жизненный цикл: создание, активация и выполнение миссий")
    void fullLifecycle_ActivateAndExecuteMissions() throws InterruptedException {
        // Arrange: создаем группировку со спутниками
        CommunicationSatellite comSat = new CommunicationSatellite(SATELLITE_NAME_1, 500.0);
        ImagingSatellite imgSat = new ImagingSatellite(SATELLITE_NAME_2, 2.5);

        operationCenter.createAndSaveConstellation(CONSTELLATION_NAME);
        operationCenter.addSatelliteToConstellation(CONSTELLATION_NAME, comSat);
        operationCenter.addSatelliteToConstellation(CONSTELLATION_NAME, imgSat);

        double initialBatteryCom = comSat.getEnergy().getBatteryLevel();
        double initialBatteryImg = imgSat.getEnergy().getBatteryLevel();

        // Act: активируем и выполняем миссии
        operationCenter.activateAllSatellites(CONSTELLATION_NAME);
        operationCenter.executeConstellationMission(CONSTELLATION_NAME);

        // Assert: проверяем, что энергия потратилась
        SatelliteConstellation saved = repository.findByName(CONSTELLATION_NAME);
        Satellite comSatellite = saved.getSatellites().get(0);
        Satellite imgSatellite = saved.getSatellites().get(1);

        assertTrue(comSatellite.getEnergy().getBatteryLevel() < initialBatteryCom);
        assertTrue(imgSatellite.getEnergy().getBatteryLevel() < initialBatteryImg);
    }

    @Test
    @DisplayName("Полный жизненный цикл: проверка статуса группировки")
    void fullLifecycle_CheckStatus() {
        // Arrange: создаем группировку со спутниками
        CommunicationSatellite comSat = new CommunicationSatellite(SATELLITE_NAME_1, 500.0);
        ImagingSatellite imgSat = new ImagingSatellite(SATELLITE_NAME_2, 2.5);

        operationCenter.createAndSaveConstellation(CONSTELLATION_NAME);
        operationCenter.addSatelliteToConstellation(CONSTELLATION_NAME, comSat);
        operationCenter.addSatelliteToConstellation(CONSTELLATION_NAME, imgSat);

        // Act: активируем спутники
        operationCenter.activateAllSatellites(CONSTELLATION_NAME);

        // Assert: проверяем статус через прямое обращение к репозиторию
        SatelliteConstellation saved = repository.findByName(CONSTELLATION_NAME);
        assertEquals(2, saved.getSatellites().size());

        for (Satellite satellite : saved.getSatellites()) {
            assertTrue(satellite.getState().isActive());
            assertTrue(satellite.isOperational());
        }
    }

    @Test
    @DisplayName("Работа с несколькими группировками")
    void multipleConstellations() {
        // Arrange
        String secondConstellationName = "Вторая-Группировка";
        CommunicationSatellite comSat1 = new CommunicationSatellite("Связь-1", 500.0);
        CommunicationSatellite comSat2 = new CommunicationSatellite("Связь-2", 1000.0);

        // Act: создаем две группировки
        operationCenter.createAndSaveConstellation(CONSTELLATION_NAME);
        operationCenter.createAndSaveConstellation(secondConstellationName);

        operationCenter.addSatelliteToConstellation(CONSTELLATION_NAME, comSat1);
        operationCenter.addSatelliteToConstellation(secondConstellationName, comSat2);

        // Assert: проверяем обе группировки
        Map<String, SatelliteConstellation> allConstellations = repository.findAll();
        assertEquals(2, allConstellations.size());

        SatelliteConstellation first = repository.findByName(CONSTELLATION_NAME);
        SatelliteConstellation second = repository.findByName(secondConstellationName);

        assertEquals(1, first.getSatellites().size());
        assertEquals(1, second.getSatellites().size());
        assertEquals("Связь-1", first.getSatellites().get(0).getName());
        assertEquals("Связь-2", second.getSatellites().get(0).getName());
    }

    @Test
    @DisplayName("Проверка граничных случаев: добавление спутника в несуществующую группировку")
    void addSatelliteToNonExistentConstellation() {
        // Arrange
        CommunicationSatellite comSat = new CommunicationSatellite(SATELLITE_NAME_1, 500.0);

        // Act: пытаемся добавить в несуществующую группировку
        operationCenter.addSatelliteToConstellation("Несуществующая", comSat);

        // Assert: репозиторий должен остаться пустым
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    @DisplayName("Проверка граничных случаев: активация несуществующей группировки")
    void activateNonExistentConstellation() {
        // Act: пытаемся активировать несуществующую группировку
        operationCenter.activateAllSatellites("Несуществующая");

        // Assert: никаких исключений не должно быть, репозиторий пуст
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    @DisplayName("Проверка состояния после множественных операций")
    void stateAfterMultipleOperations() {
        // Arrange
        CommunicationSatellite comSat = new CommunicationSatellite(SATELLITE_NAME_1, 500.0);
        ImagingSatellite imgSat = new ImagingSatellite(SATELLITE_NAME_2, 2.5);

        // 1. Создаем группировку
        operationCenter.createAndSaveConstellation(CONSTELLATION_NAME);

        // 2. Добавляем спутники
        operationCenter.addSatelliteToConstellation(CONSTELLATION_NAME, comSat);
        operationCenter.addSatelliteToConstellation(CONSTELLATION_NAME, imgSat);

        // 3. Проверяем состояние до активации
        SatelliteConstellation beforeActivation = repository.findByName(CONSTELLATION_NAME);
        for (Satellite satellite : beforeActivation.getSatellites()) {
            assertFalse(satellite.getState().isActive());
        }

        // 4. Активируем
        operationCenter.activateAllSatellites(CONSTELLATION_NAME);

        // 5. Проверяем состояние после активации
        SatelliteConstellation afterActivation = repository.findByName(CONSTELLATION_NAME);
        for (Satellite satellite : afterActivation.getSatellites()) {
            assertTrue(satellite.getState().isActive());
        }

        // 6. Выполняем миссии
        operationCenter.executeConstellationMission(CONSTELLATION_NAME);

        // 7. Проверяем итоговое состояние
        SatelliteConstellation finalState = repository.findByName(CONSTELLATION_NAME);
        Satellite comFinal = finalState.getSatellites().get(0);
        Satellite imgFinal = finalState.getSatellites().get(1);

        assertTrue(comFinal.getEnergy().getBatteryLevel() < 0.85);
        assertTrue(imgFinal.getEnergy().getBatteryLevel() < 0.9);

        if (imgFinal instanceof ImagingSatellite) {
            assertEquals(1, ((ImagingSatellite) imgFinal).getPhotosTaken());
        }
    }
}