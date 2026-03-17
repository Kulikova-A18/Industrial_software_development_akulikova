package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты для фабрик спутников")
class SatelliteFactoryTest {

    private CommunicationSatelliteFactory comFactory;
    private ImagingSatelliteFactory imgFactory;

    @BeforeEach
    void setUp() {
        comFactory = new CommunicationSatelliteFactory();
        imgFactory = new ImagingSatelliteFactory();
    }

    @Test
    @DisplayName("CommunicationSatelliteFactory создает коммуникационный спутник из параметров")
    void comFactoryCreatesCommunicationSatellite() {
        // Arrange
        double testBattery = 0.75;
        double testBandwidth = 500.0;
        CommunicationSatelliteParam param = new CommunicationSatelliteParam(
            "TestCom", testBattery, testBandwidth
        );

        // Act
        Satellite satellite = comFactory.createSatelliteWithParameter(param);

        // Assert
        assertNotNull(satellite);
        assertTrue(satellite instanceof CommunicationSatellite);
        assertEquals("TestCom", satellite.getName());

        CommunicationSatellite comSat = (CommunicationSatellite) satellite;
        assertEquals(testBandwidth, comSat.getBandwidth());
        assertEquals(testBattery, comSat.getEnergy().getBatteryLevel());
    }

    @Test
    @DisplayName("ImagingSatelliteFactory создает спутник ДЗЗ из параметров")
    void imgFactoryCreatesImagingSatellite() {
        // Arrange
        double testBattery = 0.8;
        double testResolution = 2.5;
        ImagingSatelliteParam param = new ImagingSatelliteParam(
            "TestImg", testBattery, testResolution
        );

        // Act
        Satellite satellite = imgFactory.createSatelliteWithParameter(param);

        // Assert
        assertNotNull(satellite);
        assertTrue(satellite instanceof ImagingSatellite);
        assertEquals("TestImg", satellite.getName());

        ImagingSatellite imgSat = (ImagingSatellite) satellite;
        assertEquals(testResolution, imgSat.getResolution());
        assertEquals(testBattery, imgSat.getEnergy().getBatteryLevel());
    }

    @Test
    @DisplayName("Фабрика выбрасывает исключение при неподдерживаемом типе параметра")
    void factoryThrowsExceptionForUnsupportedParamType() {
        // Arrange
        CommunicationSatelliteParam comParam = new CommunicationSatelliteParam("Test", 0.8, 500.0);

        // Act & Assert
        assertThrows(SpaceOperationException.class, () -> {
            imgFactory.createSatelliteWithParameter(comParam);
        });
    }

    @Test
    @DisplayName("isSatelliteTypeSupported возвращает true только для поддерживаемого типа")
    void isSatelliteTypeSupportedReturnsCorrectValues() {
        // CommunicationSatelliteFactory
        assertTrue(comFactory.isSatelliteTypeSupported(SatelliteType.COMMUNICATION));
        assertFalse(comFactory.isSatelliteTypeSupported(SatelliteType.IMAGE));

        // ImagingSatelliteFactory
        assertTrue(imgFactory.isSatelliteTypeSupported(SatelliteType.IMAGE));
        assertFalse(imgFactory.isSatelliteTypeSupported(SatelliteType.COMMUNICATION));
    }

    @Test
    @DisplayName("ImagingSatelliteFactory создает спутник высокого разрешения")
    void imgFactoryCreatesHighResolutionSatellite() {
        // Act
        Satellite satellite = imgFactory.createHighResolutionSatellite("HighRes", 0.9);

        // Assert
        assertNotNull(satellite);
        assertTrue(satellite instanceof ImagingSatellite);

        ImagingSatellite imgSat = (ImagingSatellite) satellite;
        assertEquals(0.5, imgSat.getResolution());
        assertEquals(0.9, imgSat.getEnergy().getBatteryLevel());
    }

    @ParameterizedTest
    @ValueSource(strings = { "Sat1", "Sat2", "VeryLongSatelliteName" })
    @DisplayName("Фабрики работают с разными именами спутников")
    void factoriesWorkWithDifferentNames(String name) {
        // Arrange
        CommunicationSatelliteParam comParam = new CommunicationSatelliteParam(name, 0.8, 500.0);
        ImagingSatelliteParam imgParam = new ImagingSatelliteParam(name, 0.8, 2.5);

        // Act
        Satellite comSat = comFactory.createSatelliteWithParameter(comParam);
        Satellite imgSat = imgFactory.createSatelliteWithParameter(imgParam);

        // Assert
        assertEquals(name, comSat.getName());
        assertEquals(name, imgSat.getName());
    }
}