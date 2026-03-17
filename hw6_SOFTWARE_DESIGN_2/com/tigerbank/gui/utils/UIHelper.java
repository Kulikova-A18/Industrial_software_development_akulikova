package com.tigerbank.gui.utils;

import javax.swing.*;
import java.awt.*;

public class UIHelper {

    public static void showErrorDialog(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    public static void showInfoDialog(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Информация", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean showConfirmDialog(Component parent, String message) {
        int result = JOptionPane.showConfirmDialog(parent, message, "Подтверждение",
                JOptionPane.YES_NO_OPTION);
        return result == JOptionPane.YES_OPTION;
    }

    public static void createStyledLabel(JLabel label) {
        label.setFont(new Font("Arial", Font.BOLD, 12));
        label.setForeground(new Color(60, 60, 60));
    }

    public static void createStyledButton(JButton button) {
        button.setFont(new Font("Arial", Font.PLAIN, 12));
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
    }
}