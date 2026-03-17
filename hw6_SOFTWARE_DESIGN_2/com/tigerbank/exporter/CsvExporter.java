package com.tigerbank.exporter;

import com.tigerbank.domain.BankAccount;
import com.tigerbank.domain.Category;
import com.tigerbank.domain.Operation;
import com.tigerbank.di.Singleton;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Singleton
public class CsvExporter implements DataExporter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void exportAccounts(List<BankAccount> accounts, String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("# ACCOUNTS");
            writer.println("id,name,balance");

            for (BankAccount account : accounts) {
                writer.printf("%s,%s,%s%n",
                        account.getId(),
                        account.getName().replace(",", " "),
                        account.getBalance());
            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка экспорта счетов: " + e.getMessage(), e);
        }
    }

    @Override
    public void exportCategories(List<Category> categories, String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("# CATEGORIES");
            writer.println("id,type,name");

            for (Category category : categories) {
                writer.printf("%s,%s,%s%n",
                        category.getId(),
                        category.getType().name(),
                        category.getName().replace(",", " "));
            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка экспорта категорий: " + e.getMessage(), e);
        }
    }

    @Override
    public void exportOperations(List<Operation> operations, String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("# OPERATIONS");
            writer.println("id,type,bankAccountId,amount,date,description,categoryId");

            for (Operation op : operations) {
                writer.printf("%s,%s,%s,%s,%s,%s,%s%n",
                        op.getId(),
                        op.getType().name(),
                        op.getBankAccountId() != null ? op.getBankAccountId() : "",
                        op.getAmount(),
                        op.getDate().format(DATE_FORMATTER),
                        op.getDescription() != null ? op.getDescription().replace(",", " ") : "",
                        op.getCategoryId() != null ? op.getCategoryId() : "");
            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка экспорта операций: " + e.getMessage(), e);
        }
    }

    @Override
    public void exportAll(List<BankAccount> accounts,
            List<Category> categories,
            List<Operation> operations,
            String baseFileName) {
        exportAccounts(accounts, baseFileName + "_accounts.csv");
        exportCategories(categories, baseFileName + "_categories.csv");
        exportOperations(operations, baseFileName + "_operations.csv");
    }
}