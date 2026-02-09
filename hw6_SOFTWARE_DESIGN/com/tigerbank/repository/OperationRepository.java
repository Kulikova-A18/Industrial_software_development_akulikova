package com.tigerbank.repository;

import com.tigerbank.domain.BankAccount;
import com.tigerbank.domain.Category;
import com.tigerbank.domain.Operation;
import com.tigerbank.enums.OperationType;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class OperationRepository implements Repository<Operation> {
    private final Map<UUID, Operation> operations = new HashMap<>();
    private final BankAccountRepository accountRepository;
    private final CategoryRepository categoryRepository;

    public OperationRepository(BankAccountRepository accountRepository,
            CategoryRepository categoryRepository) {
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Operation save(Operation operation) {
        if (operation.getBankAccountId() != null) {
            accountRepository.findById(operation.getBankAccountId()).ifPresent(operation::setBankAccount);
        }
        if (operation.getCategoryId() != null) {
            categoryRepository.findById(operation.getCategoryId()).ifPresent(operation::setCategory);
        }

        operations.put(operation.getId(), operation);
        return operation;
    }

    @Override
    public Optional<Operation> findById(UUID id) {
        return Optional.ofNullable(operations.get(id));
    }

    @Override
    public List<Operation> findAll() {
        return new ArrayList<>(operations.values());
    }

    @Override
    public boolean delete(UUID id) {
        return operations.remove(id) != null;
    }

    public List<Operation> findByBankAccountId(UUID accountId) {
        return operations.values().stream()
                .filter(op -> accountId.equals(op.getBankAccountId()))
                .collect(Collectors.toList());
    }

    public List<Operation> findByCategoryId(UUID categoryId) {
        return operations.values().stream()
                .filter(op -> categoryId.equals(op.getCategoryId()))
                .collect(Collectors.toList());
    }

    public List<Operation> findByDateBetween(LocalDateTime start, LocalDateTime end) {
        return operations.values().stream()
                .filter(op -> !op.getDate().isBefore(start) && !op.getDate().isAfter(end))
                .collect(Collectors.toList());
    }

    public List<Operation> findByType(OperationType type) {
        return operations.values().stream()
                .filter(op -> op.getType() == type)
                .collect(Collectors.toList());
    }
}