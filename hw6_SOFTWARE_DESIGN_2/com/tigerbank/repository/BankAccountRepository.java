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
            List<Operation> accountOperations = operationRepository.findByBankAccountId(id);
            account.getOperations().clear();
            account.getOperations().addAll(accountOperations);
        }
        return Optional.ofNullable(account);
    }

    @Override
    public List<BankAccount> findAll() {
        List<BankAccount> allAccounts = new ArrayList<>(accounts.values());
        for (BankAccount account : allAccounts) {
            List<Operation> accountOperations = operationRepository.findByBankAccountId(account.getId());
            account.getOperations().clear();
            account.getOperations().addAll(accountOperations);
        }
        return allAccounts;
    }

    @Override
    public boolean delete(UUID id) {
        List<Operation> accountOperations = operationRepository.findByBankAccountId(id);
        if (!accountOperations.isEmpty()) {
            for (Operation op : accountOperations) {
                operationRepository.delete(op.getId());
            }
        }
        return accounts.remove(id) != null;
    }

    @Override
    public void clear() {
        accounts.clear();
    }
}