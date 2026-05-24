package com.cosmoscan.fileanalysis.controller;

import com.cosmoscan.fileanalysis.dto.AnalysisRequest;
import com.cosmoscan.fileanalysis.entity.AnalysisReport;
import com.cosmoscan.fileanalysis.service.AnalysisService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for handling file analysis requests and retrieving analysis reports.
 * Provides both internal endpoints for inter-service communication and external endpoints for clients.
 */
@RestController
@Slf4j
public class AnalysisController {
    
    private final AnalysisService service;
    
    /**
     * @param service business logic service for performing file analysis operations
     */
    public AnalysisController(AnalysisService service) {
        this.service = service;
    }
    
    /**
     * Internal endpoint for triggering file analysis. Called by the File Storing Service
     * after a student work is successfully uploaded.
     *
     * @param request DTO containing workId, filePath, fileName, and fileSize of the work to analyze
     * @return ResponseEntity with analysis report details (reportId, workId, status, message),
     *         or HTTP 500 if analysis fails
     */
    @PostMapping("/api/internal/analyze")
    public ResponseEntity<?> analyze(@Valid @RequestBody AnalysisRequest request) {
        try {
            AnalysisReport report = service.analyze(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("reportId", report.getId().toString());
            response.put("workId", report.getWorkId().toString());
            response.put("status", report.getStatus().toString());
            response.put("message", report.getComment());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Ошибка анализа: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Retrieves all analysis reports associated with a specific work submission.
     *
     * @param workId UUID of the work submission to fetch reports for
     * @return ResponseEntity containing a list of {@link AnalysisReport} objects,
     *         or HTTP 404 if no reports are found for the given workId
     */
    @GetMapping("/api/reports/{workId}")
    public ResponseEntity<?> getReports(@PathVariable UUID workId) {
        List<AnalysisReport> reports = service.getReportsByWorkId(workId);
        if (reports.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(reports);
    }
    
    /**
     * Internal health check endpoint for monitoring the File Analysis Service status.
     *
     * @return ResponseEntity with service status, service name, and current timestamp
     */
    @GetMapping("/api/internal/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "file-analysis-service",
                "timestamp", System.currentTimeMillis()
        ));
    }
}