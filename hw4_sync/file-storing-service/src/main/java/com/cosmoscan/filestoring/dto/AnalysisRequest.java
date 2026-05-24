package com.cosmoscan.filestoring.dto;

import lombok.*;
import java.util.UUID;

/**
 * Data Transfer Object representing a request to analyze a submitted student work
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisRequest {
    
    /** Unique identifier of the work submission to be analyzed */
    private UUID workId;
    
    /** File system path where the submitted file is stored */
    private String filePath;
    
    /** Original name of the submitted file for reference */
    private String fileName;
    
    /** Size of the submitted file in bytes */
    private Long fileSize;
}