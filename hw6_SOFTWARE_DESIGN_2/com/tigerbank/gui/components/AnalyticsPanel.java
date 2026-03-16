package com.tigerbank.gui.components;

import com.tigerbank.facade.AnalyticsFacade;
import com.tigerbank.service.AccountService;
import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class AnalyticsPanel extends JPanel {
    private final AnalyticsFacade analyticsFacade;
    private final AccountService accountService;

    private JLabel totalBalanceLabel;
    private JLabel totalIncomeLabel;
    private JLabel totalExpenseLabel;
    private JLabel accountsCountLabel;
    private JLabel operationsCountLabel;
    private JTextArea analyticsArea;

    public AnalyticsPanel(AnalyticsFacade analyticsFacade, AccountService accountService) {
        this.analyticsFacade = analyticsFacade;
        this.accountService = accountService;

        setLayout(new BorderLayout());
        initializeUI();
    }

    private void initializeUI() {
        JPanel statsPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        statsPanel.setBorder(BorderFactory.createTitledBorder("Общая статистика"));

        totalBalanceLabel = new JLabel("0.00", SwingConstants.RIGHT);
        totalIncomeLabel = new JLabel("0.00", SwingConstants.RIGHT);
        totalExpenseLabel = new JLabel("0.00", SwingConstants.RIGHT);
        accountsCountLabel = new JLabel("0", SwingConstants.RIGHT);
        operationsCountLabel = new JLabel("0", SwingConstants.RIGHT);

        statsPanel.add(new JLabel("Общий баланс:"));
        statsPanel.add(totalBalanceLabel);
        statsPanel.add(new JLabel("Общий доход (мес):"));
        statsPanel.add(totalIncomeLabel);
        statsPanel.add(new JLabel("Общий расход (мес):"));
        statsPanel.add(totalExpenseLabel);
        statsPanel.add(new JLabel("Количество счетов:"));
        statsPanel.add(accountsCountLabel);
        statsPanel.add(new JLabel("Количество операций:"));
        statsPanel.add(operationsCountLabel);

        analyticsArea = new JTextArea(15, 40);
        analyticsArea.setEditable(false);
        analyticsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane analyticsScroll = new JScrollPane(analyticsArea);
        analyticsScroll.setBorder(BorderFactory.createTitledBorder("Детальная аналитика"));

        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton refreshButton = new JButton("Обновить аналитику");
        refreshButton.addActionListener(e -> updateAnalytics());

        JButton periodButton = new JButton("Анализ за период");
        periodButton.addActionListener(e -> showPeriodAnalyticsDialog());

        JButton fullReportButton = new JButton("Полный отчет");
        fullReportButton.addActionListener(e -> showFullAnalyticsReport());

        JButton categoryStatsButton = new JButton("Статистика по категориям");
        categoryStatsButton.addActionListener(e -> showCategoryStatisticsDialog());

        JButton recalcButton = new JButton("Пересчитать балансы");
        recalcButton.addActionListener(e -> recalculateBalances());

        buttonPanel.add(refreshButton);
        buttonPanel.add(periodButton);
        buttonPanel.add(fullReportButton);
        buttonPanel.add(categoryStatsButton);
        buttonPanel.add(recalcButton);

        add(statsPanel, BorderLayout.NORTH);
        add(analyticsScroll, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        updateAnalytics();
    }

    public void updateAnalytics() {
        Map<String, Object> stats = analyticsFacade.getDashboardStats();

        totalBalanceLabel.setText(String.format("%.2f", stats.get("totalBalance")));
        totalIncomeLabel.setText(String.format("%.2f", stats.get("totalIncome")));
        totalExpenseLabel.setText(String.format("%.2f", stats.get("totalExpense")));
        accountsCountLabel.setText(stats.get("accountsCount").toString());
        operationsCountLabel.setText(stats.get("operationsCount").toString());

        StringBuilder sb = new StringBuilder();
        sb.append("=== СТАТИСТИКА ПО КАТЕГОРИЯМ (последний месяц) ===\n\n");

        LocalDateTime start = LocalDateTime.now().minusMonths(1);
        LocalDateTime end = LocalDateTime.now();

        sb.append("ДОХОДЫ ПО КАТЕГОРИЯМ:\n");
        Map<String, BigDecimal> incomeByCat = analyticsFacade.getIncomeGroupedByCategory(start, end);
        if (incomeByCat.isEmpty()) {
            sb.append("  Нет данных\n");
        } else {
            incomeByCat.forEach((cat, amt) -> sb.append(String.format("  %s: %.2f\n", cat, amt)));
        }

        sb.append("\nРАСХОДЫ ПО КАТЕГОРИЯМ:\n");
        Map<String, BigDecimal> expenseByCat = analyticsFacade.getExpenseGroupedByCategory(start, end);
        if (expenseByCat.isEmpty()) {
            sb.append("  Нет данных\n");
        } else {
            expenseByCat.forEach((cat, amt) -> sb.append(String.format("  %s: %.2f\n", cat, amt)));
        }

        analyticsArea.setText(sb.toString());
    }

    private void showPeriodAnalyticsDialog() {
        JTextField startField = new JTextField(10);
        JTextField endField = new JTextField(10);

        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.add(new JLabel("Дата начала (yyyy-MM-dd):"));
        panel.add(startField);
        panel.add(new JLabel("Дата окончания (yyyy-MM-dd):"));
        panel.add(endField);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Анализ за период", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime start = LocalDateTime.parse(startField.getText() + " 00:00:00", formatter);
                LocalDateTime end = LocalDateTime.parse(endField.getText() + " 23:59:59", formatter);

                BigDecimal diff = analyticsFacade.getBalanceDifference(start, end);
                Map<String, BigDecimal> income = analyticsFacade.getIncomeGroupedByCategory(start, end);
                Map<String, BigDecimal> expense = analyticsFacade.getExpenseGroupedByCategory(start, end);

                StringBuilder sb = new StringBuilder();
                sb.append("=== АНАЛИЗ ЗА ПЕРИОД ===\n");
                sb.append(String.format("С %s по %s\n\n", startField.getText(), endField.getText()));
                sb.append(String.format("Разница доходов и расходов: %.2f\n\n", diff));

                sb.append("Доходы по категориям:\n");
                income.forEach((cat, amt) -> sb.append(String.format("  %s: %.2f\n", cat, amt)));

                sb.append("\nРасходы по категориям:\n");
                expense.forEach((cat, amt) -> sb.append(String.format("  %s: %.2f\n", cat, amt)));

                JTextArea textArea = new JTextArea(sb.toString(), 15, 40);
                textArea.setEditable(false);
                JScrollPane scrollPane = new JScrollPane(textArea);

                JOptionPane.showMessageDialog(this, scrollPane,
                        "Результаты анализа", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Ошибка формата даты: " + e.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showFullAnalyticsReport() {
        LocalDateTime start = LocalDateTime.now().minusMonths(6);
        LocalDateTime end = LocalDateTime.now();

        String report = analyticsFacade.getFullAnalyticsReport(start, end);

        JTextArea textArea = new JTextArea(report, 25, 60);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(textArea);

        JOptionPane.showMessageDialog(this, scrollPane,
                "Полный финансовый отчет", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showCategoryStatisticsDialog() {
        String[] options = { "Доходы", "Расходы" };
        int choice = JOptionPane.showOptionDialog(this,
                "Выберите тип операций для статистики",
                "Статистика по категориям",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);

        if (choice == 0 || choice == 1) {
            LocalDateTime start = LocalDateTime.now().minusMonths(1);
            LocalDateTime end = LocalDateTime.now();

            Map<String, BigDecimal> stats = choice == 0
                    ? analyticsFacade.getIncomeGroupedByCategory(start, end)
                    : analyticsFacade.getExpenseGroupedByCategory(start, end);

            StringBuilder sb = new StringBuilder();
            sb.append(choice == 0 ? "ДОХОДЫ" : "РАСХОДЫ");
            sb.append(" ПО КАТЕГОРИЯМ (последний месяц):\n\n");

            if (stats.isEmpty()) {
                sb.append("Нет данных");
            } else {
                stats.forEach((cat, amt) -> sb.append(String.format("%s: %.2f\n", cat, amt)));
            }

            JTextArea textArea = new JTextArea(sb.toString(), 15, 30);
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);

            JOptionPane.showMessageDialog(this, scrollPane,
                    "Статистика по категориям", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void recalculateBalances() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Пересчитать балансы всех счетов на основе операций?",
                "Подтверждение", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            long startTime = System.nanoTime();
            analyticsFacade.recalculateAllBalances();
            long endTime = System.nanoTime();

            updateAnalytics();

            JOptionPane.showMessageDialog(this,
                    String.format("Балансы пересчитаны успешно!\nВремя выполнения: %.2f мс",
                            (endTime - startTime) / 1_000_000.0),
                    "Успех", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}