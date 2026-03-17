package com.tigerbank.gui.dialogs;

import com.tigerbank.command.CommandExecutor;
import com.tigerbank.command.CreateOperationCommand;
import com.tigerbank.domain.BankAccount;
import com.tigerbank.domain.Category;
import com.tigerbank.enums.OperationType;
import com.tigerbank.service.AccountService;
import com.tigerbank.service.CategoryService;
import com.tigerbank.service.OperationService;
import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class OperationDialog {

    public static void showAddDialog(JComponent parent,
            OperationService operationService,
            AccountService accountService,
            CategoryService categoryService,
            CommandExecutor commandExecutor,
            Runnable refreshCallback) {
        List<BankAccount> accounts = accountService.getAllAccounts();
        List<Category> incomeCategories = categoryService.getCategoriesByType(OperationType.INCOME);
        List<Category> expenseCategories = categoryService.getCategoriesByType(OperationType.EXPENSE);

        if (accounts.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "Сначала создайте хотя бы один счет",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (incomeCategories.isEmpty() && expenseCategories.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "Сначала создайте хотя бы одну категорию",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JComboBox<BankAccount> accountCombo = new JComboBox<>(accounts.toArray(new BankAccount[0]));
        JComboBox<OperationType> typeCombo = new JComboBox<>(OperationType.values());
        JComboBox<Category> categoryCombo = new JComboBox<>();
        JTextField amountField = new JTextField(15);
        JTextArea descriptionArea = new JTextArea(3, 20);
        JScrollPane descriptionScroll = new JScrollPane(descriptionArea);

        typeCombo.addActionListener(e -> {
            categoryCombo.removeAllItems();
            OperationType selectedType = (OperationType) typeCombo.getSelectedItem();
            List<Category> categories = selectedType == OperationType.INCOME
                    ? incomeCategories
                    : expenseCategories;

            for (Category cat : categories) {
                categoryCombo.addItem(cat);
            }

            if (!categories.isEmpty()) {
                categoryCombo.setSelectedIndex(0);
            }
        });

        typeCombo.setSelectedItem(OperationType.INCOME);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Счет:"), gbc);
        gbc.gridx = 1;
        panel.add(accountCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Тип операции:"), gbc);
        gbc.gridx = 1;
        panel.add(typeCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Категория:"), gbc);
        gbc.gridx = 1;
        panel.add(categoryCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("Сумма:"), gbc);
        gbc.gridx = 1;
        panel.add(amountField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(new JLabel("Описание:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        panel.add(descriptionScroll, gbc);

        int result = JOptionPane.showConfirmDialog(parent, panel,
                "Добавление операции", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                BankAccount selectedAccount = (BankAccount) accountCombo.getSelectedItem();
                OperationType selectedType = (OperationType) typeCombo.getSelectedItem();
                Category selectedCategory = (Category) categoryCombo.getSelectedItem();

                String amountText = amountField.getText().trim().replace(",", ".");
                BigDecimal amount = new BigDecimal(amountText);
                String description = descriptionArea.getText().trim();

                if (selectedCategory == null) {
                    JOptionPane.showMessageDialog(parent, "Выберите категорию",
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    JOptionPane.showMessageDialog(parent, "Сумма должна быть положительной",
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                CreateOperationCommand command = new CreateOperationCommand(
                        operationService,
                        selectedType,
                        selectedAccount.getId(),
                        amount,
                        selectedCategory.getId(),
                        description);

                long startTime = System.nanoTime();
                commandExecutor.executeCommand(command);
                long endTime = System.nanoTime();

                refreshCallback.run();

                JOptionPane.showMessageDialog(parent,
                        String.format("Операция добавлена успешно!\nВремя выполнения: %.2f мс",
                                (endTime - startTime) / 1_000_000.0),
                        "Успех", JOptionPane.INFORMATION_MESSAGE);

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(parent, "Введите корректную сумму",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(parent, "Ошибка: " + e.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}