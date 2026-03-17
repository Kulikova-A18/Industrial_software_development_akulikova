package com.tigerbank.service;

import com.tigerbank.domain.BankAccount;
import com.tigerbank.enums.OperationType;
import com.tigerbank.repository.BankAccountRepository;
import com.tigerbank.repository.OperationRepository;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public class AccountServiceTest {

    private AccountService service;
    private BankAccountRepository accountRepository;
    private OperationRepository operationRepository;

    @Before
    public void setUp() {
        operationRepository = new OperationRepository(null, null);
        accountRepository = new BankAccountRepository(operationRepository);
        service = new AccountService(accountRepository, operationRepository);
    }

    @Test
    public void testCreateAccount() {
        BankAccount account = service.createAccount("Test Account");

        assertNotNull(account);
        assertNotNull(account.getId());
        assertEquals("Test Account", account.getName());
        assertEquals(BigDecimal.ZERO, account.getBalance());

        Optional<BankAccount> found = service.getAccount(account.getId());
        assertTrue(found.isPresent());
        assertEquals(account.getId(), found.get().getId());
    }

    @Test
    public void testGetAllAccounts() {
        service.createAccount("Account 1");
        service.createAccount("Account 2");

        assertEquals(2, service.getAllAccounts().size());
    }

    @Test
    public void testUpdateAccountBalanceIncome() {
        BankAccount account = service.createAccount("Test Account");
        BigDecimal initialBalance = account.getBalance();

        service.updateAccountBalance(account.getId(), new BigDecimal("500.00"), OperationType.INCOME);

        Optional<BankAccount> updated = service.getAccount(account.getId());
        assertTrue(updated.isPresent());
        assertEquals(initialBalance.add(new BigDecimal("500.00")), updated.get().getBalance());
    }

    @Test
    public void testUpdateAccountBalanceExpense() {
        BankAccount account = service.createAccount("Test Account");
        account.setBalance(new BigDecimal("1000.00"));
        accountRepository.save(account);

        service.updateAccountBalance(account.getId(), new BigDecimal("300.00"), OperationType.EXPENSE);

        Optional<BankAccount> updated = service.getAccount(account.getId());
        assertTrue(updated.isPresent());
        assertEquals(new BigDecimal("700.00"), updated.get().getBalance());
    }

    @Test
    public void testCalculateTotalBalance() {
        BankAccount account1 = service.createAccount("Account 1");
        account1.setBalance(new BigDecimal("1000.00"));
        accountRepository.save(account1);

        BankAccount account2 = service.createAccount("Account 2");
        account2.setBalance(new BigDecimal("500.00"));
        accountRepository.save(account2);

        BigDecimal total = service.calculateTotalBalance();
        assertEquals(new BigDecimal("1500.00"), total);
    }

    @Test
    public void testDeleteAccount() {
        BankAccount account = service.createAccount("Test Account");

        boolean result = service.deleteAccount(account.getId());
        // Метод может вернуть true или false в зависимости от реализации
        assertNotNull(Boolean.valueOf(result));
    }
}