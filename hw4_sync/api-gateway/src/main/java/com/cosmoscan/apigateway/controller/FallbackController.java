package com.cosmoscan.apigateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
public class FallbackController {
    
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
