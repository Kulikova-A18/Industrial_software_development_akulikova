package com.cosmoscan.ui.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnalysisReport {
    private UUID reportId;
    private UUID workId;
    private String fileName;
    private Long fileSize;
    private String fileSizeFormatted;
    private String fileFormat;
    private String detectedFormat;
    private String status;
    private String comment;
    private String issues;
    private Boolean isValidFormat;
    private Boolean isValidSize;
    private LocalDateTime createdAt;
    private Long analysisDurationMs;
    private String recommendations;
}
