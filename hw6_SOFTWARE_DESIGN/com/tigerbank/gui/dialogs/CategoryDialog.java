package com.tigerbank.gui.dialogs;

import com.tigerbank.enums.OperationType;
import com.tigerbank.service.CategoryService;
import javax.swing.*;
import java.awt.*;

public class CategoryDialog {

    public static void showAddDialog(JComponent parent, CategoryService categoryService, Runnable refreshCallback) {
        JComboBox<OperationType> typeCombo = new JComboBox<>(OperationType.values());
        JTextField nameField = new JTextField(20);

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.add(new JLabel("Тип категории:"));
        panel.add(typeCombo);
        panel.add(new JLabel("Название категории:"));
        panel.add(nameField);

        int result = JOptionPane.showConfirmDialog(parent, panel,
                "Добавление категории", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            OperationType type = (OperationType) typeCombo.getSelectedItem();
            String name = nameField.getText().trim();

            if (!name.isEmpty()) {
                boolean exists = categoryService.getAllCategories().stream()
                        .anyMatch(c -> c.getType() == type && c.getName().equalsIgnoreCase(name));

                if (exists) {
                    JOptionPane.showMessageDialog(parent, "Такая категория уже существует",
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                } else {
                    categoryService.createCategory(type, name);
                    refreshCallback.run();
                    JOptionPane.showMessageDialog(parent, "Категория добавлена успешно!");
                }
            } else {
                JOptionPane.showMessageDialog(parent, "Введите название категории",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}