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
        
        // Инициализация в конструкторе вместо @PostConstruct
        init();
    }
    
    private void init() {
        setTitle("КосмоСкан v1.0");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        
        // Создание панели вкладок
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        tabbedPane.addTab("Студент", studentPanel);
        tabbedPane.addTab("Преподаватель", teacherPanel);
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Статусная строка
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createLoweredBevelBorder());
        
        JLabel statusLabel = new JLabel("Готов к работе");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        
        JLabel versionLabel = new JLabel("v1.0.0");
        versionLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        
        statusBar.add(statusLabel, BorderLayout.WEST);
        statusBar.add(versionLabel, BorderLayout.EAST);
        
        add(statusBar, BorderLayout.SOUTH);
    }
}
