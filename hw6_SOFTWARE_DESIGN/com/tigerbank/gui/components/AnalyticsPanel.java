package com.tigerbank.gui.components;

import com.tigerbank.service.AccountService;
import com.tigerbank.service.AnalyticsService;
import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

public class AnalyticsPanel extends JPanel {
    private final AnalyticsService analyticsService;
    private final AccountService accountService;

    private JLabel totalBalanceLabel;
    private JLabel totalIncomeLabel;
    private JLabel totalExpenseLabel;
    private JTextArea analyticsArea;

    public AnalyticsPanel(AnalyticsService analyticsService, AccountService accountService) {
        this.analyticsService = analyticsService;
        this.accountService = accountService;

        setLayout(new BorderLayout());
        initializeUI();
    }

    private void initializeUI() {
        JPanel statsPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        statsPanel.setBorder(BorderFactory.createTitledBorder("Общая статистика"));

        totalBalanceLabel = new JLabel("0.00", SwingConstants.RIGHT);
        totalIncomeLabel = new JLabel("0.00", SwingConstants.RIGHT);
        totalExpenseLabel = new JLabel("0.00", SwingConstants.RIGHT);
        JLabel accountsCountLabel = new JLabel("0", SwingConstants.RIGHT);

        statsPanel.add(new JLabel("Общий баланс:"));
        statsPanel.add(totalBalanceLabel);
        statsPanel.add(new JLabel("Общий доход:"));
        statsPanel.add(totalIncomeLabel);
        statsPanel.add(new JLabel("Общий расход:"));
        statsPanel.add(totalExpenseLabel);
        statsPanel.add(new JLabel("Количество счетов:"));
        statsPanel.add(accountsCountLabel);

        analyticsArea = new JTextArea(15, 40);
        analyticsArea.setEditable(false);
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

        buttonPanel.add(refreshButton);
        buttonPanel.add(periodButton);
        buttonPanel.add(fullReportButton);
        buttonPanel.add(categoryStatsButton);

        add(statsPanel, BorderLayout.NORTH);
        add(analyticsScroll, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        updateAnalytics();
    }

    public void updateAnalytics() {
        // Простая реализация для начала
        BigDecimal totalBalance = accountService.calculateTotalBalance();
        totalBalanceLabel.setText(String.format("%.2f", totalBalance));

        // Можно добавить больше логики позже
        analyticsArea.setText("Аналитика будет отображена здесь после добавления операций.");
    }

    private void showPeriodAnalyticsDialog() {
        JOptionPane.showMessageDialog(this,
                "Функция анализа за период будет реализована в следующей версии",
                "Информация", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showFullAnalyticsReport() {
        JOptionPane.showMessageDialog(this,
                "Функция полного отчета будет реализована в следующей версии",
                "Информация", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showCategoryStatisticsDialog() {
        JOptionPane.showMessageDialog(this,
                "Функция статистики по категориям будет реализована в следующей версии",
                "Информация", JOptionPane.INFORMATION_MESSAGE);
    }
}