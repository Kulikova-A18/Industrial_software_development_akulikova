package com.cosmoscan.fileanalysis.entity;

import com.cosmoscan.fileanalysis.enums.ReportStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reports")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisReport {
    @Id
    private UUID id;
    private UUID workId;
    @Enumerated(EnumType.STRING)
    private ReportStatus status;
    private String fileName;
    private Long fileSize;
    private String fileFormat;
    private String detectedFormat;
    private String comment;
    private String issues;
    private Boolean isValidFormat;
    private Boolean isValidSize;
    private LocalDateTime createdAt;
    private Long analysisDurationMs;

    @PrePersist
    protected void onCreate() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = ReportStatus.PENDING;
    }
}
