package com.tigerbank.domain.factory;

import com.tigerbank.domain.BankAccount;
import com.tigerbank.domain.Category;
import com.tigerbank.domain.Operation;
import com.tigerbank.enums.OperationType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public interface DomainFactory {
    BankAccount createBankAccount(String name);
    BankAccount createBankAccount(UUID id, String name, BigDecimal balance);
    
    Category createCategory(OperationType type, String name);
    Category createCategory(UUID id, OperationType type, String name);
    
    Operation createOperation(OperationType type, UUID bankAccountId, 
                             BigDecimal amount, UUID categoryId, String description);
    Operation createOperation(UUID id, OperationType type, UUID bankAccountId,
                             BigDecimal amount, LocalDateTime date, 
                             String description, UUID categoryId);
}