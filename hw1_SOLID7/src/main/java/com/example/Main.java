package com.example;

import com.example.dto.AddSatelliteRequest;
import com.example.dto.MissionRequest;
import com.example.enums.MissionType;
import com.example.enums.SatelliteType;
import com.example.factories.CommunicationSatelliteFactory;
import com.example.factories.ImagingSatelliteFactory;
import com.example.repositories.ConstellationRepository;
import com.example.services.*;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        System.out.println("ЗАПУСК СИСТЕМЫ УПРАВЛЕНИЯ СПУТНИКОВОЙ ГРУППИРОВКОЙ");
        System.out.println("============================================================\n");

        // Инициализация компонентов
        CommunicationSatelliteFactory comFactory = new CommunicationSatelliteFactory();
        ImagingSatelliteFactory imgFactory = new ImagingSatelliteFactory();

        SatelliteService satelliteService = new SatelliteServiceImpl(
                Arrays.asList(comFactory, imgFactory));

        ConstellationRepository constellationRepository = new ConstellationRepository();
        ConstellationService constellationService = new ConstellationService(constellationRepository);

        // Создаем фасад
        SpaceOperationCenterService operationCenter = new SpaceOperationCenterService(
                satelliteService,
                constellationService,
                constellationRepository,
                comFactory,
                imgFactory);

        System.out.println("\n=== ТЕСТИРОВАНИЕ FACADE ===");
        System.out.println("============================================================\n");

        // 1. Создание типовой группировки
        operationCenter.createStandardConstellation("Стандартная-1", 2, 2);

        // 2. Добавление спутников через запрос
        AddSatelliteRequest addRequest = new AddSatelliteRequest(
                "Стандартная-1",
                Arrays.asList(
                        new AddSatelliteRequest.SatelliteSpec(
                                SatelliteType.COMMUNICATION, "Связь-Доп-1", 0.95, 2000.0),
                        new AddSatelliteRequest.SatelliteSpec(
                                SatelliteType.IMAGE, "ДЗЗ-Доп-1", 0.88, 0.3)));
        operationCenter.addSatellite(addRequest);

        // 3. Мониторинг состояния
        operationCenter.monitorConstellation("Стандартная-1");

        // 4. Выполнение миссии
        MissionRequest missionRequest = new MissionRequest(
                "Стандартная-1",
                Arrays.asList("Стандартная-1-Com-1", "Стандартная-1-Img-1"),
                MissionType.STANDARD,
                2);
        operationCenter.executeMission(missionRequest);

        // 5. Повторный мониторинг
        operationCenter.monitorConstellation("Стандартная-1");

        // 6. Экстренная деактивация
        operationCenter.emergencyShutdown("Стандартная-1");

        // 7. Финальный мониторинг
        operationCenter.monitorConstellation("Стандартная-1");

        // 8. Демонстрация работы с разными типами миссий
        System.out.println("\n=== ДЕМОНСТРАЦИЯ РАЗНЫХ ТИПОВ МИССИЙ ===");

        // Создаем новую группировку для демонстрации
        operationCenter.createStandardConstellation("Демо-1", 1, 1);

        // Аварийная миссия
        MissionRequest emergencyMission = new MissionRequest(
                "Демо-1",
                Arrays.asList("Демо-1-Com-1"),
                MissionType.EMERGENCY,
                1);
        operationCenter.executeMission(emergencyMission);

        // Миссия обслуживания
        MissionRequest maintenanceMission = new MissionRequest(
                "Демо-1",
                Arrays.asList("Демо-1-Img-1"),
                MissionType.MAINTENANCE,
                1);
        operationCenter.executeMission(maintenanceMission);

        // Финальный мониторинг
        operationCenter.monitorConstellation("Демо-1");
    }
}