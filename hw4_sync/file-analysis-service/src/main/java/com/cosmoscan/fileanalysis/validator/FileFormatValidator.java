package com.cosmoscan.fileanalysis.validator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
@Slf4j
public class FileFormatValidator {
    
    private final Set<String> allowedFormats;
    
    public FileFormatValidator(@Value("${analysis.allowed-formats}") String allowedFormatsStr) {
        this.allowedFormats = new HashSet<>(
            Arrays.asList(allowedFormatsStr.split(","))
        );
        log.info("Allowed formats: {}", allowedFormats);
    }
    
    public ValidationResult validate(String fileName) {
        String ext = getExtension(fileName);
        
        if (ext == null || ext.isEmpty()) {
            return new ValidationResult(false, "unknown", 
                    "Не удалось определить формат файла. Допустимые форматы: " + 
                    String.join(", ", allowedFormats));
        }
        
        // Проверка на архивы
        if (Set.of("zip", "rar", "7z", "tar", "gz", "bz2").contains(ext)) {
            return new ValidationResult(false, ext, 
                    "Архивы (" + ext.toUpperCase() + ") не принимаются. " +
                    "Допустимые форматы: " + String.join(", ", allowedFormats));
        }
        
        boolean valid = allowedFormats.contains(ext);
        
        return new ValidationResult(
            valid, 
            ext, 
            valid ? null : "Неразрешенный формат '" + ext + "'. Допустимые форматы: " + 
                    String.join(", ", allowedFormats)
        );
    }
    
    private String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return null;
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }
    
    public record ValidationResult(boolean isValid, String detectedFormat, String issue) {}
}
