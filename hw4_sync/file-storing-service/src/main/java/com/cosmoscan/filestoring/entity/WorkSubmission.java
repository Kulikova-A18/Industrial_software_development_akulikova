package com.cosmoscan.filestoring.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity representing a student work submission stored in the database.
 * Tracks file metadata, submission details, and analysis processing status.
 */
@Entity
@Table(name = "works")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkSubmission {
    
    /** Unique identifier for the work submission, auto-generated if not provided */
    @Id
    private UUID id;
    
    /** Name of the student who submitted the work */
    private String studentName;
    
    /** Original filename of the uploaded document */
    private String fileName;
    
    /** File system path where the uploaded file is physically stored */
    private String filePath;
    
    /** Size of the uploaded file in bytes */
    private Long fileSize;
    
    /** Timestamp indicating when the work was submitted */
    private LocalDateTime submissionTime;
    
    /** Current status of analysis processing (e.g., PENDING, PROCESSING, COMPLETED, FAILED) */
    private String analysisStatus;
    
    /**
     * Lifecycle callback executed before entity persistence.
     * Initializes id, submissionTime, and analysisStatus with default values if not already set.
     */
    @PrePersist
    protected void onCreate() {
        if (id == null) id = UUID.randomUUID();
        if (submissionTime == null) submissionTime = LocalDateTime.now();
        if (analysisStatus == null) analysisStatus = "PENDING";
    }
}