package com.cosmoscan.fileanalysis.validator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Validator component that checks whether uploaded file sizes are within the allowed limit.
 */
@Component
public class FileSizeValidator {
    
    private final long maxSize;
    
    /**
     * @param maxSize maximum allowed file size in bytes from application properties
     */
    public FileSizeValidator(@Value("${analysis.max-file-size}") long maxSize) {
        this.maxSize = maxSize;
    }
    
    /**
     * Validates the file size against the configured maximum limit.
     *
     * @param fileSize the actual file size in bytes to validate
     * @return ValidationResult containing validity flag, actual size, and error message if size exceeds limit
     */
    public ValidationResult validate(long fileSize) {
        boolean valid = fileSize <= maxSize;
        String issue = valid ? null : 
            String.format("Превышен максимальный размер файла. Текущий: %s, максимальный: %s",
                    formatSize(fileSize), formatSize(maxSize));
        return new ValidationResult(valid, fileSize, issue);
    }
    
    /**
     * Formats a file size in bytes to a human-readable string (B, KB, MB).
     *
     * @param size the file size in bytes
     * @return formatted string representation of the size
     */
    private String formatSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1048576) return String.format("%.2f KB", size / 1024.0);
        return String.format("%.2f MB", size / 1048576.0);
    }
    
    /**
     * Record representing the result of file size validation.
     * 
     * @param isValid    true if the file size is within the allowed limit, false otherwise
     * @param actualSize the actual file size in bytes that was validated
     * @param issue      description of the validation issue, null if valid
     */
    public record ValidationResult(boolean isValid, long actualSize, String issue) {}
}