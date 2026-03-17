package com.example.services;

import com.example.models.Satellite;
import com.example.models.SatelliteConstellation;
import com.example.repositories.ConstellationRepository;

public class ConstellationService {
    private final ConstellationRepository constellationRepository;

    public ConstellationService(ConstellationRepository constellationRepository) {
        this.constellationRepository = constellationRepository;
    }

    public void createAndSaveConstellation(String name) {
        SatelliteConstellation constellation = new SatelliteConstellation(name);
        constellationRepository.save(constellation);
    }

    public void addSatelliteToConstellation(String constellationName, Satellite satellite) {
        SatelliteConstellation constellation = constellationRepository.findByName(constellationName);
        if (constellation != null) {
            constellation.addSatellite(satellite);
            System.out.println(satellite.getName() + " добавлен в группировку " + constellationName);
        }
    }

    public void executeConstellationMission(String constellationName) {
        SatelliteConstellation constellation = constellationRepository.findByName(constellationName);
        if (constellation != null) {
            System.out.println("ВЫПОЛНЕНИЕ МИССИЙ ГРУППИРОВКИ " + constellationName.toUpperCase());
            System.out.println("==================================================");
            constellation.executeAllMissions();
        }
    }

    public void activateAllSatellites(String constellationName) {
        SatelliteConstellation constellation = constellationRepository.findByName(constellationName);
        if (constellation != null) {
            constellation.activateAll();
        }
    }

    public void showConstellationStatus(String constellationName) {
        SatelliteConstellation constellation = constellationRepository.findByName(constellationName);
        if (constellation != null) {
            constellation.showStatus();
        }
    }

    public void printAllConstellations() {
        System.out.println(constellationRepository.findAll());
    }
}