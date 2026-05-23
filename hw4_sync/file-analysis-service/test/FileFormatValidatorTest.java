// FileFormatValidatorTest.java
package com.cosmoscan.fileanalysis.validator;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FileFormatValidatorTest {

    @Test
    void validate_WithAllowedPdf_ShouldReturnValid() {
        FileFormatValidator validator = new FileFormatValidator("pdf,docx,txt");
        var result = validator.validate("document.pdf");
        assertTrue(result.isValid());
        assertEquals("pdf", result.detectedFormat());
    }

    @Test
    void validate_WithAllowedDocx_ShouldReturnValid() {
        FileFormatValidator validator = new FileFormatValidator("pdf,docx,txt");
        var result = validator.validate("document.docx");
        assertTrue(result.isValid());
        assertEquals("docx", result.detectedFormat());
    }

    @Test
    void validate_WithArchiveZip_ShouldReturnInvalid() {
        FileFormatValidator validator = new FileFormatValidator("pdf,docx,txt");
        var result = validator.validate("archive.zip");
        assertFalse(result.isValid());
        assertTrue(result.issue().contains("Архивы"));
    }

    @Test
    void validate_WithDisallowedFormat_ShouldReturnInvalid() {
        FileFormatValidator validator = new FileFormatValidator("pdf,docx,txt");
        var result = validator.validate("image.png");
        assertFalse(result.isValid());
        assertTrue(result.issue().contains("Неразрешенный формат"));
    }

    @Test
    void validate_WithNoExtension_ShouldReturnInvalid() {
        FileFormatValidator validator = new FileFormatValidator("pdf,docx,txt");
        var result = validator.validate("noextension");
        assertFalse(result.isValid());
        assertTrue(result.issue().contains("Не удалось определить формат"));
    }
}

