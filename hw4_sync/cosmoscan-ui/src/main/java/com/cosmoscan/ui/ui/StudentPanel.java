package com.cosmoscan.ui.ui;

import com.cosmoscan.ui.service.ApiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.io.File;
import java.util.Map;

@Component
@Slf4j
public class StudentPanel extends JPanel {
    
    private final ApiService apiService;
    private final ObjectMapper mapper = new ObjectMapper();
    
    private JTextField nameField;
    private JTextField fileField;
    private JButton chooseBtn;
    private JButton submitBtn;
    private JTextArea logArea;
    private File selectedFile;
    
    public StudentPanel(ApiService apiService) {
        this.apiService = apiService;
        initComponents();
    }
    
    private void initComponents() {
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setLayout(new BorderLayout(10, 10));
        
        // Форма
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new TitledBorder("Загрузка работы"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("ФИО студента:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        nameField = new JTextField(30);
        formPanel.add(nameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        formPanel.add(new JLabel("Файл:"), gbc);
        gbc.gridx = 1;
        JPanel filePanel = new JPanel(new BorderLayout(5, 0));
        fileField = new JTextField();
        fileField.setEditable(false);
        filePanel.add(fileField, BorderLayout.CENTER);
        chooseBtn = new JButton("Выбрать");
        chooseBtn.addActionListener(e -> chooseFile());
        filePanel.add(chooseBtn, BorderLayout.EAST);
        formPanel.add(filePanel, gbc);
        
        gbc.gridx = 1; gbc.gridy = 2;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        submitBtn = new JButton("Отправить");
        submitBtn.setEnabled(false);
        submitBtn.addActionListener(e -> submitWork());
        btnPanel.add(submitBtn);
        JButton clearBtn = new JButton("Очистить");
        clearBtn.addActionListener(e -> clearForm());
        btnPanel.add(clearBtn);
        formPanel.add(btnPanel, gbc);
        
        add(formPanel, BorderLayout.NORTH);
        
        // Инфо панель
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(new TitledBorder("Требования"));
        JTextArea info = new JTextArea(
            "✓ Форматы: PDF, DOCX, TXT\n" +
            "✓ Максимальный размер: 1 МБ\n" +
            "✗ Архивы не принимаются"
        );
        info.setEditable(false);
        info.setBackground(null);
        infoPanel.add(info, BorderLayout.CENTER);
        add(infoPanel, BorderLayout.CENTER);
        
        // Лог
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(new TitledBorder("Лог"));
        logArea = new JTextArea(8, 40);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        logPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);
        add(logPanel, BorderLayout.SOUTH);
    }
    
    private void chooseFile() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedFile = chooser.getSelectedFile();
            fileField.setText(selectedFile.getName());
            submitBtn.setEnabled(true);
            logMessage("Выбран файл: " + selectedFile.getName() + 
                       " (" + (selectedFile.length() / 1024) + " KB)");
        }
    }
    
    private void submitWork() {
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Введите ФИО студента", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(this, "Выберите файл", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        setLoading(true);
        
        new SwingWorker<String, String>() {
            @Override
            protected String doInBackground() throws Exception {
                return apiService.uploadWork(nameField.getText().trim(), selectedFile);
            }
            
            @Override
            protected void process(java.util.List<String> chunks) {
                chunks.forEach(StudentPanel.this::logMessage);
            }
            
            @Override
            protected void done() {
                setLoading(false);
                try {
                    String response = get();
                    logMessage("✓ Успешно отправлено!");
                    logMessage("Ответ: " + response);
                    
                    // Парсим workId
                    try {
                        Map<?, ?> map = mapper.readValue(response, Map.class);
                        Object workId = map.get("workId");
                        if (workId != null) {
                            logMessage("📌 ID работы: " + workId);
                            JOptionPane.showMessageDialog(StudentPanel.this,
                                "Работа успешно отправлена!\n\nID работы: " + workId +
                                "\nСохраните этот ID для проверки отчёта.",
                                "Успех", JOptionPane.INFORMATION_MESSAGE);
                        }
                    } catch (Exception e) {
                        logMessage("Ответ сервера: " + response);
                    }
                    clearForm();
                } catch (Exception e) {
                    logMessage("✗ Ошибка: " + e.getMessage());
                    JOptionPane.showMessageDialog(StudentPanel.this,
                        "Ошибка при отправке:\n" + e.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }
    
    private void setLoading(boolean loading) {
        submitBtn.setEnabled(!loading);
        chooseBtn.setEnabled(!loading);
        submitBtn.setText(loading ? "Отправка..." : "Отправить");
    }
    
    private void clearForm() {
        nameField.setText("");
        fileField.setText("");
        selectedFile = null;
        submitBtn.setEnabled(false);
    }
    
    private void logMessage(String msg) {
        String time = java.time.LocalTime.now().toString().substring(0, 8);
        logArea.append("[" + time + "] " + msg + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}
