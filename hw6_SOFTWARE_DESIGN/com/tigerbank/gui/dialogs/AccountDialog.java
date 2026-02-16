package com.tigerbank.gui.dialogs;

import com.tigerbank.domain.BankAccount;
import com.tigerbank.service.AccountService;
import javax.swing.*;
import java.awt.*;

public class AccountDialog {

    public static void showAddDialog(JComponent parent, AccountService accountService, Runnable refreshCallback) {
        JTextField nameField = new JTextField(20);

        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.add(new JLabel("Название счета:"));
        panel.add(nameField);

        int result = JOptionPane.showConfirmDialog(parent, panel,
                "Добавление счета", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            if (!name.isEmpty()) {
                accountService.createAccount(name);
                refreshCallback.run();
                JOptionPane.showMessageDialog(parent, "Счет добавлен успешно!");
            } else {
                JOptionPane.showMessageDialog(parent, "Введите название счета",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void showEditDialog(JComponent parent, AccountService accountService,
            BankAccount account, Runnable refreshCallback) {
        JTextField nameField = new JTextField(account.getName(), 20);

        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.add(new JLabel("Название счета:"));
        panel.add(nameField);
        panel.add(new JLabel("Текущий баланс:"));
        panel.add(new JLabel(String.format("%.2f", account.getBalance())));

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