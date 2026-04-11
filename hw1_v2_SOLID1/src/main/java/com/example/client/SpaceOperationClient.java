package com.example.client;

import com.example.dto.MissionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Client for communicating with the space operations service.
 * Handles mission execution requests and health checks via REST API calls.
 */
public class SpaceOperationClient {
    private static final Logger logger = LoggerFactory.getLogger(SpaceOperationClient.class);
    private final RestClient restClient;

    /**
     * Constructs a new SpaceOperationClient with the configured RestClient.
     *
     * @param restClient the Spring RestClient for making HTTP requests
     */
    public SpaceOperationClient(RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * Executes a mission by sending a POST request to the /missions endpoint.
     * Logs the request at debug level and returns a boolean indicating success or failure.
     *
     * @param request the MissionRequest containing mission parameters
     * @return true if the mission was successfully submitted, false otherwise
     */
    public boolean executeMission(MissionRequest request) {
        try {
            logger.debug("Sending mission execution request: {}", request);

            restClient.post()
                    .uri("/missions")
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        logger.error("Mission execution error: HTTP {} - {}",
                                res.getStatusCode().value(), res.getStatusText());
                    })
                    .toBodilessEntity();

            logger.info("Mission successfully submitted: constellation={}, satellites={}",
                    request.getConstellationName(), request.getSatelliteNames());
            return true;

        } catch (RestClientException e) {
            logger.error("Failed to execute mission: {} - {}",
                    request, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Performs a health check on the space operations service.
     * Sends a GET request to the /health endpoint and logs the service availability.
     *
     * @return true if the service is healthy and responding, false otherwise
     */
    public boolean checkHealth() {
        try {
            restClient.get()
                    .uri("/health")
                    .retrieve()
                    .toBodilessEntity();
            logger.debug("Main service is available");
            return true;
        } catch (Exception e) {
            logger.warn("Main service is unavailable: {}", e.getMessage());
            return false;
        }
    }
}