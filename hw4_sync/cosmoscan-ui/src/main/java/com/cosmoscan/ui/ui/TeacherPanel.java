package com.cosmoscan.ui.ui;

import com.cosmoscan.ui.model.AnalysisReport;
import com.cosmoscan.ui.service.ApiService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

@Component
public class TeacherPanel extends JPanel {
    
    private final ApiService apiService;
    private final ObjectMapper mapper = new ObjectMapper();
    private JTextField workIdField;
    private JButton searchBtn;
    private JTable reportTable;
    private DefaultTableModel tableModel;
    private JTextArea detailsArea;
    
    public TeacherPanel(ApiService apiService) {
        this.apiService = apiService;
        initComponents();
    }
    
    private void initComponents() {
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setLayout(new BorderLayout());
        
        // Панель поиска
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(new TitledBorder("Поиск отчёта"));
        
        searchPanel.add(new JLabel("ID работы:"));
        workIdField = new JTextField(30);
        searchPanel.add(workIdField);
        
        searchBtn = new JButton("Получить отчёт");
        searchBtn.addActionListener(e -> searchReport());
        searchPanel.add(searchBtn);
        
        JButton clearBtn = new JButton("Очистить");
        clearBtn.addActionListener(e -> clearSearch());
        searchPanel.add(clearBtn);
        
        add(searchPanel, BorderLayout.NORTH);
        
        // Таблица результатов
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(new TitledBorder("Результаты проверки"));
        
        String[] columns = {"ID", "Статус", "Формат", "Размер", "Дата"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        reportTable = new JTable(tableModel);
        reportTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        reportTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        reportTable.setRowHeight(25);
        
        reportTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = reportTable.getSelectedRow();
                if (row >= 0) {
                    showDetails(row);
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(reportTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        add(tablePanel, BorderLayout.CENTER);
        
        // Детали
        JPanel detailsPanel = new JPanel(new BorderLayout());
        detailsPanel.setBorder(new TitledBorder("Детали отчёта"));
        
        detailsArea = new JTextArea(10, 40);
        detailsArea.setEditable(false);
        detailsArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        
        detailsPanel.add(new JScrollPane(detailsArea), BorderLayout.CENTER);
        
        add(detailsPanel, BorderLayout.SOUTH);
    }
    
    private void searchReport() {
        String workId = workIdField.getText().trim();
        if (workId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Введите ID работы", 
                                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        searchBtn.setEnabled(false);
        searchBtn.setText("Поиск...");
        
        new SwingWorker<List<AnalysisReport>, Void>() {
            @Override
            protected List<AnalysisReport> doInBackground() throws Exception {
                String response = apiService.getReport(workId);
                return mapper.readValue(response, new TypeReference<List<AnalysisReport>>() {});
            }
            
            @Override
            protected void done() {
                try {
                    List<AnalysisReport> reports = get();
                    tableModel.setRowCount(0);
                    
                    for (AnalysisReport r : reports) {
                        tableModel.addRow(new Object[]{
                            r.getReportId() != null ? 
                                r.getReportId().toString().substring(0, 8) + "..." : "N/A",
                            formatStatus(r.getStatus()),
                            r.getFileFormat() != null ? r.getFileFormat().toUpperCase() : "N/A",
                            r.getFileSizeFormatted() != null ? r.getFileSizeFormatted() : "N/A",
                            r.getCreatedAt() != null ? r.getCreatedAt().toString() : "N/A"
                        });
                    }
                    
                    if (!reports.isEmpty()) {
                        AnalysisReport r = reports.get(0);
                        detailsArea.setText(
                            "Статус: " + formatStatus(r.getStatus()) + "\n" +
                            "Файл: " + r.getFileName() + "\n" +
                            "Формат: " + r.getFileFormat() + "\n" +
                            "Размер: " + r.getFileSizeFormatted() + "\n" +
                            "Комментарий: " + r.getComment() + "\n" +
                            "Замечания: " + r.getIssues()
                        );
                    }
                    
                } catch (Exception e) {
                    detailsArea.setText("Ошибка: " + e.getMessage());
                } finally {
                    searchBtn.setEnabled(true);
                    searchBtn.setText("Получить отчёт");
                }
            }
        }.execute();
    }
    
    private void showDetails(int row) {
        // Можно добавить дополнительную логику
    }
    
    private String formatStatus(String status) {
        if (status == null) return "Неизвестно";
        return switch (status) {
            case "ACCEPTED" -> "✓ Принято";
            case "NEEDS_REWORK" -> "⚠ Требуется доработка";
            case "ERROR" -> "✗ Ошибка";
            case "PENDING" -> "⏳ Ожидает";
            default -> status;
        };
    }
    
    private void clearSearch() {
        workIdField.setText("");
        tableModel.setRowCount(0);
        detailsArea.setText("");
    }
}
