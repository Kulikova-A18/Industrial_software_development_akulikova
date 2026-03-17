package com.tigerbank.importer;

import com.tigerbank.domain.BankAccount;
import com.tigerbank.domain.Category;
import com.tigerbank.domain.Operation;
import com.tigerbank.enums.OperationType;
import com.tigerbank.service.AccountService;
import com.tigerbank.service.CategoryService;
import com.tigerbank.service.OperationService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JsonImporter extends DataImporter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public JsonImporter(AccountService accountService,
            CategoryService categoryService,
            OperationService operationService) {
        super(accountService, categoryService, operationService);
    }

    @Override
    protected ParsedData parseData(String rawData) {
        List<BankAccount> accounts = new ArrayList<>();
        List<Category> categories = new ArrayList<>();
        List<Operation> operations = new ArrayList<>();

        JSONObject root = new JSONObject(rawData);

        if (root.has("accounts")) {
            JSONArray accountsArray = root.getJSONArray("accounts");
            for (int i = 0; i < accountsArray.length(); i++) {
                JSONObject acc = accountsArray.getJSONObject(i);
                accounts.add(new BankAccount(
                        UUID.fromString(acc.getString("id")),
                        acc.getString("name"),
                        new BigDecimal(acc.getString("balance"))));
            }
        }

        if (root.has("categories")) {
            JSONArray categoriesArray = root.getJSONArray("categories");
            for (int i = 0; i < categoriesArray.length(); i++) {
                JSONObject cat = categoriesArray.getJSONObject(i);
                categories.add(new Category(
                        UUID.fromString(cat.getString("id")),
                        OperationType.valueOf(cat.getString("type")),
                        cat.getString("name")));
            }
        }

        if (root.has("operations")) {
            JSONArray operationsArray = root.getJSONArray("operations");
            for (int i = 0; i < operationsArray.length(); i++) {
                JSONObject op = operationsArray.getJSONObject(i);
                operations.add(new Operation(
                        UUID.fromString(op.getString("id")),
                        OperationType.valueOf(op.getString("type")),
                        op.has("bankAccountId") ? UUID.fromString(op.getString("bankAccountId")) : null,
                        new BigDecimal(op.getString("amount")),
                        LocalDateTime.parse(op.getString("date"), DATE_FORMATTER),
                        op.optString("description", ""),
                        op.has("categoryId") ? UUID.fromString(op.getString("categoryId")) : null));
            }
        }

        return new ParsedData(accounts, categories, operations);
    }
}