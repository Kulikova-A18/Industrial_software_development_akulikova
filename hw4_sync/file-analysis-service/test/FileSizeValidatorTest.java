// FileSizeValidatorTest.java
package com.cosmoscan.fileanalysis.validator;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FileSizeValidatorTest {

    @Test
    void validate_WithSizeUnderLimit_ShouldReturnValid() {
        FileSizeValidator validator = new FileSizeValidator(1048576);
        var result = validator.validate(500000);
        assertTrue(result.isValid());
    }

    @Test
    void validate_WithSizeAtLimit_ShouldReturnValid() {
        FileSizeValidator validator = new FileSizeValidator(1048576);
        var result = validator.validate(1048576);
        assertTrue(result.isValid());
    }

    @Test
    void validate_WithSizeOverLimit_ShouldReturnInvalid() {
        FileSizeValidator validator = new FileSizeValidator(1048576);
        var result = validator.validate(2097152);
        assertFalse(result.isValid());
        assertTrue(result.issue().contains("Превышен"));
    }
}