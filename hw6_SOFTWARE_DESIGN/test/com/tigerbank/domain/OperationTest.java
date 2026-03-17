package com.tigerbank.domain;

import com.tigerbank.enums.OperationType;
import org.junit.Test;
import static org.junit.Assert.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class OperationTest {

    @Test
    public void testCreateOperation() {
        UUID accountId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        Operation operation = new Operation(
                OperationType.INCOME,
                accountId,
                new BigDecimal("1000.00"),
                categoryId);

        assertNotNull(operation.getId());
        assertEquals(OperationType.INCOME, operation.getType());
        assertEquals(accountId, operation.getBankAccountId());
        assertEquals(new BigDecimal("1000.00"), operation.getAmount());
        assertEquals(categoryId, operation.getCategoryId());
        assertNotNull(operation.getDate());
    }

    @Test
    public void testOperationWithFullConstructor() {
        UUID id = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        LocalDateTime date = LocalDateTime.of(2024, 1, 15, 10, 30);

        Operation operation = new Operation(
                id,
                OperationType.EXPENSE,
                accountId,
                new BigDecimal("500.00"),
                date,
                "Test description",
                categoryId);

        assertEquals(id, operation.getId());
        assertEquals(OperationType.EXPENSE, operation.getType());
        assertEquals(accountId, operation.getBankAccountId());
        assertEquals(new BigDecimal("500.00"), operation.getAmount());
        assertEquals(date, operation.getDate());
        assertEquals("Test description", operation.getDescription());
        assertEquals(categoryId, operation.getCategoryId());
    }

    @Test
    public void testToString() {
        Operation operation = new Operation(
                OperationType.INCOME,
                UUID.randomUUID(),
                new BigDecimal("1000.50"),
                UUID.randomUUID());
        operation.setDescription("Salary");

        String result = operation.toString();
        assertTrue(result.contains("Доход"));
        assertTrue(result.contains("1000.50"));
        assertTrue(result.contains("Salary"));
    }

    @Test
    public void testToStringWithoutDescription() {
        Operation operation = new Operation(
                OperationType.EXPENSE,
                UUID.randomUUID(),
                new BigDecimal("200.00"),
                UUID.randomUUID());

        String result = operation.toString();
        assertTrue(result.contains("Расход"));
        assertTrue(result.contains("200.00"));
        assertTrue(result.contains("без описания"));
    }
}