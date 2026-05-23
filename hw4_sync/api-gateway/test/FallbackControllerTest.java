// FallbackControllerTest.java
package com.cosmoscan.apigateway.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FallbackControllerTest {

    @InjectMocks
    private FallbackController controller;

    @Test
    void fileStoringFallback_ShouldReturn503() {
        var response = controller.fileStoringFallback();
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("file-storing-service", response.getBody().get("service"));
    }

    @Test
    void fileAnalysisFallback_ShouldReturn503() {
        var response = controller.fileAnalysisFallback();
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("file-analysis-service", response.getBody().get("service"));
    }

    @Test
    void healthCheck_ShouldReturnOk() {
        var response = controller.healthCheck();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("UP", response.getBody().get("status"));
        assertEquals("api-gateway", response.getBody().get("service"));
    }
}