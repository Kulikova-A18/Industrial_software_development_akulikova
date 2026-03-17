package com.tigerbank.gui.components;

import com.tigerbank.domain.Category;
import com.tigerbank.domain.Operation;
import com.tigerbank.enums.OperationType;
import com.tigerbank.gui.dialogs.CategoryDialog;
import com.tigerbank.service.CategoryService;
import com.tigerbank.service.OperationService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CategoriesPanel extends JPanel {
    private final CategoryService categoryService;
    private final OperationService operationService;

    private JTable categoriesTable;
    private DefaultTableModel categoriesModel;

    public CategoriesPanel(CategoryService categoryService, OperationService operationService) {
        this.categoryService = categoryService;
        this.operationService = operationService;

        setLayout(new BorderLayout());
        initializeUI();
    }

    private void initializeUI() {
        String[] columns = { "ID", "Тип", "Название", "Кол-во операций" };
        categoriesModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        categoriesTable = new JTable(categoriesModel);
        categoriesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(categoriesTable);

        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton addButton = new JButton("Добавить категорию");
        addButton.addActionListener(e -> showAddCategoryDialog());

        JButton deleteButton = new JButton("Удалить");
        deleteButton.addActionListener(e -> deleteSelectedCategory());

        JButton operationsButton = new JButton("Операции по категории");
        operationsButton.addActionListener(e -> showCategoryOperationsDialog());

        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(operationsButton);

        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        refreshTable();
    }

    public void refreshTable() {
        categoriesModel.setRowCount(0);
        for (Category category : categoryService.getAllCategories()) {
            int operationCount = operationService.getOperationsByCategory(category.getId()).size();
            categoriesModel.addRow(new Object[] {
                    category.getId().toString().substring(0, 8) + "...",
                    category.getType().getDescription(),
                    category.getName(),
                    operationCount
            });
        }
    }

    private void showAddCategoryDialog() {
        CategoryDialog.showAddDialog(this, categoryService, this::refreshTable);
    }

    private void showCategoryOperationsDialog() {
        int selectedRow = categoriesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Выберите категорию",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String categoryIdStr = (String) categoriesModel.getValueAt(selectedRow, 0);
        Category category = findCategoryByShortId(categoryIdStr);

        if (category != null) {
            List<Operation> operations = operationService.getOperationsByCategory(category.getId());

            StringBuilder sb = new StringBuilder();
            sb.append("Операции по категории: ").append(category.getName()).append("\n");
            sb.append("Тип: ").append(category.getType().getDescription()).append("\n\n");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            BigDecimal total = BigDecimal.ZERO;

            if (operations.isEmpty()) {
                sb.append("Нет операций");
            } else {
                for (Operation op : operations) {
                    total = total.add(op.getAmount());
                    sb.append(String.format("%s: %.2f - %s\n",
                            op.getDate().format(formatter),
                            op.getAmount(),
                            op.getDescription() != null ? op.getDescription() : "без описания"));
                }

                sb.append("\nИтого: ").append(String.format("%.2f", total));
            }

            JTextArea textArea = new JTextArea(sb.toString(), 15, 50);
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);

            JOptionPane.showMessageDialog(this, scrollPane,
                    "Операции по категории", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void deleteSelectedCategory() {
        int selectedRow = categoriesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Выберите категорию для удаления",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String categoryIdStr = (String) categoriesModel.getValueAt(selectedRow, 0);
        Category category = findCategoryByShortId(categoryIdStr);

        if (category != null) {
            int operationCount = operationService.getOperationsByCategory(category.getId()).size();

            if (operationCount > 0) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "У категории есть " + operationCount + " операций. Удалить категорию?\n" +
                                "Операции останутся без категории.",
                        "Подтверждение удаления", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    categoryService.deleteCategory(category.getId());
                    refreshTable();
                    JOptionPane.showMessageDialog(this, "Категория удалена!");
                }
            } else {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Удалить категорию \"" + category.getName() + "\"?",
                        "Подтверждение удаления", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    categoryService.deleteCategory(category.getId());
                    refreshTable();
                    JOptionPane.showMessageDialog(this, "Категория удалена!");
                }
            }
        }
    }

    private Category findCategoryByShortId(String shortId) {
        for (Category category : categoryService.getAllCategories()) {
            if (category.getId().toString().startsWith(shortId.replace("...", ""))) {
                return category;
            }
        }
        return null;
    }
}