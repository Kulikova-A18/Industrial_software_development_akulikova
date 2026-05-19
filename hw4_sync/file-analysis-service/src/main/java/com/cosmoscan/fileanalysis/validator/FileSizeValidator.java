package com.cosmoscan.fileanalysis.validator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FileSizeValidator {
    
    private final long maxSize;
    
    public FileSizeValidator(@Value("${analysis.max-file-size}") long maxSize) {
        this.maxSize = maxSize;
    }
    
    public ValidationResult validate(long fileSize) {
        boolean valid = fileSize <= maxSize;
        String issue = valid ? null : 
            String.format("Превышен максимальный размер файла. Текущий: %s, максимальный: %s",
                    formatSize(fileSize), formatSize(maxSize));
        return new ValidationResult(valid, fileSize, issue);
    }
    
    private String formatSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1048576) return String.format("%.2f KB", size / 1024.0);
        return String.format("%.2f MB", size / 1048576.0);
    }
    
    public record ValidationResult(boolean isValid, long actualSize, String issue) {}
}
