package com.cosmoscan.fileanalysis.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisRequest {
    @NotNull
    private UUID workId;
    @NotBlank
    private String filePath;
    @NotBlank
    private String fileName;
    @NotNull
    @Positive
    private Long fileSize;
}
