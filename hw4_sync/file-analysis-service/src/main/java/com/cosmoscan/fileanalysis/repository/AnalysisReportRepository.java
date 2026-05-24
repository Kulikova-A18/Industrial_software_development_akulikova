package com.cosmoscan.fileanalysis.repository;

import com.cosmoscan.fileanalysis.entity.AnalysisReport;
import com.cosmoscan.fileanalysis.enums.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository interface for {@link AnalysisReport} entities
 * 
 * @see JpaRepository
 * @see AnalysisReport
 */
public interface AnalysisReportRepository extends JpaRepository<AnalysisReport, UUID> {
    
    /**
     * @param workId UUID of the work submission to find reports for
     * @return list of all analysis reports associated with the given work
     */
    List<AnalysisReport> findByWorkId(UUID workId);
    
    /**
     * @param workId UUID of the work submission
     * @return the most recent analysis report for the given work, if any exists
     */
    Optional<AnalysisReport> findTopByWorkIdOrderByCreatedAtDesc(UUID workId);
    
    /**
     * @param status the report status to count
     * @return total number of reports with the given status
     */
    long countByStatus(ReportStatus status);
}