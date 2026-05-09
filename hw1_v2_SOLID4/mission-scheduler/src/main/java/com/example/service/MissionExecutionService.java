package com.example.service;

import com.example.client.SpaceOperationClient;
import com.example.dto.MissionRequest;
import com.example.enums.MissionType;
import com.example.properties.MissionProperties.MissionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MissionExecutionService {
    private static final Logger logger = LoggerFactory.getLogger(MissionExecutionService.class);
    private final SpaceOperationClient client;

    /**
     * Constructs a new MissionExecutionService.
     *
     * @param client the SpaceOperationClient used to execute missions remotely
     */
    public MissionExecutionService(SpaceOperationClient client) {
        this.client = client;
    }

    /**
     * Executes a scheduled mission based on the provided mission configuration.
     * Creates a MissionRequest from the configuration, sends it to the SpaceOperationClient,
     * and logs the success or failure of the execution.
     *
     * @param config the mission configuration containing target type, constellation name,
     *               satellite name (if applicable), and description
     */
    public void executeScheduledMission(MissionConfig config) {
        logger.info("STARTING EXECUTION OF SCHEDULED MISSION");
        logger.info("Mission: {}", config.getDescription());

        MissionRequest request = createMissionRequest(config);

        logger.debug("Created request: {}", request);

        boolean success = client.executeMission(request);

        if (success) {
            logger.info("MISSION EXECUTED SUCCESSFULLY");
            logger.info("Mission: {}", config.getDescription());
        } else {
            logger.error("MISSION EXECUTION FAILED");
            logger.error("Mission: {}", config.getDescription());
        }
    }

    /**
     * Creates a MissionRequest object from the given mission configuration.
     * The type of request (constellation-level or single-satellite) is determined
     * by the target type specified in the configuration.
     *
     * @param config the mission configuration containing target type and target identifiers
     * @return a MissionRequest configured for execution with SCHEDULED mission type
     * @throws IllegalArgumentException if the target type is unknown or unsupported
     */
    private MissionRequest createMissionRequest(MissionConfig config) {
        switch (config.getTargetType()) {
            case CONSTELLATION:
                return MissionRequest.forConstellation(
                        config.getConstellationName(),
                        MissionType.SCHEDULED
                );
            case SINGLE_SATELLITE:
                return MissionRequest.forSatellite(
                        config.getConstellationName(),
                        config.getSatelliteName(),
                        MissionType.SCHEDULED
                );
            default:
                throw new IllegalArgumentException("Unknown target type: " + config.getTargetType());
        }
    }
}