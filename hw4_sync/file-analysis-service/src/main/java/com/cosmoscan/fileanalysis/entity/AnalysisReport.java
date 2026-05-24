package com.cosmoscan.fileanalysis.entity;

import com.cosmoscan.fileanalysis.enums.ReportStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity representing an analysis report generated after processing a student work.
 * Stores validation results, format checks, and performance metrics from the analysis process.
 */
@Entity
@Table(name = "reports")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisReport {
    
    /** Unique identifier for the analysis report, auto-generated if not provided */
    @Id
    private UUID id;
    
    /** Reference to the work submission that was analyzed */
    private UUID workId;
    
    /** Current status of the report (PENDING, PROCESSING, COMPLETED, FAILED) */
    @Enumerated(EnumType.STRING)
    private ReportStatus status;
    
    /** Original name of the analyzed file */
    private String fileName;
    
    /** Size of the analyzed file in bytes */
    private Long fileSize;
    
    /** Expected file format based on extension */
    private String fileFormat;
    
    /** Actual detected file format after analysis */
    private String detectedFormat;
    
    /** Human-readable comment summarizing the analysis result */
    private String comment;
    
    /** Description of any issues found during analysis */
    private String issues;
    
    /** Whether the detected file format matches the expected format */
    private Boolean isValidFormat;
    
    /** Whether the file size meets the configured requirements */
    private Boolean isValidSize;
    
    /** Timestamp indicating when the report was created */
    private LocalDateTime createdAt;
    
    /** Total time taken to complete the analysis in milliseconds */
    private Long analysisDurationMs;

    /**
     * Lifecycle callback executed before entity persistence.
     * Initializes id, createdAt, and status with default values if not already set.
     */
    @PrePersist
    protected void onCreate() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = ReportStatus.PENDING;
    }
}