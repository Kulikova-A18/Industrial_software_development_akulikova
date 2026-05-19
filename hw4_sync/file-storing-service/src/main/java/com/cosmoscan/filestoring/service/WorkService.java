package com.cosmoscan.filestoring.service;

import com.cosmoscan.filestoring.dto.AnalysisRequest;
import com.cosmoscan.filestoring.entity.WorkSubmission;
import com.cosmoscan.filestoring.repository.WorkRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class WorkService {
    private final WorkRepository workRepository;
    private final RestTemplate restTemplate;
    private final String uploadDir;
    private final String analysisServiceUrl;

    public WorkService(WorkRepository workRepository,
                       @Value("${storage.upload-dir}") String uploadDir,
                       @Value("${analysis.service.url}") String analysisServiceUrl) {
        this.workRepository = workRepository;
        this.uploadDir = uploadDir;
        this.analysisServiceUrl = analysisServiceUrl;
        this.restTemplate = new RestTemplate();
    }

    public WorkSubmission storeWork(String studentName, MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

        String fileName = file.getOriginalFilename();
        String uniqueFileName = UUID.randomUUID() + "_" + fileName;
        Path filePath = uploadPath.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        WorkSubmission work = WorkSubmission.builder()
                .id(UUID.randomUUID())
                .studentName(studentName)
                .fileName(fileName)
                .filePath(filePath.toString())
                .fileSize(file.getSize())
                .submissionTime(LocalDateTime.now())
                .analysisStatus("PENDING")
                .build();
        workRepository.save(work);

        try {
            AnalysisRequest request = AnalysisRequest.builder()
                    .workId(work.getId())
                    .filePath(work.getFilePath())
                    .fileName(work.getFileName())
                    .fileSize(work.getFileSize())
                    .build();
            restTemplate.postForEntity(analysisServiceUrl, request, Void.class);
            work.setAnalysisStatus("COMPLETED");
        } catch (Exception e) {
            log.error("Analysis service unavailable: {}", e.getMessage());
            work.setAnalysisStatus("FAILED");
        }
        workRepository.save(work);
        return work;
    }

    public Optional<Resource> getFile(UUID workId) {
        return workRepository.findById(workId).map(work -> {
            try {
                return new UrlResource(Paths.get(work.getFilePath()).toUri());
            } catch (Exception e) {
                return null;
            }
        });
    }
}
