package com.tigerbank.gui.dialogs;

import com.tigerbank.command.CommandExecutor;
import com.tigerbank.command.CreateAccountCommand;
import com.tigerbank.domain.BankAccount;
import com.tigerbank.service.AccountService;
import javax.swing.*;
import java.awt.*;

public class AccountDialog {

    public static void showAddDialog(JComponent parent,
            AccountService accountService,
            CommandExecutor commandExecutor,
            Runnable refreshCallback) {
        JTextField nameField = new JTextField(20);

        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.add(new JLabel("Название счета:"));
        panel.add(nameField);

        int result = JOptionPane.showConfirmDialog(parent, panel,
                "Добавление счета", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            if (!name.isEmpty()) {
                CreateAccountCommand command = new CreateAccountCommand(accountService, name);

                long startTime = System.nanoTime();
                commandExecutor.executeCommand(command);
                long endTime = System.nanoTime();

                refreshCallback.run();

                JOptionPane.showMessageDialog(parent,
                        String.format("Счет добавлен успешно!\nВремя выполнения: %.2f мс",
                                (endTime - startTime) / 1_000_000.0));
            } else {
                JOptionPane.showMessageDialog(parent, "Введите название счета",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void showEditDialog(JComponent parent,
            AccountService accountService,
            BankAccount account,
            Runnable refreshCallback) {
        JTextField nameField = new JTextField(account.getName(), 20);

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.add(new JLabel("Название счета:"));
        panel.add(nameField);
        panel.add(new JLabel("Текущий баланс:"));
        panel.add(new JLabel(String.format("%.2f", account.getBalance())));
        panel.add(new JLabel("ID:"));
        panel.add(new JLabel(account.getId().toString().substring(0, 8) + "..."));

        int result = JOptionPane.showConfirmDialog(parent, panel,
                "Редактирование счета", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            String newName = nameField.getText().trim();
            if (!newName.isEmpty()) {
                account.setName(newName);
                refreshCallback.run();
                JOptionPane.showMessageDialog(parent, "Счет обновлен успешно!");
            }
        }
    }
}