package com.cosmoscan.ui.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnalysisReport {
    
    @JsonProperty("id")
    private UUID reportId;
    
    @JsonProperty("workId")
    private UUID workId;
    
    @JsonProperty("fileName")
    private String fileName;
    
    @JsonProperty("fileSize")
    private Long fileSize;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("comment")
    private String comment;
    
    @JsonProperty("issues")
    private String issues;
    
    @JsonProperty("fileFormat")
    private String fileFormat;
    
    @JsonProperty("detectedFormat")
    private String detectedFormat;
    
    @JsonProperty("isValidFormat")
    private Boolean isValidFormat;
    
    @JsonProperty("isValidSize")
    private Boolean isValidSize;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonProperty("analysisDurationMs")
    private Long analysisDurationMs;
    
    // Вычисляемое поле для отображения
    public String getFileSizeFormatted() {
        if (fileSize == null) return "N/A";
        if (fileSize < 1024) return fileSize + " B";
        if (fileSize < 1024 * 1024) return String.format("%.2f KB", fileSize / 1024.0);
        return String.format("%.2f MB", fileSize / (1024.0 * 1024.0));
    }
}