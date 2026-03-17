package com.example.services;

import com.example.annotations.Timed;
import com.example.dto.AddSatelliteRequest;
import com.example.dto.MissionRequest;
import com.example.enums.MissionType;
import com.example.enums.SatelliteType;
import com.example.factories.CommunicationSatelliteFactory;
import com.example.factories.ImagingSatelliteFactory;
import com.example.models.Satellite;
import com.example.models.SatelliteConstellation;
import com.example.params.CommunicationSatelliteParam;
import com.example.params.ImagingSatelliteParam;
import com.example.repositories.ConstellationRepository;

import java.util.List;
import java.util.stream.Collectors;

public class SpaceOperationCenterService {
    private final SatelliteService satelliteService;
    private final ConstellationService constellationService;
    private final ConstellationRepository constellationRepository;
    private final CommunicationSatelliteFactory comFactory;
    private final ImagingSatelliteFactory imgFactory;

    public SpaceOperationCenterService(SatelliteService satelliteService,
            ConstellationService constellationService,
            ConstellationRepository constellationRepository,
            CommunicationSatelliteFactory comFactory,
            ImagingSatelliteFactory imgFactory) {
        this.satelliteService = satelliteService;
        this.constellationService = constellationService;
        this.constellationRepository = constellationRepository;
        this.comFactory = comFactory;
        this.imgFactory = imgFactory;
    }

    @Timed
    public void addSatellite(AddSatelliteRequest request) {
        System.out.println("=== ДОБАВЛЕНИЕ СПУТНИКОВ В ГРУППИРОВКУ: " +
                request.getConstellationName() + " ===");

        SatelliteConstellation constellation = getOrCreateConstellation(request.getConstellationName());

        for (AddSatelliteRequest.SatelliteSpec spec : request.getSatellites()) {
            Satellite satellite = createSatelliteFromSpec(spec);
            constellation.addSatellite(satellite);
            System.out.println("  + Добавлен: " + satellite.getName() +
                    " (тип: " + spec.getType() + ")");
        }

        System.out.println("=== ДОБАВЛЕНИЕ ЗАВЕРШЕНО ===\n");
    }

    @Timed
    public void executeMission(MissionRequest request) {
        System.out.println("=== ВЫПОЛНЕНИЕ МИССИИ: " + request.getMissionType() + " ===");
        System.out.println("Группировка: " + request.getConstellationName());

        SatelliteConstellation constellation = constellationRepository.findByName(request.getConstellationName());
        if (constellation == null) {
            System.out.println("ОШИБКА: Группировка не найдена");
            return;
        }

        List<Satellite> targetSatellites = filterSatellites(constellation, request.getSatelliteNames());

        for (int i = 0; i < request.getRepeatCount(); i++) {
            System.out.println("\n--- Итерация " + (i + 1) + " ---");
            executeMissionOnSatellites(targetSatellites, request.getMissionType());
        }

        System.out.println("=== МИССИЯ ЗАВЕРШЕНА ===\n");
    }

    @Timed
    public void monitorConstellation(String constellationName) {
        System.out.println("=== МОНИТОРИНГ ГРУППИРОВКИ: " + constellationName + " ===");

        SatelliteConstellation constellation = constellationRepository.findByName(constellationName);
        if (constellation == null) {
            System.out.println("Группировка не найдена");
            return;
        }

        List<Satellite> satellites = constellation.getSatellites();
        long operationalCount = satellites.stream()
                .filter(Satellite::isOperational)
                .count();

        System.out.println("Всего спутников: " + satellites.size());
        System.out.println("Работоспособных: " + operationalCount);
        System.out.println("Процент работоспособности: " +
                String.format("%.1f%%", (operationalCount * 100.0 / satellites.size())));

        satellites.forEach(sat -> {
            System.out.println("  " + sat.getName() +
                    " | Статус: " + (sat.isOperational() ? "АКТИВЕН" : "НЕАКТИВЕН") +
                    " | Заряд: " + String.format("%.2f", sat.getEnergy().getBatteryLevel()));
        });

        System.out.println("=== МОНИТОРИНГ ЗАВЕРШЕН ===\n");
    }

    public void emergencyShutdown(String constellationName) {
        System.out.println("!!! ЭКСТРЕННАЯ ДЕАКТИВАЦИЯ ГРУППИРОВКИ: " + constellationName + " !!!");

        SatelliteConstellation constellation = constellationRepository.findByName(constellationName);
        if (constellation != null) {
            constellation.getSatellites().forEach(Satellite::deactivate);
            System.out.println("Все спутники деактивированы");
        }
    }

    @Timed
    public void createStandardConstellation(String name, int comCount, int imgCount) {
        System.out.println("=== СОЗДАНИЕ ТИПОВОЙ ГРУППИРОВКИ: " + name + " ===");

        SatelliteConstellation constellation = new SatelliteConstellation(name);

        for (int i = 1; i <= comCount; i++) {
            Satellite satellite = comFactory.createSatelliteWithParameter(
                    new CommunicationSatelliteParam(name + "-Com-" + i, 0.9, 500.0 * i));
            constellation.addSatellite(satellite);
        }

        for (int i = 1; i <= imgCount; i++) {
            Satellite satellite = imgFactory.createSatelliteWithParameter(
                    new ImagingSatelliteParam(name + "-Img-" + i, 0.85, 2.5 / i));
            constellation.addSatellite(satellite);
        }

        constellationRepository.save(constellation);
        System.out.println("Создано спутников: " + (comCount + imgCount));
    }

    private SatelliteConstellation getOrCreateConstellation(String name) {
        SatelliteConstellation constellation = constellationRepository.findByName(name);
        if (constellation == null) {
            constellation = new SatelliteConstellation(name);
            constellationRepository.save(constellation);
        }
        return constellation;
    }

    private Satellite createSatelliteFromSpec(AddSatelliteRequest.SatelliteSpec spec) {
        if (spec.getType() == SatelliteType.COMMUNICATION) {
            return satelliteService.createSatellite(
                    new CommunicationSatelliteParam(spec.getName(), spec.getBatteryLevel(), spec.getSpecialParam()));
        } else {
            return satelliteService.createSatellite(
                    new ImagingSatelliteParam(spec.getName(), spec.getBatteryLevel(), spec.getSpecialParam()));
        }
    }

    private List<Satellite> filterSatellites(SatelliteConstellation constellation, List<String> names) {
        if (names == null || names.isEmpty()) {
            return constellation.getSatellites();
        }
        return constellation.getSatellites().stream()
                .filter(sat -> names.contains(sat.getName()))
                .collect(Collectors.toList());
    }

    private void executeMissionOnSatellites(List<Satellite> satellites, MissionType missionType) {
        for (Satellite satellite : satellites) {
            if (!satellite.isOperational()) {
                System.out.println("  " + satellite.getName() + ": НЕДОСТАТОЧНО ЭНЕРГИИ");
                continue;
            }

            switch (missionType) {
                case EMERGENCY:
                    System.out.println("  " + satellite.getName() + ": АВАРИЙНЫЙ РЕЖИМ");
                    satellite.performSpecificMission();
                    break;
                case MAINTENANCE:
                    System.out.println("  " + satellite.getName() + ": ТЕХНИЧЕСКОЕ ОБСЛУЖИВАНИЕ");
                    satellite.getEnergy().recharge(0.1);
                    break;
                case STANDARD:
                default:
                    satellite.executeMission();
                    break;
            }
        }
    }
}