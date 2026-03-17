package com.tigerbank.repository;

import com.tigerbank.domain.BankAccount;
import com.tigerbank.domain.Category;
import com.tigerbank.domain.Operation;
import com.tigerbank.enums.OperationType;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public class OperationRepositoryTest {

    private OperationRepository repository;
    private BankAccountRepository accountRepository;
    private CategoryRepository categoryRepository;

    @Before
    public void setUp() {
        accountRepository = new BankAccountRepository(null);
        categoryRepository = new CategoryRepository(null);
        repository = new OperationRepository(accountRepository, categoryRepository);
    }

    @Test
    public void testSaveAndFindById() {
        Operation operation = new Operation(
                UUID.randomUUID(),
                OperationType.INCOME,
                UUID.randomUUID(),
                new BigDecimal("1000.00"),
                LocalDateTime.now(),
                "Test operation",
                UUID.randomUUID());

        Operation saved = repository.save(operation);
        assertEquals(operation, saved);

        Optional<Operation> found = repository.findById(operation.getId());
        assertTrue(found.isPresent());
        assertEquals(operation.getId(), found.get().getId());
    }

    @Test
    public void testFindAll() {
        Operation op1 = new Operation(
                OperationType.INCOME,
                UUID.randomUUID(),
                new BigDecimal("1000.00"),
                UUID.randomUUID());

        Operation op2 = new Operation(
                OperationType.EXPENSE,
                UUID.randomUUID(),
                new BigDecimal("500.00"),
                UUID.randomUUID());

        repository.save(op1);
        repository.save(op2);

        assertEquals(2, repository.findAll().size());
    }

    @Test
    public void testDelete() {
        Operation operation = new Operation(
                OperationType.INCOME,
                UUID.randomUUID(),
                new BigDecimal("1000.00"),
                UUID.randomUUID());

        repository.save(operation);
        assertTrue(repository.delete(operation.getId()));
        assertFalse(repository.findById(operation.getId()).isPresent());
    }
}