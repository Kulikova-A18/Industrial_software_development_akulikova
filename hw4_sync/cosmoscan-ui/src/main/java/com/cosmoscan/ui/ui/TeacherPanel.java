package com.cosmoscan.ui.ui;

import com.cosmoscan.ui.model.AnalysisReport;
import com.cosmoscan.ui.service.ApiService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@Slf4j
public class TeacherPanel extends JPanel {
    
    private final ApiService apiService;
    private final ObjectMapper mapper;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    
    private JTextField workIdField;
    private JButton searchBtn;
    private JTable reportTable;
    private DefaultTableModel tableModel;
    private JTextArea detailsArea;
    private JLabel statusLabel;
    
    public TeacherPanel(ApiService apiService) {
        this.apiService = apiService;
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        initComponents();
    }
    
    private void initComponents() {
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setLayout(new BorderLayout(10, 10));
        
        // Панель поиска
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(new TitledBorder("Поиск отчёта"));
        
        searchPanel.add(new JLabel("ID работы:"));
        workIdField = new JTextField(36);
        workIdField.setToolTipText("Введите UUID работы");
        searchPanel.add(workIdField);
        
        searchBtn = new JButton("Получить отчёт");
        searchBtn.addActionListener(e -> searchReport());
        searchPanel.add(searchBtn);
        
        JButton clearBtn = new JButton("Очистить");
        clearBtn.addActionListener(e -> clearForm());
        searchPanel.add(clearBtn);
        
        add(searchPanel, BorderLayout.NORTH);
        
        // Таблица
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(new TitledBorder("Результаты проверки"));
        
        String[] columns = {"ID отчёта", "Статус", "Формат", "Размер", "Дата"};
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
                    showReportDetails(row);
                }
            }
        });
        
        tablePanel.add(new JScrollPane(reportTable), BorderLayout.CENTER);
        add(tablePanel, BorderLayout.CENTER);
        
        // Детали
        JPanel detailsPanel = new JPanel(new BorderLayout());
        detailsPanel.setBorder(new TitledBorder("Детали отчёта"));
        
        detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        detailsArea.setRows(10);
        
        detailsPanel.add(new JScrollPane(detailsArea), BorderLayout.CENTER);
        add(detailsPanel, BorderLayout.SOUTH);
        
        // Статус
        statusLabel = new JLabel("Готов к поиску");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(statusLabel, BorderLayout.SOUTH);
    }
    
    private void searchReport() {
        String workId = workIdField.getText().trim();
        if (workId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Введите ID работы", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        searchBtn.setEnabled(false);
        searchBtn.setText("Загрузка...");
        statusLabel.setText("Загрузка отчётов...");
        tableModel.setRowCount(0);
        detailsArea.setText("");
        
        new SwingWorker<List<AnalysisReport>, Void>() {
            @Override
            protected List<AnalysisReport> doInBackground() throws Exception {
                String response = apiService.getReports(workId);
                if (response == null || response.isEmpty() || "[]".equals(response)) {
                    return List.of();
                }
                return mapper.readValue(response, new TypeReference<List<AnalysisReport>>() {});
            }
            
            @Override
            protected void done() {
                try {
                    List<AnalysisReport> reports = get();
                    
                    if (reports.isEmpty()) {
                        JOptionPane.showMessageDialog(TeacherPanel.this,
                            "Отчёты не найдены", "Информация", JOptionPane.INFORMATION_MESSAGE);
                        statusLabel.setText("Отчёты не найдены");
                        return;
                    }
                    
                    for (AnalysisReport report : reports) {
                        tableModel.addRow(new Object[]{
                            report.getReportId() != null ? report.getReportId().toString().substring(0, 8) + "..." : "N/A",
                            formatStatus(report.getStatus()),
                            report.getFileFormat() != null ? report.getFileFormat().toUpperCase() : "N/A",
                            report.getFileSizeFormatted(),
                            report.getCreatedAt() != null ? report.getCreatedAt().format(dateFormatter) : "N/A"
                        });
                    }
                    
                    statusLabel.setText("Найдено отчётов: " + reports.size());
                    
                    if (!reports.isEmpty()) {
                        showReportDetails(reports.get(0));
                    }
                    
                } catch (Exception e) {
                    log.error("Ошибка: {}", e.getMessage());
                    statusLabel.setText("Ошибка: " + e.getMessage());
                    JOptionPane.showMessageDialog(TeacherPanel.this,
                        "Ошибка загрузки:\n" + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                } finally {
                    searchBtn.setEnabled(true);
                    searchBtn.setText("Получить отчёт");
                }
            }
        }.execute();
    }
    
    private void showReportDetails(int row) {
        String workId = workIdField.getText().trim();
        if (workId.isEmpty()) return;
        
        new SwingWorker<List<AnalysisReport>, Void>() {
            @Override
            protected List<AnalysisReport> doInBackground() throws Exception {
                String response = apiService.getReports(workId);
                return mapper.readValue(response, new TypeReference<List<AnalysisReport>>() {});
            }
            
            @Override
            protected void done() {
                try {
                    List<AnalysisReport> reports = get();
                    if (row < reports.size()) {
                        showReportDetails(reports.get(row));
                    }
                } catch (Exception e) {
                    log.error("Ошибка: {}", e.getMessage());
                }
            }
        }.execute();
    }
    
    private void showReportDetails(AnalysisReport report) {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════════════\n");
        sb.append("                    ДЕТАЛИ ОТЧЁТА                           \n");
        sb.append("═══════════════════════════════════════════════════════════\n\n");
        
        sb.append("📋 ОСНОВНАЯ ИНФОРМАЦИЯ\n");
        sb.append("───────────────────────────────────────────────────────────\n");
        sb.append("  ID отчёта:     ").append(report.getReportId()).append("\n");
        sb.append("  ID работы:     ").append(report.getWorkId()).append("\n");
        sb.append("  Статус:        ").append(formatStatus(report.getStatus())).append("\n");
        sb.append("  Дата:          ").append(report.getCreatedAt() != null ? 
            report.getCreatedAt().format(dateFormatter) : "N/A").append("\n\n");
        
        sb.append("📄 ИНФОРМАЦИЯ О ФАЙЛЕ\n");
        sb.append("───────────────────────────────────────────────────────────\n");
        sb.append("  Имя файла:     ").append(report.getFileName() != null ? report.getFileName() : "N/A").append("\n");
        sb.append("  Размер:        ").append(report.getFileSizeFormatted()).append("\n");
        sb.append("  Формат:        ").append(report.getFileFormat() != null ? 
            report.getFileFormat().toUpperCase() : "N/A").append("\n\n");
        
        sb.append("✅ РЕЗУЛЬТАТЫ ПРОВЕРКИ\n");
        sb.append("───────────────────────────────────────────────────────────\n");
        sb.append("  Формат валиден: ").append(report.getIsValidFormat() != null && report.getIsValidFormat() ? 
            "✓ Да" : "✗ Нет").append("\n");
        sb.append("  Размер валиден: ").append(report.getIsValidSize() != null && report.getIsValidSize() ? 
            "✓ Да" : "✗ Нет").append("\n");
        sb.append("  Длительность:   ").append(report.getAnalysisDurationMs() != null ? 
            report.getAnalysisDurationMs() + " мс" : "N/A").append("\n\n");
        
        sb.append("💬 КОММЕНТАРИЙ\n");
        sb.append("───────────────────────────────────────────────────────────\n");
        sb.append("  ").append(report.getComment() != null ? report.getComment() : "Нет комментария").append("\n\n");
        
        sb.append("⚠ ЗАМЕЧАНИЯ\n");
        sb.append("───────────────────────────────────────────────────────────\n");
        sb.append("  ").append(report.getIssues() != null ? report.getIssues() : "Нет замечаний").append("\n");
        
        detailsArea.setText(sb.toString());
        detailsArea.setCaretPosition(0);
    }
    
    private String formatStatus(String status) {
        if (status == null) return "Неизвестно";
        switch (status.toUpperCase()) {
            case "ACCEPTED": return "✓ Принято";
            case "NEEDS_REWORK": return "⚠ Требуется доработка";
            case "ERROR": return "✗ Ошибка";
            case "PENDING": return "⏳ Ожидает";
            default: return status;
        }
    }
    
    private void clearForm() {
        workIdField.setText("");
        tableModel.setRowCount(0);
        detailsArea.setText("");
        statusLabel.setText("Готов к поиску");
    }
}
