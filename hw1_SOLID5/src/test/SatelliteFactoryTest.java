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
    @DisplayName("CommunicationSatelliteFactory создает коммуникационный спутник")
    void comFactoryCreatesCommunicationSatellite() {
        Satellite satellite = comFactory.createSatellite("TestCom");

        assertNotNull(satellite);
        assertTrue(satellite instanceof CommunicationSatellite);
        assertEquals("TestCom", satellite.getName());

        CommunicationSatellite comSat = (CommunicationSatellite) satellite;
        assertEquals(100.0, comSat.getBandwidth());
    }

    @Test
    @DisplayName("CommunicationSatelliteFactory создает спутник с заданной полосой")
    void comFactoryCreatesSatelliteWithBandwidth() {
        double testBandwidth = 750.0;
        Satellite satellite = comFactory.createSatelliteWithParameter("TestCom", testBandwidth);

        assertNotNull(satellite);
        assertTrue(satellite instanceof CommunicationSatellite);

        CommunicationSatellite comSat = (CommunicationSatellite) satellite;
        assertEquals(testBandwidth, comSat.getBandwidth());
    }

    @Test
    @DisplayName("ImagingSatelliteFactory создает спутник ДЗЗ")
    void imgFactoryCreatesImagingSatellite() {
        Satellite satellite = imgFactory.createSatellite("TestImg");

        assertNotNull(satellite);
        assertTrue(satellite instanceof ImagingSatellite);
        assertEquals("TestImg", satellite.getName());

        ImagingSatellite imgSat = (ImagingSatellite) satellite;
        assertEquals(1.0, imgSat.getResolution());
    }

    @Test
    @DisplayName("ImagingSatelliteFactory создает спутник с заданным разрешением")
    void imgFactoryCreatesSatelliteWithResolution() {
        double testResolution = 2.5;
        Satellite satellite = imgFactory.createSatelliteWithParameter("TestImg", testResolution);

        assertNotNull(satellite);
        assertTrue(satellite instanceof ImagingSatellite);

        ImagingSatellite imgSat = (ImagingSatellite) satellite;
        assertEquals(testResolution, imgSat.getResolution());
    }

    @Test
    @DisplayName("ImagingSatelliteFactory создает спутник высокого разрешения")
    void imgFactoryCreatesHighResolutionSatellite() {
        Satellite satellite = imgFactory.createHighResolutionSatellite("HighRes");

        assertNotNull(satellite);
        assertTrue(satellite instanceof ImagingSatellite);

        ImagingSatellite imgSat = (ImagingSatellite) satellite;
        assertEquals(0.5, imgSat.getResolution());
    }

    @ParameterizedTest
    @ValueSource(strings = { "Sat1", "Sat2", "VeryLongSatelliteName" })
    @DisplayName("Фабрики работают с разными именами спутников")
    void factoriesWorkWithDifferentNames(String name) {
        Satellite comSat = comFactory.createSatellite(name);
        Satellite imgSat = imgFactory.createSatellite(name);

        assertEquals(name, comSat.getName());
        assertEquals(name, imgSat.getName());
    }
}