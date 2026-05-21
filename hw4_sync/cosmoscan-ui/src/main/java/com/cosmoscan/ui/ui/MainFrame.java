package com.cosmoscan.ui.ui;

import org.springframework.stereotype.Component;
import javax.swing.*;
import java.awt.*;

@Component
public class MainFrame extends JFrame {
    
    private final StudentPanel studentPanel;
    private final TeacherPanel teacherPanel;
    
    public MainFrame(StudentPanel studentPanel, TeacherPanel teacherPanel) {
        this.studentPanel = studentPanel;
        this.teacherPanel = teacherPanel;
        init();
    }
    
    private void init() {
        setTitle("КосмоСкан v1.0 - Система проверки работ");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 750);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("📤 Студент", studentPanel);
        tabs.addTab("📊 Преподаватель", teacherPanel);
        
        add(tabs, BorderLayout.CENTER);
        
        // Статус бар
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createLoweredBevelBorder());
        JLabel statusLabel = new JLabel(" ✅ Готов к работе | API: http://localhost:8080");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
        statusBar.add(statusLabel, BorderLayout.WEST);
        add(statusBar, BorderLayout.SOUTH);
    }
}
