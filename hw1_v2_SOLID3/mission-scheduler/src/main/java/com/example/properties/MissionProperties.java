package com.example.properties;

import java.util.List;

/**
 * Root configuration properties class for mission scheduling.
 * Holds server configuration and a list of mission configurations.
 * Designed to be populated from external configuration sources (e.g., application.yml, application.properties).
 */
public class MissionProperties {
    
    private ServerConfig server;
    private List<MissionConfig> missions;

    /**
     * Returns the server configuration containing connection details.
     *
     * @return the server configuration object
     */
    public ServerConfig getServer() {
        return server;
    }

    /**
     * Sets the server configuration.
     *
     * @param server the server configuration object to set
     */
    public void setServer(ServerConfig server) {
        this.server = server;
    }

    /**
     * Returns the list of mission configurations to be scheduled.
     *
     * @return list of mission configuration objects
     */
    public List<MissionConfig> getMissions() {
        return missions;
    }

    /**
     * Sets the list of mission configurations.
     *
     * @param missions the list of mission configuration objects to set
     */
    public void setMissions(List<MissionConfig> missions) {
        this.missions = missions;
    }

    /**
     * Configuration class for server connection settings.
     * Contains the base URL of the service that missions will interact with.
     */
    public static class ServerConfig {
        private String url;

        /**
         * Returns the server base URL.
         *
         * @return the server URL as a string
         */
        public String getUrl() {
            return url;
        }

        /**
         * Sets the server base URL.
         *
         * @param url the server URL to set
         */
        public void setUrl(String url) {
            this.url = url;
        }
    }

    /**
     * Configuration class for an individual mission.
     * Defines the target (constellation or single satellite), cron schedule,
     * and provides validation and description generation.
     */
    public static class MissionConfig {
        private TargetType targetType;
        private String constellationName;
        private String satelliteName;
        private String cron;

        /**
         * Returns the target type of the mission (CONSTELLATION or SINGLE_SATELLITE).
         *
         * @return the target type enum value
         */
        public TargetType getTargetType() {
            return targetType;
        }

        /**
         * Sets the target type of the mission.
         *
         * @param targetType the target type to set (CONSTELLATION or SINGLE_SATELLITE)
         */
        public void setTargetType(TargetType targetType) {
            this.targetType = targetType;
        }

        /**
         * Returns the name of the constellation associated with this mission.
         *
         * @return the constellation name as a string
         */
        public String getConstellationName() {
            return constellationName;
        }

        /**
         * Sets the name of the constellation for this mission.
         *
         * @param constellationName the constellation name to set
         */
        public void setConstellationName(String constellationName) {
            this.constellationName = constellationName;
        }

        /**
         * Returns the name of the specific satellite for this mission.
         * May be null if the mission targets an entire constellation.
         *
         * @return the satellite name, or null if not applicable
         */
        public String getSatelliteName() {
            return satelliteName;
        }

        /**
         * Sets the name of the specific satellite for this mission.
         *
         * @param satelliteName the satellite name to set
         */
        public void setSatelliteName(String satelliteName) {
            this.satelliteName = satelliteName;
        }

        /**
         * Returns the cron expression defining the mission execution schedule.
         *
         * @return the cron expression as a string (e.g., "0 0 * * * *")
         */
        public String getCron() {
            return cron;
        }

        /**
         * Sets the cron expression for the mission schedule.
         *
         * @param cron the cron expression to set
         */
        public void setCron(String cron) {
            this.cron = cron;
        }

        /**
         * Generates a human-readable description of the mission based on its target type.
         * For CONSTELLATION missions: "CONSTELLATION [constellationName]"
         * For SINGLE_SATELLITE missions: "SINGLE_SATELLITE [constellationName/satelliteName]"
         *
         * @return formatted description string
         */
        public String getDescription() {
            if (targetType == TargetType.CONSTELLATION) {
                return String.format("CONSTELLATION [%s]", constellationName);
            } else {
                return String.format("SINGLE_SATELLITE [%s/%s]", constellationName, satelliteName);
            }
        }

        /**
         * Validates the mission configuration to ensure all required fields are present and valid.
         * Throws an IllegalArgumentException if validation fails.
         * Validation rules:
         * - targetType cannot be null
         * - constellationName cannot be null or blank
         * - cron expression cannot be null or blank
         * - For SINGLE_SATELLITE missions, satelliteName cannot be null or blank
         *
         * @throws IllegalArgumentException if any validation rule is violated
         */
        public void validate() {
            if (targetType == null) {
                throw new IllegalArgumentException("targetType cannot be null");
            }
            if (constellationName == null || constellationName.isBlank()) {
                throw new IllegalArgumentException("constellationName cannot be null or blank");
            }
            if (cron == null || cron.isBlank()) {
                throw new IllegalArgumentException("cron expression cannot be null or blank");
            }
            if (targetType == TargetType.SINGLE_SATELLITE &&
                    (satelliteName == null || satelliteName.isBlank())) {
                throw new IllegalArgumentException("satelliteName is required for SINGLE_SATELLITE missions");
            }
        }
    }

    /**
     * Enumeration defining the possible target types for a mission.
     * - CONSTELLATION: The mission targets an entire constellation of satellites
     * - SINGLE_SATELLITE: The mission targets a specific satellite within a constellation
     */
    public enum TargetType {
        CONSTELLATION, 
        SINGLE_SATELLITE
    }
}