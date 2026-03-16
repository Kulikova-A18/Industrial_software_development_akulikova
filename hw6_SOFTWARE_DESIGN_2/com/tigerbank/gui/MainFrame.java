package com.tigerbank.gui;

import com.tigerbank.command.CommandExecutor;
import com.tigerbank.di.DIContainer;
import com.tigerbank.exporter.CsvExporter;
import com.tigerbank.exporter.JsonExporter;
import com.tigerbank.facade.AnalyticsFacade;
import com.tigerbank.gui.components.*;
import com.tigerbank.importer.CsvImporter;
import com.tigerbank.importer.JsonImporter;
import com.tigerbank.service.*;

import javax.swing.*;
import java.io.File;

public class MainFrame extends JFrame {
    private final DIContainer container;

    private JTabbedPane tabbedPane;
    private AccountsPanel accountsPanel;
    private CategoriesPanel categoriesPanel;
    private OperationsPanel operationsPanel;
    private AnalyticsPanel analyticsPanel;

    public MainFrame(DIContainer container) {
        this.container = container;
        initializeUI();
        loadInitialData();
    }

    private void initializeUI() {
        setTitle("ТигрБанк - Учет финансов");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);

        AccountService accountService = container.resolve(AccountService.class);
        CategoryService categoryService = container.resolve(CategoryService.class);
        OperationService operationService = container.resolve(OperationService.class);
        AnalyticsFacade analyticsFacade = container.resolve(AnalyticsFacade.class);
        CommandExecutor commandExecutor = new CommandExecutor();

        tabbedPane = new JTabbedPane();

        accountsPanel = new AccountsPanel(accountService, operationService, commandExecutor);
        categoriesPanel = new CategoriesPanel(categoryService, operationService, commandExecutor);
        operationsPanel = new OperationsPanel(operationService, accountService, categoryService, commandExecutor);
        analyticsPanel = new AnalyticsPanel(analyticsFacade, accountService);

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

        // Импорт
        JMenu importMenu = new JMenu("Импорт");
        JMenuItem importCsvItem = new JMenuItem("Импорт из CSV");
        importCsvItem.addActionListener(e -> importDataFromCsv());
        importMenu.add(importCsvItem);

        JMenuItem importJsonItem = new JMenuItem("Импорт из JSON");
        importJsonItem.addActionListener(e -> importDataFromJson());
        importMenu.add(importJsonItem);

        fileMenu.add(importMenu);
        fileMenu.addSeparator();

        JMenuItem exitItem = new JMenuItem("Выход");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);

        // Меню "Правка"
        JMenu editMenu = new JMenu("Правка");
        JMenuItem undoItem = new JMenuItem("Отменить (Ctrl+Z)");
        undoItem.addActionListener(e -> {
            CommandExecutor executor = new CommandExecutor();
            executor.undo();
            refreshAllPanels();
        });
        editMenu.add(undoItem);

        JMenuItem redoItem = new JMenuItem("Повторить (Ctrl+Y)");
        redoItem.addActionListener(e -> {
            CommandExecutor executor = new CommandExecutor();
            executor.redo();
            refreshAllPanels();
        });
        editMenu.add(redoItem);

        // Меню "Настройки"
        JMenu settingsMenu = new JMenu("Настройки");
        JMenuItem recalcBalancesItem = new JMenuItem("Пересчитать балансы");
        recalcBalancesItem.addActionListener(e -> {
            AnalyticsFacade facade = container.resolve(AnalyticsFacade.class);
            facade.recalculateAllBalances();
            refreshAllPanels();
            JOptionPane.showMessageDialog(this, "Балансы пересчитаны успешно!");
        });
        settingsMenu.add(recalcBalancesItem);

        settingsMenu.addSeparator();

        JMenuItem loadDefaultsItem = new JMenuItem("Загрузить стандартные категории");
        loadDefaultsItem.addActionListener(e -> loadDefaultCategories());
        settingsMenu.add(loadDefaultsItem);

        // Меню "Помощь"
        JMenu helpMenu = new JMenu("Помощь");
        JMenuItem aboutItem = new JMenuItem("О программе");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(settingsMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);
    }

    private void loadInitialData() {
        CategoryService categoryService = container.resolve(CategoryService.class);
        AccountService accountService = container.resolve(AccountService.class);

        categoryService.loadDefaultCategories();

        if (accountService.getAllAccounts().isEmpty()) {
            accountService.createAccount("Основной счет");
            accountService.createAccount("Накопительный счет");
            accountService.createAccount("Кредитная карта");
        }

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
                CsvExporter exporter = container.resolve(CsvExporter.class);
                AccountService accountService = container.resolve(AccountService.class);
                CategoryService categoryService = container.resolve(CategoryService.class);
                OperationService operationService = container.resolve(OperationService.class);

                exporter.exportAll(
                        accountService.getAllAccounts(),
                        categoryService.getAllCategories(),
                        operationService.getAllOperations(),
                        fileName.trim());

                JOptionPane.showMessageDialog(this,
                        "Данные успешно экспортированы в CSV файлы",
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
                JsonExporter exporter = container.resolve(JsonExporter.class);
                AccountService accountService = container.resolve(AccountService.class);
                CategoryService categoryService = container.resolve(CategoryService.class);
                OperationService operationService = container.resolve(OperationService.class);

                exporter.exportAll(
                        accountService.getAllAccounts(),
                        categoryService.getAllCategories(),
                        operationService.getAllOperations(),
                        fileName.trim());

                JOptionPane.showMessageDialog(this,
                        "Данные успешно экспортированы в JSON файлы",
                        "Экспорт завершен", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Ошибка экспорта: " + e.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void importDataFromCsv() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Выберите CSV файл для импорта");

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            try {
                AccountService accountService = container.resolve(AccountService.class);
                CategoryService categoryService = container.resolve(CategoryService.class);
                OperationService operationService = container.resolve(OperationService.class);

                CsvImporter importer = new CsvImporter(accountService, categoryService, operationService);
                importer.importData(file.getAbsolutePath());

                refreshAllPanels();
                JOptionPane.showMessageDialog(this,
                        "Импорт из CSV выполнен успешно!",
                        "Успех", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Ошибка импорта: " + e.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void importDataFromJson() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Выберите JSON файл для импорта");

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            try {
                AccountService accountService = container.resolve(AccountService.class);
                CategoryService categoryService = container.resolve(CategoryService.class);
                OperationService operationService = container.resolve(OperationService.class);

                JsonImporter importer = new JsonImporter(accountService, categoryService, operationService);
                importer.importData(file.getAbsolutePath());

                refreshAllPanels();
                JOptionPane.showMessageDialog(this,
                        "Импорт из JSON выполнен успешно!",
                        "Успех", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Ошибка импорта: " + e.getMessage(),
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
            CategoryService categoryService = container.resolve(CategoryService.class);
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
                        "Версия 3.0\n\n" +
                        "Реализованные паттерны:\n" +
                        "• Фабричный метод (DomainFactory)\n" +
                        "• Фасад (AnalyticsFacade)\n" +
                        "• Команда (Command, CommandExecutor)\n" +
                        "• Шаблонный метод (DataImporter)\n\n" +
                        "• DI-контейнер (DIContainer)\n" +
                        "• SOLID и GRASP принципы\n\n" +
                        "Все данные связаны через ID для целостности системы.",
                "О программе", JOptionPane.INFORMATION_MESSAGE);
    }
}