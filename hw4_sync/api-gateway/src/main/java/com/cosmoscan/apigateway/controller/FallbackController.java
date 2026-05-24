package com.cosmoscan.apigateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller responsible for handling fallback responses when downstream services
 * are unavailable due to circuit breaker activation or service failures.
 */
@RestController
@Slf4j
public class FallbackController {
    
    /**
     * Returns a structured error response when the File Storing Service circuit breaker is open.
     *
     * @return ResponseEntity containing HTTP 503 status and a map with timestamp,
     *         error code, descriptive message, and the affected service name
     */
    @RequestMapping("/fallback/file-storing")
    public ResponseEntity<Map<String, Object>> fileStoringFallback() {
        log.warn("Circuit Breaker opened: File Storing Service is unavailable");
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Service Unavailable");
        response.put("message", "File Storing Service is currently unavailable. Please try again later.");
        response.put("service", "file-storing-service");
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
    
    /**
     * Returns a structured error response when the File Analysis Service circuit breaker is open.
     * Informs clients that file uploads are still functional but analysis processing is delayed.
     *
     * @return ResponseEntity containing HTTP 503 status and a map with timestamp,
     *         error code, user-friendly message, and the affected service name
     */
    @RequestMapping("/fallback/file-analysis")
    public ResponseEntity<Map<String, Object>> fileAnalysisFallback() {
        log.warn("Circuit Breaker opened: File Analysis Service is unavailable");
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Service Unavailable");
        response.put("message", "File Analysis Service is currently unavailable. File uploads still work, but analysis may be delayed.");
        response.put("service", "file-analysis-service");
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
    
    /**
     * Provides a simple health check endpoint for monitoring the API Gateway status.
     *
     * @return ResponseEntity with HTTP 200 and a map containing service status,
     *         service name, current timestamp, and application version
     */
    @RequestMapping("/health-check")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "api-gateway");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("version", "1.0.0");
        
        return ResponseEntity.ok(response);
    }
}