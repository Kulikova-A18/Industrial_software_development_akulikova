package com.tigerbank.exporter;

import com.tigerbank.domain.BankAccount;
import com.tigerbank.domain.Category;
import com.tigerbank.domain.Operation;
import java.util.List;

public interface DataExporter {
    void exportAccounts(List<BankAccount> accounts, String filePath);

    void exportCategories(List<Category> categories, String filePath);

    void exportOperations(List<Operation> operations, String filePath);

    void exportAll(List<BankAccount> accounts,
            List<Category> categories,
            List<Operation> operations,
            String baseFileName);
}