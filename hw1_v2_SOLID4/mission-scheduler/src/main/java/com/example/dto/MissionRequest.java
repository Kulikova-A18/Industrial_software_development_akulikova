package com.example.dto;

import com.example.enums.MissionType;
import java.util.List;
import java.util.Objects;

/**
 * Data Transfer Object (DTO) representing a mission request.
 * Immutable class that encapsulates all parameters needed to execute a mission
 * against either a full constellation or specific satellites within a constellation.
 */
public class MissionRequest {
    private final String constellationName;
    private final List<String> satelliteNames;
    private final MissionType missionType;
    private final int repeatCount;

    /**
     * Private constructor for creating a MissionRequest instance.
     * All fields are immutable and set at construction time.
     *
     * @param constellationName the name of the constellation to target
     * @param satelliteNames list of satellite names (null for constellation-level missions)
     * @param missionType the type of mission to execute (e.g., IMAGING, COMMUNICATION)
     * @param repeatCount number of times to repeat the mission execution
     */
    public MissionRequest(String constellationName, List<String> satelliteNames,
                          MissionType missionType, int repeatCount) {
        this.constellationName = constellationName;
        this.satelliteNames = satelliteNames;
        this.missionType = missionType;
        this.repeatCount = repeatCount;
    }

    /**
     * Factory method for creating a mission request targeting an entire constellation.
     * This is a convenience method for constellation-level missions where no specific
     * satellites are selected.
     *
     * @param constellationName the name of the constellation to target
     * @param missionType the type of mission to execute
     * @return a new MissionRequest configured for constellation-level execution with repeat count of 1
     */
    public static MissionRequest forConstellation(String constellationName, MissionType missionType) {
        return new MissionRequest(constellationName, null, missionType, 1);
    }

    /**
     * Factory method for creating a mission request targeting a single satellite.
     * This is a convenience method for missions that need to target one specific satellite.
     *
     * @param constellationName the name of the constellation containing the satellite
     * @param satelliteName the name of the specific satellite to target
     * @param missionType the type of mission to execute
     * @return a new MissionRequest configured for single-satellite execution with repeat count of 1
     */
    public static MissionRequest forSatellite(String constellationName, String satelliteName, MissionType missionType) {
        return new MissionRequest(constellationName, List.of(satelliteName), missionType, 1);
    }

    /**
     * Returns the name of the constellation targeted by this mission.
     *
     * @return the constellation name as a string
     */
    public String getConstellationName() {
        return constellationName;
    }

    /**
     * Returns the list of satellite names targeted by this mission.
     * May be null for constellation-level missions where no specific satellites are selected.
     *
     * @return list of satellite names, or null if targeting an entire constellation
     */
    public List<String> getSatelliteNames() {
        return satelliteNames;
    }

    /**
     * Returns the type of mission to be executed.
     *
     * @return the MissionType enum value
     */
    public MissionType getMissionType() {
        return missionType;
    }

    /**
     * Returns the number of times the mission should be repeated.
     *
     * @return the repeat count as an integer
     */
    public int getRepeatCount() {
        return repeatCount;
    }

    /**
     * Returns a string representation of the MissionRequest.
     * Includes all fields for debugging and logging purposes.
     *
     * @return formatted string containing all mission request details
     */
    @Override
    public String toString() {
        return "MissionRequest{" +
                "constellationName='" + constellationName + '\'' +
                ", satelliteNames=" + satelliteNames +
                ", missionType=" + missionType +
                ", repeatCount=" + repeatCount +
                '}';
    }

    /**
     * Compares this MissionRequest to another object for equality.
     * Two MissionRequest objects are considered equal if all their fields
     * (constellationName, satelliteNames, missionType, repeatCount) are equal.
     *
     * @param o the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MissionRequest that = (MissionRequest) o;
        return repeatCount == that.repeatCount &&
                Objects.equals(constellationName, that.constellationName) &&
                Objects.equals(satelliteNames, that.satelliteNames) &&
                missionType == that.missionType;
    }

    /**
     * Returns a hash code value for this MissionRequest.
     * The hash code is computed based on all fields to maintain the contract
     * that equal objects must have equal hash codes.
     *
     * @return hash code value for this object
     */
    @Override
    public int hashCode() {
        return Objects.hash(constellationName, satelliteNames, missionType, repeatCount);
    }
}