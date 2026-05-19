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

@RestController
@Slf4j
public class AnalysisController {
    
    private final AnalysisService service;
    
    public AnalysisController(AnalysisService service) {
        this.service = service;
    }
    
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
    
    @GetMapping("/api/reports/{workId}")
    public ResponseEntity<?> getReports(@PathVariable UUID workId) {
        List<AnalysisReport> reports = service.getReportsByWorkId(workId);
        if (reports.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(reports);
    }
    
    @GetMapping("/api/internal/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "file-analysis-service",
                "timestamp", System.currentTimeMillis()
        ));
    }
}
