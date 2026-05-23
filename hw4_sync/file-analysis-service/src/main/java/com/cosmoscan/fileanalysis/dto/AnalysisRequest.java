package com.cosmoscan.fileanalysis.dto;

import jakarta.validation.constraints.*;
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
    
    /** Unique identifier of the work submission, must not be null */
    @NotNull
    private UUID workId;
    
    /** File system path to the stored file, must not be blank */
    @NotBlank
    private String filePath;
    
    /** Original name of the submitted file, must not be blank */
    @NotBlank
    private String fileName;
    
    /** Size of the file in bytes, must be positive and not null */
    @NotNull
    @Positive
    private Long fileSize;
}