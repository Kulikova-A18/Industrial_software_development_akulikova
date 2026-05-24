package com.cosmoscan.fileanalysis.service;

import com.cosmoscan.fileanalysis.dto.AnalysisRequest;
import com.cosmoscan.fileanalysis.entity.AnalysisReport;
import com.cosmoscan.fileanalysis.enums.ReportStatus;
import com.cosmoscan.fileanalysis.repository.AnalysisReportRepository;
import com.cosmoscan.fileanalysis.validator.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service class handling the business logic for file analysis operations.
 * Validates uploaded files against format and size requirements, generates reports,
 * and persists analysis results to the database.
 */
@Service
@Slf4j
public class AnalysisService {
    
    private final AnalysisReportRepository repository;
    private final FileFormatValidator formatValidator;
    private final FileSizeValidator sizeValidator;

    /**
     * @param repository      repository for persisting analysis report entities
     * @param formatValidator validator for checking file format requirements
     * @param sizeValidator   validator for checking file size requirements
     */
    public AnalysisService(AnalysisReportRepository repository,
                           FileFormatValidator formatValidator,
                           FileSizeValidator sizeValidator) {
        this.repository = repository;
        this.formatValidator = formatValidator;
        this.sizeValidator = sizeValidator;
    }

    /**
     * Performs analysis on a submitted student work by validating file format and size.
     * Generates a report with the validation results and persists it to the database.
     *
     * @param request DTO containing the work details to analyze (workId, filePath, fileName, fileSize)
     * @return the saved {@link AnalysisReport} entity with validation results and status
     */
    @Transactional
    public AnalysisReport analyze(AnalysisRequest request) {
        long start = System.currentTimeMillis();
        List<String> issues = new ArrayList<>();

        FileFormatValidator.ValidationResult formatResult = formatValidator.validate(request.getFileName());
        FileSizeValidator.ValidationResult sizeResult = sizeValidator.validate(request.getFileSize());

        if (!formatResult.isValid()) issues.add(formatResult.issue());
        if (!sizeResult.isValid()) issues.add(sizeResult.issue());

        AnalysisReport report = AnalysisReport.builder()
                .workId(request.getWorkId())
                .fileName(request.getFileName())
                .fileSize(request.getFileSize())
                .fileFormat(formatResult.detectedFormat())
                .isValidFormat(formatResult.isValid())
                .isValidSize(sizeResult.isValid())
                .analysisDurationMs(System.currentTimeMillis() - start)
                .build();

        if (issues.isEmpty()) {
            report.setStatus(ReportStatus.ACCEPTED);
            report.setComment("Проверка пройдена");
            report.setIssues("Проблем не обнаружено");
        } else {
            report.setStatus(ReportStatus.NEEDS_REWORK);
            report.setComment("Требуется доработка");
            report.setIssues(String.join("; ", issues));
        }

        return repository.save(report);
    }

    /**
     * Retrieves all analysis reports associated with a specific work submission.
     *
     * @param workId UUID of the work submission to fetch reports for
     * @return list of {@link AnalysisReport} entities for the given work, empty list if none found
     */
    public List<AnalysisReport> getReportsByWorkId(UUID workId) {
        return repository.findByWorkId(workId);
    }
}