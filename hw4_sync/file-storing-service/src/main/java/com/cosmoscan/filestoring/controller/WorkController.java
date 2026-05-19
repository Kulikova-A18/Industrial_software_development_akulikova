package com.cosmoscan.filestoring.controller;

import com.cosmoscan.filestoring.entity.WorkSubmission;
import com.cosmoscan.filestoring.service.WorkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/works")
@Slf4j
@CrossOrigin(origins = "*")
public class WorkController {
    
    private final WorkService workService;
    
    public WorkController(WorkService workService) {
        this.workService = workService;
    }
    
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> submitWork(
            @RequestParam("studentName") String studentName,
            @RequestParam("file") MultipartFile file) {
        
        if (studentName == null || studentName.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Имя студента обязательно"));
        }
        
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Файл обязателен"));
        }
        
        try {
            WorkSubmission saved = workService.storeWork(studentName, file);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Работа успешно загружена");
            response.put("workId", saved.getId().toString());
            response.put("studentName", saved.getStudentName());
            response.put("fileName", saved.getFileName());
            response.put("fileSize", saved.getFileSize());
            response.put("analysisStatus", saved.getAnalysisStatus());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            log.error("Ошибка при сохранении работы: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Ошибка при сохранении файла: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{workId}/file")
    public ResponseEntity<?> getFile(@PathVariable UUID workId) {
        return workService.getFile(workId)
                .map(resource -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .header("Content-Disposition", 
                                "attachment; filename=\"" + workId + "\"")
                        .body((Object) resource))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "file-storing-service"
        ));
    }
}
