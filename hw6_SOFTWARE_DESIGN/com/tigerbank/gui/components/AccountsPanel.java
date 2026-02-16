package com.tigerbank.gui.components;

import com.tigerbank.domain.BankAccount;
import com.tigerbank.domain.Operation;
import com.tigerbank.enums.OperationType;
import com.tigerbank.gui.dialogs.AccountDialog;
import com.tigerbank.service.AccountService;
import com.tigerbank.service.OperationService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AccountsPanel extends JPanel {
    private final AccountService accountService;
    private final OperationService operationService;

    private JTable accountsTable;
    private DefaultTableModel accountsModel;

    public AccountsPanel(AccountService accountService, OperationService operationService) {
        this.accountService = accountService;
        this.operationService = operationService;

        setLayout(new BorderLayout());
        initializeUI();
    }

    private void initializeUI() {
        String[] columns = { "ID", "Название", "Баланс", "Кол-во операций" };
        accountsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        accountsTable = new JTable(accountsModel);
        accountsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(accountsTable);

        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton addButton = new JButton("Добавить счет");
        addButton.addActionListener(e -> showAddAccountDialog());

        JButton editButton = new JButton("Редактировать");
        editButton.addActionListener(e -> showEditAccountDialog());

        JButton deleteButton = new JButton("Удалить");
        deleteButton.addActionListener(e -> deleteSelectedAccount());

        JButton operationsButton = new JButton("Операции по счету");
        operationsButton.addActionListener(e -> showAccountOperationsDialog());

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(operationsButton);

        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        refreshTable();
    }

    public void refreshTable() {
        accountsModel.setRowCount(0);
        for (BankAccount account : accountService.getAllAccounts()) {
            int operationCount = operationService.getOperationsByAccount(account.getId()).size();
            accountsModel.addRow(new Object[] {
                    account.getId().toString().substring(0, 8) + "...",
                    account.getName(),
                    String.format("%.2f", account.getBalance()),
                    operationCount
            });
        }
    }

    private void showAddAccountDialog() {
        AccountDialog.showAddDialog(this, accountService, this::refreshTable);
    }

    private void showEditAccountDialog() {
        int selectedRow = accountsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Выберите счет для редактирования",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String accountIdStr = (String) accountsModel.getValueAt(selectedRow, 0);
        BankAccount account = findAccountByShortId(accountIdStr);

        if (account != null) {
            AccountDialog.showEditDialog(this, accountService, account, this::refreshTable);
        }
    }

    private void showAccountOperationsDialog() {
        int selectedRow = accountsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Выберите счет",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String accountIdStr = (String) accountsModel.getValueAt(selectedRow, 0);
        BankAccount account = findAccountByShortId(accountIdStr);

        if (account != null) {
            List<Operation> operations = operationService.getOperationsByAccount(account.getId());

            StringBuilder sb = new StringBuilder();
            sb.append("Операции по счету: ").append(account.getName()).append("\n");
            sb.append("Баланс: ").append(String.format("%.2f", account.getBalance())).append("\n\n");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

            if (operations.isEmpty()) {
                sb.append("Нет операций");
            } else {
                BigDecimal totalIncome = BigDecimal.ZERO;
                BigDecimal totalExpense = BigDecimal.ZERO;

                for (Operation op : operations) {
                    if (op.getType() == OperationType.INCOME) {
                        totalIncome = totalIncome.add(op.getAmount());
                    } else {
                        totalExpense = totalExpense.add(op.getAmount());
                    }

                    sb.append(String.format("%s %s: %.2f - %s\n",
                            op.getDate().format(formatter),
                            op.getType().getDescription(),
                            op.getAmount(),
                            op.getDescription() != null ? op.getDescription() : "без описания"));
                }

                sb.append("\nИтого:\n");
                sb.append(String.format("Доходы: %.2f\n", totalIncome));
                sb.append(String.format("Расходы: %.2f\n", totalExpense));
                sb.append(String.format("Баланс: %.2f\n", totalIncome.subtract(totalExpense)));
            }

            JTextArea textArea = new JTextArea(sb.toString(), 20, 50);
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);

            JOptionPane.showMessageDialog(this, scrollPane,
                    "Операции по счету", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void deleteSelectedAccount() {
        int selectedRow = accountsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Выберите счет для удаления",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String accountIdStr = (String) accountsModel.getValueAt(selectedRow, 0);
        BankAccount account = findAccountByShortId(accountIdStr);

        if (account != null) {
            int operationCount = operationService.getOperationsByAccount(account.getId()).size();

            if (operationCount > 0) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "У счета есть " + operationCount + " операций. Удалить счет вместе с операциями?",
                        "Подтверждение удаления", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    for (Operation op : operationService.getOperationsByAccount(account.getId())) {
                        operationService.deleteOperation(op.getId());
                    }
                    accountService.deleteAccount(account.getId());
                    refreshTable();
                    JOptionPane.showMessageDialog(this, "Счет и операции удалены!");
                }
            } else {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Удалить счет \"" + account.getName() + "\"?",
                        "Подтверждение удаления", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    if (accountService.deleteAccount(account.getId())) {
                        refreshTable();
                        JOptionPane.showMessageDialog(this, "Счет удален!");
                    } else {
                        JOptionPane.showMessageDialog(this, "Не удалось удалить счет",
                                "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }

    private BankAccount findAccountByShortId(String shortId) {
        for (BankAccount account : accountService.getAllAccounts()) {
            if (account.getId().toString().startsWith(shortId.replace("...", ""))) {
                return account;
            }
        }
        return null;
    }
}