package com.tigerbank.domain;

import org.junit.Test;
import static org.junit.Assert.*;
import java.math.BigDecimal;
import java.util.UUID;

public class BankAccountTest {

    @Test
    public void testCreateBankAccount() {
        BankAccount account = new BankAccount("Test Account");
        assertNotNull(account.getId());
        assertEquals("Test Account", account.getName());
        assertEquals(BigDecimal.ZERO, account.getBalance());
    }

    @Test
    public void testBankAccountWithInitialBalance() {
        BigDecimal initialBalance = new BigDecimal("1000.50");
        UUID id = UUID.randomUUID();
        BankAccount account = new BankAccount(id, "Test Account", initialBalance);

        assertEquals(id, account.getId());
        assertEquals("Test Account", account.getName());
        assertEquals(initialBalance, account.getBalance());
    }

    @Test
    public void testToString() {
        BankAccount account = new BankAccount("Test Account");
        account.setBalance(new BigDecimal("500.75"));
        String expected = "Test Account (Баланс: 500.75)";
        assertEquals(expected, account.toString());
    }

    @Test
    public void testToCsv() {
        BankAccount account = new BankAccount("Test,Account");
        account.setBalance(new BigDecimal("1000.00"));
        String csv = account.toCsv();
        assertTrue(csv.contains("Test Account")); // Запятая должна быть заменена
    }

    @Test
    public void testToJson() {
        BankAccount account = new BankAccount("Test\"Account");
        account.setBalance(new BigDecimal("500.00"));
        String json = account.toJson();
        assertTrue(json.contains("Test\\\"Account")); // Кавычки должны быть экранированы
    }
}