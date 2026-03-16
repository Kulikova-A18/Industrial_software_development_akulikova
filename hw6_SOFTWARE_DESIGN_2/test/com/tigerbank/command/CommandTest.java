package com.tigerbank.command;

import com.tigerbank.domain.BankAccount;
import com.tigerbank.domain.factory.DomainFactory;
import com.tigerbank.domain.factory.DomainFactoryImpl;
import com.tigerbank.enums.OperationType;
import com.tigerbank.repository.*;
import com.tigerbank.service.*;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.Assert.*;

public class CommandTest {
    private AccountService accountService;
    private OperationService operationService;
    private CategoryService categoryService;
    private CommandExecutor commandExecutor;
    private DomainFactory domainFactory;

    @Before
    public void setUp() {
        domainFactory = new DomainFactoryImpl();

        OperationRepository operationRepo = new OperationRepository(null, null);
        BankAccountRepository accountRepo = new BankAccountRepository(operationRepo);
        CategoryRepository categoryRepo = new CategoryRepository(operationRepo);

        operationRepo = new OperationRepository(accountRepo, categoryRepo);

        accountService = new AccountService(accountRepo, operationRepo, domainFactory);
        categoryService = new CategoryService(categoryRepo, domainFactory);
        operationService = new OperationService(operationRepo, accountService, categoryService, domainFactory);

        commandExecutor = new CommandExecutor();
    }

    @Test
    public void testCreateAccountCommand() {
        // Given
        String accountName = "Тестовый счет";
        CreateAccountCommand command = new CreateAccountCommand(accountService, accountName);

        // When
        commandExecutor.executeCommand(command);

        // Then
        assertEquals(1, accountService.getAllAccounts().size());
        assertEquals(accountName, accountService.getAllAccounts().get(0).getName());
        assertTrue(command.getExecutionTime() > 0);

        // When - Undo
        commandExecutor.undo();

        // Then
        assertEquals(0, accountService.getAllAccounts().size());
    }

    @Test
    public void testCreateOperationCommand() {
        // Given
        BankAccount account = accountService.createAccount("Основной счет");
        categoryService.loadDefaultCategories();
        UUID categoryId = categoryService.getCategoriesByType(OperationType.INCOME).get(0).getId();

        CreateOperationCommand command = new CreateOperationCommand(
                operationService,
                OperationType.INCOME,
                account.getId(),
                new BigDecimal("1000"),
                categoryId,
                "Тестовая операция");

        // When
        commandExecutor.executeCommand(command);

        // Then
        assertEquals(1, operationService.getAllOperations().size());
        assertEquals(new BigDecimal("1000"), accountService.getAccount(account.getId()).get().getBalance());
        assertTrue(command.getExecutionTime() > 0);

        // When - Undo
        commandExecutor.undo();

        // Then
        assertEquals(0, operationService.getAllOperations().size());
        assertEquals(BigDecimal.ZERO, accountService.getAccount(account.getId()).get().getBalance());
    }

    @Test
    public void testDeleteAccountCommand() {
        // Given
        BankAccount account = accountService.createAccount("Счет для удаления");
        UUID accountId = account.getId();

        DeleteAccountCommand command = new DeleteAccountCommand(accountService, operationService, accountId);

        // When
        commandExecutor.executeCommand(command);

        // Then
        assertTrue(accountService.getAccount(accountId).isEmpty());
        assertTrue(command.getExecutionTime() > 0);
    }
}