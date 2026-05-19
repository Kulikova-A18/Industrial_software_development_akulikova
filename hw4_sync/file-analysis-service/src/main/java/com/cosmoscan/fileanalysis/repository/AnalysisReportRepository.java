package com.cosmoscan.fileanalysis.repository;

import com.cosmoscan.fileanalysis.entity.AnalysisReport;
import com.cosmoscan.fileanalysis.enums.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AnalysisReportRepository extends JpaRepository<AnalysisReport, UUID> {
    List<AnalysisReport> findByWorkId(UUID workId);
    Optional<AnalysisReport> findTopByWorkIdOrderByCreatedAtDesc(UUID workId);
    long countByStatus(ReportStatus status);
}
