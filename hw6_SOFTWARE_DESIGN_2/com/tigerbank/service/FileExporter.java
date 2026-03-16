package com.tigerbank.service;

import com.tigerbank.domain.BankAccount;
import com.tigerbank.domain.Category;
import com.tigerbank.domain.Operation;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;

public class FileExporter {

    public void exportAllToCsv(String baseFileName,
            List<BankAccount> accounts,
            List<Category> categories,
            List<Operation> operations) {

        exportToCsv(accounts, baseFileName + "_accounts.csv",
                new String[] { "ID", "Name", "Balance" },
                acc -> new String[] { acc.getId().toString(), acc.getName(), acc.getBalance().toString() });

        exportToCsv(categories, baseFileName + "_categories.csv",
                new String[] { "ID", "Type", "Name" },
                cat -> new String[] { cat.getId().toString(), cat.getType().name(), cat.getName() });

        exportToCsv(operations, baseFileName + "_operations.csv",
                new String[] { "ID", "Type", "AccountID", "Amount", "Date", "Description", "CategoryID" },
                op -> {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    return new String[] {
                            op.getId().toString(),
                            op.getType().name(),
                            op.getBankAccountId() != null ? op.getBankAccountId().toString() : "",
                            op.getAmount().toString(),
                            op.getDate().format(formatter),
                            op.getDescription() != null ? op.getDescription() : "",
                            op.getCategoryId() != null ? op.getCategoryId().toString() : ""
                    };
                });
    }

    public void exportAllToJson(String baseFileName,
            List<BankAccount> accounts,
            List<Category> categories,
            List<Operation> operations) {

        exportToJson(accounts, baseFileName + "_accounts.json");
        exportToJson(categories, baseFileName + "_categories.json");
        exportToJson(operations, baseFileName + "_operations.json");
    }

    private <T> void exportToCsv(List<T> items, String fileName, String[] headers,
            Function<T, String[]> converter) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            writer.println(String.join(",", headers));
            for (T item : items) {
                writer.println(String.join(",", converter.apply(item)));
            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка экспорта в CSV: " + e.getMessage(), e);
        }
    }

    private void exportToJson(List<?> items, String fileName) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            writer.println("[");
            for (int i = 0; i < items.size(); i++) {
                Object item = items.get(i);
                String json;
                if (item instanceof BankAccount) {
                    json = ((BankAccount) item).toJson();
                } else if (item instanceof Category) {
                    json = ((Category) item).toJson();
                } else if (item instanceof Operation) {
                    json = ((Operation) item).toJson();
                } else {
                    continue;
                }

                writer.print("  " + json);
                if (i < items.size() - 1) {
                    writer.println(",");
                } else {
                    writer.println();
                }
            }
            writer.println("]");
        } catch (IOException e) {
            throw new RuntimeException("Ошибка экспорта в JSON: " + e.getMessage(), e);
        }
    }

    public void importFromCsv(String baseFileName) {
        System.out.println("Импорт из CSV файлов: " + baseFileName);
    }
}