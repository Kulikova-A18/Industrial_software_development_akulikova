package com.cosmoscan.fileanalysis.validator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Validator component that checks whether uploaded file formats are allowed.
 * Rejects archive files and formats not included in the configured allowed list.
 */
@Component
@Slf4j
public class FileFormatValidator {
    
    private final Set<String> allowedFormats;
    
    /**
     * @param allowedFormatsStr comma-separated string of allowed file extensions from application properties
     */
    public FileFormatValidator(@Value("${analysis.allowed-formats}") String allowedFormatsStr) {
        this.allowedFormats = new HashSet<>(
            Arrays.asList(allowedFormatsStr.split(","))
        );
        log.info("Allowed formats: {}", allowedFormats);
    }
    
    /**
     * Validates the file extension against the allowed formats list.
     * Automatically rejects archive file types (zip, rar, 7z, tar, gz, bz2).
     *
     * @param fileName the original filename to extract and validate the extension from
     * @return ValidationResult containing validity flag, detected format, and error message if invalid
     */
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
    
    /**
     * Extracts the file extension from a filename.
     *
     * @param fileName the filename to extract the extension from
     * @return lowercase file extension without the dot, or null if no extension found
     */
    private String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return null;
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }
    
    /**
     * Record representing the result of file format validation.
     * 
     * @param isValid        true if the format is allowed, false otherwise
     * @param detectedFormat the detected file extension
     * @param issue          description of the validation issue, null if valid
     */
    public record ValidationResult(boolean isValid, String detectedFormat, String issue) {}
}