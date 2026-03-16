package com.tigerbank.importer;

import com.tigerbank.domain.BankAccount;
import com.tigerbank.domain.Category;
import com.tigerbank.domain.Operation;
import com.tigerbank.enums.OperationType;
import com.tigerbank.service.AccountService;
import com.tigerbank.service.CategoryService;
import com.tigerbank.service.OperationService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CsvImporter extends DataImporter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public CsvImporter(AccountService accountService,
            CategoryService categoryService,
            OperationService operationService) {
        super(accountService, categoryService, operationService);
    }

    @Override
    protected ParsedData parseData(String rawData) {
        List<BankAccount> accounts = new ArrayList<>();
        List<Category> categories = new ArrayList<>();
        List<Operation> operations = new ArrayList<>();

        String[] lines = rawData.split("\n");
        String currentSection = "";

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty())
                continue;

            if (line.startsWith("#")) {
                currentSection = line.substring(1).trim();
                continue;
            }

            String[] values = line.split(",");

            switch (currentSection) {
                case "ACCOUNTS":
                    if (values.length >= 3) {
                        accounts.add(new BankAccount(
                                UUID.fromString(values[0]),
                                values[1],
                                new BigDecimal(values[2])));
                    }
                    break;

                case "CATEGORIES":
                    if (values.length >= 3) {
                        categories.add(new Category(
                                UUID.fromString(values[0]),
                                OperationType.valueOf(values[1]),
                                values[2]));
                    }
                    break;

                case "OPERATIONS":
                    if (values.length >= 7) {
                        operations.add(new Operation(
                                UUID.fromString(values[0]),
                                OperationType.valueOf(values[1]),
                                values[2].isEmpty() ? null : UUID.fromString(values[2]),
                                new BigDecimal(values[3]),
                                LocalDateTime.parse(values[4], DATE_FORMATTER),
                                values[5],
                                values[6].isEmpty() ? null : UUID.fromString(values[6])));
                    }
                    break;
            }
        }

        return new ParsedData(accounts, categories, operations);
    }
}