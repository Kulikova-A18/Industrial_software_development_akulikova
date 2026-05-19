package com.cosmoscan.filestoring.dto;

import lombok.*;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisRequest {
    private UUID workId;
    private String filePath;
    private String fileName;
    private Long fileSize;
}
