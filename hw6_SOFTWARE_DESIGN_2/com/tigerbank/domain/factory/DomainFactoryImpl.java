package com.tigerbank.domain.factory;

import com.tigerbank.domain.BankAccount;
import com.tigerbank.domain.Category;
import com.tigerbank.domain.Operation;
import com.tigerbank.enums.OperationType;
import com.tigerbank.di.Singleton;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Singleton
public class DomainFactoryImpl implements DomainFactory {

    @Override
    public BankAccount createBankAccount(String name) {
        return new BankAccount(name);
    }

    @Override
    public BankAccount createBankAccount(UUID id, String name, BigDecimal balance) {
        return new BankAccount(id, name, balance);
    }

    @Override
    public Category createCategory(OperationType type, String name) {
        return new Category(type, name);
    }

    @Override
    public Category createCategory(UUID id, OperationType type, String name) {
        return new Category(id, type, name);
    }

    @Override
    public Operation createOperation(OperationType type, UUID bankAccountId,
            BigDecimal amount, UUID categoryId, String description) {
        Operation operation = new Operation(type, bankAccountId, amount, categoryId);
        operation.setDescription(description);
        return operation;
    }

    @Override
    public Operation createOperation(UUID id, OperationType type, UUID bankAccountId,
            BigDecimal amount, LocalDateTime date,
            String description, UUID categoryId) {
        return new Operation(id, type, bankAccountId, amount, date, description, categoryId);
    }
}