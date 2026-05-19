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

@Service
@Slf4j
public class AnalysisService {
    private final AnalysisReportRepository repository;
    private final FileFormatValidator formatValidator;
    private final FileSizeValidator sizeValidator;

    public AnalysisService(AnalysisReportRepository repository,
                           FileFormatValidator formatValidator,
                           FileSizeValidator sizeValidator) {
        this.repository = repository;
        this.formatValidator = formatValidator;
        this.sizeValidator = sizeValidator;
    }

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

    public List<AnalysisReport> getReportsByWorkId(UUID workId) {
        return repository.findByWorkId(workId);
    }
}
