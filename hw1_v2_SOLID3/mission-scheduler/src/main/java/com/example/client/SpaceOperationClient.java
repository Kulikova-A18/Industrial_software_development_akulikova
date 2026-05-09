package com.example.client;

import com.example.dto.MissionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;

public class SpaceOperationClient {
    private static final Logger logger = LoggerFactory.getLogger(SpaceOperationClient.class);
    private final RestClient restClient;

    public SpaceOperationClient(RestClient restClient) {
        this.restClient = restClient;
    }

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
            logger.error("Failed to execute mission: {} - {}", request, e.getMessage(), e);
            return false;
        }
    }

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