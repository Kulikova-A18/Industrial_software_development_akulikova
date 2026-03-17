package com.tigerbank.gui;

import com.tigerbank.gui.components.*;
import com.tigerbank.repository.*;
import com.tigerbank.service.*;
import javax.swing.*;

public class MainFrame extends JFrame {
    private AccountService accountService;
    private CategoryService categoryService;
    private OperationService operationService;
    private AnalyticsService analyticsService;

    private JTabbedPane tabbedPane;
    private AccountsPanel accountsPanel;
    private CategoriesPanel categoriesPanel;
    private OperationsPanel operationsPanel;
    private AnalyticsPanel analyticsPanel;

    public MainFrame() {
        // Инициализация сервисов
        initializeServices();
        initializeUI();
        loadInitialData();
    }

    private void initializeServices() {
        // Инициализация репозиториев
        OperationRepository operationRepo = new OperationRepository(null, null);
        BankAccountRepository accountRepo = new BankAccountRepository(operationRepo);
        CategoryRepository categoryRepo = new CategoryRepository(operationRepo);

        // Устанавливаем связи
        operationRepo = new OperationRepository(accountRepo, categoryRepo);

        // Инициализация сервисов
        accountService = new AccountService(accountRepo, operationRepo);
        categoryService = new CategoryService(categoryRepo);
        operationService = new OperationService(operationRepo, accountService, categoryService);
        analyticsService = new AnalyticsService(operationService, categoryService, accountService);
    }

    private void initializeUI() {
        setTitle("ТигрБанк - Учет финансов");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();

        accountsPanel = new AccountsPanel(accountService, operationService);
        categoriesPanel = new CategoriesPanel(categoryService, operationService);
        operationsPanel = new OperationsPanel(operationService, accountService, categoryService);
        analyticsPanel = new AnalyticsPanel(analyticsService, accountService);

        tabbedPane.addTab("Счета", accountsPanel);
        tabbedPane.addTab("Категории", categoriesPanel);
        tabbedPane.addTab("Операции", operationsPanel);
        tabbedPane.addTab("Аналитика", analyticsPanel);

        add(tabbedPane);
        createMenuBar();
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // Меню "Файл"
        JMenu fileMenu = new JMenu("Файл");

        // Экспорт
        JMenu exportMenu = new JMenu("Экспорт");
        JMenuItem exportCsvItem = new JMenuItem("Экспорт в CSV");
        exportCsvItem.addActionListener(e -> exportDataToCsv());
        exportMenu.add(exportCsvItem);

        JMenuItem exportJsonItem = new JMenuItem("Экспорт в JSON");
        exportJsonItem.addActionListener(e -> exportDataToJson());
        exportMenu.add(exportJsonItem);

        fileMenu.add(exportMenu);
        fileMenu.addSeparator();

        JMenuItem exitItem = new JMenuItem("Выход");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);

        // Меню "Настройки"
        JMenu settingsMenu = new JMenu("Настройки");

        JMenuItem loadDefaultsItem = new JMenuItem("Загрузить стандартные категории");
        loadDefaultsItem.addActionListener(e -> loadDefaultCategories());
        settingsMenu.add(loadDefaultsItem);

        // Меню "Помощь"
        JMenu helpMenu = new JMenu("Помощь");
        JMenuItem aboutItem = new JMenuItem("О программе");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(settingsMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);
    }

    private void loadInitialData() {
        // Загружаем стандартные категории
        categoryService.loadDefaultCategories();

        // Создаем начальные счета
        accountService.createAccount("Основной счет");
        accountService.createAccount("Накопительный счет");
        accountService.createAccount("Кредитная карта");

        refreshAllPanels();
    }

    private void refreshAllPanels() {
        accountsPanel.refreshTable();
        categoriesPanel.refreshTable();
        operationsPanel.refreshTable();
        analyticsPanel.updateAnalytics();
    }

    private void exportDataToCsv() {
        String fileName = JOptionPane.showInputDialog(this,
                "Введите базовое имя для файлов (без расширения):",
                "tigerbank_finances_" + System.currentTimeMillis());

        if (fileName != null && !fileName.trim().isEmpty()) {
            try {
                FileExporter fileExporter = new FileExporter();
                fileExporter.exportAllToCsv(fileName.trim(),
                        accountService.getAllAccounts(),
                        categoryService.getAllCategories(),
                        operationService.getAllOperations());

                JOptionPane.showMessageDialog(this,
                        "Данные успешно экспортированы в CSV файлы:\n" +
                                "- " + fileName + "_accounts.csv\n" +
                                "- " + fileName + "_categories.csv\n" +
                                "- " + fileName + "_operations.csv",
                        "Экспорт завершен", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Ошибка экспорта: " + e.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportDataToJson() {
        String fileName = JOptionPane.showInputDialog(this,
                "Введите базовое имя для файлов (без расширения):",
                "tigerbank_finances_" + System.currentTimeMillis());

        if (fileName != null && !fileName.trim().isEmpty()) {
            try {
                FileExporter fileExporter = new FileExporter();
                fileExporter.exportAllToJson(fileName.trim(),
                        accountService.getAllAccounts(),
                        categoryService.getAllCategories(),
                        operationService.getAllOperations());

                JOptionPane.showMessageDialog(this,
                        "Данные успешно экспортированы в JSON файлы:\n" +
                                "- " + fileName + "_accounts.json\n" +
                                "- " + fileName + "_categories.json\n" +
                                "- " + fileName + "_operations.json",
                        "Экспорт завершен", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Ошибка экспорта: " + e.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadDefaultCategories() {
        int result = JOptionPane.showConfirmDialog(this,
                "Загрузить стандартные категории?\n" +
                        "Существующие категории не будут удалены.",
                "Загрузка стандартных категорий",
                JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            categoryService.loadDefaultCategories();
            refreshAllPanels();
            JOptionPane.showMessageDialog(this,
                    "Стандартные категории загружены успешно!",
                    "Успех", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showAboutDialog() {
        JOptionPane.showMessageDialog(this,
                "ТигрБанк - Учет финансов\n" +
                        "Версия 2.0\n\n" +
                        "Функции:\n" +
                        "1. Управление счетами\n" +
                        "2. Категории доходов и расходов\n" +
                        "3. Учет операций\n" +
                        "4. Финансовая аналитика\n" +
                        "5. Экспорт данных\n\n" +
                        "Все данные связаны через ID для целостности системы.",
                "О программе", JOptionPane.INFORMATION_MESSAGE);
    }
}