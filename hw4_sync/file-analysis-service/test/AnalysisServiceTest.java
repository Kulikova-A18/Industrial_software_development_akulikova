// AnalysisServiceTest.java
package com.cosmoscan.fileanalysis.service;

import com.cosmoscan.fileanalysis.dto.AnalysisRequest;
import com.cosmoscan.fileanalysis.entity.AnalysisReport;
import com.cosmoscan.fileanalysis.enums.ReportStatus;
import com.cosmoscan.fileanalysis.repository.AnalysisReportRepository;
import com.cosmoscan.fileanalysis.validator.FileFormatValidator;
import com.cosmoscan.fileanalysis.validator.FileSizeValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalysisServiceTest {

    @Mock private AnalysisReportRepository repository;
    @Mock private FileFormatValidator formatValidator;
    @Mock private FileSizeValidator sizeValidator;
    @InjectMocks private AnalysisService service;

    @Test
    void analyze_WithValidFile_ShouldReturnAccepted() {
        UUID workId = UUID.randomUUID();
        AnalysisRequest request = AnalysisRequest.builder()
                .workId(workId)
                .filePath("/path/file.pdf")
                .fileName("file.pdf")
                .fileSize(500000L)
                .build();

        when(formatValidator.validate("file.pdf")).thenReturn(new FileFormatValidator.ValidationResult(true, "pdf", null));
        when(sizeValidator.validate(500000L)).thenReturn(new FileSizeValidator.ValidationResult(true, 500000L, null));
        when(repository.save(any(AnalysisReport.class))).thenAnswer(inv -> inv.getArgument(0));

        AnalysisReport result = service.analyze(request);

        assertEquals(ReportStatus.ACCEPTED, result.getStatus());
        assertTrue(result.getIsValidFormat());
        assertTrue(result.getIsValidSize());
    }

    @Test
    void analyze_WithInvalidFormat_ShouldReturnNeedsRework() {
        UUID workId = UUID.randomUUID();
        AnalysisRequest request = AnalysisRequest.builder()
                .workId(workId)
                .filePath("/path/file.exe")
                .fileName("file.exe")
                .fileSize(500000L)
                .build();

        when(formatValidator.validate("file.exe")).thenReturn(new FileFormatValidator.ValidationResult(false, "exe", "Invalid format"));
        when(sizeValidator.validate(500000L)).thenReturn(new FileSizeValidator.ValidationResult(true, 500000L, null));
        when(repository.save(any(AnalysisReport.class))).thenAnswer(inv -> inv.getArgument(0));

        AnalysisReport result = service.analyze(request);

        assertEquals(ReportStatus.NEEDS_REWORK, result.getStatus());
        assertFalse(result.getIsValidFormat());
        assertTrue(result.getIsValidSize());
    }

    @Test
    void getReportsByWorkId_ShouldReturnReports() {
        UUID workId = UUID.randomUUID();
        List<AnalysisReport> expected = List.of(
                AnalysisReport.builder().workId(workId).build()
        );
        when(repository.findByWorkId(workId)).thenReturn(expected);

        List<AnalysisReport> result = service.getReportsByWorkId(workId);

        assertEquals(1, result.size());
        verify(repository).findByWorkId(workId);
    }
}

