package com.tigerbank.service;

import com.tigerbank.domain.BankAccount;
import com.tigerbank.domain.Category;
import com.tigerbank.domain.Operation;
import com.tigerbank.enums.OperationType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class AnalyticsService {
    private final OperationService operationService;
    private final CategoryService categoryService;
    private final AccountService accountService;

    public AnalyticsService(OperationService operationService,
            CategoryService categoryService,
            AccountService accountService) {
        this.operationService = operationService;
        this.categoryService = categoryService;
        this.accountService = accountService;
    }

    public Map<String, Object> getFullAnalytics(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> analytics = new LinkedHashMap<>();

        analytics.put("period", startDate.toLocalDate() + " - " + endDate.toLocalDate());
        analytics.put("total_income", operationService.getTotalIncome(startDate, endDate));
        analytics.put("total_expense", operationService.getTotalExpense(startDate, endDate));
        analytics.put("balance", operationService.getBalanceForPeriod(startDate, endDate));

        Map<String, BigDecimal> incomeByCategory = new HashMap<>();
        for (Category category : categoryService.getCategoriesByType(OperationType.INCOME)) {
            BigDecimal total = operationService.getOperationsByCategory(category.getId()).stream()
                    .filter(op -> !op.getDate().isBefore(startDate) && !op.getDate().isAfter(endDate))
                    .map(Operation::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (total.compareTo(BigDecimal.ZERO) > 0) {
                incomeByCategory.put(category.getName(), total);
            }
        }
        analytics.put("income_by_category", incomeByCategory);

        Map<String, BigDecimal> expenseByCategory = new HashMap<>();
        for (Category category : categoryService.getCategoriesByType(OperationType.EXPENSE)) {
            BigDecimal total = operationService.getOperationsByCategory(category.getId()).stream()
                    .filter(op -> !op.getDate().isBefore(startDate) && !op.getDate().isAfter(endDate))
                    .map(Operation::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (total.compareTo(BigDecimal.ZERO) > 0) {
                expenseByCategory.put(category.getName(), total);
            }
        }
        analytics.put("expense_by_category", expenseByCategory);

        Map<String, BigDecimal> accountBalances = new HashMap<>();
        for (BankAccount account : accountService.getAllAccounts()) {
            BigDecimal income = operationService.getOperationsByAccount(account.getId()).stream()
                    .filter(op -> op.getType() == OperationType.INCOME)
                    .filter(op -> !op.getDate().isBefore(startDate) && !op.getDate().isAfter(endDate))
                    .map(Operation::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal expense = operationService.getOperationsByAccount(account.getId()).stream()
                    .filter(op -> op.getType() == OperationType.EXPENSE)
                    .filter(op -> !op.getDate().isBefore(startDate) && !op.getDate().isAfter(endDate))
                    .map(Operation::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal balance = income.subtract(expense);
            if (balance.compareTo(BigDecimal.ZERO) != 0) {
                accountBalances.put(account.getName(), balance);
            }
        }
        analytics.put("account_balances", accountBalances);

        List<Operation> allOperations = operationService.getAllOperations().stream()
                .filter(op -> !op.getDate().isBefore(startDate) && !op.getDate().isAfter(endDate))
                .sorted((o1, o2) -> o2.getAmount().compareTo(o1.getAmount()))
                .limit(10)
                .collect(Collectors.toList());
        analytics.put("top_operations", allOperations);

        return analytics;
    }

    public Map<String, BigDecimal> getCategoryStatistics(OperationType type) {
        Map<UUID, BigDecimal> categoryTotals = new HashMap<>();

        for (Operation op : operationService.getAllOperations()) {
            if (op.getType() == type) {
                categoryTotals.merge(op.getCategoryId(), op.getAmount(), BigDecimal::add);
            }
        }

        Map<String, BigDecimal> result = new HashMap<>();
        for (Map.Entry<UUID, BigDecimal> entry : categoryTotals.entrySet()) {
            Optional<Category> categoryOpt = categoryService.getCategory(entry.getKey());
            result.put(categoryOpt.map(Category::getName).orElse("Неизвестная"), entry.getValue());
        }

        return result;
    }

    public String generateAnalyticsReport(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> analytics = getFullAnalytics(startDate, endDate);

        StringBuilder report = new StringBuilder();
        report.append("=== ФИНАНСОВЫЙ ОТЧЕТ ===\n");
        report.append("Период: ").append(analytics.get("period")).append("\n\n");

        report.append("ОБЩАЯ СТАТИСТИКА:\n");
        report.append(String.format("Общий доход: %.2f\n", analytics.get("total_income")));
        report.append(String.format("Общий расход: %.2f\n", analytics.get("total_expense")));
        report.append(String.format("Баланс за период: %.2f\n\n", analytics.get("balance")));

        report.append("ДОХОДЫ ПО КАТЕГОРИЯМ:\n");
        Map<String, BigDecimal> incomeByCategory = (Map<String, BigDecimal>) analytics.get("income_by_category");
        if (incomeByCategory.isEmpty()) {
            report.append("  Нет данных\n");
        } else {
            incomeByCategory.forEach((cat, amt) -> report.append(String.format("  %s: %.2f\n", cat, amt)));
        }

        report.append("\nРАСХОДЫ ПО КАТЕГОРИЯМ:\n");
        Map<String, BigDecimal> expenseByCategory = (Map<String, BigDecimal>) analytics.get("expense_by_category");
        if (expenseByCategory.isEmpty()) {
            report.append("  Нет данных\n");
        } else {
            expenseByCategory.forEach((cat, amt) -> report.append(String.format("  %s: %.2f\n", cat, amt)));
        }

        report.append("\nБАЛАНС ПО СЧЕТАМ:\n");
        Map<String, BigDecimal> accountBalances = (Map<String, BigDecimal>) analytics.get("account_balances");
        if (accountBalances.isEmpty()) {
            report.append("  Нет данных\n");
        } else {
            accountBalances.forEach((acc, bal) -> report.append(String.format("  %s: %.2f\n", acc, bal)));
        }

        report.append("\nСАМЫЕ КРУПНЫЕ ОПЕРАЦИИ:\n");
        List<Operation> topOperations = (List<Operation>) analytics.get("top_operations");
        if (topOperations.isEmpty()) {
            report.append("  Нет данных\n");
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            for (Operation op : topOperations) {
                String categoryName = categoryService.getCategory(op.getCategoryId())
                        .map(Category::getName)
                        .orElse("Неизвестно");
                report.append(String.format("  %s %.2f (%s) - %s\n",
                        op.getType().getDescription(), op.getAmount(),
                        op.getDate().format(formatter), categoryName));
            }
        }

        return report.toString();
    }
}