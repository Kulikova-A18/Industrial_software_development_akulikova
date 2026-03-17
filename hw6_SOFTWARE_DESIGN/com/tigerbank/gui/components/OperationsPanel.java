package com.tigerbank.gui.components;

import com.tigerbank.domain.BankAccount;
import com.tigerbank.domain.Category;
import com.tigerbank.domain.Operation;
import com.tigerbank.enums.OperationType;
import com.tigerbank.gui.dialogs.OperationDialog;
import com.tigerbank.service.AccountService;
import com.tigerbank.service.CategoryService;
import com.tigerbank.service.OperationService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class OperationsPanel extends JPanel {
    private final OperationService operationService;
    private final AccountService accountService;
    private final CategoryService categoryService;

    private JTable operationsTable;
    private DefaultTableModel operationsModel;

    public OperationsPanel(OperationService operationService,
            AccountService accountService,
            CategoryService categoryService) {
        this.operationService = operationService;
        this.accountService = accountService;
        this.categoryService = categoryService;

        setLayout(new BorderLayout());
        initializeUI();
    }

    private void initializeUI() {
        String[] columns = { "ID", "Тип", "Счет", "Категория", "Сумма", "Дата", "Описание" };
        operationsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        operationsTable = new JTable(operationsModel);
        operationsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(operationsModel);
        operationsTable.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(operationsTable);

        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton addButton = new JButton("Добавить операцию");
        addButton.addActionListener(e -> showAddOperationDialog());

        JButton deleteButton = new JButton("Удалить");
        deleteButton.addActionListener(e -> deleteSelectedOperation());

        JButton filterButton = new JButton("Фильтр по дате");
        filterButton.addActionListener(e -> showFilterDialog());

        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(filterButton);

        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        refreshTable();
    }

    public void refreshTable() {
        operationsModel.setRowCount(0);
        for (Operation operation : operationService.getAllOperations()) {
            String accountName = accountService.getAccount(operation.getBankAccountId())
                    .map(BankAccount::getName)
                    .orElse("Неизвестный счет");

            String categoryName = categoryService.getCategory(operation.getCategoryId())
                    .map(Category::getName)
                    .orElse("Неизвестная категория");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

            operationsModel.addRow(new Object[] {
                    operation.getId().toString().substring(0, 8) + "...",
                    operation.getType().getDescription(),
                    accountName,
                    categoryName,
                    String.format("%.2f", operation.getAmount()),
                    operation.getDate().format(formatter),
                    operation.getDescription()
            });
        }
    }

    private void showAddOperationDialog() {
        OperationDialog.showAddDialog(this, operationService, accountService,
                categoryService, this::refreshTable);
    }

    private void showFilterDialog() {
        JOptionPane.showMessageDialog(this,
                "Функция фильтрации будет реализована в следующей версии",
                "Информация", JOptionPane.INFORMATION_MESSAGE);
    }

    private void deleteSelectedOperation() {
        int selectedRow = operationsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Выберите операцию для удаления",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String operationIdStr = (String) operationsModel.getValueAt(selectedRow, 0);
        Operation operation = findOperationByShortId(operationIdStr);

        if (operation != null) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    String.format("Удалить операцию?\n%s: %.2f - %s",
                            operation.getType().getDescription(),
                            operation.getAmount(),
                            operation.getDescription() != null ? operation.getDescription() : "без описания"),
                    "Подтверждение удаления", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                if (operationService.deleteOperation(operation.getId())) {
                    refreshTable();
                    JOptionPane.showMessageDialog(this, "Операция удалена!");
                } else {
                    JOptionPane.showMessageDialog(this, "Не удалось удалить операцию",
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private Operation findOperationByShortId(String shortId) {
        for (Operation operation : operationService.getAllOperations()) {
            if (operation.getId().toString().startsWith(shortId.replace("...", ""))) {
                return operation;
            }
        }
        return null;
    }
}