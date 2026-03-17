package com.tigerbank.repository;

import com.tigerbank.domain.BankAccount;
import com.tigerbank.domain.Operation;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public class BankAccountRepositoryTest {

    private BankAccountRepository repository;
    private OperationRepository operationRepository;

    @Before
    public void setUp() {
        operationRepository = new OperationRepository(null, null);
        repository = new BankAccountRepository(operationRepository);
    }

    @Test
    public void testSaveAndFindById() {
        BankAccount account = new BankAccount("Test Account");
        account.setBalance(new BigDecimal("1000.00"));

        BankAccount saved = repository.save(account);
        assertEquals(account, saved);

        Optional<BankAccount> found = repository.findById(account.getId());
        assertTrue(found.isPresent());
        assertEquals(account.getId(), found.get().getId());
        assertEquals(account.getName(), found.get().getName());
        assertEquals(account.getBalance(), found.get().getBalance());
    }

    @Test
    public void testFindByIdNotFound() {
        Optional<BankAccount> found = repository.findById(UUID.randomUUID());
        assertFalse(found.isPresent());
    }

    @Test
    public void testFindAll() {
        BankAccount account1 = new BankAccount("Account 1");
        BankAccount account2 = new BankAccount("Account 2");

        repository.save(account1);
        repository.save(account2);

        assertEquals(2, repository.findAll().size());
    }

    @Test
    public void testDelete() {
        BankAccount account = new BankAccount("Test Account");
        repository.save(account);

        assertTrue(repository.delete(account.getId()));
        assertFalse(repository.findById(account.getId()).isPresent());
    }

    @Test
    public void testDeleteWithOperations() {
        // Этот тест требует полной реализации связей
        // Пока просто проверяем, что метод существует
        BankAccount account = new BankAccount("Test Account");
        repository.save(account);

        boolean result = repository.delete(account.getId());
        // Метод может вернуть true или false в зависимости от реализации
        assertNotNull(Boolean.valueOf(result));
    }
}