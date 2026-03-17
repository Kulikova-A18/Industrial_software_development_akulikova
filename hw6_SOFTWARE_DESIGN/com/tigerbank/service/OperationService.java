package com.tigerbank.service;

import com.tigerbank.domain.Operation;
import com.tigerbank.enums.OperationType;
import com.tigerbank.repository.OperationRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class OperationService {
    private final OperationRepository operationRepository;
    private final AccountService accountService;
    private final CategoryService categoryService;

    public OperationService(OperationRepository operationRepository,
            AccountService accountService,
            CategoryService categoryService) {
        this.operationRepository = operationRepository;
        this.accountService = accountService;
        this.categoryService = categoryService;
    }

    public Operation createOperation(OperationType type, UUID accountId,
            BigDecimal amount, UUID categoryId, String description) {
        if (accountService.getAccount(accountId).isEmpty()) {
            throw new IllegalArgumentException("Счет не найден");
        }
        if (categoryService.getCategory(categoryId).isEmpty()) {
            throw new IllegalArgumentException("Категория не найдена");
        }

        Operation operation = new Operation(type, accountId, amount, categoryId);
        operation.setDescription(description);

        accountService.updateAccountBalance(accountId, amount, type);

        return operationRepository.save(operation);
    }

    public Optional<Operation> getOperation(UUID id) {
        return operationRepository.findById(id);
    }

    public List<Operation> getOperationsByAccount(UUID accountId) {
        return operationRepository.findByBankAccountId(accountId);
    }

    public List<Operation> getAllOperations() {
        return operationRepository.findAll();
    }

    public boolean deleteOperation(UUID id) {
        Optional<Operation> operationOpt = operationRepository.findById(id);
        if (operationOpt.isPresent()) {
            Operation operation = operationOpt.get();

            OperationType reverseType = operation.getType() == OperationType.INCOME ? OperationType.EXPENSE
                    : OperationType.INCOME;
            accountService.updateAccountBalance(
                    operation.getBankAccountId(),
                    operation.getAmount(),
                    reverseType);

            return operationRepository.delete(id);
        }
        return false;
    }

    public List<Operation> getOperationsByCategory(UUID categoryId) {
        return operationRepository.findByCategoryId(categoryId);
    }

    public List<Operation> getOperationsByType(OperationType type) {
        return operationRepository.findByType(type);
    }

    public List<Operation> getOperationsByDateRange(LocalDateTime start, LocalDateTime end) {
        return operationRepository.findByDateBetween(start, end);
    }

    public BigDecimal getTotalIncome(LocalDateTime start, LocalDateTime end) {
        return getOperationsByDateRange(start, end).stream()
                .filter(op -> op.getType() == OperationType.INCOME)
                .map(Operation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalExpense(LocalDateTime start, LocalDateTime end) {
        return getOperationsByDateRange(start, end).stream()
                .filter(op -> op.getType() == OperationType.EXPENSE)
                .map(Operation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getBalanceForPeriod(LocalDateTime start, LocalDateTime end) {
        return getTotalIncome(start, end).subtract(getTotalExpense(start, end));
    }
}