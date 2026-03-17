package com.tigerbank.repository;

import com.tigerbank.domain.BankAccount;
import com.tigerbank.domain.Operation;
import java.util.*;

public class BankAccountRepository implements Repository<BankAccount> {
    private final Map<UUID, BankAccount> accounts = new HashMap<>();
    private final OperationRepository operationRepository;

    public BankAccountRepository(OperationRepository operationRepository) {
        this.operationRepository = operationRepository;
    }

    @Override
    public BankAccount save(BankAccount account) {
        accounts.put(account.getId(), account);
        return account;
    }

    @Override
    public Optional<BankAccount> findById(UUID id) {
        BankAccount account = accounts.get(id);
        if (account != null) {
            account.getOperations().clear();
            account.getOperations().addAll(operationRepository.findByBankAccountId(id));
        }
        return Optional.ofNullable(account);
    }

    @Override
    public List<BankAccount> findAll() {
        List<BankAccount> allAccounts = new ArrayList<>(accounts.values());
        for (BankAccount account : allAccounts) {
            account.getOperations().clear();
            account.getOperations().addAll(operationRepository.findByBankAccountId(account.getId()));
        }
        return allAccounts;
    }

    @Override
    public boolean delete(UUID id) {
        List<Operation> accountOperations = operationRepository.findByBankAccountId(id);
        if (!accountOperations.isEmpty()) {
            return false;
        }
        return accounts.remove(id) != null;
    }
}