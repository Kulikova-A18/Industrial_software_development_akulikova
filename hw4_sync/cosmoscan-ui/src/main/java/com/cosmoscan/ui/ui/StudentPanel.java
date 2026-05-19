package com.cosmoscan.ui.ui;

import com.cosmoscan.ui.service.ApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
import java.util.Map;

@Component
@Slf4j
public class StudentPanel extends JPanel {
    
    private final ApiService apiService;
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
        setLayout(new BorderLayout());
        
        // Верхняя панель с формой
        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBorder(new TitledBorder("Загрузка работы"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // ФИО
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.1;
        topPanel.add(new JLabel("ФИО студента:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.9;
        nameField = new JTextField(30);
        nameField.setToolTipText("Введите фамилию, имя и отчество студента");
        topPanel.add(nameField, gbc);
        
        // Файл
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.1;
        topPanel.add(new JLabel("Файл работы:"), gbc);
        
        JPanel filePanel = new JPanel(new BorderLayout(5, 0));
        fileField = new JTextField(20);
        fileField.setEditable(false);
        filePanel.add(fileField, BorderLayout.CENTER);
        
        chooseBtn = new JButton("Выбрать файл");
        chooseBtn.addActionListener(e -> chooseFile());
        filePanel.add(chooseBtn, BorderLayout.EAST);
        
        gbc.gridx = 1;
        gbc.weightx = 0.9;
        topPanel.add(filePanel, gbc);
        
        // Кнопки
        gbc.gridx = 1;
        gbc.gridy = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        submitBtn = new JButton("Отправить работу");
        submitBtn.setEnabled(false);
        submitBtn.addActionListener(e -> submitWork());
        buttonPanel.add(submitBtn);
        
        JButton clearBtn = new JButton("Очистить");
        clearBtn.addActionListener(e -> clearForm());
        buttonPanel.add(clearBtn);
        
        topPanel.add(buttonPanel, gbc);
        
        add(topPanel, BorderLayout.NORTH);
        
        // Информационная панель
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(new TitledBorder("Требования к файлу"));
        
        JTextArea reqText = new JTextArea(
                "• Допустимые форматы: PDF, DOCX, TXT\n" +
                "• Архивы (ZIP, RAR и др.) не принимаются\n" +
                "• Максимальный размер файла: 1 МБ\n" +
                "• Имя файла должно содержать расширение"
        );
        reqText.setEditable(false);
        reqText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        reqText.setBackground(getBackground());
        
        infoPanel.add(reqText, BorderLayout.CENTER);
        add(infoPanel, BorderLayout.CENTER);
        
        // Лог
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(new TitledBorder("Лог операций"));
        
        logArea = new JTextArea(8, 40);
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(logArea);
        bottomPanel.add(scrollPane, BorderLayout.CENTER);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void chooseFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Выберите файл работы");
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            fileField.setText(selectedFile.getAbsolutePath());
            submitBtn.setEnabled(true);
            logMessage("Выбран файл: " + selectedFile.getName() + 
                       " (" + (selectedFile.length() / 1024) + " КБ)");
        }
    }
    
    private void submitWork() {
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Введите ФИО студента", 
                                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(this, "Выберите файл", 
                                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        submitBtn.setEnabled(false);
        chooseBtn.setEnabled(false);
        submitBtn.setText("Отправка...");
        
        new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    String response = apiService.uploadWork(nameField.getText().trim(), selectedFile);
                    publish("✓ Работа успешно отправлена");
                    publish("Ответ сервера: " + response);
                    
                    SwingUtilities.invokeLater(() -> clearForm());
                    
                } catch (Exception e) {
                    publish("✗ Ошибка: " + e.getMessage());
                }
                return null;
            }
            
            @Override
            protected void process(java.util.List<String> chunks) {
                chunks.forEach(msg -> logMessage(msg));
            }
            
            @Override
            protected void done() {
                submitBtn.setEnabled(true);
                chooseBtn.setEnabled(true);
                submitBtn.setText("Отправить работу");
            }
        }.execute();
    }
    
    private void clearForm() {
        nameField.setText("");
        fileField.setText("");
        selectedFile = null;
        submitBtn.setEnabled(false);
    }
    
    private void logMessage(String message) {
        String time = java.time.LocalTime.now().toString().substring(0, 8);
        logArea.append("[" + time + "] " + message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}
