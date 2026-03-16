package com.tigerbank.importer;

import com.tigerbank.domain.BankAccount;
import com.tigerbank.domain.Category;
import com.tigerbank.domain.Operation;
import com.tigerbank.service.AccountService;
import com.tigerbank.service.CategoryService;
import com.tigerbank.service.OperationService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public abstract class DataImporter {
    protected final AccountService accountService;
    protected final CategoryService categoryService;
    protected final OperationService operationService;

    public DataImporter(AccountService accountService,
            CategoryService categoryService,
            OperationService operationService) {
        this.accountService = accountService;
        this.categoryService = categoryService;
        this.operationService = operationService;
    }

    public final void importData(String filePath) {
        System.out.println("Начало импорта из файла: " + filePath);

        try {
            String rawData = readFile(filePath);
            ParsedData parsedData = parseData(rawData);
            saveToDomain(parsedData);

            System.out.println("Импорт завершен успешно.");
            System.out.printf("Импортировано: %d счетов, %d категорий, %d операций%n",
                    parsedData.accounts.size(),
                    parsedData.categories.size(),
                    parsedData.operations.size());

        } catch (Exception e) {
            System.err.println("Ошибка импорта: " + e.getMessage());
            throw new RuntimeException("Ошибка импорта данных", e);
        }
    }

    protected String readFile(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }

    protected abstract ParsedData parseData(String rawData);

    private void saveToDomain(ParsedData data) {
        for (BankAccount account : data.accounts) {
            accountService.createAccount(account.getName());
        }

        for (Category category : data.categories) {
            categoryService.createCategory(category.getType(), category.getName());
        }

        for (Operation op : data.operations) {
            operationService.createOperation(
                    op.getType(),
                    op.getBankAccountId(),
                    op.getAmount(),
                    op.getCategoryId(),
                    op.getDescription());
        }
    }

    protected static class ParsedData {
        public final List<BankAccount> accounts;
        public final List<Category> categories;
        public final List<Operation> operations;

        public ParsedData(List<BankAccount> accounts,
                List<Category> categories,
                List<Operation> operations) {
            this.accounts = accounts;
            this.categories = categories;
            this.operations = operations;
        }
    }
}