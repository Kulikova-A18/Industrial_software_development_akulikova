package com.tigerbank.exporter;

import com.tigerbank.domain.BankAccount;
import com.tigerbank.domain.Category;
import com.tigerbank.domain.Operation;
import com.tigerbank.di.Singleton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Singleton
public class JsonExporter implements DataExporter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void exportAccounts(List<BankAccount> accounts, String filePath) {
        JSONArray jsonArray = new JSONArray();

        for (BankAccount account : accounts) {
            JSONObject json = new JSONObject();
            json.put("id", account.getId().toString());
            json.put("name", account.getName());
            json.put("balance", account.getBalance().toString());
            jsonArray.put(json);
        }

        writeToFile(jsonArray.toString(2), filePath);
    }

    @Override
    public void exportCategories(List<Category> categories, String filePath) {
        JSONArray jsonArray = new JSONArray();

        for (Category category : categories) {
            JSONObject json = new JSONObject();
            json.put("id", category.getId().toString());
            json.put("type", category.getType().name());
            json.put("name", category.getName());
            jsonArray.put(json);
        }

        writeToFile(jsonArray.toString(2), filePath);
    }

    @Override
    public void exportOperations(List<Operation> operations, String filePath) {
        JSONArray jsonArray = new JSONArray();

        for (Operation op : operations) {
            JSONObject json = new JSONObject();
            json.put("id", op.getId().toString());
            json.put("type", op.getType().name());
            json.put("bankAccountId", op.getBankAccountId() != null ? op.getBankAccountId().toString() : "");
            json.put("amount", op.getAmount().toString());
            json.put("date", op.getDate().format(DATE_FORMATTER));
            json.put("description", op.getDescription() != null ? op.getDescription() : "");
            json.put("categoryId", op.getCategoryId() != null ? op.getCategoryId().toString() : "");
            jsonArray.put(json);
        }

        writeToFile(jsonArray.toString(2), filePath);
    }

    @Override
    public void exportAll(List<BankAccount> accounts,
            List<Category> categories,
            List<Operation> operations,
            String baseFileName) {
        JSONObject root = new JSONObject();

        JSONArray accountsArray = new JSONArray();
        for (BankAccount acc : accounts) {
            JSONObject json = new JSONObject();
            json.put("id", acc.getId().toString());
            json.put("name", acc.getName());
            json.put("balance", acc.getBalance().toString());
            accountsArray.put(json);
        }
        root.put("accounts", accountsArray);

        JSONArray categoriesArray = new JSONArray();
        for (Category cat : categories) {
            JSONObject json = new JSONObject();
            json.put("id", cat.getId().toString());
            json.put("type", cat.getType().name());
            json.put("name", cat.getName());
            categoriesArray.put(json);
        }
        root.put("categories", categoriesArray);

        JSONArray operationsArray = new JSONArray();
        for (Operation op : operations) {
            JSONObject json = new JSONObject();
            json.put("id", op.getId().toString());
            json.put("type", op.getType().name());
            json.put("bankAccountId", op.getBankAccountId() != null ? op.getBankAccountId().toString() : "");
            json.put("amount", op.getAmount().toString());
            json.put("date", op.getDate().format(DATE_FORMATTER));
            json.put("description", op.getDescription() != null ? op.getDescription() : "");
            json.put("categoryId", op.getCategoryId() != null ? op.getCategoryId().toString() : "");
            operationsArray.put(json);
        }
        root.put("operations", operationsArray);

        writeToFile(root.toString(2), baseFileName + "_all.json");
    }

    private void writeToFile(String content, String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println(content);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка записи в файл: " + e.getMessage(), e);
        }
    }
}