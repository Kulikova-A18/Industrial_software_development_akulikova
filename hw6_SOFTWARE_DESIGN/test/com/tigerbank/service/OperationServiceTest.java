package com.tigerbank.service;

import com.tigerbank.domain.BankAccount;
import com.tigerbank.domain.Category;
import com.tigerbank.domain.Operation;
import com.tigerbank.enums.OperationType;
import com.tigerbank.repository.BankAccountRepository;
import com.tigerbank.repository.CategoryRepository;
import com.tigerbank.repository.OperationRepository;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class OperationServiceTest {

    private OperationService service;
    private AccountService accountService;
    private CategoryService categoryService;
    private OperationRepository operationRepository;
    private BankAccountRepository accountRepository;
    private CategoryRepository categoryRepository;

    @Before
    public void setUp() {
        operationRepository = new OperationRepository(null, null);
        accountRepository = new BankAccountRepository(operationRepository);
        categoryRepository = new CategoryRepository(operationRepository);

        operationRepository = new OperationRepository(accountRepository, categoryRepository);

        accountService = new AccountService(accountRepository, operationRepository);
        categoryService = new CategoryService(categoryRepository);
        service = new OperationService(operationRepository, accountService, categoryService);
    }

    @Test
    public void testCreateOperation() {
        BankAccount account = accountService.createAccount("Test Account");
        Category category = categoryService.createCategory(OperationType.INCOME, "Salary");

        Operation operation = service.createOperation(
                OperationType.INCOME,
                account.getId(),
                new BigDecimal("1000.00"),
                category.getId(),
                "Monthly salary");

        assertNotNull(operation);
        assertNotNull(operation.getId());
        assertEquals(OperationType.INCOME, operation.getType());
        assertEquals(account.getId(), operation.getBankAccountId());
        assertEquals(category.getId(), operation.getCategoryId());
        assertEquals(new BigDecimal("1000.00"), operation.getAmount());
        assertEquals("Monthly salary", operation.getDescription());

        // Проверяем, что баланс счета обновился
        Optional<BankAccount> updatedAccount = accountService.getAccount(account.getId());
        assertTrue(updatedAccount.isPresent());
        assertEquals(new BigDecimal("1000.00"), updatedAccount.get().getBalance());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateOperationWithInvalidAccount() {
        UUID invalidAccountId = UUID.randomUUID();
        Category category = categoryService.createCategory(OperationType.INCOME, "Salary");

        service.createOperation(
                OperationType.INCOME,
                invalidAccountId,
                new BigDecimal("1000.00"),
                category.getId(),
                "Test");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateOperationWithInvalidCategory() {
        BankAccount account = accountService.createAccount("Test Account");
        UUID invalidCategoryId = UUID.randomUUID();

        service.createOperation(
                OperationType.INCOME,
                account.getId(),
                new BigDecimal("1000.00"),
                invalidCategoryId,
                "Test");
    }

    @Test
    public void testGetOperationsByAccount() {
        BankAccount account = accountService.createAccount("Test Account");
        Category category = categoryService.createCategory(OperationType.INCOME, "Salary");

        service.createOperation(
                OperationType.INCOME,
                account.getId(),
                new BigDecimal("1000.00"),
                category.getId(),
                "Operation 1");

        service.createOperation(
                OperationType.EXPENSE,
                account.getId(),
                new BigDecimal("500.00"),
                category.getId(),
                "Operation 2");

        List<Operation> operations = service.getOperationsByAccount(account.getId());
        assertEquals(2, operations.size());
    }

    @Test
    public void testGetAllOperations() {
        BankAccount account = accountService.createAccount("Test Account");
        Category category = categoryService.createCategory(OperationType.INCOME, "Salary");

        service.createOperation(
                OperationType.INCOME,
                account.getId(),
                new BigDecimal("1000.00"),
                category.getId(),
                "Operation 1");

        service.createOperation(
                OperationType.EXPENSE,
                account.getId(),
                new BigDecimal("500.00"),
                category.getId(),
                "Operation 2");

        assertEquals(2, service.getAllOperations().size());
    }

    @Test
    public void testDeleteOperation() {
        BankAccount account = accountService.createAccount("Test Account");
        Category category = categoryService.createCategory(OperationType.INCOME, "Salary");

        Operation operation = service.createOperation(
                OperationType.INCOME,
                account.getId(),
                new BigDecimal("1000.00"),
                category.getId(),
                "Test operation");

        BigDecimal initialBalance = account.getBalance();

        boolean result = service.deleteOperation(operation.getId());
        assertTrue(result);

        // Проверяем, что операция удалена
        assertFalse(service.getOperation(operation.getId()).isPresent());

        // Проверяем, что баланс счета вернулся к исходному значению
        Optional<BankAccount> updatedAccount = accountService.getAccount(account.getId());
        assertTrue(updatedAccount.isPresent());
        assertEquals(initialBalance.subtract(new BigDecimal("1000.00")),
                updatedAccount.get().getBalance());
    }
}