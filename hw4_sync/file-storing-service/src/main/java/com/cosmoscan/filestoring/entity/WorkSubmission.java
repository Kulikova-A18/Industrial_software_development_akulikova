package com.cosmoscan.filestoring.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "works")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkSubmission {
    @Id
    private UUID id;
    private String studentName;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private LocalDateTime submissionTime;
    private String analysisStatus;
    
    @PrePersist
    protected void onCreate() {
        if (id == null) id = UUID.randomUUID();
        if (submissionTime == null) submissionTime = LocalDateTime.now();
        if (analysisStatus == null) analysisStatus = "PENDING";
    }
}
