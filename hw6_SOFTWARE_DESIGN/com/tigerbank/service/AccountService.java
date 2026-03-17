package com.tigerbank.service;

import com.tigerbank.domain.BankAccount;
import com.tigerbank.enums.OperationType;
import com.tigerbank.repository.BankAccountRepository;
import com.tigerbank.repository.OperationRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AccountService {
    private final BankAccountRepository accountRepository;
    private final OperationRepository operationRepository;

    public AccountService(BankAccountRepository accountRepository,
            OperationRepository operationRepository) {
        this.accountRepository = accountRepository;
        this.operationRepository = operationRepository;
    }

    public BankAccount createAccount(String name) {
        BankAccount account = new BankAccount(name);
        return accountRepository.save(account);
    }

    public Optional<BankAccount> getAccount(UUID id) {
        return accountRepository.findById(id);
    }

    public List<BankAccount> getAllAccounts() {
        return accountRepository.findAll();
    }

    public boolean deleteAccount(UUID id) {
        return accountRepository.delete(id);
    }

    public void updateAccountBalance(UUID accountId, BigDecimal amount, OperationType type) {
        Optional<BankAccount> accountOpt = accountRepository.findById(accountId);
        if (accountOpt.isPresent()) {
            BankAccount account = accountOpt.get();
            BigDecimal newBalance = type == OperationType.INCOME ? account.getBalance().add(amount)
                    : account.getBalance().subtract(amount);
            account.setBalance(newBalance);
            accountRepository.save(account);
        }
    }

    public BigDecimal calculateTotalBalance() {
        return getAllAccounts().stream()
                .map(BankAccount::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}