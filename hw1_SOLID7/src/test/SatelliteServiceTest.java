package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Интеграционные тесты для SatelliteService")
class SatelliteServiceTest {

    private SatelliteService satelliteService;
    private CommunicationSatelliteFactory comFactory;
    private ImagingSatelliteFactory imgFactory;

    @BeforeEach
    void setUp() {
        comFactory = new CommunicationSatelliteFactory();
        imgFactory = new ImagingSatelliteFactory();
        
        List<SatelliteFactory> factories = Arrays.asList(comFactory, imgFactory);
        satelliteService = new SatelliteServiceImpl(factories);
    }

    @Test
    @DisplayName("Сервис создает коммуникационный спутник через подходящую фабрику")
    void serviceCreatesCommunicationSatellite() {
        // Arrange
        double testBattery = 0.75;
        double testBandwidth = 1000.0;
        CommunicationSatelliteParam param = new CommunicationSatelliteParam(
            "Service-Com-1", testBattery, testBandwidth
        );

        // Act
        Satellite satellite = satelliteService.createSatellite(param);

        // Assert
        assertNotNull(satellite);
        assertTrue(satellite instanceof CommunicationSatellite);
        
        CommunicationSatellite comSat = (CommunicationSatellite) satellite;
        assertEquals("Service-Com-1", comSat.getName());
        assertEquals(testBandwidth, comSat.getBandwidth());
        assertEquals(testBattery, comSat.getEnergy().getBatteryLevel());
        
        // Проверяем, что спутник работает корректно
        comSat.activate();
        assertTrue(comSat.isOperational());
        
        double beforeMission = comSat.getEnergy().getBatteryLevel();
        comSat.executeMission();
        assertTrue(comSat.getEnergy().getBatteryLevel() < beforeMission);
    }

    @Test
    @DisplayName("Сервис создает спутник ДЗЗ через подходящую фабрику")
    void serviceCreatesImagingSatellite() {
        // Arrange
        double testBattery = 0.85;
        double testResolution = 1.5;
        ImagingSatelliteParam param = new ImagingSatelliteParam(
            "Service-Img-1", testBattery, testResolution
        );

        // Act
        Satellite satellite = satelliteService.createSatellite(param);

        // Assert
        assertNotNull(satellite);
        assertTrue(satellite instanceof ImagingSatellite);
        
        ImagingSatellite imgSat = (ImagingSatellite) satellite;
        assertEquals("Service-Img-1", imgSat.getName());
        assertEquals(testResolution, imgSat.getResolution());
        assertEquals(testBattery, imgSat.getEnergy().getBatteryLevel());
        
        // Проверяем, что спутник работает корректно
        imgSat.activate();
        assertTrue(imgSat.isOperational());
        
        int photosBefore = imgSat.getPhotosTaken();
        imgSat.executeMission();
        assertEquals(photosBefore + 1, imgSat.getPhotosTaken());
    }

    @Test
    @DisplayName("Сервис создает несколько спутников разных типов")
    void serviceCreatesMultipleSatellitesOfDifferentTypes() {
        // Arrange
        CommunicationSatelliteParam comParam = new CommunicationSatelliteParam(
            "Com-1", 0.8, 750.0
        );
        ImagingSatelliteParam imgParam = new ImagingSatelliteParam(
            "Img-1", 0.9, 2.0
        );

        // Act
        Satellite comSat = satelliteService.createSatellite(comParam);
        Satellite imgSat = satelliteService.createSatellite(imgParam);

        // Assert
        assertTrue(comSat instanceof CommunicationSatellite);
        assertTrue(imgSat instanceof ImagingSatellite);
        
        assertEquals(750.0, ((CommunicationSatellite) comSat).getBandwidth());
        assertEquals(2.0, ((ImagingSatellite) imgSat).getResolution());
    }

    @Test
    @DisplayName("Сервис выбрасывает исключение при отсутствии подходящей фабрики")
    void serviceThrowsExceptionWhenNoSuitableFactory() {
        // Arrange
        // Создаем сервис только с одной фабрикой
        List<SatelliteFactory> singleFactory = Arrays.asList(comFactory);
        SatelliteService limitedService = new SatelliteServiceImpl(singleFactory);
        
        ImagingSatelliteParam imgParam = new ImagingSatelliteParam("Img-1", 0.9, 2.0);

        // Act & Assert
        assertThrows(SpaceOperationException.class, () -> {
            limitedService.createSatellite(imgParam);
        });
    }

    @Test
    @DisplayName("Сервис выбрасывает исключение при null параметре")
    void serviceThrowsExceptionWhenParamIsNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            satelliteService.createSatellite(null);
        });
    }

    @Test
    @DisplayName("Спутник, созданный через сервис, корректно работает в группировке")
    void satelliteCreatedViaServiceWorksInConstellation() {
        // Arrange
        CommunicationSatelliteParam comParam = new CommunicationSatelliteParam(
            "Constellation-Com", 0.8, 600.0
        );
        ImagingSatelliteParam imgParam = new ImagingSatelliteParam(
            "Constellation-Img", 0.85, 1.8
        );

        Satellite comSat = satelliteService.createSatellite(comParam);
        Satellite imgSat = satelliteService.createSatellite(imgParam);

        SatelliteConstellation constellation = new SatelliteConstellation("Test-Group");
        constellation.addSatellite(comSat);
        constellation.addSatellite(imgSat);

        // Act
        constellation.activateAll();
        constellation.executeAllMissions();

        // Assert
        assertTrue(comSat.getEnergy().getBatteryLevel() < 0.8);
        assertTrue(imgSat.getEnergy().getBatteryLevel() < 0.85);
        
        assertEquals(1, ((ImagingSatellite) imgSat).getPhotosTaken());
    }
}