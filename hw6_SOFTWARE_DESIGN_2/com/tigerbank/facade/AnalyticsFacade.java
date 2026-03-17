package com.tigerbank.facade;

import com.tigerbank.domain.BankAccount;
import com.tigerbank.domain.Category;
import com.tigerbank.domain.Operation;
import com.tigerbank.enums.OperationType;
import com.tigerbank.service.AccountService;
import com.tigerbank.service.AnalyticsService;
import com.tigerbank.service.CategoryService;
import com.tigerbank.service.OperationService;
import com.tigerbank.di.Singleton;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class AnalyticsFacade {
    private final AnalyticsService analyticsService;
    private final OperationService operationService;
    private final CategoryService categoryService;
    private final AccountService accountService;

    public AnalyticsFacade(AnalyticsService analyticsService,
            OperationService operationService,
            CategoryService categoryService,
            AccountService accountService) {
        this.analyticsService = analyticsService;
        this.operationService = operationService;
        this.categoryService = categoryService;
        this.accountService = accountService;
    }

    public BigDecimal getBalanceDifference(LocalDateTime start, LocalDateTime end) {
        return operationService.getBalanceForPeriod(start, end);
    }

    public Map<String, BigDecimal> getIncomeGroupedByCategory(LocalDateTime start, LocalDateTime end) {
        return getGroupedByCategory(start, end, OperationType.INCOME);
    }

    public Map<String, BigDecimal> getExpenseGroupedByCategory(LocalDateTime start, LocalDateTime end) {
        return getGroupedByCategory(start, end, OperationType.EXPENSE);
    }

    private Map<String, BigDecimal> getGroupedByCategory(LocalDateTime start, LocalDateTime end, OperationType type) {
        Map<String, BigDecimal> result = new HashMap<>();

        List<Operation> operations = operationService.getOperationsByDateRange(start, end).stream()
                .filter(op -> op.getType() == type)
                .collect(Collectors.toList());

        for (Operation op : operations) {
            categoryService.getCategory(op.getCategoryId())
                    .map(Category::getName)
                    .ifPresent(catName -> result.merge(catName, op.getAmount(), BigDecimal::add));
        }

        return result;
    }

    public String getFullAnalyticsReport(LocalDateTime start, LocalDateTime end) {
        return analyticsService.generateAnalyticsReport(start, end);
    }

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        BigDecimal totalBalance = accountService.calculateTotalBalance();
        BigDecimal totalIncome = operationService.getTotalIncome(
                LocalDateTime.now().minusMonths(1), LocalDateTime.now());
        BigDecimal totalExpense = operationService.getTotalExpense(
                LocalDateTime.now().minusMonths(1), LocalDateTime.now());

        stats.put("totalBalance", totalBalance);
        stats.put("totalIncome", totalIncome);
        stats.put("totalExpense", totalExpense);
        stats.put("accountsCount", accountService.getAllAccounts().size());
        stats.put("operationsCount", operationService.getAllOperations().size());

        return stats;
    }

    public void recalculateAllBalances() {
        for (BankAccount account : accountService.getAllAccounts()) {
            BigDecimal calculatedBalance = operationService
                    .getOperationsByAccount(account.getId())
                    .stream()
                    .map(op -> op.getType() == OperationType.INCOME
                            ? op.getAmount()
                            : op.getAmount().negate())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            account.setBalance(calculatedBalance);
            accountService.updateAccount(account);
        }
    }
}