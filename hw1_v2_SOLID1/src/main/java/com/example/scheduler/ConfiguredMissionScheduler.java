package com.example.scheduler;

import com.example.properties.MissionProperties;
import com.example.properties.MissionProperties.MissionConfig;
import com.example.service.MissionExecutionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import java.util.TimeZone;

public class ConfiguredMissionScheduler {
    private static final Logger logger = LoggerFactory.getLogger(ConfiguredMissionScheduler.class);

    private final MissionProperties properties;
    private final ThreadPoolTaskScheduler taskScheduler;
    private final MissionExecutionService missionExecutionService;

    /**
     * Constructs a new ConfiguredMissionScheduler.
     *
     * @param properties The mission properties containing configuration
     * @param taskScheduler The thread pool task scheduler for executing scheduled tasks
     * @param missionExecutionService The service that executes mission logic
     */
    public ConfiguredMissionScheduler(MissionProperties properties,
                                      ThreadPoolTaskScheduler taskScheduler,
                                      MissionExecutionService missionExecutionService) {
        this.properties = properties;
        this.taskScheduler = taskScheduler;
        this.missionExecutionService = missionExecutionService;
    }

    /**
     * Initializes the mission scheduler.
     * Validates mission configurations and schedules each mission according to its cron expression.
     * Logs the number of successfully scheduled missions.
     */
    public void init() {
        logger.info("Mission scheduler initialization started");
        logger.info("Main service URL: {}", properties.getServer().getUrl());

        if (properties.getMissions() == null || properties.getMissions().isEmpty()) {
            logger.warn("No scheduled missions found in configuration");
            return;
        }

        logger.info("Missions found for scheduling: {}", properties.getMissions().size());

        int successfulSchedules = 0;
        for (MissionConfig config : properties.getMissions()) {
            if (scheduleMission(config)) {
                successfulSchedules++;
            }
        }

        logger.info("Mission scheduler initialization completed. Successfully scheduled: {}/{}",
                successfulSchedules, properties.getMissions().size());
    }

    /**
     * Schedules a single mission using a cron trigger.
     * Validates the mission configuration, creates a cron trigger with the system default timezone,
     * and schedules the mission execution task to run according to the cron expression.
     *
     * @param config The mission configuration containing target type, constellation, 
     *               satellite name, cron expression, and description
     * @return true if the mission was successfully scheduled, false otherwise
     */
    private boolean scheduleMission(MissionConfig config) {
        try {
            // Validate mission configuration before scheduling
            config.validate();

            logger.info("Scheduling mission:");
            logger.info("  Target type: {}", config.getTargetType());
            logger.info("  Constellation: {}", config.getConstellationName());
            if (config.getSatelliteName() != null) {
                logger.info("  Satellite: {}", config.getSatelliteName());
            }
            logger.info("  Cron schedule: {}", config.getCron());
            logger.info("  Description: {}", config.getDescription());

            // Create a cron trigger with the system default timezone
            CronTrigger cronTrigger = new CronTrigger(config.getCron(), TimeZone.getDefault());

            // Schedule the mission execution task with the cron trigger
            // The task will be executed each time the cron expression matches the current time
            taskScheduler.schedule(
                () -> {
                    try {
                        missionExecutionService.executeScheduledMission(config);
                    } catch (Exception e) {
                        logger.error("Error executing scheduled mission: {}", 
                            config.getDescription(), e);
                    }
                },
                cronTrigger
            );

            logger.info("Mission scheduled successfully with cron expression: {}", config.getCron());
            return true;

        } catch (Exception e) {
            logger.error("Failed to schedule mission: {}", config.getDescription(), e);
            return false;
        }
    }
}